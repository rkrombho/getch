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
  def nameResolutionService
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
      def host = nameResolutionService.getHostnameFromIP(request.remoteAddr)
      //try to get the value witht the hostname
      value = fileSystemTreeService.findValue(host, key, params.addition) 
      if (!value) {
        //try with the fully qualified hostname
        def fqdn = nameResolutionService.getHostnameFromIP(request.remoteAddr, false)
        value = fileSystemTreeService.findValue(fqdn, key, params.addition)
      }
    }
    //try with the given host param
    else {
      def allowedProxies = grailsApplication.config.getch.trusted.proxies
      if (allowedProxies.contains(request.remoteAddr) || 
          allowedProxies.contains(nameResolutionService.getHostnameFromIP(request.remoteAddr, false))) {
        value = fileSystemTreeService.findValue(params.host, key, params.addition)
      }
      else {
        log.error("received proxied query from unauthorized host ${request.remoteAddr} (key=$key)")
        render(status:403, text: "Querying host (${request.remoteAddr}) not configured as trusted proxy server.")
      }
    }
  
    if (!value) {
      render(status:404, text: "No value found for key: $key")
    } 
    else {
      if(value instanceof Map) {
        byte[] file = value.content.bytes
        response.setContentType("application/octet-stream")
        response.setHeader("Content-disposition", "attachment;filename=${value.filename}")
        response.setContentLength(file.size())
        response.outputStream << file
      }
      else {
        render(text:value, contentType: 'text/plain')
      }
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
      def host = nameResolutionService.getHostnameFromIP(request.remoteAddr)
      values = fileSystemTreeService.listValues(host, params.addition) 
      //in case we didn't find anything for the given host
      if(!values) {
        //try with the fully qualified hostname
        def fqdn = nameResolutionService.getHostnameFromIP(request.remoteAddr, false)
        values = fileSystemTreeService.listValues(fqdn, params.addition)
      }
    }
    else {
      def allowedProxies = grailsApplication.config.getch.trusted.proxies
      if (allowedProxies.contains(request.remoteAddr) ||
          allowedProxies.contains(nameResolutionService.getHostnameFromIP(request.remoteAddr, false))) {
        values = fileSystemTreeService.listValues(params.host, params.addition)
      }
      else {
        log.error("received proxied query from unauthorized host ${request.remoteAddr} (key=$key)")
        render(status:403, text: "Querying host (${request.remoteAddr}) not configured as trusted proxy server.")
      }
    }
    if (!values) {
      render(status:404, text: "No value found for the queried host")
    } 
    else {
      //iterate over the returned map and change some values
      values = values?.collectEntries { key, value ->
        def newValue
        if(value instanceof String) {
          //decrypt all potentially encrypted values
          newValue = value?.startsWith('sec:') ? textEncryptor.decrypt(value.split('sec:')[1]) : value
        }
        else if (value instanceof Collection) {
          //joing potential collection to a comma seperated string
          newValue = value.join(',')
        }
        else {
          //use the toString representation of all other values
          newValue = value.toString()
        }
        [key, newValue]
      }

      withFormat {
        html { render(text:values.collect{key, value -> "${key}=${value}"}.join('\n'), contentType: "text/plain") }
        json { render books as JSON }
        xml { render books as XML }
      }
    }
  }
}
