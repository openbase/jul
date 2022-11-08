package org.openbase.rct

import org.apache.commons.lang.builder.EqualsBuilder
import org.apache.commons.lang.builder.HashCodeBuilder
import javax.media.j3d.Transform3D
import javax.vecmath.Matrix3d
import javax.vecmath.Matrix4d
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
 */ /**
 * A transform from one coordinate frame "parent" to "child". It holds the
 * transformation itself as a Java3D [Transform3D] object, the parent and
 * child node IDs, the time at which it was created from the database and the
 * ID of the authority that generated this object.
 *
 * @author lziegler
 */
data class Transform(
    /**
     * The geometric transform representation.
     */
    var transform: Transform3D,
    /**
     * The ID of the parent coordinate frame.
     */
    var parentNode: String,

    /**
     * The ID of the child coordinate frame.
     */
    var childNode: String,
    /**
     * The timestamp of the moment this object was created.
     */
    var time: Long,
    /**
     * The ID of the authority that created this object.
     */
    var authority: String? = null,
) {

    /**
     * The translation part of the complete transform.
     *
     * @return The translation as Java3D Vecmath [Vector3d]
     */
    val translation: Vector3d
        get() {
            val translation = Vector3d()
            transform[translation]
            return translation
        }

    /**
     * The rotation part of the complete transform.
     *
     * @return The rotation as Java3D Vecmath [Quat4d]
     */
    val rotationQuat: Quat4d
        get() {
            val quat = Quat4d()
            transform[quat]
            return quat
        }

    /**
     * The rotation part of the complete transform.
     *
     * @return The rotation as Java3D Vecmath [Matrix3d]
     */
    val rotationMatrix: Matrix3d
        get() {
            val rot = Matrix3d()
            transform[rot]
            return rot
        }// this code is taken from buttel btMatrix3x3 getEulerYPR().
    // http://bulletphysics.org/Bullet/BulletFull/btMatrix3x3_8h_source.html
    // first use the normal calculus

    // on pitch = +/-HalfPI
    /**
     * The rotation part of the complete transform as yaw, pitch and
     * roll angles.
     *
     * @return The yaw, pitch and roll angles as Java3D Vecmath [Vector3d]
     */
    val rotationYPR: Vector3d
        get() {
            val rot = rotationMatrix

            // this code is taken from buttel btMatrix3x3 getEulerYPR().
            // http://bulletphysics.org/Bullet/BulletFull/btMatrix3x3_8h_source.html
            // first use the normal calculus
            var yawOut = Math.atan2(rot.m10, rot.m00)
            var pitchOut = Math.asin(-rot.m20)
            val rollOut = Math.atan2(rot.m21, rot.m22)

            // on pitch = +/-HalfPI
            if (Math.abs(pitchOut) == Math.PI / 2.0) {
                if (yawOut > 0) yawOut -= Math.PI else yawOut += Math.PI
                if (pitchOut > 0) pitchOut -= Math.PI else pitchOut += Math.PI
            }
            return Vector3d(yawOut, pitchOut, rollOut)
        }

    override fun toString(): String {
        val mat = Matrix4d()
        transform[mat]
        val tStr = String.format(
            "{%.2f %.2f %.2f %.2f; %.2f %.2f %.2f %.2f; %.2f %.2f %.2f %.2f; %.2f %.2f %.2f %.2f}",
            mat.m00, mat.m01, mat.m02, mat.m03, mat.m10, mat.m11,
            mat.m12, mat.m13, mat.m20, mat.m21, mat.m22, mat.m23,
            mat.m30, mat.m31, mat.m32, mat.m33
        )
        return ("Transform[parent:" + parentNode + "; child:" + childNode
                + "; time:" + time + "; transform:" + tStr + "]")
    }

    fun equalsWithoutTime(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (obj === this) {
            return true
        }
        if (obj.javaClass != javaClass) {
            return false
        }
        val rhs = obj as Transform
        return EqualsBuilder().append(childNode, rhs.childNode).append(parentNode, rhs.parentNode)
            .append(transform, rhs.transform).isEquals
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (obj === this) {
            return true
        }
        if (obj.javaClass != javaClass) {
            return false
        }
        val rhs = obj as Transform
        return EqualsBuilder().append(childNode, rhs.childNode).append(parentNode, rhs.parentNode)
            .append(time, rhs.time).append(transform, rhs.transform).isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(13, 53).append(childNode).append(parentNode).append(time).append(transform).toHashCode()
    }
}
