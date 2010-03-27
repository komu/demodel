/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain.project

import java.util.Collections.unmodifiableList
import java.util.{ ArrayList, List }

final class Project(name: String) {

  val _inputSources = new ArrayList[InputSource]

  def addInputSource(inputSource: InputSource) {
    assert(inputSource != null)
    _inputSources.add(inputSource)
  }

  def inputSources = unmodifiableList(_inputSources)
}
