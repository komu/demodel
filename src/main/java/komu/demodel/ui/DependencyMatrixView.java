/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import komu.demodel.domain.DependencyModel;
import komu.demodel.domain.Module;

public class DependencyMatrixView extends JComponent {

    private static final long serialVersionUID = 1L;
    
    private final DependencyModel model;
    private Module selectedModule;
    private final Font headerFont = new Font("dialog", Font.BOLD, 12);
    private final Font gridFont = new Font("dialog", Font.PLAIN, 11);
    private final Color headerBackground = new Color(200, 200, 255);
    private final Color headerBackgroundSelected = new Color(200, 200, 255).brighter();
    private final Color gridColor = new Color(100, 100, 140);
    private final Color violationColor = Color.RED;
    
    public DependencyMatrixView(DependencyModel model) {
        if (model == null) throw new NullPointerException("null model");
        
        this.model = model;
        
        updatePreferredSize();
        setFocusable(true);
        
        addMouseListener(new MyMouseListener());
        addKeyListener(new MyKeyListener());
    }

    private void updatePreferredSize() {
        Insets insets = getInsets();
        int leftWidth = calculateLeftHeaderWidth();
        int cellSize = getCellSize();
        
        int width = insets.left + insets.right 
                  + leftWidth + (cellSize * model.getModuleCount());
        int height = insets.top + insets.bottom
                  + (cellSize * (1 + model.getModuleCount()));
        
        setPreferredSize(new Dimension(width, height));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        g2.setFont(headerFont);
        
        Insets insets = getInsets();
        int x = insets.left;
        int y = insets.top;
        int width = getWidth() - insets.right - insets.left;
        int height = getHeight() - insets.top - insets.bottom;
        
        g2.setPaint(getBackground());
        g2.fillRect(x, y, width, height);
        
        paintMatrix(g2);
    }

    private void paintMatrix(Graphics2D g) {
        Insets insets = getInsets();
        int dx = insets.left;
        int dy = insets.top;
        int moduleCount = model.getModuleCount();
        
        FontMetrics hfm = getFontMetrics(headerFont);
        
        int cellHeight = getCellSize();
        int cellWidth = cellHeight;
        int textdx = (cellHeight - hfm.charWidth('0')) / 2;
        
        // Calculate the width for the module list on the left.
        int leftWidth = calculateLeftHeaderWidth();

        int gridX = dx + leftWidth;
        int gridY = dy + cellHeight;
        
        // Now that we know the left width we can calculate the actual
        // width of the whole grid. We also calculate the height here.
        int width = min(leftWidth + (moduleCount * cellWidth),
                        getWidth() - insets.right - insets.left);
        int height = min((moduleCount + 1) * cellHeight,
                         getHeight() - insets.top - insets.bottom);

        // Paint background for module list
        g.setColor(headerBackground);
        g.fillRect(dx, gridY, leftWidth, moduleCount * cellHeight);
        g.fillRect(dx + leftWidth, dy, width - leftWidth, cellHeight);
        
        // Draw the background for selected module, if there is one
        if (selectedModule != null) {
            int index = model.indexOfModule(selectedModule);
            g.setColor(headerBackgroundSelected);
            g.fillRect(dx, gridY + index * cellHeight, leftWidth, cellHeight);
        }
        
        // Draw the module list on left.
        drawLeftModuleList(g, dx, dy, textdx, leftWidth);
        
        // Draw the modules on the top bar.
        drawTopBar(g, dx, dy, cellHeight, cellWidth, textdx, leftWidth);
        
        // Draw both the horizontal and vertical lines of the grid.
        g.setColor(gridColor);
        g.drawLine(dx, dy + cellHeight, dx, dy + height);
        g.drawLine(gridX, dy, dx + width, dy);

        for (int mod = 0; mod <= moduleCount; mod++) {
            int xx = dx + leftWidth + (mod * cellWidth);
            int yy = dy + ((mod + 1) * cellHeight);
            
            g.drawLine(xx, dy, xx, dy + height);
            g.drawLine(dx, yy, dx + width, yy);
        }

        FontMetrics gfm = g.getFontMetrics(gridFont);
        
        // Draw the dependency strengths
        g.setFont(gridFont);
        g.setColor(getForeground());
        int gridFontHeight = gfm.getHeight();
        for (int row = 0; row < moduleCount; row++) {
            for (int col = 0; col < moduleCount; col++) {
                int strength = model.dependencyStrength(col, row);
                if (row != col && strength > 0) {
                    String str = String.valueOf(strength);
                    int sw = gfm.stringWidth(str);
                    
                    int xx = gridX + (col * cellWidth) + ((cellWidth - sw) / 2);
                    int yy = gridY + ((row + 1) * cellHeight) - ((cellHeight - gridFontHeight) / 2) - gfm.getDescent();
                    
                    g.drawString(str, xx + 1, yy);
                }
            }
        }

        g.setColor(violationColor);
        for (int row = 0; row < moduleCount; row++) {
            for (int col = 0; col < moduleCount; col++) {
                if (containsViolation(col, row)) {
                    int xx = dx + leftWidth + ((col + 1) * cellWidth);
                    int yy = dy + ((row + 1) * cellHeight) + 1;
                    int[] xs = { xx, xx - (cellWidth / 3), xx };
                    int[] ys = { yy, yy, yy + (cellHeight / 3)};
                    
                    g.fillPolygon(xs, ys, 3);
                }
            }
        }
        
        // Paint the background of diagonal cells
        g.setColor(headerBackground);
        for (int mod = 0; mod < moduleCount; mod++) {
            int xx = dx + leftWidth + (mod * cellWidth);
            int yy = dy + ((mod + 1) * cellHeight);
            
            g.fillRect(xx + 1, yy + 1, cellWidth - 1, cellHeight - 1);
        }
    }

