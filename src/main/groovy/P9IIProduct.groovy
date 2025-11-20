import static CmdUtils.killProcess
import static InstallerProperties.P_P9II
import static PrintUtils.prints

public class P9IIProduct extends P9Product {
    public P9IIProduct() {
        super(P_P9II, "P3ModuleForP9II");
    }

    public void start() {
        executeWinServiceCommand("abc_P3_P9_II_*", (serviceName) -> CmdUtils.startService(serviceName))
    }

    public void stop() {
        executeWinServiceCommand("abc_P3_P9_II_*", (serviceName) -> CmdUtils.stopService(serviceName))
        for (String pid : CmdUtils.getPID("java", "P9 II")) killProcess(pid)
    }

    @Override
    void install() {
        super.install()
    }

    @Override
    void uninstall() {
        prints("Killing msiexec  ...")
        for (String pid : CmdUtils.getPID("msiexec", "")) killProcess(pid)
        prints("Removing product  ...")
        ProductUninstaller.removeProduct(this.product)
        prints("Deleting product folder  ...")
        deleteFolder(System.getenv("RootPath"), this.productFolder)
        prints("Deleting profiles  ...")
        deleteProfileFolders("P9 II.*")
    }
}
