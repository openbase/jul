package org.openbase.rct.impl.rsb;

/*-
 * #%L
 * RCT
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import kotlin.Unit;
import org.openbase.jul.communication.config.CommunicatorConfig;
import org.openbase.jul.communication.iface.Communicator;
import org.openbase.jul.communication.iface.CommunicatorFactory;
import org.openbase.jul.communication.iface.Publisher;
import org.openbase.jul.communication.iface.Subscriber;
import org.openbase.jul.communication.mqtt.CommunicatorFactoryImpl;
import org.openbase.jul.communication.mqtt.DefaultCommunicatorConfig;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.type.processing.ScopeProcessor;
import org.openbase.jul.schedule.WatchDog;
import org.openbase.rct.*;
import org.openbase.rct.impl.TransformCommunicator;
import org.openbase.rct.impl.TransformListener;
import org.openbase.type.communication.EventType.Event;
import org.openbase.type.communication.EventType.EventOrBuilder;
import org.openbase.type.communication.ScopeType.Scope;
import org.openbase.type.communication.mqtt.PrimitiveType.Primitive;
import org.openbase.type.geometry.FrameTransformType.FrameTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransformCommunicatorImpl implements TransformCommunicator {

    private static final String PUBLISHER_ID = "PUBLISHER_ID";
    private static final String AUTHORITY = "AUTHORITY";
    private static final String STATIC_TRANSFORMATION = "STATIC_TRANSFORMATION";
    public static final Scope RCT_SCOPE_TRANSFORM = ScopeProcessor.generateScope("/rct/transform");
    public static final Scope RCT_SCOPE_SYNC = ScopeProcessor.generateScope("/rct/sync");
    private static final String RCT_SCOPE_SUFFIX_STATIC = "static";
    private static final String RCT_SCOPE_SUFFIX_DYNAMIC = "dynamic";
    private static final String RCT_SCOPE_SEPARATOR = "/";
    public static final Scope RCT_SCOPE_TRANSFORM_STATIC = ScopeProcessor.concat(
            RCT_SCOPE_TRANSFORM,
            ScopeProcessor.generateScope(RCT_SCOPE_SEPARATOR + RCT_SCOPE_SUFFIX_STATIC
            ));
    public static final Scope RCT_SCOPE_TRANSFORM_DYNAMIC = ScopeProcessor.concat(
            RCT_SCOPE_TRANSFORM,
            ScopeProcessor.generateScope(RCT_SCOPE_SEPARATOR + RCT_SCOPE_SUFFIX_DYNAMIC
            ));
    private static final Logger log = LoggerFactory.getLogger(TransformCommunicatorImpl.class);
    private final Set<TransformListener> subscribers = new HashSet<>();
    private final Map<String, Transform> sendCacheDynamic = new HashMap<>();
    private final Map<String, Transform> sendCacheStatic = new HashMap<>();
    private final Object lock = new Object();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final String authority;

    private Publisher transformationPublisher;
    private Publisher publisherSync;

    private WatchDog rsbPublisherTransformWatchDog;
    private WatchDog rsbPublisherSyncWatchDog;
    private WatchDog staticTransformSubscriberWatchDog;
    private WatchDog dynamicTransformSubscriberWatchDog;
    private WatchDog rsbSubscriberSyncWatchDog;

    private final CommunicatorFactory factory = CommunicatorFactoryImpl.Companion.getInstance();
    private final CommunicatorConfig defaultCommunicatorConfig = DefaultCommunicatorConfig.Companion.getInstance();

    public TransformCommunicatorImpl(String authority) {
        this.authority = authority;
    }

    @Override
    public void init(final TransformerConfig conf) throws TransformerException {
        try {

            log.debug("init communication");

            this.transformationPublisher = factory.createPublisher(RCT_SCOPE_TRANSFORM, defaultCommunicatorConfig);
            this.publisherSync = factory.createPublisher(RCT_SCOPE_SYNC, defaultCommunicatorConfig);
            Subscriber staticTransformationSubscriber = factory.createSubscriber(RCT_SCOPE_TRANSFORM_STATIC, defaultCommunicatorConfig);
            Subscriber dynamicTransformationSubscriber = factory.createSubscriber(RCT_SCOPE_TRANSFORM_DYNAMIC, defaultCommunicatorConfig);
            Subscriber subscriberSync = factory.createSubscriber(RCT_SCOPE_SYNC, defaultCommunicatorConfig);

            this.rsbPublisherTransformWatchDog = new WatchDog(transformationPublisher, "RSBPublisherTransform");
            this.rsbPublisherSyncWatchDog = new WatchDog(publisherSync, "RSBPublisherSync");
            this.staticTransformSubscriberWatchDog = new WatchDog(staticTransformationSubscriber, "RSBSubscriberTransform");
            this.dynamicTransformSubscriberWatchDog = new WatchDog(dynamicTransformationSubscriber, "RSBSubscriberTransform");
            this.rsbSubscriberSyncWatchDog = new WatchDog(subscriberSync, "RSBSubscriberSync");

            staticTransformationSubscriber.registerDataHandler(this::transformCallback);
            dynamicTransformationSubscriber.registerDataHandler(this::transformCallback);
            subscriberSync.registerDataHandler(this::syncCallback);

            this.rsbPublisherTransformWatchDog.activate();
            this.rsbPublisherSyncWatchDog.activate();
            this.staticTransformSubscriberWatchDog.activate();
            this.dynamicTransformSubscriberWatchDog.activate();
            this.rsbSubscriberSyncWatchDog.activate();

            this.rsbPublisherTransformWatchDog.waitForServiceActivation();
            this.rsbPublisherSyncWatchDog.waitForServiceActivation();
            this.staticTransformSubscriberWatchDog.waitForServiceActivation();
            this.dynamicTransformSubscriberWatchDog.waitForServiceActivation();
            this.rsbSubscriberSyncWatchDog.waitForServiceActivation();

            this.requestSync();

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (CouldNotPerformException ex) {
            throw new TransformerException("Can not initialize rsb communicator.", ex);
        }
    }

    public void requestSync() throws TransformerException {
        try {
            if (publisherSync == null || !publisherSync.isActive()) {
                throw new TransformerException("Rsb communicator is not initialized.");
            }

            log.debug("Sending sync request trigger from id " + publisherSync.getId());

            // trigger other instances to send transforms
            publisherSync.publish(getEventBuilder(publisherSync).build(), true);
        } catch (CouldNotPerformException ex) {
            throw new TransformerException("Can not send transforms!", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private Event.Builder getEventBuilder(Communicator communicator) {
        return  Event.newBuilder()
                .putHeader(
                        PUBLISHER_ID,
                        Any.pack(Primitive
                                .newBuilder()
                                .setString(communicator.getId().toString())
                                .build())
                );
    }

    private Boolean isPublishedBy(Communicator communicator, Event event) {
        try {
            return event
                    .getHeaderMap()
                    .get(PUBLISHER_ID)
                    .unpack(Primitive.class)
                    .getString()
                    .equals(communicator.getId().toString());
        } catch (InvalidProtocolBufferException e) {
            return false;
        }
    }

    private Event.Builder setStatic(Boolean value, Event.Builder builder) {
            return builder.putHeader(
                STATIC_TRANSFORMATION,
                Any.pack(Primitive
                        .newBuilder()
                        .setBoolean(value)
                        .build()
                )
            );
    }

    private Boolean isStatic(EventOrBuilder eventOrBuilder) {
        try {
            return eventOrBuilder.getHeaderMap().get(STATIC_TRANSFORMATION).unpack(Primitive.class).getBoolean();
        } catch (InvalidProtocolBufferException e) {
            ExceptionPrinter.printHistory("Static transformation flag missing!", e, log);
            return true;
        }
    }

    private Event.Builder setAuthority(String authority, Event.Builder builder) {

        // use default if authority is empty
        if (authority == null || authority.isEmpty()) {
            authority = this.authority;
        }

        return builder.putHeader(
                AUTHORITY,
                Any.pack(Primitive
                        .newBuilder()
                        .setString(authority)
                        .build()));
    }

    private String getAuthority(EventOrBuilder eventOrBuilder) {
        try {
            return eventOrBuilder.getHeaderMap().get(AUTHORITY).unpack(Primitive.class).getString();
        } catch (InvalidProtocolBufferException e) {
            ExceptionPrinter.printHistory("Transformation fram authority not defined!", e, log);
            return "?";
        }
    }

    private Event.Builder setPayload(Transform transform, Event.Builder builder) {
        return builder.setPayload(Any.pack(FrameTransformProcessor.convert(transform)));
    }

    private Transform getPayload(EventOrBuilder eventOrBuilder) throws InvalidProtocolBufferException {
        return FrameTransformProcessor.convert(eventOrBuilder.getPayload().unpack(FrameTransform.class));
    }

    @Override
    public void sendTransform(final Transform transform, final TransformType type) throws TransformerException {
        try {
            if (transformationPublisher == null || !transformationPublisher.isActive()) {
                throw new TransformerException("RSB interface is not initialized!");
            }

            String cacheKey = transform.getFrameParent() + transform.getFrameChild();

            log.debug("Publishing transform from " + transformationPublisher.getId());

            synchronized (lock) {
                Event.Builder eventBuilder = getEventBuilder(transformationPublisher);
                setPayload(transform, eventBuilder);
                setAuthority(transform.getAuthority(), eventBuilder);

                Scope scope;
                switch (type) {
                    case STATIC:
                        if (transform.equalsWithoutTime(sendCacheStatic.get(cacheKey))) {
                            if (transform.equalsWithoutTime(GlobalTransformReceiver.getInstance().lookupTransform(transform.getFrameParent(), transform.getFrameChild(), System.currentTimeMillis()))) {
                                log.debug("Publishing static transform from " + transformationPublisher.getId() + " done because Transformation[" + cacheKey + "] already known.");
                                // we are done if transformation is already known
                                return;
                            }
                            log.warn("Publishing static transform from " + transformationPublisher.getId() + " again because Transformation[" + cacheKey + "] sync failed.");
                        }
                        sendCacheStatic.put(cacheKey, transform);
                        scope = RCT_SCOPE_TRANSFORM_STATIC;
                        setStatic(true, eventBuilder);
                        break;
                    case DYNAMIC:
                        if (transform.equals(sendCacheDynamic.get(cacheKey))) {
                            if (transform.equalsWithoutTime(GlobalTransformReceiver.getInstance().lookupTransform(transform.getFrameParent(), transform.getFrameChild(), System.currentTimeMillis()))) {
                                log.debug("Publishing dynamic transform from " + transformationPublisher.getId() + " done because Transformation[" + cacheKey + "] already known.");
                                // we are done if transformation is already known
                                return;
                            }
                            log.warn("Publishing dynamic transform from " + transformationPublisher.getId() + " again because Transformation[" + cacheKey + "] sync failed.");
                            return;
                        }
                        sendCacheDynamic.put(cacheKey, transform);
                        scope = RCT_SCOPE_TRANSFORM_DYNAMIC;
                        setStatic(false, eventBuilder);
                        break;
                    default:
                        throw new TransformerException("Unknown TransformType: " + type.name());
                }

                log.debug("Publishing transform from " + transformationPublisher.getId() + " initiated.");
                transformationPublisher.publish(eventBuilder.build(), scope, true);
            }
        } catch (CouldNotPerformException ex) {
            throw new TransformerException("Can not send transform: " + transform, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void sendTransform(Set<Transform> transforms, TransformType type)
            throws TransformerException {
        for (Transform t : transforms) {
            sendTransform(t, type);
        }
    }

    @Override
    public void addTransformListener(TransformListener subscriber) {
        synchronized (lock) {
            this.subscribers.add(subscriber);
        }
    }

    @Override
    public void addTransformListener(Set<TransformListener> subscribers) {
        synchronized (lock) {
            this.subscribers.addAll(subscribers);
        }
    }

    @Override
    public void removeTransformListener(TransformListener subscriber) {
        synchronized (lock) {
            this.subscribers.remove(subscriber);
        }
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    private Unit transformCallback(Event event) {

        Transform transform = null;

        try {
            transform = getPayload(event);
        } catch (InvalidProtocolBufferException e) {
            ExceptionPrinter.printHistory("Received non-rct type on rct scope.", e, log);
        }

        // ignore own events
        if (isPublishedBy(transformationPublisher, event)) {
            return null;
        }

        boolean isStatic = isStatic(event);
        String authority = getAuthority(event);

        log.debug("Received transform {} - static: {} - from {}", transform, isStatic, authority);

        synchronized (lock) {
            for (TransformListener l : subscribers) {
                l.newTransformAvailable(transform, isStatic);
            }
        }
        return null;
    }

    private Unit syncCallback(Event event) {

        // ignore own events
        try {
            if (event.getHeaderMap().get(PUBLISHER_ID).unpack(Primitive.class).getString().equals(publisherSync.getId().toString())) {
                return null;
            }
        } catch (InvalidProtocolBufferException e) {
            // continue if id could not be validated...
        }

        // concurrently publish currently known transform cache
        executor.execute(this::publishCache);
        return null;
    }

    private void publishCache() {

        try {
            log.debug("Publishing cache from " + transformationPublisher.getId());
            synchronized (lock) {
                for (String key : sendCacheDynamic.keySet()) {

                    Transform transform = sendCacheDynamic.get(key);
                    Event.Builder eventBuilder = getEventBuilder(transformationPublisher);
                    setAuthority(sendCacheDynamic.get(key).getAuthority(), eventBuilder);
                    setStatic(false, eventBuilder);
                    setPayload(transform, eventBuilder);

                    try {
                        transformationPublisher.publish(eventBuilder.build(), RCT_SCOPE_TRANSFORM_DYNAMIC, true);
                    } catch (CouldNotPerformException ex) {
                        throw new CouldNotPerformException("Can not publish cached dynamic transform " + sendCacheDynamic.get(key) + ".", ex);
                    }

//                    // apply workaround to avoid sending to many events at once,
//                    // because otherwise spread is killing some sessions.
//                    // todo: implement more efficient by sending a collection instead of all transformations one by one.
//                    try {
//                        Thread.sleep(50);
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                        return;
//                    }
                }
                for (String key : sendCacheStatic.keySet()) {

                    Transform transform = sendCacheStatic.get(key);
                    Event.Builder eventBuilder = getEventBuilder(transformationPublisher);
                    setAuthority(transform.getAuthority(), eventBuilder);
                    setStatic(true, eventBuilder);
                    setPayload(transform, eventBuilder);

                    try {
                        transformationPublisher.publish(eventBuilder.build(), RCT_SCOPE_TRANSFORM_STATIC, true);
                    } catch (CouldNotPerformException ex) {
                        throw new CouldNotPerformException("Can not publish cached static transform " + sendCacheDynamic.get(key) + ".", ex);
                    }

//                    // apply workaround to avoid sending to many events at once,
//                    // because otherwise spread is killing some sessions.
//                    // todo: implement more efficient by sending a collection instead of all transformations one by one.
//                    try {
//                        Thread.sleep(50);
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                        return;
//                    }
                }
            }
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not publish all transformations!", ex, log);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {

        if (staticTransformSubscriberWatchDog != null) {
            staticTransformSubscriberWatchDog.shutdown();
        }

        if (dynamicTransformSubscriberWatchDog != null) {
            dynamicTransformSubscriberWatchDog.shutdown();
        }

        if (rsbSubscriberSyncWatchDog != null) {
            rsbSubscriberSyncWatchDog.shutdown();
        }

        if (rsbPublisherTransformWatchDog != null) {
            rsbPublisherTransformWatchDog.shutdown();
        }

        if (rsbPublisherSyncWatchDog != null) {
            rsbPublisherSyncWatchDog.shutdown();
        }
    }
}
