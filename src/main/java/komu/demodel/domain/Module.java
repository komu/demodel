/*
 * Copyright (C) 2006 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Module {

    private String name = "";
    private boolean programModule = false;
    private final List<Module> children = new ArrayList<Module>();
    private final Map<Module,Integer> strengths = new HashMap<Module,Integer>();
    
    public Module(String name) {
        assert name != null;
        
        this.name = name;
    }
 
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isProgramModule() {
        return programModule;
    }
    
    public void setProgramModule(boolean programModule) {
        this.programModule = programModule;
    }
    
    public boolean isLeaf() {
        return children.isEmpty();
    }
    
    public int countLeafsUnder() {
        if (isLeaf()) return 1;
        
        int count = 0;
        for (Module child : children) {
            count += child.countLeafsUnder();
        }
        
        return count;
    }
    
    public List<Module> getChildren() {
        return children;
    }
    
    public int getDependencyStrength(Module module) {
        return getDirectDependencyStrength(module);
    }

    private int getDirectDependencyStrength(Module module) {
        Integer strength = strengths.get(module);
        if (strength != null) {
            return strength.intValue();
        } else {
            return 0;
        }
    }

    public void setDependencyStrength(Module module, int strength) {
        strengths.put(module, strength);
    }

    public void addDependency(Module module, DependencyType type) {
        Integer strength = strengths.get(module);
        if (strength != null) {
            strengths.put(module, strength + 1);
        } else {
            strengths.put(module, 1);
        }
    }
    
    @Override
    public String toString() {
        return name;
    }
}
