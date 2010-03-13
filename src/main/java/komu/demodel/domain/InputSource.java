/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import komu.demodel.utils.Resource;

public interface InputSource {
    Iterable<Resource> getResources();
}
