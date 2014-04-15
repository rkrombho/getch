package org.getch

/**
 * Factory class that abstracts the reation of the various different readers
 * as per the Chain of Responsibiliry pattern 
 * see: http://groovy.codehaus.org/Chain+of+Responsibility+Pattern
 *
 * @author Robert Krombholz
 */
class FileReaderFactory {

  public static FileReader createNewInstance() {
    //currently we only have one implementation
    return new PropertyFileReader()
  }
}
