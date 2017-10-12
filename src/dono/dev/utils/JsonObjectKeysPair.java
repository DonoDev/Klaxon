package dono.dev.utils;

import java.util.Collection;

import org.json.JSONObject;

public class JsonObjectKeysPair {
    private JSONObject object;
    private Collection<String> keys;

    public JsonObjectKeysPair(JSONObject object, Collection<String> keys){
        this.object = object;
        this.keys   = keys;
    }

    public JSONObject getObject() {
        return object;
    }
    public Collection<String> getKeys() {
        return keys;
    }
}
