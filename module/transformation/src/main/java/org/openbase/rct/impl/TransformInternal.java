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

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.openbase.rct.Transform;

public class TransformInternal {

    Vector3d translation = new Vector3d();
    Quat4d rotation = new Quat4d();
    long stamp = 0;
    int frame_id = 0;
    int child_frame_id = 0;

    public TransformInternal() {

    }

    public TransformInternal(Vector3d translation, Quat4d rotation, int frameNumber, int childFrameNumber, long time) {
        this.translation = translation;
        this.rotation = rotation;
        this.frame_id = frameNumber;
        this.child_frame_id = childFrameNumber;
        this.stamp = time;
    }

    public TransformInternal(Transform t, int frameNumber, int childFrameNumber) {
        this.translation = t.getTranslation();
        this.rotation = t.getRotationQuat();
        this.stamp = t.getTime();
        this.frame_id = frameNumber;
        this.child_frame_id = childFrameNumber;
    }

    TransformInternal(TransformInternal rhs) {
        this.translation = rhs.translation;
        this.rotation = rhs.rotation;
        this.frame_id = rhs.frame_id;
        this.child_frame_id = rhs.child_frame_id;
        this.stamp = rhs.stamp;
    }

    public void replaceWith(TransformInternal rhs) {
        this.translation = rhs.translation;
        this.rotation = rhs.rotation;
        this.frame_id = rhs.frame_id;
        this.child_frame_id = rhs.child_frame_id;
        this.stamp = rhs.stamp;
    }

    @Override
    public String toString() {
        String translationStr = String.format("{%.2f; %.2f; %.2f}", translation.x, translation.y, translation.z);
        String rotationStr = String.format("{w:%.2f; x:%.2f; y:%.2f; z:%.2f}", rotation.w, rotation.x, rotation.y, rotation.z);
        return "TransformInternal[parent:" + frame_id + ",child:" + child_frame_id + ",stamp:" + stamp + ",t:" + translationStr + ",r:" + rotationStr + "]";
    }
}
