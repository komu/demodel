/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils.ui

import javax.swing.{ Action, JMenu, JMenuBar }

object MenuBuilder {
  def menuBar(action: MenuBarBuilder => Unit) = {
    var builder = new MenuBarBuilder
    action(builder)
    builder.menuBar
  }
}

class MenuBarBuilder {
  val menuBar = new JMenuBar
  
  def menu(name: String, mnenomic: Char) (action: MenuBuilder => Unit) = {
    var builder = new MenuBuilder(name, mnenomic)
    action(builder)
    menuBar.add(builder.menu)
  }
}

class MenuBuilder(name: String, mnemonic: Char) {
  val menu = new JMenu(name)
  menu.setMnemonic(mnemonic)
  
  def addSeparator() = menu.addSeparator()
  def add(action: Action) = menu.add(action)
}
