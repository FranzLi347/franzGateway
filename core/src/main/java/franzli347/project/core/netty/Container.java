package franzli347.project.core.netty;

import franzli347.project.core.netty.processor.NettyCoreProcessor;
import franzli347.project.core.netty.processor.NettyProcessor;
import lombok.extern.slf4j.Slf4j;
import franzli347.project.common.utils.GatewayConst;

/**
 * @author FranzLi347
 * @ClassName: Container
 * @date 2/4/2024 10:40 AM
 */
@Slf4j
public class Container implements LifeCycle {

    private final Config config;

    private NettyHttpServer nettyHttpServer;

    private NettyHttpClient nettyHttpClient;

    private NettyProcessor nettyProcessor;

    public Container(Config config) {
        this.config = config;
        init();
    }

    @Override
    public void init() {
        NettyCoreProcessor nettyCoreProcessor = new NettyCoreProcessor();
        //如果启动要使用多生产者多消费组 那么我们读取配置
        if (GatewayConst.BUFFER_TYPE_PARALLEL.equals(config.getParallelBufferType())) {
            //todo Disruptor 优化
        } else {
            this.nettyProcessor = nettyCoreProcessor;
        }
        this.nettyHttpServer = new NettyHttpServer(config, nettyProcessor);
        this.nettyHttpClient = new NettyHttpClient(config, nettyHttpServer.getEventLoopGroupWorker());
        nettyHttpServer.init();
        nettyHttpClient.init();
    }

    @Override
    public void start() {
        nettyProcessor.start();
        nettyHttpServer.start();
        nettyHttpClient.start();
    }

    @Override
    public void shutdown() {
        nettyProcessor.shutDown();
        nettyHttpServer.shutdown();
    }
}
