package org.getch

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(QueryController)
class QueryControllerSpec extends Specification {

    def setup() {
      //mock the config
      grailsApplication.config.getch.base.directory = System.getProperty("java.io.tmpdir") + '/getchtest' 
      def workdir = grailsApplication.config.getch.base.directory
      //create a single-leg directory hierarchy with 'localhost' because we use this as our hostname in the test
      def directory = new File(grailsApplication.config.getch.base.directory + '/common/dc1/mydepartment/myproduct/web/localhost')
      directory.mkdirs()
      //create a few property files on different levels using the value to inidicate the layer
      new File(directory, 'config.properties').text = """
testkey1=testvalue1"""
    }

    def cleanup() {
      def workdir = System.getProperty("java.io.tmpdir") + '/getchtest'
      new File(workdir).delete()
    }

    void "test query single value from properties file"() {
      setup:
      controller.fileSystemTreeService = new FileSystemTreeService(grailsApplication:grailsApplication) 
      when:
      request.remoteAddr = '127.0.0.1'
      controller.query('testkey1')
   
      then:
      response.text == 'testvalue1'
      response.contentType == 'text/plain;charset=utf-8'
    }

    void "test query with non-existing key"() {
      setup:
      controller.fileSystemTreeService = new FileSystemTreeService(grailsApplication:grailsApplication) 
      when:
      controller.query('blahkey')
      then:
      response.text == 'No value found for key: blahkey'
      response.status == 404
    }
}
