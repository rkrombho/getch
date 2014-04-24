package org.getch

/**
 * Controller to encrypt values to be placed inside configuration files
 */
class EncryptionController {
    static allowedMethods = [encrypt:'POST']
  
    def textEncryptor

    def encrypt() {
      if(!params.value) {
        render(status:404, text: "URL parameter 'value' not provided.")
      }
      render(text: textEncryptor.encrypt(params.value), contentType: "text/plain")
    }
}
