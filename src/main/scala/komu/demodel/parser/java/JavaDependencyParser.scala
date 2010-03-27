/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.parser.java

import scala.collection.JavaConversions._

import AccessUtils.getTypeFromAccessFlags
import SignatureUtils.{ getTypesFromGenericMethodSignature, getTypesFromGenericSignature }

import java.util.{ Map, TreeMap }

import komu.demodel.domain._
import komu.demodel.domain.project._
import komu.demodel.utils.Resource
import komu.demodel.utils.ResourceProvider;

import org.objectweb.asm._

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
  private val modules = new TreeMap[String, Module]
  private var currentModule: Module = _
  
  
  def parse(inputSource: InputSource) {
    inputSource.withResources(visitResource)
  }

  def rootModule = {
    _rootModule.filterNonProgramReferences();
    _rootModule.normalizeTree();
    _rootModule.flushCaches();
    _rootModule
  }
    
  private def visitResource(resource: Resource) {
    val in = resource.open();
    try {
      new ClassReader(in).accept(classVisitor, false)
    } finally {
      in.close()
    }
  }
    
  private def getVisitedModuleForType(name: String, typetype: TypeType) = {
    val module = getModuleForType(name)
    module.markAsProgramModule(typetype)
    module
  }
    
  private def getModuleForType(className: String) =
    getModuleByName(moduleNameForType(className), ModuleType.TYPE).asInstanceOf[ClassModule]

  private def getPackageModuleByName(name: String): PackageModule =
    getModuleByName(name, ModuleType.PACKAGE) match {
      case m: PackageModule => m
      case _                => throw new Exception("module: " + name + " is not a package")
    }
    
  private def getModuleByName(name: String, moduleType: ModuleType) = {
    var module = modules.get(name)
    if (module == null) {
      module = moduleType.createModule(name, getParentModule(name))
      modules.put(name, module)
    }
    module
  }

  private def getParentModule(name: String) =
    name.lastIndexOf('.') match {
      case -1    => _rootModule
      case index => getPackageModuleByName(name.substring(0, index))
    }
    
  private def moduleNameForType(name: String) = name.replace('/', '.')
    
  private def addDependencyToType(typeName: String, dependencyType: DependencyType) {
    val target = getModuleForType(typeName)
    if (currentModule != target)
      currentModule.addDependency(target, dependencyType)
  }

  private def addDependency(toType: Type, dependencyType: DependencyType) {
    addDependencyToType(toType.getClassName(), dependencyType)
  }
    
  private object classVisitor extends ClassVisitor {
        
    def visit(version: Int, access: Int, name: String, signature: String, superName: String, interfaces: Array[String]) {
      currentModule = getVisitedModuleForType(name, getTypeFromAccessFlags(access))
            
      if (superName != null)
        addDependencyToType(superName, DependencyType.INHERITANCE)
          
      for (interfaceName <- interfaces)
        addDependencyToType(interfaceName, DependencyType.INHERITANCE)
    }
        
    def visitMethod(access: Int, name: String, signature: String, genericSignature: String, exceptions: Array[String]) = {
      val descriptor = if (genericSignature != null) genericSignature else signature
      for (ty <- getTypesFromGenericMethodSignature(descriptor))
        addDependency(ty, DependencyType.REF)
            
      methodVisitor
    }
        
    def visitEnd() {
      currentModule = null
    }
        
    def visitField(access: Int, name: String, signature: String, genericSignature: String, value: Object) = {
      val descriptor = if (genericSignature != null) genericSignature else signature
      for (ty <- getTypesFromGenericSignature(descriptor))
        addDependency(ty, DependencyType.FIELD_REF);
      fieldVisitor
    }
        
    def visitAnnotation(desc: String, visible: Boolean) = annotationVisitor
    def visitInnerClass(name: String, outerName: String, innerName: String, access: Int) { }
    def visitOuterClass(owner: String, name: String, desc: String) { }
    def visitSource(source: String, debug: String) { }
    def visitAttribute(attr: Attribute) { }
  }
    
  private object methodVisitor extends MethodVisitor {
    def visitAnnotation(desc: String, visible: Boolean) = {
      addDependency(Type.getType(desc), DependencyType.REF)
      annotationVisitor
    }
    
    def visitParameterAnnotation(parameter: Int, desc: String, visible: Boolean) = {
      addDependency(Type.getType(desc), DependencyType.REF)
      annotationVisitor
    }
    
    def visitAnnotationDefault = annotationVisitor

    def visitLocalVariable(name: String, signature: String, genericSignature: String, start: Label, end: Label, index: Int) {
      var descriptor = if (genericSignature != null) genericSignature else signature
      for (ty <- getTypesFromGenericSignature(descriptor))
        addDependency(ty, DependencyType.REF)
    }
        
    def visitTypeInsn(opcode: Int, desc: String) =
      addDependencyToType(desc, DependencyType.REF)
        
    def visitFieldInsn(opcode: Int, owner: String, name: String, desc: String) {
      addDependencyToType(owner, DependencyType.REF)
      addDependency(Type.getType(desc), DependencyType.REF)
    }
        
    def visitMethodInsn(opcode: Int, owner: String, name: String, desc: String) {
      addDependencyToType(owner, DependencyType.REF)
      addDependency(Type.getReturnType(desc), DependencyType.REF)

      for (ty <- Type.getArgumentTypes(desc))
        addDependency(ty, DependencyType.REF)
    }
        
    def visitMultiANewArrayInsn(desc: String, dims: Int) {
      addDependency(Type.getType(desc), DependencyType.REF)
    }
        
    def visitTryCatchBlock(start: Label, end: Label, handler: Label, typename: String) { }
    def visitAttribute(attr: Attribute) { }
    def visitCode() { }
    def visitEnd() { }
    def visitIincInsn(variable: Int, increment: Int) { }
    def visitInsn(opcode: Int) { }
    def visitIntInsn(opcode: Int, operand: Int) { }
    def visitJumpInsn(opcode: Int, label: Label) { }
    def visitLabel(label: Label) { }
    def visitLdcInsn(cst: Object) { }
    def visitLineNumber(line: Int, start: Label) { }
    def visitLookupSwitchInsn(dflt: Label, keys: Array[Int], labels: Array[Label]) { }
    def visitMaxs(maxStack: Int, maxLocals: Int) { }
    def visitTableSwitchInsn(min: Int, max: Int, dflt: Label, labels: Array[Label]) { }
    def visitVarInsn(opcode: Int, variable: Int) { }
  }
    
  private object fieldVisitor extends FieldVisitor {
    def visitAnnotation(desc: String, visible: Boolean) = {
      addDependency(Type.getType(desc), DependencyType.REF)
      annotationVisitor
    }

    def visitAttribute(attr: Attribute) = ()
    def visitEnd = ()
  }
    
  private object annotationVisitor extends AnnotationVisitor {
    def visit(name: String, value: Object) = ()
    def visitAnnotation(name: String, desc: String) = this
    def visitArray(name: String) = this
    def visitEnum(name: String, desc: String, value: String) = ()
    def visitEnd() = ()
  }
}
