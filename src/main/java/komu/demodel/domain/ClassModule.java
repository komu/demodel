/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import static java.util.Collections.emptyList;

import java.util.List;

public final class ClassModule extends Module {

    private boolean programModule = false;
    
    /** Detailed information about the type of this module, or null if we haven't visited it. */
    private TypeType type;
    
    public ClassModule(String name, PackageModule parent) {
        super(name, parent);
    }
   
    public void markAsProgramModule(TypeType type) {
        programModule = true;
        this.type = type;
    }
    
    @Override
    PackageModule getRoot() {
        return parent.getRoot();
    }

    public boolean isAbstract() {
        return type != null && type.isAbstract();
    }
    
    @Override
    public ModuleType getType() {
        return ModuleType.TYPE;
    }
    
    @Override
    public boolean isProgramModule() {
        return programModule;
    }
    
    @Override
    public boolean isLeaf() {
        return true;
    }
    
    @Override
    public int countLeafsUnder() {
        return 1;
    }
    
    @Override
    public void filterNonProgramReferences() {
    }
    
    @Override
    public List<Module> getChildren() {
        return emptyList();
    }
    
    @Override
    public void sortChildren() {
    }
}
