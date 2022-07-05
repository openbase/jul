package org.openbase.rct;

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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

/**
 * A transform from one coordinate frame "parent" to "child". It holds the
 * transformation itself as a Java3D {@link Transform3D} object, the parent and
 * child frame IDs, the time at which it was created from the database and the
 * ID of the authority that generated this object.
 * 
 * @author lziegler
 *
 */
public class Transform {

	private Transform3D transform;
	private String frameParent;
	private String frameChild;
	private long time;
	private String authority;

	/**
	 * Creates a new transform object as copy from another transform object.
	 * 
	 * @param transform The original to copy from.
	 */
	public Transform(final Transform transform) {
		this.transform = new Transform3D(transform.transform);
		this.frameParent = transform.frameParent;
		this.frameChild = transform.frameChild;
		this.time = transform.time;
		this.authority = transform.authority;
	}

	/**
	 * Creates a new transform object.
	 * 
	 * @param transform
	 *            The transform itself as Java3D {@link Transform3D}
	 * @param frameParent
	 *            The parent coordinate frame ID
	 * @param frameChild
	 *            The child coordinate frame ID
	 * @param time
	 *            The time at which this object was created.
	 */
	public Transform(Transform3D transform, String frameParent, String frameChild, long time) {
		this.transform = transform;
		this.frameParent = frameParent;
		this.frameChild = frameChild;
		this.time = time;
	}

	/**
	 * Getter for the geometric transform representation.
	 * 
	 * @return The transform as Java3D {@link Transform3D}
	 */
	public Transform3D getTransform() {
		return transform;
	}

	/**
	 * Getter for the translation part of the complete transform.
	 * 
	 * @return The translation as Java3D Vecmath {@link Vector3d}
	 */
	public Vector3d getTranslation() {
		Vector3d translation = new Vector3d();
		transform.get(translation);
		return translation;
	}

	/**
	 * Getter for the rotation part of the complete transform.
	 * 
	 * @return The rotation as Java3D Vecmath {@link Quat4d}
	 */
	public Quat4d getRotationQuat() {
		Quat4d quat = new Quat4d();
		transform.get(quat);
		return quat;
	}

	/**
	 * Getter for the rotation part of the complete transform.
	 * 
	 * @return The rotation as Java3D Vecmath {@link Matrix3d}
	 */
	public Matrix3d getRotationMatrix() {
		Matrix3d rot = new Matrix3d();
		transform.get(rot);
		return rot;
	}

	/**
	 * Getter for the rotation part of the complete transform as yaw, pitch and
	 * roll angles.
	 * 
	 * @return The yaw, pitch and roll angles as Java3D Vecmath {@link Vector3d}
	 */
	public Vector3d getRotationYPR() {

		Matrix3d rot = getRotationMatrix();

		// this code is taken from buttel btMatrix3x3 getEulerYPR().
		// http://bulletphysics.org/Bullet/BulletFull/btMatrix3x3_8h_source.html
		// first use the normal calculus
		double yawOut = Math.atan2(rot.m10, rot.m00);
		double pitchOut = Math.asin(-rot.m20);
		double rollOut = Math.atan2(rot.m21, rot.m22);

		// on pitch = +/-HalfPI
		if (Math.abs(pitchOut) == Math.PI / 2.0) {
			if (yawOut > 0)
				yawOut -= Math.PI;
			else
				yawOut += Math.PI;
			if (pitchOut > 0)
				pitchOut -= Math.PI;
			else
				pitchOut += Math.PI;
		}

		return new Vector3d(yawOut, pitchOut, rollOut);
	}

	/**
	 * Setter for the geometric transform object.
	 * 
	 * @param transform
	 *            The transform as Java3D {@link Transform3D}
	 */
	public void setTransform(Transform3D transform) {
		this.transform = transform;
	}

	/**
	 * Getter for the ID of the parent coordinate frame.
	 * 
	 * @return The frame ID.
	 */
	public String getFrameParent() {
		return frameParent;
	}

	/**
	 * Setter for the ID of the parent coordinate frame.
	 * 
	 * @param frameParent
	 *            The frame ID.
	 */
	public void setFrameParent(String frameParent) {
		this.frameParent = frameParent;
	}

	/**
	 * Getter for the ID of the child coordinate frame.
	 * 
	 * @return The frame ID.
	 */
	public String getFrameChild() {
		return frameChild;
	}

	/**
	 * Setter for the ID of the child coordinate frame.
	 * 
	 * @param frameChild
	 *            The frame ID.
	 */
	public void setFrameChild(String frameChild) {
		this.frameChild = frameChild;
	}

	/**
	 * Getter for the timestamp of the moment this object was created.
	 * 
	 * @return The timestamp in milliseconds
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Setter for the timestamp of the moment this object was created.
	 * @param time the time to set.
	 *
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * Getter for the ID of the authority that created this object.
	 * 
	 * @return The authority ID
	 */
	public String getAuthority() {
		return authority;
	}

	/**
	 * Setter for the ID of the authority that created this object.
	 * 
	 * @param authority The authority ID
	 */
	public void setAuthority(String authority) {
		this.authority = authority;
	}

	@Override
	public String toString() {

		Matrix4d mat = new Matrix4d();
		transform.get(mat);
		String tStr = String
				.format("{%.2f %.2f %.2f %.2f; %.2f %.2f %.2f %.2f; %.2f %.2f %.2f %.2f; %.2f %.2f %.2f %.2f}",
						mat.m00, mat.m01, mat.m02, mat.m03, mat.m10, mat.m11,
						mat.m12, mat.m13, mat.m20, mat.m21, mat.m22, mat.m23,
						mat.m30, mat.m31, mat.m32, mat.m33);

		return "Transform[parent:" + frameParent + "; child:" + frameChild
				+ "; time:" + time + "; transform:" + tStr + "]";
	}

	public boolean equalsWithoutTime(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		Transform rhs = (Transform) obj;
		return new EqualsBuilder().
				append(frameChild, rhs.frameChild).
				append(frameParent, rhs.frameParent).
				append(transform, rhs.transform).
				isEquals();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		Transform rhs = (Transform) obj;
		return new EqualsBuilder().
				append(frameChild, rhs.frameChild).
				append(frameParent, rhs.frameParent).
				append(time, rhs.time).
				append(transform, rhs.transform).
				isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(13, 53).
				append(frameChild).
				append(frameParent).
				append(time).
				append(transform).
				toHashCode();
	}
}
