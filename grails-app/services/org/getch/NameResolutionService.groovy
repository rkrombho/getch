package org.getch

import java.net.InetAddress

class NameResolutionService {

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
