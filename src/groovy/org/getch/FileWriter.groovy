package org.getch

/**
 * Interface that defines how File writed should work in Getch
 * This is to allow clients to PUT values into files in their directory/subdirectory
 *
 * @author Robert Krombholz
 */
interface FileWriter {
 
  /**
   * This method should check the file, see if it can handle it, 
   * and if so take care of actually writing the value
   * If the key does not exist it should return null.
   * If the implementing reader does not feel responsible to handle a given
   * file-type (probably identified by the suffix of the filename) it should 
   * return the result of the nextReader.getValueForKey()
   *
   * @param file The file to work on
   * @param key The Key to look for in the file 
   */
  public void writeValue(File file, String key, String value);

}
