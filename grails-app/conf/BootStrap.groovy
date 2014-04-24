import org.apache.commons.logging.LogFactory


class BootStrap {
    private static final log = LogFactory.getLog(this)
    def grailsApplication

    def init = { servletContext ->
      def config = grailsApplication.config.getch.base.directory
      def encPw = grailsApplication.config.getch.encryption.password
      if (!config || !encPw) {
        log.severe("Could not find required configuration values 'getch.base.directory' or 'getch.encryption.password'. Make sure you providen your configuration file in the System Property: 'getch.config.location'.")
        throw new IOException("Could not find required configuration values 'getch.base.directory' or 'getch.encryption.password'.")
      }
    }
    def destroy = {
    }
}
