import static CmdUtils.isWin
import static CmdUtils.killProcess
import static InstallerProperties.P_P6
import static PrintUtils.printlndecor
import static PrintUtils.printlns

public class P6Product extends BaseProduct {
    public P6Product() {
        super(P_P6);
    }

    public void start() {
        if (isWin()) {
            List<String> services1 = CmdUtils.getWinServices("abcP6")
            for (String s : services1) CmdUtils.startService(s)
            Thread.sleep(15 * 1000)
        }
    }

    public void stop() {
        printlns("Stopping P8 service ... ")
        if (isWin()) {
            List<String> services1 = CmdUtils.getWinServices("abcP6")
            for (String s : services1) CmdUtils.stopService(s)
            Thread.sleep(15 * 1000)
        }
        for (String pid : CmdUtils.getPID("tomcat", "abcP6")) killProcess(pid)
    }

    @Override
    void install() {
        super.install()
        String workingDir = System.getenv("RootPath") + "${File.separator}P6${File.separator}tomcat${File.separator}bin"
        String cmd = "call service.bat install abcP6"
        if (isWin()) {
            CmdUtils.runCommand(cmd, true, new File(workingDir))
        }
        printlndecor("$product installed")
        println()
    }

    @Override
    void uninstall() {
        String workingDir = System.getenv("RootPath") + "${File.separator}P6${File.separator}tomcat${File.separator}bin"
        String cmd = "call service.bat uninstall abcP6"
        if (isWin()) {
            CmdUtils.runCommand(cmd, false, new File(workingDir))
        }
        super.uninstall()
    }
}
