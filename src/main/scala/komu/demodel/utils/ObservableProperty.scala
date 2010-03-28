/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils

object ObservableProperty {
  def observable[T](value: T) = new ObservableProperty(value)
}

class ObservableProperty[T](private var value: T) {
  
  private var listeners: List[() => Unit] = Nil
  def apply() = value
  
  def := (value: T) {
    if (this.value != value) {
      this.value = value
      for (listener <- listeners)
        listener()
    }
  }
  
  def onChange(listener: => Unit) {
    listeners ::= { () => listener }
  }
}
