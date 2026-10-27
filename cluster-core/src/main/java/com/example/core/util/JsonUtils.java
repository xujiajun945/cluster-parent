package com.example.core.util;

import com.example.core.model.User;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import java.io.Serializable;
import java.util.*;

/**
 * json工具类--jackson实现
 * jackson反序列化依赖对象setter方法，否则为null
 *
 * @author xujiajun
 * @since 2026/10/19
 */
@Slf4j
public class JsonUtils {

    // JsonMapper线程安全
    private static final JsonMapper PARSER;

    static {
        PARSER = JsonMapper.builder()
                .defaultTimeZone(TimeZone.getDefault())
                // 反序列化时, 当出现未知属性, 不失败
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    private JsonUtils() {
        throw new RuntimeException("util class can not be init!");
    }

    public static String toJson(Object o) {
        String json = PARSER.writeValueAsString(o);
        return json;
    }

    public static Object parseObject(String json) {
        return parseObject(json, Object.class);
    }

    public static <T> T parseObject(String json, Class<T> t) {
        if (json == null) {
            return null;
        }
        T Object = PARSER.readValue(json, t);
        return Object;
    }

    public static <T> T parseObject(Object o, Class<T> t) {
        String json = toJson(o);
        T object = parseObject(json, t);
        return object;
    }

    public static <T> List<T> parseArray(String json, Class<T> tClass) {
        return parseArray(json, ArrayList.class, tClass);
    }

    public static <T> List<T> parseArray(String json, Class<? extends List> listClass, Class<T> tClass) {
        if (json == null) {
            return null;
        }
        JavaType javaType = PARSER.getTypeFactory().constructParametricType(listClass, tClass);
        List<T> list = PARSER.readValue(json, javaType);
        return list;
    }

    public static <k, v> Map<k, v> parseMap(String json, Class<k> k, Class<v> v) {
        return parseMap(json, HashMap.class, k, v);
    }

    public static <k, v> Map<k, v> parseMap(String json, Class<? extends Map> mapClass, Class<k> k, Class<v> v) {
        if (json == null) {
            return null;
        }
        JavaType javaType = PARSER.getTypeFactory().constructParametricType(mapClass, k, v);
        Map<k, v> map = PARSER.readValue(json, javaType);
        return map;
    }

    /**
     * 复杂嵌套json
     * example:
     * String multiJson = "[" +
     *                         "{\"param1\": \"value1\", \"param2\": \"value2\"}, " +
     *                         "{\"param3\": \"value3\", \"param4\": \"value4\"}, " +
     *                         "{\"param5\": \"value5\", \"param6\": \"value6\"}"
     *                  + "]";
     * List<Map<String, String>> result = parse(multiJson, ArrayList.class, HashMap.class);
     *
     */
    public static <T> T parse(String json, Class<T> tClass, Class<?>... classes) {
        if (json == null) {
            return null;
        }
        JavaType javaType = PARSER.getTypeFactory().constructParametricType(tClass, classes);
        T t = PARSER.readValue(json, javaType);
        return t;
    }

    public static void main(String[] args) {
//        String json = "{\"username\": \"xujiajun\", \"son\": {\"name\": \"xunianan\", \"age\": \"1\"}}";
//        String json2 = "{\"son\": {\"name\": \"xunianan\", \"age\": \"1\"}}";
//        String intJson = "[1, 2, 3]";
//        String userListJson = "[{\"name\": \"xunianan\", \"age\": \"1\"}, {\"name\": \"xunianan2\", \"age\": \"2\"}]";
//        Map<String, User> map = parseMap(json2, String.class, User.class);
//        List<Integer> integerList = parseArray(intJson, Integer.class);
//        List<User> userList = parseArray(userListJson, User.class);
//        List<User> userList2 = parseArray(userListJson, LinkedList.class, User.class);
//        User son = map.get("son");
//        System.out.println(integerList);
//        System.out.println(userList);
//
//        // 复杂嵌套json
//        String multiJson = "[" +
//                                "{\"param1\": \"value1\", \"param2\": \"value2\"}, " +
//                                "{\"param3\": \"value3\", \"param4\": \"value4\"}, " +
//                                "{\"param5\": \"value5\", \"param6\": \"value6\"}"
//                         + "]";
//        List<Map<String, String>> parse = parse(multiJson, ArrayList.class, HashMap.class);

        User user = parseObject(null, User.class);
        System.out.println(user);
        String json = toJson(null);
        System.out.println(json);
    }

    @Setter
    @Getter
    private static class User implements Serializable {
        private String name;
        private Integer age;
    }
}
