import static InstallerProperties.*

class PrintUtils {
    static final String SPACES = "   - "
    static final String DECOR = "----------"

    static printlns(String text) {
        println(SPACES + text)
    }

    static printlnsDebug(String text) {
        if (p[PRODUCTS_LOG_DEBUG] == "true") println(SPACES + "DEBUG: " + text)
    }

    static prints(String text) {
        print(SPACES + text)
    }

    static printlndecor(String text) {
        println("${DECOR}$text${DECOR}")

    }
}
