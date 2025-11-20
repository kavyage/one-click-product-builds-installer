import static java.lang.System.getenv

class InstallerProperties {

    static final String PRODUCTS_EXCLUDE = "products.exclude"

    static final String PRODUCTS_INCLUDE = "products.include"
    static final String PRODUCTS_LOCATION = "products.location"
    static final String PRODUCTS_SEARCH_IN_OTHER_LOCATIONS = "products.search.in.other.locations"
    static final String PRODUCTS_LOCATION_DEV = "products.location.dev"
    static final String PRODUCTS_LOCATION_RELEASE = "products.location.release"
    static final String PRODUCTS_LOCATION_LOCAL = "products.location.local"
    static final String PRODUCTS_VERSION = "products.version"
    static final String PRODUCTS_CLEANUP = "products.cleanup"
    static final String PRODUCTS_CUSTOMIZATION_DIR = "products.customization.dir"
    static final String PRODUCTS_LOG_DEBUG = "products.log.debug"
    static final String PRODUCTS_CUSTOM_BRANCH = "products.custom.branch"
    static final String PRODUCTS_SKIP_START = "products.skip.start"

    static String RootPath = getenv("RootPath")

    static final String P_P1 = "P1"
    static final String P_P2 = "P2"
    static final String P_P3 = "P3"
    static final String P_P4 = "P4"
    static final String P_P5 = "P5"
    static final String P_P6 = "P6"
    static final String P_P7 = "P7"
    static final String P_P7II = "P7II"
    static final String P_P7EXT = "P7Extension"
    static final String P_P8 = "P8"
    static final String P_P9 = "P9"
    static final String P_P9II = "P9II"
    static final String P_P9EXT = "P9Extension"


    static Map<String, String> getProductDirs() {

        Map<String, String> p = new LinkedHashMap<>()
        p.put(P_P1, "$RootPath${File.separator}P1")
        def p2 = getenv("P2Root")
        p.put(P_P2, p2 == null ? "${RootPath}${File.separator}P2" : p2)
        def p3 = getenv("P3Root")
        p.put(P_P3, p3 == null ? "${RootPath}${File.separator}P3" : p3)
        def p4 = getenv("P4Root")
        p.put(P_P4, p4 == null ? "$RootPath${File.separator}P4" : p4)
        def p5 = getenv("P5Root")
        p.put(P_P5, p5 == null ? "$RootPath${File.separator}P5" : p5)
        p.put(P_P6, "${RootPath}${File.separator}P6")
        p.put(P_P7, "$RootPath${File.separator}dir${File.separator}P9-P7")
        p.put(P_P7II, "$RootPath${File.separator}dir${File.separator}P9-P7 II")
        p.put(P_P7EXT, "$RootPath${File.separator}dir${File.separator}P9-P7 Extension")
        def p8 = getenv("P8Root")
        p.put(P_P8, p8 == null ? "${RootPath}${File.separator}P8" : p8)
        def P9 = getenv("P3P9Root")
        p.put(P_P9, P9 == null ? "$RootPath${File.separator}P3ModuleForP9" : P9)
        def P92 = getenv("P3P9IIRoot")
        p.put(P_P9II, P92 == null ? "$RootPath${File.separator}P3ModuleForP9II" : P92)
        def P9Ex = getenv("P3P9ExtensionRoot")
        p.put(P_P9EXT, P9Ex == null ? "$RootPath${File.separator}P3ModuleForP9Extension" : P9Ex)
        return p
    }

    static getProductDirsFiltered() {
        String version = p[PRODUCTS_VERSION]
        Map<String, String> p = new LinkedHashMap<>()
        p.putAll(getProductDirs())
        if (version < "0.0.0") {
            p.remove(P_P7II)
            p.remove(P_P9II)
        } else {
            p.remove(P_P7EXT)
            p.remove(P_P9EXT)
        }
        return p
    }

    static getPrerequisites() {
        Map<String, List<String>> p = new LinkedHashMap<>()
        p.put(P_P1, [])
        p.put(P_P2, [])
        p.put(P_P3, [P_P2])
        p.put(P_P4, [P_P2, P_P3])
        p.put(P_P5, [P_P4])
        p.put(P_P6, [])
        p.put(P_P7, [])
        p.put(P_P7II, [])
        p.put(P_P7EXT, [])
        p.put(P_P8, [])
        p.put(P_P9, [P_P2, P_P3, P_P5, P_P7])
        p.put(P_P9II, [P_P2, P_P3, P_P5, P_P7, P_P9, P_P7II])
        p.put(P_P9EXT, [P_P2, P_P3, P_P5, P_P7, P_P9, P_P7EXT])
        return p
    }

