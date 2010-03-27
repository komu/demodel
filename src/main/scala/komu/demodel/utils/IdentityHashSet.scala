/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils

import java.util.{ AbstractSet, IdentityHashMap, Iterator }

final class IdentityHashSet[T] extends AbstractSet[T] {
  private val map = new IdentityHashMap[T,Object]
  
  def iterator = map.keySet.iterator  
  def size = map.size
  override def add(o: T) = map.put(o, new Object()) == null
  override def remove(o: Object) = map.remove(o) != null
}
