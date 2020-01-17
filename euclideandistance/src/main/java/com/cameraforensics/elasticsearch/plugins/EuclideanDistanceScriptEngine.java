package com.cameraforensics.elasticsearch.plugins;

import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.script.ScoreScript;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;

public class EuclideanDistanceScriptEngine implements ScriptEngine {
    @Override
    public String getType() {
        return "expert_scripts";
    }

    @Override
    public <T> T compile(String scriptName, String scriptSource, ScriptContext<T> context, Map<String, String> params) {
        if (context.equals(ScoreScript.CONTEXT) == false) {
            throw new IllegalArgumentException(getType() + " scripts cannot be used for context [" + context.name + "]");
        }
        // we use the script "source" as the script identifier
        if ("euclidean_distance".equals(scriptSource)) {
            ScoreScript.Factory factory = (p, lookup) -> new ScoreScript.LeafFactory() {
                final String field;
                final String hash;
                final byte[] paramHash;
                private int maxScore = 9363600;

                {
                    if (p.containsKey("field") == false) {
                        throw new IllegalArgumentException("Missing parameter [field]");
                    }
                    if (p.containsKey("hash") == false) {
                        throw new IllegalArgumentException("Missing parameter [hash]");
                    }
                    field = p.get("field").toString();
                    hash = p.get("hash").toString();
                    if (hash != null) {
                        paramHash = convertFromString(hash);
                    }
                    else {
                        paramHash = null;
                    }

                }

                private byte[] convertFromString(String hashStr) {
                    return Base64.decodeBase64(hashStr);
                }

                @Override
                public ScoreScript newInstance(LeafReaderContext context) {
                    return new ScoreScript(p, lookup, context) {

                        @Override
                        public double execute(ExplanationHolder explanation) {
                            String fieldValue = ((ScriptDocValues.Strings) getDoc().get(field)).getValue();
                            if (hash == null || fieldValue == null) {
                                return (float) maxScore;
                            }

                            byte[] fieldValueHash = convertFromString(fieldValue);

                            if (fieldValueHash.length != paramHash.length) {
                                return (float) maxScore;
                            }
                            double distance = 0.0;
                            for (int i = 0; i < fieldValueHash.length; i++) {
                                final int diff = fieldValueHash[i] - paramHash[i];
                                distance += diff * diff;
                            }
                            return (float) Math.floor(maxScore - distance);

                        }
                    };
                }

                @Override
                public boolean needs_score() {
                    return false;
                }
            };
            return context.factoryClazz.cast(factory);
        }
        throw new IllegalArgumentException("Unknown script name " + scriptSource);
    }

    @Override
    public void close() {
        // optionally close resources
    }
}