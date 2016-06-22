package com.cameraforensics.elasticsearch.plugins;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;

import java.util.Map;

public class EuclideanDistanceScriptFactory implements NativeScriptFactory {
    @Override
    public ExecutableScript newScript(@Nullable Map<String, Object> params) {
        return new EuclideanDistanceScript(params);
    }
}
