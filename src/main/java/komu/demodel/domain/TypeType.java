/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

public enum TypeType {
    CONCRETE_CLASS, ABSTRACT_CLASS, INTERFACE;
    
    public boolean isAbstract() {
        return this != CONCRETE_CLASS;
    }
}
