/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import komu.demodel.utils.ExtensionFileFilter;
import komu.demodel.utils.FileResource;
import komu.demodel.utils.FileSet;
import komu.demodel.utils.Resource;

public final class ClassDirectoryInputSource implements InputSource {

    private final File directory;

    public ClassDirectoryInputSource(File directory) {
        if (directory == null) throw new IllegalArgumentException("null directory");

        this.directory = directory;
    }

    @Override
    public Iterable<Resource> getResources() {
        List<Resource> resources = new ArrayList<Resource>();

        for (File file : new FileSet(directory, new ExtensionFileFilter("class")))
            resources.add(new FileResource(file));

        return resources;
    }
    
    @Override
    public String toString() {
        return directory.toString();
    }
}
