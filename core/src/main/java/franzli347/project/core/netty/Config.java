package franzli347.project.core.netty;

import lombok.Data;

/**
 * @author FranzLi347
 * @ClassName: Config
 * @date 2/4/2024 9:50 AM
 */
@Data
public class Config {

    private Integer eventLoopGroupBossNum = 1;

    /**
     * worker线程组的线程数 默认为CPU核数*2
     */
    private Integer eventLoopGroupWorkerNum = Runtime.getRuntime().availableProcessors() * 2;

    private Integer port = 8088;

    private Integer maxContentLength = 1024 * 1024 * 100;

    private int httpConnectTimeout = 30 * 1000;

    private Integer httpRequestTimeout = 1000;

    private Integer httpMaxRequestRetry = 3;

    private Integer httpMaxConnections = 100;

    private Integer httpConnectionsPerHost = 100;

    private Integer httpPooledConnectionIdleTimeout = 1000;

    private String parallelBufferType = "none";

    private String registryAddress = "127.0.0.1:8848";

    private String env = "dev";

    private String applicationName = "gateway";


}
