package org.openbase.jul.visual.javafx.geometry.svg;

/*-
 * #%L
 * JUL Visual JavaFX
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.openbase.jul.visual.javafx.control.AbstractFXController;

public class SVGLogoPaneController extends AbstractFXController {

    private double size;

    private SVGIcon svgIcon;

    @FXML
    private StackPane stack;

    @FXML
    private Text text;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initContent() {
        try {
            final double size = Math.min(stack.getPrefHeight(), stack.getPrefWidth());
            stack.setPrefSize(size, size);
            stack.requestLayout();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Method sets the text displayed next to the logo.
     * @param text
     */
    public void setText(final String text) {
        this.text.setText(text);
        this.text.setFont(Font.font(this.text.getFont().getFamily(), FontWeight.BOLD, size / 2));
        stack.requestLayout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDynamicContent() {
        // nothing to update
    }

    /**
     * Method sets the icon to display.
     * @param svgIcon
     */
    public void setSvgIcon(final SVGIcon svgIcon) {
        if (this.svgIcon != null) {
            stack.getChildren().remove(this.svgIcon);
        }
        this.svgIcon = svgIcon;
        this.svgIcon.setSize(size);
        stack.getChildren().add(this.svgIcon);
        stack.requestLayout();
    }

    /**
     * Method can be used to set the size of this component.
     * @param size
     */
    public void setSize(final double size) {
        this.size = size;
        stack.setPrefSize(size, size);
        text.setFont(Font.font(text.getFont().getFamily(), FontWeight.BOLD, size / 2));
        svgIcon.setSize(size);
        stack.requestLayout();
    }

    /**
     * Method returns the currently displayed icon.
     * @return the icon which is currently displayed.
     */
    public SVGIcon getSVGIcon() {
        return svgIcon;
    }

    public Text getTextPane() {
        return text;
    }
}
