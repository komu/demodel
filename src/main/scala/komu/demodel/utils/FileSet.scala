/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils

import java.io.{ File, FileFilter }

final class FileSet(rootDirectory: File, filter: FileFilter) extends Iterable[File] {
  assert(rootDirectory != null)
  assert(filter != null)

  def iterator = new FileIterator(rootDirectory, filter)
}
