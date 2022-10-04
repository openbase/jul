package org.openbase.rct.impl.mqtt

import com.google.protobuf.Any.pack
import com.google.protobuf.InvalidProtocolBufferException
import org.openbase.jul.communication.iface.Communicator
import org.openbase.jul.communication.iface.CommunicatorFactory
import org.openbase.jul.communication.iface.Publisher
import org.openbase.jul.communication.mqtt.CommunicatorFactoryImpl
import org.openbase.jul.communication.mqtt.DefaultCommunicatorConfig
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.exception.printer.ExceptionPrinter
import org.openbase.jul.extension.type.processing.ScopeProcessor
import org.openbase.jul.schedule.WatchDog
import org.openbase.rct.*
import org.openbase.rct.impl.TransformCommunicator
import org.openbase.rct.impl.TransformListener
import org.openbase.rct.impl.mqtt.TransformLinkProcessor.Companion.convert
import org.openbase.type.communication.EventType
import org.openbase.type.communication.ScopeType
import org.openbase.type.communication.mqtt.PrimitiveType
import org.openbase.type.geometry.TransformLinksType
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

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
class TransformCommunicatorImpl(private val authority: String) : TransformCommunicator {
    private val subscribers: MutableSet<TransformListener> = HashSet()
    private val sendCacheDynamic: MutableMap<String, Transform> = HashMap()
    private val sendCacheStatic: MutableMap<String, Transform> = HashMap()
    private val lock = Any()
    private val executor = Executors.newCachedThreadPool()
    private var transformationPublisher: Publisher? = null
    private var publisherSync: Publisher? = null
    private var rsbPublisherTransformWatchDog: WatchDog? = null
    private var rsbPublisherSyncWatchDog: WatchDog? = null
    private var staticTransformSubscriberWatchDog: WatchDog? = null
    private var dynamicTransformSubscriberWatchDog: WatchDog? = null
    private var rsbSubscriberSyncWatchDog: WatchDog? = null
    private val factory: CommunicatorFactory = CommunicatorFactoryImpl.instance
    private val defaultCommunicatorConfig = DefaultCommunicatorConfig.instance

