/*
 * Copyright (C) 2006 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.LinkedList;

public final class FileIterator implements Iterator<File> {

    private final LinkedList<File> files = new LinkedList<File>();
    private final LinkedList<File> directories = new LinkedList<File>();
    private final FileFilter filter;
    private File current;
    
    public FileIterator(File rootDirectory, FileFilter filter) {
        directories.add(rootDirectory);
        
        this.filter = filter;
        
        addIfAccepted(rootDirectory);
        
        step();
    }

    public boolean hasNext() {
        return !files.isEmpty();
    }
    
    public File next() {
        if (files.isEmpty()) {
            throw new IllegalStateException("no more files");
        }
        
        File file = files.removeFirst();
        step();
        return file;
    }
    
    public void remove() {
        if (current == null) {
            throw new IllegalStateException("no current file");
        }
        
        current.delete();
        current = null;
    }
    
    private void step() {
        while (files.isEmpty() && !directories.isEmpty()) {
            File directory = directories.removeFirst();
            
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    directories.add(file);
                }
                
                addIfAccepted(file);
            }
        }
    }

    private void addIfAccepted(File file) {
        if (filter.accept(file)) {
            files.add(file);
        }
    }
}
