package com.example.consumer.configuration;

import com.example.core.util.TimeUtils;
import io.valkey.springframework.data.valkey.core.ValkeyTemplate;
import org.jspecify.annotations.Nullable;
import org.springframework.session.MapSession;
import org.springframework.session.SessionRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ValkeySessionRepository implements SessionRepository<MapSession> {

    private ValkeyTemplate<String, Object> valkeyTemplate;

    private String namespace;

    private String sessionPrefix;

    private String expirePrefix;

    private Duration maxInactiveInterval;

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public ValkeySessionRepository(ValkeyTemplate<String, Object> valkeyTemplate, ValkeySessionConfig valkeySessionConfig) {
        this.valkeyTemplate = valkeyTemplate;
        this.namespace = valkeySessionConfig.getNamespace();
        this.sessionPrefix = valkeySessionConfig.getSessionPrefix();
        this.expirePrefix = valkeySessionConfig.getExpirePrefix();
        this.maxInactiveInterval = valkeySessionConfig.getMaxInactiveInterval();
    }

    @Override
    public MapSession createSession() {
        MapSession mapSession = new MapSession();
        mapSession.setMaxInactiveInterval(maxInactiveInterval);
        return mapSession;
    }

    @Override
    public @Nullable MapSession findById(String sessionId) {
        String key = getSessionKey(sessionId);

        Map<String, Object> entries = this.valkeyTemplate.<String, Object>opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        MapSession session = new MapSession(sessionId);

        String creationTimeStr = String.valueOf(entries.get("creationTime"));
        Instant creationTime = TimeUtils.parseInstant(creationTimeStr, DATE_TIME_FORMAT);
        session.setCreationTime(creationTime);
        entries.remove("creationTime");

        Long interval = Long.valueOf(String.valueOf(entries.get("maxInactiveInterval")));
        Duration maxInactiveInterval = Duration.ofSeconds(interval);
        session.setMaxInactiveInterval(maxInactiveInterval);
        entries.remove("maxInactiveInterval");

        String lastAccessedTimeStr = String.valueOf(entries.get("lastAccessedTime"));
        Instant lastAccessedTime = TimeUtils.parseInstant(lastAccessedTimeStr, DATE_TIME_FORMAT);
        session.setLastAccessedTime(lastAccessedTime);
        entries.remove("lastAccessedTime");

        if (session.isExpired()) {
            deleteById(sessionId);
            return null;
        }
        if (!entries.isEmpty()) {
            // 回填attribute
            entries.forEach(session::setAttribute);
        }
        return session;
    }

    @Override
    public void save(MapSession session) {
        String key = getSessionKey(session.getId());

        Map<String, Object> entries = new HashMap<>();
        String creationTimeStr = TimeUtils.format(session.getCreationTime(), DATE_TIME_FORMAT);
        entries.put("creationTime", creationTimeStr);

        Duration maxInactiveInterval = session.getMaxInactiveInterval();
        Long seconds = maxInactiveInterval.toSeconds();
        entries.put("maxInactiveInterval", seconds);

        String lastAccessedTimeStr = TimeUtils.format(session.getLastAccessedTime(), DATE_TIME_FORMAT);
        entries.put("lastAccessedTime", lastAccessedTimeStr);

        setSessionAttribute(session, entries);

        this.valkeyTemplate.<String, Object>opsForHash().putAll(key, entries);
        // TTL
        valkeyTemplate.expire(key, maxInactiveInterval);
    }

    @Override
    public void deleteById(String sessionId) {
        String key = getSessionKey(sessionId);
        valkeyTemplate.delete(key);
    }

    private String getSessionKey(String sessionId) {
        return namespace + sessionPrefix + sessionId;
    }

    private void setSessionAttribute(MapSession session, Map<String, Object> targetMap) {
        session.getAttributeNames()
                .forEach(attributeName -> {
                    Object attribute = session.getAttribute(attributeName);
                    targetMap.putIfAbsent(attributeName, attribute);
                });
    }
}
