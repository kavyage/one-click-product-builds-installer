# Overview

This Project is designed to facilitate one-click installation and uninstallation of multiple product builds across
operating systems (Windows and Linux) environments (dev, release, local, any custom environment as required systems
accordingly.)
It provides a scalable structure where each product can have its own installation logic, while common utilities and
executors are centrally managed.

The installation build files supports .exe, .msi, .sh, .jar, .zip, .rar, .tgz, .gz and other formats as needed.

# Tech stack:

Groovy, Java, Gradle

# Structure

* src/main/groovy/Utils: Contains the main Groovy scripts needed for installation and uninstallation process.
* src/main/groovy/Products: Contains sample product-specific installation and uninstallation scripts.
* src/main/groovy/Customizations: Contains any custom scripts or configurations needed, it supports test project executions and security tests.
* src/main/resources: Contains file handler executor configuration and .bat needed for the installation process.

# Run (refer src/main/resources/README.TXT)

Sample manual command for dev env builds “install-dev.bat -u -d -v 0.0.0"

# CI/CD adaption

1. [x] to run the installation scripts (lib/*;scripts"),
* configure jenkins or any CI/CD tool to build the latest installer artifacts when new product build is available
* copy the latest installer artifacts to target and run the installer scripts .bat/.sh (from src/main/resources) with
  required parameters
2. [x] to run the product based tests,
* configure jenkins test jobs with respective test project repositories to run the test cases against the installed products. 
