package com.example.gateway.configuration;

import com.example.core.config.ValkeySessionConfig;
import com.example.core.util.TimeUtils;
import io.valkey.springframework.data.valkey.core.ReactiveValkeyTemplate;
import org.springframework.session.MapSession;
import org.springframework.session.ReactiveSessionRepository;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ValkeyReactiveSessionRepository implements ReactiveSessionRepository<MapSession> {

    private ReactiveValkeyTemplate<String, Object> valkeyTemplate;

    private String namespace;

    private String sessionPrefix;

    private String expirePrefix;

    private Duration maxInactiveInterval;

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public ValkeyReactiveSessionRepository(ReactiveValkeyTemplate<String, Object> valkeyTemplate,
                                           ValkeySessionConfig valkeySessionConfig) {
        this.valkeyTemplate = valkeyTemplate;
        this.namespace = valkeySessionConfig.getNamespace();
        this.sessionPrefix = valkeySessionConfig.getSessionPrefix();
        this.expirePrefix = valkeySessionConfig.getExpirePrefix();
        this.maxInactiveInterval = valkeySessionConfig.getMaxInactiveInterval();
    }

    @Override
    public Mono<MapSession> createSession() {
        MapSession mapSession = new MapSession();
        mapSession.setMaxInactiveInterval(maxInactiveInterval);
        return Mono.just(mapSession);
    }

    @Override
    public Mono<MapSession> findById(String sessionId) {
        String key = getSessionKey(sessionId);
        return valkeyTemplate.<String, Object>opsForHash().entries(key)
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .filter(map -> !map.isEmpty())
                .flatMap(entries -> {
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
                        return deleteById(sessionId).then(Mono.empty());
                    }
                    if (!entries.isEmpty()) {
                        // 回填attribute
                        entries.forEach(session::setAttribute);
                    }
                    return Mono.just(session);
                });
    }

    @Override
    public Mono<Void> save(MapSession session) {
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
        return valkeyTemplate.<String, Object>opsForHash().putAll(key, entries)
                .then(valkeyTemplate.expire(key, maxInactiveInterval)).then();
    }

    @Override
    public Mono<Void> deleteById(String sessionId) {
        String key = getSessionKey(sessionId);
        return valkeyTemplate.delete(key).then();
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
