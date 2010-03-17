/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui;

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
    
    Module findModuleAt(int x, int y) {
        int cellHeight = getCellHeight();
        
        int ypos = y - cellHeight;
        if (x >= 0 && x <= getLeftHeaderWidth() && ypos >= 0) {
            int moduleIndex = ypos / cellHeight;
            if (moduleIndex < model.getVisibleModuleCount())
                return model.getModuleAt(moduleIndex);
        }
        
        return null;
    }

    int getYOffsetOfModule(int index) {
        return getHeaderYOffset() + (index * getCellHeight()); 
    }
}
