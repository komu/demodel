/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class FileResource implements Resource {

    private final File file;

    public FileResource(File file) {
        Check.notNull(file, "file");

        this.file = file;
    }

    @Override
    public InputStream open() throws IOException {
        return new FileInputStream(file);
    }
}
