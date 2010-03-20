/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import komu.demodel.utils.Resource;
import komu.demodel.utils.ResourceProvider;

public final class JarFileInputSource implements InputSource {

    private final File file;
    
    public JarFileInputSource(File file) {
        if (file == null) throw new NullPointerException("null file");
        
        this.file = file;
    }
    
    @Override
    public ResourceProvider getResources() throws IOException {
        final JarFile jar = new JarFile(file);
        final List<Resource> entries = getClassEntries(jar);

        return new ResourceProvider() {
            @Override
            public Iterator<Resource> iterator() {
                return entries.iterator();
            }
            
            @Override
            public void close() throws IOException {
                jar.close();
            }
        };
    }

    private static List<Resource> getClassEntries(JarFile jar) {
        List<Resource> classEntries = new ArrayList<Resource>();
        for (Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements(); ) {
            JarEntry entry = e.nextElement();
        
            if (entry.getName().endsWith(".class"))
                classEntries.add(new JarEntryResource(jar, entry));
        }
        return classEntries;
    }
    
    @Override
    public String toString() {
        return file.toString();
    }

    private static class JarEntryResource implements Resource {

        private final JarFile jar;
        private final JarEntry entry;

        public JarEntryResource(JarFile jar, JarEntry entry) {
            this.jar = jar;
            this.entry = entry;
        }
        
        @Override
        public InputStream open() throws IOException {
            return jar.getInputStream(entry);
        }
    }
}