    @Throws(TransformerException::class)
    override fun init(conf: TransformerConfig) {
        try {
            log.debug("init communication")
            transformationPublisher = factory.createPublisher(RCT_SCOPE_TRANSFORM, defaultCommunicatorConfig)
            publisherSync = factory.createPublisher(RCT_SCOPE_SYNC, defaultCommunicatorConfig)
            val staticTransformationSubscriber =
                factory.createSubscriber(RCT_SCOPE_TRANSFORM_STATIC, defaultCommunicatorConfig)
            val dynamicTransformationSubscriber =
                factory.createSubscriber(RCT_SCOPE_TRANSFORM_DYNAMIC, defaultCommunicatorConfig)
            val subscriberSync = factory.createSubscriber(RCT_SCOPE_SYNC, defaultCommunicatorConfig)
            rsbPublisherTransformWatchDog = WatchDog(transformationPublisher, "RSBPublisherTransform")
            rsbPublisherSyncWatchDog = WatchDog(publisherSync, "RSBPublisherSync")
            staticTransformSubscriberWatchDog = WatchDog(staticTransformationSubscriber, "RSBSubscriberTransform")
            dynamicTransformSubscriberWatchDog = WatchDog(dynamicTransformationSubscriber, "RSBSubscriberTransform")
            rsbSubscriberSyncWatchDog = WatchDog(subscriberSync, "RSBSubscriberSync")
            staticTransformationSubscriber.registerDataHandler { event: EventType.Event -> transformCallback(event) }
            dynamicTransformationSubscriber.registerDataHandler { event: EventType.Event -> transformCallback(event) }
            subscriberSync.registerDataHandler { event: EventType.Event -> syncCallback(event) }
            rsbPublisherTransformWatchDog!!.activate()
            rsbPublisherSyncWatchDog!!.activate()
            staticTransformSubscriberWatchDog!!.activate()
            dynamicTransformSubscriberWatchDog!!.activate()
            rsbSubscriberSyncWatchDog!!.activate()
            rsbPublisherTransformWatchDog!!.waitForServiceActivation()
            rsbPublisherSyncWatchDog!!.waitForServiceActivation()
            staticTransformSubscriberWatchDog!!.waitForServiceActivation()
            dynamicTransformSubscriberWatchDog!!.waitForServiceActivation()
            rsbSubscriberSyncWatchDog!!.waitForServiceActivation()
            requestSync()
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (ex: CouldNotPerformException) {
            throw TransformerException("Can not initialize rsb communicator.", ex)
        }
    }

    @Throws(TransformerException::class)
    fun requestSync() {
        try {
            if (publisherSync == null || !publisherSync!!.isActive) {
                throw TransformerException("Rsb communicator is not initialized.")
            }
            log.debug("Sending sync request trigger from id " + publisherSync!!.id)

            // trigger other instances to send transforms
            publisherSync!!.publish(getEventBuilder(publisherSync).build(), true)
        } catch (ex: CouldNotPerformException) {
            throw TransformerException("Can not send transforms!", ex)
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private fun getEventBuilder(communicator: Communicator?): EventType.Event.Builder {
        return EventType.Event.newBuilder()
            .putHeader(
                PUBLISHER_ID,
                pack(
                    PrimitiveType.Primitive
                        .newBuilder()
                        .setString(communicator!!.id.toString())
                        .build()
                )
            )
    }

    private fun isPublishedBy(communicator: Communicator?, event: EventType.Event): Boolean = try {
        event
            .headerMap[PUBLISHER_ID]
            ?.unpack(PrimitiveType.Primitive::class.java)
            ?.string == communicator!!.id.toString()
    } catch (e: InvalidProtocolBufferException) {
        false
    }

    private fun setStatic(value: Boolean, builder: EventType.Event.Builder): EventType.Event.Builder {
        return builder.putHeader(
            STATIC_TRANSFORMATION,
            pack(
                PrimitiveType.Primitive
                    .newBuilder()
                    .setBoolean(value)
                    .build()
            )
        )
    }

    private fun isStatic(eventOrBuilder: EventType.EventOrBuilder): Boolean {
        return try {
            eventOrBuilder.headerMap[STATIC_TRANSFORMATION]!!.unpack(
                PrimitiveType.Primitive::class.java
            ).boolean
        } catch (e: InvalidProtocolBufferException) {
            ExceptionPrinter.printHistory("Static transformation flag missing!", e, log)
            true
        }
    }

    @Throws(InvalidProtocolBufferException::class)
    private fun getPayload(eventOrBuilder: EventType.EventOrBuilder): List<Transform> {
        return convert(
            eventOrBuilder.payload.unpack(
                TransformLinksType.TransformLinks::class.java
            )
        )
    }

    @Throws(TransformerException::class)
    override fun sendTransform(transform: Transform, type: TransformType) {
        try {
            if (transformationPublisher?.isActive != true) {
                throw TransformerException("Communicator not initialized!")
            }
            val cacheKey = transform.parentNode + transform.childNode
            log.debug("Publishing transform from " + transformationPublisher!!.id)
            synchronized(lock) {
                val eventBuilder = getEventBuilder(transformationPublisher)
                eventBuilder.payload = pack(convert(listOf(transform)))
                val scope: ScopeType.Scope
                when (type) {
                    TransformType.STATIC -> {
                        if (transform.equalsWithoutTime(sendCacheStatic[cacheKey])) {
                            if (transform.equalsWithoutTime(
                                    GlobalTransformReceiver.getInstance().lookupTransform(
                                        transform.parentNode,
                                        transform.childNode,
                                        System.currentTimeMillis()
                                    )
                                )
                            ) {
                                log.debug("Publishing static transform from " + transformationPublisher!!.id + " done because Transformation[" + cacheKey + "] already known.")
                                // we are done if transformation is already known
                                return
                            }
                            log.warn("Publishing static transform from " + transformationPublisher!!.id + " again because Transformation[" + cacheKey + "] sync failed.")
                        }
                        sendCacheStatic[cacheKey] = transform
                        scope = RCT_SCOPE_TRANSFORM_STATIC
                        setStatic(true, eventBuilder)
                    }

                    TransformType.DYNAMIC -> {
                        if (transform == sendCacheDynamic[cacheKey]) {
                            if (transform.equalsWithoutTime(
                                    GlobalTransformReceiver.getInstance().lookupTransform(
                                        transform.parentNode,
                                        transform.childNode,
                                        System.currentTimeMillis()
                                    )
                                )
                            ) {
                                log.debug("Publishing dynamic transform from " + transformationPublisher!!.id + " done because Transformation[" + cacheKey + "] already known.")
                                // we are done if transformation is already known
                                return
                            }
                            log.warn("Publishing dynamic transform from " + transformationPublisher!!.id + " again because Transformation[" + cacheKey + "] sync failed.")
                            return
                        }
                        sendCacheDynamic[cacheKey] = transform
                        scope = RCT_SCOPE_TRANSFORM_DYNAMIC
                        setStatic(false, eventBuilder)
                    }

                    else -> throw TransformerException("Unknown TransformType: " + type.name)
                }
                log.debug("Publishing transform from " + transformationPublisher!!.id + " initiated.")
                transformationPublisher!!.publish(eventBuilder.build(), scope, true)
            }
        } catch (ex: CouldNotPerformException) {
            throw TransformerException("Can not send transform: $transform", ex)
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    @Throws(TransformerException::class)
    override fun sendTransform(transforms: Set<Transform>, type: TransformType) {
        for (t in transforms) {
            sendTransform(t, type)
        }
    }

    override fun addTransformListener(subscriber: TransformListener) {
        synchronized(lock) { subscribers.add(subscriber) }
    }

    override fun addTransformListener(subscribers: Set<TransformListener>) {
        synchronized(lock) { this.subscribers.addAll(subscribers) }
    }

    override fun removeTransformListener(subscriber: TransformListener) {
        synchronized(lock) { subscribers.remove(subscriber) }
    }

    override fun getAuthority(): String {
        return authority
    }

    private fun transformCallback(event: EventType.Event) {
        var transforms: List<Transform> = try {
            getPayload(event)
        } catch (e: InvalidProtocolBufferException) {
            ExceptionPrinter.printHistory("Received non-rct type on rct scope.", e, log)
            return
        }

        // ignore own events
        if (isPublishedBy(transformationPublisher, event)) {
            return
        }
        val isStatic = isStatic(event)

        // log.debug("Received transforms {} - static: {} - from {}", transforms, isStatic, getAuthority(event));
        synchronized(lock) {
            subscribers.forEach {
                it.newTransformAvailable(transforms, isStatic)
            }
        }
    }

    private fun syncCallback(event: EventType.Event) {

        // ignore own events
        try {
            if (event.headerMap[PUBLISHER_ID]!!.unpack(
                    PrimitiveType.Primitive::class.java
                ).string == publisherSync!!.id.toString()
            ) {
                return
            }
        } catch (e: InvalidProtocolBufferException) {
            // continue if id could not be validated...
        }

        // concurrently publish currently known transform cache
        executor.execute { publishCache() }
    }

    private fun publishCache() {
        try {
            log.debug("Publishing cache from " + transformationPublisher!!.id)
            synchronized(lock) {

                getEventBuilder(transformationPublisher).let { eventBuilder ->
                    setStatic(false, eventBuilder)
                    eventBuilder.payload = pack(convert(sendCacheDynamic.values))
                    try {
                        transformationPublisher!!.publish(eventBuilder.build(), RCT_SCOPE_TRANSFORM_DYNAMIC, true)
                    } catch (ex: CouldNotPerformException) {
                        throw CouldNotPerformException(
                            "Can not publish cached dynamic transform.", ex
                        )
                    }
                }

                getEventBuilder(transformationPublisher).let { eventBuilder ->
                    setStatic(true, eventBuilder)
                    eventBuilder.payload = pack(convert(sendCacheStatic.values))
                    try {
                        transformationPublisher!!.publish(eventBuilder.build(), RCT_SCOPE_TRANSFORM_STATIC, true)
                    } catch (ex: CouldNotPerformException) {
                        throw CouldNotPerformException(
                            "Can not publish cached static transform.", ex
                        )
                    }

                }
            }
        } catch (ex: CouldNotPerformException) {
            ExceptionPrinter.printHistory("Could not publish all transformations!", ex, log)
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun shutdown() {
        if (staticTransformSubscriberWatchDog != null) {
            staticTransformSubscriberWatchDog!!.shutdown()
        }
        if (dynamicTransformSubscriberWatchDog != null) {
            dynamicTransformSubscriberWatchDog!!.shutdown()
        }
        if (rsbSubscriberSyncWatchDog != null) {
            rsbSubscriberSyncWatchDog!!.shutdown()
        }
        if (rsbPublisherTransformWatchDog != null) {
            rsbPublisherTransformWatchDog!!.shutdown()
        }
        if (rsbPublisherSyncWatchDog != null) {
            rsbPublisherSyncWatchDog!!.shutdown()
        }
    }

    companion object {
        private const val PUBLISHER_ID = "PUBLISHER_ID"
        private const val STATIC_TRANSFORMATION = "STATIC_TRANSFORMATION"
        val RCT_SCOPE_TRANSFORM: ScopeType.Scope = ScopeProcessor.generateScope("/rct/transform")
        val RCT_SCOPE_SYNC: ScopeType.Scope = ScopeProcessor.generateScope("/rct/sync")
        private const val RCT_SCOPE_SUFFIX_STATIC = "static"
        private const val RCT_SCOPE_SUFFIX_DYNAMIC = "dynamic"
        private const val RCT_SCOPE_SEPARATOR = "/"
        val RCT_SCOPE_TRANSFORM_STATIC: ScopeType.Scope = ScopeProcessor.concat(
            RCT_SCOPE_TRANSFORM,
            ScopeProcessor.generateScope(
                RCT_SCOPE_SEPARATOR + RCT_SCOPE_SUFFIX_STATIC
            )
        )
        val RCT_SCOPE_TRANSFORM_DYNAMIC: ScopeType.Scope = ScopeProcessor.concat(
            RCT_SCOPE_TRANSFORM,
            ScopeProcessor.generateScope(
                RCT_SCOPE_SEPARATOR + RCT_SCOPE_SUFFIX_DYNAMIC
            )
        )
        private val log = LoggerFactory.getLogger(TransformCommunicatorImpl::class.java)
    }
}
