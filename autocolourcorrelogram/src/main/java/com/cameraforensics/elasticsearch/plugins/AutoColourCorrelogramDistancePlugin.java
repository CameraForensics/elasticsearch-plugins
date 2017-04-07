package com.cameraforensics.elasticsearch.plugins;

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.script.ScriptModule;

public class AutoColourCorrelogramDistancePlugin extends Plugin {

    private static final String PLUGIN_NAME = "autocolourcorrelogram";

    @Override
    public String name() {
        return PLUGIN_NAME;
    }

    @Override
    public String description() {
        return "A scoring function to calculate the distance between two Auto Colour Correlograms.";
    }

    public void onModule(ScriptModule scriptModule) {
        scriptModule.registerScript(PLUGIN_NAME, AutoColourCorrelogramDistanceScriptFactory.class);
    }
}
