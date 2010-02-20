/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.parser.java;

import static junit.framework.Assert.assertEquals;
import static komu.demodel.parser.java.SignatureUtils.getTypesFromGenericSignature;

import java.util.List;

import org.junit.Test;
import org.objectweb.asm.Type;

public class SignatureUtilsTest {
    
    @Test
    public void nullSignatureHasNoTypes() {
        assertTypes(null);
    }
    
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
    
    private static void assertTypes(String signature, String... classNames) {
        List<Type> types = getTypesFromGenericSignature(signature);
        assertEquals("size", classNames.length, types.size());
        
        for (int i = 0; i < classNames.length; i++)
            assertEquals("className", classNames[i], types.get(i).getClassName());
    }
}
