package com.cameraforensics.elasticsearch.plugins;

import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.common.inject.internal.Nullable;
import org.elasticsearch.script.*;
import org.elasticsearch.search.lookup.SearchLookup;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

public class HammingDistanceScriptEngine implements ScriptEngineService {

    @Override
    public String getType() {
        return "expert_scripts";
    }

    @Override
    public Function<Map<String, Object>, SearchScript> compile(String scriptName, String scriptSource, Map<String, String> params) {
        // we use the script "source" as the script identifier
        if ("hamming_distance".equals(scriptSource)) {
            return p -> new SearchScript() {
                final String field;
                final String hash;
                private int length;

                {
                    if (!p.containsKey("field")) {
                        throw new IllegalArgumentException("Missing parameter [field]");
                    }
                    if (!p.containsKey("hash")) {
                        throw new IllegalArgumentException("Missing parameter [hash]");
                    }
                    field = p.get("field").toString();
                    hash = p.get("hash").toString();
                    if (hash != null) {
                        length = hash.length();
                    } else {
                        length = 0;
                    }

                }

                @Override
                public LeafSearchScript getLeafSearchScript(LeafReaderContext context) throws IOException {
                    return new LeafSearchScript() {
                        int currentDocId = -1;

                        @Override
                        public void setDocument(int docid) {
                            // advance has undefined behavior calling with a docid <= its current docid
                            currentDocId = docid;
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
                        public double runAsDouble() {
                            String fieldValue = null;
                            try {
                                fieldValue = context.reader().document(currentDocId).get(field);
                            } catch (IOException e) {

                            }

                            if (hash == null || fieldValue == null || fieldValue.length() != hash.length()) {
                                return 0.0f;
                            }

                            return hammingDistance(fieldValue, hash);
                        }
                    };

                }

                @Override
                public boolean needsScores() {
                    return false;
                }
            };
        }
        throw new IllegalArgumentException("Unknown script name " + scriptSource);
    }

    @Override
    @SuppressWarnings("unchecked")
    public SearchScript search(CompiledScript compiledScript, SearchLookup lookup, @Nullable Map<String, Object> params) {
        Function<Map<String, Object>, SearchScript> scriptFactory = (Function<Map<String, Object>, SearchScript>) compiledScript.compiled();
        return scriptFactory.apply(params);
    }

    @Override
    public ExecutableScript executable(CompiledScript compiledScript, @Nullable Map<String, Object> params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInlineScriptEnabled() {
        return true;
    }

    @Override
    public void close() {
    }
}
