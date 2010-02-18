/*
 * Copyright (C) 2006 Juha Komulainen. All rights reserved.
 */
package komu.demodel.parser.java;

import static junit.framework.Assert.*;
import static komu.demodel.parser.java.SignatureUtils.getTypesFromGenericSignature;

import java.util.List;

import org.junit.Test;
import org.objectweb.asm.Type;

public class SignatureUtilsTest {
    
    @Test
    public void testNullSignature() {
        assertTrue(getTypesFromGenericSignature(null).isEmpty());
    }
    
    @Test
    public void testListSignature() {
        List<Type> types = getTypesFromGenericSignature("Ljava/util/List<Ljava/lang/String;>;");
        assertEquals("size", 2, types.size());
        assertEquals("java.util.List", types.get(0).getClassName());
        assertEquals("java.lang.String", types.get(1).getClassName());
    }

    @Test
    public void testMapSignature() {
        List<Type> types = getTypesFromGenericSignature("Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;");
        assertEquals("size", 3, types.size());
        assertEquals("java.util.Map", types.get(0).getClassName());
        assertEquals("java.lang.String", types.get(1).getClassName());
        assertEquals("java.lang.Integer", types.get(2).getClassName());
    }

    @Test
    public void testNestedMapSignature() {
        List<Type> types = getTypesFromGenericSignature(
                "Ljava/util/Map<Ljava/lang/String;Ljava/util/Map<Ljava/lang/Integer;Ljava/lang/Float;>;>;");
        System.out.println(types);
        assertEquals("size", 5, types.size());
        assertEquals("java.util.Map", types.get(0).getClassName());
        assertEquals("java.lang.String", types.get(1).getClassName());
        assertEquals("java.util.Map", types.get(2).getClassName());
        assertEquals("java.lang.Integer", types.get(3).getClassName());
        assertEquals("java.lang.Float", types.get(4).getClassName());
    }

    /*
     * 
Ljava/util/List<Lkomu/demodel/domain/Module;>;
Ljava/util/Map<Lkomu/demodel/domain/Module;Ljava/lang/Integer;>;
Ljava/util/LinkedList<Ljava/io/File;>;
Ljava/util/LinkedList<Ljava/io/File;>;
Ljava/util/Map<Ljava/lang/String;Lkomu/demodel/domain/Module;>;

     */
}
