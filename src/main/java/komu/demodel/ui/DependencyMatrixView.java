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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import komu.demodel.domain.Module;
import komu.demodel.domain.MoveDirection;

public class DependencyMatrixView extends JComponent {

    private static final long serialVersionUID = 1L;
    
    private final DependencyMatrixViewModel model;
    private final Font headerFont = new Font("dialog", Font.PLAIN, 12);
    private final Font gridFont = new Font("dialog", Font.PLAIN, 11);
    private final Color headerBackground = new Color(200, 200, 255);
    private final Color headerBackgroundSelected = new Color(200, 200, 255).brighter();
    private final Color gridColor = new Color(100, 100, 140);
    private final Color violationColor = Color.RED;
    private final JFileChooser exportFileChooser = new JFileChooser();
    private static final int PLUS_WIDTH = 20;
    private static final int DEPTH_DELTA = 10;
    
    public DependencyMatrixView(Module root) {
        if (root == null) throw new NullPointerException("null root");
        
        this.model = new DependencyMatrixViewModel(root);
        this.model.addListener(new MyModelListener());
        
        updateSize();
        setFocusable(true);
        
        addMouseListener(new MyMouseListener());
        addKeyListener(new MyKeyListener());
    }

    private void updateSize() {
        Insets insets = getInsets();
        int leftWidth = calculateLeftHeaderWidth();
        int cellSize = getCellHeight();
        
        int width = insets.left + insets.right 
                  + leftWidth + (cellSize * model.getVisibleModuleCount());
        int height = insets.top + insets.bottom
                  + (cellSize * (1 + model.getVisibleModuleCount()));

        setPreferredSize(new Dimension(width, height));
        setSize(new Dimension(width, height));
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
        List<Module> visibleModules = model.getVisibleModules();
        int moduleCount = visibleModules.size();
        
        FontMetrics hfm = getFontMetrics(headerFont);
        
        int cellHeight = getCellHeight();
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
        if (model.getSelectedModule() != null) {
            int selectedModuleIndex = visibleModules.indexOf(model.getSelectedModule());
            g.setColor(headerBackgroundSelected);
            g.fillRect(dx, gridY + selectedModuleIndex * cellHeight, leftWidth, cellHeight);
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
    
    private int getHeaderYOffset() {
        Insets insets = getInsets();
        int dy = insets.top;

        int cellHeight = getCellHeight();
        int gridY = dy + cellHeight;
        FontMetrics hfm = getFontMetrics(headerFont);
        
        int headerFontHeight = hfm.getHeight();
        return gridY - ((cellHeight - headerFontHeight) / 2) - hfm.getDescent();
    }

    private void drawLeftModuleList(Graphics2D g, int dx, int dy, int textdx, int leftWidth) {
        int cellHeight = getCellHeight();
        int headerYOffset = getHeaderYOffset();

        FontMetrics hfm = g.getFontMetrics(headerFont);
        
        g.setColor(getForeground());
        
        int moduleNumber = 1;
        for (Module module : model.getVisibleModules()) {
            String num = String.valueOf(moduleNumber);
            int numWidth = hfm.stringWidth(num);
            int yy = headerYOffset + (moduleNumber * cellHeight); 
            
            int depthDx = module.getDepth() * DEPTH_DELTA;
            int xx = depthDx + dx + textdx;
            if (model.isOpened(module))
                g.drawString("-", xx, yy);
            else if (!module.isLeaf())
                g.drawString("+", xx, yy);
            
            g.drawString(module.getLocalName(), PLUS_WIDTH + xx, yy);
            g.drawString(num, dx + leftWidth - numWidth - textdx, yy);
            
            moduleNumber++;
        }
    }

    private void drawTopBar(Graphics2D g, int dx, int dy, int cellHeight, int cellWidth, int textdx, int leftWidth) {
        int moduleCount = model.getVisibleModuleCount();
        FontMetrics hfm = g.getFontMetrics(headerFont);

        g.setColor(getForeground());
        for (int mod = 0; mod < moduleCount; mod++) {
            int xx = dx + leftWidth + (mod * cellWidth);
            int yy = dy + cellHeight;
            
            g.drawString(String.valueOf(mod + 1), xx + textdx, yy - hfm.getDescent());
        }
    }

    private int getCellHeight() {
        return (4 * getFontMetrics(headerFont).getHeight()) / 3;
    }
    
    private Module findModuleAt(int x, int y) {
        Insets insets = getInsets();
        int dx = insets.left;
        int dy = insets.top;
        int cellSize = getCellHeight();
        
        int leftWidth = calculateLeftHeaderWidth();
        int ypos = y - dy - cellSize;
        if (x >= dx && x <= dx + leftWidth && ypos >= 0) {
            int moduleIndex = ypos / cellSize; 
            if (moduleIndex < model.getVisibleModuleCount())
                return model.getModuleAt(moduleIndex);                
        }
        
        return null;
    }

    private int calculateLeftHeaderWidth() {
        FontMetrics hfm = getFontMetrics(headerFont);
        
        int leftWidth = 20;
        int extraWidth = 2 * hfm.stringWidth(" 999");
        
        for (Module module : model.getAllModules())
            leftWidth = max(leftWidth, hfm.stringWidth(module.getLocalName()) + module.getDepth() * DEPTH_DELTA);
        
        return PLUS_WIDTH + leftWidth + extraWidth;
    }
    
    private boolean containsViolation(int from, int to) {
        return from > to && model.dependencyStrength(from, to) > 0;
    }

    private void moveSelection(MoveDirection direction) {
        model.moveSelection(direction);
        Module selected = model.getSelectedModule();
        if (selected != null) {
            int index = model.getVisibleModules().indexOf(selected);
            
            int cellHeight = getCellHeight();
            int yy = getHeaderYOffset() + (index * getCellHeight()); 

            scrollRectToVisible(new Rectangle(0, yy - cellHeight, 10, cellHeight * 3));
        }
    }
    
    private void exportImage() {
    	try {
    		if (exportFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
    			return;
    		
    		BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    		paintComponent(image.createGraphics());
    	
    		ImageIO.write(image, "PNG", exportFileChooser.getSelectedFile());
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }

    private class MyModelListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            updateSize();
            repaint();
        }
    }

    private class MyMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            model.setSelectedModule(findModuleAt(e.getX(), e.getY()));
        }
    }
    
    private class MyKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (e.isShiftDown())
                    model.moveSelectedModule(MoveDirection.UP);
                else
                    moveSelection(MoveDirection.UP);
                e.consume();
                break;
            case KeyEvent.VK_DOWN:
                if (e.isShiftDown())
                    model.moveSelectedModule(MoveDirection.DOWN);
                else 
                    moveSelection(MoveDirection.DOWN);
                e.consume();
                break;
            case KeyEvent.VK_S:
                model.sortModules();
                e.consume();
                break;
            case KeyEvent.VK_SPACE:
                if (model.isOpened(model.getSelectedModule()))
                    model.closeSelectedModule();
                else
                    model.openSelectedModule();
                break;
            case KeyEvent.VK_X:
            	exportImage();
            	break;
            }
        }
    }
}
