/*
 * Copyright (C) 2006 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    public void sortModules() {
        List<Module> workList = new ArrayList<Module>(modules);
        modules.clear();
        
        while (!workList.isEmpty()) {
            Module module = pickModuleWithLeastIncomingDependencies(workList);
            workList.remove(module);
            modules.add(module);
        }
    }

    private static Module pickModuleWithLeastIncomingDependencies(List<Module> modules) {
        if (modules.isEmpty()) throw new IllegalArgumentException("empty modules");

        List<ModuleWithWeight> weightedModules = new ArrayList<ModuleWithWeight>(modules.size());
        for (Module module : modules) 
            weightedModules.add(new ModuleWithWeight(module, incomingDependencies(module, modules)));
    
        return Collections.min(weightedModules).module;
    }
    
    private static int incomingDependencies(Module module, Collection<Module> modules) {
        int sum = 0;
        for (Module m : modules)
            sum += m.getDependencyStrength(module);
        return sum;
    }
    
    private static class ModuleWithWeight implements Comparable<ModuleWithWeight> {

        final Module module;
        final int weight;

        public ModuleWithWeight(Module module, int weight) {
            this.module = module;
            this.weight = weight;
        }
        
        public int compareTo(ModuleWithWeight o) {
            return weight - o.weight;
        }
        
    }
}
