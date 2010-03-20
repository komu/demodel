package komu.demodel.domain;

public final class Dependency {

    //TODO: it would be nice to store more info
    private final Module to;
    private final DependencyType type;
    
    public Dependency(Module to, DependencyType type) {
        if (to == null) throw new NullPointerException("null to");
        if (type == null) throw new NullPointerException("null type");

        this.to = to;
        this.type = type;
    }
    
    public Module getTo() {
        return to;
    }

    public DependencyType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "to=" + to + ", type=" + type;
    }
}

