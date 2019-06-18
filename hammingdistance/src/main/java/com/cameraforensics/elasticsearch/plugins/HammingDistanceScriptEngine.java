package com.cameraforensics.elasticsearch.plugins;

import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;
import org.elasticsearch.script.ScoreScript;

import java.io.IOException;
import java.util.Map;

public class HammingDistanceScriptEngine implements ScriptEngine {
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
        if ("hamming_distance".equals(scriptSource)) {
            ScoreScript.Factory factory = (p, lookup) -> new ScoreScript.LeafFactory() {
                final String field;
                final String hash;
                final int length;

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
                        length = hash.length();
                    }
                    else {
                        length = 0;
                    }
                }

                @Override
                public ScoreScript newInstance(LeafReaderContext context) {
                    return new ScoreScript(p, lookup, context) {
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
                        public double execute() {
                            String fieldValue = ((ScriptDocValues.Strings) getDoc().get(field)).getValue();
                            if (hash == null || fieldValue == null || fieldValue.length() != hash.length()) {
                                return 0.0f;
                            }

                            return hammingDistance(fieldValue, hash);
                        }                    };
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