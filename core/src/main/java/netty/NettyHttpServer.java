package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import netty.channalHandler.NettyHttpServerHandler;
import netty.channalHandler.NettyServerConnectManagerHandler;
import netty.processor.NettyProcessor;

import java.net.InetSocketAddress;

/**
 * @author FranzLi347
 * @ClassName: NettyHttpServer
 * @date 2/4/2024 9:45 AM
 */
@Slf4j
public class NettyHttpServer implements LifeCycle {

    public NettyHttpServer(Config config, NettyProcessor nettyProcessor) {
        this.config = config;
        this.nettyProcessor = nettyProcessor;
    }
    private final Config config;
    // 自定义的Netty处理器接口，用于定义如何处理接收到的请求
    private final NettyProcessor nettyProcessor;
    // 服务器引导类，用于配置和启动Netty服务
    private ServerBootstrap serverBootstrap;
    // boss线程组，用于处理新的客户端连接
    private EventLoopGroup eventLoopGroupBoss;
    // worker线程组，用于处理已经建立的连接的后续操作
    @Getter
    private EventLoopGroup eventLoopGroupWorker;

    @Override
    public void init() {
        this.serverBootstrap = new ServerBootstrap();
        if (Epoll.isAvailable()) {
            this.eventLoopGroupBoss = new EpollEventLoopGroup(config.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("epoll-netty-boss-nio"));
            this.eventLoopGroupWorker = new EpollEventLoopGroup(config.getEventLoopGroupWorkerNum(),
                    new DefaultThreadFactory("epoll-netty-worker-nio"));
        } else {
            // 否则使用默认的NIO模型
            this.eventLoopGroupBoss = new NioEventLoopGroup(config.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("default-netty-boss-nio"));
            this.eventLoopGroupWorker = new NioEventLoopGroup(config.getEventLoopGroupWorkerNum(),
                    new DefaultThreadFactory("default-netty-worker-nio"));
        }
        serverBootstrap.group(eventLoopGroupBoss, eventLoopGroupWorker)
                .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_SNDBUF, 65535)
                .childOption(ChannelOption.SO_RCVBUF, 65535)
                .localAddress(new InetSocketAddress(config.getPort()))
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        // 添加自定义的处理器
                        ch.pipeline().addLast(
                                new HttpServerCodec(),
                                new HttpObjectAggregator(config.getMaxContentLength()),
                                new ChunkedWriteHandler(),
                                new NettyHttpServerHandler(nettyProcessor),
                                new NettyServerConnectManagerHandler()
                        );
                    }
                });
    }

    @Override
    public void start() {
        try {
            serverBootstrap.bind().sync();
            log.info("gateway start success, running in port: {}", config.getPort());
        } catch (InterruptedException e) {
            log.error("gateway start fail reason", e);
        }
    }

    @Override
    public void shutdown() {
        eventLoopGroupBoss.shutdownGracefully();
        eventLoopGroupWorker.shutdownGracefully();
    }


}
