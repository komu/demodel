/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain

final class ClassModule(name: String, _parent: PackageModule) extends Module(name, Some(_parent)) {

  /** Detailed information about the type of this module, or null if we haven't visited it. */
  private var kind: Option[TypeType] = None

  def markAsProgramModule(kind: TypeType) {
    this.kind = Some(kind)
  }

  def root = _parent.root
  def isAbstract = kind.map(_.isAbstract).getOrElse(false)
  def moduleType = ModuleType.TYPE  
  def isProgramModule = kind.isDefined
  def isLeaf = true
  def countLeafsUnder = 1
  def filterNonProgramReferences() { }
  def children = List()
  def sortChildren() { }
}
