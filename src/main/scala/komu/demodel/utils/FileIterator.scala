/*
 * Copyright (C) 2006-2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.utils

import java.io.{ File, FileFilter }
import collection.mutable.Queue

final class FileIterator(rootDirectory: File, filter: FileFilter) extends Iterator[File] {

  private val files = new Queue[File]
  private val directories = new Queue[File]

  directories.enqueue(rootDirectory)
  addIfAccepted(rootDirectory)
  step()

  def hasNext = !files.isEmpty

  def next() = {
    if (files.isEmpty) throw new IllegalStateException("no more files")
        
    var file = files.dequeue()
    step();
    file
  }
    
  private def step() {
    while (files.isEmpty && !directories.isEmpty) {
      val directory = directories.dequeue()
            
      for (file <- directory.listFiles) {
        if (file.isDirectory)
          directories.enqueue(file)
                
        addIfAccepted(file);
      }
    }
  }

  private def addIfAccepted(file: File) {
    if (filter.accept(file)) 
      files.enqueue(file)
  }
}
