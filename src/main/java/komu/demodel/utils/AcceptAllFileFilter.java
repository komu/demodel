/*
 * Copyright (C) 2006 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils;

import java.io.File;
import java.io.FileFilter;

public final class AcceptAllFileFilter implements FileFilter {
    public boolean accept(File pathname) {
        return true;
    }
}
