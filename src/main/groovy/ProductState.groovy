import org.apache.commons.io.IOUtils

import java.nio.file.Files
import java.nio.file.Paths

import static InstallerProperties.*
import static PrintUtils.*

class ProductState {

    static printProductsState(String prefix) {
        println()
        printlndecor("$prefix environment state")
        def productDirMap = getProductDirs()
        int count = 0
        for (String product : productDirMap.keySet()) {
            def path = productDirMap.get(product)
            if (path != null) {
                File dir = new File(path)
                if (product == P_P1) {
                    if (printSBVersion(dir, product)) count++
                } else if (product.startsWith(P_P7)) {
                    if (printCommonVersion(dir, product)) count++
                } else if (product == P_P4) {
                    if (printVersion(dir, p4.client.version.txt)) count++
                    if (printVersion(dir, p4.server.version.txt)) count++
                } else if (product.startsWith("P7")) {
                    if (printP7Version(product, dir)) count++
                } else {
                    if (printVersion(dir, "version.txt")) count++
                }
            }
        }
        if (count == 0) {
            printlns("There are no installed products")
        }
        println()

    }

    static boolean isInstalledProducts(List<String> products) {
        for (String p : products) {
            if (!isProductInstalled(p)) return false
        }
        return true
    }

    static boolean isProductInstalled(String product) {
        prints("Checking if $product has been installed ... ")
        def dir = getProductDirs().get(product)
        if (dir == null) return false
        File file
        if (product.startsWith(P_P7) || product == P_P4) {
            file = new File(dir)
        } else if (product == P_P1) {
            file = new File(dir, "${product}.exe")
        } else {
            file = new File(dir, "bin")
        }
        boolean result = file.exists()
        println(result ? "done." : "failed. [${file.getAbsolutePath()} does not exists]")

        if (result) {
            File dfile = new File(ProductInstaller.copiedFile)
            prints("Cleaning copied installation package file: " + dfile)
            if (dfile.exists()) {
                dfile.delete()
                println(dfile.exists() ? "   - Can't remove [${dfile}" : " ... done.")
            }
            printlns("Cleaning additional files generated:")
            def folderPath = Paths.get(p[PRODUCTS_LOCATION_LOCAL])
            Files.list(folderPath).each { filePath ->
                if (filePath.fileName.toString().contains(product) ||
                        filePath.fileName.toString().endsWith(".tar")
                        || filePath.fileName.toString().contains("NetworkdirRegistry")) {
//                    Files.delete(filePath)
                    boolean deleted = Files.deleteIfExists(filePath)
                    println deleted ? "Deleted: ${filePath.fileName} ... done" : "Can't remove file: ${filePath.fileName}"
                }
            }

        }
        return result
    }

    private static boolean printVersion(File dir, String filename) {
        File versionFile = new File(dir, filename)
        if (!versionFile.exists()) return false
        try (def reader = new FileReader(versionFile)) {
            def version = IOUtils.readLines(reader).get(0)
            println(" - $version. Location: ${dir.getAbsolutePath()}")
        }
        return true
    }

    private static boolean printSBVersion(File dir, String product) {
        if (!new File(dir, "${P_P1}.exe").exists()) return false
        File versionFile = new File("$RootPath${File.separator}${product}.dat")
        if (versionFile.exists())
            try (def reader = new FileReader(versionFile)) {
                def version = IOUtils.readLines(reader).get(2).split("=")[1]
                println(" - ${product} v.$version. Location: ${dir.getAbsolutePath()}")
            }
        return true
    }

    private static boolean printCommonVersion(File dir, String product) {
        if (!dir.exists()) return false
        String filename = product.startsWith(P_P7) ? "P9$product" : product
        File versionFile = new File("$RootPath${File.separator}${filename}.dat")
        if (versionFile.exists())
            try (def reader = new FileReader(versionFile)) {
                def version = IOUtils.readLines(reader).get(2).split("=")[1]
                println(" - ${product} v.$version. Location: ${dir.getAbsolutePath()}")
            }
        return true
    }

    private static boolean printP7Version(String product, File dir) {
        if (!dir.exists()) return false
        println(" - P9-$product. Location: ${dir.getAbsolutePath()}")
        return true
    }

}
