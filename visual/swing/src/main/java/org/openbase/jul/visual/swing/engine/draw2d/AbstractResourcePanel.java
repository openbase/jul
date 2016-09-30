package org.openbase.jul.visual.swing.engine.draw2d;

/*-
 * #%L
 * JUL Visual
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import org.openbase.jps.preset.JPVisualDebugMode;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.JComponent;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.provider.NameProvider;
import org.openbase.jul.visual.swing.image.ImageLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <R>
 * @param <RP>
 * @param <PRP>
 */
public abstract class AbstractResourcePanel<R extends NameProvider, RP extends AbstractResourcePanel, PRP extends RP> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractResourcePanel.class);

    public final static Object NO_IMAGE = null;
    public final static boolean DEBUG;

    static {
        boolean debug = false;
        try {
            debug = JPService.getProperty(JPVisualDebugMode.class).getValue();
        } catch (JPNotAvailableException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER);
        }
        DEBUG = debug;
    }

    public enum VisibleResourcePanelState {

        Selected, Unselected, Unknown;
    };

    public enum VisibleResourcePanelMouseState {

        Rollover, Klick, Untouched, Unknown;
    };

    public enum DrawLayer {

        FORGROUND, BACKGROUND;
    };

    public enum ObjectType {

        Static, Dynamic;
    };
    protected final ResourceDisplayPanel parentPanel;
    protected final PRP parentResourcePanel;
    protected final R resource;
    protected final Polygon placementPolygon;
    private final ArrayList<JComponent> jComponents;
    protected Rectangle2D boundingBox;
    private Rectangle2D transformedBoundingBox;
    protected Shape tranformedPlacement;
    protected VisibleResourcePanelState state;
    protected VisibleResourcePanelMouseState mouseState;
    protected VolatileImage image;
    protected final LinkedList<RP> childrens;
    private final Object CHILDREN_MONITOR = new Object();
    protected ObjectType objectType;

    /**
     *
     * @param resource
     * @param placementPolygon
     * @param parentPanel
     * @param imageURI
     * @deprecated use public AbstractResourcePanel(R resource, Polygon
     * placementPolygon, ObjectType objectType, String imageURI,
     * ResourceDisplayPanel parentPanel) instead.
     */
    @Deprecated
    public AbstractResourcePanel(final R resource, final Polygon placementPolygon, final ResourceDisplayPanel parentPanel, final String imageURI) {
        this(resource, placementPolygon, ObjectType.Static, imageURI, parentPanel);
    }

    public AbstractResourcePanel(final R resource, final Polygon placementPolygon, final String imageURI, final ResourceDisplayPanel parentPanel) {
        this(resource, placementPolygon, ObjectType.Static, imageURI, parentPanel);
    }

    public AbstractResourcePanel(final R resource, final Polygon placementPolygon, final ObjectType objectType, final String imageURI, final ResourceDisplayPanel parentPanel) {
        this(resource, placementPolygon, parentPanel);
        this.objectType = objectType;
        if (imageURI != NO_IMAGE) {
            try {
                this.image = ImageLoader.getInstance().loadVolatileImage(imageURI);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not load " + this + " image.", ex), LOGGER);
            }
        }
    }

    public AbstractResourcePanel(R resource, Polygon placementPolygon, ResourceDisplayPanel parentPanel) {
        this(resource, placementPolygon, ObjectType.Static, parentPanel);
    }

    public AbstractResourcePanel(R resource, Polygon placementPolygon, ObjectType objectType, ResourceDisplayPanel parentPanel) {
        this.resource = resource;
        this.objectType = objectType;
        this.state = VisibleResourcePanelState.Unselected;
        this.mouseState = VisibleResourcePanelMouseState.Untouched;
        this.parentResourcePanel = (PRP) this;
        this.parentPanel = parentPanel;
        this.placementPolygon = placementPolygon;
        this.boundingBox = placementPolygon.getBounds2D();
        this.transformedBoundingBox = new Rectangle2D.Double();
        this.jComponents = new ArrayList<JComponent>();
        this.childrens = new LinkedList<RP>();
    }

    /**
     *
     * @param resource
     * @param placementPolygon
     * @param parentResourcePanel
     * @param drawLayer
     * @param imageURI
     * @deprecated use public AbstractResourcePanel(R resource, Polygon
     * placementPolygon, String imageURI, PRP parentResourcePanel, DrawLayer
     * drawLayer) instead.
     */
    @Deprecated
    public AbstractResourcePanel(final R resource, final Polygon placementPolygon, final PRP parentResourcePanel, final DrawLayer drawLayer, final String imageURI) {
        this(resource, placementPolygon, ObjectType.Static, imageURI, parentResourcePanel, drawLayer);
    }

    public AbstractResourcePanel(final R resource, final Polygon placementPolygon, final String imageURI, final PRP parentResourcePanel, final DrawLayer drawLayer) {
        this(resource, placementPolygon, ObjectType.Static, imageURI, parentResourcePanel, drawLayer);
    }

    public AbstractResourcePanel(final R resource, final Polygon placementPolygon, final ObjectType objectType, final String imageURI, final PRP parentResourcePanel, final DrawLayer drawLayer) {
        this(resource, placementPolygon, objectType, parentResourcePanel, drawLayer);
        if (imageURI != NO_IMAGE) {
            try {
                this.image = ImageLoader.getInstance().loadVolatileImage(imageURI);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not load " + this + " image.", ex), LOGGER);
            }
        }
    }

    public AbstractResourcePanel(final R resource, final Polygon placementPolygon, final PRP parentResourcePanel, final DrawLayer drawLayer) {
        this(resource, placementPolygon, ObjectType.Static, parentResourcePanel, drawLayer);
    }

    public AbstractResourcePanel(final R resource, final Polygon placementPolygon, final ObjectType objectType, final PRP parentResourcePanel, final DrawLayer drawLayer) {
        this.resource = resource;
        this.objectType = objectType;
        this.state = VisibleResourcePanelState.Unselected;
        this.mouseState = VisibleResourcePanelMouseState.Untouched;
        this.parentResourcePanel = parentResourcePanel;
        this.parentPanel = parentResourcePanel.getParentPanel();
        this.placementPolygon = placementPolygon;
        this.boundingBox = placementPolygon.getBounds2D();
        this.transformedBoundingBox = new Rectangle2D.Double();
        this.jComponents = new ArrayList<>();
        this.childrens = new LinkedList<>();
        this.parentResourcePanel.addChild(this, drawLayer);
        assert (placementPolygon != null);
    }

    public final void mouseClicked(MouseEvent evt) {
    }

    protected void paintImage(Graphics2D g2) {
        try {
            g2.drawImage(image, getSkaleImageToBoundsTransformation(), parentPanel);
        } catch (Exception ex) {
            LOGGER.error("Could not paint image!", ex);
        }

    }

    protected void paintImage(BufferedImage image, Graphics2D g2) {
        g2.drawImage(image, getSkaleImageToBoundsTransformation(), parentPanel);
    }

    int mx = 0, my = 0;

    protected boolean containsMousePointer(MouseEvent evt) {
//		Logger.info(this, "DEBUG: check contains: " + getClass().getSimpleName());
//		//Visual MousePointer

        if (DEBUG) {
            mx = evt.getPoint().x;
            my = evt.getPoint().y;
        }
        return transformedBoundingBox.contains(evt.getPoint()) ? tranformedPlacement.contains(evt.getPoint()) : false;
    }

    protected AbstractResourcePanel getSelectedInstance(MouseEvent evt) {
        if (containsMousePointer(evt)) {
            AbstractResourcePanel selectedInstance;
            for (RP listener : childrens) {
                selectedInstance = listener.getSelectedInstance(evt);
                if (selectedInstance != null) {
                    return selectedInstance;
                }
            }
            return this;
        }
        return null;
    }

    protected final void setMouseState(VisibleResourcePanelMouseState mouseState) {
        if (this.mouseState == mouseState) {
            return;
        }
        this.mouseState = mouseState;
        notifyMouseEntered();
        repaint();
    }

    public VisibleResourcePanelMouseState getMouseState() {
        return mouseState;
    }

    public abstract boolean isFocusable();

    protected void setState(VisibleResourcePanelState state) {
        if (this.state == state) {
            return;
        }
        this.state = state;
        repaint();
    }

    protected abstract void notifyMouseEntered();

    protected abstract void notifyMouseClicked(MouseEvent evt);

    public VisibleResourcePanelState getState() {
        return state;
    }

    public AffineTransform getSkaleImageToBoundsTransformation() {
        return new AffineTransform(
                boundingBox.getWidth() / image.getWidth(), 0,
                0, boundingBox.getHeight() / image.getHeight(),
                boundingBox.getX(), boundingBox.getY());
    }

    public void repaint() {
        if (parentPanel.isDisplayable()) {
            parentPanel.repaint(this);
        }
    }

    public void paint(Graphics2D g, Graphics2D glg) {
//		Logger.info(this, "paint "+this+ " in bounds "+boundingBox);
        Graphics2D g2 = (Graphics2D) g.create();
        Graphics2D glg2 = (Graphics2D) glg.create();
        switch (objectType) {
            case Dynamic:
//				Graphics2D g2t = (Graphics2D) g2.create();
//				Graphics2D glg2t = (Graphics2D) glg2.create();
                g2.transform(getParentPanel().getObjectTransformation());
                glg2.transform(getParentPanel().getObjectTransformation());
                paintComponent(g2, glg2);
                paintChilderen(g2, glg2);
//				g2t.dispose();
//				glg2t.dispose();
                break;
            case Static:
                paintComponent(g2, glg2);
                paintChilderen(g2, glg2);
                break;
        }
        g2.dispose();
        glg2.dispose();

        if (DEBUG) {
            try {
                glg2.setColor(new Color(0, 200, 0));
                glg2.draw(transformedBoundingBox);
                glg2.setColor(new Color(0, 0, 200));
                glg2.draw(tranformedPlacement.getBounds2D());
                glg2.setColor(new Color(200, 0, 0));
                glg2.drawLine((int) transformedBoundingBox.getX(), (int) transformedBoundingBox.getCenterY(), (int) transformedBoundingBox.getMaxX(), (int) transformedBoundingBox.getCenterY());
                glg2.drawLine((int) transformedBoundingBox.getCenterX(), (int) transformedBoundingBox.getY(), (int) transformedBoundingBox.getCenterX(), (int) transformedBoundingBox.getMaxY());
                if (mx != 0 && my != 0) {
                    glg2.fillOval(mx - 10, my - 10, 20, 20);
                    glg2.drawLine(mx, 0, mx, 100000);
                    glg2.drawLine(0, my, 100000, my);
                }
            } catch (Exception ex) {
            }
        }

    }

    protected abstract void paintComponent(Graphics2D g2, Graphics2D gl);

    protected void paintChilderen(Graphics2D g, Graphics2D glg) {
        synchronized (CHILDREN_MONITOR) {
            for (AbstractResourcePanel child : childrens) {
//				long time = System.currentTimeMillis();
                child.paint(g, glg);
//				System.out.println("Compontent["+child.getClass().getSimpleName()+"] update time: "+(System.currentTimeMillis()-time));
            }
        }
    }

    public Rectangle2D updateBounds() {
        if (parentResourcePanel != this) {
            parentResourcePanel.boundingBox.add(boundingBox);
            parentResourcePanel.updateBounds();
        }
        return boundingBox;
    }

    public void updateObjectTransformationAndBounds(AffineTransform objectTransform) {
        tranformedPlacement = objectTransform.createTransformedShape(placementPolygon);
        transformedBoundingBox.setRect(objectTransform.createTransformedShape(boundingBox).getBounds2D());
        rearrageJComponents();
        synchronized (CHILDREN_MONITOR) {
            for (AbstractResourcePanel child : childrens) {
                child.updateObjectTransformationAndBounds(objectTransform);
            }
        }
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public Rectangle2D getBoundingBox() {
        return boundingBox;
    }

    public Rectangle2D getTransformedBoundingBox() {
        return transformedBoundingBox;
    }

    public ResourceDisplayPanel getParentPanel() {
        return parentPanel;
    }

    public R getResource() {
        return resource;
    }

    public PRP getParentResourcePanel() {
        return parentResourcePanel;
    }

    public AbstractResourcePanel getFocusable() {
        if (isFocusable() || parentResourcePanel == this) {
            return this;
        }
        return parentResourcePanel.getFocusable();
    }

    public void addChild(RP child, DrawLayer drawLayer) {
//		Logger.info(this, "Add child "+resource);
        synchronized (CHILDREN_MONITOR) {
            if (drawLayer == DrawLayer.BACKGROUND) {
                childrens.addFirst(child);
            } else if (drawLayer == DrawLayer.FORGROUND) {
                childrens.addLast(child);
            }
        }
        boundingBox.add(child.boundingBox);
        updateBounds();
    }

    public void removeChild(RP resource) {
        synchronized (CHILDREN_MONITOR) {
            childrens.remove(resource);
        }
        updateBounds();
    }

    protected void addJComponent(JComponent component) {
        if (jComponents.contains(component)) {
            LOGGER.warn("JComponent allready registrated! Ignore new one...");
            return;
        }

        jComponents.add(component);
        parentPanel.add(component, new AbsoluteConstraints((int) transformedBoundingBox.getX(), (int) transformedBoundingBox.getY(),
                (int) transformedBoundingBox.getWidth(), (int) transformedBoundingBox.getHeight()));
    }

    protected void removeJComponent(JComponent component) {
        jComponents.remove(component);
        parentPanel.remove(component);
    }

    private void rearrageJComponents() {
        for (JComponent component : jComponents) {
            parentPanel.remove(component);
            parentPanel.add(component, new AbsoluteConstraints((int) transformedBoundingBox.getX(), (int) transformedBoundingBox.getY(),
                    (int) transformedBoundingBox.getWidth(), (int) transformedBoundingBox.getHeight()));
        }
    }

    public Polygon getPlacementPolygon() {
        return placementPolygon;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[Resource:" + resource + "]";
    }
}
