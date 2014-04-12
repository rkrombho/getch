package org.getch

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(FileSystemTreeService)
class FileSystemTreeServiceSpec extends Specification {

    def setup() {
      //mock the config
      grailsApplication.config.getch.base.directory = System.getProperty("java.io.tmpdir") + '/getchtest' 
      def workdir = grailsApplication.config.getch.base.directory
      //create a single-leg directory hierarchy
      def directory = new File(grailsApplication.config.getch.base.directory + '/common/dc1/mydepartment/myproduct/web/hostname1')
      directory.mkdirs()
      //create a few property files on different levels using the value to inidicate the layer
      new File(directory, 'config.properties').text = """
testkey1=hostname1_testvalue1"""
      new File( workdir + '/common/dc1/mydepartment/myproduct/web/config.properties').text = """
testkey1=web_testvalue1
testkey2=web_testvalue2"""
      new File( workdir + '/common/dc1/mydepartment/myproduct/config.properties').text = """
testkey1=myproduct_testvalue1
testkey2=myproduct_testvalue2
testkey3=myproduct_testvalue3"""
    }

    def cleanup() {
      def workdir = System.getProperty("java.io.tmpdir") + '/getchtest'
      new File(workdir).delete()
    }
  
    void "test recursive upwards searching property"(String host, String key, String value) {
      setup:
      def service = new FileSystemTreeService(grailsApplication:grailsApplication)
      expect: 
      service.findValue(host, key) == value
      where:
      host | key || value 
      'hostname1' | 'testkey1' || 'hostname1_testvalue1'
      'hostname1' | 'testkey2' || 'web_testvalue2'
      'hostname1' | 'testkey3' || 'myproduct_testvalue3'
      'hostname1' | 'testkey4' || null
      'hostname2' | 'testkey2' || null
      'hostname2' | 'testkey1' || null

    }
}
