package com.example.consumer.listener;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.annotation.NacosConfigListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author xujiajun
 * @since 2026/10/16
 */
@Component
@Slf4j
public class DemoNacosConfigListener {

    @Autowired
    private NacosConfigManager configManager;

    @NacosConfigListener(dataId = "application-dev.yml", group = "cluster-provider")
    public void demoConfigListener(String config) throws NoSuchMethodException {
        Class<DemoNacosConfigListener> demoNacosConfigListenerClass = DemoNacosConfigListener.class;
        Method demoConfigListener = demoNacosConfigListenerClass.getDeclaredMethod("demoConfigListener", String.class);
        NacosConfigListener declaredAnnotation = demoConfigListener.getDeclaredAnnotation(NacosConfigListener.class);
        String dataId = declaredAnnotation.dataId();
        String groupId = declaredAnnotation.group();
        String serverStatus = configManager.getConfigService().getServerStatus();
        log.info("nacos config change: dataId: {} \n groupId: {} \n config: {}", dataId, groupId, config);
    }

    @NacosConfigListener(dataId = "application-dev.yml", group = "cluster-consumer")
    public void demoConfigListenerGroup(String config) throws NoSuchMethodException {
        Class<DemoNacosConfigListener> demoNacosConfigListenerClass = DemoNacosConfigListener.class;
        Method demoConfigListener = demoNacosConfigListenerClass.getDeclaredMethod("demoConfigListenerGroup", String.class);
        NacosConfigListener declaredAnnotation = demoConfigListener.getDeclaredAnnotation(NacosConfigListener.class);
        String dataId = declaredAnnotation.dataId();
        String groupId = declaredAnnotation.group();
        String serverStatus = configManager.getConfigService().getServerStatus();
        log.info("nacos config change: dataId: {} \n groupId: {} \n config: {}", dataId, groupId, config);
    }
}
