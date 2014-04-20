package org.getch

/**
 * Controller represening the HTTP query interface of Getch
 *  
 * @author Robert Krombholz 
 */
class QueryController {

  def fileSystemTreeService
  static defaultAction = "query"

  def query(String key) { 
    def value
    //try with the requesters IP/hostname if no param is given
    if (!params.host) {
      //get the hostname of the requester without the domainname
      def host = fileSystemTreeService.getHostnameFromIP(request.remoteAddr)
      //also get the fqdn
      def fqdn = fileSystemTreeService.getHostnameFromIP(request.remoteAddr, false)
      //try to get the value witht the hostname
      value = fileSystemTreeService.findValue(host, key) 
      if (!value) {
        //now try with the fqdn
        value = fileSystemTreeService.findValue(fqdn, key)
      }
    }
    //try with the given host param
    else {
      value = fileSystemTreeService.findValue(params.host, key)
    }
  
    if (!value) {
      render(status:404, text: "No value found for key: $key")
    } 
    else {
      render(text:value, contentType: 'text/plain')
    }
  }
}
