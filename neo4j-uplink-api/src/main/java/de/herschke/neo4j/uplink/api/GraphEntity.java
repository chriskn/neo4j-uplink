package de.herschke.neo4j.uplink.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;

/**
 * the base class for node or relationships of a graph
 *
 * @author rhk
 */
public abstract class GraphEntity {

    protected static final Pattern selfUrlPattern = Pattern.compile("http://.+/db/data/(node|relationship)/(\\d+)");
    private final int id;
    protected final JSONObject entity;

    public GraphEntity(String type, JSONObject entity) {
        if (!entity.containsKey("self") || !entity.containsKey("data")) {
            throw new IllegalArgumentException("given map is not a graphEntity, must contain 'self' and 'data' entry!");
        }
        String selfUrl = (String) entity.get("self");
        Matcher m = selfUrlPattern.matcher(selfUrl);
        if (m.matches()) {
            if (type.equalsIgnoreCase(m.group(1))) {
                this.id = Integer.parseInt(m.group(2));
                this.entity = entity;
            } else {
                throw new IllegalArgumentException("map is not of type: " + type);
            }
        } else {
            throw new IllegalArgumentException("self entry of map must match: " + selfUrlPattern.pattern());
        }
    }

    public int getId() {
        return this.id;
    }

    public Object getPropertyValue(String name) {
        return ((JSONObject) entity.get("data")).get(name);
    }

    public boolean hasProperty(String name) {
        return ((JSONObject) entity.get("data")).containsKey(name);
    }
}
