/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.visual.screen;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author divine
 */
public class ScreenModeController {

	private static final Logger logger = LoggerFactory.getLogger(ScreenModeController.class);

	public static final String SCREEN_MODE = "ScreenMode";

	public enum ScreenMode {

		Fullscreen, Normal
	};

	public enum OnTopMode {

		Always, OnlyInFullscreen, Never
	};
	private final ComponentAdapter screenModeAdapter = new ComponentAdapter() {
		/**
		 * Invoked when the component's size changes.
		 */
		@Override
		public void componentResized(final ComponentEvent e) {
			synchronized (modeLock) {
				if (screenMode != ScreenMode.Fullscreen) {
					e.getComponent().getSize(screenDimension);
				}
			}
		}

		/**
		 * Invoked when the component's position changes.
		 */
		@Override
		public void componentMoved(final ComponentEvent e) {
			synchronized (modeLock) {
				if (screenMode != ScreenMode.Fullscreen) {
					e.getComponent().getLocation(screenLocation);
				}
			}
		}
	};
	public final Object modeLock = new Object();
	private ScreenMode screenMode;
	private OnTopMode onTopMode;
	private PropertyChangeSupport changes;
	private JFrame frame;
	private final Dimension screenDimension;
	private final Point screenLocation;

	public ScreenModeController(final JFrame frame) {
		this(frame, ScreenMode.Normal, OnTopMode.OnlyInFullscreen);
	}

	public ScreenModeController(final JFrame frame, final ScreenMode screenMode, final OnTopMode onTopMode) {
		this.frame = frame;
		this.changes = new PropertyChangeSupport(this);
		this.screenDimension = new Dimension();
		this.screenLocation = new Point();
		this.frame.getLocation(screenLocation);
		this.frame.getSize(screenDimension);
		this.frame.addComponentListener(screenModeAdapter);
		this.setOnTopMode(onTopMode);
		if (onTopMode == null) {
			throw new NullPointerException("onTopMode is null!");
		}
		this.setScreenMode(screenMode);
		if (screenMode == null) {
			throw new NullPointerException("screenMode is null!");
		}
		activateShortKey();
	}

	public final void setScreenMode(ScreenMode mode) {
		synchronized (modeLock) {
			if (this.screenMode == mode) {
				return;
			}

			logger.debug("Change screenmode to " + mode.name());
			changes.firePropertyChange(SCREEN_MODE, this.screenMode, mode);
			this.screenMode = mode;

			frame.setVisible(false);

			if (frame.isDisplayable()) {
				frame.dispose();
			}

			final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			final GraphicsDevice device = getCurrentGraphicsDevice();

			final Point originalPosition = new Point(screenLocation);
			switch (mode) {
				case Fullscreen:
					frame.setUndecorated(true);

					switch (onTopMode) {
						case Always:
						case OnlyInFullscreen:
							frame.setAlwaysOnTop(true);
							break;

						case Never:
							frame.setAlwaysOnTop(false);
							break;
						default:
							throw new AssertionError(OnTopMode.class.getSimpleName() + " " + mode + " not handled!");
					}

					try {
						device.setFullScreenWindow(frame); // Setzen des FullScreenmodus.
//						frame.validate();
					} catch (Exception e) {
						logger.error("Coult not enter " + mode + " mode!", e);
						device.setFullScreenWindow(null);
						setScreenMode(ScreenMode.Normal);
					}
					break;

				case Normal:
					frame.setUndecorated(false);

					switch (onTopMode) {
						case Always:
							frame.setAlwaysOnTop(true);
							break;
						case OnlyInFullscreen:
						case Never:
							frame.setAlwaysOnTop(false);
							break;
						default:
							throw new AssertionError(OnTopMode.class.getSimpleName() + " " + mode + " not handled!");
					}

					try {
						device.setFullScreenWindow(null);
					} catch (Exception e) {
						logger.error("Could not reset fullscreenwindow!", e);
					}

					frame.setLocation(originalPosition);
					frame.setSize(screenDimension);
					break;
				default:
					throw new AssertionError(ScreenMode.class.getSimpleName() + " " + mode + " not handled!");
			}
			frame.validate();
			frame.setVisible(true);
			frame.toFront();
			frame.requestFocus();
			frame.repaint();
		}
	}

	public final ScreenMode getMode() {
		synchronized (modeLock) {
			return screenMode;
		}
	}

	public OnTopMode getOnTopMode() {
		return onTopMode;
	}

	public final void setOnTopMode(final OnTopMode onTopMode) {
		this.onTopMode = onTopMode;
	}

	public final void toggleFullScreen() {
		synchronized (modeLock) {
			switch (screenMode) {
				case Normal:
					setScreenMode(ScreenMode.Fullscreen);
					break;
				case Fullscreen:
					setScreenMode(ScreenMode.Normal);
					break;
				default:
					throw new AssertionError("ScreenMode " + screenMode + " not handled!");
			}
		}
	}

	// bounding tolerance
	private final int MARGIN = 10;

	private GraphicsDevice getCurrentGraphicsDevice() {
		final Point screenCenter = new Point();
		//		screenCenter.setLocation(frame.getBounds().getCenterX(), frame.getBounds().getCenterY());
		Rectangle frameBounds = frame.getBounds();
		// make more tolerant
		frameBounds.grow(-MARGIN, -MARGIN);
		for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			for (GraphicsConfiguration config : device.getConfigurations()) {
//				Logger.info(this, s"Compare device["+device.getIDstring()+"] with config["+config.getBounds()+"] and frameBounds["+frameBounds+"]");
				if (config.getBounds().contains(frameBounds)) {
					return device;
				}
			}
		}
		logger.info("Could not detect current device, use default instead.");
		// if point is outside all monitors, return default monitor
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	}

	public final void activateShortKey() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(final KeyEvent evt) {
				if (evt.getID() == KeyEvent.KEY_PRESSED) {
					return false;
				}

				if (evt.getKeyCode() == KeyEvent.VK_F11) {
					toggleFullScreen();
				} else {
					return false;
				}
				return true;
			}
		});
	}

	public final synchronized void addPropertyChangeListener(final PropertyChangeListener listener) {
		changes.addPropertyChangeListener(listener);
	}

	public final synchronized void removePropertyChangeListener(final PropertyChangeListener listener) {
		changes.removePropertyChangeListener(listener);
	}
}
