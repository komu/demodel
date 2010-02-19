/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
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
        return programModule;
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
        return getDirectDependencyStrength(module);
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
}
