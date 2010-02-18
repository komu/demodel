/*
 * Copyright (C) 2006 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import java.util.ArrayList;
import java.util.List;

public class DependencyModel {

    private final List<Module> modules = new ArrayList<Module>();
    
    public DependencyModel() {
        /*
        Module a = new Module("Module A");
        Module b = new Module("Module B");
        Module c = new Module("Module C");
        Module d = new Module("Module D");
        Module e = new Module("Module E");
        
        a.setDependencyStrength(b, 10);
        a.setDependencyStrength(e, 4);
        b.setDependencyStrength(a, 10);
        b.setDependencyStrength(c, 4);
        b.setDependencyStrength(d, 2);
        b.setDependencyStrength(e, 4);
        c.setDependencyStrength(a, 5);
        c.setDependencyStrength(d, 4);
        c.setDependencyStrength(e, 1);
        d.setDependencyStrength(a, 8);
        d.setDependencyStrength(b, 5);
        d.setDependencyStrength(e, 284);
        e.setDependencyStrength(a, 10);
        e.setDependencyStrength(b, 4);
        e.setDependencyStrength(d, 4);
        
        modules.addAll(Arrays.asList(a, b, c, d, e));
        */
    }
    
    public List<Module> getModules() {
        return modules;
    }
}
