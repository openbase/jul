package org.openbase.rct.examples

import org.openbase.rct.*
import org.openbase.rct.TransformerFactory.TransformerFactoryException
import org.slf4j.LoggerFactory
import java.util.logging.Level
import java.util.logging.Logger
import javax.media.j3d.Transform3D
import javax.vecmath.Quat4f
import javax.vecmath.Vector3d
import javax.vecmath.Vector4d

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
 *
 * @author nkoester
 */
class Cross_lib_provider {
    var receiver: TransformReceiver? = null
    private fun yrp2q(roll: Float, pitch: Float, yaw: Float): Quat4f {
        val halfYaw = yaw * 0.5f
        val halfPitch = pitch * 0.5f
        val halfRoll = roll * 0.5f
        val cosYaw = Math.cos(halfYaw.toDouble()).toFloat()
        val sinYaw = Math.sin(halfYaw.toDouble()).toFloat()
        val cosPitch = Math.cos(halfPitch.toDouble()).toFloat()
        val sinPitch = Math.sin(halfPitch.toDouble()).toFloat()
        val cosRoll = Math.cos(halfRoll.toDouble()).toFloat()
        val sinRoll = Math.sin(halfRoll.toDouble()).toFloat()
        return Quat4f(
            sinRoll * cosPitch * cosYaw - cosRoll * sinPitch * sinYaw,  //x
            cosRoll * sinPitch * cosYaw + sinRoll * cosPitch * sinYaw,  //y
            cosRoll * cosPitch * sinYaw - sinRoll * sinPitch * cosYaw,  //z
            cosRoll * cosPitch * cosYaw + sinRoll * sinPitch * sinYaw
        ) //formerly yzx
    }

    @Throws(InterruptedException::class, TransformerException::class, TransformerFactoryException::class)
    private fun provide() {
        var publisher = TransformerFactory.getInstance().createTransformPublisher("java_provider")

        ////////////////
        // STATIC
        ////////////////
        // Define the translation
        val translation = Vector3d(0.0, 1.0, 1.0)

        // Define the rotation
        //Quat4f rotation = yrp2q(0.0f, 0.0f, 0.0f);
        val rotation = yrp2q(0.0f, 0.0f, -Math.PI.toFloat() / 2)
        val rot_val = Math.PI.toFloat() / 2
        val rot_val_d = Math.PI / 2
        println("Using translation: $translation")
        println("Using rotation   : $rotation")
        // Create the transformation
        val transform = Transform3D(rotation, translation, 1.0)
        val transform_base_java = Transform(transform, "base", "java_static", System.currentTimeMillis())

        // Publish the static offset
        println("Sending static transform now ...")
        publisher.sendTransform(transform_base_java, TransformType.STATIC)

        ////////////////
        // DYNAMIC
        ////////////////
        // translate further along the original y axis
        val translation_dyn = Vector3d(-1.0, 0.0, -2.0)
        // Define the rotation
        val rotation_dyn = yrp2q(0.0f, 0.0f, -Math.PI.toFloat() / 2)

        // Create the transformation
        val transform_dyn = Transform3D(rotation_dyn, translation_dyn, 1.0)
        println("Sending dynamic transform now ...")

        // Publish the static offset
        while (true) {
            val transform_java_java_dynamic =
                Transform(transform_dyn, "java_static", "java_dynamic", System.currentTimeMillis())
            publisher.sendTransform(transform_java_java_dynamic, TransformType.DYNAMIC)
        }
    }

    @Throws(InterruptedException::class, TransformerException::class)
    private fun transform_now(source: String, target: String, point: Vector4d): Vector4d {
        val `when` = System.currentTimeMillis()
        return if (receiver!!.canTransform(target, source, `when`)) {
            val (transform, parentNode, childNode) = receiver!!.lookupTransform(target, source, `when`)
            println("[$childNode]  -->  [$parentNode]")
            val point4d = Vector4d(point.x, point.y, point.z, 1.0)
            transform.transform(point4d)
            point4d
        } else {
            println("Error: Cannot transfrom $source --> $target")
            Vector4d()
        }
    }

    @Throws(InterruptedException::class, TransformerException::class, TransformerFactoryException::class)
    private fun test() {
        receiver = TransformerFactory.getInstance().createTransformReceiver()
        println("Gathering available transformations ...")
        Thread.sleep(1000)

        // The different systems and the base
        val targets = arrayOf("java_static", "java_dynamic", "python_static", "python_dynamic")
        val base = "base"
        val point = Vector4d(1.0, 1.0, 1.0, 1.0)
        println("Point to transform: $point\n")
        for (target in targets) {
            // Forward transfrom
            val transformed_point = transform_now(base, target, point)
            println("[$point]  -->  [$transformed_point]")

            // Backward transfrom
            val re_transformed_point = transform_now(target, base, transformed_point)
            println("[$transformed_point]  -->  [$re_transformed_point]")
            println()
        }
        System.exit(0)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Cross_lib_provider::class.java)
        fun usage() {
            println("ERROR: Please provide task argument!")
            println()
            println("Usage: program [TASK_ARGUMENT] (where TASK_ARGUMENT is either 'provide' or 'test')")
        }

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size == 1) {
                val start_type = args[0]
                try {
                    val prov = Cross_lib_provider()
                    if (start_type == "provide") {
                        prov.provide()
                    } else if (start_type == "test") {
                        prov.test()
                    } else {
                        println("Unknown type: $start_type")
                        usage()
                    }
                } catch (ex: InterruptedException) {
                    Logger.getLogger(Cross_lib_provider::class.java.name).log(Level.SEVERE, null, ex)
                } catch (ex: TransformerException) {
                    Logger.getLogger(Cross_lib_provider::class.java.name).log(Level.SEVERE, null, ex)
                } catch (ex: TransformerFactoryException) {
                    Logger.getLogger(Cross_lib_provider::class.java.name).log(Level.SEVERE, null, ex)
                }
            } else {
                println("No arg given ...")
                usage()
            }
        }
    }
}
