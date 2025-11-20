static final boolean execute(File scriptFile) {
    String cmd = "for /f \"tokens=*\" %G in ('dir /b /a:d \"%RootPath%\\P3\\profiles\\P9 *\"') do powershell -Command \"(gc '%RootPath%\\P3\\profiles\\%G\\path\\startup.ini') -replace '-vmargs', '-vmargs -Xmx1500m' | Out-File -encoding UTF8 '%RootPath%\\P3\\profiles\\%G\\path\\startup.ini'\""
    CmdUtils.runCommand(cmd, (new File(System.getenv("RootPath"))))
    return true
}