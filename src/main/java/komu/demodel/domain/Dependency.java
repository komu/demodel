package komu.demodel.domain;

import komu.demodel.utils.Check;

public final class Dependency {

    private final Module to;
    private final DependencyType type;
    
    public Dependency(Module to, DependencyType type) {
        Check.notNull(to, "to");
        Check.notNull(type, "type");

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

