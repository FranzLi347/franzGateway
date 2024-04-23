package franzli347.project.core.netty;

import com.alibaba.fastjson2.JSON;
import franzli347.project.common.config.DynamicConfigManager;
import franzli347.project.common.config.ServiceDefinition;
import franzli347.project.common.config.ServiceInstance;
import franzli347.project.common.utils.NetUtils;
import franzli347.project.core.utils.ConfigLoader;
import franzli347.project.register.center.api.RegisterCenter;
import franzli347.project.register.center.api.RegisterCenterListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;


/**
 * @author FranzLi347
 * @ClassName: Bootstrap
 * @date 2/4/2024 10:44 AM
 */
@Slf4j
public class Bootstrap {
    public static void main(String[] args) {
        Config config = ConfigLoader.getInstance().load(args);
        // ServiceLoader<ConfigCenter> serviceLoader = ServiceLoader.load(ConfigCenter.class);
        // final ConfigCenter configCenter = serviceLoader.findFirst().orElseThrow(() -> {
        //     log.error("not found ConfigCenter impl");
        //     return new RuntimeException("not found ConfigCenter impl");
        // });

        Container container = new Container(config);
        container.start();
        // 注册服务
        registerAndSubscribe(config);
        
    }

    private static RegisterCenter registerAndSubscribe(Config config) {
        ServiceLoader<RegisterCenter> serviceLoader = ServiceLoader.load(RegisterCenter.class);
        final RegisterCenter registerCenter = serviceLoader.findFirst().orElseThrow(() -> {
            log.error("not found RegisterCenter impl");
            return new RuntimeException("not found RegisterCenter impl");
        });
        registerCenter.init(config.getRegistryAddress(), config.getEnv());

        //构造网关服务定义和服务实例
        ServiceDefinition serviceDefinition = buildGatewayServiceDefinition(config);
        ServiceInstance serviceInstance = buildGatewayServiceInstance(config);

        //注册
        registerCenter.register(serviceDefinition, serviceInstance);

        registerCenter.registerListener(
            (serviceDefinition1, serviceInstanceSet) -> {
            log.info("refresh service and instance: {} {}", serviceDefinition1.getUniqueId(),
                    JSON.toJSON(serviceInstanceSet));
            DynamicConfigManager manager = DynamicConfigManager.getInstance();
            //将这次变更事件影响之后的服务实例再次添加到对应的服务实例集合
            manager.addServiceInstance(serviceDefinition1.getUniqueId(), serviceInstanceSet);
            //修改发生对应的服务定义
            manager.putServiceDefinition(serviceDefinition1.getUniqueId(), serviceDefinition1);
        });

        registerCenter.subscribeAllServices();
        return registerCenter;
    }

    private static ServiceInstance buildGatewayServiceInstance(Config config) {
        String localIp = NetUtils.getLocalIp();
        int port = config.getPort();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId(localIp + ":" + port);
        serviceInstance.setIp(localIp);
        serviceInstance.setPort(port);
        serviceInstance.setRegisterTime(System.currentTimeMillis());
        return serviceInstance;
    }

    private static ServiceDefinition buildGatewayServiceDefinition(Config config) {
        ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.setInvokerMap(Map.of());
        serviceDefinition.setUniqueId(config.getApplicationName());
        serviceDefinition.setServiceId(config.getApplicationName());
        serviceDefinition.setEnvType(config.getEnv());
        return serviceDefinition;
    }
}
