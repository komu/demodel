/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.parser.java;

import static komu.demodel.parser.java.SignatureUtils.getTypesFromGenericMethodSignature;
import static komu.demodel.parser.java.SignatureUtils.getTypesFromGenericSignature;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import komu.demodel.domain.DependencyType;
import komu.demodel.domain.InputSource;
import komu.demodel.domain.Module;
import komu.demodel.utils.Resource;
import komu.demodel.utils.ResourceProvider;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class JavaDependencyParser {

    private final Module rootModule = new Module("<root>", true, null);
    private final Map<String, Module> modules = new TreeMap<String, Module>();
    private final ClassVisitor classVisitor = new MyClassVisitor();
    private final MethodVisitor methodVisitor = new MyMethodVisitor();
    private final FieldVisitor fieldVisitor = new MyFieldVisitor();
    private final AnnotationVisitor annotationVisitor = new MyAnnotationVisitor();
    private Module currentModule;
    
    public void parse(InputSource inputSource) throws IOException {
        ResourceProvider resources = inputSource.getResources();
        try {
            for (Resource resource : resources)
    		visitResource(resource);
        } finally {
            resources.close();
        }
    }
    
    private void visitResource(Resource resource) throws IOException {
        InputStream in = resource.open();
        try {
            ClassReader reader = new ClassReader(in);
            reader.accept(classVisitor, false);
            
        } finally {
            in.close();
        }
    }
    
    public Module getRoot() {
        rootModule.filterNonProgramReferences();
        rootModule.normalizeTree();
        rootModule.flushCaches();
        return rootModule;
    }

    private Module getVisitedModuleForType(String name) {
        Module module = getModuleForType(name);
        module.markAsProgramModule();
        return module;
    }
    
    private Module getModuleForType(String className) {
        return getModuleByName(moduleNameForType(className), false);
    }

    private Module getModuleByName(String name, boolean container) {
        Module module = modules.get(name);
        if (module == null) {
            module = new Module(name, container, getParentModule(name));
            modules.put(name, module);
        }
        return module;
    }

    private Module getParentModule(String name) {
        int periodIndex = name.lastIndexOf('.');
        return (periodIndex != -1) ? getModuleByName(name.substring(0, periodIndex), true) : rootModule;
    }
    
    private static String moduleNameForType(String name) {
        return name.replace('/', '.');
    }
    
    private void addDependencyToType(String typeName, DependencyType type) {
        Module target = getModuleForType(typeName);
        if (currentModule != target)
            currentModule.addDependency(target, type);
    }

    private void addDependency(Type toType, DependencyType type) {
        addDependencyToType(toType.getClassName(), type);
    }
    
    private class MyClassVisitor implements ClassVisitor {
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            currentModule = getVisitedModuleForType(name);
            
            if (superName != null)
                addDependencyToType(superName, DependencyType.INHERITANCE);
            
            for (String interfaceName : interfaces)
                addDependencyToType(interfaceName, DependencyType.INHERITANCE);
        }
        
        public MethodVisitor visitMethod(int access, String name, String signature, String genericSignature, String[] exceptions) {
            String descriptor = (genericSignature != null) ? genericSignature : signature;
            for (Type type : getTypesFromGenericMethodSignature(descriptor))
                addDependency(type, DependencyType.REF);
            
            return methodVisitor;
        }
        
        public void visitEnd() {
            currentModule = null;
        }
        
        public FieldVisitor visitField(int access, String name, String signature, String genericSignature, Object value) {
            String descriptor = (genericSignature != null) ? genericSignature : signature;
            for (Type type : getTypesFromGenericSignature(descriptor))
                addDependency(type, DependencyType.FIELD_REF);
            return fieldVisitor;
        }
        
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return annotationVisitor;
        }

        public void visitInnerClass(String name, String outerName, String innerName, int access) {
        }
        public void visitOuterClass(String owner, String name, String desc) {
        }
        public void visitSource(String source, String debug) {
        }
        public void visitAttribute(Attribute attr) {
        }
    }
    
    private class MyMethodVisitor implements MethodVisitor {
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            addDependency(Type.getType(desc), DependencyType.REF);
            return annotationVisitor;
        }
        public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
            addDependency(Type.getType(desc), DependencyType.REF);
            return annotationVisitor;
        }
        public AnnotationVisitor visitAnnotationDefault() {
            return annotationVisitor;
        }
        
        public void visitLocalVariable(String name, String signature, String genericSignature, Label start, Label end, int index) {
            String descriptor = (genericSignature != null) ? genericSignature : signature;
            for  (Type type : getTypesFromGenericSignature(descriptor))
                addDependency(type, DependencyType.REF);
        }
        
        public void visitTypeInsn(int opcode, String desc) {
            addDependencyToType(desc, DependencyType.REF);
        }
        
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            addDependencyToType(owner, DependencyType.REF);
            addDependency(Type.getType(desc), DependencyType.REF);
        }
        
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            addDependencyToType(owner, DependencyType.REF);
            
            addDependency(Type.getReturnType(desc), DependencyType.REF);

            for (Type type : Type.getArgumentTypes(desc))
                addDependency(type, DependencyType.REF);
        }
        
        public void visitMultiANewArrayInsn(String desc, int dims) {
            addDependency(Type.getType(desc), DependencyType.REF);
        }
        
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        }

        public void visitAttribute(Attribute attr) {
        }
        public void visitCode() {
        }
        public void visitEnd() {
        }
        public void visitIincInsn(int var, int increment) {
        }
        public void visitInsn(int opcode) {
        }
        public void visitIntInsn(int opcode, int operand) {
        }
        public void visitJumpInsn(int opcode, Label label) {
        }
        public void visitLabel(Label label) {
        }
        public void visitLdcInsn(Object cst) {
        }
        public void visitLineNumber(int line, Label start) {
        }
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        }
        public void visitMaxs(int maxStack, int maxLocals) {
        }
        public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
        }
        public void visitVarInsn(int opcode, int var) {
        }
    }
    
    private class MyFieldVisitor implements FieldVisitor {
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            addDependency(Type.getType(desc), DependencyType.REF);
            return annotationVisitor;
        }
        public void visitAttribute(Attribute attr) {
        }
        public void visitEnd() {
        }
    }
    
    private class MyAnnotationVisitor implements AnnotationVisitor {
        public void visit(String name, Object value) {
        }
        public void visitEnd() {
        }
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            return this;
        }
        public AnnotationVisitor visitArray(String name) {
            return this;
        }
        public void visitEnum(String name, String desc, String value) {
        }
    }
}
