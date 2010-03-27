/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui

import scala.collection.JavaConversions._

import java.io.File

import java.awt.BorderLayout
import java.awt.event.{ ActionEvent, KeyEvent }

import javax.swing.{ Action, AbstractAction, JFileChooser, JFrame, JMenu, 
  JMenuBar, JScrollPane, UIManager }
import javax.swing.filechooser.FileFilter

import komu.demodel.domain.{ ClassDirectoryInputSource, JarFileInputSource, Project }
import komu.demodel.parser.java.JavaDependencyParser
import komu.demodel.ui.matrix.DependencyMatrixView
import komu.demodel.ui.model.DependencyMatrixViewModel

object Main {

  def main(args: Array[String]) =
    try {
      initializePlatformLookAndFeel()

      val file = if (args.length == 0) chooseFileOrDirectory() else Some(new File(args(0)))
      file match {
        case Some(f) => loadFile(f)
        case None    => ()
      }
    } catch {
      case e => e.printStackTrace()
    }

  def loadFile(file: File) {
    val project = new Project(file.toString)
    if (file.isDirectory)
      project.addInputSource(new ClassDirectoryInputSource(file))
    else
      project.addInputSource(new JarFileInputSource(file))

    val parser = new JavaDependencyParser
            
    for (source <- project.getInputSources)
      parser.parse(source)

      val root = parser.getRoot

      val model = new DependencyMatrixViewModel(root)
      val view = new DependencyMatrixView(model)

      val fileMenu = new JMenu("File")
      fileMenu.setMnemonic('F')
      fileMenu.add(view.exportAction)
      if (!isRunningOnMacOSX) {
        // On OS X, we already got Quit under application menu, no need to add other exit action
        fileMenu.addSeparator()
        fileMenu.add(ExitAction)
      }

      val modulesMenu = new JMenu("Modules")
      modulesMenu.setMnemonic('M')
      modulesMenu.add(view.toggleAction)
      modulesMenu.add(view.sortModulesAction)
      modulesMenu.add(view.detailsAction)

      val menuBar = new JMenuBar
      menuBar.add(fileMenu)
      menuBar.add(modulesMenu)

      val frame = new JFrame("demodel")
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      frame.setJMenuBar(menuBar)
      frame.add(new JScrollPane(view), BorderLayout.CENTER)
      frame.add(new ModuleDetailsView(model), BorderLayout.EAST)
      frame.setSize(800, 600)
      frame.setLocationRelativeTo(null)
      frame.setVisible(true)
  }

  private def initializePlatformLookAndFeel() {
    if (isRunningOnMacOSX) {
      System.setProperty("apple.laf.useScreenMenuBar", "true")
      System.setProperty("com.apple.mrj.application.apple.menu.about.name", "demodel")
    }
        
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
  }

  def isRunningOnMacOSX = System.getProperty("mrj.version") != null
    
  def chooseFileOrDirectory() = {
    val chooser = new JFileChooser
    chooser.setDialogTitle("Select directory of class-files");
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    chooser.setFileFilter(new FileFilter {
        def accept(f: File) = f.isDirectory || f.getName.endsWith(".jar");
        def getDescription = "JAR-files and directories"
    });

    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
          Some(chooser.getSelectedFile) else None
  }
  
  object ExitAction extends AbstractAction("Exit") {
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X)
      
    def actionPerformed(e: ActionEvent) = System.exit(0)
  }
}
