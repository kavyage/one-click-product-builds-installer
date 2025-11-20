import org.apache.commons.io.FileUtils

import static CmdUtils.killProcess
import static InstallerProperties.P_P3

public class P3Product extends BaseProduct {
    public P3Product() {
        super(P_P3);
    }

    public void start() {
    }

    public void stop() {
        File workDir = new File(System.getenv("RootPath") + "P3${File.separator}bin")
        if (workDir.exists()) {
            String cmd = "call shutdown_all_profiles.bat"
            String cmdLin = "./shutdown_all_profiles.sh"
            def isSuccessful = CmdUtils.runCommand(cmd, false, workDir)
            def isSuccessfulLin = CmdUtils.runCommand(cmdLin, false, workDir)
            if (isSuccessful || isSuccessfulLin) {
                Thread.sleep(60 * 1000)
            }
        }
        for (String pid : CmdUtils.getPID("java", "P3")) killProcess(pid)
    }

    @Override
    void install() {
        def path = InstallerProperties.getLocalBuildsPath()
        def filesToCleanUp = CmdUtils.getListFilesInFolderByRegExTemplate(path, "P3Installer.exe|p3.*|setx.*")
        for (final def toCleanUp in filesToCleanUp) {
            if (toCleanUp.exists()) {
                def isDeleted = toCleanUp.delete()
                if (!isDeleted) {
                    def pids = ProcessFileHandlerExecutor.getHandlerPIDsByFilePath(toCleanUp.parentFile)
                    println "" + toCleanUp + " " + pids
                    if (pids != null) {
                        for (final def pid in pids) {
                            CmdUtils.killProcess(pid)
                        }
                    }
                    isDeleted = toCleanUp.delete()
                }
                println toCleanUp.getAbsolutePath() + " " + isDeleted
            }
        }
        super.install()
    }

    @Override
    void uninstall() {
        deleteProfileFolders(".*")
        super.uninstall()
    }

    protected static void deleteProfileFolders(String profilesFilter) {
        def profiles = CmdUtils.getListFilesInFolderByRegExTemplate(System.getenv("RootPath") + "P3${File.separator}profiles", profilesFilter)
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
