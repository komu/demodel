/*
 * Copyright (C) 2006 Juha Komulainen. All rights reserved.
 */
package komu.demodel.parser.java;

import static komu.demodel.parser.java.SignatureUtils.getTypesFromGenericSignature;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import komu.demodel.domain.DependencyModel;
import komu.demodel.domain.DependencyType;
import komu.demodel.domain.Module;
import komu.demodel.utils.ExtensionFileFilter;
import komu.demodel.utils.FileSet;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class JavaDependencyParser {
    
    private final Map<String, Module> modules = new TreeMap<String, Module>();
    private final ClassVisitor classVisitor = new MyClassVisitor();
    private final MethodVisitor methodVisitor = new MyMethodVisitor();
    private final FieldVisitor fieldVisitor = new MyFieldVisitor();
    private final AnnotationVisitor annotationVisitor = new MyAnnotationVisitor();
    private Module module;

    public void parseDirectory(File directory) throws IOException {
        for (File file : new FileSet(directory, new ExtensionFileFilter("class"))) {
            visitFile(file);
        }
    }
    
    private void visitFile(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            ClassReader reader = new ClassReader(in);
            reader.accept(classVisitor, false);
            
        } finally {
            in.close();
        }
    }
    
    public DependencyModel getModel() {
        DependencyModel model = new DependencyModel();
        
        for (Module module : modules.values()) {
            if (module.isProgramModule()) {
                model.getModules().add(module);
            }
        }
        
        return model;
    }
    
    private Module getVisitedModule(String name) {
        Module module = getModule(name);
        module.setProgramModule(true);
        return module;
    }
    
    private Module getModule(String name) {
        name = name.replace('/', '.');
        int dollarIndex = name.indexOf('$');
        if (dollarIndex != -1) {
            // inner classes are considered the same module as parent
            name = name.substring(0, dollarIndex);
        }
        
        // finally, strip the class name (for now)
        int periodIndex = name.lastIndexOf('.');
        if (periodIndex != -1) {
            name = name.substring(0, periodIndex);
        }
        
        Module module = modules.get(name);
        if (module == null) {
            module = new Module(name);
            modules.put(name, module);
        }
        return module;
    }
    
    private void addDependency(String toModule, DependencyType type) {
        Module target = getModule(toModule);
        if (module != target) {
            module.addDependency(target, type);
        }
    }

    private void addDependency(Type toType, DependencyType type) {
        addDependency(toType.getClassName(), type);
    }
    
    private class MyClassVisitor implements ClassVisitor {
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            module = getVisitedModule(name);
            
            if (superName != null) {
                addDependency(superName, DependencyType.INHERITANCE);
            }
            
            for (String interfaceName : interfaces) {
                addDependency(interfaceName, DependencyType.INHERITANCE);
            }
        }
        
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            addDependency(Type.getReturnType(desc), DependencyType.REF);

            for (Type type : Type.getArgumentTypes(desc)) {
                addDependency(type, DependencyType.REF);
            }
            
            for (Type type : getTypesFromGenericSignature(signature)) {
                addDependency(type, DependencyType.REF);
            }
            
            return methodVisitor;
        }
        
        public void visitEnd() {
            module = null;
        }
        
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            addDependency(Type.getType(desc), DependencyType.FIELD_REF);

            for (Type type : getTypesFromGenericSignature(signature)) {
                addDependency(type, DependencyType.FIELD_REF);
            }

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
        
        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
            Type fieldType = Type.getType(desc);
            
            addDependency(fieldType, DependencyType.REF);

            for (Type type : getTypesFromGenericSignature(signature)) {
                addDependency(type, DependencyType.REF);
            }
        }
        
        public void visitTypeInsn(int opcode, String desc) {
            addDependency(desc, DependencyType.REF);
        }
        
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            addDependency(owner, DependencyType.REF);
            addDependency(Type.getType(desc), DependencyType.REF);
        }
        
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            addDependency(owner, DependencyType.REF);
            
            addDependency(Type.getReturnType(desc), DependencyType.REF);

            for (Type type : Type.getArgumentTypes(desc)) {
                addDependency(type, DependencyType.REF);
            }
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
