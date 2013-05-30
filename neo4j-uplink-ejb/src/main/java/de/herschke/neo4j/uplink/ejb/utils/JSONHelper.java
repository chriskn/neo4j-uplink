package de.herschke.neo4j.uplink.ejb.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

/**
 * collects some helper methods for JSON handling.
 *
 * @author rhk
 */
public final class JSONHelper {

    private JSONHelper() {
        // don't allow instantiation.
    }

    public static JSONObject toDeepJSONObject(Map<String, Object> map) {
        JSONObject object = new JSONObject();
        for (Entry<String, Object> entry : map.entrySet()) {
            addToJSONObject(object, entry.getKey(), entry.getValue());
        }
        return object;
    }

    private static void addToJSONObject(JSONObject object, String prefix, Object value) {
        if (value == null || value instanceof String || value instanceof Double || value instanceof Float || value instanceof Number || value instanceof Boolean || value instanceof JSONStreamAware || value instanceof JSONAware || value instanceof Map || value instanceof List) {
            object.put(prefix, value);
        } else if (value instanceof Class) {
            object.put(prefix, ((Class) value).getSimpleName());
        } else {
            try {
                JSONObject innerObject = new JSONObject();
                for (Method method : value.getClass().getMethods()) {
                    if (!"getClass".equals(method.getName()) && (method.getName().startsWith("get") || method.getName().startsWith("is")) && method.getParameterTypes().length == 0 && method.getReturnType() != void.class) {
                        String name = method.getName().startsWith("get") ? (method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4)) : (method.getName().substring(2, 3).toLowerCase() + method.getName().substring(3));
                        final Object newValue = method.invoke(value);
                        addToJSONObject(object, prefix + "." + name, newValue);
                        innerObject.put(name, newValue);
                    }
                }
                object.put(prefix, innerObject);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new IllegalArgumentException(String.format("cannot build JSON from Object, due to: %s(%s)", ex.getClass().getSimpleName(), ex.getMessage()), ex);
            }
        }
    }
}
