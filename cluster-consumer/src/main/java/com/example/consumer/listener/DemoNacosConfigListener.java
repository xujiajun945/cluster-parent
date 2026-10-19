package com.example.consumer.listener;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author xujiajun
 * @since 2026/10/16
 */
@Component
@Slf4j
public class DemoNacosConfigListener {

    @NacosInjected
    private ConfigService configService;

    @NacosConfigListener(dataId = "demo")
    public void demoConfigListener(String config) throws NoSuchMethodException {
        Class<DemoNacosConfigListener> demoNacosConfigListenerClass = DemoNacosConfigListener.class;
        Method demoConfigListener = demoNacosConfigListenerClass.getDeclaredMethod("demoConfigListener", String.class);
        NacosConfigListener declaredAnnotation = demoConfigListener.getDeclaredAnnotation(NacosConfigListener.class);
        String dataId = declaredAnnotation.dataId();
        String groupId = declaredAnnotation.groupId();
        String serverStatus = configService.getServerStatus();
        log.info("nacos config change: serverStatus: {} \n dataId: {} \n groupId: {} \n config: {}", serverStatus, dataId, groupId, config);
    }

    @NacosConfigListener(dataId = "demo", groupId = "default")
    public void demoConfigListenerGroup(String config) throws NoSuchMethodException {
        Class<DemoNacosConfigListener> demoNacosConfigListenerClass = DemoNacosConfigListener.class;
        Method demoConfigListener = demoNacosConfigListenerClass.getDeclaredMethod("demoConfigListenerGroup", String.class);
        NacosConfigListener declaredAnnotation = demoConfigListener.getDeclaredAnnotation(NacosConfigListener.class);
        String dataId = declaredAnnotation.dataId();
        String groupId = declaredAnnotation.groupId();
        String serverStatus = configService.getServerStatus();
        log.info("nacos config change: serverStatus: {} \n dataId: {} \n groupId: {} \n config: {}", serverStatus, dataId, groupId, config);
    }
}
