/*
 * Copyright (C) 2006 Juha Komulainen. All rights reserved.
 */
package komu.demodel.parser.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.Type;

public class SignatureUtils {
    
    private static final Pattern SIGNATURE_PATTERN =
        Pattern.compile("^(L[^<;]+)<(.+)>;$");
    
    public static List<Type> getTypesFromGenericSignature(String signature) {
        if (signature == null) return Collections.emptyList();
        
        List<Type> types = new ArrayList<Type>();
        parseTypesFromGenericSignature(types, signature);
        return types;
    }

    private static void parseTypesFromGenericSignature(List<Type> types, String signature) {
        Matcher m = SIGNATURE_PATTERN.matcher(signature);
        if (m.matches()) {
            types.add(Type.getType(m.group(1) + ";"));
            parseTypesFromGenericSignature(types, m.group(2));
            
        } else {
            for (String s : signature.split(";")) {
                types.add(Type.getType(s + ";"));
            }
        }
    }
}