    static Properties p = new Properties()

    static String get(String key) {
        p[key]
    }

    static boolean isAllCleanup() {
        p[PRODUCTS_CLEANUP] == "true"
    }

    static boolean isRelease() {
        p[PRODUCTS_VERSION] != "" && p[PRODUCTS_LOCATION] == "release"
    }

    static boolean isMasterBranch(String product) {
        if (p[PRODUCTS_CUSTOM_BRANCH] == "master") return true
        def map = getCustomBranchMap()
        def branch = map.get(product)
        return branch == null || branch == "master" || branch == "origin/master"
    }

    static Map<String, String> getCustomBranchMap() {
        Map<String, String> result = new HashMap<>()
        String customBranchParam = p[PRODUCTS_CUSTOM_BRANCH]
        if (customBranchParam != "master") {
            String[] params = customBranchParam.split(",")
            for (String p : params) {
                String[] values = p.trim().split("_", 2) // value P4_11111_bugfix will be split to P4 and 11111_bugfix
                if (values.length == 2 && getProductDirs().keySet().asList().contains(values[0])) {
                    result.put(values[0], values[1])
                } else {
                    throw new IllegalArgumentException("Incorrect -b parameter value")
                }
            }
        }
        return result
    }

    static String getLocalBuildsPath() {
        String folder = p[PRODUCTS_LOCATION_LOCAL]

        def f = new File(folder)
        if (!f.exists() && !f.mkdirs()) {
            PrintUtils.printlns("Can't create local folder : ${f.getAbsolutePath()}")
        }
        return folder
    }

    static String getReleaseProductPath(String product) {
        def path = p[PRODUCTS_LOCATION_RELEASE]
        def version = p[PRODUCTS_VERSION]
        if ("0.0.0".equalsIgnoreCase(version) && !P_P9.equalsIgnoreCase(product)) {
            version = "0.0.0"
        }
        def subDir = ""
        switch (product) {  //TODO: move this logic to options per product
            case P_P1:  //TODO: make constant for each product name
                subDir = "P1"
                break;
            case P_P7:
                if (version.toString().startsWith("0.0")) {
                    subDir = "dir${File.separator}Releases 0.0${File.separator}Release $version"
                    break
                }
            case P_P7II:
            case P_P7EXT:
                subDir = "dir${File.separator}Release $version"
                break
            case P_P4:
            case P_P5:
            case P_P2:
            case P_P8:
            case P_P6:
            case P_P3:
                subDir = "P2${File.separator}${product}"
                break
            default:
                subDir = "P2"
        }
        path += "${File.separator}${subDir}"
        return path
    }

    static String getDevProductPath(String product) {
        def path = p[PRODUCTS_LOCATION_DEV]
        def version = p[PRODUCTS_VERSION]
        if ("0.0.0".equalsIgnoreCase(version) && !P_P9.equalsIgnoreCase(product)) {
            version = "0.0.0"
        }
        def subDir = version
        switch (product) {
            case P_P7:
                subDir = "$subDir${File.separator}P9-P7"
                break
            case P_P7II:
                subDir = "$subDir${File.separator}P9P7II"
                break
            case P_P9:
            case P_P9II:
                subDir = "$subDir${File.separator}P6oduleFor$product"
                break
            case P_P1:
            case P_P4:
            case P_P5:
            case P_P2:
            case P_P8:
            case P_P6:
            case P_P3:
                subDir = "${subDir}${File.separator}${product}"
                break
        }
        path += "${File.separator}${subDir}"
        return path
    }

    static String getLocalProductPath(String product) {
        def path = p[PRODUCTS_LOCATION_LOCAL]
        def subDir = ""
        path += "${File.separator}${subDir}"
        return path
    }

    static String getProductPathByLocation(String product, String location) {
        switch (location) {
            case "local": return getLocalProductPath(product)
            case "dev": return getDevProductPath(product)
            case "release": return getReleaseProductPath(product)
        }
        throw new Exception("Unknown location: $location")
    }

    static String getLocationByPackagePath(String packagePath) {
        if (packagePath.startsWith(p[PRODUCTS_LOCATION_LOCAL])) {
            return "local"
        } else if (packagePath.startsWith(p[PRODUCTS_LOCATION_DEV])) {
            return "dev"
        } else if (packagePath.startsWith(p[PRODUCTS_LOCATION_RELEASE])) {
            return "release"
        }
        throw new Exception("Unknown location for path: $packagePath")
    }

    static String getProductPath(String product) {
        def location = p[PRODUCTS_LOCATION]
        return getProductPathByLocation(product, location)
    }
}
