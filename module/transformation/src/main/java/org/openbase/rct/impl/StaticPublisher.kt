package org.openbase.rct.impl

import org.openbase.rct.*
import org.openbase.rct.TransformerFactory.TransformerFactoryException
import java.io.IOException
import javax.media.j3d.Transform3D
import javax.vecmath.Quat4f
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
 */
object StaticPublisher {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val transformer = TransformerFactory.getInstance().createTransformPublisher("static-publisher-java")
            val transform = Transform3D(Quat4f(1f, 0f, 0f, 1f), Vector3d(1.0, 2.0, 3.0), 1.0)
            val t = Transform(transform, "start", "foo", System.currentTimeMillis())
            transformer.sendTransform(t, TransformType.STATIC)
            Thread.sleep(1000)
            println("Press ENTER to exit")
            System.`in`.read()
        } catch (ex: TransformerException) {
            ex.printStackTrace()
            System.exit(1)
        } catch (ex: TransformerFactoryException) {
            ex.printStackTrace()
            System.exit(1)
        } catch (ex: IOException) {
            ex.printStackTrace()
            System.exit(1)
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
            System.exit(1)
        }
        println("done")
        System.exit(0)
    }
}
