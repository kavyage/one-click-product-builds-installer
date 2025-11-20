static final boolean execute(File scriptFile) {
    String cmd = "mvn clean install -DskipTests -f P9-installation-prep\\pom.xml"
    CmdUtils.runCommand(cmd, false, new File("jenkins\\workspace\\job_P9_BVT"))

    cmd = "java -jar P9-installation-prep\\target\\P9-installation-prep-1.0-SNAPSHOT.jar"
    CmdUtils.runCommand(cmd, false, new File("jenkins\\workspace\\job_P9_BVT"))

    return true
}