package org.getch

/**
 * Controller to encrypt values to be placed inside configuration files
 */
class EncryptionController {
    def textEncryptor

    def encrypt(String value) {
      render(text: textEncryptor.encrypt(value), contentType: "text/plain")
    }
}
