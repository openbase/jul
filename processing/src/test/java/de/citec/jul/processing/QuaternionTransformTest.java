/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.processing;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mpohling
 */
public class QuaternionTransformTest {

    public QuaternionTransformTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
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
        double[] expResult = new double[4];
        expResult[0] = 1;
        expResult[1] = 0;
        expResult[2] = 0;
        expResult[3] = 0;
        double[] result = QuaternionEulerTransform.transform(roll, pitch, yaw);
        assertArrayEquals(expResult, result, 0.1d);
    }

    @Test
    public void testTransformTaitBryanToQuaternion() {
//        System.out.println("transformTaitBryanToQuaternion");
//        double roll = 45.0;
//        double pitch = 0.0;
//        double yaw = 180.0;
//        double[] expResult = new double[4];
//        expResult[0] = 1;
//        expResult[1] = 0;
//        expResult[2] = 0;
//        expResult[3] = 0;
//
//        System.out.println("roll:" + Math.toRadians(roll));
//        System.out.println("pitch:" + Math.toRadians(pitch));
//        System.out.println("yaw:" + Math.toRadians(yaw));
//
//        double[] result = QuaternionTransform.transformTaitBryanToQuaternion(roll, pitch, yaw);
//        System.out.println("w:" + result[0]);
//        System.out.println("x:" + result[1]);
//        System.out.println("y:" + result[2]);
//        System.out.println("z:" + result[3]);
//        assertArrayEquals(expResult, result, 0.1d);
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
//        double roll = Math.toRadians(30);
//        double pitch = Math.toRadians(54);
//        double yaw = Math.toRadians(12);
        double roll = Math.toRadians(45);
        double pitch = Math.toRadians(89);
        double yaw = Math.toRadians(78);
        double[] euler = new double[3];
        euler[0] = roll;
        euler[1] = pitch;
        euler[2] = yaw;
        double[] result = QuaternionEulerTransform.transformQuaternionToEuler(QuaternionEulerTransform.transformEulerToQuaternion(euler));
        System.out.println("Expected ["+Math.toDegrees(euler[0])+"]["+Math.toDegrees(euler[1])+"]["+Math.toDegrees(euler[2])+"]");
        System.out.println("Result ["+Math.toDegrees(result[0])+"]["+Math.toDegrees(result[1])+"]["+Math.toDegrees(result[2])+"]");
        assertArrayEquals(euler, QuaternionEulerTransform.transformQuaternionToEuler(QuaternionEulerTransform.transformEulerToQuaternion(euler)), 0.1d);
    }
}
