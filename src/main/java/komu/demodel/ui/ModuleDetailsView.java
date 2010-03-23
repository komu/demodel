/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import komu.demodel.domain.Module;
import komu.demodel.domain.PackageMetrics;
import komu.demodel.domain.PackageModule;
import komu.demodel.ui.model.DependencyMatrixViewModel;
import komu.demodel.utils.Check;

public final class ModuleDetailsView extends JPanel {
    
    private final DependencyMatrixViewModel model;
    private final JTextArea textArea = new JTextArea();
    
    public ModuleDetailsView(DependencyMatrixViewModel model) {
        Check.notNull(model, "model");
        this.model = model;
        
        setLayout(new BorderLayout());
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        
        model.addListener(new MyModelListener());
        setPreferredSize(new Dimension(300, 300));
        
        updateViewFromSelectedModule();
    }
    
    private void updateViewFromSelectedModule() {
        Module module = model.getSelectedRow();
        if (module instanceof PackageModule) {
            PackageMetrics metrics = ((PackageModule) module).getMetrics();
            
            StringBuilder text = new StringBuilder();
            text.append("Number of Types: ").append(metrics.getNumberOfTypes()).append("\n");
            text.append("Number of Abstract Types: ").append(metrics.getNumberOfAbstractTypes()).append("\n");
            text.append("Afferent Couplings: ").append(metrics.getAfferentCouplings()).append("\n");
            text.append("Efferent Couplings: ").append(metrics.getEfferentCouplings()).append("\n");
            text.append("Abstractness: ").append(metrics.getAbstractness()).append("\n");
            text.append("Instability: ").append(metrics.getInstability()).append("\n");
            text.append("Distance from the Main Sequence: ").append(metrics.getDistanceFromMainSequence()).append("\n");

            textArea.setText(text.toString());
        } else {
            textArea.setText("");
        }
    }

    private class MyModelListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            updateViewFromSelectedModule();
        }
    }
}
