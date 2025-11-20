import static CmdUtils.isWin
import static CmdUtils.killProcess
import static InstallerProperties.P_P8
import static InstallerProperties.get
import static PrintUtils.printlns

public class P8Product extends BaseProduct {
    public P8Product() {
        super(P_P8);
    }

    public void start() {
        if (isWin()) {
            List<String> services1 = CmdUtils.getWinServices("p8-*")
            for (String s : services1) CmdUtils.startService(s)
            List<String> services2 = CmdUtils.getWinServices("abc P8*")
            for (String s : services2) CmdUtils.startService(s)
            Thread.sleep(15 * 1000)
        }
    }

    public void stop() {
        printlns("Stopping P8 service ... ")
        if (isWin()) {
            List<String> services1 = CmdUtils.getWinServices("p8-*")
            for (String s : services1) CmdUtils.stopService(s)
            List<String> services2 = CmdUtils.getWinServices("abc P8*")
            for (String s : services2) CmdUtils.stopService(s)
            Thread.sleep(15 * 1000)
        }
        for (String pid : CmdUtils.getPID("tomcat", "p8-")) killProcess(pid)
        for (String pid : CmdUtils.getPID("elasticsearch", "P8")) killProcess(pid)
        for (String pid : CmdUtils.getPID("opensearch", "P8")) killProcess(pid)
        for (String pid : CmdUtils.getPID("wsm", "P8")) killProcess(pid)
        for (String pid : CmdUtils.getPID("node", "P8")) killProcess(pid)
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
                def webService = "p8-web-service"
                for (String pid : CmdUtils.getPID("tomcat", webService)) killProcess(pid)
                CmdUtils.stopService(webService)
                ProductInstaller.installHotfix(hotfixPath.getAbsolutePath())
                CmdUtils.startService(webService)
            }
        }
    }
}
