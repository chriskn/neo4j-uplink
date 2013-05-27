package de.herschke.neo4j.uplink.api;

import java.util.regex.Matcher;
import org.json.simple.JSONObject;

/**
 * a simple relationship object.
 *
 * @author rhk
 */
public class Relationship extends GraphEntity {

    private final int startId, endId;

    public Relationship(JSONObject entity) {
        super("relationship", entity);
        Matcher m = selfUrlPattern.matcher((String) entity.get("start"));
        if (m.matches()) {
            if ("node".equalsIgnoreCase(m.group(1))) {
                this.startId = Integer.parseInt(m.group(2));
            } else {
                throw new IllegalArgumentException("start of relationship is not a node");
            }
        } else {
            throw new IllegalArgumentException("start url must match: " + selfUrlPattern.pattern());
        }
        m = selfUrlPattern.matcher((String) entity.get("end"));
        if (m.matches()) {
            if ("node".equalsIgnoreCase(m.group(1))) {
                this.endId = Integer.parseInt(m.group(2));
            } else {
                throw new IllegalArgumentException("end of relationship is not a node");
            }
        } else {
            throw new IllegalArgumentException("end url must match: " + selfUrlPattern.pattern());
        }
    }

    public String getType() {
        return (String) entity.get("type");
    }

    public int getStartId() {
        return startId;
    }

    public int getEndId() {
        return endId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        sb.append(getStartId());
        sb.append(")-[");
        sb.append(getId());
        sb.append(":");
        sb.append(getType());
        sb.append(" ");
        sb.append(entity.get("data"));
        sb.append("]->(");
        sb.append(getEndId());
        sb.append(")");
        return sb.toString();
    }
}
