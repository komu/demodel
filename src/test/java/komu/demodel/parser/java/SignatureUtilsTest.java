/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.parser.java;

import static junit.framework.Assert.assertEquals;
import static komu.demodel.parser.java.SignatureUtils.getTypesFromGenericSignature;
import static komu.demodel.parser.java.SignatureUtils.getTypesFromGenericMethodSignature;

import java.util.List;

import org.junit.Test;
import org.objectweb.asm.Type;

public class SignatureUtilsTest {
    
    @Test
    public void typeParametersShouldBeParsed() {
        assertTypes("Ljava/util/List<Ljava/lang/String;>;", 
                    "java.util.List", "java.lang.String");
        assertTypes("Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;",
                    "java.util.Map", "java.lang.String", "java.lang.Integer");
    }

    @Test
    public void nestedSignature() {
        assertTypes("Ljava/util/Map<Ljava/lang/String;Ljava/util/Map<Ljava/lang/Integer;Ljava/lang/Float;>;>;",
                "java.util.Map", "java.lang.String", "java.util.Map", "java.lang.Integer", "java.lang.Float");
    }
    
    @Test
    public void methodSignature() {
        assertMethodTypes("(Ljava/util/List<Lfoo/Bar;>;)Lfoo/Baz;",
                          "java.util.List", "foo.Bar", "foo.Baz");
    }

    @Test
    public void methodSignatureWithoutParameters() {
        assertMethodTypes("()Lfoo/Baz;",
                          "foo.Baz");
    }
    
    @Test
    public void complexMethodSignature() {
    	assertMethodTypes("<T:Ljava/lang/Object;>(Ljava/lang/Class<*>;)Lfoo/bar/Baz$FieldSet<TT;>;",
    			":Ljava.lang.Object", "java.lang.Class", "foo.bar.Baz$FieldSet", "T");
    }
    
    private static void assertTypes(String signature, String... classNames) {
        assertReturnTypes(classNames, getTypesFromGenericSignature(signature));
    }

    private static void assertMethodTypes(String signature, String... classNames) {
        assertReturnTypes(classNames, getTypesFromGenericMethodSignature(signature));
    }

    private static void assertReturnTypes(String[] expectedTypes, List<Type> types) {
        assertEquals("size", expectedTypes.length, types.size());
        
        for (int i = 0; i < expectedTypes.length; i++)
            assertEquals("className", expectedTypes[i], types.get(i).getClassName());
    }

}
