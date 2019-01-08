package org.openbase.jul.visual.javafx.geometry;

/*-
 * #%L
 * JUL Visual JavaFX
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
import javafx.geometry.Point3D;
import javafx.scene.paint.Material;
import org.openbase.type.geometry.Ray3DFloatType.Ray3DFloat;
import org.openbase.type.math.Vec3DFloatType.Vec3DFloat;

/**
 * This class represents a ray in a 3d space represented by a cylinder. Default width is 2cm.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class Ray3D extends Line3D {

    /**
     * The default length of a ray.
     */
    private final static double DEFAULT_LENGTH = 10;

    /**
     * The default width of a ray.
     */
    private final static double DEFAULT_WIDTH = 0.02;

    /**
     * The length of this ray.
     */
    private final double rayLength;

    /**
     * Transforms a Vec3DFloat object to a Point3D object.
     *
     * @param vector The Vec3DFloat object.
     * @return The transformed Point3D.
     */
    private Point3D VecToPoint(final Vec3DFloat vector) {
        return new Point3D(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * Constructor using default ray length (10 meters).
     *
     * @param material The ray material.
     */
    public Ray3D(final Material material) {
        super(Line3D.LineType.CYLINDER, DEFAULT_WIDTH, material);
        this.rayLength = DEFAULT_LENGTH;
    }

    /**
     * Constructor using the given ray length.
     *
     * @param material The ray material.
     * @param rayLength The ray length.
     */
    public Ray3D(final Material material, double rayLength) {
        super(Line3D.LineType.CYLINDER, DEFAULT_WIDTH, material);
        this.rayLength = rayLength;
    }

    /**
     * Updates the ray orientation and position to the specified data.
     *
     * @param ray Ray3DFloat object defining the placement data of the ray.
     */
    public void update(final Ray3DFloat ray) {
        final Point3D origin = VecToPoint(ray.getOrigin());
        final Point3D direction = VecToPoint(ray.getDirection());
        final Point3D end = origin.add(direction.normalize().multiply(rayLength));
        super.setStartEndPoints(origin, end);
    }
}
