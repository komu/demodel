/*
 * Copyright (C) 2006 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import komu.demodel.domain.DependencyModel;
import komu.demodel.parser.java.JavaDependencyParser;

public class Main {
    
    public static void main(String[] args) {
        try {
            JavaDependencyParser parser = new JavaDependencyParser();
            parser.parseDirectory(new File("."));
            DependencyModel model = parser.getModel();
            
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            JFrame frame = new JFrame("demodel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new JScrollPane(new DependencyMatrixView(model)));
            frame.pack();
            frame.setVisible(true);
            
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
