/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain

import collection.mutable.{ ArrayBuffer, HashMap }

abstract class Module(val name: String, val parent: Option[PackageModule]) {

  for (p <- parent)
    p.addChild(this)

  private val strengths = new HashMap[Module,Int]
  private val dependencies = new HashMap[Module,List[Dependency]]
  private val cachedDependencyStrengths = new HashMap[Module,Int]
  private var cachedSelfAndAncestors: List[Module] = null

  def children: List[Module]
  def root: PackageModule
  def moduleType: ModuleType
  def sortChildren()
  def isProgramModule: Boolean
  def filterNonProgramReferences()
  def isLeaf: Boolean
  def countLeafsUnder: Int

  def flushCaches() {
    cachedDependencyStrengths.clear();
    cachedSelfAndAncestors = null;
        
    for (child <- children) child.flushCaches()
  }
    
  def depth: Int =
    parent match {
      case Some(p) => p.depth + 1
      case None    => 0
    }

  def localName =
    parent match {
      case None    => name
      case Some(p) => if (name.startsWith(p.name + "."))
                        name.substring(p.name.length+1)
                      else
                        name
    }

  def getDependencyStrength(module: Module): Int =
    cachedDependencyStrengths.getOrElseUpdate(module, getUncachedDependencyStrength(module))

  private def getUncachedDependencyStrength(module: Module): Int = {
    val myAncestors = selfAndAncestors
    if (myAncestors.contains(module)) return 0
            
    val targetAncestors = module.selfAndAncestors
    if (targetAncestors.contains(this)) return 0
            
    var strength = 0
    for (ancestor <- myAncestors)
      for (target <- targetAncestors)
        strength += ancestor.getDirectDependencyStrength(target)
    strength
  }
    
  def selfAndAncestors = {
    if (cachedSelfAndAncestors == null) {
        val result = new ArrayBuffer[Module]
        result += this
        addAncestors(result)
        cachedSelfAndAncestors = result.toList
    }
    cachedSelfAndAncestors;
  }
    
  private def addAncestors(result: ArrayBuffer[Module]) {
    for (child <- children) {
      result += child
      child.addAncestors(result)
    }
  }

  def getDirectDependencyStrength(module: Module) =
    strengths.getOrElse(module, 0)

  def setDependencyStrength(module: Module, strength: Int) {
    assert(module != null)
    assert(strength >= 0)
        
    strengths.put(module, strength)
  }

  def addDependency(module: Module, kind: DependencyType) {
    assert(module != null)
    assert(kind != null)
        
    strengths(module) = getDirectDependencyStrength(module) + 1
    dependencies(module) = new Dependency(module, kind) :: getDependencies(module)
  }
  
  def getDependencies(to: Module) =
    dependencies.getOrElse(to, Nil)

  override def toString = name

  def move(direction: MoveDirection) {
    for (p <- parent)
      p.move(this, direction)
  }

  def outgoingDependencies(modules: Iterable[Module]) =
    modules.map(getDependencyStrength(_)).sum
}
