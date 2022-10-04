package org.openbase.rct.impl

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.openbase.rct.Transform
import org.openbase.rct.TransformerException
import org.slf4j.LoggerFactory
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.media.j3d.Transform3D
import javax.vecmath.Quat4d
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
 */   class TransformerCoreDefaultTest {
    fun generateDefaultTransform(): Transform {
        val q = Quat4d(0.0, 1.0, 2.0, 1.0)
        val v = Vector3d(0.0, 1.0, 2.0)
        val t = Transform3D(q, v, 1.0)
        val transform = Transform(t, "foo", "bar", 0)
        transform.authority = TransformerCoreDefaultTest::class.java.simpleName
        return transform
    }

    @Timeout(10)
    @Test
    @Throws(TransformerException::class)
    fun testSetTransformIllegalArguments() {
        val transform = generateDefaultTransform()
        try {
            // test same frame name
            val transform0 = Transform(transform)
            transform0.parentNode = "foo"
            transform0.childNode = "foo"
            val core = TransformerCoreDefault(1000)
            core.setTransform(listOf(transform0), false)
            Assertions.fail<Any>("a TransformerException was expected")
        } catch (ex: TransformerException) {
            LOGGER.debug("expected transformer exception", ex)
        }
        try {
            // test empty frame name
            val transform0 = Transform(transform)
            transform0.parentNode = ""
            transform0.childNode = "foo"
            val core = TransformerCoreDefault(1000)
            core.setTransform(listOf(transform0), false)
            Assertions.fail<Any>("a TransformerException was expected")
        } catch (ex: TransformerException) {
            LOGGER.debug("expected transformer exception", ex)
        }
        try {
            // test nan
            val v0 = Vector3d(0.0, 1.0, Double.NaN)
            val q = Quat4d(0.0, 1.0, 2.0, 1.0)
            val t0 = Transform3D(q, v0, 1.0)
            val transform0 = Transform(transform)
            transform0.transform = t0
            val core = TransformerCoreDefault(1000)
            core.setTransform(listOf(transform0), false)
            Assertions.fail<Any>("a TransformerException was expected")
        } catch (ex: TransformerException) {
            LOGGER.debug("expected transformer exception", ex)
        }
    }

    @Timeout(10)
    @Test
    @Throws(TransformerException::class)
    fun testSetTransformNonStatic() {
        val transform = generateDefaultTransform()
        val transform1 = Transform(transform)
        val core = TransformerCoreDefault(1000)
        core.setTransform(listOf(transform1), false)
        var framesAsString = core.allFramesAsString()
        LOGGER.debug("framesAsString (0): $framesAsString")
        Assertions.assertTrue(framesAsString.contains("foo"))
        Assertions.assertTrue(framesAsString.contains("bar"))
        Assertions.assertFalse(framesAsString.contains("baz"))
        val transform2 = Transform(transform)
        transform2.parentNode = "bar"
        transform2.childNode = "baz"
        core.setTransform(listOf(transform2), false)
        framesAsString = core.allFramesAsString()
        LOGGER.debug("framesAsString (1): $framesAsString")
        Assertions.assertTrue(framesAsString.contains("foo"))
        Assertions.assertTrue(framesAsString.contains("bar"))
        Assertions.assertTrue(framesAsString.contains("baz"))
    }

    @Timeout(10)
    @Test
    @Throws(TransformerException::class)
    fun testSetTransformStatic() {
        val transform = generateDefaultTransform()
        val transform1 = Transform(transform)
        val core = TransformerCoreDefault(1000)
        core.setTransform(listOf(transform1), true)
        var framesAsString = core.allFramesAsString()
        LOGGER.debug("framesAsString (0): $framesAsString")
        Assertions.assertTrue(framesAsString.contains("foo"))
        Assertions.assertTrue(framesAsString.contains("bar"))
        Assertions.assertFalse(framesAsString.contains("baz"))
        val transform2 = Transform(transform)
        transform2.parentNode = "bar"
        transform2.childNode = "baz"
        core.setTransform(listOf(transform2), true)
        framesAsString = core.allFramesAsString()
        LOGGER.debug("framesAsString (1): $framesAsString")
        Assertions.assertTrue(framesAsString.contains("foo"))
        Assertions.assertTrue(framesAsString.contains("bar"))
        Assertions.assertTrue(framesAsString.contains("baz"))
    }

    @Timeout(10)
    @Test
    @Throws(TransformerException::class, InterruptedException::class)
    fun testLookupTransformIllegalArguments() {
        val transform1 = generateDefaultTransform()
        val core = TransformerCoreDefault(1000)
        core.setTransform(listOf(transform1), false)
        try {
            // expect exception because interpolation with one value is not possible
            core.lookupTransform("foo", "bar", 1)
            Assertions.fail<Any>("interpolation with one value is not possible")
        } catch (ex: TransformerException) {
            // expected
        }
        transform1.time = 10
        core.setTransform(listOf(transform1), false)
        try {
            // expect exception because extrapolation into future is not possible
            core.lookupTransform("foo", "bar", 100)
            Assertions.fail<Any>("extrapolation into future is not possible")
        } catch (ex: TransformerException) {
            // expected
        }
    }

    @Timeout(10)
    @Test
    @Throws(TransformerException::class, InterruptedException::class)
    fun testLookupTransformNonStatic() {
        val transform = generateDefaultTransform()
        val core = TransformerCoreDefault(1000)
        core.setTransform(listOf(transform), false)
        transform.time = 10
        core.setTransform(listOf(transform), false)

        // lookup A->B
        val out0 = core.lookupTransform("foo", "bar", 5)
        LOGGER.debug(out0.toString())
        Assertions.assertEquals("foo", out0.parentNode)
        Assertions.assertEquals("bar", out0.childNode)
        Assertions.assertEquals(Vector3d(0.0, 1.0, 2.0), out0.translation)

        // lookup B->A
        val out1 = core.lookupTransform("bar", "foo", 5)
        LOGGER.debug(out1.toString())
        Assertions.assertEquals("bar", out1.parentNode)
        Assertions.assertEquals("foo", out1.childNode)
        Assertions.assertEquals(Vector3d(0.0, -1.0, -2.0), out1.translation)

        // add additional transform C
        transform.parentNode = "bar"
        transform.childNode = "baz"
        transform.time = 0
        core.setTransform(listOf(transform), false)
        transform.time = 10
        core.setTransform(listOf(transform), false)

        // lookup A->C
        val out2 = core.lookupTransform("foo", "baz", 5)
        LOGGER.debug(out2.toString())
        Assertions.assertEquals("foo", out2.parentNode)
        Assertions.assertEquals("baz", out2.childNode)
        Assertions.assertEquals(Vector3d(0.0, 2.0, 4.0), out2.translation)

        // lookup C->A
        val out3 = core.lookupTransform("baz", "foo", 5)
        LOGGER.debug(out3.toString())
        Assertions.assertEquals("baz", out3.parentNode)
        Assertions.assertEquals("foo", out3.childNode)
        Assertions.assertEquals(Vector3d(0.0, -2.0, -4.0), out3.translation)
    }

    @Timeout(10)
    @Test
    @Throws(TransformerException::class, InterruptedException::class)
    fun testLookupTransformStatic() {
        val transform = generateDefaultTransform()
        val core = TransformerCoreDefault(1000)
        core.setTransform(listOf(transform), true)

        // lookup A->B
        val out0 = core.lookupTransform("foo", "bar", 5)
        LOGGER.debug(out0.toString())
        Assertions.assertEquals("foo", out0.parentNode)
        Assertions.assertEquals("bar", out0.childNode)
        Assertions.assertEquals(Vector3d(0.0, 1.0, 2.0), out0.translation)

        // lookup B->A
        val out1 = core.lookupTransform("bar", "foo", 5)
        LOGGER.debug(out1.toString())
        Assertions.assertEquals("bar", out1.parentNode)
        Assertions.assertEquals("foo", out1.childNode)
        Assertions.assertEquals(Vector3d(0.0, -1.0, -2.0), out1.translation)

        // add additional transform C
        transform.parentNode = "bar"
        transform.childNode = "baz"
        transform.time = 0
        core.setTransform(listOf(transform), true)

        // lookup A->C
        val out2 = core.lookupTransform("foo", "baz", 5)
        LOGGER.debug(out2.toString())
        Assertions.assertEquals("foo", out2.parentNode)
        Assertions.assertEquals("baz", out2.childNode)
        Assertions.assertEquals(Vector3d(0.0, 2.0, 4.0), out2.translation)

        // lookup C->A
        val out3 = core.lookupTransform("baz", "foo", 5)
        LOGGER.debug(out3.toString())
        Assertions.assertEquals("baz", out3.parentNode)
        Assertions.assertEquals("foo", out3.childNode)
        Assertions.assertEquals(Vector3d(0.0, -2.0, -4.0), out3.translation)
    }

    @Timeout(10)
    @Test
    @Throws(TransformerException::class, InterruptedException::class)
    fun testcanTransformIllegalArguments() {
        val transform1 = generateDefaultTransform()
        val core = TransformerCoreDefault(1000)
        core.setTransform(listOf(transform1), false)

        // expect exception because interpolation with one value is not possible
        Assertions.assertFalse(core.canTransform("foo", "bar", 1))
        transform1.time = 10
        core.setTransform(listOf(transform1), false)

        // expect exception because extrapolation into future is not possible
        Assertions.assertFalse(core.canTransform("foo", "bar", 100))
    }

    @Timeout(10)
    @Test
    @Throws(TransformerException::class, InterruptedException::class)
    fun testCanTransformNonStatic() {
        val transform = generateDefaultTransform()
        val core = TransformerCoreDefault(1000)
        core.setTransform(listOf(transform), false)
        transform.time = 10
        core.setTransform(listOf(transform), false)

        // lookup A->B
        Assertions.assertTrue(core.canTransform("foo", "bar", 5))

        // lookup B->A
        Assertions.assertTrue(core.canTransform("bar", "foo", 5))

        // add additional transform C
        transform.parentNode = "bar"
        transform.childNode = "baz"
        transform.time = 0
        core.setTransform(listOf(transform), false)
        transform.time = 10
        core.setTransform(listOf(transform), false)

        // lookup A->C
        Assertions.assertTrue(core.canTransform("foo", "baz", 5))

        // lookup C->A
        Assertions.assertTrue(core.canTransform("baz", "foo", 5))
    }

    @Timeout(10)
    @Test
    @Throws(TransformerException::class, InterruptedException::class)
    fun testCanTransformStatic() {
        val transform = generateDefaultTransform()
        val core = TransformerCoreDefault(1000)
        core.setTransform(listOf(transform), true)

        // lookup A->B
        Assertions.assertTrue(core.canTransform("foo", "bar", 5))

        // lookup B->A
        Assertions.assertTrue(core.canTransform("bar", "foo", 5))

        // add additional transform C
        transform.parentNode = "bar"
        transform.childNode = "baz"
        transform.time = 0
        core.setTransform(listOf(transform), true)

        // lookup A->C
        Assertions.assertTrue(core.canTransform("foo", "baz", 5))

        // lookup C->A
        Assertions.assertTrue(core.canTransform("baz", "foo", 5))
    }

    @Timeout(10)
    @Test
    @Throws(
        InterruptedException::class,
        ExecutionException::class,
        TransformerException::class,
        TimeoutException::class
    )
    fun testRequestTransform() {
        val core = TransformerCoreDefault(1000)
        var future = core.requestTransform("foo", "bar", 5)
        Assertions.assertFalse(future.isDone)
        Assertions.assertFalse(future.isCancelled)
        try {
            val t = future[400, TimeUnit.MILLISECONDS]
            LOGGER.error("wrong object: $t")
            Assertions.fail<Any>("not available yet")
        } catch (ex: TimeoutException) {
            // expected
        }
        val transform = generateDefaultTransform()
        core.setTransform(listOf(transform), true)
        val out0 = future[400, TimeUnit.MILLISECONDS]
        LOGGER.debug(out0.toString())
        Assertions.assertEquals("foo", out0.parentNode)
        Assertions.assertEquals("bar", out0.childNode)
        Assertions.assertEquals(Vector3d(0.0, 1.0, 2.0), out0.translation)
        Assertions.assertTrue(future.isDone)
        Assertions.assertFalse(future.isCancelled)
        val out1 = future.get()
        LOGGER.debug(out1.toString())
        Assertions.assertEquals("foo", out1.parentNode)
        Assertions.assertEquals("bar", out1.childNode)
        Assertions.assertEquals(Vector3d(0.0, 1.0, 2.0), out1.translation)

        // ---------
        future = core.requestTransform("foo", "baz", 5)
        try {
            val t = future[400, TimeUnit.MILLISECONDS]
            LOGGER.error("wrong object: $t")
            Assertions.fail<Any>("not available yet")
        } catch (ex: TimeoutException) {
            // expected
        }
        Assertions.assertFalse(future.isDone)
        Assertions.assertFalse(future.isCancelled)
        future.cancel(true)
        Assertions.assertTrue(future.isDone)
        Assertions.assertTrue(future.isCancelled)
        try {
            val t = future[400, TimeUnit.MILLISECONDS]
            LOGGER.error("wrong object: $t")
            Assertions.fail<Any>("is cancelled")
        } catch (ex: CancellationException) {
            // expected
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(TransformerCoreDefaultTest::class.java)
    }
}
