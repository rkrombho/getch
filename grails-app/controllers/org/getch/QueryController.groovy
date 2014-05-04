package org.getch

import grails.converters.JSON
import grails.converters.XML

/**
 * Controller represening the HTTP query interface of Getch
 *  
 * @author Robert Krombholz 
 */
class QueryController {

  def fileSystemTreeService
  static defaultAction = "query"


  /**
   * queries a single value from the hierarchy
   * @key the configuration key to look for
   */
  def query(String key) { 
    def value
    //try with the requesters IP/hostname if no param is given
    if (!params.host) {
      //get the hostname of the requester without the domainname
      def host = fileSystemTreeService.getHostnameFromIP(request.remoteAddr)
      //try to get the value witht the hostname
      value = fileSystemTreeService.findValue(host, key) 
      if (!value) {
        //try with the fully qualified hostname
        def fqdn = fileSystemTreeService.getHostnameFromIP(request.remoteAddr, false)
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

  /**
   * lists all available configuration key-value pairs found for the 
   * querying host (or the params.host)
   */
  def list(){
    def values
    if(!params.host) {
      //get the hostname of the requester without the domainname
      def host = fileSystemTreeService.getHostnameFromIP(request.remoteAddr)
      println host
      values = fileSystemTreeService.listValues(host) 
      //in case we didn't find anything for the given host
      if(!values) {
        //try with the fully qualified hostname
        def fqdn = fileSystemTreeService.getHostnameFromIP(request.remoteAddr, false)
        values = fileSystemTreeService.listValues(fqdn)
      }
    }
    else {
      values = fileSystemTreeService.listValues(params.host)
    }
    if (!values) {
      render(status:404, text: "No value found for the queried host")
    } 
    else {
      withFormat {
        html { render(text:values.collect{key, value -> "${key}=${value}"}.join('\n'), contentType: "text/plain") }
        json { render books as JSON }
        xml { render books as XML }
      }
    }
  }
}
