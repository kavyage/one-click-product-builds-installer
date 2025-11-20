import org.apache.commons.io.FilenameUtils

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

import static PrintUtils.*

class CmdUtils {
    private static final String STOP_SERVICE = "sc stop "
    private static final String DELETE_SERVICE = "sc delete "
    public static final String START_SERVICE = "sc start "
    public static final String LIST_SERVICES = "net start"

    static String getOSName() {
        System.getProperty("os.name").startsWith("Win") ? "nt" : "posix"
    }

    static boolean isWin() {
        getOSName() == "nt"
    }

    static boolean isPosix() {
        getOSName() == "posix"
    }

    static boolean isLinux() {
        return isPosix() && System.getProperty("os.name").contains("Linux")
    }

    static String getPathForOS(List<String> paths) {
        def separator = FileSystems.getDefault().getSeparator()
        if (isWin()) {
            return paths[0].split('\\\\').join(separator)
        } else if (isLinux()) {
            return paths[1].split('/').join(separator)
        } else {
            return "unsupported os"
        }
    }


    static Set<String> getPID(String processName, String filterInclude, String... filtersExclude = null) {
        //TODO: get pid for linux : ps -aux |grep ${processName} |grep ${filter}
        String os = getOSName()
        StringBuilder cmd = new StringBuilder("powershell.exe -Command \"Get-CimInstance Win32_Process -Filter \\\"name like '%$processName%'\\\"| select ProcessId, CommandLine | format-list")
        Map<String, String> out
        Map<String, String> processes = new HashMap<>()
        if (os == "nt") {
            out = runCommand(cmd.toString())
            String stdout = out.get("stdout")
            String pid = ""
            cmd = new StringBuilder()
            for (String l : Arrays.asList(stdout.split("\n"))) {
                if (l.contains("ProcessId ")) {
                    if (pid.length() > 0
                            && cmd.contains(filterInclude)
                            && (filtersExclude == null || (filtersExclude != null && doesNotContain(cmd, filtersExclude))))
                        processes.put(pid.strip(), cmd.toString())
                    cmd = new StringBuilder()
                    pid = l.split(":")[1]
                } else if (l.contains("CommandLine "))
                    cmd = new StringBuilder(l)
                else if (cmd.length() > 0)
                    cmd.append("  ").append(l)
            }
            if (pid.length() > 0 && !cmd.contains("Installer")
                    && cmd.contains(filterInclude)
                    && (filtersExclude == null || (filtersExclude != null && doesNotContain(cmd, filtersExclude))))
                processes.put(pid.strip(), cmd.toString())
        }
        for (String k : processes.keySet()) {
            printlns("id: $k")
            printlns(processes.get(k))
        }
        return processes.keySet()
    }

    static boolean doesNotContain(StringBuilder cmd, String... filtersExclude) {
        boolean res = true
        for (final def filterExclude in filtersExclude) {
            res &= !cmd.contains(filterExclude)
        }
        return res
    }

    static killProcess(pid) {
        if (isWin())
            runCommand("powershell -Command \"Stop-Process -Id $pid -Force\"")
        else
            runCommand("kill -s 9 $pid")
    }


    static void deleteService(String sName) {
        if (isWin()) {
            runCommand(STOP_SERVICE + sName)
            runCommand(DELETE_SERVICE + sName)
        }
    }

    static int stopService(String sName) {
        if (isWin()) {
            def command = runCommand(STOP_SERVICE + sName)
            return Integer.parseInt(command.get("code"))
        }
        throw new UnsupportedOperationException("Not implemented")
    }

    static int startService(String sName) {
        if (isWin()) {
            def command = runCommand(START_SERVICE + sName)
            return Integer.parseInt(command.get("code"))
        }
    }

    static List<String> getWinServices(String sMask) {
        List<String> l_services = new ArrayList<String>()
        String cmd = "powershell -Command \"Get-Service \\\"$sMask\\\" |select Name | foreach { \$_.Name }\""
        Map<String, String> out = runCommand(cmd)
        String stdout = out.get("stdout")
        if (out.get("code") == "0") {
            for (String s : Arrays.asList(stdout.split("\n"))) {
                if (s.length() > 0) {
                    l_services.add(s.strip())
                }
            }
        }
        return l_services
    }

    private static String getAppID(String template) {
        def operator = template.contains("*") ? "-match" : "-eq"
        String cmd = "powershell -Command \"Get-WmiObject -Class Win32_Product | Where-Object { \$_.Name $operator '$template'} | select IdentifyingNumber | foreach { \$_.IdentifyingNumber }\""
        Map<String, String> out = runCommand(cmd)
        def id = out.get("stdout").strip()
        if (id.isEmpty()) {
            printlns("AppID is not found")
            return null
        } else {
            printlns("AppID=" + id.replaceAll("\n", " ")) //TODO: print details
            return id.split("\n")[0]
        }
    }

    private static int uninstallAppByID(String appID) {
        String cmd = "powershell -Command \"Start-Process MsiExec.exe -ArgumentList '/norestart /q/x " + appID + " REMOVE=ALL' -wait\""
        Map<String, String> out = runCommand(cmd)
        return Integer.parseInt(out.get("code"))
    }

