import org.apache.commons.io.FileUtils

import static CmdUtils.deleteService
import static CmdUtils.killProcess
import static InstallerProperties.*
import static PrintUtils.*

class ProductUninstaller {

    private static final String SERVICE_P4 = "P4Service"
    private static final String SERVICE_P6 = "abcP6"
    private static final String SERVICE_P9Portal = "abcP9Portal"

    static void uninstallAll() {
        getProductDirs().keySet().forEach { uninstallProduct(it) }
    }

    private static void uninstallProduct(String product) {
        if (ProductState.isProductInstalled(product)) {
            printlndecor("Uninstalling $product")
            removeProduct(product)
            printlndecor("$product uninstalled")
            println()
        }
    }

    static void removeP4() {
        println("Uninstalling:")
        printlns("Killing P4 processes ... ")
        for (String pid : CmdUtils.getPID("java", "P4")) killProcess(pid)
        for (String pid : CmdUtils.getPID(p4(64) ", p4")) killProcess(pid)
        printlns("Deleting P4 service ... ")
        deleteService(SERVICE_P4)
        deleteProductDir(P_P4)
    }

    static void removeP3() {
        println("Uninstalling:")
        printlns("Killing P3 processes ... ")
        for (String pid : CmdUtils.getPID("java", "P3")) killProcess(pid)
        printlns("Deleting P3 profiles services ... ")
        List<String> l_services = CmdUtils.getWinServices("abc_P3*")
        for (String s : l_services) deleteService(s)
        deleteProductDir(P_P3)

    }

    static void removeXEC() {
        println("Uninstalling:")
        printlns("Killing P3 processes ... ")
        for (String pid : CmdUtils.getPID("java", "P3")) killProcess(pid)
        deleteProductDir(P_P5)
    }

    static void removeP6() {
        println("Uninstalling:")
        printlns("Killing P6 processes ... ")
        for (String pid : CmdUtils.getPID("tomcat", "P6")) killProcess(pid)
        println("deleting P6 service ... ")
        deleteService(SERVICE_P6)
        deleteProductDir(P_P6)
    }

    static void removeP8() throws IOException {
        println("Uninstalling:")
        printlns("Killing P8 processes ... ")
        for (String pid : CmdUtils.getPID("tomcat", "p8-")) killProcess(pid)
        for (String pid : CmdUtils.getPID("elasticsearch", "P8")) killProcess(pid)
        for (String pid : CmdUtils.getPID("opensearch", "P8")) killProcess(pid)
        for (String pid : CmdUtils.getPID("wsm", "P8")) killProcess(pid)
        for (String pid : CmdUtils.getPID("node", "P8")) killProcess(pid)
        printlns("Deleting P8 service ... ")
        List<String> services1 = CmdUtils.getWinServices("p8-*")//todo: check service names
        for (String s : services1) deleteService(s)
        List<String> services2 = CmdUtils.getWinServices("abc P8*")
        for (String s : services2) deleteService(s)
        printlns("Uninstalling P8 App ... ")
        CmdUtils.uninstallAppByName("abc P8*")
        prints("Removing P8 tokens ... ")
        File tokensPath
        if (CmdUtils.isWin()) {
            tokensPath = new File(System.getenv("USERPROFILE") + "path/P8/tokens")
            if (tokensPath.exists())
                FileUtils.deleteDirectory(tokensPath)
            println("done")
        } else if (CmdUtils.isLinux()) {
            //TODO: need to specify tokens dir!!!
            tokensPath = new File(System.getenv("USERPROFILE") + "path${File.separator}P8${File.separator}tokens")
//        print("FAILED!!!  Need to implement removing tokens for non-win platform\n")
            if (tokensPath.exists())
                FileUtils.deleteDirectory(tokensPath)
            println("done")
        }

        deleteProductDir(P_P8)
    }

