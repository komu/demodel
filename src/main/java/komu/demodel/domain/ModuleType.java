/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

public enum ModuleType {
    PACKAGE {
        @Override
        public Module createModule(String name, PackageModule parent) {
            return new PackageModule(name, parent);
        }
    },
    
    TYPE {
        @Override
        public Module createModule(String name, PackageModule parent) {
            return new ClassModule(name, parent);
        }
    };

    public abstract Module createModule(String name, PackageModule parent);
}
