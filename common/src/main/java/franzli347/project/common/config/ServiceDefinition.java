package franzli347.project.common.config;

import lombok.Builder;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * @author FranzLi347
 * @ClassName: ServiceDefinition
 * @date 2/4/2024 6:18 PM
 */
@Builder
public class ServiceDefinition implements Serializable {

    private static final long serialVersionUID = -8263365765897285189L;

    /**
     * 	唯一的服务ID: serviceId:version
     */
    private String uniqueId;

    /**
     * 	服务唯一id
     */
    private String serviceId;

    /**
     * 	服务的版本号
     */
    private String version;

    /**
     * 	服务的具体协议：http(mvc http) dubbo ..
     */
    private String protocol;

    /**
     * 	路径匹配规则：访问真实ANT表达式：定义具体的服务路径的匹配规则
     */
    private String patternPath;

    /**
     * 	环境名称
     */
    private String envType;

    /**
     * 	服务启用禁用
     */
    private boolean enable = true;

    /**
     * 	服务列表信息：
     */
    private Map<String, ServiceInvoker> invokerMap;


    public ServiceDefinition() {
        super();
    }

    public ServiceDefinition(String uniqueId, String serviceId, String version, String protocol, String patternPath,
                             String envType, boolean enable, Map<String, ServiceInvoker> invokerMap) {
        super();
        this.uniqueId = uniqueId;
        this.serviceId = serviceId;
        this.version = version;
        this.protocol = protocol;
        this.patternPath = patternPath;
        this.envType = envType;
        this.enable = enable;
        this.invokerMap = invokerMap;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getPatternPath() {
        return patternPath;
    }

    public void setPatternPath(String patternPath) {
        this.patternPath = patternPath;
    }

    public String getEnvType() {
        return envType;
    }

    public void setEnvType(String envType) {
        this.envType = envType;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Map<String, ServiceInvoker> getInvokerMap() {
        return invokerMap;
    }

    public void setInvokerMap(Map<String, ServiceInvoker> invokerMap) {
        this.invokerMap = invokerMap;
    }
}

