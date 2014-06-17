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
      //mock what we normally too in Bootstrap.groovy
      FileMetaAddition.addMethods()
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
      controller.nameResolutionService = new NameResolutionService()
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
      controller.nameResolutionService = new NameResolutionService()
      when:
      request.remoteAddr = '127.0.0.1'
      controller.query('blahkey')
      then:
      response.text == 'No value found for key: blahkey in the context of 127.0.0.1'
      response.status == 404
    }

    void "test list action"() {
      setup:
      controller.fileSystemTreeService = new FileSystemTreeService(grailsApplication:grailsApplication) 
      controller.nameResolutionService = new NameResolutionService()
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

    void "test query with additions to match hostname uniquely"() {
      setup:
      controller.fileSystemTreeService = new FileSystemTreeService(grailsApplication:grailsApplication) 
      controller.nameResolutionService = new NameResolutionService()
      def directory1 = new File(grailsApplication.config.getch.base.directory + '/common/dc1/mydepartment/myproduct/web/localhost/')
      directory1.mkdirs()
      new File(directory1, 'test.properties').text = '''
testkey55=webvalue
'''
      def directory2 = new File(grailsApplication.config.getch.base.directory + '/common/dc1/mydepartment/myproduct/app/localhost/')
      directory2.mkdirs()
      new File(directory2, 'test.properties').text = '''
testkey55=appvalue
'''
      when:
      request.remoteAddr = '127.0.0.1'
      params.addition = addition
      controller.query(key)
      then:
      response.status == 200
      response.text == value
      where:
      key | addition || value
      'testkey55' | 'web' || 'webvalue'
      'testkey55' | 'app' || 'appvalue'

    }

    void "test list with additions to match hostname uniquely"() {
      setup:
      controller.fileSystemTreeService = new FileSystemTreeService(grailsApplication:grailsApplication) 
      controller.nameResolutionService = new NameResolutionService()
      def directory1 = new File(grailsApplication.config.getch.base.directory + '/common/dc1/mydepartment/myproduct/web/localhost/')
      directory1.mkdirs()
      new File(directory1, 'test.properties').text = '''
testkey56=webvalue
'''
      def directory2 = new File(grailsApplication.config.getch.base.directory + '/common/dc1/mydepartment/myproduct/app/localhost/')
      directory2.mkdirs()
      new File(directory2, 'test.properties').text = '''
testkey56=appvalue
'''
      when:
      request.remoteAddr = '127.0.0.1'
      params.addition = addition
      controller.list()
      then:
      response.status == 200
      response.text.contains(value)
      where:
      addition || value
      'web' || 'webvalue'
      'app' || 'appvalue'
  }
}
