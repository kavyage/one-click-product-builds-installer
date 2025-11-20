static final Object execute(File scriptFile) {
    String cmd = "java -cp p5-oauth-cli-0.0.0.0.jar OAuthClientSecretUpdater P9-XX-auth-sample-client XXXXXX-XXXX-XXXX-XXXX-XXXXXXXX"
    def workDir = new File(scriptFile.parentFile.parentFile.parentFile, "acunetix/P9")
    def command = CmdUtils.runCommand(cmd, workDir)
    return command
}