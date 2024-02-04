package franzli347.project.core.netty;

import com.alibaba.fastjson2.JSON;
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
        Container container = new Container(config);
        container.start();
        final RegisterCenter registerCenter = registerAndSubscribe(config);
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
