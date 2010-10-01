package komu.demodel.parser.java

import org.objectweb.asm.tree._
import scala.collection.JavaConversions._
import komu.demodel.domain.TypeName

class RichClassNode(node: ClassNode) {

  def name = TypeName.forInternalClassName(node.name)
  def superName = if (node.superName != null) Some(TypeName.forInternalClassName(node.superName)) else None
  def typeType = AccessUtils.getTypeFromAccessFlags(node.access)
  def interfaceNames = node.interfaces.map(i => TypeName.forInternalClassName(i.asInstanceOf[String]))
  
  def methods = node.methods.map(_.asInstanceOf[MethodNode])
  def fields = node.fields.map(_.asInstanceOf[FieldNode])
}

class RichMethodNode(node: MethodNode) {
  
  def signature = if (node.signature != null) node.signature else node.desc
  
  def visibleAnnotations: Iterable[AnnotationNode] = 
    asAnnotationList(node.visibleAnnotations)
    
  def visibleParameterAnnotations: Iterable[Iterable[AnnotationNode]] = 
    nullSafe(node.visibleParameterAnnotations).map(asAnnotationList)
    
  def localVariables: Iterable[LocalVariableNode] = 
    nullSafe(node.localVariables).map(_.asInstanceOf[LocalVariableNode])
    
  def instructions: Iterable[AbstractInsnNode] =
    node.instructions.iterator.map(_.asInstanceOf[AbstractInsnNode]).toList

  private def asAnnotationList(xs: java.util.List[_]): Iterable[AnnotationNode] =
    nullSafe(xs).map(_.asInstanceOf[AnnotationNode])
    
  private def nullSafe[T](xs: java.util.List[T]): Iterable[T] =
    if (xs != null) xs else Nil
    
  private def nullSafe[T](xs: Array[T]): Iterable[T] =
    if (xs != null) xs else Nil
}

object RichNodeConversions {
  implicit def classToRichClass(node: ClassNode) = new RichClassNode(node)
  implicit def methodToRichMethod(node: MethodNode) = new RichMethodNode(node)
}
