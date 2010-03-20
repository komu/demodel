/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui.matrix;

import static java.lang.Math.max;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

import komu.demodel.domain.Module;

final class MatrixMetrics {
    private final DependencyMatrixViewModel model;
    private final FontMetrics headerFontMetrics;
    private final FontMetrics gridFontMetrics;
    private int leftHeaderWidth = -1;
    private Dimension gridSize;
    private static final Font headerFont = new Font("dialog", Font.PLAIN, 12);
    private static final Font gridFont = new Font("dialog", Font.PLAIN, 11);
    private static final int DEPTH_DELTA = 10;
    static final int PLUS_WIDTH = 20;

    MatrixMetrics(DependencyMatrixViewModel model, FontMetricsProvider fontMetricsProvider) {
        assert model != null;
        assert fontMetricsProvider != null;
        
        this.model = model;
        this.headerFontMetrics = fontMetricsProvider.getFontMetrics(headerFont);
        this.gridFontMetrics = fontMetricsProvider.getFontMetrics(gridFont);
    }
    
    int getLeftHeaderWidth() {
        if (leftHeaderWidth == -1) {
            int leftWidth = 20;
            int extraWidth = 2 * headerFontMetrics.stringWidth(" 999");
            
            for (Module module : model.getVisibleModules())
                leftWidth = max(leftWidth, headerFontMetrics.stringWidth(module.getLocalName()) + getModuleDepthDx(module));
            leftHeaderWidth = PLUS_WIDTH + leftWidth + extraWidth;
        }
        return leftHeaderWidth;
    }
    
    int getModuleDepthDx(Module module) {
        return module.getDepth() * DEPTH_DELTA;
    }
    
    Font getHeaderFont() {
        return headerFont;
    }
    
    FontMetrics getHeaderFontMetrics() {
        return headerFontMetrics;
    }

    Font getGridFont() {
        return gridFont;
    }

    FontMetrics getGridFontMetrics() {
        return gridFontMetrics;
    }
    
    Dimension getGridSize() {
        if (gridSize == null) {
            int moduleCount = model.getVisibleModuleCount();
            
            int gridWidth = getLeftHeaderWidth() + moduleCount * getCellWidth();
            int gridHeight = (moduleCount + 1) * getCellHeight();
            
            gridSize = new Dimension(gridWidth, gridHeight);
        }
        return gridSize;
    }
    
    int getHeaderYOffset() {
        int cellHeight = getCellHeight();
        
        return cellHeight - ((cellHeight - headerFontMetrics.getHeight()) / 2) - headerFontMetrics.getDescent();
    }

    int getCellHeight() {
        return (4 * headerFontMetrics.getHeight()) / 3;
    }
    
    int getCellWidth() {
        return getCellHeight();
    }
    
    boolean clickedAtModule(int x, int y) {
        return (
            x >= 0 && 
            x <= getLeftHeaderWidth() &&
            getMatrixYPos(y) >= 0 &&
            y <= getGridSize().height);
    }
    
    boolean clickedAtCell(int x, int y) {
        return (
            x > getLeftHeaderWidth() && 
            x <= getGridSize().width &&
            getMatrixYPos(y) >= 0 && 
            y <= getGridSize().height);
    }

    private int getMatrixYPos(int y) {
        return y - getCellHeight();
    }
    
    private int getMatrixXPos(int x) {
        return x - getLeftHeaderWidth();
    }
    
    Module findModuleAt(int x, int y) {
        int ypos = getMatrixYPos(y);
        int moduleIndex = ypos / getCellHeight();
        if (moduleIndex < model.getVisibleModuleCount())
            return model.getModuleAt(moduleIndex);
        return null;
    }
    
    Dimension findCellAt(int x, int y) {
        int ypos = getMatrixYPos(y);
        int xpos = getMatrixXPos(x);
        return new Dimension(xpos / getCellWidth(), ypos / getCellHeight());
    }

    int getYOffsetOfModule(int index) {
        return getHeaderYOffset() + (index * getCellHeight()); 
    }
}
