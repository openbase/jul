    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.visual.layout;

import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.printer.LogLevel;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import org.slf4j.LoggerFactory;

/**
 *
 * @author divine
 */
public final class LayoutGenerator {

    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(LayoutGenerator.class);

    public static final int DEFAULT_GAB_TYPE = -1;
    public static final int ZERO_GAB_TYPE = -2;

    private LayoutGenerator() {
    }

    public static GroupLayout designList(final JPanel panel, final Collection<? extends JComponent> componentCollection) {
        return designList(panel, componentCollection, DEFAULT_GAB_TYPE);
    }

    public static GroupLayout designList(final JPanel panel, final Collection<? extends JComponent> componentCollection, final int gabSize) {
        final GroupLayout listLayout = new GroupLayout(panel);
        try {
            SwingUtilities.invokeAndWait(() -> {
                synchronized (panel) {
                    assert panel != null;
                    assert componentCollection != null;
                    panel.removeAll();
                    
                    panel.setLayout(listLayout);
                    final GroupLayout.ParallelGroup parallelGroup = listLayout.createParallelGroup(Alignment.LEADING);
                    
                    // return if list is components empty
                    if (componentCollection.isEmpty()) {
                        return;
                    }
                    
                    componentCollection.stream().forEach((component) -> {
                        parallelGroup.addComponent(component, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
                    });
                    listLayout.setHorizontalGroup(parallelGroup);
                    
                    final GroupLayout.SequentialGroup sequentialGroup = listLayout.createSequentialGroup();
                    for (JComponent component : componentCollection) {
                        sequentialGroup.addComponent(component);
                        switch (gabSize) {
                            case ZERO_GAB_TYPE:
                                break;
                            case DEFAULT_GAB_TYPE:
                                sequentialGroup.addPreferredGap(ComponentPlacement.RELATED);
                                break;
                            default:
                                sequentialGroup.addPreferredGap(ComponentPlacement.RELATED, gabSize, gabSize);
                        }
                    }
                    listLayout.setVerticalGroup(listLayout.createParallelGroup(Alignment.LEADING).addGroup(sequentialGroup));
                }
            });

        } catch (InterruptedException | InvocationTargetException ex) {
            ExceptionPrinter.printHistory(ex, logger, LogLevel.WARN);
        }
        return listLayout;
    }

    public static GroupLayout designLine(final JPanel panel, final Collection<? extends JComponent> componentCollection) {
        return designLine(panel, componentCollection, DEFAULT_GAB_TYPE);
    }

    public static GroupLayout designLine(final JPanel panel, final Collection<? extends JComponent> componentCollection, final int gabSize) {
        final GroupLayout lineLayout = new GroupLayout(panel);
        try {
            SwingUtilities.invokeAndWait(() -> {
                synchronized (panel) {
                    assert panel != null;
                    assert componentCollection != null;
                    panel.removeAll();

                    panel.setLayout(lineLayout);

                    final ParallelGroup parallelGroup = lineLayout.createParallelGroup(Alignment.LEADING);
                    final SequentialGroup sequentialGroup = lineLayout.createSequentialGroup();
                    componentCollection.stream().map((component) -> {
                        sequentialGroup.addComponent(component);
                        return component;
                    }).forEach((_item) -> {
                        sequentialGroup.addGap(0, 0, 0);
                    });
                    parallelGroup.addGroup(sequentialGroup);
                    lineLayout.setHorizontalGroup(parallelGroup);

                    final ParallelGroup outerParallelGroup = lineLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
                    final ParallelGroup innerParallelGroup = lineLayout.createParallelGroup(Alignment.BASELINE);
                    componentCollection.stream().forEach((component) -> {
                        innerParallelGroup.addComponent(component);
                    });
                    outerParallelGroup.addGroup(innerParallelGroup);
                    lineLayout.setVerticalGroup(outerParallelGroup);
                }
            });

        } catch (InterruptedException | InvocationTargetException ex) {
            ExceptionPrinter.printHistory(ex, logger, LogLevel.WARN);
        }
        return lineLayout;
    }

}
