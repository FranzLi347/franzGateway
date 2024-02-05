package franzli347.project.register.center.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.utils.CollectionUtils;
import franzli347.project.common.Constant;
import franzli347.project.common.config.ServiceDefinition;
import franzli347.project.common.config.ServiceInstance;
import franzli347.project.common.utils.GatewayConst;
import franzli347.project.register.center.api.RegisterCenter;
import franzli347.project.register.center.api.RegisterCenterListener;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.alibaba.nacos.api.naming.pojo.ServiceInfo;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author FranzLi347
 * @ClassName: NacosRegisterCenter
 * @date 2/4/2024 6:23 PM
 */
@Slf4j
public class NacosRegisterCenter implements RegisterCenter {

    /**
     * 注册中心的地址
     */
    private String registerAddress;

    /**
     * 环境选择
     */
    private String env;

    /**
     * 主要用于维护服务实例信息
     */
    private NamingService namingService;

    /**
     * 主要用于维护服务定义信息
     */
    private NamingMaintainService namingMaintainService;

    /**
     * 监听器列表
     * 这里由于监听器可能变更 会出现线程安全问题
     */
    private List<RegisterCenterListener> registerCenterListenerList = new CopyOnWriteArrayList<>();

    @Override
    public void init(String registerAddress, String env) {
        this.registerAddress = registerAddress;
        this.env = env;
        try {
            this.namingMaintainService = NamingMaintainFactory.createMaintainService(registerAddress);
            this.namingService = NamingFactory.createNamingService(registerAddress);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        try {
            //构造nacos实例信息
            Instance nacosInstance = new Instance();
            nacosInstance.setInstanceId(serviceInstance.getServiceInstanceId());
            nacosInstance.setPort(serviceInstance.getPort());
            nacosInstance.setIp(serviceInstance.getIp());
            //实例信息可以放入到metadata中
            nacosInstance.setMetadata(Map.of(GatewayConst.META_DATA_KEY, JSON.toJSONString(serviceInstance)));
            //注册
            namingService.registerInstance(serviceDefinition.getServiceId(), env, nacosInstance);
            namingMaintainService.updateService(serviceDefinition.getServiceId(), env, 0,
                    Map.of(GatewayConst.META_DATA_KEY, JSON.toJSONString(serviceDefinition)));
            log.info("register {} {}", serviceDefinition, serviceInstance);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void deregister(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        try {
            //进行服务注销
            namingService.deregisterInstance(serviceDefinition.getServiceId(), env, serviceInstance.getIp(), serviceInstance.getPort());
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerListener(RegisterCenterListener registerCenterListener) {
        //服务订阅首先需要将我们的监听器加入到我们的服务列表中
        registerCenterListenerList.add(registerCenterListener);
    }

    @Override
    public void subscribeAllServices() {
        //进行服务订阅
        doSubscribeAllServices();
        addSubscribeTask();
    }

    private void addSubscribeTask() {
        //可能有新服务加入，所以需要有一个定时任务来检查
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1, new NameThreadFactory("doSubscribeAllServices"));
        //循环执行服务发现与订阅操作
        scheduledThreadPool.scheduleWithFixedDelay(this::doSubscribeAllServices, 10, 10, TimeUnit.SECONDS);
    }

    private void doSubscribeAllServices() {
        try {
            //得到当前服务已经订阅的服务
            //这里其实已经在init的时候初始化过namingservice了，所以这里可以直接拿到当前服务已经订阅的服务
            Set<String> subscribeService = namingService.getSubscribeServices().stream().map(ServiceInfo::getName).collect(Collectors.toSet());

            int pageNo = 1;
            int pageSize = 100;

            //分页从nacos拿到所有的服务列表
            List<String> serviseList = namingService.getServicesOfServer(pageNo, pageSize, env).getData();

            //拿到所有的服务名称后进行遍历
            while (CollectionUtils.isNotEmpty(serviseList)) {
                log.info("service list size {}", serviseList.size());
                for (String service : serviseList) {
                    //判断是否已经订阅了当前服务
                    if (subscribeService.contains(service)) {
                        continue;
                    }
                    //nacos事件监听器 订阅当前服务
                    //这里我们需要自己实现一个nacos的事件订阅类 来具体执行订阅执行时的操作
                    EventListener eventListener = new NacosRegisterListener();
                    eventListener.onEvent(new NamingEvent(service, null));
                    namingService.subscribe(service, env, eventListener);
                    log.info("subscribe {} {}", service, env);
                }
                //遍历下一页的服务列表
                serviseList = namingService.getServicesOfServer(++pageNo, pageSize, env).getData();
            }
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }
   public class NacosRegisterListener implements EventListener {

        @Override
        public void onEvent(Event event) {
            //先判断是否是注册中心事件
            if (event instanceof NamingEvent) {
                log.info("the triggered event info is：{}", JSON.toJSON(event));
                NamingEvent namingEvent = (NamingEvent) event;
                //获取当前变更的服务名
                String serviceName = namingEvent.getServiceName();

                try {
                    //获取服务定义信息
                    Service service = namingMaintainService.queryService(serviceName, env);
                    //得到服务定义信息
                    ServiceDefinition serviceDefinition =
                            JSON.parseObject(service.getMetadata().get(GatewayConst.META_DATA_KEY),
                                    ServiceDefinition.class);

                    //获取服务实例信息
                    List<Instance> allInstances = namingService.getAllInstances(service.getName(), env);
                    Set<ServiceInstance> set = new HashSet<>();

                    //**
     /* meta-data数据如下
     * {
     *   "version": "1.0.0",
     *   "environment": "production",
     *   "weight": 80,
     *   "region": "us-west",
     *   "labels": "web, primary",
     *   "description": "Main production service"
     * }
     */
                    for (Instance instance : allInstances) {
                        ServiceInstance serviceInstance =
                                JSON.parseObject(instance.getMetadata().get(GatewayConst.META_DATA_KEY),
                                        ServiceInstance.class);
                        set.add(serviceInstance);
                    }
                    //调用我们自己的订阅监听器
                    registerCenterListenerList.forEach(registerCenterListener -> registerCenterListener.onChange(serviceDefinition, set));
                } catch (NacosException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
