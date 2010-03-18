/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui.matrix;

import java.awt.Dimension;
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
                int yy = metrics.getYOffsetOfModule(index);
                int cellHeight = metrics.getCellHeight();

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
