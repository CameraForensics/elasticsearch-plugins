package com.cameraforensics.elasticsearch.plugins;

import org.elasticsearch.plugins.AbstractPlugin;

public class EuclideanDistancePlugin extends AbstractPlugin {
    @Override
    public String name() {
        return "euclidean_distance";
    }

    @Override
    public String description() {
        return "A scoring function to calculate the euclidean distance between two hex encoded strings.";
    }
}