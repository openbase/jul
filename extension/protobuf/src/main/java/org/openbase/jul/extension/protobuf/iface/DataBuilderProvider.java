package org.openbase.jul.extension.protobuf.iface;

/*-
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
import com.google.protobuf.AbstractMessage;
import org.openbase.jul.extension.protobuf.BuilderSyncSetup;
import org.openbase.jul.extension.protobuf.ClosableDataBuilder;

/**
 *
 * * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 * @param <M> Message
 * @param <MB> Builder
 */
public interface DataBuilderProvider<M extends AbstractMessage, MB extends M.Builder<MB>> {

    MB cloneDataBuilder();

    BuilderSyncSetup<MB> getBuilderSetup();

    /**
     * This method generates a closable data builder wrapper including the
     * internal builder instance. Be informed that the internal builder is
     * directly locked and all internal builder operations are queued. Therefore please
     * call the close method soon as possible to release the builder lock after
     * you builder modifications, otherwise the overall processing pipeline is
     * delayed.
     *
     *
     * <pre>
     * {@code Usage Example:
     *
     *     try (ClosableDataBuilder<MotionSensor.Builder> dataBuilder = getDataBuilder(this)) {
     *         dataBuilder.getInternalBuilder().setMotionState(motion);
     *     } catch (Exception ex) {
     *         throw new CouldNotPerformException("Could not apply data change!", ex);
     *     }
     * }
     * </pre> In this example the ClosableDataBuilder.close method is be called
     * in background after leaving the try brackets.
     *
     * @param consumer
     * @return a new builder wrapper with a locked builder instance.
     */
    ClosableDataBuilder<MB> getDataBuilder(final Object consumer);

    /**
     * This method generates a closable data builder wrapper including the
     * internal builder instance. Be informed that the internal builder is
     * directly locked and all internal builder operations are queued. Therefore please
     * call the close method soon as possible to release the builder lock after
     * you builder modifications, otherwise the overall processing pipeline is
     * delayed.
     *
     *
     * <pre>
     * {@code Usage Example:
     *
     *     try (ClosableDataBuilder<MotionSensor.Builder> dataBuilder = getDataBuilder(this)) {
     *         dataBuilder.getInternalBuilder().setMotionState(motion);
     *     } catch (Exception ex) {
     *         throw new CouldNotPerformException("Could not apply data change!", ex);
     *     }
     * }
     * </pre> In this example the ClosableDataBuilder.close method is be called
     * in background after leaving the try brackets.
     *
     * @param consumer
     * @param notifyChange this flag defines if notifyChange is done after unlocking.
     * @return a new builder wrapper with a locked builder instance.
     */
    ClosableDataBuilder<MB> getDataBuilder(final Object consumer, final boolean notifyChange);
}
