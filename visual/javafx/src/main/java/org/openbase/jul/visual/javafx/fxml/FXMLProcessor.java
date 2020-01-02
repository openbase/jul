package org.openbase.jul.visual.javafx.fxml;

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

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import javafx.util.Pair;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.visual.javafx.control.AbstractFXController;

import java.io.IOException;
import java.net.URL;

public class FXMLProcessor {

    private static final Callback<Class<?>, Object> DEFAULT_CONTROLLER_FACTORY = null;

    /**
     * Method load the pane of the given fxml file.
     *
     * @param controllerClass the controller of the fxml pane.
     * @return the new pane.
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static Pane loadFxmlPane(final Class<? extends AbstractFXController> controllerClass, final Class uriLoaderClass) throws CouldNotPerformException {
        return loadFxmlPaneAndControllerPair(loadDefaultFXML(controllerClass), controllerClass, uriLoaderClass, DEFAULT_CONTROLLER_FACTORY).getKey();
    }

    /**
     * Method load the pane and controller of the given fxml file.
     *
     * @return an pair of the pane and its controller.
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static <CONTROLLER extends AbstractFXController> Pair<Pane, CONTROLLER> loadFxmlPaneAndControllerPair(final Class<? extends CONTROLLER> controllerClass) throws CouldNotPerformException {
        return loadFxmlPaneAndControllerPair(loadDefaultFXML(controllerClass), controllerClass, controllerClass, DEFAULT_CONTROLLER_FACTORY);
    }

    /**
     * Method load the pane and controller of the given fxml file.
     *
     * @param controllerClass the class of the controller.
     * @param uriLoaderClass  the responsible class which is used for class path resolution.
     * @param <CONTROLLER>    the type of controller which is controlling the new pane.
     * @return an pair of the pane and its controller.
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static <CONTROLLER extends AbstractFXController> Pair<Pane, CONTROLLER> loadFxmlPaneAndControllerPair(final Class<? extends CONTROLLER> controllerClass, final Class uriLoaderClass) throws CouldNotPerformException {
        return loadFxmlPaneAndControllerPair(loadDefaultFXML(controllerClass), controllerClass, uriLoaderClass);
    }

    /**
     * Method load the pane and controller of the given fxml file.
     *
     * @param controllerClass   the class of the controller.
     * @param uriLoaderClass    the responsible class which is used for class path resolution.
     * @param controllerFactory the controller factory to use. Can be null if the default one should be used.
     * @param <CONTROLLER>      the type of controller which is controlling the new pane.
     * @return an pair of the pane and its controller.
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static <CONTROLLER extends AbstractFXController> Pair<Pane, CONTROLLER> loadFxmlPaneAndControllerPair(final Class<? extends CONTROLLER> controllerClass, final Class uriLoaderClass, final Callback<Class<?>, Object> controllerFactory) throws CouldNotPerformException {
        return loadFxmlPaneAndControllerPair(loadDefaultFXML(controllerClass), controllerClass, uriLoaderClass, controllerFactory);
    }

    /**
     * Method load the pane of the given fxml file.
     *
     * @param fxmlFileUri    the uri pointing to the fxml file within the classpath.
     * @param uriLoaderClass the responsible class which is used for class path resolution.
     * @return the new pane.
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static Pane loadFxmlPane(final String fxmlFileUri, final Class uriLoaderClass) throws CouldNotPerformException {
        return loadFxmlPaneAndControllerPair(fxmlFileUri, uriLoaderClass, DEFAULT_CONTROLLER_FACTORY).getKey();
    }

    /**
     * Method load the pane and controller of the given fxml file.
     *
     * @param fxmlFileUri    the uri pointing to the fxml file within the classpath.
     * @param uriLoaderClass the responsible class which is used for class path resolution.
     * @return an pair of the pane and its controller.
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static Pair<Pane, AbstractFXController> loadFxmlPaneAndControllerPair(final String fxmlFileUri, final Class uriLoaderClass) throws CouldNotPerformException {
        return loadFxmlPaneAndControllerPair(fxmlFileUri, uriLoaderClass, DEFAULT_CONTROLLER_FACTORY);
    }

    /**
     * Method load the pane and controller of the given fxml file.
     *
     * @param fxmlFileUri       the uri pointing to the fxml file within the classpath.
     * @param uriLoaderClass    the responsible class which is used for class path resolution.
     * @param controllerFactory the controller factory to use. Can be null if the default one should be used.
     * @return an pair of the pane and its controller.
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static Pair<Pane, AbstractFXController> loadFxmlPaneAndControllerPair(final String fxmlFileUri, final Class uriLoaderClass, final Callback<Class<?>, Object> controllerFactory) throws CouldNotPerformException {
        return loadFxmlPaneAndControllerPair(fxmlFileUri, AbstractFXController.class, uriLoaderClass, controllerFactory);
    }

    /**
     * Method load the pane and controller of the given fxml file.
     *
     * @param fxmlFileUri     the uri pointing to the fxml file within the classpath.
     * @param controllerClass the class of the controller.
     * @param uriLoaderClass  the responsible class which is used for class path resolution.
     * @param <CONTROLLER>    the type of controller which is controlling the new pane.
     * @return an pair of the pane and its controller.
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static <CONTROLLER extends AbstractFXController> Pair<Pane, CONTROLLER> loadFxmlPaneAndControllerPair(final String fxmlFileUri, final Class<? extends CONTROLLER> controllerClass, final Class uriLoaderClass) throws CouldNotPerformException {
        return loadFxmlPaneAndControllerPair(fxmlFileUri, controllerClass, uriLoaderClass, null);
    }

    /**
     * Method load the pane and controller of the given fxml file.
     *
     * @param fxmlFileUri       the uri pointing to the fxml file within the classpath.
     * @param controllerClass   the class of the controller.
     * @param uriLoaderClass    the responsible class which is used for class path resolution.
     * @param controllerFactory the controller factory to use. Can be null if the default one should be used.
     * @param <CONTROLLER>      the type of controller which is controlling the new pane.
     * @return an pair of the pane and its controller.
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static <CONTROLLER extends AbstractFXController> Pair<Pane, CONTROLLER> loadFxmlPaneAndControllerPair(final String fxmlFileUri, final Class<? extends CONTROLLER> controllerClass, final Class uriLoaderClass, final Callback<Class<?>, Object> controllerFactory) throws CouldNotPerformException {
        URL url;
        FXMLLoader loader;
        try {
            url = loadURL(fxmlFileUri, uriLoaderClass);

            loader = new FXMLLoader(url);
            if (controllerFactory != null) {
                loader.setControllerFactory(controllerFactory);
            }

            final Pane pane = loader.load();

            // validate controller
            CONTROLLER controller = loader.getController();

            if (controller == null) {
                throw new InvalidStateException("Controller[" + controllerClass.getSimpleName() + "] seems not to be declared in the FXML[" + fxmlFileUri + "] and is therefore not compatible!");
            }

            if (!controllerClass.isInstance(controller)) {
                throw new InvalidStateException("Controller[" + controller.getClass().getSimpleName() + "] declared in FXML[" + fxmlFileUri + "] is not compatible with given ControllerClass[" + controllerClass.getSimpleName() + "]!");
            }

            return new Pair<>(pane, controller);
        } catch (NullPointerException | IOException | CouldNotPerformException ex) {
            throw new MultiException("Could not load pain controller pair of [" + fxmlFileUri + "]", ex);
        }
    }

    private static URL loadURL(final String fxmlFileUri, final Class uriLoaderClass) throws NotAvailableException {
        URL url = uriLoaderClass.getResource(fxmlFileUri);
        if (url != null) {
            return url;
        }
        url = uriLoaderClass.getClassLoader().getResource(fxmlFileUri);
        if (url != null) {
            return url;
        }
        throw new NotAvailableException(fxmlFileUri);
    }

    /**
     * Method returns an uri to the default fxml file to be loaded during application start.
     *
     * @param controllerClass the controller class of the fxml to load.
     * @return
     */
    public static String loadDefaultFXML(final Class<? extends AbstractFXController> controllerClass) {
        return controllerClass.getName().replaceAll("Controller", "").replaceAll("\\.", "/") + ".fxml";
    }
}
