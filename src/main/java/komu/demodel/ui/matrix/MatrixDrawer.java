/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui.matrix;

import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;

import komu.demodel.domain.Module;
import komu.demodel.ui.model.DependencyMatrixViewModel;

final class MatrixDrawer implements FontMetricsProvider {
    
    private final DependencyMatrixViewModel model;
    private final Graphics2D g;
    private final MatrixMetrics metrics;
    private static final Color textColor = Color.BLACK;
    private static final Color headerBackground = new Color(200, 200, 255);
    private static final Color headerBackgroundSelected = Color.YELLOW;
    private static final Color gridColor = new Color(100, 100, 140);
    private static final Color violationColor = Color.RED;
    private static final Color backgroundColor = Color.WHITE;

    MatrixDrawer(DependencyMatrixViewModel model, Graphics2D g) {
        assert model != null;
        assert g != null;
        
        this.model = model;
        this.g = g;
        this.metrics = new MatrixMetrics(model, this);
    }
    
    @Override
    public FontMetrics getFontMetrics(Font font) {
        return g.getFontMetrics(font);
    }
   
    void paintMatrix() {
        g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        
        drawBackground();
        
        drawLeftModuleList();
        drawTopBar();
        
        drawGridLines();
        
        drawDiagonal();
        drawSelectedCell();
        drawDependencyStrengths();
        drawViolations();
    }

    private void drawBackground() {
        g.setPaint(backgroundColor);
        g.fill(g.getClipBounds());
    }
    
    // Draw both the horizontal and vertical lines of the grid.
    private void drawGridLines() {
        int moduleCount = model.getVisibleModuleCount();
        int leftWidth = metrics.getLeftHeaderWidth();
        int cellHeight = metrics.getCellHeight();
        int cellWidth = metrics.getCellWidth();
        Dimension gridSize = metrics.getGridSize();
        
        g.setColor(gridColor);
        g.drawLine(0, cellHeight, 0, gridSize.height);
        g.drawLine(leftWidth, 0, gridSize.width, 0);

        for (int mod = 0; mod <= moduleCount; mod++) {
            int xx = leftWidth + (mod * cellWidth);
            int yy = ((mod + 1) * cellHeight);
            
            g.drawLine(xx, 0, xx, gridSize.height);
            g.drawLine(0, yy, gridSize.width, yy);
        }
    }

    private void drawDependencyStrengths() {
        int cellHeight = metrics.getCellHeight();
        int cellWidth = metrics.getCellWidth();
        int leftHeaderWidth = metrics.getLeftHeaderWidth();

        int moduleCount = model.getVisibleModuleCount();
        
        // Draw the dependency strengths
        g.setFont(metrics.getGridFont());
        g.setColor(textColor);
        for (int row = 0; row < moduleCount; row++) {
        	if (isOpened(row)) continue;
            for (int col = 0; col < moduleCount; col++) {
            	if (isOpened(col)) continue;
            	
                int strength = model.dependencyStrength(col, row);

                if (row != col && strength > 0) {
                    
                    int x = leftHeaderWidth + col*cellWidth;
                    int y = cellHeight + row*cellHeight;

                    drawStringCentered(String.valueOf(strength), x, x+cellWidth, y, y+cellHeight);
                }
            }
        }
    }

    private void drawSelectedCell() {
        if (model.getSelectedColumn() != null && model.getSelectedRow() != null) {
            int selectedColumn = model.getVisibleModules().indexOf(model.getSelectedColumn());
            int selectedRow = model.getVisibleModules().indexOf(model.getSelectedRow());
            
            int cellHeight = metrics.getCellHeight();
            int cellWidth = metrics.getCellWidth();
            int leftHeaderWidth = metrics.getLeftHeaderWidth();
            int x = leftHeaderWidth + selectedColumn*cellWidth;
            int y = cellHeight + selectedRow*cellHeight;
            g.setColor(headerBackgroundSelected);
            g.fillRect(x+1, y+1, cellWidth-1, cellHeight-1);
        }
    }

    private void drawDiagonal() {
        int leftWidth = metrics.getLeftHeaderWidth();
        int cellHeight = metrics.getCellHeight();
        int cellWidth = metrics.getCellWidth();
        int moduleCount = model.getVisibleModuleCount();

        g.setColor(headerBackground);
        for (int mod = 0; mod < moduleCount; mod++) {
            int xx = leftWidth + (mod * cellWidth);
            int yy = ((mod + 1) * cellHeight);
            
            g.fillRect(xx + 1, yy + 1, cellWidth - 1, cellHeight - 1);
        }
    }

