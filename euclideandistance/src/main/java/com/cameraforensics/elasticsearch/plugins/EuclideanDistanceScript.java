package com.cameraforensics.elasticsearch.plugins;

import org.apache.commons.codec.binary.Base64;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.script.AbstractFloatSearchScript;

import java.util.Map;

public class EuclideanDistanceScript extends AbstractFloatSearchScript {

    private String field;
    private String hash;
    private byte[] paramHash = null;
    private int maxScore = 9363600;

    public EuclideanDistanceScript(Map<String, Object> params) {
        super();
        field = (String) params.get("param_field");
        hash = (String) params.get("param_hash");
        paramHash = convertFromString(hash);
    }

    private byte[] convertFromString(String hashStr) {
        return Base64.decodeBase64(hashStr);
    }

    private float euclideanDistance(byte[] hash1, byte[] hash2) {
        if (hash1.length != hash2.length) {
            return (float) maxScore;
        }
        double distance = 0.0;
        for (int i = 0; i < hash1.length; i++) {
            final int diff = hash1[i] - hash2[i];
            distance += diff * diff;
        }
        return (float) Math.floor(maxScore - distance);
    }

    @Override
    public float runAsFloat() {
        String fieldValue = ((ScriptDocValues.Strings) doc().get(field)).getValue();
        if (hash == null || fieldValue == null) {
            return (float) maxScore;
        }

        return euclideanDistance(paramHash, convertFromString(fieldValue));
    }
}
