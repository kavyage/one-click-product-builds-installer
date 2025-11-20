import com.github.junrar.Junrar
import com.github.junrar.exception.RarException
import org.apache.commons.io.IOUtils

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

import static CmdUtils.*
import static InstallerProperties.*
import static PrintUtils.*

class ProductInstaller {
    static String copiedFile = ""
    static Map<String, BaseProduct> productHandlers = new HashMap<>()
    static {
        productHandlers.put(P_P1, new BaseProduct(P_P1))
        productHandlers.put(P_P2, new P2Product())
        productHandlers.put(P_P3, new P3Product())
        productHandlers.put(P_P4, new BaseProduct(P_P4))
        productHandlers.put(P_P5, new P5Product())
        productHandlers.put(P_P6, new P6Product())
        productHandlers.put(P_P7, new BaseProduct(P_P7))
        productHandlers.put(P_P7II, new BaseProduct(P_P7II))
        productHandlers.put(P_P7EXT, new BaseProduct(P_P7EXT))
        productHandlers.put(P_P8, new P8Product())
        productHandlers.put(P_P9, new P9Product())
        productHandlers.put(P_P9II, new P9IIProduct())
        productHandlers.put(P_P9EXT, new BaseProduct(P_P9EXT, "P3ModuleForP9Extension"))

    }

    static void customize() {
        String customDir = p[PRODUCTS_CUSTOMIZATION_DIR]
        println(customDir)
        if (customDir != null) {
            def products = getProductsToInstall()
            println "Products to customize: " + products
            for (String product : products) {
                printlndecor("Customizing $product")
                def productHandler = productHandlers.get(product)
                productHandler.stop()
                productHandler.customize(customDir)
                boolean isSkipStart = p[PRODUCTS_SKIP_START] != null ? Boolean.parseBoolean(p[PRODUCTS_SKIP_START]) : false
                if (!isSkipStart) {
                    productHandler.start()
                }
                printlndecor("$product customized")
                println()
            }
        }
    }

    static void stopAll() {
        for (final def productHandler in productHandlers.values()) {
            printlndecor("Stopping $productHandler.product")
            try {
                productHandler.stop()
            } catch (e) {
                System.err.println("Failed to stop $productHandler.product")
                e.printStackTrace(e)
            }
            printlndecor("Stopping $productHandler.product finished")
        }
    }

    static boolean installAll() throws IOException {
        printlndecor("Installing products")
        def products = getProductsToInstall()
        println("Products to install: " + products)
        for (String product : products) {
            printlndecor("Installing $product")
            def productHandler = productHandlers.get(product)
            productHandler.stop()
            productHandler.uninstall()
            productHandler.install()
            String customDir = p[PRODUCTS_CUSTOMIZATION_DIR]
            if (customDir != null) {
                productHandler.stop()
                productHandler.customize(customDir)
            }
            boolean isSkipStart = p[PRODUCTS_SKIP_START] != null ? Boolean.parseBoolean(p[PRODUCTS_SKIP_START]) : false
            if (!isSkipStart) {
                productHandler.start()
            }
            printlndecor("$product installed")
            println()
        }
        printlndecor("Installing products finished")
        return true
    }

    private static getProductsToInstall() {
        def allProducts = getProductDirsFiltered().keySet()
        String excludeString = p[PRODUCTS_EXCLUDE]
        String includeString = p[PRODUCTS_INCLUDE]
        String[] excludeSplit = excludeString.split(",")
        String[] includeSplit = includeString.split(",")
        def excludeProducts = Arrays.stream(excludeSplit).map(String::trim).filter { !it.isEmpty() }.collect(Collectors.toList())
        def includeProducts = Arrays.stream(includeSplit).map(String::trim).filter { !it.isEmpty() }.collect(Collectors.toList())
        def products = allProducts.stream().filter(p -> includeProducts.isEmpty() || includeProducts.contains(p)).collect(Collectors.toList())
        products = products.stream().filter(p -> !excludeProducts.contains(p)).collect(Collectors.toList())
        return products
    }

    public static findBuildPath(String product, boolean searchInOtherLocations = false) throws IOException {
        String pattern = buildProductFilenamePattern(product)
        printlns("Product name pattern: $pattern")
        File f
        if (!isWin()) {
            pattern += ".*(?<!_pkg|Core|Samples)\\.tar\\.gz"
            //".*(?<!_pkg|Core|Samples)\\.tar\\.gz"
            //".*tar.gz"
            f = getLatestBuild(product, pattern, searchInOtherLocations)
        } else {
            def exePattern = pattern + ".*exe"
            f = getLatestBuild(product, exePattern, searchInOtherLocations)
            if (f == null) {
                def msiPattern = pattern + ".*msi"
                f = getLatestBuild(product, msiPattern, searchInOtherLocations)
            }
        }
        return f == null ? null : f.toString()
    }