    private void drawViolations() {
        int cellHeight = metrics.getCellHeight();
        int cellWidth = metrics.getCellWidth();
        int leftWidth = metrics.getLeftHeaderWidth();
        int moduleCount = model.getVisibleModuleCount();
        
        int[] xs = new int[3];
        int[] ys = new int[3];
        
        g.setColor(violationColor);
        for (int row = 0; row < moduleCount; row++) {
        	if (isOpened(row)) continue;
            for (int col = 0; col < moduleCount; col++) {
            	if (isOpened(col)) continue;
            	
                if (containsViolation(col, row)) {
                    int xx = leftWidth + ((col + 1) * cellWidth);
                    int yy = ((row + 1) * cellHeight) + 1;
                    xs[0] = xs[2] = xx;
                    xs[1] = xx - cellWidth/3;
                    ys[0] = ys[1] = yy;
                    ys[2] = yy + cellHeight/3;
                    
                    g.fillPolygon(xs, ys, 3);
                }
            }
        }
    }
    
    private boolean isOpened(int module) {
    	return model.isOpened(model.getModuleAt(module));
    }

    private void drawLeftModuleList() {
        int leftWidth = metrics.getLeftHeaderWidth();
        int cellHeight = metrics.getCellHeight();
        int gridY = cellHeight;
        List<Module> visibleModules = model.getVisibleModules();
        Module selected = model.getSelectedRow();

        // Paint background for module list
        g.setColor(headerBackground);
        g.fillRect(0, gridY, leftWidth, visibleModules.size() * cellHeight);

        int headerYOffset = metrics.getHeaderYOffset();

        FontMetrics hfm = metrics.getHeaderFontMetrics();
        int textdx = (metrics.getCellWidth() - hfm.charWidth('0')) / 2;
        
        g.setFont(metrics.getHeaderFont());
        
        int moduleIndex = 0;
        for (Module module : visibleModules) {
            // draw background of selected module differently
            if (module == selected) {
                g.setColor(headerBackgroundSelected);
                g.fillRect(0, gridY + moduleIndex * cellHeight, leftWidth, cellHeight);
            }
            
            int moduleNumber = moduleIndex+1;
            String num = String.valueOf(moduleNumber);
            int numWidth = hfm.stringWidth(num);
            int yy = headerYOffset + (moduleNumber * cellHeight);
            
            g.setColor(textColor);

            int xx = metrics.getModuleDepthDx(module) + textdx;
            if (model.isOpened(module))
                g.drawString("-", xx, yy);
            else if (!module.isLeaf())
                g.drawString("+", xx, yy);

            g.drawString(module.getLocalName(), MatrixMetrics.PLUS_WIDTH + xx, yy);
            g.drawString(num, leftWidth - numWidth - textdx, yy);
            
            moduleIndex++;
        }
    }

    private void drawTopBar() {
        int cellHeight = metrics.getCellHeight();
        int cellWidth = metrics.getCellWidth();
        int leftHeader = metrics.getLeftHeaderWidth();
        int moduleCount = model.getVisibleModuleCount();

        // draw the background
        g.setColor(headerBackground);
        g.fillRect(leftHeader, 0, metrics.getGridSize().width - leftHeader, cellHeight);
        
        g.setFont(metrics.getHeaderFont());
        
        Module selected = model.getSelectedColumn();

        for (int moduleIndex = 0; moduleIndex < moduleCount; moduleIndex++) {
            int x = leftHeader + (moduleIndex * cellWidth);
            
            if (model.getModuleAt(moduleIndex) == selected) {
                g.setColor(headerBackgroundSelected);
                g.fillRect(x, 0, cellWidth, cellHeight);
            }
            
            g.setColor(textColor);
            drawStringCentered(String.valueOf(moduleIndex+1), x, x + cellWidth, 0, cellHeight);
        }
    }
    
    private void drawStringCentered(String str, int xLeft, int xRight, int yTop, int yBottom) {
        FontMetrics fm = g.getFontMetrics();
        int strWidth = fm.stringWidth(str);
        int x = xLeft + (xRight-xLeft-strWidth)/2;
        int y = yTop + (yBottom-yTop)/2 + fm.getDescent();
        
        g.drawString(str, x, y);
    }

    private boolean containsViolation(int from, int to) {
        return from > to && model.dependencyStrength(from, to) > 0;
    }
}
