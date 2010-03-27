/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils

import javax.swing.event.{ ChangeEvent, ChangeListener }

final class ChangeListenerList {

  private var listeners: List[ChangeListener] = Nil
    
  def add(listener: ChangeListener) {
    assert(listener != null)
    listeners ::= listener
  }
    
  def stateChanged(event: ChangeEvent) =
    for (listener <- listeners)
      listener.stateChanged(event)
}
