/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui

import java.text.NumberFormat

import javax.swing.{ JLabel, JPanel }
import javax.swing.event.{ ChangeEvent, ChangeListener }

import komu.demodel.domain.{ PackageMetrics, PackageModule }
import komu.demodel.ui.model.DependencyMatrixViewModel
import komu.demodel.utils.Check

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
        
      model.addListener(MyModelListener)
      updateViewFromSelectedModule()
    }
    
    def updateViewFromSelectedModule() =
      model.selectedRow match {
        case Some(module: PackageModule) => {
          val metrics = module.getMetrics

          val numberFormat = NumberFormat.getInstance

          numberOfTypes.setText(numberFormat.format(metrics.getNumberOfTypes))
          numberOfAbstractTypes.setText(numberFormat.format(metrics.getNumberOfAbstractTypes))
          afferentCouplings.setText(numberFormat.format(metrics.getAfferentCouplings))
          efferentCouplings.setText(numberFormat.format(metrics.getEfferentCouplings))
          abstractness.setText(numberFormat.format(metrics.getAbstractness))
          instability.setText(numberFormat.format(metrics.getInstability))
          distance.setText(numberFormat.format(metrics.getDistanceFromMainSequence))
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

  object MyModelListener extends ChangeListener {
    def stateChanged(e: ChangeEvent) = updateViewFromSelectedModule()
  }
}
