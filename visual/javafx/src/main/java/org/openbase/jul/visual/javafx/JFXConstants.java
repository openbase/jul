package org.openbase.jul.visual.javafx;

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


/**
 * @author hoestreich
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public final class JFXConstants {

    /**
     * The size for a big size icon.
     */
    public static final double ICON_SIZE_HUGE = 128.0;
    /**
     * The size for a mid size icon.
     */
    public static final double ICON_SIZE_BIG = 64.0;
    /**
     * The size for a mid size icon.
     */
    public static final double ICON_SIZE_MIDDLE = 42.0;
    /**
     * The size for a small size icon.
     */
    public static final double ICON_SIZE_SMALL = 32.0;
    /**
     * The size for a extra small size icon.
     */
    public static final double ICON_SIZE_EXTRA_SMALL = 16.0;
    /**
     * The size for a extra extra small size icon.
     */
    public static final double ICON_SIZE_EXTRA_EXTRA_SMALL = 10.0;
    /**
     * The opacity value for fully transparent = invisible.
     */
    public static final double TRANSPARENCY_FULLY = 0.0;
    /**
     * The opacity value for fully opaque = visible.
     */
    public static final double TRANSPARENCY_NONE = 1.0;
    /**
     * The opacity value for nearly opaque style.
     */
    public static final double TRANSPARENCY_NEARLY = 0.3;
    /**
     * The opacity value for half opaque style.
     */
    public static final double TRANSPARENCY_HALF = 0.5;

    /**
     * The name of the default css file.
     */
    public static final String CSS_DEFAULT = "/css/skin.css";

    /**
     * String for CSS styling of icons.
     */
    public static final String CSS_ICON = "icons";

    /**
     * The duration for a pretty fast rotation animation.
     */
    public static final double DURATION_ROTATE_FAST = 100.0;

    /**
     * The duration for a pretty fast fade animation.
     */
    public static final double ANIMATION_DURATION_FADE_FAST = 100.0;
    /**
     * The duration for a slow fade animation.
     */
    public static final double ANIMATION_DURATION_FADE_SLOW = 2000.0;
    /**
     * The duration for a fade animation where the icon looks like it would glow.
     */
    public static final double ANIMATION_DURATION_FADE_GLOWING = 2000.0;
    /**
     * The duration for a default fade animation.
     */
    public static final double ANIMATION_DURATION_FADE_DEFAULT = 400.0;
    /**
     * The duration for a default rotate animation.
     */
    public static final double ANIMATION_DURATION_ROTATION_DEFAULT = 1500;

    /**
     * Private Constructor.
     */
    private JFXConstants() {
    }
}
