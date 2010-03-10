/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import komu.demodel.domain.Module;
import komu.demodel.domain.MoveDirection;
import komu.demodel.utils.ChangeListenerList;
import komu.demodel.utils.IdentityHashSet;

/**
 * Represents the state of the view, but not of the underlying model.
 * As an example, the order of the modules is defined by the real model,
 * but which modules are currently expanded or selected are not.
 * Also does translation from the visible indices to underlying indices.
 */
final class DependencyMatrixViewModel {

    public static final int NO_SELECTION = -1;
    
    private final Module root;
    private Module selectedModule;
    private final IdentityHashSet<Module> openedModules = new IdentityHashSet<Module>();
    private final ChangeListenerList listeners = new ChangeListenerList();
    
    DependencyMatrixViewModel(Module root) {
        if (root == null) throw new NullPointerException("null root");
        
        this.root = root;
        this.selectedModule = root;
        openedModules.add(root);
    }

    public void addListener(ChangeListener listener) {
        listeners.add(listener);
    }
    
    private void fireStateChanged() {
        listeners.stateChanged(new ChangeEvent(this));    
    }

    public Module getSelectedModule() {
        return selectedModule;
    }
    
    public int getVisibleModuleCount() {
        return getVisibleModules().size();
    }

    public int dependencyStrength(int from, int to) {
        List<Module> modules = getVisibleModules();

        return modules.get(from).getDependencyStrength(modules.get(to));
    }

    public List<Module> getVisibleModules() {
        List<Module> modules = new ArrayList<Module>();
        
        addVisibleModules(modules, singletonList(root));
        
        return modules;
    }

    private void addVisibleModules(List<Module> result, Iterable<Module> modules) {
        for (Module module : modules) {
            result.add(module);
            if (openedModules.contains(module))
                addVisibleModules(result, module.getChildren());
        }
    }

    public Module getModuleAt(int index) {
        return getVisibleModules().get(index);
    }

    public void setSelectedModule(Module module) {
        if (module != selectedModule) {
            selectedModule = module;
            fireStateChanged();
        }
    }
    
    public void moveSelection(MoveDirection direction) {
        if (selectedModule == null) return;
        
        List<Module> visibleModules = getVisibleModules();
        int index = visibleModules.indexOf(selectedModule);
        assert index != -1;
        
        int newIndex = index + direction.delta;
        if (newIndex >= 0 && newIndex < visibleModules.size()) {
            selectedModule = visibleModules.get(newIndex);
            fireStateChanged();
        }
    }

    public void sortModules() {
        if (selectedModule != null) {
            selectedModule.sortChildren();
            fireStateChanged();
        }
    }

    public void moveSelectedModule(MoveDirection direction) {
        if (selectedModule != null) {
            selectedModule.move(direction);
            fireStateChanged();
        }
    }

    public void openSelectedModule() {
        if (selectedModule != null && openedModules.add(selectedModule))
            fireStateChanged();
    }

    public void closeSelectedModule() {
        if (selectedModule != null && openedModules.remove(selectedModule))
            fireStateChanged();
    }
    
    public boolean isOpened(Module module) {
        return openedModules.contains(module);
    }

    public List<Module> getAllModules() {
        List<Module> result = new ArrayList<Module>();
        addModulesRootedAt(result, root);
        return result;
    }
    
    private void addModulesRootedAt(List<Module> result, Module module) {
        result.add(module);
        for (Module child : module.getChildren())
            addModulesRootedAt(result, child);
    }

	public boolean hasSelection() {
		return selectedModule != null;
	}
}
