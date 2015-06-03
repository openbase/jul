/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.processing;

/**
 *
 * @author mpohling
 */
public class QuaternionEulerTransform {

    /**
     * Conversion: By combining the quaternion representations of the Euler
     * rotations we get for the Body 3-2-1 sequence, where the airplane first
     * does yaw (Body-Z) turn during taxiing on the runway, then pitches
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
    public static double[] transform(double roll, double pitch, double yaw) {
        double[] quat = new double[4];
        double halfRoll = roll / 2;
        double halfPitch = pitch / 2;
        double halfYaw = yaw / 2;

        quat[0] = Math.cos(halfRoll) * Math.cos(halfPitch) * Math.cos(halfYaw) + Math.sin(halfRoll) * Math.sin(halfPitch) * Math.sin(halfYaw);
        quat[1] = Math.sin(halfRoll) * Math.cos(halfPitch) * Math.cos(halfYaw) - Math.cos(halfRoll) * Math.sin(halfPitch) * Math.sin(halfYaw);
        quat[2] = Math.cos(halfRoll) * Math.sin(halfPitch) * Math.cos(halfYaw) + Math.sin(halfRoll) * Math.cos(halfPitch) * Math.sin(halfYaw);
        quat[3] = Math.cos(halfRoll) * Math.cos(halfPitch) * Math.sin(halfYaw) - Math.sin(halfRoll) * Math.sin(halfPitch) * Math.cos(halfYaw);

        return quat;
    }

    /**
     * Conversion from quaternion to Euler rotation.
     *
     * @param quatW
     * @param quatX
     * @param quatY
     * @param quatZ
     * @return Euler rotations with double[0] = roll, double[1] = pitch,
     * double[2] = yaw in radians
     */
    public static double[] transform(double quatW, double quatX, double quatY, double quatZ) {
        double[] euler = new double[3];

        euler[0] = Math.atan2(2 * (quatW * quatX + quatY * quatZ), 1 - 2 * (Math.pow(quatX, 2) + Math.pow(quatY, 2)));
        euler[1] = Math.asin(2 * (quatW * quatY - quatX * quatZ));
        euler[2] = Math.atan2(2 * (quatW * quatZ + quatX * quatY), 1 - 2 * (Math.pow(quatY, 2) + Math.pow(quatZ, 2)));

        return euler;
    }

    /**
     * Calls transform(double roll, double pitch, double yaw) like
     * transform(euler[0], euler[1], euler[2]).
     *
     * @param euler
     * @return quaternion
     */
    public static double[] transformEulerToQuaternion(double[] euler) {
        return transform(euler[0], euler[1], euler[2]);
    }

    /**
     * Calls transform(double quatW, double quatX, double quatY, double quatZ)
     * like transform(quaternion[0], quaternion[1], quaternion[2],
     * quaternion[3])
     *
     * @param quaternion
     * @return
     */
    public static double[] transformQuaternionToEuler(double[] quaternion) {
        return transform(quaternion[0], quaternion[1], quaternion[2], quaternion[3]);
    }
}
