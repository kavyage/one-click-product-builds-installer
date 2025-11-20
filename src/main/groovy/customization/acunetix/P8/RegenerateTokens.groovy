static final boolean execute(File scriptFile) {
    String tokenFolder = System.getenv("userprofile") + "/path/P8/tokens"
    String cmd = String.format("call token-generator.bat -result-folder \"%s\" -full-access-token full_access_token -read-only-access-token read_only_token -mode custom", tokenFolder)
    CmdUtils.runCommand(cmd, new File(new File(System.getenv("RootPath")), "P8/bin/tokens-generator"))
    return true
}