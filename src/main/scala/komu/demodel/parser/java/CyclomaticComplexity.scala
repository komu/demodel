package komu.demodel.parser.java

import org.objectweb.asm.tree._
import org.objectweb.asm.tree.analysis._

object CyclomaticComplexity {

  /**
   * Calculates the cyclomatic complexity of given method.
   */
  def cyclomaticComplexity(owner: String, mn: MethodNode): Int = {
    val a = new Analyzer(new BasicInterpreter) {
      override def newFrame(nLocals: Int, nStack: Int) = 
        new Frame(nLocals, nStack) with Successors
      
      override def newFrame(src: Frame) = 
        new Frame(src) with Successors
      
      override def newControlFlowEdge(src: Int, dst: Int) =
        nodeAt(src).addSuccessor(nodeAt(dst))
      
      private def nodeAt(index: Int) = 
        getFrames()(index).asInstanceOf[Successors]
    }
    
    a.analyze(owner, mn)
    
    val frames = a.getFrames.filter(f => f != null)
    var nodes = frames.length
    val edges = frames.map(f => f.asInstanceOf[Successors].successorCount).sum
    
    edges - nodes + 2;
  }
}

trait Successors {
  private var successors = Set[Object]()
  
  def addSuccessor(node: Object) {
    successors = successors + node
  }
  
  def successorCount = successors.size
}
