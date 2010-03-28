/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui.matrix

import java.awt.RenderingHints.{ KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON }

import java.awt.{ Color, Dimension, Font, FontMetrics, Graphics2D }

import java.util.List

import komu.demodel.domain.Module
import komu.demodel.ui.model.DependencyMatrixViewModel

object MatrixColors {
  val textColor = Color.BLACK
  val headerBackground = new Color(200, 200, 255)
  val headerBackgroundSelected = Color.YELLOW
  val gridColor = new Color(100, 100, 140)
  val violationColor = Color.RED
  val backgroundColor = Color.WHITE
}

final class MatrixDrawer(model: DependencyMatrixViewModel, g: Graphics2D) extends FontMetricsProvider {
    
  val metrics = new MatrixMetrics(model, this)

  import MatrixColors._

  def getFontMetrics(font: Font) = g.getFontMetrics(font)
   
  def paintMatrix() {
    g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON)
        
    drawBackground()
        
    drawLeftModuleList()
    drawTopBar()
        
    drawGridLines()
        
    drawDiagonal()
    drawSelectedCell()
    drawDependencyStrengths()
    drawViolations()
  }

  private def drawBackground() {
    g.setPaint(backgroundColor)
    g.fill(g.getClipBounds)
  }
    
  // Draw both the horizontal and vertical lines of the grid.
  private def drawGridLines() {
    val moduleCount = model.visibleModuleCount
    val leftWidth = metrics.leftHeaderWidth
    val cellHeight = metrics.cellHeight
    val cellWidth = metrics.cellWidth
    val gridSize = metrics.gridSize
        
    g.setColor(gridColor)
    g.drawLine(0, cellHeight, 0, gridSize.height)
    g.drawLine(leftWidth, 0, gridSize.width, 0)

    for (mod <- 0 to moduleCount) {
      val xx = leftWidth + (mod * cellWidth)
      val yy = ((mod + 1) * cellHeight)
            
      g.drawLine(xx, 0, xx, gridSize.height)
      g.drawLine(0, yy, gridSize.width, yy)
    }
  }

  private def drawDependencyStrengths() {
    val cellHeight = metrics.cellHeight
    val cellWidth = metrics.cellWidth
    val leftHeaderWidth = metrics.leftHeaderWidth
    val moduleCount = model.visibleModuleCount
        
    // Draw the dependency strengths
    g.setFont(metrics.gridFont)
    g.setColor(textColor)

    for (row <- 0 until moduleCount if !isOpened(row))
      for (col <- 0 until moduleCount if !isOpened(col)) {
        val strength = model.dependencyStrength(col, row)

        if (row != col && strength > 0) {
          val x = leftHeaderWidth + col*cellWidth
          val y = cellHeight + row*cellHeight

          drawStringCentered(String.valueOf(strength), x, x+cellWidth, y, y+cellHeight)
        }
      }
  }

  private def drawSelectedCell() {
    for (row <- model.selectedRow(); col <- model.selectedColumn()) {
      val selectedColumn = model.visibleModules.indexOf(col)
      val selectedRow = model.visibleModules.indexOf(row)
            
      val cellHeight = metrics.cellHeight
      val cellWidth = metrics.cellWidth
      val leftHeaderWidth = metrics.leftHeaderWidth
      val x = leftHeaderWidth + selectedColumn*cellWidth
      val y = cellHeight + selectedRow*cellHeight
      g.setColor(headerBackgroundSelected)
      g.fillRect(x+1, y+1, cellWidth-1, cellHeight-1)
    }
  }

  private def drawDiagonal() {
    val leftWidth = metrics.leftHeaderWidth
    val cellHeight = metrics.cellHeight
    val cellWidth = metrics.cellWidth
    val moduleCount = model.visibleModuleCount

    g.setColor(headerBackground)
    for (mod <- 0 until moduleCount) {
      val xx = leftWidth + (mod * cellWidth)
      val yy = ((mod + 1) * cellHeight)
            
      g.fillRect(xx + 1, yy + 1, cellWidth - 1, cellHeight - 1)
    }
  }

  private def drawViolations() {
    def cellHeight = metrics.cellHeight
    def cellWidth = metrics.cellWidth
    def leftWidth = metrics.leftHeaderWidth
    def moduleCount = model.visibleModuleCount
        
    g.setColor(violationColor)
    for (row <- 0 until moduleCount if !isOpened(row))
      for (col <- 0 until moduleCount if !isOpened(col)) 
        if (containsViolation(col, row)) {
          val xx = leftWidth + ((col + 1) * cellWidth)
          val yy = ((row + 1) * cellHeight) + 1
          
          val xs = Array(xx, xx - cellWidth/3, xx)
          val ys = Array(yy, yy, yy + cellHeight/3)
                    
          g.fillPolygon(xs, ys, 3)
        }
  }
    
  private def isOpened(module: Int) = 
    model.isOpened(model.getModuleAt(module))

  private def drawLeftModuleList() {
    val leftWidth = metrics.leftHeaderWidth
    val cellHeight = metrics.cellHeight
    val gridY = cellHeight
    val visibleModules = model.visibleModules
    val selected = model.selectedRow()

    // Paint background for module list
    g.setColor(headerBackground)
    g.fillRect(0, gridY, leftWidth, visibleModules.size * cellHeight)

    val headerYOffset = metrics.headerYOffset

    val hfm = metrics.headerFontMetrics
    val textdx = (metrics.cellWidth - hfm.charWidth('0')) / 2
        
    g.setFont(metrics.headerFont)
        
    var moduleIndex = 0
    for (module <- visibleModules) {
      // draw background of selected module differently
      if (Some(module) == selected) {
        g.setColor(headerBackgroundSelected)
        g.fillRect(0, gridY + moduleIndex * cellHeight, leftWidth, cellHeight)
      }
            
      val moduleNumber = moduleIndex+1
      val num = String.valueOf(moduleNumber)
      val numWidth = hfm.stringWidth(num)
      val yy = headerYOffset + (moduleNumber * cellHeight)
            
      g.setColor(textColor)

      val xx = metrics.getModuleDepthDx(module) + textdx
      if (model.isOpened(module))
        g.drawString("-", xx, yy)
      else if (!module.isLeaf)
        g.drawString("+", xx, yy)

      g.drawString(module.localName, metrics.plusWidth + xx, yy)
      g.drawString(num, leftWidth - numWidth - textdx, yy)
            
      moduleIndex += 1
    }
  }

  private def drawTopBar() {
    val cellHeight = metrics.cellHeight
    val cellWidth = metrics.cellWidth
    val leftHeader = metrics.leftHeaderWidth
    val moduleCount = model.visibleModuleCount

    // draw the background
    g.setColor(headerBackground)
    g.fillRect(leftHeader, 0, metrics.gridSize.width - leftHeader, cellHeight)
        
    g.setFont(metrics.headerFont)
        
    val selected = model.selectedColumn()

    for (moduleIndex <- 0 until moduleCount) {
      val x = leftHeader + (moduleIndex * cellWidth)
            
      if (Some(model.getModuleAt(moduleIndex)) == selected) {
        g.setColor(headerBackgroundSelected)
        g.fillRect(x, 0, cellWidth, cellHeight)
      }
            
      g.setColor(textColor)
      drawStringCentered(String.valueOf(moduleIndex+1), x, x + cellWidth, 0, cellHeight)
    }
  }
    
  private def drawStringCentered(str: String, xLeft: Int, xRight: Int, yTop: Int, yBottom: Int) {
    val fm = g.getFontMetrics
    val strWidth = fm.stringWidth(str)
    val x = xLeft + (xRight-xLeft-strWidth)/2
    val y = yTop + (yBottom-yTop)/2 + fm.getDescent
        
    g.drawString(str, x, y)
  }

  private def containsViolation(from: Int, to: Int) =
    from > to && model.dependencyStrength(from, to) > 0
}
