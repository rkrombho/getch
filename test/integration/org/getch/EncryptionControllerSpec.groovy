package org.getch



import spock.lang.*

/**
 *
 */
class EncryptionControllerSpec extends Specification {

   def textEncryptor 
 
    def setup() {
    }

    def cleanup() {
    }

    void "test encrypt value"() {
      setup:
      def controller = new EncryptionController(textEncryptor:textEncryptor)
      when:
      controller.encrypt('testvalue')
      then:
      textEncryptor.decrypt(controller.response.contentAsString) == 'testvalue'
    }
}
