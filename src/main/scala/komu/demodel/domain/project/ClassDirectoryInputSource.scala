/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain.project

import java.io.File
import komu.demodel.utils.{ Resource, FileSet, ExtensionFileFilter, FileResource }

final class ClassDirectoryInputSource(directory: File) extends InputSource {
  assert(directory != null)

  def withResources(thunk: Resource => Unit) {
    for (file <- new FileSet(directory, new ExtensionFileFilter("class")))
      thunk(new FileResource(file))
  }
    
  override def toString = directory.toString
}
