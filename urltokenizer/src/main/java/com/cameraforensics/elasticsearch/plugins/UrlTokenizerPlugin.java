package com.cameraforensics.elasticsearch.plugins;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.Plugin;

public class UrlTokenizerPlugin extends Plugin {


    public void onModule(AnalysisModule module) {
        module.addTokenizer("urltokenizer", UrlTokenizerFactory.class);
    }

    @Override
    public String name() {
        return "urltokenizer";
    }

    @Override
    public String description() {
        return "A custom tokenizer to produce tokens for all permutations of URL path segmentation.";
    }
}
