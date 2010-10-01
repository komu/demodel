package komu.demodel.parser.java

import org.objectweb.asm.tree._
import org.objectweb.asm.tree.analysis._

object CyclomaticComplexity {

  /**
   * Calculates the cyclomatic complexity of given method.
   */
  def cyclomaticComplexity(owner: String, mn: MethodNode) {
    val a = new Analyzer(new BasicInterpreter) {
      override def newFrame(nLocals: Int, nStack: Int) = 
        new Node(nLocals, nStack)
      
      override def newFrame(src: Frame) = 
        new Node(src)
      
      override def newControlFlowEdge(src: Int, dst: Int) =
        nodeAt(src).addSuccessor(nodeAt(dst))
      
      private def nodeAt(index: Int) = 
        getFrames()(index).asInstanceOf[Node]
    }
    
    a.analyze(owner, mn);
    
    val frames = a.getFrames.filter(f => f != null)
    var nodes = frames.length
    val edges = frames.map(f => f.asInstanceOf[Node].successorCount).sum
    
    return edges - nodes + 2;
  }
}

class Node(nLocals: Int, nStack: Int) extends Frame(nLocals, nStack) {
  
  private var successors = Set[Node]()
  
  def this(src: Frame) {
    this(0, 0);
    init(src)
  }
  
  def addSuccessor(node: Node) {
    successors = successors + node
  }
  
  def successorCount = successors.size
}
