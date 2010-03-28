/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui.matrix

import java.awt.{ Dimension, Graphics, Graphics2D, Rectangle }
import java.awt.event.{ ActionEvent, KeyEvent, MouseAdapter, MouseEvent }
import java.awt.event.InputEvent.SHIFT_DOWN_MASK
import java.awt.image.BufferedImage

import javax.imageio.ImageIO
import javax.swing.{ AbstractAction, Action, JComponent, JFileChooser, JFrame, 
  JScrollPane, JTextArea, KeyStroke, WindowConstants }
import javax.swing.event.{ ChangeEvent, ChangeListener }

import komu.demodel.domain.{ Module, MoveDirection }
import komu.demodel.ui.model.DependencyMatrixViewModel

class DependencyMatrixView(model: DependencyMatrixViewModel) 
    extends JComponent with FontMetricsProvider {

  val exportFileChooser = new JFileChooser
  updateSize()
  updateCommandStates()
  setFocusable(true)
        
  addMouseListener(MyMouseListener)
        
  getInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "move-selection-up")
  getInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "move-selection-down")
  getInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, SHIFT_DOWN_MASK), "move-selected-module-up")
  getInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, SHIFT_DOWN_MASK), "move-selected-module-down")
  getActionMap.put("move-selection-up", new MoveSelectionAction(MoveDirection.UP))
  getActionMap.put("move-selection-down", new MoveSelectionAction(MoveDirection.DOWN))
  getActionMap.put("move-selected-module-up", new MoveSelectedModuleAction(MoveDirection.UP))
  getActionMap.put("move-selected-module-down", new MoveSelectedModuleAction(MoveDirection.DOWN))

  model.onChange {
    updateCommandStates()
    updateSize()
    repaint()
  }

  def updateSize() {
    val preferredSize = createMetrics().gridSize

    setPreferredSize(preferredSize)
    setSize(preferredSize)
  }

  def createMetrics() = new MatrixMetrics(model, this)

  override def paintComponent(g: Graphics) =
    new MatrixDrawer(model, g.asInstanceOf[Graphics2D]).paintMatrix()
    

  def exportImage() {
    try {
      if (exportFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return

      val size = getPreferredSize
      val image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB)
      val g = image.createGraphics()
      g.setClip(0, 0, size.width, size.height)
      new MatrixDrawer(model, g).paintMatrix()

      ImageIO.write(image, "PNG", exportFileChooser.getSelectedFile)
    } catch {
      case e => e.printStackTrace()
    }
  }

  def updateCommandStates() {
    val selectedRow = model.hasSelectedRow
    sortModulesAction.setEnabled(selectedRow)
    toggleAction.setEnabled(selectedRow)

    val selectedCell = model.hasSelectedCell
    detailsAction.setEnabled(selectedCell)
  }
    
  def showTextFrame(title: String, text: String) {
    val textArea = new JTextArea
    textArea.setText(text)
    textArea.setEditable(false)
    textArea.setRows(25)
    textArea.setColumns(50)
    val frame = new JFrame(title)
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    frame.add(new JScrollPane(textArea))
    frame.setSize(700, 500)
    frame.setLocationRelativeTo(null)
    frame.setVisible(true)
  }
    
  def toggleSelectedRow() =
    for (row <- model.selectedRow())
      if (model.isOpened(row))
        model.closeSelectedModule()
      else
        model.openSelectedModule()

  object MyMouseListener extends MouseAdapter {
    override def mouseClicked(e: MouseEvent) {
      val mm = createMetrics();
            
      val selectedRow = mm.findModuleByRow(e.getY)
      val selectedColumn = mm.findModuleByColumn(e.getX)
          
      if (e.getClickCount == 1) {
        model.selectedRow := selectedRow
        model.selectedColumn := selectedColumn
      } else if (e.getClickCount == 2) {
        if (selectedRow.isDefined && selectedRow == model.selectedRow() && selectedColumn.isEmpty)
          toggleSelectedRow()
      }
    }
  }

  object exportAction extends AbstractAction("Export") {
    def actionPerformed(e: ActionEvent) = exportImage()
  }

  object sortModulesAction extends AbstractAction("Sort children") {
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, 0))

    def actionPerformed(e: ActionEvent) = model.sortModules()
  }

  object toggleAction extends AbstractAction("Toggle") {
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0))

    def actionPerformed(e: ActionEvent) = toggleSelectedRow()
  }
  
  object detailsAction extends AbstractAction("Details") {
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, 0))

    def actionPerformed(e: ActionEvent) = 
      showTextFrame("Details", model.dependencyDetailsOfSelectedCell)
  }
  
  private class MoveSelectionAction(direction: MoveDirection) extends AbstractAction {

    def actionPerformed(e: ActionEvent) {
      model.moveSelection(direction)
      for (selected <- model.selectedRow()) {
        val index = model.visibleModules.indexOf(selected)

        val metrics = createMetrics()
        val yy = metrics.getYOffsetOfModule(index)
        val cellHeight = metrics.cellHeight

        scrollRectToVisible(new Rectangle(0, yy - cellHeight, 10, cellHeight * 3))
      }
    }
  }

  private class MoveSelectedModuleAction(direction: MoveDirection) extends AbstractAction {
    def actionPerformed(e: ActionEvent) = model.moveSelectedModule(direction)
  }
}
