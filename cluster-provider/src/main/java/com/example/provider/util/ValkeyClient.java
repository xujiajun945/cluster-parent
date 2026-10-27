package com.example.provider.util;

import com.example.core.util.JsonUtils;
import io.valkey.springframework.data.valkey.core.ValkeyTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author xujiajun
 * @since 2026/10/27
 */
@Component
public class ValkeyClient {

    @Autowired
    private ValkeyTemplate<String, Object> valkeyTemplate;

    private String namespace;

    private String group;

    private Duration defaultExpireTime = Duration.ofMinutes(2);

    public void set(String key, Object value) {
        valkeyTemplate.opsForValue().set(key, value, defaultExpireTime);
    }

    public void set(String key, Object value, Duration expTime) {
        valkeyTemplate.opsForValue().set(key, value, expTime);
    }

    public Object setGet(String key, Object value) {
        Object o = valkeyTemplate.opsForValue().setGet(key, value, defaultExpireTime);
        return o;
    }

    public <T> T setGet(String key, Object value, Class<T> tClass) {
        T object = setGet(key, value, defaultExpireTime, tClass);
        return object;
    }

    public <T> T setGet(String key, Object value, Duration expTime, Class<T> tClass) {
        Object o = valkeyTemplate.opsForValue().setGet(key, value, expTime);
        T object = JsonUtils.parseObject(o, tClass);
        return object;
    }

    public Object get(String key) {
        Object value = valkeyTemplate.opsForValue().get(key);
        return value;
    }

    public <T> T get(String key, Class<T> tClass) {
        Object value = valkeyTemplate.opsForValue().get(key);
        T result = JsonUtils.parseObject(value, tClass);
        return result;
    }

    public List<Object> multiGet(Collection<String> keys) {
        List<Object> objects = valkeyTemplate.opsForValue().multiGet(keys);
        return objects;
    }

}
