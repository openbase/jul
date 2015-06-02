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
public class QuaternionTransform {

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
     * @param roll
     * @param pitch
     * @param yaw
     * @return quaternion 
     */
    public static double[] transformTaitBryanToQuaternion(double roll, double pitch, double yaw) {
        double[] quat = new double[4];
        double halfRoll = Math.toRadians(roll / 2);
        double halfPitch = Math.toRadians(pitch / 2);
        double halfYaw = Math.toRadians(yaw / 2);

        quat[0] = Math.cos(halfRoll) * Math.cos(halfPitch) * Math.cos(halfYaw) + Math.sin(halfRoll) * Math.sin(halfPitch) * Math.sin(halfYaw);
        quat[1] = Math.sin(halfRoll) * Math.cos(halfPitch) * Math.cos(halfYaw) - Math.cos(halfRoll) * Math.sin(halfPitch) * Math.sin(halfYaw);
        quat[2] = Math.cos(halfRoll) * Math.sin(halfPitch) * Math.cos(halfYaw) + Math.sin(halfRoll) * Math.cos(halfPitch) * Math.sin(halfYaw);
        quat[3] = Math.cos(halfRoll) * Math.cos(halfPitch) * Math.sin(halfYaw) - Math.sin(halfRoll) * Math.sin(halfPitch) * Math.cos(halfYaw);

        return quat;
    }
}