    static int uninstallAppByName(String sTemplate) {
        def appID = getAppID(sTemplate)
        return appID = null ? 0 : uninstallAppByID(appID)
    }

    //recursively searching for files matching @sTemplate in @sourceFolder folder
    static File[] getListFilesByRegExTemplate(String sourceFolder, String template) throws IOException {
        if (sourceFolder == null) sourceFolder = "."
        List<File> files = new ArrayList<File>()
        Files.walkFileTree(Paths.get(sourceFolder), new SimpleFileVisitor<Path>() {
            @Override
            FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                var f = file.toString()
                if (f.matches(template)) {
                    files.add(new File(f))
                }
                return FileVisitResult.CONTINUE
            }

            @Override
            FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                //print(file.fileName)
                return FileVisitResult.CONTINUE
            }
        })
        File[] f_files = new File[files.size()]
        files.toArray(f_files)
        return f_files
    }

    static File[] getListFilesInFolderByRegExTemplate(String sourceFolder, String sTemplate) {
        if (sourceFolder == null) sourceFolder = "."
        def folder = new File(sourceFolder)
        return folder.listFiles((FilenameFilter) (dir, name) -> name.matches(sTemplate))
    }


    static String getFileName(String sFilePath) {
        return new File(sFilePath).getName()
    }


    static String getFileNameNoExt(String sFilePath) {
        return FilenameUtils.removeExtension(sFilePath)
    }


    static String getFileExt(String sFile) {
        return FilenameUtils.getExtension(sFile)
    }


    static String getFilePath(String sFilePath) {
        return new File(sFilePath).getParent()
    }

    static String getNormPath(String sFilePath) {
        return new File(sFilePath).getPath().toString()
    }

    static boolean runCommand(String cmd, boolean canFail, File workingDir = new File(System.getenv("RootPath"))) {
        def command = runCommand(cmd, workingDir)
        def parseInt = Integer.parseInt(command.get("code"))
        if (parseInt != 0 && canFail) {
            throw new Exception(String.format("The command '%s' failed with code %s", cmd, command.get("code")))
        }
        return parseInt == 0
    }

    static Map runCommand(String cmd, File workingDir = new File(System.getenv("RootPath")), long timeout = 0, int attempt = 0) {
        if (attempt >= 3) {
            throw new Exception("Cannot complete command $cmd")
        }
        int code = 1
        var builder = new ProcessBuilder()
        if (isWin()) {
            prints("Executing: cmd.exe /c " + cmd + "  ...")
            builder.command("cmd.exe", "/c", cmd)
        } else {
//            prints("Executing: sh -c " + cmd + "  ...")
//            builder.command("sh", "-c", cmd)
            prints("Executing: echo y | sh -c " + cmd + " ...");
            builder.command("sh", "-c", "echo y | " + cmd);
        }
        builder.directory(workingDir)
        printlnsDebug("")
        printlnsDebug("Working dir: $workingDir")

        long start = System.currentTimeMillis()
        var stdout = new StringBuilder("")
        var stderr = new StringBuilder("")
        try {
            Process process = builder.start()
            var stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()))
            var stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))
            int stdInputIndx = 0;
            int stdErrorIndx = 0;
            while (process.isAlive()) {
                if (timeout > 0 && ((start + timeout) < System.currentTimeMillis())) {
                    process.destroyForcibly().waitFor()
                    def command = runCommand(cmd, workingDir, timeout, ++attempt)
                    return command
                } else {
                    String s
                    while ((s = stdInput.readLine()) != null) {
                        printlnsDebug("Std out: $s")
                        stdout.append(s).append("\n")
                        stdInputIndx++
                    }
                    while ((s = stdError.readLine()) != null) {
                        printlnsDebug("Err out: $s")
                        stderr.append(s)
                        stdErrorIndx++
                    }
                    Thread.sleep(500)
                }
            }
            def outputs = stdout.readLines()
            for (int i = stdInputIndx; i < outputs.size(); i++) {
                def get = outputs.get(i)
                printlnsDebug("Std out: $get")
            }
            def errors = stderr.readLines()
            for (int i = stdErrorIndx; i < errors.size(); i++) {
                def get = outputs.get(i)
                printlnsDebug("Std out: $get")
            }
            code = process.exitValue()
        } catch (IOException | InterruptedException e) {
            e.printStackTrace()
            printlnsDebug("Error: ${e.getMessage()}")
        }

        Map out = new HashMap<String, String>()
        out.put("code", String.valueOf(code))
        out.put("stderr", stderr.toString())
        out.put("stdout", stdout.toString())
        if (code != 0) {
            if (code == 1060)
                println("  done [ErrCode: 1060: 'The specified service does not exist']")
            else if (code == 1062)
                println("  done [ErrCode: 1062: 'The service has not been started']")
            else {
                println("  failed [ErrCode: " + String.valueOf(code) + "]")
                println("ERROR: " + stderr)
            }
        } else println("  done [ErrCode: 0]")
        return out
    }
}
