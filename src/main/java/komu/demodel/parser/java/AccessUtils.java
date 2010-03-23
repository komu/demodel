package komu.demodel.parser.java;

import komu.demodel.domain.TypeType;

final class AccessUtils {

    // Access flags from http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html
    static final int ACC_PUBLIC    = 0x0001;
    static final int ACC_FINAL     = 0x0010;
    static final int ACC_SUPER     = 0x0020;
    static final int ACC_INTERFACE = 0x0200;
    static final int ACC_ABSTRACT  = 0x0400;
    
    static TypeType getTypeFromAccessFlags(int access) {
        return ((access & ACC_INTERFACE) != 0) ? TypeType.INTERFACE
             : ((access & ACC_ABSTRACT) != 0)  ? TypeType.ABSTRACT_CLASS
             : TypeType.CONCRETE_CLASS;
    }
}
