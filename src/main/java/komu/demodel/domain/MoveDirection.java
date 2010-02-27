package komu.demodel.domain;

public enum MoveDirection {
    UP(-1), DOWN(1);
    
    public final int delta;
   
    private MoveDirection(int delta) {
        this.delta = delta;
    }
}
