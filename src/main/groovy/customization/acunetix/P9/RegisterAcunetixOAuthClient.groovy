static final boolean execute(File scriptFile) {
    String cmd = "java -cp p5-oauth-cli-0.0.0.0.jar OAuthClientUpdater"
    CmdUtils.runCommand(cmd, scriptFile.parentFile)
    return true
}