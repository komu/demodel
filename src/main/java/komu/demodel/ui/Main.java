/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import komu.demodel.domain.Module;
import komu.demodel.parser.java.JavaDependencyParser;

public class Main {
    
    public static void main(String[] args) {
        try {
            JavaDependencyParser parser = new JavaDependencyParser();
            parser.parseDirectory(new File("."));
            Module root = parser.getRoot();
            
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame frame = new JFrame("demodel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new JScrollPane(new DependencyMatrixView(root)));
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
