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

    public Module getModuleAt(int index) {
        return roots.get(index);
    }
    
    public void addRootModule(Module module) {
        if (module == null) throw new NullPointerException("null module");

        assert !roots.contains(module);
        
        roots.add(module);
    }
    
    public void moveUp(Module module) {
        int index = roots.indexOf(module);
        if (index > 0) {
            roots.remove(module);
            roots.add(index - 1, module);
        }
    }

    public void moveDown(Module module) {
        int index = roots.indexOf(module);
        if (index != -1 && index < roots.size() - 1) {
            roots.remove(module);
            roots.add(index + 1, module);
        }
    }
    
    public int getModuleCount() {
        return roots.size();
    }
    
}
