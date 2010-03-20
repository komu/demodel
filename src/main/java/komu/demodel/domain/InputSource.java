/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import java.io.IOException;

import komu.demodel.utils.ResourceProvider;

public interface InputSource {
    ResourceProvider getResources() throws IOException;
}
