static final boolean execute(File scriptFile) {
    String cmd = "mvn clean install -DskipTests -f operation-outcome-analyzer\\pom.xml"
    CmdUtils.runCommand(cmd, false, new File("jenkins\\workspace\\job_P9_BVT"))

    return true
}