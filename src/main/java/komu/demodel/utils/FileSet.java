/*
 * Copyright (C) 2006 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;

public final class FileSet implements Iterable<File> {

    private final File rootDirectory;
    private final FileFilter filter;
    
    public FileSet(File rootDirectory, FileFilter filter) {
        this.rootDirectory = rootDirectory;
        this.filter = filter;
    }

    public Iterator<File> iterator() {
        return new FileIterator(rootDirectory, filter);
    }
}