    static void removeP9() {
        println("Uninstalling:")
        printlns("Killing P9 processes ... ")
        for (String pid : CmdUtils.getPID("tomcat", "P9")) killProcess(pid)
        printlns("Killing P3 processes ... ")
        for (String pid : CmdUtils.getPID("java", "P3")) killProcess(pid)
        printlns("Deleting P9 services ... ")
        deleteService(SERVICE_P9Portal)
        List<String> services = CmdUtils.getWinServices("abc_P3_P9*")
        for (String s : services) deleteService(s)
        printlns("Uninstalling P9 App ... ")
        CmdUtils.uninstallAppByName("abc P3 Module for P9*")
        deleteProductDir(P_P9)
    }

    static void removeP9II() {
        println("Uninstalling:")
        printlns("Killing P9II processes ... ")
        printlns("Killing P3 processes ... ")
        for (String pid : CmdUtils.getPID("java", "P9 II")) killProcess(pid)
        List<String> services = CmdUtils.getWinServices("abc_P3_P9_II*")
        for (String s : services) deleteService(s)
        printlns("Uninstalling P9II App ... ")
        CmdUtils.uninstallAppByName("abc P3 Module for P9 II*")
        deleteProductDir(P_P9II)
    }

    static void removeP9Ext() {
        println("Uninstalling:")
        printlns("Killing P9Extension processes ... ")
        for (String pid : CmdUtils.getPID("tomcat", "P9Extension")) killProcess(pid)
        printlns("Killing P3 processes ... ")
        for (String pid : CmdUtils.getPID("java", "P3")) killProcess(pid)
        printlns("Deleting P9Extension services ... ")
        deleteService(SERVICE_P9Portal)
        List<String> services = CmdUtils.getWinServices("abc_P3_P9Extension*")
        for (String s : services) deleteService(s)
        printlns("Uninstalling P9Extension App ... ")
        CmdUtils.uninstallAppByName("abc P3 Module for P9 Extension*")
        deleteProductDir("P9Extension")
    }

    static void removeXE() {
        println("Uninstalling:")
        deleteProductDir(P_P2)
    }

    static void removeP7() {
        println("Uninstalling:")
        CmdUtils.uninstallAppByName("abc dir Database P9-P7")
    }

    static void removeP7II() {
        println("Uninstalling:")
        CmdUtils.uninstallAppByName("abc dir Database P9-P7 II")
    }

    static void removeP7Ext() {
        println("Uninstalling:")
        CmdUtils.uninstallAppByName("abc dir Database P9-P7 Extensions")
    }

    static void removeSB() {
        println("Uninstalling:")
        CmdUtils.uninstallAppByName("abc P1*")
        deleteProductDir(P_P1)
    }

    private static deleteProductDir(String product) {
        printlns("Removing installation folder... ")
        def productDir = getProductDirs().get(product)
        if (productDir == null) {
            println("failed! Environment variable is not defined")
            return
        }
        Set<String> pids = ProcessFileHandlerExecutor.getHandlerPIDsByFilePath(new File(productDir))
        pids.forEach { killProcess(it) }

        File dir = new File(productDir)
        try {
            if (dir.isDirectory()) FileUtils.deleteDirectory(dir)
            println("done")
        } catch (Exception e) {
            println("failed! " + e.getMessage())
            PrintUtils.printlnsDebug(e.printStackTrace())
        }
    }

    static removeProduct(String product) {
        switch (product) {
            case P_P1: removeP1()
                break
            case P_P2: removeP2()
                break
            case P_P3: removeP3()
                break
            case P_P4: removeP4()
                break
            case P_P5: removeP5()
                break
            case P_P6: removeP6()
                break
            case P_P7: removeP7()
                break
            case P_P7II: removeP7II()
                break
            case P_P7EXT: removeP7Ext()
                break
            case P_P8: removeP8()
                break
            case P_P9: removeP9()
                break
            case P_P9II: removeP9II()
                break
            case P_P9EXT: removeP9Ext()
                break
        }
    }
}
