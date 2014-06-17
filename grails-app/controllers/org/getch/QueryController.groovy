package org.getch

import grails.converters.JSON

/**
 * Controller represening the HTTP query interface of Getch
 *  
 * @author Robert Krombholz 
 */
class QueryController {

  def fileSystemTreeService
  def nameResolutionService
  static defaultAction = "query"
  static allowedMethods = [query:'GET', list:'GET']

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
      log.info("query from ${request.remoteAddr} - resolved to hostname: $host for key: $key")
      if (!value) {
        //try with the fully qualified hostname
        def fqdn = nameResolutionService.getHostnameFromIP(request.remoteAddr, false)
        log.info("query from ${request.remoteAddr} - no value found for key $key and host: $host. Now trying with fqdn: $fqdn")
        value = fileSystemTreeService.findValue(fqdn, key, params.addition)
      }
    }
    //try with the given host param
    else {
      def allowedProxies = grailsApplication.config.getch.trusted.proxies
      if (allowedProxies.contains(request.remoteAddr) || 
          allowedProxies.contains(nameResolutionService.getHostnameFromIP(request.remoteAddr, false))) {
        value = fileSystemTreeService.findValue(params.host, key, params.addition)
        log.info("query from ${request.remoteAddr} - received query from allowed Proxy  for key: $key and context of host: ${params.host}")
      }
      else {
        log.error("query from ${request.remoteAddr} - received proxied query from unauthorized host (key=$key)")
        render(status:403, text: "Querying host (${request.remoteAddr}) not configured as trusted proxy server.")
      }
    }
  
    if (!value) {
      render(status:404, text: "No value found for key: $key in the context of ${request.remoteAddr}")
    } 
    else {
      if(value instanceof Map) {
        log.info("query from ${request.remoteAddr} - found matching File for key: $key")
        byte[] file = value.content.bytes
        response.setContentType("application/octet-stream")
        response.setHeader("Content-disposition", "attachment;filename=${value.filename}")
        response.setContentLength(file.size())
        response.outputStream << file
      }
      else {
        log.info("query from ${request.remoteAddr} - found value for key: $key")
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
      log.info("list request from ${request.remoteAddr} - resolved to hostname: $host")
      //in case we didn't find anything for the given host
      if(!values) {
        //try with the fully qualified hostname
        def fqdn = nameResolutionService.getHostnameFromIP(request.remoteAddr, false)
        values = fileSystemTreeService.listValues(fqdn, params.addition)
        log.info("list request from ${request.remoteAddr} - no values found for host: $host. Now trying with fqdn: $fqdn")
      }
    }
    else {
      def allowedProxies = grailsApplication.config.getch.trusted.proxies
      if (allowedProxies.contains(request.remoteAddr) ||
          allowedProxies.contains(nameResolutionService.getHostnameFromIP(request.remoteAddr, false))) {
        values = fileSystemTreeService.listValues(params.host, params.addition)
        log.info("list request from ${request.remoteAddr} - received list request from allowed Proxy for host: ${params.host}")
      }
      else {
        log.error("list request from ${request.remoteAddr} - received proxied list request from unauthorized host ${request.remoteAddr}")
        render(status:403, text: "Querying host (${request.remoteAddr}) not configured as trusted proxy server.")
      }
    }
    if (!values) {
      log.info("list request from ${request.remoteAddr} - no values found for given host")
      render(status:404, text: "No values found for host with IP ${request.remoteAddr}. It this IP resolvable by a DNS reverse lookup from the Getch host?")
    } 
    else {
      log.info("list request from ${request.remoteAddr} - found values and returning them to the querying host")
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
        json { render values as JSON }
      }
    }
  }
}
