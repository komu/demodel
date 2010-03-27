/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain.project

import scala.collection.JavaConversions._
import java.io.File
import java.util.jar.{ JarEntry, JarFile }
import komu.demodel.utils.{ Resource, ResourceProvider }

final class JarFileInputSource(private file: File) extends InputSource {
    
  def withResources(thunk: Resource => Unit) {
    val jar = new JarFile(file)
    try {
      for (entry <- getClassEntries(jar))
        thunk(entry)
    } finally {
      jar.close()
    }
  }

  private def getClassEntries(jar: JarFile) =
    for (entry <- jar.entries if entry.getName.endsWith(".class"))
      yield new Resource {
              def open() = jar.getInputStream(entry)
            }
  
  override def toString = file.toString  
}
