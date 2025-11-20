static final boolean execute(File scriptFile) {
    String cmd = "call cleanup_security_key.bat"
    CmdUtils.runCommand(cmd, false, new File("path\\P3\\path\\agent"))

    return true
}