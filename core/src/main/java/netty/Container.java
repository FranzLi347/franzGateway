package netty;

import lombok.extern.slf4j.Slf4j;
import netty.processor.NettyCoreProcessor;
import netty.processor.NettyProcessor;
import utils.GateWayConstant;

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
        if (GateWayConstant.BUFFER_TYPE_PARALLEL.equals(config.getParallelBufferType())) {
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
