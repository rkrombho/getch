package org.getch

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(NameResolutionService)
class NameResolutionServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test get hostname from IP"() {
      setup:
      def service = new NameResolutionService()
      expect:
      service.getHostnameFromIP('127.0.0.1') == 'localhost'
    }

}
