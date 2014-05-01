package org.getch

import grails.test.mixin.TestFor
import spock.lang.Specification
import org.apache.commons.io.FileUtils

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
      FileUtils.deleteDirectory(new File(workdir));
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

    void "test list action"() {
      setup:
      controller.fileSystemTreeService = new FileSystemTreeService(grailsApplication:grailsApplication) 
      def yamlFile = new File(grailsApplication.config.getch.base.directory + '/common/dc1/mydepartment/myproduct/web/localhost/config.yaml')
      yamlFile.text='''
testkey7: testvalue7
testkey8: testvalue8
'''
      def propertiesFile = new File(grailsApplication.config.getch.base.directory + '/common/dc1/mydepartment/myproduct/web/config.properties')
      propertiesFile.text='''
testkey9=testvalue9
testkey10=testvalue10
'''
      when:
      request.remoteAddr = '127.0.0.1'
      controller.list()
      then:
      response.status == 200
      response.text == '''testkey1=testvalue1\ntestkey10=testvalue10\ntestkey7=testvalue7\ntestkey8=testvalue8\ntestkey9=testvalue9'''
      cleanup:
      yamlFile.delete()
      propertiesFile.delete()
    }
}
