/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import static java.lang.Math.abs;
import komu.demodel.utils.Check;

/**
 * Various <a href="http://en.wikipedia.org/wiki/Software_package_metrics">package metrics</a>.
 */
public final class PackageMetrics {

    private int numberOfTypes;
    private int numberOfAbstractTypes;
    private int afferentCouplings;
    private int efferentCouplings;
    
    /**
     * Ratio of efferent coupling to total coupling. (0=completely stable, 1=completely instable)
     */
    public double getInstability() {
        double total = efferentCouplings + afferentCouplings;
        
        return (total != 0) ? (efferentCouplings / total) : 0; // TODO: what to return if undefined?
    }

    /**
     * Number of abstract classes and interfaces in this package.
     */
    public int getNumberOfAbstractTypes() {
        return numberOfAbstractTypes;
    }
    
    /**
     * Number of classes and interfaces in this package.
     */
    public int getNumberOfTypes() {
        return numberOfTypes;
    }
    
    public void setNumberOfTypes(int numberOfTypes) {
        Check.notNegative(numberOfTypes, "numberOfTypes");
        
        this.numberOfTypes = numberOfTypes;
    }
    
    public void setNumberOfAbstractTypes(int numberOfAbstractTypes) {
        Check.notNegative(numberOfAbstractTypes, "numberOfAbstractTypes");

        this.numberOfAbstractTypes = numberOfAbstractTypes;
    }
    
    /**
     * The number of packages that depend upon classes within this package.
     */
    public int getAfferentCouplings() {
        return afferentCouplings;
    }
    
    public void setAfferentCouplings(int afferentCouplings) {
        Check.notNegative(afferentCouplings, "afferentCouplings");

        this.afferentCouplings = afferentCouplings;
    }
    
    /**
     * The number of packages that this package depends on.
     */
    public int getEfferentCouplings() {
        return efferentCouplings;
    }

    public void setEfferentCouplings(int efferentCouplings) {
        Check.notNegative(efferentCouplings, "efferentCouplings");
        
        this.efferentCouplings = efferentCouplings;
    }

    /**
     * The ratio of abstract classes (and interfaces) to total number of classes.
     * (0=concrete, 1=totally abstract)
     */
    public double getAbstractness() {
        assert numberOfAbstractTypes <= numberOfTypes;
        
        if (numberOfTypes == 0) return 0; // TODO: what's the correct value for undefined?
        
        return ((double) numberOfAbstractTypes) / numberOfTypes;
    }
    
    /**
     * The perpendicular distance of a package from the idealized line A + I = 1. This metric is an
     * indicator of the package's balance between abstractness and stability. A package squarely on
     * the main sequence is optimally balanced with respect to its abstractness and stability.
     * Ideal packages are either completely abstract and stable (x=0, y=1) or completely concrete
     * and instable (x=1, y=0). The range for this metric is 0 to 1, with D=0 indicating a package
     * that is coincident with the main sequence and D=1 indicating a package that is as far from
     * the main sequence as possible.
     */
    public double getDistanceFromMainSequence() {
        return abs(getAbstractness() + getInstability() - 1);
    }
}
