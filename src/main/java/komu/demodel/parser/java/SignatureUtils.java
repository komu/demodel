/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.parser.java;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.Type;

public class SignatureUtils {
    
    private static final Pattern METHOD_SIGNATURE_PATTERN =
        Pattern.compile("^\\((.*)\\)(.*)$");

    
    public static List<Type> getTypesFromGenericMethodSignature(String signature) {
        Matcher m = METHOD_SIGNATURE_PATTERN.matcher(signature);
        if (!m.matches())
            throw new IllegalArgumentException("invalid signature: " + signature);
        
        List<Type> result = new ArrayList<Type>();
        result.addAll(getTypesFromGenericSignature(m.group(1)));
        result.addAll(getTypesFromGenericSignature(m.group(2)));
        return result;
    }
    
    public static List<Type> getTypesFromGenericSignature(String signature) {
        if (signature.length() == 0) return emptyList();
        
        String[] parts = signature.split("[;<>]"); 
        List<Type> types = new ArrayList<Type>(parts.length);
        for (String part : parts)
            types.add(Type.getType(part + ";"));
        return types;
    }
}
