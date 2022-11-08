package org.openbase.rct.impl.mqtt

import org.openbase.rct.Transform
import org.openbase.type.geometry.TransformLinksType.TransformLinks
import org.openbase.type.geometry.TransformLinkType
import org.openbase.type.geometry.TransformLinksType.TransformLinksOrBuilder
import javax.media.j3d.Transform3D
import javax.vecmath.Quat4d
import javax.vecmath.Vector3d

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
class TransformLinkProcessor {

    companion object {
        @JvmStatic
        fun convert(transformations: Collection<Transform>): TransformLinks = transformations
            .asSequence()
            .map { transform ->
                val quat = transform.rotationQuat
                val vec = transform.translation
                val builder = TransformLinkType.TransformLink.newBuilder()
                builder.authority = transform.authority
                builder.timeBuilder.time = transform.time * 1000L
                builder.childNode = transform.childNode
                builder.parentNode = transform.parentNode
                builder.transformBuilder.rotationBuilder.qw = quat.w
                builder.transformBuilder.rotationBuilder.qx = quat.x
                builder.transformBuilder.rotationBuilder.qy = quat.y
                builder.transformBuilder.rotationBuilder.qz = quat.z
                builder.transformBuilder.translationBuilder.x = vec.x
                builder.transformBuilder.translationBuilder.y = vec.y
                builder.transformBuilder.translationBuilder.z = vec.z
                builder.build()
            }
            .toList()
            .let { TransformLinks.newBuilder().addAllTransforms(it).build() }

        @JvmStatic
        fun convert(transformations: TransformLinksOrBuilder): List<Transform> =
            transformations.transformsList
                .asSequence()
                .map { transform ->
                    val rstRot = transform.transform.rotation
                    val rstTrans = transform.transform.translation
                    val quat = Quat4d(rstRot.qx, rstRot.qy, rstRot.qz, rstRot.qw)
                    val vec = Vector3d(rstTrans.x, rstTrans.y, rstTrans.z)
                    val transform3d = Transform3D(quat, vec, 1.0)
                    Transform(transform3d, transform.parentNode, transform.childNode, transform.time.time / 1000L)

                }.toList()
    }
}
