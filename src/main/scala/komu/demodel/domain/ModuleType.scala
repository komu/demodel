/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain

sealed abstract class ModuleType {
  def createModule(name: String, parent: PackageModule): Module
}

object ModuleType {
  case object PACKAGE extends ModuleType {
    def createModule(name: String, parent: PackageModule) = new PackageModule(name, Some(parent))
  }
  
  case object TYPE extends ModuleType {
    def createModule(name: String, parent: PackageModule) = new ClassModule(name, parent)
  }
}
