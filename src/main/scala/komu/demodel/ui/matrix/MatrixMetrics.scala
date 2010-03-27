/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui.matrix

import scala.collection.JavaConversions._

import java.lang.Math.max

import java.awt.{ Dimension, Font, FontMetrics }

import komu.demodel.domain.Module
import komu.demodel.ui.model.DependencyMatrixViewModel

object MatrixFonts {
  val headerFont = new Font("dialog", Font.PLAIN, 12)
  val gridFont = new Font("dialog", Font.PLAIN, 11)
}

final class MatrixMetrics(private model: DependencyMatrixViewModel, 
                          private fontMetricsProvider: FontMetricsProvider) {
  
  val headerFontMetrics = fontMetricsProvider.getFontMetrics(headerFont)
  val gridFontMetrics = fontMetricsProvider.getFontMetrics(gridFont)
  val plusWidth = 20
  private val depthDelta = 10

  lazy val leftHeaderWidth = {
    var leftWidth = 20
    val extraWidth = 2 * headerFontMetrics.stringWidth(" 999")
    
    for (module <- model.visibleModules)
      leftWidth = max(leftWidth, headerFontMetrics.stringWidth(module.getLocalName()) + getModuleDepthDx(module))

    plusWidth + leftWidth + extraWidth
  }
    
  def getModuleDepthDx(module: Module) = module.getDepth * depthDelta
  def headerFont = MatrixFonts.headerFont
  def gridFont = MatrixFonts.gridFont
    
  lazy val gridSize = {
    val moduleCount = model.visibleModuleCount
            
    val gridWidth = leftHeaderWidth + moduleCount * cellWidth
    val gridHeight = (moduleCount + 1) * cellHeight
            
    new Dimension(gridWidth, gridHeight)
  }
    
  def headerYOffset =
    cellHeight - ((cellHeight - headerFontMetrics.getHeight) / 2) - headerFontMetrics.getDescent

  def cellHeight = (4 * headerFontMetrics.getHeight) / 3
  def cellWidth = cellHeight

  def getMatrixYPos(y: Int) = y - cellHeight
  def getMatrixXPos(x: Int) = x - leftHeaderWidth
  def getYOffsetOfModule(index: Int) = headerYOffset + (index * cellHeight)
    
  def findModuleByRow(y: Int) = {
    val index = getMatrixYPos(y) / cellHeight
    if (index >= 0 && index < model.visibleModuleCount)
      Some(model.getModuleAt(index))
    else
      None
  }

  def findModuleByColumn(x: Int) = {
    val index = getMatrixXPos(x) / cellWidth
    if (index >= 0 && index < model.visibleModuleCount)
      Some(model.getModuleAt(index))
    else
      None
  }
}
