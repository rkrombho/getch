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
      //Initialize the file Reader. This delegates reading to the correct reader implmentation
      //depending on the file suffix
      FileReader reader = FileReaderFactory.createNewInstance()
      //for all properties file in the current directory
      dir.eachFile(FileType.FILES) {
        //only if we don't already have a result (we match the first)
        if (!result) {
          //save the property - may be null but that's okay
          result = reader.getValueForKey(it, key)
        }
      }
      //return the result if we found one or if we reached the top-level dir
      //call the method recusively otherwhise
      if (result == null && dir.name != baseDir.name && dir.parentFile != null) {
        return findValueUpwards(dir.parentFile, key, baseDir)
      }
      else {
        return result
      }
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
}
