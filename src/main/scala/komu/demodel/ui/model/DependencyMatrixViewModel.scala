/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui.model

import scala.collection.JavaConversions._

import java.util.Collections.{ singletonList, unmodifiableList }
import java.util.{ ArrayList, List }

import javax.swing.event.{ ChangeEvent, ChangeListener }

import komu.demodel.domain.{ Dependency, Module, MoveDirection }
import komu.demodel.utils.{ ChangeListenerList, IdentityHashSet }

/**
 * Represents the state of the view, but not of the underlying model.
 * As an example, the order of the modules is defined by the real model,
 * but which modules are currently expanded or selected are not.
 * Also does translation from the visible indices to underlying indices.
 */
final class DependencyMatrixViewModel(root: Module) {

  private var _selectedRow: Option[Module] = Some(root)
  private var _selectedColumn: Option[Module] = None
  
  private val openedModules = new IdentityHashSet[Module]
  openedModules.add(root)

  private val NO_SELECTION = -1
    
  private val listeners = new ChangeListenerList
  private var cachedVisibleModules: List[Module] = null
    
  def flushCaches() {
    cachedVisibleModules = null
    root.flushCaches()
  }

  def addListener(listener: ChangeListener) =
    listeners.add(listener)
    
  private def fireStateChanged() =
    listeners.stateChanged(new ChangeEvent(this))

  def selectedRow = _selectedRow
  
  def selectedRow_=(module: Option[Module]) {
    if (module != _selectedRow) {
      _selectedRow = module
      fireStateChanged()
    }
  }
  
  def selectedColumn = _selectedColumn

  def selectedColumn_=(module: Option[Module]) {
    if (module != _selectedColumn) {
      _selectedColumn = module
      fireStateChanged()
    }
  }
    
  def visibleModuleCount = visibleModules.size

  def dependencyStrength(from: Int, to: Int) = {
    var modules = visibleModules
    modules.get(from).getDependencyStrength(modules.get(to))
  }
    
  def dependencyDetailsOfSelectedCell: String = {
    if (selectedRow.isEmpty || selectedColumn.isEmpty) return ""

    var sb = new StringBuilder
    sb.append("Dependencies from module\n")
    sb.append(selectedColumn.get.getName)
    sb.append("\nto module\n    ")
    sb.append(selectedRow.get.getName)
    sb.append("\n\n")
        
    listDependencies(selectedColumn.get, selectedRow.get, sb)
    sb.toString
  }

  private def listDependencies(fromModule: Module, toModule: Module, sb: StringBuilder) {
    for (fromChild <- fromModule.getChildren)
      listDependencies(fromChild, toModule, sb)

    for (toChild <- toModule.getChildren)
      listDependencies(fromModule, toChild, sb)

    var dependencies = fromModule.getDependencies(toModule)
    if (dependencies != null)
      for (d <- dependencies)
        sb.append("Depencency from=").append(fromModule.getName).append(" ").append(d).append("\n")
  }

  def visibleModules = {
    if (cachedVisibleModules == null) {
      val modules = new ArrayList[Module]
      addVisibleModules(modules, singletonList(root))
      cachedVisibleModules = unmodifiableList(modules)
    }
    cachedVisibleModules
  }

  private def addVisibleModules(result: ArrayList[Module], modules: Iterable[Module]) {
    for (module <- modules) {
      result.add(module)
      if (openedModules.contains(module))
        addVisibleModules(result, module.getChildren())
    }
  }

  def getModuleAt(index: Int) = visibleModules.get(index)

  def moveSelection(direction: MoveDirection) {
    if (selectedRow.isEmpty) return
        
    var modules = visibleModules
    var index = visibleModules.indexOf(selectedRow.get)
    assert(index != -1)
        
    val newIndex = index + direction.delta
    if (newIndex >= 0 && newIndex < visibleModules.size) {
      selectedRow = Some(visibleModules.get(newIndex))
      fireStateChanged()
    }
  }

  def sortModules() =
    if (selectedRow.isDefined) {
      selectedRow.get.sortChildren()
      flushCaches()
      fireStateChanged()
    }

  def moveSelectedModule(direction: MoveDirection) =
    if (selectedRow.isDefined) {
      selectedRow.get.move(direction)
      flushCaches()
      fireStateChanged()
    }

  def openSelectedModule() =
    if (selectedRow.isDefined && !selectedRow.get.isLeaf() && openedModules.add(selectedRow.get)) {
      cachedVisibleModules = null
      fireStateChanged()
    }

  def closeSelectedModule() =
    if (selectedRow.isDefined && !selectedRow.get.isLeaf() && openedModules.remove(selectedRow.get)) {
      cachedVisibleModules = null
      fireStateChanged()
    }
    
  def isOpened(module: Module) = openedModules.contains(module)

  def allModules = root.getSelfAndAncestors
    
  def hasSelectedRow = selectedRow.isDefined
  def hasSelectedCell = selectedRow.isDefined && selectedColumn.isDefined
}