    public static findHotfixPath(String buildPath, String product) throws IOException {
        if (!isRelease()) return null

        def buildParentFile = new File(buildPath).getParentFile()
        if (buildParentFile.getName() != "Release") {
            buildParentFile = buildParentFile.getParentFile()
        }
        def hotfixParentPath = buildParentFile.getParent()
        def version = p[PRODUCTS_VERSION]
        if ("0.0.0".equalsIgnoreCase(version) && !P_P9.equalsIgnoreCase(product)) {
            version = "0.0.0"
        }
        def pattern = ".*-hotfix-$version.*jar"
        File[] files = CmdUtils.getListFilesByRegExTemplate(hotfixParentPath, pattern)

        if (files.length == 0) return null
        else return files[files.length - 1]
    }

    private static String buildProductFilenamePattern(String product) {
        String pattern = ".*${product}"
        String versionParam = p[PRODUCTS_VERSION]
        if ("0.0.0".equalsIgnoreCase(versionParam) && !P_P9.equalsIgnoreCase(product)) {
            versionParam = "0.0.0"
        }
        if (product.startsWith("P9")) pattern = pattern.replace("P9", "P9Module")
        if (product == P_P7II && isWin()) pattern = pattern.replace(product, "P7 II")
        if (product == P_P7II && isLinux()) pattern = pattern.replace(product, "P7IIInst")
        if (product == P_P7EXT && isWin()) pattern = pattern.replace(product, "P7 Extension")
        if (product == P_P7EXT && isLinux()) pattern = pattern.replace(product, "P7Extension")
        if (product == P_P7 && isLinux()) pattern = pattern.replace("P7", "P7Inst")
        if (product == P_P7 && isWin()) pattern = pattern.replace("P7", "P7\\.")
        if (product == "P1") {
            def version = versionParam.replace(".", "")
            if (version.endsWith("0")) {
                version = version.substring(0, version.length() - 1);
            }
            pattern += "$version" //dot in the end to avoid _Healthcare version
            pattern += "_Healthcare"
        } else if (!product.startsWith("P7")) {
            def version = versionParam.replace(".", "\\.")
            pattern += "_.*$version"
        }
        pattern
    }

    private static removeExtraFiles() {
        List<String> patternsToDelete = Arrays.asList(
                "Elevate.*",
                ".*\\.ex_",
                ".*\\.pl",
                ".*\\.ini",
                ".*Installer\\.exe",
                ".*Uninstaller\\.exe",
                "NetworkdirRegistry\\.exe",
                ".*Install\\.bat",
                "P9P7Installer.*",
                "P9P7Uninstaller.*",
                "tests_log.txt"
        )

        def folder = new File(getLocalBuildsPath())
        def filesToDelete = new ArrayList<File>();
        for (String f : patternsToDelete) {
            filesToDelete.addAll(folder.listFiles((FilenameFilter) (dir, name) -> name.toLowerCase().matches(f.toLowerCase())))
        }
        if (!filesToDelete.isEmpty()) printlns("Removing old extracted files")
        for (File file : filesToDelete) {
            if (!file.delete()) {
                printlns("Can't remove " + file.getAbsolutePath())
            }
        }
    }

    private static unpacking(String productPackage) {
        def buildsLocalFile = new File(getLocalBuildsPath())
        switch (CmdUtils.getFileExt(productPackage)) {
            case ("exe"): {
                printlns("Self-extractable package:$productPackage")
                runCommand(productPackage, buildsLocalFile)
                break
            }
            case ("zip"): {
                printlns("Zip package:$productPackage")
                unZip(productPackage, getLocalBuildsPath())
                break
            }
            case ("rar"): {
                printlns("Rar package:$productPackage")
                unRar(productPackage, getLocalBuildsPath())
                break
            }
            case ("tar"): {
                printlns("Tar package:$productPackage")
                runCommand("tar -xf \"$productPackage\"", buildsLocalFile)
                break
            }
            case ("gz"): {
                if (CmdUtils.getFileExt(CmdUtils.getFileNameNoExt(productPackage)) == "tar") {
                    printlns("Tar-Gzip package:$productPackage")
                    runCommand("tar -zxf \"$productPackage\"", buildsLocalFile)
                } else {
                    println("Gzip package:" + productPackage)
                    runCommand("gunzip \"$productPackage\"", buildsLocalFile)
                }
                break
            }

        }
    }

