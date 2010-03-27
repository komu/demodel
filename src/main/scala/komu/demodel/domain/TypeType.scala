/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain

sealed abstract class TypeType {
  def isAbstract = this != TypeType.CONCRETE_CLASS
}

object TypeType {
  case object CONCRETE_CLASS extends TypeType
  case object ABSTRACT_CLASS extends TypeType
  case object INTERFACE extends TypeType
}
