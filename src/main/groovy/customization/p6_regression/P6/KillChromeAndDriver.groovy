import static CmdUtils.killProcess

static final boolean execute(File scriptFile) {
    for (String pid : CmdUtils.getPID("chrome", "")) killProcess(pid)
    return true
}