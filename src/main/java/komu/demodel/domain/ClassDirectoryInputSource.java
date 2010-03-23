/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import komu.demodel.utils.Check;
import komu.demodel.utils.ExtensionFileFilter;
import komu.demodel.utils.FileResource;
import komu.demodel.utils.FileSet;
import komu.demodel.utils.Resource;
import komu.demodel.utils.ResourceProvider;

public final class ClassDirectoryInputSource implements InputSource {

    private final File directory;

    public ClassDirectoryInputSource(File directory) {
        Check.notNull(directory, "directory");

        this.directory = directory;
    }

    @Override
    public ResourceProvider getResources() {
        final List<Resource> resources = new ArrayList<Resource>();

        for (File file : new FileSet(directory, new ExtensionFileFilter("class")))
            resources.add(new FileResource(file));

        return new ResourceProvider() {
            @Override
            public Iterator<Resource> iterator() {
                return resources.iterator();
            }
            
            @Override
            public void close() throws IOException {
            }
        };
    }
    
    @Override
    public String toString() {
        return directory.toString();
    }
}
