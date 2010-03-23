/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils;

/**
 * Static utility methods for parameter checking.
 */
public final class Check {

    private Check() { }
    
    public static void notNull(Object obj, String name) {
        if (obj == null) throw new NullArgumentException(name);
    }
    
    public static void notNegative(int value, String name) {
        if (value < 0) throw new IllegalArgumentException("argument " + name + " was negative: " + value);
    }
}

final class NullArgumentException extends IllegalArgumentException {
    public NullArgumentException(String name) {
        super("argument " + name + " was null");
    }
}
