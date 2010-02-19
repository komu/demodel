/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui;

import komu.demodel.domain.DependencyModel;
import komu.demodel.domain.Module;

/**
 * Represents the state of the view, but not of the underlying model.
 * As an example, the order of the modules is defined by the real model,
 * but which modules are currently expanded or selected are not.
 * Also does translation from the visible indices to underlying indices.
 */
final class DependencyMatrixViewModel {

    public static final int NO_SELECTION = -1;
    
    private final DependencyModel model;
    private Module selectedModule;
    
    DependencyMatrixViewModel(DependencyModel model) {
        if (model == null) throw new NullPointerException("null model");
        
        this.model = model;
    }

    public int getVisibleModuleCount() {
        return model.getModuleCount();
    }

    /**
     * Returns the index of selected module, or {@link #NO_SELECTION} if no module is selected.
     */
    public int getIndexOfSelectedModule() {
        return (selectedModule != null) ? model.indexOfModule(selectedModule) : NO_SELECTION;
    }

    public int dependencyStrength(int from, int to) {
        return model.dependencyStrength(from, to);
    }

    public Iterable<Module> getVisibleModules() {
        return model.getModules();
    }

    public Module getModuleAt(int index) {
        return model.getModuleAt(index);
    }

    public void setSelectedModule(Module module) {
        selectedModule = module;
    }

    public void sortModules() {
        model.sortModules();
    }

    public void moveSelectedModule(MoveDirection direction) {
        if (selectedModule != null) {
            switch (direction) {
            case UP: 
                model.moveUp(selectedModule);
                break;
            case DOWN:
                model.moveDown(selectedModule);
                break;
            default:
                throw new IllegalArgumentException("unknown MoveDirection: " + direction);    
            }
        }
    }

    public void moveSelectedModuleDown() {
        if (selectedModule != null)
            model.moveUp(selectedModule);
    }
}
