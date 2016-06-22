package com.cameraforensics.elasticsearch.plugins;

import org.elasticsearch.plugins.AbstractPlugin;

public class HammingDistancePlugin extends AbstractPlugin {
    @Override
    public String name() {
        return "hamming_distance";
    }

    @Override
    public String description() {
        return "A scoring function to calculate hamming distance between two hex encoded strings.";
    }
}
