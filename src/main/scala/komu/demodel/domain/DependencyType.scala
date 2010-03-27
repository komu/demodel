/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain

sealed abstract class DependencyType

object DependencyType {
  case object INHERITANCE extends DependencyType
  case object FIELD_REF extends DependencyType
  case object REF extends DependencyType
}
