package org.openbase.rct.type

import org.openbase.rct.Transform
import org.openbase.type.geometry.PoseType
import javax.media.j3d.Transform3D
import javax.vecmath.Quat4d
import javax.vecmath.Vector3d

/*
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
 * @author [Divine Threepwood](mailto:divine@openbase.org)
 */
class PoseTransformer {

    companion object {
        /**
         * Method transforms a Transform type into a
         * @param pose the pose describing the transformation.
         * @param parentNode The parent coordinate node ID.
         * @param childNode The child coordinate node ID.
         * @return the generated transformation.
         */
        fun transform(pose: PoseType.Pose, parentNode: String?, childNode: String?, authority: String?): Transform {
            return Transform(transform(pose), parentNode!!, childNode!!, System.currentTimeMillis(), authority)
        }

        /**
         * Method transforms a PoseType.Pose into a Transform3D type.
         * @param pose the used as input.
         * @return the generated transformation.
         */
        fun transform(pose: PoseType.Pose): Transform3D {
            val pRotation = pose.rotation
            val pTranslation = pose.translation
            val jRotation = Quat4d(pRotation.qx, pRotation.qy, pRotation.qz, pRotation.qw)
            val jTranslation = Vector3d(pTranslation.x, pTranslation.y, pTranslation.z)
            return Transform3D(jRotation, jTranslation, 1.0)
        }
    }
}
