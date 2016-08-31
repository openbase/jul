/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.visual.swing.image;

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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import javax.imageio.ImageIO;
import org.openbase.jul.exception.CouldNotPerformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

;

/**
 *
 * @author divine
 */
public class ImageLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageLoader.class);

    private final HashMap<String, BufferedImage> imapeCacheMap;
    private static final ImageLoader instance = new ImageLoader();

    public ImageLoader() {
        this.imapeCacheMap = new HashMap<>();
    }

    public BufferedImage loadImage(String uri) throws CouldNotPerformException {
        if (imapeCacheMap.containsKey(uri)) {
            return imapeCacheMap.get(uri);
        }

        BufferedImage image;
        try {
            URL imageURL = ClassLoader.getSystemResource(uri);
            if (imageURL != null) {
                image = ImageIO.read(imageURL);
            } else {
                File imageFile = new File(uri);
                if (!imageFile.exists()) {
                    throw new FileNotFoundException("Could not found image " + uri);
                }
                image = ImageIO.read(imageFile);
            }
            imapeCacheMap.put(uri, image);
            return image;
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not load image[" + uri + "]!", ex);
        }
    }

    public VolatileImage loadVolatileImage(String uri) throws CouldNotPerformException {

        // Load Image
        BufferedImage image = loadImage(uri);

        // Create Volatile Image
        VolatileImage volatileImage = createVolatileImage(image.getWidth(), image.getHeight(), Transparency.TRANSLUCENT);

        // Copy
        copyBufferedImageIntoVolatileImage(image, volatileImage);

        return volatileImage;
    }

    public static VolatileImage copyBufferedImageIntoVolatileImage(BufferedImage image, VolatileImage volatileImage) {
        Graphics2D g = null;

        try {
            g = volatileImage.createGraphics();

            // clear image to support alpha channel
            g.setComposite(AlphaComposite.Src);
            g.setColor(new Color(0, 0, 0, 0));
            g.fillRect(0, 0, volatileImage.getWidth(), volatileImage.getHeight()); // Clears the image.
            g.drawImage(image, null, 0, 0);

        } finally {
            if (g != null) {
                g.dispose();
            }
        }
        return volatileImage;
    }
    public final static Color DEFAULT_BACKGROUND_MASK_COLOR = Color.GREEN;

    public BufferedImage replaceBackground(BufferedImage image, Color replacement) {
        return replaceBackground(image, DEFAULT_BACKGROUND_MASK_COLOR, replacement);
    }

    public BufferedImage replaceBackground(BufferedImage image, Color identificationColor, Color replacement) {

        Graphics2D g2 = (Graphics2D) image.getGraphics();

        int x, y, i, clr, red, green, blue;

        for (x = 0; x < image.getWidth(); x++) {
            for (y = 0; y < image.getHeight(); y++) {
                // calculate r g b values for each pixel
                clr = image.getRGB(x, y);
                red = (clr & 0x00ff0000) >> 16;
                green = (clr & 0x0000ff00) >> 8;
                blue = clr & 0x000000ff;
                if (image.getRGB(x, y) == identificationColor.getRGB()) {
                    //g2.setComposite(Transparency[i]);
                    g2.setColor(replacement);
                    g2.fillRect(x, y, 1, 1);
                }
            }
        }

        return image;
    }

    private VolatileImage createVolatileImage(int width, int height, int transparency) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        VolatileImage image = null;

        image = gc.createCompatibleVolatileImage(width, height, transparency);

        int valid = image.validate(gc);

        if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
            LOGGER.error("Could not create valatileImage! Image is not compatible!");
        }

        return image;
    }

    public static ImageLoader getInstance() {
        return instance;
    }
}
