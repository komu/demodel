/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import static java.util.Collections.min;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Module {

    private final String name;
    private boolean programModule = false;
    private final List<Module> children = new ArrayList<Module>();
    private final Map<Module,Integer> strengths = new HashMap<Module,Integer>();
    
    public Module(String name) {
        if (name ==  null) throw new NullPointerException("null name");
        
        this.name = name;
    }
 
    public String getName() {
        return name;
    }
    
    public boolean isProgramModule() {
        if (programModule) return true;
        
        for (Module child : children)
            if (child.isProgramModule()) return true;
        
        return false;
    }
    
    public void markAsProgramModule() {
        programModule = true;
    }
    
    public boolean isLeaf() {
        return children.isEmpty();
    }
    
    public int countLeafsUnder() {
        if (isLeaf()) return 1;
        
        int count = 0;
        for (Module child : children)
            count += child.countLeafsUnder();
        
        return count;
    }
    
    public void addChild(Module child) {
        if (child == null) throw new NullPointerException("null child");
        
        children.add(child);
    }
    
    public List<Module> getChildren() {
        return unmodifiableList(children);
    }
    
    public int getDependencyStrength(Module module) {
        List<Module> myAncestors = getSelfAndAncestors();
        if (myAncestors.contains(module)) return 0;
        
        List<Module> targetAncestors = module.getSelfAndAncestors();
        if (targetAncestors.contains(this)) return 0;
        
        int strength = 0;
        for (Module ancestor : myAncestors)
            for (Module target : targetAncestors)
                strength += ancestor.getDirectDependencyStrength(target);

        return strength;
    }
    
    private List<Module> getSelfAndAncestors() {
        List<Module> result = new ArrayList<Module>();
        result.add(this);
        addAncestors(result);
        return result;
    }
    
    private void addAncestors(List<Module> result) {
        for (Module child : children) {
            result.add(child);
            child.addAncestors(result);
        }
    }

    private int getDirectDependencyStrength(Module module) {
        Integer strength = strengths.get(module);
        return (strength != null) ? strength.intValue() : 0;
    }

    public void setDependencyStrength(Module module, int strength) {
        if (module == null) throw new NullPointerException("null module");
        if (strength < 0) throw new IllegalArgumentException("negative strength: " + strength);
        
        strengths.put(module, strength);
    }

    public void addDependency(Module module, DependencyType type) {
        if (module == null) throw new NullPointerException("null module");
        if (type == null) throw new NullPointerException("null type");
        
        Integer strength = strengths.get(module);
        strengths.put(module, (strength != null) ? (strength + 1) : 1);
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public void sortChildren() {
        List<Module> workList = new ArrayList<Module>(children);
        children.clear();
        
        while (!workList.isEmpty()) {
            Module module = pickModuleWithLeastIncomingDependencies(workList);
            workList.remove(module);
            children.add(module);
        }
    }

    private static Module pickModuleWithLeastIncomingDependencies(List<Module> modules) {
        assert !modules.isEmpty();

        List<ModuleWithWeight> weightedModules = new ArrayList<ModuleWithWeight>(modules.size());
        for (Module module : modules) 
            weightedModules.add(new ModuleWithWeight(module, incomingDependencies(module, modules)));
    
        return min(weightedModules).module;
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
