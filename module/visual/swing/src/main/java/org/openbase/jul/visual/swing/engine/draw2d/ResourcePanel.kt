package org.openbase.jul.visual.swing.engine.draw2d

import java.awt.Graphics2D
import java.awt.event.MouseEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D

interface ResourcePanel {
    val parentPanel: ResourceDisplayPanel<out ResourcePanel>
    val parentResourcePanel: ResourcePanel
    val boundingBox: Rectangle2D
    val skaleImageToBoundsTransformation: AffineTransform
    fun updateObjectTransformationAndBounds(objectTransform: AffineTransform)

    fun getName(): String

    fun addChild(child: ResourcePanel, drawLayer: AbstractResourcePanel.DrawLayer)

    fun removeChild(child: ResourcePanel)

    fun mouseClicked(evt: MouseEvent)

    fun getSelectedInstance(evt: MouseEvent): ResourcePanel?

    fun paint(g: Graphics2D, glg: Graphics2D)

    fun updateBounds(): Rectangle2D

    fun getFocusable(): ResourcePanel

    fun notifyMouseClicked(evt: MouseEvent)

    fun setState(state: AbstractResourcePanel.VisibleResourcePanelState)

    fun setMouseState(mouseState: AbstractResourcePanel.VisibleResourcePanelMouseState)
}
