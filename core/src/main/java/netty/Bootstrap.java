package netty;

import utils.ConfigLoader;

/**
 * @author FranzLi347
 * @ClassName: Bootstrap
 * @date 2/4/2024 10:44 AM
 */
public class Bootstrap {
    public static void main(String[] args) {
        Config config = ConfigLoader.getInstance().load(args);
        Container container = new Container(config);
        container.start();
    }
}
