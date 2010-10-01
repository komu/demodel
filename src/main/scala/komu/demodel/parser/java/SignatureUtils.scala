/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.parser.java

import scala.collection.mutable.ArrayBuffer
import komu.demodel.domain.TypeName

object SignatureUtils {

  def getTypesFromGenericMethodSignature(signature: String, descriptor: String): Iterable[TypeName] =
    getTypesFromGenericMethodSignature(if (signature != null) signature else descriptor)

  def getTypesFromGenericMethodSignature(signature: String): Iterable[TypeName] =
    new SignatureParser(signature).methodDescriptor()
    
  def getTypesFromGenericSignature(signature: String, descriptor: String): Iterable[TypeName] =
    getTypesFromGenericSignature(if (signature != null) signature else descriptor)
    
  def getTypesFromGenericSignature(signature: String): Iterable[TypeName] =
    new SignatureParser(signature).fieldDescriptor()
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
  def fieldType(): Iterable[TypeName] =
    lexer.read() match {
      case 'B' => List(TypeName.BYTE)
      case 'C' => List(TypeName.CHAR)
      case 'D' => List(TypeName.DOUBLE)
      case 'F' => List(TypeName.FLOAT)
      case 'I' => List(TypeName.INT)
      case 'J' => List(TypeName.LONG)
      case 'S' => List(TypeName.SHORT)
      case 'Z' => List(TypeName.BOOLEAN)
      case 'L' => objectType()
      case '[' => componentType()
      case 'T' => typeParameterName(); lexer.expect(';'); Nil
      case '*' => Nil
      case c   => fail("invalid descriptor; expected fieldType, but got " + c)
    }
  
  // ObjectType:
  //   L <className> ;
  def objectType() = {
    val args = new ArrayBuffer[TypeName]
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
    val types = new ArrayBuffer[TypeName]
    
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
  
  def makeType(name: String) = 
    TypeName.forInternalClassName(name)
  
  // MethodDescriptor:
  //   ( ParameterDescriptor* ) ReturnDescriptor
  def methodDescriptor(): Seq[TypeName] = {
    val types = new ArrayBuffer[TypeName]
    
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
