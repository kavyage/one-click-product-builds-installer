import org.apache.commons.cli.Option
import org.apache.commons.cli.Options

class CmdOptions {
    static Options parse() {
        Options options = new Options()

        Option install = new Option("i", "install", true, "Install all products from specified location 'dev|release|local': dev=host, release=\\\\host\\release_builds\\dir path, local=%RootPath%\\builds")
        install.setRequired(false)
        options.addOption(install)

        Option searchInOtherLocations = new Option("sol", "search-in-other-locations", false, "If you choose dev location for builds look up, then you can use this option to switch to release location in case the build is not found in dev location")
        install.setRequired(false)
        options.addOption(searchInOtherLocations)

        Option uninstall = new Option("u", "uninstall", false, "Uninstall all products.")
        uninstall.setRequired(false)
        options.addOption(uninstall)

        Option printState = new Option("s", "state", false, "Print the current state of installed products.")
        options.addOption(printState)

        Option exclude = new Option("e", "exclude", true, "Comma separated list of products to exclude from install/uninstall process. List of supported products: "
                + ProductInstaller.productHandlers.keySet())
        options.addOption(exclude)

        Option include = new Option("pi", "include", true, "Comma separated list of products to include in install/uninstall process. List of supported products: "
                + ProductInstaller.productHandlers.keySet())
        options.addOption(include)

        Option custom = new Option("c", "customization", true, "Execute customization scripts from the folder specified in argument, for example, 'acunetix'.")
        options.addOption(custom)

        Option branches = new Option("b", "branch", true, "Custom branches. Works only with '-i dev' option. Values example: P3_1111_bugfip3,P4_ui_fip3")
        options.addOption(branches)

        Option debug = new Option("d", "debug", false, "Debug logging level.")
        options.addOption(debug)

        Option help = new Option("h", "help", false, "Print this help.")
        options.addOption(help)

        Option version = new Option("v", "version", true, "Products version")
        options.addOption(version)

        Option skipStart = new Option("ss", "skip-start", false, "Skip products start")
        options.addOption(skipStart)

        Option stopAll = new Option("sa", "stop-all-products", false, "Stop all products before installation")
        options.addOption(stopAll)

        return options
    }
}
