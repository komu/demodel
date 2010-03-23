/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class PackageModule extends Module {

    private final List<Module> children = new ArrayList<Module>();
    private PackageMetrics metrics;
    
    public PackageModule(String name, PackageModule parent) {
        super(name, parent);
    }
    
    public PackageMetrics getMetrics() {
        if (metrics == null)
            metrics = calculateMetrics();
        return metrics;
    }
    
    private PackageMetrics calculateMetrics() {
        PackageMetrics metrics = new PackageMetrics();

        metrics.setNumberOfTypes(countTypes());
        metrics.setNumberOfAbstractTypes(countAbstractTypes());
        metrics.setAfferentCouplings(countAfferentCouplings());
        metrics.setEfferentCouplings(countEfferentCouplings());
        
        return metrics;
    }
    
    private int countEfferentCouplings() {
        int total = 0;
        for (PackageModule module : getRoot().getPackagesUnder())
            if (module != this && this.hasDependenciesToClassesIn(module))
                total++;
        return total;
    }
    
    private boolean hasDependenciesToClassesIn(PackageModule module) {
        for (ClassModule myClass : getClasses())
            for (ClassModule theirClass : module.getClasses())
                if (myClass.getDependencyStrength(theirClass) > 0)
                    return true;
        return false;
    }
    
    private int countAfferentCouplings() {
        int total = 0;
        for (PackageModule module : getRoot().getPackagesUnder())
            if (module != this && module.hasDependenciesToClassesIn(this))
                total++;
        return total;
    }
    
    private List<PackageModule> getPackagesUnder() {
        List<PackageModule> result = new ArrayList<PackageModule>();
        for (Module module : getSelfAndAncestors())
            if (module instanceof PackageModule)
                result.add((PackageModule) module);
        return result;
    }

    private List<ClassModule> getClasses() {
        List<ClassModule> classes = new ArrayList<ClassModule>();
        for (Module child : children)
            if (child instanceof ClassModule)
                classes.add((ClassModule) child);
        return classes;
    }

    private int countTypes() {
        return getClasses().size();
    }
    
    private int countAbstractTypes() {
        int total = 0;
        for (ClassModule cm : getClasses())
            if (cm.isAbstract())
                total++;
        return total;
    }

    @Override
    PackageModule getRoot() {
        return (parent == null) ? this : parent.getRoot();
    }

    @Override
    public ModuleType getType() {
        return ModuleType.PACKAGE;
    }
    
    @Override
    public boolean isLeaf() {
        return children.isEmpty();
    }
    
    @Override
    public boolean isProgramModule() {
        for (Module child : getChildren())
            if (child.isProgramModule())
                return true;
        
        return false;
    }
    
    @Override
    public int countLeafsUnder() {
        if (isLeaf()) return 1;
        
        int count = 0;
        for (Module child : children)
            count += child.countLeafsUnder();
        
        return count;
    }
    
    /**
     * Normalize tree so that all packages contain only packages or no packages
     * at all. If a package has both, then a new pseudo-package is created under
     * that package and all non-packages are moved under that.
     */
    public void normalizeTree() {
        List<PackageModule> packages = new ArrayList<PackageModule>();
        List<Module> nonPackages = new ArrayList<Module>();

        for (Module child : getChildren()) {
            if (child instanceof PackageModule) {
                PackageModule pm = (PackageModule) child;
                pm.normalizeTree();
                packages.add(pm);
            } else {
                nonPackages.add(child);
            }
        }

        if (!packages.isEmpty() && !nonPackages.isEmpty()) {
            PackageModule pseudoPackage = new PackageModule("<classes>", this);
            pseudoPackage.children.addAll(nonPackages);
            children.removeAll(nonPackages);
        }
    }
    
    @Override
    public List<Module> getChildren() {
        return unmodifiableList(children);
    }
    
    void addChild(Module module) {
        assert module != null;
        assert module.getParent() == this;
        children.add(module);
    }
    
    void move(Module module, MoveDirection direction) {
        int index = children.indexOf(module);
        int newIndex = index + direction.delta;
        if (index != -1 && isValidChildIndex(newIndex)) {
            children.remove(module);
            children.add(newIndex, module);
        }
    }
    
    @Override
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
    
    @Override
    public void filterNonProgramReferences() {
        for (Iterator<Module> it = children.iterator(); it.hasNext(); ) {
            Module child = it.next();
            if (child.isProgramModule())
                child.filterNonProgramReferences();
            else
                it.remove();
        }
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
    
    private boolean isValidChildIndex(int index) {
        return index >= 0 && index < children.size();
    }
}