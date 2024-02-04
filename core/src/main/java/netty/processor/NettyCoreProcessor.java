package netty.processor;

import context.HttpRequestWrapper;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;

/**
 * @author FranzLi347
 * @ClassName: NettyCoreProcessor
 * @date 2/4/2024 10:23 AM
 */
public class NettyCoreProcessor implements NettyProcessor{

//    private FilterChainFactory filterChainFactory = GatewayFilterChainChainFactory.getInstance();
    @Override
    public void process(HttpRequestWrapper wrapper) {
        FullHttpRequest request = wrapper.getRequest();
        ChannelHandlerContext ctx = wrapper.getCtx();
        // todo 通过 GatewayContext 和 GatewayFilterChain 执行过滤器链逻辑
        if (request.uri().equals("/")){
            // todo 通过记录日志并发送适当的 HTTP 响应处理已知异常。
            DefaultHttpHeaders headers = new DefaultHttpHeaders();
            headers.set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            headers.set(HttpHeaderNames.SERVER, "gateway");
            FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                    request.protocolVersion(), HttpResponseStatus.OK, ctx.alloc().buffer().writeBytes("Hello World".getBytes()), headers, headers);
            doWriteAndRelease(ctx, request, httpResponse);
        }

    }

    private void doWriteAndRelease(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse httpResponse) {
        ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE); // 发送响应后关闭通道。
        ReferenceCountUtil.release(request); // 释放与请求相关联的资源。
    }
    @Override
    public void start() {

    }

    @Override
    public void shutDown() {

    }
}
