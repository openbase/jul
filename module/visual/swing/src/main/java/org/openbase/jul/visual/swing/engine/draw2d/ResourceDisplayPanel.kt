package org.openbase.jul.visual.swing.engine.draw2d

import org.netbeans.lib.awtextra.AbsoluteLayout
import org.openbase.jul.exception.NotAvailableException
import org.openbase.jul.schedule.LastValueHandler
import org.openbase.jul.visual.swing.engine.draw2d.AbstractResourcePanel.VisibleResourcePanelMouseState
import org.openbase.jul.visual.swing.engine.draw2d.AbstractResourcePanel.VisibleResourcePanelState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.*
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import javax.swing.JPanel
import kotlin.math.min

/**
 *
 * @author [Divine Threepwood](mailto:divine@openbase.org)
 */
open class ResourceDisplayPanel<RP : ResourcePanel> : JPanel() {
    var borderSize: Double = 0.01
    protected var visibleResourcePanel: RP? = null
        private set
    val objectTransformation: AffineTransform = AffineTransform()
    private val mouseMovedHandler: LastValueHandler<MouseEvent>
    private var enteredPanel: ResourcePanel? = null
    private var selectedPanel: ResourcePanel? = null

    private fun mouseClicked(evt: MouseEvent) {
        if (visibleResourcePanel == null) {
            return
        }
        var clickedPanel: ResourcePanel?
        clickedPanel = visibleResourcePanel!!.getSelectedInstance(evt)

        if (clickedPanel == null) {
            clickedPanel = visibleResourcePanel
        }

        try {
            LOGGER.debug("Mouse clicked over " + clickedPanel?.getName())
        } catch (ex: NotAvailableException) {
            LOGGER.debug("Mouse clicked")
        }

        if (clickedPanel === selectedPanel) {
            selectedPanel!!.notifyMouseClicked(evt)
            return
        }

        // update focus
        val nextSelection = clickedPanel!!.getFocusable()

        if (nextSelection === selectedPanel) {
            clickedPanel.notifyMouseClicked(evt)
            return
        }

        if (selectedPanel != null) {
            selectedPanel!!.setState(VisibleResourcePanelState.Unselected)
        }
        selectedPanel = nextSelection
        selectedPanel!!.setState(VisibleResourcePanelState.Selected)
        clickedPanel.notifyMouseClicked(evt)
    }

    private fun mouseMoved(evt: MouseEvent) {
        if (visibleResourcePanel == null) {
            return
        }
        var rolloveredPanel: ResourcePanel?
        rolloveredPanel = visibleResourcePanel!!.getSelectedInstance(evt)
        if (rolloveredPanel == null) {
            rolloveredPanel = visibleResourcePanel
        }

        if (rolloveredPanel === enteredPanel) {
            return
        }

        val nextRollovered = rolloveredPanel!!.getFocusable()

        if (nextRollovered === enteredPanel) {
            return
        }

        if (enteredPanel != null) {
            enteredPanel!!.setMouseState(VisibleResourcePanelMouseState.Untouched)
        }
        enteredPanel = nextRollovered
        nextRollovered.setMouseState(VisibleResourcePanelMouseState.Rollover)
    }

    @Throws(InterruptedException::class)
    fun setVisibleResourcePanel(visibleResourcePanel: RP) {
        mouseMovedHandler.stop()
        this.visibleResourcePanel = visibleResourcePanel
        mouseMovedHandler.start()
        updateObjectTransformation()
        repaint()
    }

    private fun updateObjectTransformation() {
        if (visibleResourcePanel != null) {
            objectTransformation.setToIdentity()
            // Translation intro Center
            objectTransformation.translate(size.getWidth() / 2, size.getHeight() / 2)

            //Scale for best boundingbox resolution
            var scaleFactor: Double = min(
                ((height.toFloat()) / (visibleResourcePanel!!.boundingBox.height.toFloat())).toDouble(),
                ((width.toFloat()) / (visibleResourcePanel!!.boundingBox.width.toFloat())).toDouble()
            )
            scaleFactor -= borderSize.toFloat()
            objectTransformation.scale(scaleFactor.toDouble(), scaleFactor.toDouble())

            // Translation intro base
            objectTransformation.translate(
                -visibleResourcePanel!!.boundingBox.centerX,
                -visibleResourcePanel!!.boundingBox.centerY
            )
            visibleResourcePanel!!.updateObjectTransformationAndBounds(objectTransformation)
        }
    }

    fun loadRenderingHints(g2: Graphics2D?) {
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
    fun repaint(panel: AbstractResourcePanel<*, *>) {
        repaint(panel.transformedBoundingBox.bounds)
    }

    private var glassLayer = BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB)

    init {
        this.layout = AbsoluteLayout()
        this.isOpaque = false
        this.isDoubleBuffered = true

        this.mouseMovedHandler = object : LastValueHandler<MouseEvent>("MouseMoved Handler") {
            override fun handle(evt: MouseEvent) {
                mouseMoved(evt)
            }
        }

        this.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(ex: MouseEvent) {
                this@ResourceDisplayPanel.mouseClicked(ex)
            }
        })

        this.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseMoved(evt: MouseEvent) {
                if (visibleResourcePanel != null) {
                    mouseMovedHandler.setValue(evt)
                }
            }
        })

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(evt: ComponentEvent) {
                isVisible = false
                updateObjectTransformation()
                isVisible = true
                //				repaint();
            }
        })
    }

    public override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        //		Logger.info(this, "paint display with bounds " + visibleResourcePanel.boundingBox);
        if (visibleResourcePanel != null) {
            val g2 = g.create() as Graphics2D
            loadRenderingHints(g2)
            //			g2.setTransform(getObjectTransformation());
            if (bounds !== glassLayer.data.bounds) {
                glassLayer = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            }
            visibleResourcePanel!!.paint(g2, glassLayer.createGraphics())
            g2.drawImage(glassLayer, null, 0, 0)
            g2.dispose()
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ResourceDisplayPanel::class.java)

        private const val serialVersionUID = 4L
    }
}
