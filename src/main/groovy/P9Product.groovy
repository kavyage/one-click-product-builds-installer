import static CmdUtils.killProcess
import static InstallerProperties.P_P9
import static PrintUtils.printlns

public class P9Product extends BaseProduct {
    public P9Product() {
        super(P_P9, "P3ModuleForP9");
    }

    protected P9Product(String product, String productFolder) {
        super(product, productFolder);
    }

    public void start() {
        executeWinServiceCommand("abc_P3_P9_*", (serviceName) -> CmdUtils.startService(serviceName))
        printlns("Check availability of P9 Server")
        def getRC
        long tStart = System.currentTimeMillis()
        do {
            try {
                URLConnection get = new URL("http://localhost:12345/P9-server/XX/xx").openConnection()
                getRC = get.getResponseCode()
                println(getRC)
                if (!getRC.equals(200)) {
                    Thread.sleep(10 * 1000)
                }
            } catch (e) {
                printlns("P9 Server availability check result: " + getRC + " " + e.getMessage())
            }
        } while (!getRC.equals(200) && ((System.currentTimeMillis() - tStart) <= 10 * 60 * 1000))
        printlns("P9 Server availability check result: " + getRC)
    }

    public void stop() {
        executeWinServiceCommand("abc_P3_P9_*", (serviceName) -> CmdUtils.stopService(serviceName))
        for (String pid : CmdUtils.getPID("java", "P9", "P9 II", "jenkins")) killProcess(pid)
    }

    @Override
    void install() {
        super.install()
    }

    @Override
    void uninstall() {
        super.uninstall()
        deleteProfileFolders("P9 (?!II).*")
    }

    protected static void executeWinServiceCommand(def serviceFilter, cmd) {
        def isSuccessful
        int iteration = 0
        do {
            iteration++
            isSuccessful = true
            List<String> services1 = CmdUtils.getWinServices(serviceFilter)
            for (String s : services1) {
                def returnCode = cmd(s)
                if (returnCode != 0) {
                    isSuccessful = false
                }
            }
            if (isSuccessful) {
                Thread.sleep(120 * 1000)
            }
        } while (!isSuccessful && iteration <= 3)
    }

    protected static void deleteProfileFolders(String profilesFilter) {
        deleteFolder(System.getenv("RootPath") + "P3${File.separator}profiles", profilesFilter)
    }
}
