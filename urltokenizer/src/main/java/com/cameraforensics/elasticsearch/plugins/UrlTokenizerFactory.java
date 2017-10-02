package com.cameraforensics.elasticsearch.plugins;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import org.elasticsearch.index.settings.IndexSettingsService;

public class UrlTokenizerFactory extends AbstractTokenizerFactory {

    @Inject
    public UrlTokenizerFactory(Index index, IndexSettingsService indexSettings, Environment env, @Assisted String name, @Assisted Settings settings){
        super(index, indexSettings.getSettings(), name, settings);
    }

    @Override
    public Tokenizer create() {
        return new UrlTokenizer();
    }
}
