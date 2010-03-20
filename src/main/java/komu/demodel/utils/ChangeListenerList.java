package komu.demodel.utils;

import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public final class ChangeListenerList {

    private final CopyOnWriteArrayList<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();
    
    public void add(ChangeListener listener) {
        if (listener == null) throw new NullPointerException("null listener");
        
        listeners.add(listener);
    }
    
    public void stateChanged(ChangeEvent event) {
        for (ChangeListener listener : listeners)
            listener.stateChanged(event);
    }
}
