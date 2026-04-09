package org.openbase.jul.visual.swing.engine.draw2d

import org.netbeans.lib.awtextra.AbsoluteConstraints
import org.openbase.jps.core.JPService
import org.openbase.jps.exception.JPNotAvailableException
import org.openbase.jps.preset.JPVisualDebugMode
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.exception.printer.ExceptionPrinter
import org.openbase.jul.iface.provider.NameProvider
import org.openbase.jul.visual.swing.engine.draw2d.AbstractResourcePanel
import org.openbase.jul.visual.swing.image.ImageLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Polygon
import java.awt.Shape
import java.awt.event.MouseEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.awt.image.VolatileImage
import java.util.*
import javax.swing.JComponent

/**
 *
 * @author [Divine Threepwood](mailto:divine@openbase.org)
 * @param <R>
 * @param <RP>
 * @param <PRP>
</PRP></RP></R> */
abstract class AbstractResourcePanel<R : NameProvider, PRP : ResourcePanel>: ResourcePanel {
    enum class VisibleResourcePanelState {
        Selected, Unselected, Unknown
    }

    enum class VisibleResourcePanelMouseState {
        Rollover, Klick, Untouched, Unknown
    }

    enum class DrawLayer {
        FORGROUND, BACKGROUND
    }

    enum class ObjectType {
        Static, Dynamic
    }

    final override val parentPanel: ResourceDisplayPanel<out ResourcePanel>
    final override val parentResourcePanel: PRP
    val resource: R

    val placementPolygon: Polygon?
    private val jComponents: ArrayList<JComponent>
    final override var boundingBox: Rectangle2D
        protected set
    var transformedBoundingBox: Rectangle2D
        private set
    protected var tranformedPlacement: Shape? = null
    var state: VisibleResourcePanelState
        private set
    private var mouseState: VisibleResourcePanelMouseState

    protected var image: VolatileImage? = null
    protected val childrens: LinkedList<ResourcePanel>
    private val CHILDREN_MONITOR = Any()
    var objectType: ObjectType

    constructor(
        resource: R,
        placementPolygon: Polygon?,
        objectType: ObjectType,
        parentResourcePanel: PRP?,
        drawLayer: DrawLayer? = null,
        parentPanel: ResourceDisplayPanel<out ResourcePanel>,
        linkAsChild: Boolean = false,
    ) {
        this.resource = resource
        this.objectType = objectType
        this.state = VisibleResourcePanelState.Unselected
        this.mouseState = VisibleResourcePanelMouseState.Untouched
        this.parentResourcePanel = parentResourcePanel?: this as PRP
        this.parentPanel = parentPanel
        this.placementPolygon = placementPolygon
        this.boundingBox = placementPolygon!!.bounds2D
        this.transformedBoundingBox = Rectangle2D.Double()
        this.jComponents = ArrayList()
        this.childrens = LinkedList()
        if(linkAsChild) {
            this.parentResourcePanel.addChild(this, drawLayer!!)
        }
    }

    constructor(resource: R, placementPolygon: Polygon, imageURI: String?, parentPanel: ResourceDisplayPanel<out ResourcePanel>) : this(
        resource,
        placementPolygon,
        ObjectType.Static,
        imageURI,
        parentPanel
    )

    constructor(
        resource: R,
        placementPolygon: Polygon,
        objectType: ObjectType,
        imageURI: String?,
        parentPanel: ResourceDisplayPanel<out ResourcePanel>
    ) : this(resource, placementPolygon, parentPanel) {
        this.objectType = objectType
        if (imageURI !== NO_IMAGE) {
            try {
                this.image = ImageLoader.getInstance().loadVolatileImage(imageURI)
            } catch (ex: CouldNotPerformException) {
                ExceptionPrinter.printHistory(CouldNotPerformException("Could not load $this image.", ex), LOGGER)
            }
        }
    }

    constructor(resource: R, placementPolygon: Polygon, parentPanel: ResourceDisplayPanel<out ResourcePanel>) : this(
        resource,
        placementPolygon,
        ObjectType.Static,
        parentPanel
    )

    constructor(resource: R,
                placementPolygon: Polygon,
                objectType: ObjectType,
                parentPanel: ResourceDisplayPanel<out ResourcePanel>
    ) : this (
        resource = resource,
        placementPolygon = placementPolygon,
        objectType = objectType,
        parentResourcePanel = null,
        parentPanel = parentPanel,
    )

