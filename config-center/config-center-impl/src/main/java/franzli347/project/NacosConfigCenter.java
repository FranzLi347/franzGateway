package blossom.project.config.center.nacos.impl;


import blossom.project.common.config.Rule;
import blossom.project.config.center.api.ConfigCenter;
import blossom.project.config.center.api.RulesChangeListener;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executor;


/*
 * @Author: franzli 
 * @Date: 2024-04-23 08:13:30 
 * @Last Modified by: franzli
 * @Last Modified time: 2024-04-23 08:16:29
 */
@Slf4j
public class NacosConfigCenter implements ConfigCenter {

    /**
     * 需要拉取的服务配置的DATA_ID 要求自定义
     */
    private static final String DATA_ID = "api-gateway";


    /**
     * 服务端地址
     */
    private String serverAddr;

    /**
     * 环境
     */
    private String env;

    /**
     * Nacos提供的与配置中心进行交互的接口
     */
    private ConfigService configService;

    @Override
    public void init(String serverAddr, String env) {
        this.serverAddr = serverAddr;
        this.env = env;

        try {
            this.configService = NacosFactory.createConfigService(serverAddr);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribeRulesChange(RulesChangeListener listener) {
        try {
            //初始化通知 DATA_ID是自己定义的 返回值就是一个json
            String configJson = configService.getConfig(DATA_ID, env, 5000);
            //configJson : {"rules":[{}, {}]}
            log.info("config from nacos: {}", configJson);
            List<Rule> rules = JSON.parseObject(configJson).getJSONArray("rules").toJavaList(Rule.class);
            listener.onRulesChange(rules);

            //监听变化
            configService.addListener(DATA_ID, env, new Listener() {
                //是否使用额外线程执行
                @Override
                public Executor getExecutor() {
                    return null;
                }
                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("config from nacos: {}", configInfo);
                    List<Rule> rules = JSON.parseObject(configInfo).getJSONArray("rules").toJavaList(Rule.class);
                    listener.onRulesChange(rules);
                }
            });

        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }
}
