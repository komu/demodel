package komu.demodel.domain

case class TypeName private (name: String) extends Ordered[TypeName] {
  assert(!name.contains("/"), "invalid type: " + name)
  
  def compare(tn: TypeName) = name.compareTo(tn.name)
  
  def parentName =
    name.lastIndexOf('.') match {
      case -1    => None
      case index => Some(TypeName(name.substring(0, index)))
    }
}

object TypeName {

  val BYTE = TypeName("byte")
  val CHAR = TypeName("char")
  val DOUBLE = TypeName("double")
  val FLOAT = TypeName("float")
  val INT = TypeName("int")
  val LONG = TypeName("long")
  val SHORT = TypeName("short")
  val BOOLEAN = TypeName("boolean")

  import org.objectweb.asm.Type

  def forInternalClassName(name: String) = {
    assert(!name.contains("<"), "invalid internal class name: " + name);
    TypeName(name.replace('.', '$').replace('/', '.'))
  }
  
  def forDescriptor(desc: String) =
    forDesc("L" + desc + ";")
    
  def forType(t: Type) =
    TypeName(t.getClassName)
    
  def forReturnType(t: String) =
    forType(Type.getReturnType(t))
    
  def forDesc(t: String) =
    forType(Type.getType(t))
}
