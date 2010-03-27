/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils

import java.io.{ File, FileFilter }
import java.util.{ Iterator, LinkedList }

final class FileIterator(rootDirectory: File, filter: FileFilter) extends Iterator[File] {

  private val files = new LinkedList[File]
  private val directories = new LinkedList[File]

  directories.add(rootDirectory)
  addIfAccepted(rootDirectory)
  step()

  def hasNext = !files.isEmpty

  def next() = {
    if (files.isEmpty) throw new IllegalStateException("no more files")
        
    var file = files.removeFirst()
    step();
    file
  }
    
  def remove() = throw new UnsupportedOperationException
    
  private def step() {
    while (files.isEmpty && !directories.isEmpty) {
      val directory = directories.removeFirst()
            
      for (file <- directory.listFiles) {
        if (file.isDirectory)
          directories.add(file)
                
        addIfAccepted(file);
      }
    }
  }

  private def addIfAccepted(file: File) {
    if (filter.accept(file)) 
      files.add(file)
  }
}
