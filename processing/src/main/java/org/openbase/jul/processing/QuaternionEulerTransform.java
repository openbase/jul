package org.openbase.jul.processing;

/*
 * #%L
 * JUL Processing
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import org.slf4j.LoggerFactory;

/**
 *
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
     * @param roll in radians
     * @param pitch in radians
     * @param yaw in radians
     * @return quaternion
     */
    public static Quat4d transform(double roll, double pitch, double yaw) {
        return transform(new Vector3d(roll, pitch, yaw));
    }

    public static Quat4d transform(final Vector3d vector3d) {
//        double halfRoll = roll / 2;
//        double halfPitch = pitch / 2;
//        double halfYaw = yaw / 2;
//
//        // qW
//        quat[0] = Math.cos(halfRoll) * Math.cos(halfPitch) * Math.cos(halfYaw) + Math.sin(halfRoll) * Math.sin(halfPitch) * Math.sin(halfYaw);
//        // qX
//        quat[1] = -Math.cos(halfRoll) * Math.sin(halfPitch) * Math.cos(halfYaw) - Math.sin(halfRoll) * Math.cos(halfPitch) * Math.sin(halfYaw);
//        // qY
//        quat[2] = Math.sin(halfRoll) * Math.sin(halfPitch) * Math.cos(halfYaw) - Math.cos(halfRoll) * Math.cos(halfPitch) * Math.sin(halfYaw);
//        // qZ
//        quat[3] = Math.cos(halfRoll) * Math.sin(halfPitch) * Math.sin(halfYaw) - Math.sin(halfRoll) * Math.cos(halfPitch) * Math.cos(halfYaw);

        logger.info("QuaternionEulerTransform roll: " + vector3d.x);
        logger.info("QuaternionEulerTransform pitch: " + vector3d.y);
        logger.info("QuaternionEulerTransform yaw: " + vector3d.z);

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
     *
     *
     * @return Euler rotations with double[0] = roll, double[1] = pitch,
     * double[2] = yaw in radians
     */
    /**
     *
     * @param quat4d
     * @return
     */
    public static Vector3d transform(final Quat4d quat4d) {
        double[] euler = new double[3];
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

//        euler[0] = Math.atan2(2 * (quat4d.w * quatX + quat4d.y * quat4d.z), 1 - 2 * (Math.pow(quatX, 2) + Math.pow(quat4d.y, 2)));
//        euler[1] = Math.asin(2 * (quat4d.w * quat4d.y - quatX * quat4d.z));
//        euler[2] = Math.atan2(2 * (quat4d.w * quat4d.z + quatX * quat4d.y), 1 - 2 * (Math.pow(quat4d.y, 2) + Math.pow(quat4d.z, 2)));
        return result;
    }

//    /**
//     * Calls transform(double roll, double vector3d.y, double yaw) like
//     * transform(euler[0], euler[1], euler[2]).
//     *
//     * @param euler
//     * @return quaternion
//     */
//    public static double[] transformEulerToQuaternion(double[] euler) {
//        return transform(euler[0], euler[1], euler[2]);
//    }
//
//    /**
//     * Calls transform(double quat4d.w, double quatX, double quat4d.y, double quat4d.z)
//     * like transform(quaternion[0], quaternion[1], quaternion[2],
//     * quaternion[3])
//     *
//     * @param quaternion
//     * @return
//     */
//    public static double[] transformQuaternionToEuler(double[] quaternion) {
//        return transform(quaternion[0], quaternion[1], quaternion[2], quaternion[3]);
//    }
}