    private void drawLeftModuleList(Graphics2D g, int dx, int dy, int textdx, int leftWidth) {
        int cellHeight = getCellSize();
        int gridY = dy + cellHeight;
        FontMetrics hfm = g.getFontMetrics(headerFont);
        
        g.setColor(getForeground());
        int headerFontHeight = hfm.getHeight();
        int headerYOffset = gridY - ((cellHeight - headerFontHeight) / 2) - hfm.getDescent();
        
        int moduleNumber = 1;
        for (Module module : model.getModules()) {
            String num = String.valueOf(moduleNumber);
            int numWidth = hfm.stringWidth(num);
            
            int yy = headerYOffset + (moduleNumber * cellHeight); 
            g.drawString(module.getName(), dx + textdx, yy);
            g.drawString(num, dx + leftWidth - numWidth - textdx, yy);
            
            moduleNumber++;
        }
    }

    private void drawTopBar(Graphics2D g, int dx, int dy, int cellHeight, int cellWidth, int textdx, int leftWidth) {
        int moduleCount = model.getModuleCount();
        FontMetrics hfm = g.getFontMetrics(headerFont);

        g.setColor(getForeground());
        for (int mod = 0; mod < moduleCount; mod++) {
            int xx = dx + leftWidth + (mod * cellWidth);
            int yy = dy + cellHeight;
            
            g.drawString(String.valueOf(mod + 1), xx + textdx, yy - hfm.getDescent());
        }
    }

    private int getCellSize() {
        return (4 * getFontMetrics(headerFont).getHeight()) / 3;
    }
    
    private Module findModuleAt(int x, int y) {
        Insets insets = getInsets();
        int dx = insets.left;
        int dy = insets.top;
        int cellSize = getCellSize();
        
        int leftWidth = calculateLeftHeaderWidth();
        int ypos = y - dy - cellSize;
        if (x >= dx && x <= dx + leftWidth && ypos >= 0) {
            int moduleIndex = ypos / cellSize; 
            if (moduleIndex < model.getModuleCount())
                return model.getModuleAt(moduleIndex);                
        }
        
        return null;
    }

    private int calculateLeftHeaderWidth() {
        FontMetrics hfm = getFontMetrics(headerFont);
        
        int leftWidth = 20;
        int extraWidth = 2 * hfm.stringWidth(" 999");
        
        for (Module module : model.getModules())
            leftWidth = max(leftWidth, hfm.stringWidth(module.getName()));            
        
        return leftWidth + extraWidth;
    }
    
    private boolean containsViolation(int from, int to) {
        return from > to && model.dependencyStrength(from, to) > 0;
    }
    
    private void fireModuleSelected(Module module) {
        this.selectedModule = module;
        repaint();
    }
    
    private void sortModules() {
        model.sortModules();
        repaint();
    }
    
    private void moveSelectedModuleUp() {
        if (selectedModule == null) return;
        
        model.moveUp(selectedModule);
        repaint();
    }
    
    private void moveSelectedModuleDown() {
        if (selectedModule == null) return;
     
        model.moveDown(selectedModule);
        repaint();
    }
    
    private class MyMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            Module module = findModuleAt(e.getX(), e.getY());
            fireModuleSelected(module);
        }
    }
    
    private class MyKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                moveSelectedModuleUp();
                e.consume();
                break;
            case KeyEvent.VK_DOWN:
                moveSelectedModuleDown();
                e.consume();
                break;
            case KeyEvent.VK_S:
                sortModules();
                e.consume();
                break;
            }
        }
    }
}
