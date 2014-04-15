package org.getch

import org.ho.yaml.Yaml

/**
 * Implementation of a Reader for Yaml files.
 * Supported are base-level mapping and mappings to sequences
 *
 * @author Robert Krombholz
 */
class YamlFileReader implements FileReader {
  //used to implement the Chain of Responsibilities pattern
  private FileReader nextReader
  public YamlFileReader(FileReader reader) {
    nextReader = reader
  }
  
  /**
   * returns the value of the given Key from the File 
   *
   * @param file The file to lookup the key in
   * @param key The key to load from the Properties file
   */
  public String getValueForKey(File file, String key) {
    if (file.name.endsWith('.yaml') || file.name.endsWith('.yml')) {
      def yaml = Yaml.load(file)
      def value = yaml."$key"
      //if the key references a collection than join this with a ,
      return value instanceof Collection ? value.join(',') : value
    }
    else {
      return nextReader?.getValueForKey(file, key)
    }
  }

  public String getMimeType() {
    return 'text/yaml'
  }
}

