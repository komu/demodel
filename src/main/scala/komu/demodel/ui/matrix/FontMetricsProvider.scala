/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui.matrix

import java.awt.{ Font, FontMetrics }

trait FontMetricsProvider {
  def getFontMetrics(font: Font): FontMetrics
}
