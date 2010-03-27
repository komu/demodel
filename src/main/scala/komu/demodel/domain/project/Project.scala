/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain.project

import collection.mutable.ArrayBuffer

final class Project(val name: String) {

  val _inputSources = new ArrayBuffer[InputSource]

  def addInputSource(inputSource: InputSource) {
    assert(inputSource != null)
    _inputSources += inputSource
  }

  def inputSources = _inputSources.toList
}
