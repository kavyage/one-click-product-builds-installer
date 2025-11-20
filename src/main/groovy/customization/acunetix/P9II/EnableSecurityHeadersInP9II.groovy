static final boolean execute(File scriptFile) {
    String cmd = "for /f \"tokens=*\" %G in ('dir /b /a:d \"%RootPath%\\P3\\profiles\\P9 II*\"') do powershell -Command \"(gc '%RootPath%\\P3\\profiles\\%G\\path\\Security.properties') -replace 'com.abc.etools.P3.jetty.custom.response.headers.enabled=false', 'com.abc.etools.P3.jetty.custom.response.headers.enabled=true' | Out-File -encoding UTF8 '%RootPath%\\P3\\profiles\\%G\\path\\Security.properties'\""
    CmdUtils.runCommand(cmd, (new File(System.getenv("RootPath"))))
    return true
}