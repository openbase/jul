/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.visual.layout;

import java.util.Collection;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;

/**
 *
 * @author divine
 */
public final class LayoutGenerator {

    public static final int DEFAULT_GAB_TYPE = -1;
    public static final int ZERO_GAB_TYPE = -2;

    private LayoutGenerator() {
    }

    public static GroupLayout designList(final JPanel panel, final Collection<? extends JComponent> componentCollection) {
        return designList(panel, componentCollection, DEFAULT_GAB_TYPE);
    }

    public static GroupLayout designList(final JPanel panel, final Collection<? extends JComponent> componentCollection, final int gabSize) {
        assert panel != null;
        assert componentCollection != null;
        panel.removeAll();
        final GroupLayout listLayout = new GroupLayout(panel);
        panel.setLayout(listLayout);
        final GroupLayout.ParallelGroup parallelGroup = listLayout.createParallelGroup(Alignment.LEADING);

        for (JComponent component : componentCollection) {
            parallelGroup.addComponent(component, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        }
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
        return listLayout;
    }

    public static GroupLayout designLine(final JPanel panel, final Collection<? extends JComponent> componentCollection) {
        return designLine(panel, componentCollection, DEFAULT_GAB_TYPE);
    }

    public static GroupLayout designLine(final JPanel panel, final Collection<? extends JComponent> componentCollection, final int gabSize) {
        assert panel != null;
        assert componentCollection != null;
        panel.removeAll();
        final GroupLayout lineLayout = new GroupLayout(panel);
        panel.setLayout(lineLayout);

        final ParallelGroup parallelGroup = lineLayout.createParallelGroup(Alignment.LEADING);
        final SequentialGroup sequentialGroup = lineLayout.createSequentialGroup();
        for (JComponent component : componentCollection) {
            sequentialGroup.addComponent(component);
            sequentialGroup.addGap(0, 0, 0);
        }
        parallelGroup.addGroup(sequentialGroup);
        lineLayout.setHorizontalGroup(parallelGroup);

        final ParallelGroup outerParallelGroup = lineLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
        final ParallelGroup innerParallelGroup = lineLayout.createParallelGroup(Alignment.BASELINE);
        for (JComponent component : componentCollection) {
            innerParallelGroup.addComponent(component);
        }
        outerParallelGroup.addGroup(innerParallelGroup);
        lineLayout.setVerticalGroup(outerParallelGroup);

        return lineLayout;
    }
}
