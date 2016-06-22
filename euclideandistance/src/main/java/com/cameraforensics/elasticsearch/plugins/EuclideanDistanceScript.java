package com.cameraforensics.elasticsearch.plugins;

import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.script.AbstractFloatSearchScript;

import java.util.Map;

public class EuclideanDistanceScript extends AbstractFloatSearchScript {

    private String field;
    private String hash;
    private int[] intHash = null;

    public EuclideanDistanceScript(Map<String, Object> params) {
        super();
        field = (String) params.get("param_field");
        hash = (String) params.get("param_hash");
        intHash = convertFromString(hash);
    }

    private int[] convertFromString(String hashStr) {
        String[] parts = hashStr.split("(?<=\\G.{2})");
        int[] hash = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            hash[i] = Integer.valueOf(parts[i], 16);
        }

        return hash;
    }

    private float euclideanDistance(int[] hash1, int[] hash2) {
        double distance = 0.0;
        for (int i = 0; i < hash1.length; i++) {
            distance += Math.pow((double) (hash1[i] - hash2[i]), 2.0);
        }
        return (float) Math.sqrt(Math.floor(Double.MAX_VALUE - distance));
    }

    @Override
    public float runAsFloat() {
        String fieldValue = ((ScriptDocValues.Strings) doc().get(field)).getValue();
        if (hash == null || fieldValue == null || fieldValue.length() != hash.length()) {
            return (float) Double.MAX_VALUE;
        }

        return euclideanDistance(intHash, convertFromString(fieldValue));
    }
}
