/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain

import collection.mutable.ArrayBuffer

final class PackageModule(name: String, parent: Option[PackageModule]) extends Module(name, parent) {

  private val _children = new ArrayBuffer[Module]

  lazy val metrics = {
    val metrics = new PackageMetrics

    metrics.numberOfTypes = countTypes
    metrics.numberOfAbstractTypes = countAbstractTypes
    metrics.afferentCouplings = countAfferentCouplings
    metrics.efferentCouplings = countEfferentCouplings
        
    metrics
  }

  private def countEfferentCouplings =
    root.packagesUnder.filter(m => m != this && this.hasDependenciesToClassesIn(m)).size

  private def countAfferentCouplings =
    root.packagesUnder.filter(m => m != this && m.hasDependenciesToClassesIn(this)).size
  
  private def hasDependenciesToClassesIn(module: PackageModule): Boolean = {
    for (myClass <- classes)
      for (theirClass <- module.classes)
        if (myClass.getDependencyStrength(theirClass) > 0)
          return true
    return false
  }

  def children = _children.toList

  private def packagesUnder =
    for (m <- selfAndAncestors if m.isInstanceOf[PackageModule])
      yield m.asInstanceOf[PackageModule]

  private def classes =
    for (m <- _children if m.isInstanceOf[ClassModule])
      yield m.asInstanceOf[ClassModule]

  private def countTypes = classes.size
    
  private def countAbstractTypes =
    classes.filter(_.isAbstract).size

  def root = 
    parent match {
      case Some(p) => p.root
      case None    => this
    }

  def moduleType = ModuleType.PACKAGE
  
  def isLeaf = children.isEmpty  

  def isProgramModule = children.exists(_.isProgramModule)

  def countLeafsUnder =
    if (isLeaf)
      1
    else
      children.map(_.countLeafsUnder).sum

  
  /**
   * Normalize tree so that all packages contain only packages or no packages
   * at all. If a package has both, then a new pseudo-package is created under
   * that package and all non-packages are moved under that.
   */
  def normalizeTree() {
    val packages = new ArrayBuffer[PackageModule]
    val nonPackages = new ArrayBuffer[Module]

    for (child <- _children)
      child match {
        case p: PackageModule => 
          p.normalizeTree()
          packages += p
        case c =>
          nonPackages += c
      }


    if (!packages.isEmpty && !nonPackages.isEmpty) {
      val pseudoPackage = new PackageModule("<classes>", Some(this))
      pseudoPackage._children ++= nonPackages
      _children --= nonPackages
    }
  }

  protected[domain] def addChild(child: Module) {
    assert(child.parent == Some(this))
    _children += child
  }
  
  def move(module: Module, direction: MoveDirection) {
    val index = children.indexOf(module)
    val newIndex = index + direction.delta
    if (index != -1 && isValidChildIndex(newIndex)) {
      _children -= module
      _children.insert(newIndex, module)
    }
  }
    
  def sortChildren() {
    val workList = new ArrayBuffer[Module](_children.size)
    workList ++= _children
    _children.clear()
        
    while (!workList.isEmpty) {
      val module = pickModuleWithLeastOutgoingDependencies(workList)
      workList -= module
      _children.insert(0, module)
    }
  }
    
  def filterNonProgramReferences() {
    _children --= _children.filter(!_.isProgramModule)
    for (child <- _children)
      child.filterNonProgramReferences()
  }

  private def pickModuleWithLeastOutgoingDependencies(modules: Iterable[Module]) = {
    assert(!modules.isEmpty)

    var minimumModule = modules.head
    var minimumDependencies = minimumModule.outgoingDependencies(modules)
    for (module <- modules.tail) {
      val dependencies = module.outgoingDependencies(modules)
      if (dependencies < minimumDependencies) {
        minimumDependencies = dependencies
        minimumModule = module
      }
    }
        
    minimumModule
  }
    
  private def isValidChildIndex(index: Int) = index >= 0 && index < _children.size
}
