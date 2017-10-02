package com.cameraforensics.elasticsearch.plugins;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttributeImpl;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class UrlTokenizer extends Tokenizer {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
//    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    protected List<String> tokens = new ArrayList<>();

    protected String stringToTokenize;

    protected int position = 0;

    public UrlTokenizer() {
        super(DEFAULT_TOKEN_ATTRIBUTE_FACTORY);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        clearAttributes();
        if (position >= tokens.size()) {
            position = 0;
            return false;
        } else {
            String token = tokens.get(position);
            termAtt.setEmpty().append(token);
            termAtt.setLength(token.length());
//            int startOffset = stringToTokenize.indexOf(token); //Index of is linear and so not performant. Record start/ends when splitting
//            offsetAtt.setOffset(startOffset, startOffset + token.length());
            position++;
            return true;
        }
    }

    protected List<String> splitUrl(String url) {
        Set<String> parts = new HashSet<>();
        URL realUrl;
        try {
            realUrl = new URL(url);
            String host = realUrl.getHost();
            parts.add(host);

            String query = realUrl.getQuery();
            if (query != null && !"".equals(query.trim())) {
                parts.addAll(Arrays.asList(realUrl.getQuery().split("&")));
                parts.add(realUrl.getQuery());
                parts.add(url.substring(0, url.length() - (query.length() + 1)));
            }

            String path = realUrl.getPath();
            if (path != null && !"".equals(path.trim())) {
                parts.add(realUrl.getPath().substring(1));
                String[] pathSegments = realUrl.getPath().substring(1).split("/");

                int idx = url.indexOf(realUrl.getHost() + "/") + (realUrl.getHost() + "/").length();
                String upToHost = url.substring(0, idx);

                for (int i = 0; i < pathSegments.length; i++) {
                    parts.add(pathSegments[i]);
                    for (int j = i+1; j <= pathSegments.length; j++) {
                        String permutation = join(Arrays.copyOfRange(pathSegments, i, j), "/"); // + (j == pathSegments.length ? "" : "/");
                        parts.add(/* "/" + */permutation);
                        if (i == 0) {
                            parts.add(upToHost + permutation);
                            parts.add(host + "/" + permutation);
                        }
                    }
                }
            }
        } catch (MalformedURLException e) {
            // Ok, we'll do it the old skool way:
            // Let's assume that there's no hippity tippities
            String path = null;
            if (url.indexOf('?') != -1) {
                if (!url.startsWith("?")) {
                    path = url.substring(0, url.indexOf('?'));
                }
                if (!url.endsWith("?")) {
                    String queryString = url.substring(url.indexOf('?') + 1);
                    parts.addAll(Arrays.asList(queryString.split("&")));

                }

            } else {
                path = url;
            }

            if (path != null) {
                String[] pathSegments = path.split("/");
                for (int i = 0; i < pathSegments.length; i++) {
                    parts.add(pathSegments[i]);
                    for (int j = i+1; j <= pathSegments.length; j++) {
                        String permutation = join(Arrays.copyOfRange(pathSegments, i, j), "/"); // + (j == pathSegments.length ? "" : "/");
                        parts.add(/* "/" + */permutation);
                    }

                }
            }
        }

        return new ArrayList<>(parts);
    }

    private String join(String[] values, String delimiter) {
        StringBuffer strbuf = new StringBuffer();

        boolean first = true;

        for (String value : values) {
            if (!first) { strbuf.append(delimiter); } else { first = false; }
            strbuf.append(value);
        }

        return strbuf.toString();
    }

    @Override
    public void end() throws IOException {
        super.end();
        tokens.clear();
        position = 0;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
//            tokens.clear();
//            position = 0;
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        fillBuffer(input);
//        position = 0;
    }

    final char[] buffer = new char[8192];
    private void fillBuffer(Reader input) throws IOException {
        int len;
        StringBuilder str = new StringBuilder();
        str.setLength(0);
        while ((len = input.read(buffer)) > 0) {
            str.append(buffer, 0, len);
        }
        stringToTokenize = str.toString();
        tokens = splitUrl(stringToTokenize);
    }

}
