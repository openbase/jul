package org.openbase.jul.annotation
/*-
 * #%L
 * JUL Annoation
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
 */ /**
 * This annotations is used to tag methods that are registered for an RPCServer.
 * Therefore the RPCHelper will skip every method when registering an interface which does not have this annotation.
 * Important to note is that when an interface overrides a method tagged with an annotation, the annotation is lost.
 * Thus if desired the annotation has to be added to the overriding method again.
 *
 * @author [Tamino Huxohl](mailto:pleminoq@openbase.org)
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(
    AnnotationRetention.RUNTIME
)
annotation class RPCMethod(
    /**
     * Flag marking legacy RPCs. When true they will not be registered per default, but can be activated using
     * a jp property `JPComLegacyMode.class` via "--communication-legacy".
     *
     * @return if the annotated method is an rpc method
     */
    val legacy: Boolean = false,

    val priority: Priority = Priority.NORMAL,
) {
    enum class Priority {
        NORMAL, HIGH
    }
}