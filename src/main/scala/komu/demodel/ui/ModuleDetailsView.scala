/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui

import java.text.NumberFormat
import javax.swing.{ JLabel, JPanel }

import komu.demodel.domain.PackageModule
import komu.demodel.ui.model.DependencyMatrixViewModel

import com.jgoodies.forms.builder.DefaultFormBuilder
import com.jgoodies.forms.layout.FormLayout

final class ModuleDetailsView(model: DependencyMatrixViewModel) extends JPanel {
    
    val numberOfTypes = new JLabel
    val numberOfAbstractTypes = new JLabel
    val afferentCouplings = new JLabel
    val efferentCouplings = new JLabel
    val abstractness = new JLabel
    val instability = new JLabel
    val distance = new JLabel
    
    {
      val layout = new FormLayout("max(40dlu;p), 8dlu, 20dlu", "");
      val builder = new DefaultFormBuilder(layout)
      builder.setDefaultDialogBorder()
        
      builder.appendTitle("Package metrics")
      builder.nextLine()
      builder.append("Number of Types:", numberOfTypes)
      builder.append("Number of Abstract Types:", numberOfAbstractTypes)
      builder.append("Afferent Couplings:", afferentCouplings)
      builder.append("Efferent Couplings:", efferentCouplings)
      builder.append("Abstractness:", abstractness)
      builder.append("Instability:", instability)
      builder.append("Distance: ", distance)
        
      add(builder.getPanel)

      updateViewFromSelectedModule()
      model.selectedRow.onChange { updateViewFromSelectedModule() }
    }
    
    def updateViewFromSelectedModule() =
      model.selectedRow() match {
        case Some(module: PackageModule) => {
          val metrics = module.metrics
          val numberFormat = NumberFormat.getInstance

          numberOfTypes.setText(numberFormat.format(metrics.numberOfTypes))
          numberOfAbstractTypes.setText(numberFormat.format(metrics.numberOfAbstractTypes))
          afferentCouplings.setText(numberFormat.format(metrics.afferentCouplings))
          efferentCouplings.setText(numberFormat.format(metrics.efferentCouplings))
          abstractness.setText(numberFormat.format(metrics.abstractness))
          instability.setText(numberFormat.format(metrics.instability))
          distance.setText(numberFormat.format(metrics.distanceFromMainSequence))
        }
      case _ => {
        numberOfTypes.setText("-")
        numberOfAbstractTypes.setText("-")
        afferentCouplings.setText("-")
        efferentCouplings.setText("-")
        abstractness.setText("-")
        instability.setText("-")
        distance.setText("-")
      }
    }
}
