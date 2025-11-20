import org.apache.commons.io.FileUtils

import static CmdUtils.killProcess
import static InstallerProperties.get
import static InstallerProperties.getProductDirs
import static PrintUtils.*

class BaseProduct {

    protected String product
    protected String productFolder

    BaseProduct(String product) {
        this(product, product)
    }

    BaseProduct(String product, String productFolder) {
        this.product = product
        this.productFolder = productFolder
    }

    public void start() {
//        throw new UnsupportedOperationException("Not implemented")
    }

    public void stop() {
//        throw new UnsupportedOperationException("Not implemented")
    }

    public void customize(String scriptsPath) {
        executeScripts(scriptsPath)
    }

    public void install() {
        prints("Killing msiexec  ...")
        for (String pid : CmdUtils.getPID("msiexec", "")) killProcess(pid)
        for (String pid : CmdUtils.getPID("CustomSetup.exe", "")) killProcess(pid)
        installProduct();
        installHF();
        printlndecor("$product installed")
        println()
    }

    public void installProduct() {
        boolean searchInOtherLocations = Boolean.parseBoolean(get(PRODUCTS_SEARCH_IN_OTHER_LOCATIONS))
        def buildPath = ProductInstaller.findBuildPath(product, searchInOtherLocations)
        if (buildPath == null) {
            throw new Exception("*** ERROR ***  Failed to install $product (installation package is not found)")
        }
        if (!ProductInstaller.checkPrerequisites(product)) throw new Exception("*** ERROR ***  Failed to install $product (prerequisites are not satisfied)")

        ProductInstaller.install(product, buildPath)

        if (!ProductState.isProductInstalled(product)) {
            throw new Exception("Error installing $product to " + getProductDirs().get(product))
        }
    }

    public void installHF() {
        boolean searchInOtherLocations = Boolean.parseBoolean(get(PRODUCTS_SEARCH_IN_OTHER_LOCATIONS))
        def buildPath = ProductInstaller.findBuildPath(product, searchInOtherLocations)
        if (buildPath == null) {
            throw new Exception("*** ERROR ***  Failed to install $product (installation package is not found)")
        }
        if (InstallerProperties.getLocationByPackagePath(buildPath).equalsIgnoreCase("release")) {
            def hotfixPath = ProductInstaller.findHotfixPath(buildPath, this.product)
            if (hotfixPath != null) {
                ProductInstaller.installHotfix(hotfixPath.getAbsolutePath())
            }
        }
    }

    public void uninstall() {
        prints("Killing msiexec  ...")
        for (String pid : CmdUtils.getPID("msiexec", "")) killProcess(pid)
        prints("Removing product  ...")
        ProductUninstaller.removeProduct(product)
        prints("Deleting product folder  ...")
        deleteFolder(System.getenv("RootPath"), productFolder)
    }

    void executeScripts(String customDir) {
        def baseScriptsFolder = new File(getClass().protectionDomain.codeSource.location.path).parent
        def files = CmdUtils.getListFilesByRegExTemplate(new File(baseScriptsFolder, "customization/$customDir/$product").getCanonicalPath(), ".*groovy")
        printlndecor("Customization scripts executing")
        if (files == null || files.length == 0) {
            printlns("There are no scripts found in ${new File(customDir).getAbsolutePath()} for product $product")
        }
        for (File file : files) {
            try {
                printlndecor("Executing ${file.getAbsolutePath()}")
//                def bindings = new Binding()
//                bindings.setVariable("p", InstallerProperties.p)
//                bindings.setVariable("InstallerProperties.p", InstallerProperties.p)
//                Script customizations = new GroovyShell(bindings).parse(new GroovyCodeSource(file, null),bindings)
                Script customizations = new GroovyShell(this.class.getClassLoader()).parse(file)
                customizations.invokeMethod("execute", new Object[]{file})
                printlndecor("Executing ${file.getAbsolutePath()} done.")
            } catch (Exception e) {
                printlndecor("Executing ${file.getAbsolutePath()} failed! ${e.printStackTrace()}")
            }
        }
        printlndecor("Customization scripts executed")

    }

    protected static void deleteFolder(String folder, String profilesFilter) {
        prints("Deleting " + profilesFilter + " from" + folder + "...")
        def profiles = CmdUtils.getListFilesInFolderByRegExTemplate(folder, profilesFilter)
        prints("Deleting " + profilesFilter + " from" + folder + ": found " + profiles)
        if (profiles != null) {
            for (final def profileFolder in profiles) {
                def pids = ProcessFileHandlerExecutor.getHandlerPIDsByFilePath(profileFolder)
                println "" + profileFolder + " " + pids
                if (pids != null) {
                    for (final def pid in pids) {
                        CmdUtils.killProcess(pid)
                    }
                }
                FileUtils.deleteDirectory(profileFolder)
            }
        }
    }

}
