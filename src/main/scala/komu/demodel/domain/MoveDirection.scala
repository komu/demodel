/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain

sealed abstract class MoveDirection(val delta: Int)

object MoveDirection {
  case object UP extends MoveDirection(-1)
  case object DOWN extends MoveDirection(1)
}
