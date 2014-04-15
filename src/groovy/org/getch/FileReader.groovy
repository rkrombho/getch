package org.getch

/**
 * Interface that defines how FileRead should work in Getch
 * in order to support mutliple configuration files in the hierarchy
 *
 * @author Robert Krombholz
 */
interface FileReader {
 
  /**
   * This methos should check the file, see if it can handle it, 
   * and if so return the value of the provided key.
   * If the key does not exist it should return null.
   * If the implementing reader does not feel responsible to handle a given
   * file-type (probably identified by the suffix of the filename) it should 
   * return the result of the nextReader.getValueForKey()
   *
   * @param file The file to work on
   * @param key The Key to look for in the file 
   */
  public String getValueForKey(File file, String key);

  /**
   * Should return the MIME type for the file types that an implementing 
   * reader feels responsible for
   * @return The MIME type for files matching this FileReader
   */
  public String getMimeType();
}
