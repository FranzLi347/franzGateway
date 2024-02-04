package franzli347.project.common.config;

/**
 * @author FranzLi347
 * @ClassName: ServiceInvoker
 * @date 2/4/2024 6:19 PM
 */
public interface ServiceInvoker {

    /**
     * 获取真正的服务调用的全路径
     */
    String getInvokerPath();

    void setInvokerPath(String invokerPath);

    /**
     * 获取该服务调用(方法)的超时时间
     */
    int getTimeout();

    void setTimeout(int timeout);

}
