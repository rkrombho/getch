package org.getch

import static groovy.io.FileType.DIRECTORIES
import static groovy.io.FileType.FILES

class FileMetaAddition {
  public static Closure addMethods() {
    //We need that because Groovy File.eachFileRecurse does not work as described in the docs
    //it is meant to work in a depth-first fashion but this doesn't really work
    File.metaClass.eachFileRecurseDepthFirst = { Closure c ->
      delegate.eachFile(DIRECTORIES) {
        it.eachFileRecurseDepthFirst(c)
      }
      delegate.eachFile(FILES) {
        c.call(it)
      }
    }
  }
}