    constructor(
        resource: R,
        placementPolygon: Polygon?,
        imageURI: String?,
        parentResourcePanel: PRP,
        drawLayer: DrawLayer?
    ) : this(resource, placementPolygon, ObjectType.Static, imageURI, parentResourcePanel, drawLayer)

    constructor(
        resource: R,
        placementPolygon: Polygon?,
        objectType: ObjectType,
        imageURI: String?,
        parentResourcePanel: PRP,
        drawLayer: DrawLayer?
    ) : this(resource, placementPolygon, objectType, parentResourcePanel, drawLayer) {
        if (imageURI !== NO_IMAGE) {
            try {
                this.image = imageURI?.let {  ImageLoader.getInstance().loadVolatileImage(it) }
            } catch (ex: CouldNotPerformException) {
                ExceptionPrinter.printHistory(CouldNotPerformException("Could not load $this image.", ex), LOGGER)
            }
        }
    }

    constructor(resource: R, placementPolygon: Polygon?, parentResourcePanel: PRP, drawLayer: DrawLayer?) : this(
        resource,
        placementPolygon,
        ObjectType.Static,
        parentResourcePanel,
        drawLayer
    )

    constructor(
        resource: R,
        placementPolygon: Polygon?,
        objectType: ObjectType,
        parentResourcePanel: PRP,
        drawLayer: DrawLayer?,
    ) : this (
        resource = resource,
        placementPolygon = placementPolygon,
        objectType = objectType,
        parentResourcePanel = parentResourcePanel,
        parentPanel = parentResourcePanel.parentPanel,
        drawLayer = drawLayer,
        linkAsChild = true,
    )

    override fun getName(): String {
        return resource.name
    }

    override fun mouseClicked(evt: MouseEvent) {
    }

    protected fun paintImage(g2: Graphics2D) {
        try {
            g2.drawImage(image, skaleImageToBoundsTransformation, parentPanel)
        } catch (ex: Exception) {
            ExceptionPrinter.printHistory("Could not paint image!", ex, LOGGER)
        }
    }

    protected fun paintImage(image: BufferedImage?, g2: Graphics2D) {
        g2.drawImage(image, skaleImageToBoundsTransformation, parentPanel)
    }

    var mx: Int = 0
    var my: Int = 0

    protected fun containsMousePointer(evt: MouseEvent): Boolean {
//		Logger.info(this, "DEBUG: check contains: " + getClass().getSimpleName());
//		//Visual MousePointer

        if (DEBUG) {
            mx = evt.point.x
            my = evt.point.y
        }
        return transformedBoundingBox.contains(evt.point) && tranformedPlacement!!.contains(evt.point)
    }

    override fun getSelectedInstance(evt: MouseEvent): ResourcePanel? {
        if (containsMousePointer(evt)) {
            var selectedInstance: ResourcePanel?
            for (listener in childrens) {
                selectedInstance = listener.getSelectedInstance(evt)
                if (selectedInstance != null) {
                    return selectedInstance
                }
            }
            return this
        }
        return null
    }

    override fun setMouseState(mouseState: VisibleResourcePanelMouseState) {
        if (this.mouseState == mouseState) {
            return
        }
        this.mouseState = mouseState
        notifyMouseEntered()
        repaint()
    }

    fun getMouseState(): VisibleResourcePanelMouseState {
        return mouseState
    }

    abstract val isFocusable: Boolean

    override fun setState(state: VisibleResourcePanelState) {
        if (this.state == state) {
            return
        }
        this.state = state
        repaint()
    }

    protected abstract fun notifyMouseEntered()

    override val skaleImageToBoundsTransformation: AffineTransform
        get() = AffineTransform(
            boundingBox.width / image!!.width, 0.0,
            0.0, boundingBox.height / image!!.height,
            boundingBox.x, boundingBox.y
        )

    fun repaint() {
        if (parentPanel.isDisplayable) {
            parentPanel.repaint(this)
        }
    }

