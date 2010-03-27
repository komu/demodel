/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.parser.java

import komu.demodel.domain.TypeType

object AccessUtils {

  // Access flags from http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html
  val ACC_PUBLIC    = 0x0001;
  val ACC_FINAL     = 0x0010;
  val ACC_SUPER     = 0x0020;
  val ACC_INTERFACE = 0x0200;
  val ACC_ABSTRACT  = 0x0400;
    
  def getTypeFromAccessFlags(access: Int) =
    if ((access & ACC_INTERFACE) != 0) 
      TypeType.INTERFACE
    else if ((access & ACC_ABSTRACT) != 0) 
      TypeType.ABSTRACT_CLASS
    else 
      TypeType.CONCRETE_CLASS
}
