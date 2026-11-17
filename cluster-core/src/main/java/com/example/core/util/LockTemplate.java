package com.example.core.util;

import com.example.core.exception.LockException;
import io.valkey.springframework.data.valkey.core.ValkeyTemplate;
import io.valkey.springframework.data.valkey.core.script.DefaultValkeyScript;
import io.valkey.springframework.data.valkey.core.script.ValkeyScript;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 分布式锁工具类，封装加锁解锁逻辑，封装看门狗逻辑
 *
 * @author xujiajun
 * @since 2026/11/17
 */
@Slf4j
public class LockTemplate {

    /**
     * 释放分布式锁Lua脚本：KEYS[1]=lockKey，ARGV[1]=自己的uuid
     */
    private static final String UNLOCK_LUA =
            "if valkey.call('GET', KEYS[1]) == ARGV[1] then " +
                    "    return valkey.call('DEL', KEYS[1]) " +
                    "else " +
                    "    return 0 " +
                    "end";

    /**
     * 封装脚本对象，只初始化一次
     */
    private static final ValkeyScript<Long> UNLOCK_SCRIPT = new DefaultValkeyScript<>(UNLOCK_LUA, Long.class);

    private ValkeyTemplate<String, Object> valkeyTemplate;

    public LockTemplate(ValkeyTemplate<String, Object> valkeyTemplate) {
        this.valkeyTemplate = valkeyTemplate;
    }

    public <T> T lockWithDog(String lockKey, Duration ttl, Supplier<T> action) {
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = valkeyTemplate.opsForValue().setIfAbsent(lockKey, lockValue, ttl);
        if (!locked) {
            log.error("Failed to acquire lock:{}", lockKey);
            throw new LockException("Failed to acquire lock: " + lockKey);
        }
        WatchDog watchDog = new WatchDog(valkeyTemplate, lockKey, lockValue, ttl);
        watchDog.start();
        try {
            return action.get();
        } finally {
            if (watchDog.isRunning() && !watchDog.isLockLost()) {
                watchDog.stop();
            }
            valkeyTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(lockKey), lockValue);
        }
    }

}