    override fun paint(g: Graphics2D, glg: Graphics2D) {
//		Logger.info(this, "paint "+this+ " in bounds "+boundingBox);
        val g2 = g.create() as Graphics2D
        val glg2 = glg.create() as Graphics2D
        when (objectType) {
            ObjectType.Dynamic -> {
                g2.transform(parentPanel.objectTransformation)
                glg2.transform(parentPanel.objectTransformation)
                paintComponent(g2, glg2)
                paintChildren(g2, glg2)
            }

            ObjectType.Static -> {
                paintComponent(g2, glg2)
                paintChildren(g2, glg2)
            }
        }
        g2.dispose()
        glg2.dispose()

        if (DEBUG) {
            try {
                glg2.color = Color(0, 200, 0)
                glg2.draw(transformedBoundingBox)
                glg2.color = Color(0, 0, 200)
                glg2.draw(tranformedPlacement!!.bounds2D)
                glg2.color = Color(200, 0, 0)
                glg2.drawLine(
                    transformedBoundingBox.x.toInt(),
                    transformedBoundingBox.centerY.toInt(),
                    transformedBoundingBox.maxX.toInt(),
                    transformedBoundingBox.centerY.toInt()
                )
                glg2.drawLine(
                    transformedBoundingBox.centerX.toInt(),
                    transformedBoundingBox.y.toInt(),
                    transformedBoundingBox.centerX.toInt(),
                    transformedBoundingBox.maxY.toInt()
                )
                if (mx != 0 && my != 0) {
                    glg2.fillOval(mx - 10, my - 10, 20, 20)
                    glg2.drawLine(mx, 0, mx, 100000)
                    glg2.drawLine(0, my, 100000, my)
                }
            } catch (ex: Exception) {
            }
        }
    }

    protected abstract fun paintComponent(g2: Graphics2D, gl: Graphics2D)

    protected fun paintChildren(g: Graphics2D, glg: Graphics2D) {
        synchronized(CHILDREN_MONITOR) {
            for (child in childrens) {
                child.paint(g, glg)
            }
        }
    }

    override fun updateBounds(): Rectangle2D {
        if (parentResourcePanel !== this) {
            parentResourcePanel.boundingBox.add(boundingBox)
            parentResourcePanel.updateBounds()
        }
        return boundingBox
    }

    override fun updateObjectTransformationAndBounds(objectTransform: AffineTransform) {
        tranformedPlacement = objectTransform.createTransformedShape(placementPolygon)
        transformedBoundingBox.setRect(objectTransform.createTransformedShape(boundingBox).bounds2D)
        rearrageJComponents()
        synchronized(CHILDREN_MONITOR) {
            for (child in childrens) {
                child.updateObjectTransformationAndBounds(objectTransform)
            }
        }
    }

    override fun getFocusable(): ResourcePanel {
        if (isFocusable || parentResourcePanel === this) {
            return this
        }
        return parentResourcePanel.getFocusable()
    }

    override fun addChild(child: ResourcePanel, drawLayer: DrawLayer) {
//		Logger.info(this, "Add child "+resource);
        synchronized(CHILDREN_MONITOR) {
            if (drawLayer == DrawLayer.BACKGROUND) {
                childrens.addFirst(child)
            } else if (drawLayer == DrawLayer.FORGROUND) {
                childrens.addLast(child)
            }
        }
        boundingBox.add(child.boundingBox)
        updateBounds()
    }

    override fun removeChild(child: ResourcePanel) {
        synchronized(CHILDREN_MONITOR) {
            childrens.remove(child)
        }
        updateBounds()
    }

    protected fun addJComponent(component: JComponent) {
        if (jComponents.contains(component)) {
            LOGGER.warn("JComponent allready registrated! Ignore new one...")
            return
        }

        jComponents.add(component)
        parentPanel.add(
            component, AbsoluteConstraints(
                transformedBoundingBox.x.toInt(), transformedBoundingBox.y.toInt(),
                transformedBoundingBox.width.toInt(), transformedBoundingBox.height.toInt()
            )
        )
    }

    protected fun removeJComponent(component: JComponent) {
        jComponents.remove(component)
        parentPanel.remove(component)
    }

    private fun rearrageJComponents() {
        for (component in jComponents) {
            parentPanel.remove(component)
            parentPanel.add(
                component, AbsoluteConstraints(
                    transformedBoundingBox.x.toInt(), transformedBoundingBox.y.toInt(),
                    transformedBoundingBox.width.toInt(), transformedBoundingBox.height.toInt()
                )
            )
        }
    }

    override fun toString(): String {
        return javaClass.simpleName + "[Resource:" + resource + "]"
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(AbstractResourcePanel::class.java)

        val NO_IMAGE: Any? = null
        val DEBUG: Boolean

        init {
            var debug = false
            try {
                debug = JPService.getProperty(JPVisualDebugMode::class.java).value
            } catch (ex: JPNotAvailableException) {
                ExceptionPrinter.printHistory(ex, LOGGER)
            }
            DEBUG = debug
        }
    }
}
