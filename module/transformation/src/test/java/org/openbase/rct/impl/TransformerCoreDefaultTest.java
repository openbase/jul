package org.openbase.rct.impl;

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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.media.j3d.Transform3D;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openbase.rct.Transform;
import org.openbase.rct.TransformerException;
import org.slf4j.LoggerFactory;

public class TransformerCoreDefaultTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TransformerCoreDefaultTest.class);
	
	Transform generateDefaultTransform() {
		Quat4d q = new Quat4d(0, 1, 2, 1);
		Vector3d v = new Vector3d(0, 1, 2);
		Transform3D t = new Transform3D(q,v,1);
		Transform transform = new Transform(t,"foo", "bar", 0);
		transform.setAuthority(TransformerCoreDefaultTest.class.getSimpleName());
		return transform;
	}

	@Timeout(10)
    @Test
	public void testSetTransformIllegalArguments() throws TransformerException {

		Transform transform = generateDefaultTransform();

		try {
			// test same frame name
			Transform transform0 = new Transform(transform);
			transform0.setFrameParent("foo");
			transform0.setFrameChild("foo");
			TransformerCoreDefault core = new TransformerCoreDefault(1000);
			core.setTransform(transform0, false);
			Assertions.fail("a TransformerException was expected");
		} catch(TransformerException ex) {
			LOGGER.debug("expected transformer exception", ex);
		}
		
		try {
			// test empty frame name
			Transform transform0 = new Transform(transform);
			transform0.setFrameParent("");
			transform0.setFrameChild("foo");
			TransformerCoreDefault core = new TransformerCoreDefault(1000);
			core.setTransform(transform0, false);
			Assertions.fail("a TransformerException was expected");
		} catch(TransformerException ex) {
			LOGGER.debug("expected transformer exception", ex);
		}
		
		try {
			// test nan
			Vector3d v0 = new Vector3d(0, 1, Double.NaN);
			Quat4d q = new Quat4d(0, 1, 2, 1);
			Transform3D t0 = new Transform3D(q,v0,1);
			Transform transform0 = new Transform(transform);
			transform0.setTransform(t0);
			TransformerCoreDefault core = new TransformerCoreDefault(1000);
			core.setTransform(transform0, false);
			Assertions.fail("a TransformerException was expected");
		} catch(TransformerException ex) {
			LOGGER.debug("expected transformer exception", ex);
		}
	}

	@Timeout(10)
    @Test
	public void testSetTransformNonStatic() throws TransformerException {

		Transform transform = generateDefaultTransform();
		
		Transform transform1 = new Transform(transform);
		TransformerCoreDefault core = new TransformerCoreDefault(1000);
		core.setTransform(transform1, false);

		String framesAsString = core.allFramesAsString();
		LOGGER.debug("framesAsString (0): " + framesAsString);

		Assertions.assertTrue(framesAsString.contains("foo"));
		Assertions.assertTrue(framesAsString.contains("bar"));
		Assertions.assertFalse(framesAsString.contains("baz"));

		Transform transform2 = new Transform(transform);
		transform2.setFrameParent("bar");
		transform2.setFrameChild("baz");

		core.setTransform(transform2, false);
		framesAsString = core.allFramesAsString();
		LOGGER.debug("framesAsString (1): " + framesAsString);

		Assertions.assertTrue(framesAsString.contains("foo"));
		Assertions.assertTrue(framesAsString.contains("bar"));
		Assertions.assertTrue(framesAsString.contains("baz"));
	}

	@Timeout(10)
    @Test
	public void testSetTransformStatic() throws TransformerException {

		Transform transform = generateDefaultTransform();
		
		Transform transform1 = new Transform(transform);
		TransformerCoreDefault core = new TransformerCoreDefault(1000);
		core.setTransform(transform1, true);

		String framesAsString = core.allFramesAsString();
		LOGGER.debug("framesAsString (0): " + framesAsString);

		Assertions.assertTrue(framesAsString.contains("foo"));
		Assertions.assertTrue(framesAsString.contains("bar"));
		Assertions.assertFalse(framesAsString.contains("baz"));

		Transform transform2 = new Transform(transform);
		transform2.setFrameParent("bar");
		transform2.setFrameChild("baz");

		core.setTransform(transform2, true);
		framesAsString = core.allFramesAsString();
		LOGGER.debug("framesAsString (1): " + framesAsString);

		Assertions.assertTrue(framesAsString.contains("foo"));
		Assertions.assertTrue(framesAsString.contains("bar"));
		Assertions.assertTrue(framesAsString.contains("baz"));
	}

	@Timeout(10)
    @Test
	public void testLookupTransformIllegalArguments() throws TransformerException, InterruptedException {

		Transform transform1 = generateDefaultTransform();

		TransformerCoreDefault core = new TransformerCoreDefault(1000);
		core.setTransform(transform1, false);
		
		try{
			// expect exception because interpolation with one value is not possible
			core.lookupTransform("foo", "bar", 1);
			Assertions.fail("interpolation with one value is not possible");
		} catch (TransformerException ex) {
			// expected
		}
		transform1.setTime(10);
		core.setTransform(transform1, false);
		
		try{
			// expect exception because extrapolation into future is not possible
			core.lookupTransform("foo", "bar", 100);
			Assertions.fail("extrapolation into future is not possible");
		} catch (TransformerException ex) {
			// expected
		}
	}
	@Timeout(10)
    @Test
	public void testLookupTransformNonStatic() throws TransformerException, InterruptedException {

		Transform transform = generateDefaultTransform();
		TransformerCoreDefault core = new TransformerCoreDefault(1000);
		core.setTransform(transform, false);
		transform.setTime(10);
		core.setTransform(transform, false);
		
		// lookup A->B
		Transform out0 = core.lookupTransform("foo", "bar", 5);
		LOGGER.debug(out0.toString());
		Assertions.assertEquals("foo", out0.getFrameParent());
		Assertions.assertEquals("bar", out0.getFrameChild());
		Assertions.assertEquals(new Vector3d(0, 1, 2), out0.getTranslation());
		
		// lookup B->A
		Transform out1 = core.lookupTransform("bar", "foo", 5);
		LOGGER.debug(out1.toString());
		Assertions.assertEquals("bar", out1.getFrameParent());
		Assertions.assertEquals("foo", out1.getFrameChild());
		Assertions.assertEquals(new Vector3d(0, -1, -2), out1.getTranslation());
		
		// add additional transform C
		transform.setFrameParent("bar");
		transform.setFrameChild("baz");
		transform.setTime(0);
		core.setTransform(transform, false);
		transform.setTime(10);
		core.setTransform(transform, false);
		
		// lookup A->C
		Transform out2 = core.lookupTransform("foo", "baz", 5);
		LOGGER.debug(out2.toString());
		Assertions.assertEquals("foo", out2.getFrameParent());
		Assertions.assertEquals("baz", out2.getFrameChild());
		Assertions.assertEquals(new Vector3d(0, 2, 4), out2.getTranslation());
		
		// lookup C->A
		Transform out3 = core.lookupTransform("baz", "foo", 5);
		LOGGER.debug(out3.toString());
		Assertions.assertEquals("baz", out3.getFrameParent());
		Assertions.assertEquals("foo", out3.getFrameChild());
		Assertions.assertEquals(new Vector3d(0, -2, -4), out3.getTranslation());
	}
	
	@Timeout(10)
    @Test
	public void testLookupTransformStatic() throws TransformerException, InterruptedException {

		Transform transform = generateDefaultTransform();
		TransformerCoreDefault core = new TransformerCoreDefault(1000);
		core.setTransform(transform, true);
		
		// lookup A->B
		Transform out0 = core.lookupTransform("foo", "bar", 5);
		LOGGER.debug(out0.toString());
		Assertions.assertEquals("foo", out0.getFrameParent());
		Assertions.assertEquals("bar", out0.getFrameChild());
		Assertions.assertEquals(new Vector3d(0, 1, 2), out0.getTranslation());
		
		// lookup B->A
		Transform out1 = core.lookupTransform("bar", "foo", 5);
		LOGGER.debug(out1.toString());
		Assertions.assertEquals("bar", out1.getFrameParent());
		Assertions.assertEquals("foo", out1.getFrameChild());
		Assertions.assertEquals(new Vector3d(0, -1, -2), out1.getTranslation());
		
		// add additional transform C
		transform.setFrameParent("bar");
		transform.setFrameChild("baz");
		transform.setTime(0);
		core.setTransform(transform, true);
		
		// lookup A->C
		Transform out2 = core.lookupTransform("foo", "baz", 5);
		LOGGER.debug(out2.toString());
		Assertions.assertEquals("foo", out2.getFrameParent());
		Assertions.assertEquals("baz", out2.getFrameChild());
		Assertions.assertEquals(new Vector3d(0, 2, 4), out2.getTranslation());
		
		// lookup C->A
		Transform out3 = core.lookupTransform("baz", "foo", 5);
		LOGGER.debug(out3.toString());
		Assertions.assertEquals("baz", out3.getFrameParent());
		Assertions.assertEquals("foo", out3.getFrameChild());
		Assertions.assertEquals(new Vector3d(0, -2, -4), out3.getTranslation());
	}
	

	@Timeout(10)
    @Test
	public void testcanTransformIllegalArguments() throws TransformerException, InterruptedException {

		Transform transform1 = generateDefaultTransform();

		TransformerCoreDefault core = new TransformerCoreDefault(1000);
		core.setTransform(transform1, false);
		
			// expect exception because interpolation with one value is not possible
		Assertions.assertFalse(core.canTransform("foo", "bar", 1));
		transform1.setTime(10);
		core.setTransform(transform1, false);
		
		// expect exception because extrapolation into future is not possible
		Assertions.assertFalse(core.canTransform("foo", "bar", 100));
	}
	
	@Timeout(10)
    @Test
	public void testCanTransformNonStatic() throws TransformerException, InterruptedException {

		Transform transform = generateDefaultTransform();
		TransformerCoreDefault core = new TransformerCoreDefault(1000);
		core.setTransform(transform, false);
		transform.setTime(10);
		core.setTransform(transform, false);
		
		// lookup A->B
		Assertions.assertTrue(core.canTransform("foo", "bar", 5));
		
		// lookup B->A
		Assertions.assertTrue(core.canTransform("bar", "foo", 5));
		
		// add additional transform C
		transform.setFrameParent("bar");
		transform.setFrameChild("baz");
		transform.setTime(0);
		core.setTransform(transform, false);
		transform.setTime(10);
		core.setTransform(transform, false);
		
		// lookup A->C
		Assertions.assertTrue(core.canTransform("foo", "baz", 5));
		
		// lookup C->A
		Assertions.assertTrue(core.canTransform("baz", "foo", 5));
	}
	
	@Timeout(10)
    @Test
	public void testCanTransformStatic() throws TransformerException, InterruptedException {

		Transform transform = generateDefaultTransform();
		TransformerCoreDefault core = new TransformerCoreDefault(1000);
		core.setTransform(transform, true);
		
		// lookup A->B
		Assertions.assertTrue(core.canTransform("foo", "bar", 5));
		
		// lookup B->A
		Assertions.assertTrue(core.canTransform("bar", "foo", 5));
		
		// add additional transform C
		transform.setFrameParent("bar");
		transform.setFrameChild("baz");
		transform.setTime(0);
		core.setTransform(transform, true);
		
		// lookup A->C
		Assertions.assertTrue(core.canTransform("foo", "baz", 5));
		
		// lookup C->A
		Assertions.assertTrue(core.canTransform("baz", "foo", 5));
	}
	
	@Timeout(10)
    @Test
	public void testRequestTransform() throws InterruptedException, ExecutionException, TransformerException, TimeoutException {
		TransformerCoreDefault core = new TransformerCoreDefault(1000);
		Future<Transform> future = core.requestTransform("foo", "bar", 5);
		
		Assertions.assertFalse(future.isDone());
		Assertions.assertFalse(future.isCancelled());
		
		try {
			Transform t = future.get(400, TimeUnit.MILLISECONDS);
			LOGGER.error("wrong object: " + t);
			Assertions.fail("not available yet");
		} catch(TimeoutException ex) {
			// expected
		}
		
		Transform transform = generateDefaultTransform();
		core.setTransform(transform, true);
		
		Transform out0 = future.get(400, TimeUnit.MILLISECONDS);
		LOGGER.debug(out0.toString());
		Assertions.assertEquals("foo", out0.getFrameParent());
		Assertions.assertEquals("bar", out0.getFrameChild());
		Assertions.assertEquals(new Vector3d(0, 1, 2), out0.getTranslation());
		
		Assertions.assertTrue(future.isDone());
		Assertions.assertFalse(future.isCancelled());
		
		Transform out1 = future.get();
		LOGGER.debug(out1.toString());
		Assertions.assertEquals("foo", out1.getFrameParent());
		Assertions.assertEquals("bar", out1.getFrameChild());
		Assertions.assertEquals(new Vector3d(0, 1, 2), out1.getTranslation());
		
		// ---------
		
		future = core.requestTransform("foo", "baz", 5);
		
		try {
			Transform t = future.get(400, TimeUnit.MILLISECONDS);
			LOGGER.error("wrong object: " + t);
			Assertions.fail("not available yet");
		} catch(TimeoutException ex) {
			// expected
		}
		
		Assertions.assertFalse(future.isDone());
		Assertions.assertFalse(future.isCancelled());
		future.cancel(true);
		Assertions.assertTrue(future.isDone());
		Assertions.assertTrue(future.isCancelled());
		
		try {
			Transform t = future.get(400, TimeUnit.MILLISECONDS);
			LOGGER.error("wrong object: " + t);
			Assertions.fail("is cancelled");
		} catch(CancellationException ex) {
			// expected
		}
	}
}
