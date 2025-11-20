static final boolean execute(File scriptFile) {
    String cmd = "powershell -ExecutionPolicy Bypass -File enableHibernateSearch.ps1"
    CmdUtils.runCommand(cmd, scriptFile.parentFile)
    return true
}