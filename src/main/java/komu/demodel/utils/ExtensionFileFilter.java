/*
 * Copyright (C) 2006 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils;

import java.io.File;
import java.io.FileFilter;

public final class ExtensionFileFilter implements FileFilter {

    private final String suffix;
    
    public ExtensionFileFilter(String extension) {
        this.suffix = "." + extension;
    }
    
    public boolean accept(File file) {
        return file.isFile() && file.getName().endsWith(suffix);
    }
}
