/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import komu.demodel.utils.Check;

public abstract class Module {

    private final String name;
    protected final PackageModule parent;
    private final Map<Module,Integer> strengths = new HashMap<Module,Integer>();
    private final Map<Module,List<Dependency>> dependencies = new HashMap<Module,List<Dependency>>();
    
    private final Map<Module,Integer> cachedDependencyStrengths = new HashMap<Module,Integer>();
    private List<Module> cachedSelfAndAncestors = null;
    
    public Module(String name, PackageModule parent) {
        Check.notNull(name, "name");
        
        this.name = name;
        this.parent = parent;
        if (parent != null)
            parent.addChild(this);
    }
    
    public abstract ModuleType getType();
    public abstract List<Module> getChildren();
    public abstract void sortChildren();
    
    abstract PackageModule getRoot();
    
    public void flushCaches() {
        cachedDependencyStrengths.clear();
        cachedSelfAndAncestors = null;
        
        for (Module child : getChildren())
            child.flushCaches();
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
    
    public String getLocalName() {
        if (parent == null) return name;
        
        return name.startsWith(parent.getName() + ".")
                ? name.substring(parent.getName().length() + 1)
                : name;
    }
    
    public abstract boolean isProgramModule();
    
    public abstract void filterNonProgramReferences();
    
    public abstract boolean isLeaf();
    public abstract int countLeafsUnder();
    
    public int getDependencyStrength(Module module) {
        Integer cachedStrength = cachedDependencyStrengths.get(module);
        if (cachedStrength != null) {
            return cachedStrength.intValue();
        } else {
            int strength = getUncachedDependencyStrength(module);
            cachedDependencyStrengths.put(module, strength);
            return strength;
        }
    }

    private int getUncachedDependencyStrength(Module module) {
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
    
    public List<Module> getSelfAndAncestors() {
        if (cachedSelfAndAncestors == null) {
            List<Module> result = new ArrayList<Module>();
            result.add(this);
            addAncestors(result);
            cachedSelfAndAncestors = unmodifiableList(result);
        }
        return cachedSelfAndAncestors;
    }
    
    private void addAncestors(List<Module> result) {
        for (Module child : getChildren()) {
            result.add(child);
            child.addAncestors(result);
        }
    }

    int getDirectDependencyStrength(Module module) {
        Integer strength = strengths.get(module);
        return (strength != null) ? strength.intValue() : 0;
    }

    public void setDependencyStrength(Module module, int strength) {
        Check.notNull(module, "module");
        Check.notNegative(strength, "strength");
        
        strengths.put(module, strength);
    }

    public void addDependency(Module module, DependencyType type) {
        Check.notNull(module, "module");
        Check.notNull(type, "type");
        
        Integer strength = strengths.get(module);
        strengths.put(module, (strength != null) ? (strength + 1) : 1);
        
        // TODO: We get dependencies to void, java.lang.Class, java.lang.String as well, maybe we should get rid of them
        List<Dependency> list = dependencies.get(module);
        if (list == null) {
            list = new ArrayList<Dependency>();
            dependencies.put(module, list);
        }
        list.add(new Dependency(module, type));
    }
    
    public List<Dependency> getDependencies(Module to) {
        return dependencies.get(to);
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public void move(MoveDirection direction) {
        if (parent != null)
            parent.move(this, direction);
    }
    
    int outgoingDependencies(Collection<Module> modules) {
        int sum = 0;
        for (Module m : modules)
            sum += getDependencyStrength(m);
        return sum;
    }
}
