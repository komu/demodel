/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.parser.java

import scala.collection.mutable.ArrayBuffer
import org.objectweb.asm.Type

object SignatureUtils {

  def getTypesFromGenericMethodSignature(signature: String) =
    new SignatureParser(signature).methodDescriptor()
    
  def getTypesFromGenericSignature(signature: String): Iterable[Type] = {
    new SignatureParser(signature).fieldDescriptor()
    /*
    if (signature.length == 0) return Nil
        
    try {
      val parts = signature.split("[;<>\\(\\)]+")
      val types = new ArrayBuffer[Type](parts.length)
      for (part <- parts if part.length != 0 && !part.startsWith(".")) {
        val typ = Type.getType((part + ";"))
        if (!typ.getClassName.equals(""))
          types += typ
      }
      return types
    } catch {
      case _ =>
        throw new IllegalArgumentException("invalid signature: " + signature);
    }
    */
  }
}  

// http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#1169
class SignatureParser(descriptor: String) {

  // FieldDescriptor:
  //   FieldType
  def fieldDescriptor() = 
    fieldType()
  
  // ComponentType:
  //   FieldType
  def componentType() =
    fieldType()
  
  // FieldType:
  //   BaseType
  //   ObjectType
  //   ArrayType
  //   GenericType     (+)
  //
  // BaseType:
  //   B | C | D | F | I | J | S | Z
  //
  // ArrayType:
  //   [ ComponentType
  //
  // GenericType:
  //   T <alias> ;
  def fieldType(): Iterable[Type] =
    lexer.read() match {
      case 'B' => List(Type.BYTE_TYPE)
      case 'C' => List(Type.CHAR_TYPE)
      case 'D' => List(Type.DOUBLE_TYPE)
      case 'F' => List(Type.FLOAT_TYPE)
      case 'I' => List(Type.INT_TYPE)
      case 'J' => List(Type.LONG_TYPE)
      case 'S' => List(Type.SHORT_TYPE)
      case 'Z' => List(Type.BOOLEAN_TYPE)
      case 'L' => objectType()
      case '[' => componentType()
      case 'T' => typeParameterName(); lexer.expect(';'); Nil
      case '*' => Nil
      case c   => fail("invalid descriptor; expected fieldType, but got " + c)
    }
  
  // ObjectType:
  //   L <className> ;
  def objectType() = {
    val args = new ArrayBuffer[Type]
    val name = new StringBuilder
    
    while (lexer.peek() != ';') {
      if (lexer.peek() == '<')
        args ++= genericArguments()
      else
        name.append(lexer.read())
    }
  
    lexer.expect(';')
     
    val typ = makeType(name.toString)
    
    typ :: args.toList
  }
  
  def genericArguments() = {
    val types = new ArrayBuffer[Type]
    
    lexer.expect('<')

    while (lexer.peek() != '>')
      types ++= genericArgumentType()
    
    lexer.expect('>')

    types
  }
  
  def genericArgumentType() = {
    lexer.ignore('!')
    lexer.ignore('+')
    lexer.ignore('-')
    
    fieldType()
  }
      
  def typeParameterName() = {
    val sb = new StringBuilder()
    
    while (!";<".contains(lexer.peek()))
      sb.append(lexer.read())

    sb.toString  
  }
  
  def makeType(name: String) = Type.getType("L" + name + ";")
  
  // MethodDescriptor:
  //   ( ParameterDescriptor* ) ReturnDescriptor
  def methodDescriptor(): Seq[Type] = {
    val types = new ArrayBuffer[Type]
    
    if (lexer.peek() == '<') { // generic method
      lexer.expect('<')
      
      while (lexer.peek() != '>') {
        lexer.skipUntil(':')
        
        // TODO: hack. for some reason there can be extra colons
        lexer.skipWhile(':')
      
        types ++= fieldType()
      }

      lexer.expect('>')
    }
    
    lexer.expect('(')

    while (lexer.peek() != ')')
      types ++= parameterDescriptor()
    
    lexer.expect(')')
    
    types ++= returnDescriptor()

    types
  }
  
  // ParameterDescriptor:
  //   FieldType
  def parameterDescriptor() = 
    fieldType()

  // ReturnDescriptor:
  //   FieldType
  //   V
  def returnDescriptor() =
    if (lexer.peek() == 'V') {
      lexer.read()
      Nil
    } else
      fieldType()
      
  private def fail(message: String) =
    throw new Exception(message + " (at " + lexer.index + " in " + descriptor + ")")
  
  object lexer {
    var next: Option[Char] = None
    var index = 0
    
    def skipUntil(ch: Char) {
      while (read() != ch) {
        // nada
      }
    }
    
    def skipWhile(ch: Char) {
      while (peek() == ch)
        read()
    }
    
    def ignore(ch: Char) {
      if (lexer.peek() == ch)
        lexer.read()
    }
    
    def expect(ch: Char) {
      val c = read()
      if (c != ch)
        fail("expected " + ch + ", but got " + c)
    }
    
    def read(): Char =
      next match {
      case Some(c) => 
        next = None
        c
      case None =>
        readInternal()
      }
    
    def peek(): Char =
      next match {
      case Some(c) =>
        c
      case None =>
        val c = readInternal()
        next = Some(c)
        c
      }
    
    def readInternal() = {
      if (index >= descriptor.length)
        fail("unexpected end of descriptor")
      
      val c = descriptor(index)
      index += 1
      c
    }
  }
}
