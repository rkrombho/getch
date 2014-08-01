package org.getch

/**
 * Implementation of a Writer for Java Properties files
 *
 * @author Robert Krombholz
 */
class PropertyFileWriter implements FileWriter {
  //used to implement the Chain of Responsibilities pattern
  private FileWriter nextWriter
  public PropertyFileWriter(FileWriter writer) {
    nextWriter = writer
  }
  
  /**
   * @Inheritdoc
   */
  public void writeValue(File file, String key, String value) {
    if (file.name.endsWith('.properties')) {
      def props = new Properties()
      if(file.exists()) {
        //load it
        props.load(file.newInputStream())
      }
      //set the value
      props."$key" = value
      //persist it back to disk
      props.store(file.newOutputStream(), null)
    }
    else {
      nextWriter?.writeValue(file, key, value)
    }
  }
}

