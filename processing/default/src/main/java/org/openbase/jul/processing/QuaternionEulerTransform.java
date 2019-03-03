package org.openbase.jul.processing;

/*
 * #%L
 * JUL Processing Default
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

import org.slf4j.LoggerFactory;

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class QuaternionEulerTransform {

    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(QuaternionEulerTransform.class);

    /**
     * Conversion: By combining the quaternion representations of the Euler
     * rotations we get for the Body 3-2-1 sequence, where the airplane first
     * does yaw (Body-Z) turn during taxiing on the runway, then vector3d.yes
     * (Body-Y) during take-off, and finally rolls (Body-X) in the air. The
     * resulting orientation of Body 3-2-1 sequence (around the capitalized axis
     * in the illustration of Tait–Bryan angles) is equivalent to that of lab
     * 1-2-3 sequence (around the lower-cased axis), where the airplane is
     * rolled first (lab-x axis), and then nosed up around the horizontal lab-y
     * axis, and finally rotated around the vertical lab-z axis:
     *
     * @param roll  in radians
     * @param pitch in radians
     * @param yaw   in radians
     *
     * @return quaternion
     */
    public static Quat4d transform(double roll, double pitch, double yaw) {
        return transform(new Vector3d(roll, pitch, yaw));
    }

    public static Quat4d transform(final Vector3d vector3d) {
        // Assuming the angles are in radians.
        double cosYawHalf = Math.cos(vector3d.z / 2);
        double sinYawHalf = Math.sin(vector3d.z / 2);
        double cosPitchHalf = Math.cos(vector3d.y / 2);
        double sinPitchHalf = Math.sin(vector3d.y / 2);
        double cosRollHalf = Math.cos(vector3d.x / 2);
        double sinRollHalf = Math.sin(vector3d.x / 2);
        double cosYawPitchHalf = cosYawHalf * cosPitchHalf;
        double sinYawPitchHalf = sinYawHalf * sinPitchHalf;

        Quat4d quat4d = new Quat4d();
        quat4d.w = cosYawPitchHalf * cosRollHalf - sinYawPitchHalf * sinRollHalf;
        quat4d.x = cosYawPitchHalf * sinRollHalf + sinYawPitchHalf * cosRollHalf;
        quat4d.y = cosYawHalf * sinPitchHalf * cosRollHalf - sinYawHalf * cosPitchHalf * sinRollHalf;
        quat4d.z = sinYawHalf * cosPitchHalf * cosRollHalf + cosYawHalf * sinPitchHalf * sinRollHalf;
        return quat4d;
    }

    /**
     * Conversion from quaternion to Euler rotation.
     * The x value fr
     *
     * @param quat4d the quaternion from which the euler rotation is computed.
     *
     * @return a vector with the mapping: x = roll, y = pitch, z = yaw
     */
    public static Vector3d transform(final Quat4d quat4d) {
        Vector3d result = new Vector3d();

        double test = quat4d.x * quat4d.z + quat4d.y * quat4d.w;
        if (test >= 0.5) { // singularity at north pole
            result.x = 0;
            result.y = Math.PI / 2;
            result.z = 2 * Math.atan2(quat4d.x, quat4d.w);
            return result;
        }
        if (test <= -0.5) { // singularity at south pole
            result.x = 0;
            result.y = -Math.PI / 2;
            result.z = -2 * Math.atan2(quat4d.x, quat4d.w);
            return result;
        }
        double sqx = quat4d.x * quat4d.x;
        double sqz = quat4d.y * quat4d.y;
        double sqy = quat4d.z * quat4d.z;

        result.x = Math.atan2(2 * quat4d.x * quat4d.w - 2 * quat4d.z * quat4d.y, 1 - 2 * sqx - 2 * sqz);
        result.y = Math.asin(2 * test);
        result.z = Math.atan2(2 * quat4d.z * quat4d.w - 2 * quat4d.x * quat4d.y, 1 - 2 * sqy - 2 * sqz);
        return result;
    }
}
