package franzli347.project.register.center.api;

import franzli347.project.common.config.ServiceDefinition;
import franzli347.project.common.config.ServiceInstance;

import java.util.Set;

/**
 * @author FranzLi347
 * @ClassName: RegisterCenterListener
 * @date 2/4/2024 6:21 PM
 */
public interface RegisterCenterListener {

    void onChange(ServiceDefinition serviceDefinition,
                  Set<ServiceInstance> serviceInstanceSet);
}