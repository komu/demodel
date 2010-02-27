/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

public class DependencyModel {

    private final List<Module> roots = new ArrayList<Module>();
    
    public List<Module> getModules() {
        return unmodifiableList(roots);
    }
    
    public void addRootModule(Module module) {
        if (module == null) throw new NullPointerException("null module");

        assert !roots.contains(module);
        
        roots.add(module);
    }
}
