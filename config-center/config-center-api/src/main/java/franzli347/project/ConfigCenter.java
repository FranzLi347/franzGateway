package franzli347.project.register.center.api;

/**
 * @author FranzLi347
 * @ClassName: ConfigCenter
 * @date 2/4/2024 6:49 PM
 */
public interface ConfigCenter {
    /**
     * 初始化配置中心配置
     * @param serverAddr  配置中心地址
     * @param env 环境
     */
    void init(String serverAddr, String env);


    /**
     * 订阅配置中心配置变更
     * @param listener  配置变更监听器
     */
    void subscribeRulesChange(RulesChangeListener listener);
}