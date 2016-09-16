package com.cameraforensics.elasticsearch.plugins;

import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.script.AbstractFloatSearchScript;

import java.util.Map;

public class HammingDistanceScript extends AbstractFloatSearchScript {

    private String field;
    private String hash;
    private int length;

    public HammingDistanceScript(Map<String, Object> params) {
        super();
        field = (String) params.get("param_field");
        hash = (String) params.get("param_hash");
        if (hash != null) {
            length = hash.length();
        }
    }

    private int hammingDistance(CharSequence lhs, CharSequence rhs) {
        int distance = length;
        for (int i = 0, l = lhs.length(); i < l; i++) {
            if (lhs.charAt(i) != rhs.charAt(i)) {
                distance--;
            }
        }

        return distance;
    }

    @Override
    public float runAsFloat() {
        String fieldValue = ((ScriptDocValues.Strings) doc().get(field)).getValue();
        if (hash == null || fieldValue == null || fieldValue.length() != hash.length()) {
            return 0.0f;
        }

        return hammingDistance(fieldValue, hash);
    }
}
