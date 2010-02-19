/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DependencyModel {

    private final List<Module> modules = new ArrayList<Module>();
    
    public List<Module> getModules() {
        return unmodifiableList(modules);
    }

    public Module getModuleAt(int index) {
        return modules.get(index);
    }

    public int indexOfModule(Module module) {
        int index = modules.indexOf(module);
        if (index != -1)
            return index;
        else
            throw new IllegalArgumentException("unknown module: " + module);
    }
    
    public int dependencyStrength(int from, int to) {
        Module fromModule = modules.get(from);
        Module toModule = modules.get(to);
        return fromModule.getDependencyStrength(toModule);
    }
    
    public void addModule(Module module) {
        if (module == null) throw new NullPointerException("null module");

        assert !modules.contains(module);
        
        modules.add(module);
    }
    
    public void moveUp(Module module) {
        int index = modules.indexOf(module);
        if (index > 0) {
            modules.remove(module);
            modules.add(index - 1, module);
        }
    }

    public void moveDown(Module module) {
        int index = modules.indexOf(module);
        if (index != -1 && index < modules.size() - 1) {
            modules.remove(module);
            modules.add(index + 1, module);
        }
    }
    
    public int getModuleCount() {
        return modules.size();
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
