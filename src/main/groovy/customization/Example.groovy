/**
 * This is an example of custom script to be executed by Installer process
 * @return result true or false
 */
static final boolean execute(File locationOfThisScriptFile) {
    println("      * This is an example for a custom script execution * . Look at /customization/Example.groovy")
    return true
}