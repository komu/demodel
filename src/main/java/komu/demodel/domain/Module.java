/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class Module {

    private final String name;
    private final boolean container;
    private final Module parent;
    private boolean programModule = false;
    private final List<Module> children = new ArrayList<Module>();
    private final Map<Module,Integer> strengths = new HashMap<Module,Integer>();
    private final Map<Module,List<Dependency>> dependencies = new HashMap<Module,List<Dependency>>();
    
    private final Map<Module,Integer> cachedDependencyStrengths = new HashMap<Module,Integer>();
    private List<Module> cachedSelfAndAncestors = null;
    
    public Module(String name, boolean container, Module parent) {
        if (name ==  null) throw new NullPointerException("null name");
        
        this.name = name;
        this.container = container;
        this.parent = parent;
        if (parent != null)
            parent.children.add(this);
    }
    
    public void flushCaches() {
        cachedDependencyStrengths.clear();
        cachedSelfAndAncestors = null;
        
        for (Module child : children)
            child.flushCaches();
    }
    
    /**
     * Normalize tree that all nodes have only containers or non-containers. If a node has
     * both, then a new container node is created for non-containers. 
     */
	public void normalizeTree() {
		List<Module> containers = new ArrayList<Module>();
		List<Module> nonContainers = new ArrayList<Module>();
		
		for (Module child : children) {
			child.normalizeTree();
			if (child.container)
				containers.add(child);
			else
				nonContainers.add(child);
		}
		
		if (!containers.isEmpty() && !nonContainers.isEmpty()) {
			Module container = new Module("<classes>", true, this);
			container.children.addAll(nonContainers);
			children.removeAll(nonContainers);
		}
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
    
    public void sortChildren() {
        List<Module> workList = new ArrayList<Module>(children);
        children.clear();
        
        while (!workList.isEmpty()) {
            Module module = pickModuleWithLeastOutgoingDependencies(workList);
            workList.remove(module);
            children.add(module);
        }
        
        reverse(children);
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

    private static Module pickModuleWithLeastOutgoingDependencies(List<Module> modules) {
        assert !modules.isEmpty();

        Module minimumModule = modules.get(0);
        int minimumDependencies = minimumModule.outgoingDependencies(modules);
        for (Module module : modules.subList(1, modules.size())) {
            int dependencies = module.outgoingDependencies(modules);
            if (dependencies < minimumDependencies) {
                minimumDependencies = dependencies;
                minimumModule = module;
            }
        }
        
        return minimumModule;
    }
    
    private int outgoingDependencies(Collection<Module> modules) {
        int sum = 0;
        for (Module m : modules)
            sum += getDependencyStrength(m);
        return sum;
    }
}
