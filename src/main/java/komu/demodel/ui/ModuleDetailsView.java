/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui;

import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import komu.demodel.domain.Module;
import komu.demodel.domain.PackageMetrics;
import komu.demodel.domain.PackageModule;
import komu.demodel.ui.model.DependencyMatrixViewModel;
import komu.demodel.utils.Check;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public final class ModuleDetailsView extends JPanel {
    
    private final DependencyMatrixViewModel model;
    private final JLabel numberOfTypes = new JLabel();
    private final JLabel numberOfAbstractTypes = new JLabel();
    private final JLabel afferentCouplings = new JLabel();
    private final JLabel efferentCouplings = new JLabel();
    private final JLabel abstractness = new JLabel();
    private final JLabel instability = new JLabel();
    private final JLabel distance = new JLabel();
    
    public ModuleDetailsView(DependencyMatrixViewModel model) {
        Check.notNull(model, "model");
        this.model = model;
        
        FormLayout layout = new FormLayout("max(40dlu;p), 8dlu, 20dlu", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        
        builder.appendTitle("Package metrics");
        builder.nextLine();
        builder.append("Number of Types:", numberOfTypes);
        builder.append("Number of Abstract Types:", numberOfAbstractTypes);
        builder.append("Afferent Couplings:", afferentCouplings);
        builder.append("Efferent Couplings:", efferentCouplings);
        builder.append("Abstractness:", abstractness);
        builder.append("Instability:", instability);
        builder.append("Distance: ", distance);
        
        add(builder.getPanel());
        
        model.addListener(new MyModelListener());
        updateViewFromSelectedModule();
    }
    
    private void updateViewFromSelectedModule() {
        Module module = model.getSelectedRow();
        if (module instanceof PackageModule) {
            PackageMetrics metrics = ((PackageModule) module).getMetrics();
            
            NumberFormat numberFormat = NumberFormat.getInstance();
            
            numberOfTypes.setText(numberFormat.format(metrics.getNumberOfTypes()));
            numberOfAbstractTypes.setText(numberFormat.format(metrics.getNumberOfAbstractTypes()));
            afferentCouplings.setText(numberFormat.format(metrics.getAfferentCouplings()));
            efferentCouplings.setText(numberFormat.format(metrics.getEfferentCouplings()));
            abstractness.setText(numberFormat.format(metrics.getAbstractness()));
            instability.setText(numberFormat.format(metrics.getInstability()));
            distance.setText(numberFormat.format(metrics.getDistanceFromMainSequence()));
        } else {
            numberOfTypes.setText("-");
            numberOfAbstractTypes.setText("-");
            afferentCouplings.setText("-");
            efferentCouplings.setText("-");
            abstractness.setText("-");
            instability.setText("-");
            distance.setText("-");
        }
    }

    private class MyModelListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            updateViewFromSelectedModule();
        }
    }
}
