/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import komu.demodel.domain.ClassDirectoryInputSource;
import komu.demodel.domain.InputSource;
import komu.demodel.domain.JarFileInputSource;
import komu.demodel.domain.Module;
import komu.demodel.domain.Project;
import komu.demodel.parser.java.JavaDependencyParser;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            File file = (args.length == 0) ? chooseFileOrDirectory() : new File(args[0]);
            if (file == null) return;

            Project project = new Project(file.toString());
            if (file.isDirectory())
                project.addInputSource(new ClassDirectoryInputSource(file));
            else
                project.addInputSource(new JarFileInputSource(file));

            JavaDependencyParser parser = new JavaDependencyParser();
            for (InputSource source : project.getInputSources())
                parser.parse(source);

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

    private static File chooseFileOrDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select directory of class-files");
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".jar");
            }
            @Override
            public String getDescription() {
                return "JAR-files and directories";
            }
        });

        return (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            ? chooser.getSelectedFile()
            : null;
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
