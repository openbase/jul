/**
 * ==================================================================
 * <p>
 * This file is part of org.openbase.bco.bcozy.
 * <p>
 * org.openbase.bco.bcozy is free software: you can redistribute it and modify
 * it under the terms of the GNU General Public License (Version 3)
 * as published by the Free Software Foundation.
 * <p>
 * org.openbase.bco.bcozy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with org.openbase.bco.bcozy. If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */
package org.openbase.jul.visual.javafx.geometry.svg;

/*-
 * #%L
 * JUL Visual JavaFX
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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

import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.visual.javafx.JFXConstants;
import org.openbase.jul.visual.javafx.animation.Animations;
import org.openbase.jul.visual.javafx.geometry.ShapeProcessor;
import org.openbase.jul.visual.javafx.geometry.svg.provider.ShapeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hoestreich
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class SVGIcon<PROVIDER, SHAPE_PROVIDER extends ShapeProvider<PROVIDER>> extends StackPane {

    private static final Logger LOGGER = LoggerFactory.getLogger(SVGIcon.class);
    private final SHAPE_PROVIDER shapeProvider;
    private final boolean styled;
    private final IconState iconState;
    private double size;
    private Shape backgroundIcon;
    private Shape backgroundFadeIcon;
    private Shape foregroundIcon;
    private Shape foregroundFadeIcon;
    private FadeTransition foregroundColorFadeAnimation, backgroundIconColorFadeAnimation;
    private RotateTransition foregroundRotateAnimation, backgroundRotateAnimation;

    /**
     * Constructor for a SVGIcon.
     *
     * @param foregroundIconProvider the Icon to be set in the backgroundIcon
     * @param shapeProvider          the provider to use for extracting the {@code Shape} out of the given {@code iconProvider}.
     *                               (can be chosen from one of the supported fonts from fontawesomefx)
     * @param size                   the size in px for the icon
     * @param styled                 true if color should be changed by theme, otherwise false
     */
    public SVGIcon(final PROVIDER foregroundIconProvider, final SHAPE_PROVIDER shapeProvider, final double size, final boolean styled) {
        this(size, styled, shapeProvider);
        this.foregroundIcon = createIcon(foregroundIconProvider, Layer.FOREGROUND);
        this.foregroundFadeIcon = createColorFadeIcon(foregroundIconProvider, Layer.FOREGROUND);
        this.backgroundIcon = null;
        this.backgroundFadeIcon = null;
        this.getChildren().addAll(foregroundIcon, foregroundFadeIcon);
    }

    /**
     * Constructor for a SVGIcon.
     *
     * @param backgroundIconProvider the Icon to be set in the backgroundIcon
     *                               (can be chosen from one of the supported fonts from fontawesomefx)
     * @param foregroundIconProvider the Icon to be set in the foregroundIcon
     * @param shapeProvider          the provider to use for extracting the {@code Shape} out of the given {@code iconProvider}.
     * @param size                   the size in px for the icon
     */
    public SVGIcon(final PROVIDER backgroundIconProvider, final PROVIDER foregroundIconProvider, final SHAPE_PROVIDER shapeProvider, final double size) {
        this(size, true, shapeProvider);
        this.foregroundIcon = createIcon(foregroundIconProvider, Layer.FOREGROUND);
        this.foregroundFadeIcon = createColorFadeIcon(foregroundIconProvider, Layer.FOREGROUND);
        this.backgroundIcon = createIcon(backgroundIconProvider, Layer.BACKGROUND);
        this.backgroundFadeIcon = createColorFadeIcon(backgroundIconProvider, Layer.BACKGROUND);
        this.getChildren().addAll(this.backgroundIcon, this.backgroundFadeIcon, this.foregroundIcon, this.foregroundFadeIcon);
    }

    public SVGIcon(double size, boolean styled, final SHAPE_PROVIDER shapeProvider) {
        this.size = size;
        this.styled = styled;
        this.shapeProvider = shapeProvider;
        this.iconState = new IconState();
        this.setWidth(size);
        this.setHeight(size);

        disableProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean disabled) -> {
            if (disabled) {
                // save values
                iconState.save(this);
                stopIconAnimation();
                setForegroundIconColor(Color.GRAY);
                setBackgroundIconColor(Color.GRAY);
            } else {
                // restore values if exists
                iconState.restore(this);
            }
        });
    }

    private Shape createIcon(final PROVIDER iconProvider, final Layer layer) {
        return createIcon(iconProvider, size, layer == Layer.FOREGROUND && styled);
    }

    private Shape createIcon(final PROVIDER iconProvider, final double size, final boolean styled) {
        Shape shape;

        try {
            shape = shapeProvider.getShape(iconProvider, size);
        } catch (final NotAvailableException ex) {
            ExceptionPrinter.printHistory("Could not create icon!", ex, LOGGER);
            shape = ShapeProcessor.resize(new Text("?"), size);
        }

        shape.setSmooth(true);
        if (styled) {
            shape.getStyleClass().clear();
            shape.getStyleClass().add(JFXConstants.CSS_ICON);
        }
        return shape;
    }

    private Shape createColorFadeIcon(final PROVIDER iconProvider, final Layer layer) {
        return createColorFadeIcon(iconProvider, size, layer);
    }

    private Shape createColorFadeIcon(final PROVIDER iconProvider, final double size, final Layer layer) {
        final Shape icon = createIcon(iconProvider, size, layer == Layer.FOREGROUND);

        // should be only visible on fade.
        icon.setOpacity(JFXConstants.TRANSPARENCY_FULLY);
        return icon;
    }

    /**
     * Apply and play a FadeTransition on the icon in the foregroundIcon.
     * This Transition modifies the opacity of the foregroundIcon from fully transparent to opaque.
     *
     * @param cycleCount the number of times the animation should be played (use Animation.INDEFINITE for endless)
     */
    public void fadeForegroundIconColorFromTransparentToOpaque(final int cycleCount) {
        stopForegroundIconColorFadeAnimation();
        foregroundColorFadeAnimation = Animations.createFadeTransition(foregroundIcon, JFXConstants.TRANSPARENCY_FULLY, JFXConstants.TRANSPARENCY_NONE, cycleCount, JFXConstants.ANIMATION_DURATION_FADE_SLOW);
        foregroundColorFadeAnimation.setOnFinished(event -> foregroundIcon.setOpacity(JFXConstants.TRANSPARENCY_NONE));
        foregroundColorFadeAnimation.play();
    }

    /**
     * Apply and play a FadeTransition on the icon in the foregroundIcon.
     * This Transition modifies the opacity of the foregroundIcon from opaque to fully transparent.
     *
     * @param cycleCount the number of times the animation should be played (use Animation.INDEFINITE for endless)
     */
    public void fadeForegroundIconColorFromOpaqueToTransparent(final int cycleCount) {
        stopForegroundIconColorFadeAnimation();
        foregroundColorFadeAnimation = Animations.createFadeTransition(foregroundIcon, JFXConstants.TRANSPARENCY_NONE, JFXConstants.TRANSPARENCY_FULLY, cycleCount, JFXConstants.ANIMATION_DURATION_FADE_SLOW);
        foregroundColorFadeAnimation.setOnFinished(event -> foregroundIcon.setOpacity(JFXConstants.TRANSPARENCY_FULLY));
        foregroundColorFadeAnimation.play();
    }

    /**
     * Apply and play a FadeTransition on the icon in the foregroundIcon.
     * This Transition modifies the opacity of the foregroundIcon from fully transparent to opaque.
     *
     * @param cycleCount the number of times the animation should be played (use Animation.INDEFINITE for endless)
     */
    public void fadeBackgroundIconColorFromTransparentToOpaque(final int cycleCount) {
        stopBackgroundIconColorFadeAnimation();
        backgroundIconColorFadeAnimation = Animations.createFadeTransition(backgroundIcon, JFXConstants.TRANSPARENCY_FULLY, JFXConstants.TRANSPARENCY_NONE, 1, JFXConstants.ANIMATION_DURATION_FADE_SLOW);
        backgroundIconColorFadeAnimation.setOnFinished(event -> backgroundIcon.setOpacity(JFXConstants.TRANSPARENCY_NONE));
        backgroundIconColorFadeAnimation.play();
    }

    /**
     * Apply and play a FadeTransition on the icon in the foregroundIcon.
     * This Transition modifies the opacity of the foregroundIcon from opaque to fully transparent.
     *
     * @param cycleCount the number of times the animation should be played (use Animation.INDEFINITE for endless)
     */
    public void fadeBackgroundIconColorFromOpaqueToTransparent(final int cycleCount) {
        stopBackgroundIconColorFadeAnimation();
        backgroundIconColorFadeAnimation = Animations.createFadeTransition(backgroundIcon, JFXConstants.TRANSPARENCY_NONE, JFXConstants.TRANSPARENCY_FULLY, cycleCount, JFXConstants.ANIMATION_DURATION_FADE_SLOW);
        backgroundIconColorFadeAnimation.setOnFinished(event -> backgroundIcon.setOpacity(JFXConstants.TRANSPARENCY_FULLY));
        backgroundIconColorFadeAnimation.play();
    }

    /**
     * Method starts the fade animation of the background icon color.
     *
     * @param cycleCount the number of times the animation should be played (use Animation.INDEFINITE for endless)
     */
    public void startForegroundIconColorFadeAnimation(final int cycleCount) {
        foregroundColorFadeAnimation = Animations.createFadeTransition(foregroundIcon, JFXConstants.TRANSPARENCY_FULLY, JFXConstants.TRANSPARENCY_NONE, cycleCount, JFXConstants.ANIMATION_DURATION_FADE_SLOW);
        foregroundColorFadeAnimation.setOnFinished(event -> foregroundIcon.setOpacity(JFXConstants.TRANSPARENCY_FULLY));
        foregroundColorFadeAnimation.play();
    }

    /**
     * Method starts the fade animation of the background icon color.
     *
     * @param cycleCount the number of times the animation should be played (use Animation.INDEFINITE for endless)
     */
    public void startBackgroundIconColorFadeAnimation(final int cycleCount) {
        if (backgroundIcon == null) {
            LOGGER.warn("Background animation skipped because background icon not set!");
            return;
        }
        backgroundIconColorFadeAnimation = Animations.createFadeTransition(backgroundIcon, JFXConstants.TRANSPARENCY_FULLY, JFXConstants.TRANSPARENCY_NONE, cycleCount, JFXConstants.ANIMATION_DURATION_FADE_SLOW);
        backgroundIconColorFadeAnimation.setOnFinished(event -> backgroundIcon.setOpacity(JFXConstants.TRANSPARENCY_FULLY));
        backgroundIconColorFadeAnimation.play();
    }

    /**
     * Method stops the fade animation of the background icon color.
     */
    public void stopForegroundIconColorFadeAnimation() {
        if (foregroundColorFadeAnimation != null) {
            foregroundColorFadeAnimation.stop();
        }
    }

    /**
     * Method stops the fade animation of the background icon color.
     */
    public void stopBackgroundIconColorFadeAnimation() {
        if (backgroundIconColorFadeAnimation != null) {
            backgroundIconColorFadeAnimation.stop();
        }
    }

    /**
     * Method stops the foreground and background fade animation .
     */
    public void stopIconColorFadeAnimation() {
        stopForegroundIconColorFadeAnimation();
        stopBackgroundIconColorFadeAnimation();
    }

    /**
     * Method starts the fade animation of the background icon color.
     * If no animation was previously started than an default infinite animation is applied.
     */
    public void startForegroundIconColorFadeAnimation() {

        // init infinite animation of no animation was previously configured.
        if (foregroundColorFadeAnimation == null) {
            startForegroundIconColorFadeAnimation(Animation.INDEFINITE);
            return;
        }
        foregroundColorFadeAnimation.play();
    }

    /**
     * Method starts the fade animation of the background icon color.
     * If no animation was previously started than an default infinite animation is applied.
     */
    public void startBackgroundIconColorFadeAnimation() {

        // init infinite animation of no animation was previously configured.
        if (backgroundIconColorFadeAnimation == null) {
            startBackgroundIconColorFadeAnimation(Animation.INDEFINITE);
            return;
        }
        backgroundIconColorFadeAnimation.play();
    }

    /**
     * Method starts the foreground and background fade animation .
     */
    public void startIconColorFadeAnimation() {
        startForegroundIconColorFadeAnimation();
        startBackgroundIconColorFadeAnimation();
    }

    /**
     * Allows to set a new color to the foregroundIcon icon and setAnimation its change (by a FadeTransition).
     *
     * @param color      the color for the foregroundIcon icon to be set
     * @param cycleCount the number of times the animation should be played (use Animation.INDEFINITE for endless)
     */
    public void setForegroundIconColorAnimated(final Color color, final int cycleCount) {
        assert color != null;
        stopForegroundIconColorFadeAnimation();
        foregroundFadeIcon.setFill(color);
        foregroundColorFadeAnimation = Animations.createFadeTransition(foregroundFadeIcon, JFXConstants.TRANSPARENCY_FULLY, JFXConstants.TRANSPARENCY_NONE, cycleCount, JFXConstants.ANIMATION_DURATION_FADE_DEFAULT);
        foregroundColorFadeAnimation.setOnFinished(event -> {
            foregroundFadeIcon.setFill(color);
            foregroundFadeIcon.setOpacity(JFXConstants.TRANSPARENCY_FULLY);
        });
        foregroundColorFadeAnimation.play();
    }

    /**
     * Allows to set a new color to the backgroundIcon icon and setAnimation its change (by a FadeTransition).
     *
     * @param color      the color for the backgroundIcon icon to be setfeature-rights-and-access-management
     * @param cycleCount the number of times the animation should be played (use Animation.INDEFINITE for endless)
     */
    public void setBackgroundIconColorAnimated(final Color color, final int cycleCount) {
        if (backgroundIcon == null) {
            LOGGER.warn("Background modification skipped because background icon not set!");
            return;
        }
        stopBackgroundIconColorFadeAnimation();
        backgroundFadeIcon.setFill(color);
        backgroundIconColorFadeAnimation = Animations.createFadeTransition(backgroundFadeIcon, JFXConstants.TRANSPARENCY_FULLY, JFXConstants.TRANSPARENCY_NONE, cycleCount, JFXConstants.ANIMATION_DURATION_FADE_DEFAULT);
        backgroundIconColorFadeAnimation.setOnFinished(event -> {
            backgroundFadeIcon.setFill(color);
            backgroundFadeIcon.setOpacity(JFXConstants.TRANSPARENCY_FULLY);
        });
        backgroundIconColorFadeAnimation.play();
    }

    /**
     * Method starts the rotate animation of the foreground icon.
     *
     * @param fromAngle    the rotation angle where the transition should start.
     * @param toAngle      the rotation angle where the transition should end.
     * @param cycleCount   the number of times the animation should be played (use Animation.INDEFINITE for endless).
     * @param duration     the duration which one animation cycle should take.
     * @param interpolator defines the rotation value interpolation between {@code fromAngle} and {@code toAngle}.
     * @param autoReverse  defines if the animation should be reversed at the end.
     */
    public void startForegroundIconRotateAnimation(final double fromAngle, final double toAngle, final int cycleCount, final double duration, final Interpolator interpolator, final boolean autoReverse) {
        stopForegroundIconRotateAnimation();
        foregroundRotateAnimation = Animations.createRotateTransition(foregroundIcon, fromAngle, toAngle, cycleCount, duration, interpolator, autoReverse);
        foregroundRotateAnimation.setOnFinished(event -> foregroundIcon.setRotate(0));
        foregroundRotateAnimation.play();
    }

    /**
     * Method starts the rotate animation of the background icon.
     *
     * @param fromAngle    the rotation angle where the transition should start.
     * @param toAngle      the rotation angle where the transition should end.
     * @param cycleCount   the number of times the animation should be played (use Animation.INDEFINITE for endless).
     * @param duration     the duration which one animation cycle should take.
     * @param interpolator defines the rotation value interpolation between {@code fromAngle} and {@code toAngle}.
     * @param autoReverse  defines if the animation should be reversed at the end.
     */
    public void startBackgroundIconRotateAnimation(final double fromAngle, final double toAngle, final int cycleCount, final double duration, final Interpolator interpolator, final boolean autoReverse) {
        stopBackgroundIconRotateAnimation();
        backgroundRotateAnimation = Animations.createRotateTransition(backgroundIcon, fromAngle, toAngle, cycleCount, duration, interpolator, autoReverse);
        backgroundRotateAnimation.setOnFinished(event -> backgroundIcon.setRotate(0));
        backgroundRotateAnimation.play();
    }

    /**
     * Method stops the rotate animation of the background icon color.
     */
    public void stopForegroundIconRotateAnimation() {
        if (foregroundRotateAnimation != null) {
            foregroundRotateAnimation.stop();
        }
    }

    /**
     * Method stops the rotate animation of the background icon color.
     */
    public void stopBackgroundIconRotateAnimation() {
        if (backgroundRotateAnimation != null) {
            backgroundRotateAnimation.stop();
        }
    }

    /**
     * Method stops the foreground and background rotate animation.
     */
    public void stopIconRotateAnimation() {
        stopForegroundIconRotateAnimation();
        stopBackgroundIconRotateAnimation();
    }

    /**
     * Method starts the rotate animation of the background icon color.
     * If no animation was previously started than an default infinite animation is applied.
     */
    public void startForegroundIconRotateAnimation() {
        // init infinite animation of no animation was previously configured.
        if (foregroundRotateAnimation == null) {
            startForegroundIconRotateAnimation(0d, 360d, Animation.INDEFINITE, JFXConstants.ANIMATION_DURATION_ROTATION_DEFAULT, Interpolator.LINEAR, false);
            return;
        }
        foregroundRotateAnimation.play();
    }

    /**
     * Method starts the rotate animation of the background icon color.
     * If no animation was previously started than an default infinite animation is applied.
     */
    public void startBackgroundIconRotateAnimation() {
        // init infinite animation of no animation was previously configured.
        if (backgroundRotateAnimation == null) {
            startForegroundIconRotateAnimation(0d, 360d, Animation.INDEFINITE, JFXConstants.ANIMATION_DURATION_ROTATION_DEFAULT, Interpolator.LINEAR, false);
            return;
        }
        backgroundRotateAnimation.play();
    }

    /**
     * Method starts the foreground and background rotate animation.
     */
    public void startIconRotateAnimation() {
        startForegroundIconRotateAnimation();
        startBackgroundIconRotateAnimation();
    }

    /**
     * Method stops the foreground and background fade and rotate animation .
     */
    public void stopIconAnimation() {
        stopIconColorFadeAnimation();
        stopIconRotateAnimation();
    }

    /**
     * Method starts the foreground and background fade and rotate animation .
     */
    public void startIconAnimation() {
        startIconColorFadeAnimation();
        startIconRotateAnimation();
    }

    /**
     * Method sets the icon color and a stroke with a given color and width.
     *
     * @param color   color for the foreground icon to be set
     * @param outline color for the stroke
     * @param width   width of the stroke
     */
    public void setForegroundIconColor(final Color color, final Color outline, final double width) {
        setForegroundIconColor(color);
        foregroundIcon.setStroke(outline);
        foregroundIcon.setStrokeWidth(width);
    }

    /**
     * Method sets the icon color and a stroke with a given color and width.
     *
     * @param color   color for the background icon to be set
     * @param outline color for the stroke
     * @param width   width of the stroke
     */
    public void seBackgroundIconColor(final Color color, final Color outline, final double width) {
        setBackgroundIconColor(color);
        backgroundIcon.setStroke(outline);
        backgroundIcon.setStrokeWidth(width);
    }

    /**
     * Reset the current foreground icon color to default.
     */
    public void setForegroundIconColorDefault() {
        stopForegroundIconColorFadeAnimation();
        foregroundIcon.getStyleClass().clear();
        foregroundIcon.getStyleClass().add(JFXConstants.CSS_ICON);
        foregroundFadeIcon.setFill(Color.TRANSPARENT);
    }

    /**
     * Reset the current background icon color to default.
     */
    public void setBackgroundIconColorDefault() {
        if (backgroundIcon == null) {
            LOGGER.warn("Background modification skipped because background icon not set!");
            return;
        }
        stopBackgroundIconColorFadeAnimation();
        backgroundIcon.getStyleClass().clear();
        backgroundIcon.getStyleClass().add(JFXConstants.CSS_ICON);
        backgroundFadeIcon.setFill(Color.TRANSPARENT);
    }

    /**
     * Reset the current foreground icon color to default.
     */
    public void setForegroundIconColorDefaultInverted() {
        setForegroundIconColorDefault();
        setForegroundIconColor(getForegroundIconColor().invert());
    }

    /**
     * Reset the current background icon color to default.
     */
    public void setBackgroundIconColorDefaultInverted() {
        if (backgroundIcon == null) {
            LOGGER.warn("Background modification skipped because background icon not set!");
            return;
        }
        setBackgroundIconColorDefault();
        setBackgroundIconColor(getBackgroundIconColor().invert());
    }

    /**
     * Changes the foregroundIcon icon.
     * <p>
     * Note: previous color setup and animations are reset as well.
     *
     * @param icon the icon which should be set as the new icon
     */
    public void setForegroundIcon(final PROVIDER icon) {
        setForegroundIcon(icon, null);
    }

    /**
     * Changes the backgroundIcon icon.
     * <p>
     * Note: previous color setup and animations are reset as well.
     *
     * @param icon the icon which should be set as the new icon.
     */
    public void setBackgroundIcon(final PROVIDER icon) {
        setBackgroundIcon(icon, null);
    }

    /**
     * Changes the foregroundIcon icon.
     * <p>
     * Note: previous color setup and animations are reset as well.
     *
     * @param iconProvider the icon which should be set as the new icon.
     * @param color        the color of the new icon.
     */
    public void setForegroundIcon(final PROVIDER iconProvider, final Color color) {
        // copy old images to replace later.
        final Shape oldForegroundIcon = foregroundIcon;
        final Shape oldForegroundFadeIcon = foregroundFadeIcon;

        // create new images.
        this.foregroundIcon = createIcon(iconProvider, Layer.FOREGROUND);
        this.foregroundFadeIcon = createColorFadeIcon(iconProvider, Layer.FOREGROUND);

        // setup icon color
        if (color != null) {
            setForegroundIconColor(color);
        }

        // replace old icons with new ones.
        getChildren().replaceAll((node) -> {
            if (node.equals(oldForegroundIcon)) {
                return foregroundIcon;
            } else if (node.equals(oldForegroundFadeIcon)) {
                return foregroundFadeIcon;
            } else {
                return node;
            }
        });
    }

    /**
     * Changes the backgroundIcon icon.
     * <p>
     * Note: previous color setup and animations are reset as well.
     *
     * @param iconProvider the icon which should be set as the new icon.
     * @param color        the color of the new icon.
     */
    public void setBackgroundIcon(final PROVIDER iconProvider, final Color color) {
        // copy old images to replace later.
        final Shape oldBackgroundIcon = backgroundIcon;
        final Shape oldBackgroundFadeIcon = backgroundFadeIcon;

        // create new images.
        this.backgroundIcon = createIcon(iconProvider, Layer.BACKGROUND);
        this.backgroundFadeIcon = createColorFadeIcon(iconProvider, Layer.BACKGROUND);

        // setup icon color
        if (color != null) {
            setBackgroundIconColor(color);
        }

        // add background icon if not exists
        if (oldBackgroundIcon == null || oldBackgroundFadeIcon == null) {
            getChildren().clear();
            getChildren().addAll(this.backgroundIcon, this.backgroundFadeIcon, this.foregroundIcon, this.foregroundFadeIcon);
            return;
        }

        // replace old icons with new ones.
        getChildren().replaceAll((node) -> {
            if (node.equals(oldBackgroundIcon)) {
                return backgroundIcon;
            } else if (node.equals(oldBackgroundFadeIcon)) {
                return backgroundFadeIcon;
            } else {
                return node;
            }
        });
    }

    /**
     * Getter for the color of the foreground icons.
     *
     * @return color value
     */
    public Color getForegroundIconColor() {
        return (Color) foregroundIcon.getFill();
    }

    /**
     * Method sets the icon color only.
     *
     * @param color the color for the foregroundIcon icon to be set
     */
    public void setForegroundIconColor(final Color color) {
        stopForegroundIconColorFadeAnimation();
        foregroundIcon.setFill(color);
        foregroundIcon.setStroke(Color.TRANSPARENT);
        foregroundIcon.setStrokeWidth(0);
        foregroundFadeIcon.setFill(Color.TRANSPARENT);
    }

    /**
     * Getter for the color of the background icons.
     *
     * @return color value
     */
    public Color getBackgroundIconColor() {
        if (backgroundIcon == null) {
            LOGGER.warn("Background color unknown because background icon not set!");
            return Color.TRANSPARENT;
        }
        return (Color) backgroundIcon.getFill();
    }

    /**
     * Method sets the icon color only.
     *
     * @param color the color for the backgroundIcon icon to be set
     */
    public void setBackgroundIconColor(final Color color) {
        if (backgroundIcon == null) {
            LOGGER.warn("Background modification skipped because background icon not set!");
            return;
        }
        stopBackgroundIconColorFadeAnimation();
        backgroundIcon.setFill(color);
//        backgroundIcon.setStroke(Color.TRANSPARENT);
//        backgroundIcon.setStrokeWidth(0);
        backgroundFadeIcon.setFill(Color.TRANSPARENT);
    }

    /**
     * Getter for the size of the icons.
     *
     * @return size as a double value
     */
    public double getSize() {
        return size;
    }

    public void setSize(final double size) {
        this.size = size;
        setWidth(size);
        setHeight(size);

        if (backgroundIcon != null) {
            backgroundIcon = ShapeProcessor.resize(backgroundIcon, size);
        }
        if (backgroundFadeIcon != null) {
            backgroundFadeIcon = ShapeProcessor.resize(backgroundFadeIcon, size);
        }
        if (foregroundIcon != null) {
            foregroundIcon = ShapeProcessor.resize(foregroundIcon, size);
        }
        if (foregroundFadeIcon != null) {
            foregroundFadeIcon = ShapeProcessor.resize(foregroundFadeIcon, size);
        }

        requestLayout();
    }

    public void setSize(final double width, final double height) {
        this.size = Math.max(width, height);
        setWidth(width);
        setHeight(height);

        if (backgroundIcon != null) {
            backgroundIcon = ShapeProcessor.resize(backgroundIcon, width, height);
        }
        if (backgroundFadeIcon != null) {
            backgroundFadeIcon = ShapeProcessor.resize(backgroundFadeIcon, width, height);
        }
        if (foregroundIcon != null) {
            foregroundIcon = ShapeProcessor.resize(foregroundIcon, width, height);
        }
        if (foregroundFadeIcon != null) {
            foregroundFadeIcon = ShapeProcessor.resize(foregroundFadeIcon, width, height);
        }

        requestLayout();
    }

    public enum Layer {
        FOREGROUND,
        BACKGROUND
    }

    private class IconState {

        private Boolean foregroundIconColorFadeAnimationEnabled, backgroundIconColorFadeAnimationEnabled, foregroundIconRotateAnimationEnabled, backgroundIconRotateAnimationEnabled;
        private Color foregroundIconColor, backgroundIconColor;

        public Boolean isForegroundIconColorFadeAnimationEnabled() {
            return foregroundIconColorFadeAnimationEnabled;
        }

        public Boolean isBackgroundIconColorFadeAnimationEnabled() {
            return backgroundIconColorFadeAnimationEnabled;
        }

        public Boolean isForegroundIconRotateAnimationEnabled() {
            return foregroundIconRotateAnimationEnabled;
        }

        public Boolean isBackgroundIconRotateAnimationEnabled() {
            return backgroundIconRotateAnimationEnabled;
        }

        public Color getForegroundIconColor() {
            return foregroundIconColor;
        }

        public Color getBackgroundIconColor() {
            return backgroundIconColor;
        }

        public void save(final SVGIcon icon) {
            assert icon != null;
            foregroundIconColorFadeAnimationEnabled = icon.foregroundColorFadeAnimation != null && icon.foregroundColorFadeAnimation.getStatus().equals(Status.RUNNING);
            backgroundIconColorFadeAnimationEnabled = icon.backgroundIconColorFadeAnimation != null && icon.backgroundIconColorFadeAnimation.getStatus().equals(Status.RUNNING);
            foregroundIconRotateAnimationEnabled = icon.foregroundRotateAnimation != null && icon.foregroundRotateAnimation.getStatus().equals(Status.RUNNING);
            backgroundIconRotateAnimationEnabled = icon.backgroundRotateAnimation != null && icon.backgroundRotateAnimation.getStatus().equals(Status.RUNNING);
            foregroundIconColor = icon.getForegroundIconColor();
            backgroundIconColor = icon.getBackgroundIconColor();
        }

        public void restore(final SVGIcon icon) {
            if (icon == null) {
                return;
            }

            if (foregroundIconColorFadeAnimationEnabled != null) {
                if (foregroundIconColorFadeAnimationEnabled) {
                    startForegroundIconColorFadeAnimation();
                } else {
                    stopForegroundIconColorFadeAnimation();
                }
            }

            if (backgroundIconColorFadeAnimationEnabled != null) {
                if (backgroundIconColorFadeAnimationEnabled) {
                    startBackgroundIconColorFadeAnimation();
                } else {
                    stopBackgroundIconColorFadeAnimation();
                }
            }

            if (foregroundIconRotateAnimationEnabled != null) {
                if (foregroundIconRotateAnimationEnabled) {
                    startForegroundIconRotateAnimation();
                } else {
                    stopForegroundIconRotateAnimation();
                }
            }

            if (backgroundIconRotateAnimationEnabled != null) {
                if (backgroundIconRotateAnimationEnabled) {
                    startBackgroundIconRotateAnimation();
                } else {
                    stopBackgroundIconRotateAnimation();
                }
            }

            if (foregroundIconColor != null) {
                icon.setForegroundIconColor(foregroundIconColor);
            }

            if (backgroundIconColor != null) {
                icon.setBackgroundIconColor(backgroundIconColor);
            }
        }
    }
}
