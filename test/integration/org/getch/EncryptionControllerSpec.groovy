package org.getch

import spock.lang.*

/**
 * Integration test for EncryptionController
 */
class EncryptionControllerSpec extends Specification {

    def textEncryptor 
 
    def setup() {
    }

    def cleanup() {
    }

    void "test encrypt value"() {
      setup:
      def controller = new EncryptionController()
      controller.textEncryptor = textEncryptor
      when:
      controller.request.addParameter('value', 'testvalue')
      controller.request.method = 'POST'
      controller.encrypt()
      then:
      textEncryptor.decrypt(controller.response.contentAsString) == 'testvalue'
    }
}
