package org.getch

import grails.transaction.Transactional
import groovy.io.FileType
import java.net.InetAddress

/**
 * Main Service class of Getch implementing all required interactions 
 * with the configuration filsystem hierarchy.
 *
 * @author Robert Krombholz
 */
class FileSystemTreeService {

    def grailsApplication

    def textEncryptor
   
    /**
     * finds a value for the given key searching upwards
     * in the filesystem hierarchy starting from the 'fromDir'
     * assuming that this exist.
     *
     * param fromDir the directory to upwards from 
     * param key the key to search for in all config.properties
     */
    String findValue(String fromDir, String key) {
      def baseDir = new File(grailsApplication.config.getch.base.directory)
      if (!baseDir.canRead()) {
        throw new IOException("Can not read ${baseDir.absolutePath}")
      }
      def startDir = null
      //find the first matching directory
      baseDir.eachDirRecurse {
        if( !startDir && it.name == fromDir ) {
          startDir= it
        }
      }
      //return null if the searched directory does not exist in the tree
      //or the result of findValueUpwards in case it does
      //TODO: seperate those statements to make it possible to search for other start dirs with the same name. Currently we stop at the first found dir
      def returnValue 
      if(startDir) {
        returnValue = findValueUpwards(startDir, key, baseDir) 
        if(returnValue && returnValue?.startsWith('sec:')) {
          returnValue = textEncryptor.decrypt(returnValue.split('sec:')[1])
        }
      }
      return returnValue
    }
    
    /**
     * Lists all available configuration values upwards in the hierarchy
     * starting from the given directory name. Returns null if the directory 
     * is not found in the hirarchy.
     *
     * @param fromDir the directory to upwards from 
     */
    public Map listValues(String fromDir) {
      def baseDir = new File(grailsApplication.config.getch.base.directory)
      if (!baseDir.canRead()) {
        throw new IOException("Can not read ${baseDir.absolutePath}")
      }
      def startDir = null
      //find the first matching directory
      baseDir.eachDirRecurse {
        if( !startDir && it.name == fromDir ) {
          startDir= it
        }
      }
      def returnValue 
      if(startDir) {
	returnValue = findValueUpwards(startDir, null, baseDir) 
        //decrypt all potentially encrypted values
        returnValue = returnValue?.collectEntries { key, value -> 
          def newValue 
          if(value instanceof String) {
            newValue = value?.startsWith('sec:') ? textEncryptor.decrypt(value.split('sec:')[1]) : value
          }
          else if (value instanceof Collection) {
            newValue = value.join(',')
          }
          else {
            newValue = value.toString()
          }
          [key, newValue]
        }
      }
      return returnValue.sort()
    }

    /**
     * does a reverse DNS lookup and returns the hostname for the given
     * textual representation of the IP address.
     *
     * @param ip Textual representation of the IP
     */
    public String getHostnameFromIP(String ip, boolean stripDomainName = true) {
      def fqdn = InetAddress.getByName(ip).hostName
      // return the hostname only unless the stripDomainName flag is set to false
      return stripDomainName ? fqdn.split("\\.")[0] : fqdn
    }

    /**
     * scans through all .properties files in the given dir
     * and searches for the Key.
     * It calls itself recuisively untill the baseDir is reached
     *
     * @param dir The directory to search int
     * @param key the key to look for - leave this empty to get a map of all values
     * @param baseDir the top directory where the search should stop
     * @param result internally used to pass results through recursion levels
     * @return A String representing the value of the queried key or a Map representing all configuration values in case the key was null.
     */
    private def findValueUpwards(File dir, String key, File baseDir, def result = null) {
      //Initialize the file Reader. This delegates reading to the correct reader implmentation
      //depending on the file suffix
      FileReader reader = FileReaderFactory.createNewInstance()
      //for all properties file in the current directory
      dir.eachFile(FileType.FILES) {
        //only if a key was provided and we didn't yet find the result
        if (key && !result) {
            //save the property - may be null but that's okay
            result = reader.getValueForKey(it, key)
        }
        //if no key was provided we want to list all config values
        else if (!key)  {
            //initialize a map as the result
            if (!result) {
              result = [:]
            }
            //add all values to the result
            def allValues = reader.getAllValues(it)
            if(allValues) {
              result += allValues
            }
        }
      }
      //call the method recusively if a key was given and the result was empty
      if (result == null && dir.name != baseDir.name && key) {
        return findValueUpwards(dir.parentFile, key, baseDir)
      }
      //in case no key was given we want to do the recursion up to the top-level dir anyway
      else if(!key && dir.name != baseDir.name) {
        //we need to pass through the results found until now so that later recursions can add to that
        return findValueUpwards(dir.parentFile, key, baseDir, result)
      }
      //return the result if we found one or if we reached the top-level dir
      else {
        return result
      }
    }

}
