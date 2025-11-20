import org.apache.commons.cli.*

import static InstallerProperties.*
import static PrintUtils.printlndecor
import static ProductInstaller.*
import static ProductState.*

static init() {
    def devPath = ["\\\\host\\Builds", "/mnt/smb/"]
    def releasePath = ["\\\\host\\release_builds\\biolds path", "/mnt/smb2/"]

    p[PRODUCTS_VERSION] = "0.0.0" // if not selected the current build will be installed
    p[PRODUCTS_LOCATION] = "dev" // local|dev|release
    p[PRODUCTS_SEARCH_IN_OTHER_LOCATIONS] = "false" // local|dev|release
    p[PRODUCTS_EXCLUDE] = "P1" // list of products comma separated
    p[PRODUCTS_INCLUDE] = "" // list of products comma separated
    p[PRODUCTS_LOCATION_DEV] = CmdUtils.getPathForOS(devPath)
    p[PRODUCTS_LOCATION_RELEASE] = CmdUtils.getPathForOS(releasePath)
    p[PRODUCTS_LOCATION_LOCAL] = "${RootPath}${File.separator}builds"
    p[PRODUCTS_CLEANUP] = "false"
//        p[PRODUCTS_CUSTOMIZATION_DIR] = null
    p[PRODUCTS_LOG_DEBUG] = "false"
    p[PRODUCTS_CUSTOM_BRANCH] = "master"
    return p
}

static initProperties(CommandLine cmd, def props) {
    if (cmd.hasOption("debug")) {
        props[PRODUCTS_LOG_DEBUG] = "true"
    }
    if (cmd.hasOption("branch")) {
        props[PRODUCTS_CUSTOM_BRANCH] = cmd.getOptionValue("branch")
    }
    if (cmd.hasOption("version")) {
        props[PRODUCTS_VERSION] = cmd.getOptionValue("version")
    }
    if (cmd.hasOption("uninstall")) {
        props[PRODUCTS_CLEANUP] = "true"
    }
    if (cmd.hasOption("customization")) {
        props[PRODUCTS_CUSTOMIZATION_DIR] = cmd.getOptionValue("customization")
    }
    if (cmd.hasOption("skip-start")) {
        props[PRODUCTS_SKIP_START] = "true"
    }

    def location = cmd.getOptionValue("install")
    if (location != null) props[PRODUCTS_LOCATION] = location

    if (cmd.hasOption("search-in-other-locations")) {
        props[PRODUCTS_SEARCH_IN_OTHER_LOCATIONS] = "true"
    }

    def exclude = cmd.getOptionValue("exclude")
    if (exclude != null) props[PRODUCTS_EXCLUDE] = exclude

    def include = cmd.getOptionValue("include")
    if (include != null) props[PRODUCTS_INCLUDE] = include

}

static void main(String[] args) throws IOException, InterruptedException {
    def props = init()
    Options options = CmdOptions.parse()
    CommandLineParser parser = new DefaultParser()
    HelpFormatter formatter = new HelpFormatter()
    CommandLine cmd
    try {
        cmd = parser.parse(options, args)
        printlndecor("Initializing the tool")
        for (final def opt in options.getOptions()) {
            println opt.getLongOpt()
            if (cmd.hasOption(opt.getLongOpt())) {
                println "    provided: " + cmd.hasOption(opt.getLongOpt())
                println "    value: " + cmd.getOptionValue(opt.getOpt())
            }
        }
        initProperties(cmd, props)
        printlndecor("Initializing the tool done")

        if (cmd.hasOption("branch")) {
            try {
                getCustomBranchMap()
            } catch (IllegalArgumentException ignore) {
                println("Incorrect option '-b ${props[PRODUCTS_CUSTOM_BRANCH]}'")
                System.exit(1)
            }
        }
        if (cmd.hasOption("state")) {
            printProductsState("Current")
        }
        if (cmd.hasOption("stop-all-products")) {
            ProductInstaller.stopAll()
        }
        if (cmd.hasOption("uninstall")) {
            ProductUninstaller.uninstallAll()
        }
        boolean successInstalled = true
        if (cmd.hasOption("install")) {
            successInstalled = installAll()
        }
        if (cmd.hasOption("customization") && !cmd.hasOption("install")) {
            ProductInstaller.customize()
        }
        if (cmd.hasOption("help")) {
            printHelpAndExit(formatter, options)
        }
        if (cmd.getOptions().length == 0) {
            println("Default mode: $p")
            successInstalled = installAll()
        }
        if (cmd.hasOption("state") && (cmd.hasOption("uninstall") || cmd.hasOption("install"))) {
            printProductsState("Updated")
        }
        if (!successInstalled) System.exit(1)
    } catch (e) {
        println(e.getMessage())
        printHelpAndExit(formatter, options)
    }
}

private static void printHelpAndExit(HelpFormatter formatter, Options options) {
    formatter.printHelp("groovy -cp \"lib/*\"  [default mode: $p]", options)
    System.exit(1)
}

