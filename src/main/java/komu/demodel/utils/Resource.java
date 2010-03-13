/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils;

import java.io.IOException;
import java.io.InputStream;

public interface Resource {
    InputStream open() throws IOException;
}
