package org.hot.batis.util;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalUtil {

    private static ThreadLocal<Map<String,Object>> threadLocalMap = new ThreadLocal<>();

    public static Object get(String key) {
        Map<String,Object> map = threadLocalMap.get();
        if (map == null) return null;
        return map.get(key);
    }

    public static void set(String key,Object value) {
        Map<String,Object> map = threadLocalMap.get() == null ? new HashMap<>() : threadLocalMap.get();
        map.put(key,value);
        threadLocalMap.set(map);
    }


    public static  void clear() {
        threadLocalMap.remove();
    }


}
