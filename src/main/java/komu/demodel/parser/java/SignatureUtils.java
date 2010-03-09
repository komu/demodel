/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.parser.java;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;

public class SignatureUtils {
    
    public static List<Type> getTypesFromGenericMethodSignature(String signature) {
        List<Type> result = new ArrayList<Type>();
        for (String part : signature.split("[\\(\\)]+"))
        	if (part.length() != 0)
        		result.addAll(getTypesFromGenericSignature(part));	
        return result;
    }
    
    public static List<Type> getTypesFromGenericSignature(String signature) {
        if (signature.length() == 0) return emptyList();
        
        try {
            String[] parts = signature.split("[;<>\\(\\)]+"); 
            List<Type> types = new ArrayList<Type>(parts.length);
            for (String part : parts)
            	if (part.length() != 0) {
            		Type type = Type.getType((part + ";"));
            		if (!type.getClassName().equals(""))
            			types.add(type);
            	}
            return types;
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid signature: " + signature);
        }
    }
}
