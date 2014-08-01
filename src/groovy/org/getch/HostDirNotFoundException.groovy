package org.getch

/**
 * Exception to be thrown in situation where the queries hostname is not
 * found in the Getch hierarchy.
 */
public class HostDirNotFoundException extends RuntimeException {
  public HostDirNotFoundException(String host) {
    super("Host with name $host not found in the Getch hierarchy")
  }
}

 
