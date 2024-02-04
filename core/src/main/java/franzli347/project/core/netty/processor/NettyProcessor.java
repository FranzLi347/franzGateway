package franzli347.project.core.netty.processor;

import franzli347.project.core.context.HttpRequestWrapper;

/**
 * @author FranzLi347
 * @ClassName: NettyProcessor
 * @date 2/4/2024 10:21 AM
 */
public interface NettyProcessor {
    void process(HttpRequestWrapper wrapper);

    void start();

    void shutDown();
}
