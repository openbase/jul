package org.openbase.jul.visual.swing.animation;

/*-
 * #%L
 * JUL Visual Swing
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class LoadingAnimation extends javax.swing.JPanel implements ActionListener {

	public final static Color GREY0 = new Color(200, 200, 200);
	public final static Color GREY1 = new Color(80, 80, 80);
	public final static Color GREY2 = new Color(60, 60, 60);
	public final static Color GREY3 = new Color(40, 40, 40);
	public final static Color GREY4 = new Color(20, 20, 20);
	public final static Color GREY5 = new Color(5, 5, 5);
	public final static int MIN_PROGRESS_VALUE = 0;
	public final static int MAX_PROGRESS_VALUE = 100;
	public final static int DEFAULT_ANIMATION_POINT_COUNT = 16;
	public final static int DEFAULT_ANIMATION_POINT_RADIUS = 10;
	public final static int DEFAULT_ANIMATION_SCALE = 50;
	//public final static int DEFAULT_ANIMATION_SPEED = 50;
	public final static int DEFAULT_ANIMATION_SPEED = 50;
	public final static boolean DEFAULT_ANIMATION_INDETERMINATE = true;
    
    private final Logger LOGGER = LoggerFactory.getLogger(LoadingAnimation.class);
	
    private final Timer animationTimer;
	private double animationCounter;
	private int animationPointCount;
	private double step;
	private long[] animationPoint;
	private int animationPointRadius;
	private int animationX;
	private int animationY;
	private int scale;
	private boolean indeterminate;
	private int progress;
	private int maxProgressValue;
	private final javax.swing.GroupLayout layout;

	public LoadingAnimation(int animationPointCount, int animationPointRadius, int scale, boolean indeterminate) {
		this.animationPointCount = animationPointCount;
		this.animationPointRadius = animationPointRadius;
		this.scale = scale;
		this.indeterminate = indeterminate;
		this.animationX = getWidth() / 2;
		this.animationY = getWidth() / 2;
		this.step = Math.PI / (animationPointCount / 2);
		this.updateMinimumSize();
		this.progress = 0;
		this.animationPoint = new long[animationPointCount];
		//this.initComponents();
		this.setBackground(Color.BLACK);
		this.animationTimer = new Timer(DEFAULT_ANIMATION_SPEED, this);
		this.animationTimer.setCoalesce(true);
		this.maxProgressValue = MAX_PROGRESS_VALUE;

		stateTextLabel = new javax.swing.JLabel();
		stateTextLabel.setForeground(Color.WHITE);
		stateTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		stateTextLabel.setText("Loading");
		stateTextLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		stateTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

		layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(stateTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap(167, Short.MAX_VALUE)
				.addComponent(stateTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, animationY / 2, Short.MAX_VALUE)
				.addContainerGap()));
		this.setDoubleBuffered(true);
		if (indeterminate) {
			animationTimer.start();
		}
	}

	/**
	 * Creates new form LoadingAnimation
	 */
	public LoadingAnimation() {
		this(DEFAULT_ANIMATION_POINT_COUNT, DEFAULT_ANIMATION_POINT_RADIUS, DEFAULT_ANIMATION_SCALE, DEFAULT_ANIMATION_INDETERMINATE);
	}

	public void setAnimationSpeed(int speed) {
		animationTimer.setDelay(speed);
	}

	public void setMaxProgressValue(int value) {
		this.maxProgressValue = value;
	}

	public void setIndeterminate(boolean indeterminate) {
		this.indeterminate = indeterminate;
		synchronized (animationTimer) {
			if (animationTimer.isRunning()) {
				animationTimer.start();
			}
		}
	}

	public void setText(String text) {
		stateTextLabel.setText(text);
	}

	public void setAnimationPointRadius(int animationPointRadius) {
		this.animationPointRadius = animationPointRadius;
		updateMinimumSize();
	}

	public void setTextForeground(Color color) {
		if (stateTextLabel != null) {
			stateTextLabel.setForeground(color);
		}
	}

	public void setScale(int scale) {
		this.scale = scale;
		updateMinimumSize();
	}

	private void updateMinimumSize() {
		this.setMinimumSize(new Dimension((int) (Math.PI * scale), (int) (Math.PI * scale)));
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        stateTextLabel = new javax.swing.JLabel();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        stateTextLabel.setBackground(new java.awt.Color(238, 38, 238));
        stateTextLabel.setForeground(new java.awt.Color(255, 255, 255));
        stateTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        stateTextLabel.setText("Loading");
        stateTextLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        stateTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(stateTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(167, Short.MAX_VALUE)
                .addComponent(stateTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

	private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
	}//GEN-LAST:event_formComponentResized

	@ Override
	public void paint(Graphics g) {
		super.paint(g);
		boolean change = indeterminate;
		Graphics2D g2 = (Graphics2D) g.create();
		updateComponentMiddelpoint(g2);

		for (int animationPointID = animationPointCount - 1; animationPointID >= 0; animationPointID--) {
			if (indeterminate) {
				indeterminateAnimation(g2, animationPointID);
			} else {
				progressAnimation(g2, animationPoint[animationPointID], animationPointID);
				if (!(animationPoint[animationPointID] == 0 || animationPoint[animationPointID] == 255)) {
					change = true;
				}
			}
            Thread.yield();
		}

		if (!change) {
			synchronized (animationTimer) {
				animationTimer.stop();
				animationTimer.notifyAll();
			}
		}
		//Rectangle rec = g2.getClipBounds();
		//((GroupLayout) this.getLayout()).
		//stateTextLabel.setPreferredSize(new Dimension((int) stateTextLabel.getWidth(), ));
		//debugPaint(g2);
		g2.dispose();
	}

	public void setProcess(int process) {
		if (process < MIN_PROGRESS_VALUE && process > maxProgressValue) {
			LOGGER.error("Bad process range!");
			return;
		}
		this.progress = process;
		calculateAnimationPoints();
		//synchronized(animationTimer) {
		if (!animationTimer.isRunning()) {
			animationTimer.start();
		}
		//}
	}

	private synchronized void calculateAnimationPoints() {
		int v = maxProgressValue / animationPointCount;
		for (int i = 0; i < animationPointCount; i++) {
			if (animationPoint[i] == 0) {
				if (progress > (v * i)) {
					animationPoint[i] = System.currentTimeMillis();
				}
			} else {
				if (progress < (v * i)) {
					animationPoint[i] = 0;
				}
			}
		}
	}

	private void debugPaint(Graphics2D g2) {
		g2.setColor(Color.PINK);
		Rectangle rec = g2.getClipBounds();

		g2.drawRect((int) rec.getX(), (int) rec.getY(), (int) rec.getWidth(), (int) rec.getHeight());
		g2.setColor(Color.ORANGE);
		g2.fillOval((int) rec.getWidth() / 2, (int) rec.getHeight() / 2, 5, 5);
	}

	private void updateComponentMiddelpoint(Graphics2D g2) {
		Rectangle rec = g2.getClipBounds();
		this.animationX = (int) (rec.getWidth() / 2);
		this.animationY = (int) (rec.getHeight() / 2);
	}

	public void setLabelFont(Font font) {
		stateTextLabel.setFont(font);
	}

	private void indeterminateAnimation(Graphics2D g2, int animationPointID) {
		switch (animationPointID) {
			case 0:
				g2.setColor(Color.WHITE);
				break;
			case 1:
				g2.setColor(GREY0);
				break;
			case 2:
				g2.setColor(GREY1);
				break;
			case 3:
				g2.setColor(GREY2);
				break;
			case 4:
				g2.setColor(GREY3);
				break;
			case 5:
				g2.setColor(GREY4);
				break;
			default:
				g2.setColor(GREY5);
				break;
		}

		g2.fillOval((int) (Math.cos(animationCounter - step * animationPointID) * scale) + animationX - (animationPointRadius / 2), (int) (Math.sin(animationCounter - step * animationPointID) * scale) + animationY - (animationPointRadius / 2), animationPointRadius, animationPointRadius);
	}
	private long timeDiffMulticator;
	private int greyColorValue;

	private void progressAnimation(Graphics2D g2, long animationPointValue, int animationPointID) {
		if (animationPointValue == 0) {
			g2.setColor(GREY5);
		} else if (animationPointValue == 255) {
			g2.setColor(Color.WHITE);
		} else {
			timeDiffMulticator = (System.currentTimeMillis() - animationPointValue) / 5;
			//greyColorValue = Math.min(Math.max(((int) (timeDiff/5)), 0), 255);
			if (timeDiffMulticator > 254) {
				g2.setColor(Color.WHITE);
				animationPoint[animationPointID] = 255;
			} else {
				greyColorValue = (int) timeDiffMulticator;
				g2.setColor(new Color(greyColorValue, greyColorValue, greyColorValue));
			}
		}
		g2.fillOval((int) (Math.cos(step * animationPointID) * scale) + animationX, (int) (Math.sin(step * animationPointID) * scale) + animationY, animationPointRadius, animationPointRadius);
	}

	public int getAnimationPointRadius() {
		return animationPointRadius;
	}

	public int getAnimationPointCount() {
		return animationPointCount;
	}

	public int getAnimationX() {
		return animationX;
	}

	public int getAnimationY() {
		return animationY;
	}

	public boolean isIndeterminate() {
		return indeterminate;
	}

	public int getProcess() {
		return progress;
	}

	@Override
	public void actionPerformed(ActionEvent ex) {
		animationCounter = (animationCounter % (2 * Math.PI)) + step;
		repaint();
//		updateUI();
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel stateTextLabel;
    // End of variables declaration//GEN-END:variables

	public void waitOnProcess() {
//		if(indeterminate || !animationTimer.isRunning()) {
//			return;
//		}
//		try {
//			synchronized(animationTimer) {
//				animationTimer.wait();
//			}
//			
//			Thread.sleep(100); // Useabylity aspect: state recognition.
//		} catch (InterruptedException ex) {
//			java.util.logging.Logger.getLogger(LoadingAnimation.class.getName()).log(Level.SEVERE, null, ex);
//		}
	}
}
