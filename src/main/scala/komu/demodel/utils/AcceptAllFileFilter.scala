/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils

import java.io.{ File, FileFilter }

final class AcceptAllFileFilter extends FileFilter {
  def accept(file: File) = true
}
