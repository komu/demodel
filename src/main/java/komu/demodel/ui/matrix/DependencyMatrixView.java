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
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import komu.demodel.domain.Module;
import komu.demodel.domain.MoveDirection;
import komu.demodel.ui.model.DependencyMatrixViewModel;
import komu.demodel.utils.Check;

public class DependencyMatrixView extends JComponent implements FontMetricsProvider {

    private static final long serialVersionUID = 1L;
    
    private final DependencyMatrixViewModel model;
    private final JFileChooser exportFileChooser = new JFileChooser();
    
    public DependencyMatrixView(DependencyMatrixViewModel model) {
        Check.notNull(model, "model");
                
        this.model = model;
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
        boolean selectedRow = model.hasSelectedRow();
        boolean selectedCell = model.hasSelectedCell();
        
        sortModulesAction.setEnabled(selectedRow);
        toggleAction.setEnabled(selectedRow);
        
        detailsAction.setEnabled(selectedCell);
    }
    
    private void showTextFrame(String title, String text) {
        JTextArea textArea = new JTextArea();
        textArea.setText(text);
        textArea.setEditable(false);
        textArea.setRows(25);
        textArea.setColumns(50);
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new JScrollPane(textArea));
        frame.setSize(700, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private void toggleSelectedRow() {
        if (model.isOpened(model.getSelectedRow()))
            model.closeSelectedModule();
        else
            model.openSelectedModule();
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
            MatrixMetrics mm = createMetrics();
            
            Module selectedRow = mm.findModuleByRow(e.getY());
            Module selectedColumn = mm.findModuleByColumn(e.getX());
            
            if (e.getClickCount() == 1) {
                model.setSelectedRow(selectedRow);
                model.setSelectedColumn(selectedColumn);
            } else if (e.getClickCount() == 2) {
                if (selectedRow != null && selectedRow == model.getSelectedRow() && selectedColumn == null)
                    toggleSelectedRow();
            }
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
            toggleSelectedRow();
        }
    };
    
    public final Action detailsAction = new AbstractAction("Details") {
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            showTextFrame("Details", model.getDependencyDetailsOfSelectedCell());
        }
    };

    private class MoveSelectionAction extends AbstractAction {
        private final MoveDirection direction;

        public MoveSelectionAction(MoveDirection direction) {
            this.direction = direction;
        }

        public void actionPerformed(ActionEvent e) {
            model.moveSelection(direction);
            Module selected = model.getSelectedRow();
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
