package com.cameraforensics.elasticsearch.plugins;

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.script.ScriptModule;

public class EuclideanDistancePlugin extends Plugin {

    private static final String PLUGIN_NAME = "euclidean_distance";

    @Override
    public String name() {
        return PLUGIN_NAME;
    }

    @Override
    public String description() {
        return "A scoring function to calculate the euclidean distance between two hex encoded strings.";
    }

    public void onModule(ScriptModule scriptModule) {
        scriptModule.registerScript(PLUGIN_NAME, EuclideanDistanceScriptFactory.class);
    }

}