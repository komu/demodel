/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui.model

import collection.immutable.HashSet
import collection.mutable.ArrayBuffer

import javax.swing.event.{ ChangeEvent, ChangeListener }

import komu.demodel.domain.{ Dependency, Module, MoveDirection }
import komu.demodel.utils.{ ChangeListenerList, IdentityHashSet }
import komu.demodel.utils.ObservableProperty.observable

/**
 * Represents the state of the view, but not of the underlying model.
 * As an example, the order of the modules is defined by the real model,
 * but which modules are currently expanded or selected are not.
 * Also does translation from the visible indices to underlying indices.
 */
final class DependencyMatrixViewModel(root: Module) {

  val selectedRow = observable[Option[Module]](Some(root))
  val selectedColumn = observable[Option[Module]](None)
  private val openedModules = observable(HashSet[Module](root))
  private val NO_SELECTION = -1
  private val listeners = new ChangeListenerList
  private var cachedVisibleModules: ArrayBuffer[Module] = null
  
  selectedRow.onChange { fireStateChanged() }
  selectedColumn.onChange { fireStateChanged() }
  openedModules.onChange { 
    cachedVisibleModules = null
    fireStateChanged()
  }
 
  def flushCaches() {
    cachedVisibleModules = null
    root.flushCaches()
  }

  def onChange(listener: => Unit) {
    listeners.add(new ChangeListener {
      def stateChanged(event: ChangeEvent) = listener
    })
  }
    
  private def fireStateChanged() =
    listeners.stateChanged(new ChangeEvent(this))
  
  def visibleModuleCount = visibleModules.size

  def dependencyStrength(from: Int, to: Int) = {
    var modules = visibleModules
    modules(from).getDependencyStrength(modules(to))
  }
    
  def dependencyDetailsOfSelectedCell: String = {
    val row = selectedRow()
    val col = selectedColumn() 
    if (row.isEmpty || col.isEmpty) return ""
    
    var sb = new StringBuilder
    sb.append("Dependencies from module\n")
    sb.append(col.get.name)
    sb.append("\nto module\n    ")
    sb.append(row.get.name)
    sb.append("\n\n")
        
    listDependencies(col.get, row.get, sb)
    sb.toString
  }

  private def listDependencies(fromModule: Module, toModule: Module, sb: StringBuilder) {
    for (fromChild <- fromModule.children)
      listDependencies(fromChild, toModule, sb)

    for (toChild <- toModule.children)
      listDependencies(fromModule, toChild, sb)

    var dependencies = fromModule.getDependencies(toModule)
    if (dependencies != null)
      for (d <- dependencies)
        sb.append("Depencency from=").append(fromModule.name).append(" ").append(d).append("\n")
  }

  def visibleModules: IndexedSeq[Module] = {
    if (cachedVisibleModules == null) {
      val modules = new ArrayBuffer[Module]
      addVisibleModules(modules, List(root))
      cachedVisibleModules = modules
    }
    cachedVisibleModules
  }

  private def addVisibleModules(result: ArrayBuffer[Module], modules: Iterable[Module]) {
    for (module <- modules) {
      result += module
      if (openedModules().contains(module))
        addVisibleModules(result, module.children)
    }
  }

  def getModuleAt(index: Int) = visibleModules(index)

  def moveSelection(direction: MoveDirection) {
    if (selectedRow().isEmpty) return
        
    var index = visibleModules.indexOf(selectedRow().get)
    assert(index != -1)
        
    val newIndex = index + direction.delta
    if (newIndex >= 0 && newIndex < visibleModules.size) {
      selectedRow := Some(visibleModules(newIndex))
      fireStateChanged()
    }
  }

  def sortModules() =
    for (row <- selectedRow()) {
      row.sortChildren()
      flushCaches()
      fireStateChanged()
    }

  def moveSelectedModule(direction: MoveDirection) =
    for (row <- selectedRow()) {
      row.move(direction)
      flushCaches()
      fireStateChanged()
    }

  def openSelectedModule() =
    for (row <- selectedRow() if !row.isLeaf)
      openedModules := openedModules() + row

  def closeSelectedModule() =
    for (row <- selectedRow() if !row.isLeaf)
      openedModules := openedModules() - row
    
  def isOpened(module: Module) = openedModules().contains(module)

  def allModules = root.selfAndAncestors
    
  def hasSelectedRow = selectedRow().isDefined
  def hasSelectedCell = selectedRow().isDefined && selectedColumn().isDefined
}
