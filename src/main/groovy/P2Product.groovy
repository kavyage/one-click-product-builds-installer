import static InstallerProperties.get
import static InstallerProperties.getProductDirs

public class P2Product extends BaseProduct {
    public P2Product() {
        super(InstallerProperties.P_P2);
    }

    @Override
    public void install() {
        super.install();
    }

    @Override
    public void installProduct() {
        boolean searchInOtherLocations = Boolean.parseBoolean(get(PRODUCTS_SEARCH_IN_OTHER_LOCATIONS))
        def buildPath = ProductInstaller.findBuildPath(this.product, searchInOtherLocations)
        if (buildPath == null) {
            throw new Exception("*** ERROR ***  Failed to install $product (installation package is not found)")
        }
        if (!ProductInstaller.checkPrerequisites(product)) throw new Exception("*** ERROR ***  Failed to install $product (prerequisites are not satisfied)")
        String additionalParameters = ""//" -l xe-install.log"
        ProductInstaller.install(product, buildPath, additionalParameters)

        if (!ProductState.isProductInstalled(product)) {
            throw new Exception("Error installing $product to " + getProductDirs().get(product))
        }
    }
}