    private static boolean install(String product, String packagePath, String additionalParameters = "") throws IOException {
        packagePath = copyPackageIfRemote(packagePath)
        def pkgExt = CmdUtils.getFileExt(packagePath)
        packagePath.contains(P_P9) || packagePath.contains(P_P7) || packagePath.contains(P_P1) ? "msi" : CmdUtils.getFileExt(packagePath)
        println("Installing: " + packagePath)
        switch (pkgExt) {
            case "exe":
                exeInstall(packagePath, additionalParameters)
                break
            case "msi":
                msiInstall(packagePath, product)
                break
            case "jar":
            case "zip":
            case "rar":
                unpacking(packagePath)
                break
            default:
                tgzInstall(packagePath, product)
                break
        }
        if (product == P_P3) {
            def p3 = getProductDirs().get(P_P3)
            try {
                Files.copy(new File("../resources/license.lic").toPath(), new File(p3, "license.lic").getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING)
                printlns("License copied to $p3")
            } catch (Exception e) {
                println(e.printStackTrace())
            }
        }
        return true //success
    }

    private static String copyPackageIfRemote(String packagePath) {
        if (get(PRODUCTS_LOCATION) != "local") {
            def sourcePath = new File(packagePath).toPath()
            def targetPath = new File("${getLocalBuildsPath()}${File.separator}$sourcePath.fileName").toPath()
            printlns("Coping: $sourcePath to $targetPath")
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)
            packagePath = targetPath.toString() //replace package path
            copiedFile = packagePath
        }
        packagePath
    }

    private static boolean jarInstall(String sPackage) {
        String cmd = String.format("\"%s/bin/java.exe\" -Djava.io.tmpdir=\"%s\" -jar \"%s\"", System.getenv("JAVA_HOME"), sPackage, System.getenv("TEMP"))
        cmd = CmdUtils.getNormPath(cmd)
        runCommand(cmd)
        removeExtraFiles()
        return true
    }

    private static boolean tgzInstall(String sPackage, String product = 'product') throws IOException {
        removeExtraFiles()
        String os = CmdUtils.getOSName()
        unpacking(sPackage)
        //String cmd="start /wait " + sPackage + " /quiet"
        //OSUtils.runCommand(cmd)
        //TODO: linux install
        def installerList
        def installer
        if (product == P_P7 || product == P_P7II || product == P_P7EXT) {
            installerList = CmdUtils.getListFilesInFolderByRegExTemplate(getLocalBuildsPath(), ".*${product}Installer.*")
            installer = installerList.find { !it.toString().contains("aix-powerpc") && it.toString().contains("linux-x86_64") }?.toString()
        } else if (product.startsWith("P9")) {
            def key = product.replace("P9", "")
            installer = CmdUtils.getListFilesInFolderByRegExTemplate(getLocalBuildsPath(), ".*P9Module${key}Installer.*")[0].toString()
        } else {
            installerList = CmdUtils.getListFilesInFolderByRegExTemplate(getLocalBuildsPath(), ".*${product}Installer.*")
            installer = installerList.find { !it.toString().contains("aix-powerpc") }?.toString()
        }

        if (installer) {
            runCommand(installer, new File(getLocalBuildsPath()), 10L * 60 * 1000)
            return true
        } else {
            println "No suitable installer found"
            return false
        }
    }


    private static boolean msiInstall(String msiPackage, String product = "product") throws IOException {
        removeExtraFiles()
        if (!isWin()) {
            println("Unable to install msi package on non-win platform")
            return false
        }
        String cmd = "start /wait "
        if (msiPackage.contains("P3P9Module") || msiPackage.contains(P_P1)) cmd += "$msiPackage /quiet /norestart"
        else cmd += "msiexec /i \"$msiPackage\" /quiet /norestart /lvx ${product}_installation.log"
        runCommand(cmd, new File(getLocalBuildsPath()), 15L * 60 * 1000)
        removeExtraFiles()
        return true
    }


    private static boolean exeInstall(String exePackage, String additionalParameters = "") throws IOException {
        removeExtraFiles()
        if (!isWin()) {
            println("Unable to install exe package on non-win platform")
            return false
        }
        unpacking(exePackage)
        def installer = CmdUtils.getListFilesInFolderByRegExTemplate(getLocalBuildsPath(), ".*Installer.exe")[0].toString()
        runCommand(installer + " $additionalParameters", new File(getLocalBuildsPath()), 10L * 60 * 1000)
        removeExtraFiles()
        return true //success
    }

    public static boolean installProduct(String product, String productPackage) throws IOException {
        if (!checkPrerequisites(product)) return false
        if (ProductState.isProductInstalled(product)) {
            ProductUninstaller.removeProduct(product)
        }
        try {
            install(product, productPackage)
        } catch (Exception e) {
            printlns("Error during $product installation. ${e.getMessage()}")
            return false
        }
        if (!ProductState.isProductInstalled(product)) {
            printlns("Error installing $product to " + getProductDirs().get(product))
            return false
        }
        return true //success
    }

    public static boolean installHotfix(String hotfixPackage) {
        String hfName = new File(hotfixPackage).getName()
        println("Hotfix $hfName ...")
        hotfixPackage = copyPackageIfRemote(hotfixPackage)
        def file = new File(hotfixPackage);
        runCommand("java -jar ${file.name}", file.getParentFile())
    }

    private static checkPrerequisites(String product) {
        def prerequisites = getPrerequisites()[product]
        if (!prerequisites.isEmpty()) {
            printlns("Checking prerequisites $prerequisites ... ")
            if (!ProductState.isInstalledProducts(prerequisites)) {
                println(" failed! Required product(s) not installed")
                return false
            }
        }
        return true
    }

    public static String getProductLocation(String product, String pattern) {
        def locations = ["dev", "release"]
        def currentLocation = null
        for (final def location in locations) {
            currentLocation = location
            def path = getProductPathByLocation(product, location)
            def files = CmdUtils.getListFilesByRegExTemplate(path, pattern)
            if (files.length != 0) {
                break
            } else {
                currentLocation = null
            }
        }
        if (currentLocation == null) {
            throw new Exception("Location cannot be defined for product=$product, pattern=$pattern")
        }
        return currentLocation
    }

    static File getLatestBuild(String product, String pattern, boolean searchInOtherLocations = false) throws IOException {
        File[] files
        String location = get(PRODUCTS_LOCATION)
        def path = getProductPath(product)
        prints("Looking for product by pattern: $pattern . Location $path ... ")
        if (location == "local") {
            files = CmdUtils.getListFilesInFolderByRegExTemplate(path, pattern)
        } else {
            files = CmdUtils.getListFilesByRegExTemplate(path, pattern)
            if (files.length == 0 && searchInOtherLocations) {
                if (!"release".equalsIgnoreCase(location)) {
                    path = getReleaseProductPath(product)
                    files = CmdUtils.getListFilesByRegExTemplate(path, pattern)
                }
            }
        }
        List<File> fileList = Arrays.asList(files)
        if (product == P_P2 && (pattern.endsWith("exe") || pattern.endsWith(".*(?<!_pkg|Core|Samples)\\.tar\\.gz"))) {
            //.*(?<!_pkg|Core|Samples)\\.tar\\.gz"
            files = fileList.stream().filter(f -> f.getParentFile().getName() == "filename"
                    && !f.getName().contains("hotfix")).toList()
        }

        if (location == "dev") { //skip non master branches for dev builds
            if (isMasterBranch(product)) {
                fileList = files
            } else {
                String customBranchFileName = "_custom_branch.txt"
                fileList = fileList.stream().filter(f -> {
                    File[] customBranchFiles = CmdUtils.getListFilesInFolderByRegExTemplate(f.getParent(), customBranchFileName)
                    if (customBranchFiles.length > 0) {
                        try (FileReader fr = new FileReader(customBranchFiles[0])) {
                            List<String> lines = IOUtils.readLines(fr)
                            if (lines.size() >= 1) {
                                return lines[0].trim() == getCustomBranchMap().get(product)
                            }
                        }
                    }
                    return false
                }).toList()
            }
        }
        if (location == "release") {
            fileList = fileList.stream().filter(f -> !f.getParent().contains("_release_do_not_use")
                    && !f.getName().contains("hotfix")).toList()
        }
        File result = fileList.size() == 0 ? null : fileList.get(fileList.size() - 1)
        result == null ? println("failed [Not found]") : println("done.")
        return result
    }

    private static void unRar(String sSrcPath, String sDstPath) {
        try {
            Junrar.extract(sSrcPath, sDstPath)
        } catch (RarException | IOException e) {
            e.printStackTrace()
        }
    }

    private static void unZip(String sSrcPath, String sDstPath) {
        try (ZipFile zipFile = new ZipFile(sSrcPath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries()
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement()
                File entryDestination = new File(sDstPath, entry.getName())
                if (entry.isDirectory()) entryDestination.mkdirs()
                else {
                    entryDestination.getParentFile().mkdirs()
                    try (InputStream inStream = zipFile.getInputStream(entry)
                         OutputStream out = new FileOutputStream(entryDestination)) {
                        IOUtils.copy(inStream, out)
                    } catch (IOException e) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace()
        }
    }

}
