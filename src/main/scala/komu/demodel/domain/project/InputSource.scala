/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain.project

import komu.demodel.utils.Resource

trait InputSource {
  def withResources(thunk: Resource => Unit)
}
