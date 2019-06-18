package com.cameraforensics.elasticsearch.plugins;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

public class UrlTokenizerFactory extends AbstractTokenizerFactory {

    @Inject
    public UrlTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings){
        super(indexSettings, settings);
    }

    @Override
    public Tokenizer create() {
        return new UrlTokenizer();
    }
}
