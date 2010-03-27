/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.parser.java

import java.util.Collections.emptyList
import java.util.{ ArrayList, List }
import org.objectweb.asm.Type

object SignatureUtils {

  def getTypesFromGenericMethodSignature(signature: String): List[Type] = {
    val result = new ArrayList[Type]
    for (part <- signature.split("[\\(\\)]+") if part.length != 0)
      result.addAll(getTypesFromGenericSignature(part))
    result
  }
    
  def getTypesFromGenericSignature(signature: String): List[Type] = {
    if (signature.length == 0) return emptyList()
        
    try {
      val parts = signature.split("[;<>\\(\\)]+")
      val types = new ArrayList[Type](parts.length)
      for (part <- parts if part.length != 0) {
      	val typ = Type.getType((part + ";"))
    		if (!typ.getClassName.equals(""))
  			  types.add(typ)
      }
      types
    } catch {
      case _ =>
        throw new IllegalArgumentException("invalid signature: " + signature);
    }
  }
}
