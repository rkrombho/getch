import org.jasypt.util.text.BasicTextEncryptor
// Place your Spring DSL code here
beans = {
  textEncryptor(BasicTextEncryptor) {
    password = grailsApplication.config.getch.encryption.password
  }
}
