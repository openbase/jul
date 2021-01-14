package org.openbase.jul.visual.swing.engine.draw2d;

/*-
 * #%L
 * JUL Visual Swing
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.netbeans.lib.awtextra.AbsoluteLayout;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.schedule.LastValueHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ResourceDisplayPanel<RP extends AbstractResourcePanel> extends JPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceDisplayPanel.class);

    private static final long serialVersionUID = 4L;
    private double borderSize = 0.01;
    protected RP visibleResourcePanel;
    private final AffineTransform objectTransform;
    private LastValueHandler<MouseEvent> mouseMovedHandler;
    private AbstractResourcePanel enteredPanel, selectedPanel;

    public ResourceDisplayPanel() {
        this.objectTransform = new AffineTransform();
        this.setLayout(new AbsoluteLayout());
        this.setOpaque(false);
        this.setDoubleBuffered(true);

        this.mouseMovedHandler = new LastValueHandler<MouseEvent>("MouseMoved Handler") {
            @Override
            public void handle(MouseEvent evt) {
                mouseMoved(evt);
            }
        };

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent ex) {
                ResourceDisplayPanel.this.mouseClicked(ex);
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent evt) {

                if (visibleResourcePanel != null) {
                    mouseMovedHandler.setValue(evt);
                }
            }
        });

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                setVisible(false);
                updateObjectTransformation();
                setVisible(true);
//				repaint();
            }
        });
    }

    private void mouseClicked(MouseEvent evt) {
        if (visibleResourcePanel == null) {
            return;
        }
        AbstractResourcePanel clickedPanel, nextSelection;
        clickedPanel = visibleResourcePanel.getSelectedInstance(evt);

        if (clickedPanel == null) {
            clickedPanel = visibleResourcePanel;
        }

        try {
            LOGGER.debug("Mouse clicked over " + clickedPanel.getResource().getName());
        } catch (NotAvailableException ex) {
            LOGGER.debug("Mouse clicked");
        }
        
        if (clickedPanel == selectedPanel) {
            selectedPanel.notifyMouseClicked(evt);
            return;
        }

        // update focus
        nextSelection = clickedPanel.getFocusable();

        if (nextSelection == selectedPanel) {
            clickedPanel.notifyMouseClicked(evt);
            return;
        }

        if (selectedPanel != null) {
            selectedPanel.setState(AbstractResourcePanel.VisibleResourcePanelState.Unselected);
        }
        selectedPanel = nextSelection;
        selectedPanel.setState(AbstractResourcePanel.VisibleResourcePanelState.Selected);
        clickedPanel.notifyMouseClicked(evt);
    }

    private void mouseMoved(MouseEvent evt) {
        if (visibleResourcePanel == null) {
            return;
        }
        AbstractResourcePanel rolloveredPanel, nextRollovered;
        rolloveredPanel = visibleResourcePanel.getSelectedInstance(evt);
        if (rolloveredPanel == null) {
            rolloveredPanel = visibleResourcePanel;
        }

        if (rolloveredPanel == enteredPanel) {
            return;
        }

        nextRollovered = rolloveredPanel.getFocusable();

        if (nextRollovered == enteredPanel) {
            return;
        }

        if (enteredPanel != null) {
            enteredPanel.setMouseState(AbstractResourcePanel.VisibleResourcePanelMouseState.Untouched);
        }
        enteredPanel = nextRollovered;
        nextRollovered.setMouseState(AbstractResourcePanel.VisibleResourcePanelMouseState.Rollover);
    }

    public void setVisibleResourcePanel(RP visibleResourcePanel) throws InterruptedException {
        if (visibleResourcePanel != null) {
            mouseMovedHandler.stop();
        }
        this.visibleResourcePanel = visibleResourcePanel;
        if (visibleResourcePanel != null) {
            mouseMovedHandler.start();
        }
        updateObjectTransformation();
        repaint();
    }

    public double getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(double borderSize) {
        this.borderSize = borderSize;
    }

    private void updateObjectTransformation() {
        if (visibleResourcePanel != null) {
            objectTransform.setToIdentity();
            // Translation intro Center
            objectTransform.translate(getSize().getWidth() / 2, getSize().getHeight() / 2);

            //Scale for best boundingbox resolution
            float scaleFactor = Math.min(((float) getHeight()) / ((float) visibleResourcePanel.getBoundingBox().getHeight()), ((float) getWidth()) / ((float) visibleResourcePanel.getBoundingBox().getWidth()));
            scaleFactor -= borderSize;
            objectTransform.scale(scaleFactor, scaleFactor);

            // Translation intro base
            objectTransform.translate(-visibleResourcePanel.getBoundingBox().getCenterX(), -visibleResourcePanel.getBoundingBox().getCenterY());
            visibleResourcePanel.updateObjectTransformationAndBounds(objectTransform);
        }
    }

    public AffineTransform getObjectTransformation() {
        return objectTransform;
    }

    public void loadRenderingHints(Graphics2D g2) {
//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//		g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
//		g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
//
//		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
//		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
//		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
//		g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
//		g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
//
//		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
//		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
//		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
//		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

//	public final static long FRAMERATE = 30;
//	public final static long REPAINT_INTERVALL = 2000;
//	public long repaintTill = 0;
//	
//	public void run() {
//		while(true) {
//			repaint();
//			try {
//				if(System.currentTimeMillis() > repaintTill) {
//					logger.debug("Stop Vis");
//					synchronized(this) {
//						wait();
//					}
//					logger.debug("Start Vis");
//				} else {
//					Thread.sleep(FRAMERATE);
//					//Thread.yield();
//				}
//			} catch (InterruptedException ex) {
//				Logger.getLogger(ResourceDisplayPanel.class.getName()).log(Level.WARN, null, ex);
//			}
//		}
//	}
    public void repaint(AbstractResourcePanel panel) {
        repaint(panel.getTransformedBoundingBox().getBounds());
    }
    private BufferedImage glassLayer = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
//		Logger.info(this, "paint display with bounds " + visibleResourcePanel.boundingBox);
        if (visibleResourcePanel != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            loadRenderingHints(g2);
//			g2.setTransform(getObjectTransformation());
            if (getBounds() != glassLayer.getData().getBounds()) {
                glassLayer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            }
            visibleResourcePanel.paint(g2, glassLayer.createGraphics());
            g2.drawImage(glassLayer, null, 0, 0);
            g2.dispose();
        }
    }
}
