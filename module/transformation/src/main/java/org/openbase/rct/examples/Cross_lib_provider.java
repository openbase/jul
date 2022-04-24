package org.openbase.rct.examples;

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

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.Transform3D;
import javax.vecmath.Quat4d;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import org.slf4j.LoggerFactory;
import org.openbase.rct.Transform;
import org.openbase.rct.TransformPublisher;
import org.openbase.rct.TransformReceiver;
import org.openbase.rct.TransformType;
import org.openbase.rct.TransformerException;
import org.openbase.rct.TransformerFactory;

/**
 *
 * @author nkoester
 */
public class Cross_lib_provider {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Cross_lib_provider.class);

    static TransformPublisher publisher;
    TransformReceiver receiver;

    public Cross_lib_provider() {

    }

    private Quat4f yrp2q(float roll, float pitch, float yaw) {

        float halfYaw = yaw * 0.5f;
        float halfPitch = pitch * 0.5f;
        float halfRoll = roll * 0.5f;

        float cosYaw = (float) cos(halfYaw);
        float sinYaw = (float) sin(halfYaw);
        float cosPitch = (float) cos(halfPitch);
        float sinPitch = (float) sin(halfPitch);
        float cosRoll = (float) cos(halfRoll);
        float sinRoll = (float) sin(halfRoll);

        return new Quat4f(sinRoll * cosPitch * cosYaw - cosRoll * sinPitch * sinYaw, //x
                cosRoll * sinPitch * cosYaw + sinRoll * cosPitch * sinYaw, //y
                cosRoll * cosPitch * sinYaw - sinRoll * sinPitch * cosYaw, //z
                cosRoll * cosPitch * cosYaw + sinRoll * sinPitch * sinYaw); //formerly yzx
    }

    public static Quat4d yrp2q_marian(double roll, double pitch, double yaw) {

        // Assuming the angles are in radians.
        double cosYawHalf = Math.cos(yaw / 2);
        double sinYawHalf = Math.sin(yaw / 2);
        double cosPitchHalf = Math.cos(pitch / 2);
        double sinPitchHalf = Math.sin(pitch / 2);
        double cosRollHalf = Math.cos(roll / 2);
        double sinRollHalf = Math.sin(roll / 2);
        double cosYawPitchHalf = cosYawHalf * cosPitchHalf;
        double sinYawPitchHalf = sinYawHalf * sinPitchHalf;

        return new Quat4d((cosYawPitchHalf * cosRollHalf - sinYawPitchHalf * sinRollHalf),
                (cosYawPitchHalf * sinRollHalf + sinYawPitchHalf * cosRollHalf),
                (sinYawHalf * cosPitchHalf * cosRollHalf + cosYawHalf * sinPitchHalf * sinRollHalf),
                (cosYawHalf * sinPitchHalf * cosRollHalf - sinYawHalf * cosPitchHalf * sinRollHalf));
    }

    private void provide() throws InterruptedException, TransformerException, TransformerFactory.TransformerFactoryException {
        publisher = TransformerFactory.getInstance().createTransformPublisher("java_provider");

        ////////////////
        // STATIC
        ////////////////
        // Define the translation
        Vector3d translation = new Vector3d(0.0, 1.0, 1.0);

        // Define the rotation
        //Quat4f rotation = yrp2q(0.0f, 0.0f, 0.0f);
        Quat4f rotation = yrp2q(0.0f, 0.0f, (float) -Math.PI / 2);

        float rot_val = (float) Math.PI / 2;
        double rot_val_d = Math.PI / 2;

        System.out.println("Using translation: " + translation);
        System.out.println("Using rotation   : " + rotation);
        // Create the transformation
        Transform3D transform = new Transform3D(rotation, translation, 1.0);
        Transform transform_base_java = new Transform(transform, "base", "java_static", System.currentTimeMillis());

        // Publish the static offset
        System.out.println("Sending static transform now ...");
        publisher.sendTransform(transform_base_java, TransformType.STATIC);

        ////////////////
        // DYNAMIC
        ////////////////
        // translate further along the original y axis
        Vector3d translation_dyn = new Vector3d(-1, 0, -2);
        // Define the rotation
        Quat4f rotation_dyn = yrp2q(0.0f, 0.0f, (float) -Math.PI / 2);

        // Create the transformation
        Transform3D transform_dyn = new Transform3D(rotation_dyn, translation_dyn, 1.0);

        System.out.println("Sending dynamic transform now ...");

        // Publish the static offset
        while (true) {
            Transform transform_java_java_dynamic = new Transform(transform_dyn, "java_static", "java_dynamic", System.currentTimeMillis());
            publisher.sendTransform(transform_java_java_dynamic, TransformType.DYNAMIC);
            Thread.sleep(20);
        }

    }

    private Vector4d transform_now(String source, String target, Vector4d point) throws InterruptedException, TransformerException {
        long when = System.currentTimeMillis();
        Thread.sleep(30);

        if (receiver.canTransform(target, source, when)) {
            Transform trafo = receiver.lookupTransform(target, source, when);
            System.out.println("[" + trafo.getFrameChild() + "]  -->  " + "[" + trafo.getFrameParent() + "]");

            Vector4d point4d = new Vector4d(point.x, point.y, point.z, 1.0);
            trafo.getTransform().transform(point4d);
            return point4d;

        } else {
            System.out.println("Error: Cannot transfrom " + source + " --> " + target);
            return new Vector4d();
        }

    }

    private void test() throws InterruptedException, TransformerException, TransformerFactory.TransformerFactoryException {
        receiver = TransformerFactory.getInstance().createTransformReceiver();

        System.out.println("Gathering available transformations ...");
        Thread.sleep(1000);

        // The different systems and the base
        String[] targets = {"java_static", "java_dynamic", "python_static", "python_dynamic"};
        String base = "base";

        Vector4d point = new Vector4d(1.0, 1.0, 1.0, 1.0);
        System.out.println("Point to transform: " + point + "\n");

        for (String target : targets) {
            // Forward transfrom
            Vector4d transformed_point = transform_now(base, target, point);
            System.out.println("[" + point + "]  -->  " + "[" + transformed_point + "]");

            // Backward transfrom
            Vector4d re_transformed_point = transform_now(target, base, transformed_point);
            System.out.println("[" + transformed_point + "]  -->  " + "[" + re_transformed_point + "]");

            System.out.println();
        }
        System.exit(0);
    }

    public static void usage() {
        System.out.println("ERROR: Please provide task argument!");
        System.out.println();
        System.out.println("Usage: program [TASK_ARGUMENT] (where TASK_ARGUMENT is either 'provide' or 'test')");
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            String start_type = args[0];
            try {
                Cross_lib_provider prov = new Cross_lib_provider();
                if (start_type.equals("provide")) {
                    prov.provide();
                } else if (start_type.equals("test")) {
                    prov.test();

                } else {
                    System.out.println("Unknown type: " + start_type);
                    usage();
                }
            } catch (InterruptedException | TransformerException | TransformerFactory.TransformerFactoryException ex) {
                Logger.getLogger(Cross_lib_provider.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("No arg given ...");
            usage();
        }
    }

}
