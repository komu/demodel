/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

public enum MoveDirection {
    UP(-1), DOWN(1);
    
    public final int delta;
   
    private MoveDirection(int delta) {
        this.delta = delta;
    }
}
