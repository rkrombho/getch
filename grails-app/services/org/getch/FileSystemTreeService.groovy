package org.getch

import grails.transaction.Transactional
import groovy.io.FileType
import groovy.text.SimpleTemplateEngine

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
     * @param fromDir the directory to upwards from 
     * @param key the key to search for in all config.properties
     * @param addition a string addition that the directory name will be matched against in case multiple are found
     * @returns either a string representing the searched value or a Map with the keys 'filename' and 'content'
     */
    def findValue(String fromDir, String key, String addition = null) {
      //find the correct start directory for the provided parameter
      def startDir = findStartDir(fromDir, addition)
      def returnValue 
      //if at least one directory was found
      if(startDir) {
        //see if the queries key is a file that exists in the tree
        File file = searchFile(startDir, key)
        //if the key matches a filename in the tree
        if(file) {
          def content
          //check if the file should be treated as template or as normal file
          if(treatAsTemplate(file) && grailsApplication.config.getch.feature.templating.enabled) {
            //prepare the binding by listing all values of the tree
            def binding = listValues(fromDir, addition)
            content = resolveTemplateFile(file, binding)
          }
          else {
            content = file.text
          }
          //return a map with the Filename as key and the template text as value
          returnValue = [ 
            'filename' : file.name,
            'content' : content
          ]
        }
        else {
          //first search downwards in the tree from the startDir
          returnValue = findValueDownwards(startDir, key)
          //if nothing was found 
          if(!returnValue) {
            def baseDir = new File(grailsApplication.config.getch.base.directory)
            //search upwards
            returnValue = findValueUpwards(startDir, key, baseDir) 
          }
          //if the found value is encrypted, decrypt it
          if(returnValue && returnValue?.startsWith('sec:')) {
            returnValue = textEncryptor.decrypt(returnValue.split('sec:')[1])
          }
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
    public Map listValues(String fromDir, String addition = null) {
      //find the correct start directory
      def startDir = findStartDir(fromDir, addition)
      def returnValue 
      if(startDir) {
        def baseDir = new File(grailsApplication.config.getch.base.directory)
        //first search upwards in the tree
	returnValue = findValueUpwards(startDir, null, baseDir) 
        if (!returnValue) {
          returnValue = [:]
        }
        //then collect all values downwards. in that order because we want lower level keys to oevrwrite
        //potentially existing values with the same key from a higher level
        def downwardsFound = findValueDownwards(startDir, null)
        if(downwardsFound) {
          returnValue += downwardsFound
        }
        //iterate over the returned map and change some values
        returnValue = returnValue?.collectEntries { key, value -> 
          def newValue 
          if(value instanceof String) {
            //decrypt all potentially encrypted values
            newValue = value?.startsWith('sec:') ? textEncryptor.decrypt(value.split('sec:')[1]) : value
          }
          else {
            //use the toString representation of all other values
            newValue = value//.toString()
          }
          [key, newValue]
        }
      }
      //return the map alpahbetically sorted
      return returnValue?.sort()
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
              // we call putAll on allValues (which we just loaded from the current File 
              // in order to give prefference to lower level values (which are in this case in the 'result' variable
              allValues.putAll(result)
              result = allValues
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

    /**
     * scans through all supported configuration files downwards in the tree.
     * if a key is specified than all the single value is returned.
     * if the key is null, all supported config files are being read into a map which is then returned.
     *
     * @param dir The directory to search from
     * @param key the key to look for - leave this empty to get a map of all values
     * @return A String representing the value of the queried key or a Map representing all configuration values in case the key was null.
     */
    private def findValueDownwards(File dir, String key) {
      //Initialize the file Reader. This delegates reading to the correct reader implmentation
      //depending on the file suffix
      FileReader reader = FileReaderFactory.createNewInstance()
      def result
      //every file downwards recursively
      dir.eachFileRecurseDepthFirst {
        //only if a key was provided 
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
            //result may have more imprtant values in it
            //because of that we put all results in allValues and make this the new result
            //this way we ensure that lower level values get preffered
            allValues.putAll(result)
            result = allValues
          }
        }
      }
      return result
    }

    /**
     * searches in the filetree downwards and upwards for a given filename
     * @returns the file if it was found or null if not
     *
     */
    public File searchFile(File startDir, String name) {
      File result  
      startDir.eachFileRecurse(FileType.FILES) {
        if (!result && it.name == name) {
          result = it
        } 
      } 
      //if it was found searching downwards, return it
      if (result) {
        return result
      }
      //otherwhise search upwards
      else {
        def baseDir = new File(grailsApplication.config.getch.base.directory)
        return findFileUpwards(startDir, name, baseDir)
      }
      
    }

   /**
    * Writes a value into the directory of the given host with the given filename
    *
    * @param host the Hostname to write the value for
    * @param key the Key to write the value for
    * @param value the Value to be written
    * @param filename (optional) the filename to write too
    * @param addition (optional) an addition in case the host occurs multiple times in the tree
    */
   public void writeValue(String host, String key, String value, String filename, String addition = null) {
     //find the directory of of the given host
     File hostDir = findStartDir(host, addition)
     if (!hostDir) {
       throw new HostDirNotFoundException(host)
     }
     if (!filename) {
       filename = grailsApplication.config.getch.create.filename ?: 'config.properties'
     }
     File targetFile = new File(hostDir, filename)
     //Initialize the file writer. This delegates reading to the correct reader implmentation based on the filename suffix
     FileWriter writer = FileWriterFactory.createNewInstance()
     //Write the given value
     writer.writeValue(targetFile, key, value)
   }

   /**
    * Recursive helper method that searches for a file by name upwards in the tree
    * @returns a File object (if found) or null
    */ 
   private File findFileUpwards(File dir, String name, File baseDir) {
      def newFile = new File(dir,  name)
      //call the method recusively if the file does not exist in the current dir
      if (!newFile.exists() && dir.name != baseDir.name) {
        return findFileUpwards(dir.parentFile, name, baseDir)
      }
      //return the result if we found one or if we reached the top-level dir
      else {
        return newFile.exists() ? newFile : null
      }

   }


   /**
    * Takes the given template and runs it through a SimpleTemplateEngine.
    * Saves the content in a new File 
    *
    * @param template the template to resolve
    * @param  binding the binding used on the template
    */
   private String resolveTemplateFile(File template, Map binding) {
     def engine = new SimpleTemplateEngine()
     def fileContent = engine.createTemplate(template).make(binding).toString()
     //sadly the SimpleTemplateEngine always writes \n. 
     //If we work with Windows based files, this is not what we want.
     if(template.text.contains('\r\n')) {
       return fileContent.replaceAll('\n', '\r\n')
     }
     else {
       return fileContent
     }
   }

   /**
    * Finds the correct starting directory based on the provided 'dir' in combination with 
    * the 'addition' in case multiple 'dir's are found
    *
    * @param fromDir The directory name to find
    * @param addition The addition in case multipls 'dir's are found
    */
   private File findStartDir(String dir, String addition) {
    def baseDir = new File(grailsApplication.config.getch.base.directory)
    if (!baseDir.canRead()) {
      throw new IOException("Can not read ${baseDir.absolutePath}")
    }
    //find all matching directories
    def startDirs = []
    baseDir.eachDirRecurse {
      if( it.name == dir ) {
        startDirs << it
      }
    }
    //now find out which of the found directories to take (if we have multiple)
    def startDir = null
    if(startDirs && startDirs.size() > 1) {
      //find the first occurance which name matches also the provided addition
      startDir = startDirs.find {
        def absPath = it.absolutePath
        def pathWithoutBase = absPath - grailsApplication.config.getch.base.directory
        //match the path without the base against the provided addition
        pathWithoutBase =~ addition
      }
    }
    else if (startDirs && startDirs.size() == 1) {
      startDir = startDirs.first()
    }
    return startDir
  }

  /**
   * Private methd that checks for a specific File if it should be resolved by the template engine.
   * It does it by searching for a file called '.getchignore' in the directory of the given file.
   * If this file contains a line with the name of the given file than the result is 'false'
   *
   * @param file The File to be checked
   */
  public boolean treatAsTemplate(File file) {
    File getchIgnore = new File(file.parent, '.getchignore')
    if(getchIgnore.exists() && getchIgnore.text.contains(file.name)) {
      return false
    }
    else {
      return true 
    }
  }

}
