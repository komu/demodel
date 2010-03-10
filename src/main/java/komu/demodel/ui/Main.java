/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import komu.demodel.domain.Module;
import komu.demodel.parser.java.JavaDependencyParser;

public class Main {
    
    public static void main(String[] args) {
        String directory = (args.length == 0) ? "." : args[0];
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        	
            JavaDependencyParser parser = new JavaDependencyParser();
            parser.parseDirectory(new File(directory));
            Module root = parser.getRoot();
            
            DependencyMatrixView view = new DependencyMatrixView(root);
            
            JMenu fileMenu = new JMenu("File");
            fileMenu.setMnemonic('F');
            fileMenu.add(view.exportAction);
            fileMenu.addSeparator();
            fileMenu.add(new ExitAction());
            
            JMenu modulesMenu = new JMenu("Modules");
            modulesMenu.setMnemonic('M');
            modulesMenu.add(view.toggleAction);
            modulesMenu.add(view.sortModulesAction);
            
            JMenuBar menuBar = new JMenuBar();
            menuBar.add(fileMenu);
            menuBar.add(modulesMenu);
            
            JFrame frame = new JFrame("demodel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setJMenuBar(menuBar);
            frame.add(new JScrollPane(view));
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
           
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
    private static class ExitAction extends AbstractAction {
    	public ExitAction() {
    		super("Exit");
    		putValue(MNEMONIC_KEY, KeyEvent.VK_X);
    	}
    	
    	public void actionPerformed(ActionEvent e) {
    		System.exit(0);
    	}
    }
    
}
