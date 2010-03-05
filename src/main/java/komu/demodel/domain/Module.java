/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import static java.util.Collections.min;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public final class Module {

    private final String name;
    private final Module parent;
    private boolean programModule = false;
    private final List<Module> children = new ArrayList<Module>();
    private final Map<Module,Integer> strengths = new HashMap<Module,Integer>();
    
    public Module(String name, Module parent) {
        if (name ==  null) throw new NullPointerException("null name");
        
        this.name = name;
        this.parent = parent;
        if (parent != null)
            parent.children.add(this);
    }
    
    public int getDepth() {
        return (parent != null) ? parent.getDepth() + 1 : 0;
    }
    
    public Module getParent() {
        return parent;
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
    
    public void filterNonProgramReferences() {
        for (Iterator<Module> it = children.iterator(); it.hasNext(); ) {
            Module child = it.next();
            if (child.isProgramModule())
                child.filterNonProgramReferences();
            else
                it.remove();
        }
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
    
    public void move(MoveDirection direction) {
        if (parent != null)
            parent.move(this, direction);
    }
    
    private void move(Module module, MoveDirection direction) {
        int index = children.indexOf(module);
        int newIndex = index + direction.delta;
        if (index != -1 && isValidChildIndex(newIndex)) {
            children.remove(module);
            children.add(newIndex, module);
        }
    }

    private boolean isValidChildIndex(int index) {
        return index >= 0 && index < children.size();
    }

    private static Module pickModuleWithLeastIncomingDependencies(List<Module> modules) {
        assert !modules.isEmpty();

        List<ModuleWithWeight> weightedModules = new ArrayList<ModuleWithWeight>(modules.size());
        for (Module module : modules) 
            weightedModules.add(new ModuleWithWeight(module, module.incomingDependencies(modules)));
    
        return min(weightedModules).module;
    }
    
    private int incomingDependencies(Collection<Module> modules) {
        int sum = 0;
        for (Module m : modules)
            sum += m.getDependencyStrength(this);
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
