/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.parser.java

import SignatureUtils.{ getTypesFromGenericMethodSignature, getTypesFromGenericSignature }

import scala.collection.JavaConversions._
import scala.collection.immutable.{ Map, TreeMap }

import komu.demodel.domain._
import komu.demodel.domain.project._
import komu.demodel.utils.Resource

import org.objectweb.asm._
import org.objectweb.asm.tree._
import RichNodeConversions._

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
  private var modules = TreeMap[TypeName, Module]()
  
  def parse(inputSource: InputSource) {
    println("parsing classes...")
    val classes = inputSource.mapResources(parseClassNode)
    println("processing classes...")
    classes.foreach(c => processClass(c))
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
  
  def processClass(classNode: RichClassNode) {
    val classModule = getVisitedModuleForType(classNode.name, classNode.typeType)
    
    for (superName <- classNode.superName)
      addDependency(classModule, superName, DependencyType.INHERITANCE)
      
    for (interfaceName <- classNode.interfaceNames)
      addDependency(classModule, interfaceName, DependencyType.INHERITANCE)
      
    for (method <- classNode.methods)
      processMethod(classModule, method)
    
    for (field <- classNode.fields) {
      for (ty <- getTypesFromGenericSignature(field.signature, field.desc))
        addDependency(classModule, ty, DependencyType.FIELD_REF);
    
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
  
  def visitAnnotations(annotations: Iterable[_]) {
    // TODO
  }
  
  def processMethod(currentModule: ClassModule, method: RichMethodNode) {
    for (ty <- getTypesFromGenericMethodSignature(method.signature))
      addDependency(currentModule, ty, DependencyType.REF)

    visitAnnotations(method.visibleAnnotations)
    for (p <- method.visibleParameterAnnotations)
      visitAnnotations(p)
    
    for (v <- method.localVariables)
      for (ty <- getTypesFromGenericSignature(v.signature, v.desc))
        addDependency(currentModule, ty, DependencyType.REF)
    
    for (instr <- method.instructions) {
      instr match {
      case ti: TypeInsnNode =>
        addDependency(currentModule, TypeName.forDescriptor(ti.desc), DependencyType.REF)
      case fi: FieldInsnNode =>
        addDependency(currentModule, TypeName.forInternalClassName(fi.owner), DependencyType.REF)
        addDependency(currentModule, TypeName.forDesc(fi.desc), DependencyType.REF)
      case mi: MethodInsnNode =>
        addDependency(currentModule, TypeName.forInternalClassName(mi.owner), DependencyType.REF)
        addDependency(currentModule, TypeName.forReturnType(mi.desc), DependencyType.REF)
        for (ty <- Type.getArgumentTypes(mi.desc))
          addDependency(currentModule, TypeName.forType(ty), DependencyType.REF)
      case mn: MultiANewArrayInsnNode =>
        addDependency(currentModule, TypeName.forDesc(mn.desc), DependencyType.REF)
      case _ =>
        ()
      }
    }
  }
    
  private def getVisitedModuleForType(name: TypeName, typetype: TypeType) = {
    val module = getModuleForType(name)
    module.markAsProgramModule(typetype)
    module
  }
    
  private def getModuleForType(typeName: TypeName) =
    try {
      getModuleByName(typeName, ModuleType.TYPE).asInstanceOf[ClassModule]
    } catch {
      case e => throw new Exception("failed to resolve module for type '" + typeName + "': " + e, e)
    }
  

  private def getPackageModuleByName(name: TypeName): PackageModule =
    getModuleByName(name, ModuleType.PACKAGE) match {
      case m: PackageModule => m
      case m: ClassModule   => m.parent.get
    }
    
  private def getModuleByName(name: TypeName, moduleType: ModuleType) =
    modules.getOrElse(name, { 
      val module = moduleType.createModule(name.name, getParentModule(name))
      modules = modules + (name -> module)
      module
    })

  private def getParentModule(name: TypeName) =
    name.parentName match {
      case None    => _rootModule
      case Some(n) => getPackageModuleByName(n)
    }
  
  private def stripEverythingAfter(s: String, ch: Char) =
    s.lastIndexOf(ch) match {
      case -1 => s
      case i  => s.substring(0, i)
    }
    
  private def addDependency(currentModule: ClassModule, typeName: TypeName, dependencyType: DependencyType) {
    val target = getModuleForType(typeName)
    if (currentModule != target)
      currentModule.addDependency(target, dependencyType)
  }
  
  private def addDependency2(currentModule: ClassModule, typeName: Type, dependencyType: DependencyType) {
    addDependency(currentModule, TypeName.forType(typeName), dependencyType)
  }
}
