package org.getch

import grails.transaction.Transactional
import groovy.io.FileType

/**
 * Main Service class of Getch implementing all required interactions 
 * with the configuration filsystem hierarchy.
 *
 * @author Robert Krombholz
 */
class FileSystemTreeService {

    def grailsApplication
   
    /**
     * finds a value for the given key searching upwards
     * in the filesystem hierarchy starting from the 'fromDir'
     * assuming that this exist.
     *
     * param fromDir the directory to upwards from 
     * param key the key to search for in all config.properties
     */
    def findValue(String fromDir, String key) {
      def baseDir = new File(grailsApplication.config.getch.base.directory)
      if (!baseDir.canRead()) {
        throw new IOException("Can not read ${baseDir.absolutePath}")
      }
      def startDir 
      //find the first matching directory
      baseDir.eachDirRecurse {
        if( !startDir && it.name == fromDir ) {
          startDir= it
        }
      }
      //return null if the searched directory does not exist in the tree
      //or the result of findValueUpwards in case it does
      return startDir ? findValueUpwards(startDir, key, baseDir) : null
    }
    
    /**
     * scans through all .properties files in the given dir
     * and searches for the Key.
     * It calls itself recuisively untill the baseDir is reached
     *
     * param dir The directory to search int
     * param key the key to look for
     * param baseDir the top directory where the search should stop
     */
    private findValueUpwards(File dir, String key, File baseDir) {
      String result
      //for all properties file in the current directory
      dir.eachFileMatch(FileType.FILES, ~/.*\.properties/) {
        //load the properties
        def props = new Properties()
        props.load(it.newInputStream())
        //only if we don't already have a result (we match the first)
        if (!result) {
          //save the property
          result = props."$key" 
        }
      }
      //return the result if we found one or if we reached the top-level dir
      //call the method recusively otherwhise
      if (result == null && dir.name != baseDir.name && dir.parentFile != null) {
        println "rec call (${dir.parentFile}, $key, $baseDir)"
        return findValueUpwards(dir.parentFile, key, baseDir)
      }
      else {
        println "returning $result"
        return result
      }
    }
}
