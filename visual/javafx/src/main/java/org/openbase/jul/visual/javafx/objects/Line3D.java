package org.openbase.jul.visual.javafx.objects;

/*-
 * #%L
 * JUL Visual JavaFX
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import javafx.scene.Group;
import javafx.scene.paint.Material;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;

/**
 * This class represents a line in a 3d space. Width, line type, material and
 * start and end points can be specified.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class Line3D extends Group {

    /**
     * Upwards vector used to calculate the rotation axis.
     */
    private final static Point3D UP = new Point3D(0, 1, 0);
    /**
     * The LineType used for this Line object.
     */
    private final LineType type;
    /**
     * JavaFX Box used in case of LineType BOX.
     */
    private Box box;
    /**
     * JavaFX Cylinder used in case of LineType CYLINDER.
     */
    private Cylinder cylinder;

    /**
     * Can be used to specify the looks of the Line object.
     */
    public enum LineType {
        /**
         * A box type line is used.
         */
        BOX,
        /**
         * A cylinder type line is used.
         */
        CYLINDER
    }

    /**
     * Base constructor.
     *
     * @param type The LineType that is going to be used.
     * @param width The line width.
     * @param material The line material.
     */
    public Line3D(final LineType type, double width, final Material material) {
        this.type = type;
        super.setVisible(false);
        switch (type) {
            case BOX:
                box = new Box(width, 0, width);
                box.setMaterial(material);
                super.getChildren().add(box);
                break;
            case CYLINDER:
                cylinder = new Cylinder(width * 0.5, 0);
                cylinder.setMaterial(material);
                super.getChildren().add(cylinder);
                break;

        }
    }

    /**
     * Constructor with start and end intialization.
     *
     * @param type The LineType that is going to be used.
     * @param width The line width.
     * @param material The line material.
     * @param start Start point of the line.
     * @param end End point of the line.
     */
    public Line3D(final LineType type, double width, final Material material, final Point3D start, final Point3D end) {
        this(type, width, material);
        setStartEndPoints(start, end);
    }

    /**
     * Sets the start and end point of the line.
     *
     * @param start Start point of the line.
     * @param end End point of the line.
     */
    public final void setStartEndPoints(final Point3D start, final Point3D end) {
        Point3D direction = start.subtract(end);
        Point3D position = start.midpoint(end);
        setLength(direction.magnitude());
        Point3D axis = UP.crossProduct(direction.normalize());
        super.setVisible(true);
        super.setTranslateX(position.getX());
        super.setTranslateY(position.getY());
        super.setTranslateZ(position.getZ());
        super.setRotationAxis(axis);
        super.setRotate(UP.angle(direction.normalize()));
    }

    /**
     * Sets the Material of the line, which can be used to display different
     * colors.
     *
     * @param material The line material.
     */
    public void setMaterial(final Material material) {
        switch (type) {
            case BOX:
                box.setMaterial(material);
                break;
            case CYLINDER:
                cylinder.setMaterial(material);
                break;
        }
    }

    /**
     * Sets the width of the line.
     *
     * @param width The line width.
     */
    public void setWidth(double width) {
        switch (type) {
            case BOX:
                box.setWidth(width);
                box.setDepth(width);
                break;
            case CYLINDER:
                cylinder.setRadius(width * 0.5);
                break;
        }
    }

    /**
     * Sets the length of the line. This is only used internally to shape the
     * line correctly.
     *
     * @param length Length of the line.
     */
    private void setLength(double length) {
        switch (type) {
            case BOX:
                box.setHeight(length);
                break;
            case CYLINDER:
                cylinder.setHeight(length);
                break;
        }
    }
}
