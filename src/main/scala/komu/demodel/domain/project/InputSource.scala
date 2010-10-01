/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain.project

import komu.demodel.utils.Resource

trait InputSource {
  def withResources(thunk: Resource => Unit)
  
  def mapResources[T](thunk: Resource => T): Iterable[T] = {
    val result = new scala.collection.mutable.ArrayBuffer[T]
    withResources(r => result += thunk(r))
    result
  }
}
