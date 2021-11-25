package org.openbase.jul.processing;

/*
 * #%L
 * JUL Processing Default
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class QuaternionTransformTest {

    public QuaternionTransformTest() {
    }

    @BeforeAll
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
    }

    /**
     * Test of transformTaitBryanToQuaternion method, of class
     * QuaternionTransform.
     */
    @Test
    public void testNeutralTransformTaitBryanToQuaternion() {
        System.out.println("testNeutralTransformTaitBryanToQuaternion");
        double roll = 0.0;
        double pitch = 0.0;
        double yaw = 0.0;
        Quat4d result = QuaternionEulerTransform.transform(roll, pitch, yaw);
        assertArrayEquals(toDoubleArray(new Quat4d(0, 0, 0, 1)), toDoubleArray(result), 0.1d);
    }

    /**
     * Test of transformTaitBryanToQuaternion method, of class
     * QuaternionTransform.
     */
    @Test
    public void test90rTransformTaitBryanToQuaternion() {
        System.out.println("testNeutralTransformTaitBryanToQuaternion");
        double roll = Math.toRadians(90);
        double pitch = Math.toRadians(0);
        double yaw = Math.toRadians(0);

        Quat4d result = QuaternionEulerTransform.transform(new Vector3d(roll, pitch, yaw));
        assertArrayEquals(toDoubleArray(new Quat4d(0.7071, 0, 0, 0.7071)), toDoubleArray(result), 0.1d);
    }

    /**
     * Test of transformTaitBryanToQuaternion method, of class
     * QuaternionTransform.
     */
    @Timeout(5)
    @Test
    public void test90pTransformTaitBryanToQuaternion() {
        System.out.println("testNeutralTransformTaitBryanToQuaternion");
        double roll = Math.toRadians(0);
        double pitch = Math.toRadians(90);
        double yaw = Math.toRadians(0);

        Quat4d result = QuaternionEulerTransform.transform(new Vector3d(roll, pitch, yaw));
        assertArrayEquals(toDoubleArray(new Quat4d(0, 0.7071, 0, 0.7071)), toDoubleArray(result), 0.1d);
    }

    /**
     * Test of transformTaitBryanToQuaternion method, of class
     * QuaternionTransform.
     */
    @Test
    public void test90yTransformTaitBryanToQuaternion() {
        System.out.println("testNeutralTransformTaitBryanToQuaternion");
        double roll = Math.toRadians(0);
        double pitch = Math.toRadians(0);
        double yaw = Math.toRadians(90);

        Quat4d result = QuaternionEulerTransform.transform(new Vector3d(roll, pitch, yaw));
        assertArrayEquals(toDoubleArray(new Quat4d(0, 0, 0.7071, 0.7071)), toDoubleArray(result), 0.1d);
    }

    /**
     * Test of transformTaitBryanToQuaternion method, of class
     * QuaternionTransform.
     */
    @Test
    public void test() {
        System.out.println("testNeutralTransformTaitBryanToQuaternion");
        double roll = Math.toRadians(40);
        double pitch = Math.toRadians(80);
        double yaw = Math.toRadians(30);

        Quat4d result = QuaternionEulerTransform.transform(new Vector3d(roll, pitch, yaw));
        assertArrayEquals(toDoubleArray(new Quat4d(0.409, 0.516, 0.399, 0.638)), toDoubleArray(result), 0.1d);
    }

    /**
     * Tests it the transformation forth and back again results in the same
     * values.
     *
     * @throws Exception
     */
    @Test
    public void testTransformation() throws Exception {
        System.out.println("Test transformations");
        double roll = Math.toRadians(45);
        double pitch = Math.toRadians(89);
        double yaw = Math.toRadians(78);
        Vector3d input = new Vector3d(roll, pitch, yaw);
        assertArrayEquals(toDoubleArray(input), toDoubleArray(QuaternionEulerTransform.transform(QuaternionEulerTransform.transform(input))), 0.1d);
    }

    @Test
    public void testTransformation180Yaw() throws Exception {
        System.out.println("Test transformations 180 yaw");

        double yaw = Math.toRadians(180);
        double pitch = Math.toRadians(0);
        double roll = Math.toRadians(0);

        Vector3d input = new Vector3d(roll, pitch, yaw);
        assertArrayEquals(toDoubleArray(input), toDoubleArray(QuaternionEulerTransform.transform(QuaternionEulerTransform.transform(input))), 0.1d);
    }

    private double[] toDoubleArray(Vector3d vec) {
        double[] res = new double[3];
        res[0] = vec.x;
        res[1] = vec.y;
        res[2] = vec.z;
        return res;
    }

    private double[] toDoubleArray(Quat4d vec) {
        double[] res = new double[4];
        res[0] = vec.x;
        res[1] = vec.y;
        res[2] = vec.z;
        res[3] = vec.w;
        return res;
    }

}
