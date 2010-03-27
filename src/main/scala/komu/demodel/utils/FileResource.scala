/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils

import java.io.{ File, FileInputStream }

final class FileResource(file: File) extends Resource {
  assert(file != null)

  def open() = new FileInputStream(file)
}
