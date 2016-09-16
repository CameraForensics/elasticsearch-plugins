package com.cameraforensics.elasticsearch.plugins;

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.script.ScriptModule;

public class HammingDistancePlugin extends Plugin {

    private final String PLUGIN_NAME = "hamming_distance";

    @Override
    public String name() {
        return PLUGIN_NAME;
    }

    @Override
    public String description() {
        return "A scoring function to calculate hamming distance between two hex encoded strings.";
    }

    public void onModule(ScriptModule scriptModule) {
        scriptModule.registerScript(PLUGIN_NAME, HammingDistanceScriptFactory.class);
    }
}
