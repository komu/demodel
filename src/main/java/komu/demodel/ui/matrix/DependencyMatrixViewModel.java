/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.ui.matrix;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import komu.demodel.domain.Dependency;
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
    private Module selectedRow;
    private Module selectedColumn;
    private final IdentityHashSet<Module> openedModules = new IdentityHashSet<Module>();
    private final ChangeListenerList listeners = new ChangeListenerList();
    private List<Module> cachedVisibleModules;
    
    DependencyMatrixViewModel(Module root) {
        if (root == null) throw new NullPointerException("null root");
        
        this.root = root;
        this.selectedRow = root;
        openedModules.add(root);
    }
    
    public void flushCaches() {
        cachedVisibleModules = null;
        root.flushCaches();
    }

    public void addListener(ChangeListener listener) {
        listeners.add(listener);
    }
    
    private void fireStateChanged() {
        listeners.stateChanged(new ChangeEvent(this));
    }

    public Module getSelectedRow() {
        return selectedRow;
    }

    public Module getSelectedColumn() {
        return selectedColumn;
    }
    
    public int getVisibleModuleCount() {
        return getVisibleModules().size();
    }

    public int dependencyStrength(int from, int to) {
        List<Module> modules = getVisibleModules();

        return modules.get(from).getDependencyStrength(modules.get(to));
    }
    
    public String getDependencyDetailsOfSelectedCell() {
        if (selectedRow == null || selectedColumn == null) return null;

        StringBuilder sb = new StringBuilder();
        sb.append("Dependencies from module\n");
        sb.append(selectedColumn.getName());
        sb.append("\nto module\n    ");
        sb.append(selectedRow.getName());
        sb.append("\n\n");
        
        listDependencies(selectedColumn, selectedRow, sb);
        return sb.toString();
    }

    private void listDependencies(Module fromModule, Module toModule, StringBuilder b) {
        if (!fromModule.isLeaf()) {
            for (Module fromChild : fromModule.getChildren()) {
                listDependencies(fromChild, toModule, b);
            }
        }
        if (!toModule.isLeaf()) {
            for (Module toChild : toModule.getChildren()) {
                listDependencies(fromModule, toChild, b);
            }
        }
        if (fromModule.getDependencies(toModule) != null) {
            for (Dependency d : fromModule.getDependencies(toModule)) {
                b.append("Depencency from=").append(fromModule.getName()).append(" ").append(d).append("\n");
            }
        }
    }

    public List<Module> getVisibleModules() {
        if (cachedVisibleModules == null) {
            List<Module> modules = new ArrayList<Module>();
        
            addVisibleModules(modules, singletonList(root));
        
            cachedVisibleModules = unmodifiableList(modules);
        }
        return cachedVisibleModules;
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

    public void setSelectedRow(Module module) {
        if (module != selectedRow) {
            selectedRow = module;
            fireStateChanged();
        }
    }
    
    public void setSelectedColumn(Module module) {
        if (module != selectedColumn) {
            selectedColumn = module;
            fireStateChanged();
        }
    }
    
    public void moveSelection(MoveDirection direction) {
        if (selectedRow == null) return;
        
        List<Module> visibleModules = getVisibleModules();
        int index = visibleModules.indexOf(selectedRow);
        assert index != -1;
        
        int newIndex = index + direction.delta;
        if (newIndex >= 0 && newIndex < visibleModules.size()) {
            selectedRow = visibleModules.get(newIndex);
            fireStateChanged();
        }
    }

    public void sortModules() {
        if (selectedRow != null) {
            selectedRow.sortChildren();
            flushCaches();
            fireStateChanged();
        }
    }

    public void moveSelectedModule(MoveDirection direction) {
        if (selectedRow != null) {
            selectedRow.move(direction);
            flushCaches();
            fireStateChanged();
        }
    }

    public void openSelectedModule() {
        if (selectedRow != null && !selectedRow.isLeaf() && openedModules.add(selectedRow)) {
            cachedVisibleModules = null;
            fireStateChanged();
        }
    }

    public void closeSelectedModule() {
        if (selectedRow != null && !selectedRow.isLeaf() && openedModules.remove(selectedRow)) {
            cachedVisibleModules = null;
            fireStateChanged();
        }
    }
    
    public boolean isOpened(Module module) {
        return openedModules.contains(module);
    }

    public List<Module> getAllModules() {
        return root.getSelfAndAncestors();
    }
    
    public boolean hasSelectedRow() {
        return selectedRow != null;
    }
    
    public boolean hasSelectedCell() {
        return selectedRow != null && selectedColumn != null;
    }
    
}
