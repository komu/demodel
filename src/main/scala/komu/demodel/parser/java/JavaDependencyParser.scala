/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.parser.java

import AccessUtils.getTypeFromAccessFlags
import SignatureUtils.{ getTypesFromGenericMethodSignature, getTypesFromGenericSignature }

import scala.collection.JavaConversions._
import scala.collection.immutable.{ Map, TreeMap }

import komu.demodel.domain._
import komu.demodel.domain.project._
import komu.demodel.utils.Resource

import org.objectweb.asm._
import org.objectweb.asm.tree._

object JavaDependencyParser {
  
  def parse(inputSources: Iterable[InputSource]) = {
    val parser = new JavaDependencyParser
    for (source <- inputSources)
      parser.parse(source)
    parser.rootModule
  }
}

class JavaDependencyParser {

  private val _rootModule = new PackageModule("<root>", None)
  private var modules = TreeMap[String, Module]()
  
  def parse(inputSource: InputSource) {
    println("parsing classes...")
    val classes = inputSource.mapResources(parseClassNode)
    println("processing classes...")
    classes.foreach(processClass)
  }

  def rootModule = {
    _rootModule.filterNonProgramReferences();
    _rootModule.normalizeTree();
    _rootModule.flushCaches();
    _rootModule
  }
  
  private def parseClassNode(resource: Resource): ClassNode = {
    val in = resource.open()
    try {
      var classNode = new ClassNode
      new ClassReader(in).accept(classNode, 0)
      classNode
    } finally {
      in.close()
    }
  }
  
  def processClass(classNode: ClassNode) {
    val currentModule = getVisitedModuleForType(classNode.name, getTypeFromAccessFlags(classNode.access))
    if (classNode.superName != null)
      addDependencyToType(currentModule, classNode.superName, DependencyType.INHERITANCE)
      
    for (interfaceName <- classNode.interfaces)
      addDependencyToType(currentModule, interfaceName.asInstanceOf[String], DependencyType.INHERITANCE)
      
    for (method <- classNode.methods.map(_.asInstanceOf[MethodNode]))
      processMethod(currentModule, method)
    
    for (field <- classNode.fields.map(_.asInstanceOf[FieldNode])) {
      val descriptor = if (field.signature != null) field.signature else field.desc
      for (ty <- getTypesFromGenericSignature(descriptor))
        addDependency(currentModule, ty, DependencyType.FIELD_REF);
    
      visitAnnotations(field.visibleAnnotations)
    }
    
    /*
    for (innerClass <- classNode.innerClasses.map(_.asInstanceOf[InnerClassNode])) {
      println("inner class: " + innerClass.innerName)
    }
    
    for (method <- classNode.methods.map(_.asInstanceOf[MethodNode])) {
      val complexity = CyclomaticComplexity.cyclomaticComplexity(classNode.name, method)
      if (complexity >= 10)
        println("complexity of " + classNode.name + "." + method.name + " is " + complexity)
    }
    */
  }
  
  def visitAnnotations(annotations: java.util.List[_]) {
    // TODO
  }
  
  def processMethod(currentModule: ClassModule, method: MethodNode) {
    val descriptor = if (method.signature != null) method.signature else method.desc
    for (ty <- getTypesFromGenericMethodSignature(descriptor))
      addDependency(currentModule, ty, DependencyType.REF)

    visitAnnotations(method.visibleAnnotations)
    if (method.visibleParameterAnnotations != null)
      for (p <- method.visibleParameterAnnotations)
        visitAnnotations(p)
    
    if (method.localVariables != null)
      for (v <- method.localVariables.map(_.asInstanceOf[LocalVariableNode])) {
        var descriptor = if (v.signature != null) v.signature else v.desc
        for (ty <- getTypesFromGenericSignature(descriptor))
          addDependency(currentModule, ty, DependencyType.REF)
      }
    
    for (instr <- method.instructions.iterator) {
      instr match {
      case ti: TypeInsnNode =>
        addDependencyToType(currentModule, ti.desc, DependencyType.REF)
      case fi: FieldInsnNode =>
        addDependencyToType(currentModule, fi.owner, DependencyType.REF)
        addDependency(currentModule, Type.getType(fi.desc), DependencyType.REF)
      case mi: MethodInsnNode =>
        addDependencyToType(currentModule, mi.owner, DependencyType.REF)
        addDependency(currentModule, Type.getReturnType(mi.desc), DependencyType.REF)
        for (ty <- Type.getArgumentTypes(mi.desc))
          addDependency(currentModule, ty, DependencyType.REF)
      case mn: MultiANewArrayInsnNode =>
        addDependency(currentModule, Type.getType(mn.desc), DependencyType.REF)
      case _ =>
        ()
      }
    }
  }
    
  private def getVisitedModuleForType(name: String, typetype: TypeType) = {
    val module = getModuleForType(name)
    module.markAsProgramModule(typetype)
    module
  }
    
  private def getModuleForType(className: String) =
    try {
      getModuleByName(moduleNameForType(className), ModuleType.TYPE).asInstanceOf[ClassModule]
    } catch {
      case e => throw new Exception("failed to resolve module for type '" + className + "': " + e, e)
    }
  

  private def getPackageModuleByName(name: String): PackageModule =
    getModuleByName(name, ModuleType.PACKAGE) match {
      case m: PackageModule => m
      case m: ClassModule   => m.parent.get
    }
    
  private def getModuleByName(name: String, moduleType: ModuleType) =
    modules.getOrElse(name, { 
      val module = moduleType.createModule(name, getParentModule(name))
      modules = modules + (name -> module)
      module
    })

  private def getParentModule(name: String) = {
    name.lastIndexOf('.') match {
      case -1    => _rootModule
      case index => getPackageModuleByName(name.substring(0, index))
    }
  }
  
  private def stripEverythingAfter(s: String, ch: Char) =
    s.lastIndexOf(ch) match {
      case -1 => s
      case i  => s.substring(0, i)
    }
    
  private def moduleNameForType(name: String) =
    //Type.getType("L" + name + ";").getClassName
    name.replace('/', '.')
    
  private def addDependencyToType(currentModule: ClassModule, typeName: String, dependencyType: DependencyType) {
    //if (typeName.last.isDigit) return; // FIXME: hack to ignore classes of form "foo.Bar.1"
	  
    val target = getModuleForType(typeName)
    if (currentModule != target)
      currentModule.addDependency(target, dependencyType)
  }

  private def addDependency(currentModule: ClassModule, toType: Type, dependencyType: DependencyType) {
    addDependencyToType(currentModule, toType.getClassName, dependencyType)
  }
}
