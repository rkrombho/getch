package org.getch

/**
 * Factory class that abstracts the reation of the various different writers
 * as per the Chain of Responsibiliry pattern 
 * see: http://groovy.codehaus.org/Chain+of+Responsibility+Pattern
 *
 * @author Robert Krombholz
 */
class FileWriterFactory {

  public static FileWriter createNewInstance() {
    //currently only Properties are supported for writing activities
    return new PropertyFileWriter()
  }
}
