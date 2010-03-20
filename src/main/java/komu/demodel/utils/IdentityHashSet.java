package komu.demodel.utils;

import java.util.AbstractSet;
import java.util.IdentityHashMap;
import java.util.Iterator;

public final class IdentityHashSet<T> extends AbstractSet<T> {

    private final IdentityHashMap<T, Object> map = new IdentityHashMap<T, Object>();
    
    @Override
    public Iterator<T> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }
    
    @Override
    public boolean remove(Object o) {
        return map.remove(o) != null;
    }
    
    @Override
    public boolean add(T o) {
        return map.put(o, new Object()) == null;
    }
}
