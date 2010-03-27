/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain

import java.lang.Math.abs

/**
 * Various <a href="http://en.wikipedia.org/wiki/Software_package_metrics">package metrics</a>.
 */
final class PackageMetrics {

  /** Number of classes and interfaces in this package. */
  var numberOfTypes = 0;
  var numberOfAbstractTypes = 0
  
  /** The number of packages that depend upon classes within this package. */
  var afferentCouplings = 0

  /** The number of packages that this package depends on. */
  var efferentCouplings = 0
    
  /**
   * Ratio of efferent coupling to total coupling. (0=completely stable, 1=completely instable)
   */
  def instability = {
    var total: Double = efferentCouplings + afferentCouplings;
    if (total != 0)
      efferentCouplings / total
    else 
      0 // TODO: what to return if undefined?
  }

  /**
   * The ratio of abstract classes (and interfaces) to total number of classes.
   * (0=concrete, 1=totally abstract)
   */
  def abstractness = {
    assert(numberOfAbstractTypes <= numberOfTypes)
    val abstracts: Double = numberOfAbstractTypes
    if (numberOfTypes != 0) 
      abstracts / numberOfTypes
    else
      0 // TODO: what's the correct value for undefined?
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
  def distanceFromMainSequence = abs(abstractness + instability - 1)
}
