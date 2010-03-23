/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils;

import java.io.Closeable;

public interface ResourceProvider extends Closeable, Iterable<Resource> {

}
