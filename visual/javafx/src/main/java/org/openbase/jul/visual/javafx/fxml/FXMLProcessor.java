package org.openbase.jul.visual.javafx.fxml;

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
     *
     * @return the new pane.
     *
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static Pane loadFxmlPane(final Class<? extends AbstractFXController> controllerClass) throws CouldNotPerformException {
        return loadFxmlPaneAndControllerPair(loadDefaultFXML(controllerClass), controllerClass, DEFAULT_CONTROLLER_FACTORY).getKey();
    }

    /**
     * Method load the pane and controller of the given fxml file.
     *
     * @return an pair of the pane and its controller.
     *
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static <CONTROLLER extends AbstractFXController> Pair<Pane, CONTROLLER> loadFxmlPaneAndControllerPair(final Class<CONTROLLER> controllerClass) throws CouldNotPerformException {
        return loadFxmlPaneAndControllerPair(loadDefaultFXML(controllerClass), controllerClass, controllerClass, DEFAULT_CONTROLLER_FACTORY);
    }

    /**
     * Method load the pane and controller of the given fxml file.
     *
     * @param controllerClass the class of the controller.
     * @param clazz           the responsible class which is used for class path resolution.
     * @param <CONTROLLER>    the type of controller which is controlling the new pane.
     *
     * @return an pair of the pane and its controller.
     *
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static <CONTROLLER extends AbstractFXController> Pair<Pane, CONTROLLER> loadFxmlPaneAndControllerPair(final Class<CONTROLLER> controllerClass, final Class clazz) throws CouldNotPerformException {
        return loadFxmlPaneAndControllerPair(loadDefaultFXML(controllerClass), controllerClass, clazz);
    }

    /**
     * Method load the pane and controller of the given fxml file.
     *
     * @param controllerClass   the class of the controller.
     * @param clazz             the responsible class which is used for class path resolution.
     * @param controllerFactory the controller factory to use. Can be null if the default one should be used.
     * @param <CONTROLLER>      the type of controller which is controlling the new pane.
     *
     * @return an pair of the pane and its controller.
     *
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static <CONTROLLER extends AbstractFXController> Pair<Pane, CONTROLLER> loadFxmlPaneAndControllerPair(final Class<CONTROLLER> controllerClass, final Class clazz, final Callback<Class<?>, Object> controllerFactory) throws CouldNotPerformException {
        return loadFxmlPaneAndControllerPair(loadDefaultFXML(controllerClass), controllerClass, clazz, controllerFactory);
    }

    /**
     * Method load the pane of the given fxml file.
     *
     * @param fxmlFileUri the uri pointing to the fxml file within the classpath.
     * @param clazz       the responsible class which is used for class path resolution.
     *
     * @return the new pane.
     *
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static Pane loadFxmlPane(final String fxmlFileUri, final Class clazz) throws CouldNotPerformException {
        return loadFxmlPaneAndControllerPair(fxmlFileUri, clazz, DEFAULT_CONTROLLER_FACTORY).getKey();
    }

    /**
     * Method load the pane and controller of the given fxml file.
     *
     * @param fxmlFileUri the uri pointing to the fxml file within the classpath.
     * @param clazz       the responsible class which is used for class path resolution.
     *
     * @return an pair of the pane and its controller.
     *
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static Pair<Pane, AbstractFXController> loadFxmlPaneAndControllerPair(final String fxmlFileUri, final Class clazz) throws CouldNotPerformException {
        return loadFxmlPaneAndControllerPair(fxmlFileUri, clazz, DEFAULT_CONTROLLER_FACTORY);
    }

    /**
     * Method load the pane and controller of the given fxml file.
     *
     * @param fxmlFileUri       the uri pointing to the fxml file within the classpath.
     * @param clazz             the responsible class which is used for class path resolution.
     * @param controllerFactory the controller factory to use. Can be null if the default one should be used.
     *
     * @return an pair of the pane and its controller.
     *
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static Pair<Pane, AbstractFXController> loadFxmlPaneAndControllerPair(final String fxmlFileUri, final Class clazz, final Callback<Class<?>, Object> controllerFactory) throws CouldNotPerformException {
        return loadFxmlPaneAndControllerPair(fxmlFileUri, AbstractFXController.class, clazz, controllerFactory);
    }

    /**
     * Method load the pane and controller of the given fxml file.
     *
     * @param fxmlFileUri     the uri pointing to the fxml file within the classpath.
     * @param controllerClass the class of the controller.
     * @param clazz           the responsible class which is used for class path resolution.
     * @param <CONTROLLER>    the type of controller which is controlling the new pane.
     *
     * @return an pair of the pane and its controller.
     *
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static <CONTROLLER extends AbstractFXController> Pair<Pane, CONTROLLER> loadFxmlPaneAndControllerPair(final String fxmlFileUri, final Class<CONTROLLER> controllerClass, final Class clazz) throws CouldNotPerformException {
        return loadFxmlPaneAndControllerPair(fxmlFileUri, controllerClass, clazz, null);
    }

    /**
     * Method load the pane and controller of the given fxml file.
     *
     * @param fxmlFileUri       the uri pointing to the fxml file within the classpath.
     * @param controllerClass   the class of the controller.
     * @param clazz             the responsible class which is used for class path resolution.
     * @param controllerFactory the controller factory to use. Can be null if the default one should be used.
     * @param <CONTROLLER>      the type of controller which is controlling the new pane.
     *
     * @return an pair of the pane and its controller.
     *
     * @throws CouldNotPerformException is thrown if something went wrong like for example the fxml file does not exist.
     */
    public static <CONTROLLER extends AbstractFXController> Pair<Pane, CONTROLLER> loadFxmlPaneAndControllerPair(final String fxmlFileUri, final Class<CONTROLLER> controllerClass, final Class clazz, final Callback<Class<?>, Object> controllerFactory) throws CouldNotPerformException {
        URL url;
        FXMLLoader loader;
        try {
            url = clazz.getResource(fxmlFileUri);
            if (url == null) {
                throw new NotAvailableException(fxmlFileUri);
            }
            loader = new FXMLLoader(url);
            if (controllerFactory != null) {
                loader.setControllerFactory(controllerFactory);
            }

            final Pane pane = loader.load();

            // validate controller
            CONTROLLER controller = loader.getController();
            if (!controllerClass.isInstance(controller)) {
                throw new InvalidStateException("Controller[" + controller.getClass().getSimpleName() + "] is not compatible with given ControllerClass[" + controllerClass.getSimpleName() + "]!");
            }

            return new Pair<>(pane, controller);
        } catch (NullPointerException | IOException | CouldNotPerformException ex) {
            try {
                url = clazz.getClassLoader().getResource(fxmlFileUri);
                if (url == null) {
                    throw new NotAvailableException(fxmlFileUri);
                }
                loader = new FXMLLoader(url);
                if (controllerFactory != null) {
                    loader.setControllerFactory(controllerFactory);
                }

                final Pane pane = loader.load();

                // validate controller
                CONTROLLER controller = loader.getController();
                if (!controllerClass.isInstance(controller)) {
                    throw new InvalidStateException("Controller[" + controller.getClass().getSimpleName() + "] is not compatible with given ControllerClass[" + controllerClass.getSimpleName() + "]!");
                }
                return new Pair<>(pane, controller);
            } catch (NullPointerException | IOException | CouldNotPerformException exx) {
                MultiException.ExceptionStack exceptionStack = new MultiException.ExceptionStack();
                exceptionStack = MultiException.push(clazz, ex, exceptionStack);
                exceptionStack = MultiException.push(clazz, exx, exceptionStack);
                throw new MultiException("Could not load FXML[" + fxmlFileUri + "]", exceptionStack);
            }
        }
    }

    /**
     * Method returns an uri to the default fxml file to be loaded during application start.
     *
     * @param controllerClass the controller class of the fxml to load.
     *
     * @return
     */
    public static String loadDefaultFXML(final Class<? extends AbstractFXController> controllerClass) {
        return controllerClass.getName().replaceAll("Controller", "").replaceAll("\\.", "/") + ".fxml";
    }
}
