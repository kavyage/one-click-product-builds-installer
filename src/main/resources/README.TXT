1. Usage:
groovy -cp "lib/*"  [default mode:
             [products.location:dev,
              products.location.dev:\\host\Builds,
              products.location.release:\\host.abc.com\release_builds\dir path,
              products.location.local:C:\abc\\builds,
              products.customization.dir:customization,
              products.cleanup:false,
              products.exclude:,
              products.version:0.0.0]
       ]
 -c,--customization   Execute customization scripts.
 -e,--exclude <arg>   Comma separated list of products to exclude from
                      install/uninstall process.
 -h,--help            Print this help.
 -i,--install <arg>   Install all products from specified location
                      'dev|release|local'
 -s,--state           Print the current state of installed products.
 -u,--uninstall       Uninstall all products.
 -v,--version <arg>   Products version

 2. You can run predefined install-dev.bat or install-release.bat

 For example:
 > install-dev.bat -e P1,P2

 > install-release.bat -v 0.0.0

