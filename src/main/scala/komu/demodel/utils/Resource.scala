/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils

import java.io.InputStream

trait Resource {
  def open(): InputStream
}
