package de.herschke.neo4j.uplink.api;

import org.json.simple.JSONObject;

/**
 * a simple node object.
 *
 * @author rhk
 */
public class Node extends GraphEntity {

    public Node(JSONObject entity) {
        super("node", entity);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        sb.append(getId());
        sb.append(" ");
        sb.append(entity.get("data"));
        sb.append(")");
        return sb.toString();
    }
}
