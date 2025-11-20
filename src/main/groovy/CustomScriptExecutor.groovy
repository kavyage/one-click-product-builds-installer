import static InstallerProperties.*
import static PrintUtils.*

class CustomScriptExecutor {

    static void executeScripts() {
        String customDir = p[PRODUCTS_CUSTOMIZATION_DIR]
        def files = CmdUtils.getListFilesByRegExTemplate("$customDir", ".*groovy")
        printlndecor("Customization scripts executing")
        if (files == null || files.length == 0) {
            printlns("There are no scripts found in ${new File(customDir).getAbsolutePath()}")
        }
        for (File file : files) {
            try {
                printlns("Executing ${file.getAbsolutePath()}")
                Script customizations = new GroovyShell().parse(file)
                customizations.execute()
                printlns("done.")
            } catch (Exception e) {
                printlns("failed! ${e.printStackTrace()}")
            }
        }
        printlndecor("Customization scripts executed")

    }
}
