/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui;

import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
import static java.lang.Math.max;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import komu.demodel.domain.Module;
import komu.demodel.domain.MoveDirection;

public class DependencyMatrixView extends JComponent implements FontMetricsProvider {

    private static final long serialVersionUID = 1L;
    
    private final DependencyMatrixViewModel model;
    private final JFileChooser exportFileChooser = new JFileChooser();
    
    public DependencyMatrixView(Module root) {
        if (root == null) throw new NullPointerException("null root");
        
        this.model = new DependencyMatrixViewModel(root);
        this.model.addListener(new MyModelListener());
        
        updateSize();
        updateCommandStates();
        setFocusable(true);
        
        addMouseListener(new MyMouseListener());
        
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "move-selection-up");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "move-selection-down");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK), "move-selected-module-up");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK), "move-selected-module-down");
        getActionMap().put("move-selection-up", new MoveSelectionAction(MoveDirection.UP));
        getActionMap().put("move-selection-down", new MoveSelectionAction(MoveDirection.DOWN));
        getActionMap().put("move-selected-module-up", new MoveSelectedModuleAction(MoveDirection.UP));
        getActionMap().put("move-selected-module-down", new MoveSelectedModuleAction(MoveDirection.DOWN));
    }

    private void updateSize() {
        Dimension preferredSize = createMetrics().getGridSize();

        setPreferredSize(preferredSize);
        setSize(preferredSize);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        new MatrixDrawer(model, g2).paintMatrix();
    }
    
    private MatrixMetrics createMetrics() {
        return new MatrixMetrics(model, this);
    }

    private void exportImage() {
        try {
            if (exportFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

            Dimension size = getPreferredSize();
            BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setClip(0, 0, size.width, size.height);
            new MatrixDrawer(model, g).paintMatrix();

            ImageIO.write(image, "PNG", exportFileChooser.getSelectedFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCommandStates() {
        sortModulesAction.setEnabled(model.hasSelection());
        toggleAction.setEnabled(model.hasSelection());
    }

    private class MyModelListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            updateCommandStates();
            updateSize();
            repaint();
        }
    }

    private class MyMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            model.setSelectedModule(createMetrics().findModuleAt(e.getX(), e.getY()));
        }
    }

    public final Action exportAction = new AbstractAction("Export") {
        public void actionPerformed(ActionEvent e) {
            exportImage();
        }
    };

    public final Action sortModulesAction = new AbstractAction("Sort children") {
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
        }

        public void actionPerformed(ActionEvent e) {
            model.sortModules();
        }
    };

    public final Action toggleAction = new AbstractAction("Toggle") {
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
        }

        public void actionPerformed(ActionEvent e) {
            if (model.isOpened(model.getSelectedModule()))
                model.closeSelectedModule();
            else
                model.openSelectedModule();
        }
    };

    private class MoveSelectionAction extends AbstractAction {
        private final MoveDirection direction;

        public MoveSelectionAction(MoveDirection direction) {
            this.direction = direction;
        }

        public void actionPerformed(ActionEvent e) {
            model.moveSelection(direction);
            Module selected = model.getSelectedModule();
            if (selected != null) {
                int index = model.getVisibleModules().indexOf(selected);

                MatrixMetrics metrics = createMetrics();
                int cellHeight = metrics.getCellHeight();
                int yy = metrics.getHeaderYOffset() + (index * cellHeight);

                scrollRectToVisible(new Rectangle(0, yy - cellHeight, 10, cellHeight * 3));
            }
        }
    }

    private class MoveSelectedModuleAction extends AbstractAction {
        private final MoveDirection direction;

        public MoveSelectedModuleAction(MoveDirection direction) {
            this.direction = direction;
        }

        public void actionPerformed(ActionEvent e) {
            model.moveSelectedModule(direction);
        }
    }

}

class MatrixMetrics {
    private final DependencyMatrixViewModel model;
    private final FontMetrics headerFontMetrics;
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
    
    Font getGridFont() {
        return gridFont;
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
}

class MatrixDrawer implements FontMetricsProvider {
    
    private final DependencyMatrixViewModel model;
    private final Graphics2D g;
    private final MatrixMetrics metrics;
    private static final Color textColor = Color.BLACK;
    private static final Color headerBackground = new Color(200, 200, 255);
    private static final Color headerBackgroundSelected = new Color(200, 200, 255).brighter();
    private static final Color gridColor = new Color(100, 100, 140);
    private static final Color violationColor = Color.RED;

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
        
        g.setPaint(Color.WHITE);
        g.fill(g.getClipBounds());
        
        drawLeftModuleList();
        drawTopBar();
        
        drawGridLines();

        drawDependencyStrengths();
        drawViolations();
        drawDiagonal();
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
        int gridX = metrics.getLeftHeaderWidth();
        int gridY = cellHeight;

        int moduleCount = model.getVisibleModuleCount();
        
        FontMetrics gfm = g.getFontMetrics(metrics.getGridFont());
        
        // Draw the dependency strengths
        g.setFont(metrics.getGridFont());
        g.setColor(textColor);
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
            for (int col = 0; col < moduleCount; col++) {
                if (containsViolation(col, row)) {
                    int xx = leftWidth + ((col + 1) * cellWidth);
                    int yy = ((row + 1) * cellHeight) + 1;
                    xs[0] = xx;
                    xs[1] = xx - cellWidth/3;
                    xs[2] = xx;
                    ys[0] = yy;
                    ys[1] = yy;
                    ys[2] = yy + cellHeight/3;
                    
                    g.fillPolygon(xs, ys, 3);
                }
            }
        }
    }

    private void drawLeftModuleList() {
        int leftWidth= metrics.getLeftHeaderWidth();
        int width = metrics.getGridSize().width;
        int cellHeight = metrics.getCellHeight();
        int moduleCount = model.getVisibleModuleCount();
        int gridY = cellHeight;

        // Paint background for module list
        g.setColor(headerBackground);
        g.fillRect(0, gridY, leftWidth, moduleCount * cellHeight);
        g.fillRect(leftWidth, 0, width - leftWidth, cellHeight);

        int headerYOffset = metrics.getHeaderYOffset();

        FontMetrics hfm = g.getFontMetrics(metrics.getHeaderFont());
        int textdx = (metrics.getCellWidth() - hfm.charWidth('0')) / 2;
        
        g.setFont(metrics.getHeaderFont());
        
        int moduleIndex = 0;
        for (Module module : model.getVisibleModules()) {
            if (module == model.getSelectedModule()) {
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

        int moduleCount = model.getVisibleModuleCount();
        FontMetrics hfm = g.getFontMetrics(metrics.getHeaderFont());

        g.setFont(metrics.getHeaderFont());
        g.setColor(textColor);
        for (int mod = 0; mod < moduleCount; mod++) {
            int xx = metrics.getLeftHeaderWidth() + (mod * cellWidth);
            int yy = cellHeight;
            
            String str = String.valueOf(mod+1);
            int strWidth = hfm.stringWidth(str);
            if (strWidth < cellWidth) {
                int textdx = (cellWidth-strWidth)/2;
                g.drawString(str, xx+textdx, yy - hfm.getDescent());

            } else {
                // TODO: draw some marker
                g.drawString(str, xx, yy - hfm.getDescent());
            }
        }
    }
    
    private boolean containsViolation(int from, int to) {
        return from > to && model.dependencyStrength(from, to) > 0;
    }
}

interface FontMetricsProvider {
    FontMetrics getFontMetrics(Font font);
}
