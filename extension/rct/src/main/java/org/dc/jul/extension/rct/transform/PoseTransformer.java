/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rct.transform;

import javax.media.j3d.Transform3D;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import rct.Transform;
import rst.geometry.PoseType;
import rst.geometry.RotationType;
import rst.geometry.TranslationType;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class PoseTransformer {

    public static Transform transform(final PoseType.Pose position, String frameParent, String frameChild) {
        RotationType.Rotation pRotation = position.getRotation();
        TranslationType.Translation pTranslation = position.getTranslation();
        Quat4d jRotation = new Quat4d(pRotation.getQx(), pRotation.getQy(), pRotation.getQz(), pRotation.getQw());
        Vector3d jTranslation = new Vector3d(pTranslation.getX(), pTranslation.getY(), pTranslation.getZ());
        Transform3D transform3D = new Transform3D(jRotation, jTranslation, 1.0);
        return new Transform(transform3D, frameParent, frameChild, System.currentTimeMillis());
    }
}
