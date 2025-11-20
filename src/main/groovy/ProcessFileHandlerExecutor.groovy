import static PrintUtils.printlns

class ProcessFileHandlerExecutor {

    static Set<String> getHandlerPIDsByFilePath(File file) {
        printlns("Checking if folder ${file.getAbsolutePath()} locked ...")
        def handleFilePath = "Handle${File.separator}handle.exe"
        File handleFile = new File(this.getClassLoader().getResource(handleFilePath).getPath()) //used for dev mode
        handleFilePath = (handleFile.getAbsolutePath().contains(".jar")) ? new File(handleFilePath).getAbsolutePath() : handleFile.getAbsolutePath()
        def result = CmdUtils.runCommand("$handleFilePath -nobanner /accepteula \"${file.getAbsolutePath()}\"")
        String out = result.get("stdout")
        String[] outLines = out.split("\n")
        Set<String> pids = new HashSet<>()
        for (String line : outLines) {
            String[] tokens = line.split(" ").toList().stream().filter { it -> !it.isBlank() }.toArray()
            for (i in 0..<tokens.length) {
                if (tokens[i].trim() == "pid:") {
                    pids.add(tokens[i + 1].trim())
                    break
                }
            }
        }
        printlns(pids.isEmpty() ? "No locks" : "Following processes lock the folder: $pids")
        return pids
    }

    static void main(String[] args) throws IOException, InterruptedException {
        // Example how to lock file: (>&2 pause) >> test.txt
        Set<String> pids = getHandlerPIDsByFilePath(new File("path\\test.txt"))
        pids.forEach { it -> CmdUtils.killProcess(it) }
    }
}
