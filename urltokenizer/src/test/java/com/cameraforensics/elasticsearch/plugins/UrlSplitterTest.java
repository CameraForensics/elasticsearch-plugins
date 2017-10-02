package com.cameraforensics.elasticsearch.plugins;

import org.junit.Test;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class UrlSplitterTest {

    private UrlTokenizer urlTokenizer = new UrlTokenizer();

    @Test
    public void correctly_split_double_segment() {
        String url = "129136207330/good-times-only";
        List<String> splts = urlTokenizer.splitUrl(url);
        Collections.sort(splts);
        for (String part : splts) {
            System.out.println(part);
        }

    }

    @Test
    public void correctly_splits_single_segment() {
        String url = "good-times-only";
        List<String> splts = urlTokenizer.splitUrl(url);
        Collections.sort(splts);
        for (String part : splts) {
            System.out.println(part);
        }

    }

    @Test
    public void correctly_splits_url_without_domain_or_protocol() {
        String url = "129136207330/good-times-only?one=two&three=four";
        List<String> splts = urlTokenizer.splitUrl(url);
        Collections.sort(splts);
        for (String part : splts) {
            System.out.println(part);
        }

    }

    @Test
    public void correctly_splits_long_url_with_params() throws MalformedURLException {
        String url = "https://farm1.domain.com/one/two/three/four.jpg?param1=value1&param2=value2";
        List<String> splts = urlTokenizer.splitUrl(url);
        Collections.sort(splts);
        for (String part : splts) {
            System.out.println(part);
        }

        assertTrue(splts.contains("farm1.domain.com"));
        assertTrue(splts.contains("one"));
        assertTrue(splts.contains("two"));
        assertTrue(splts.contains("three"));
        assertTrue(splts.contains("four.jpg"));
        assertTrue(splts.contains("one/two/three/four.jpg"));
        assertTrue(splts.contains("param1=value1&param2=value2"));
        assertTrue(splts.contains("param1=value1"));
        assertTrue(splts.contains("param2=value2"));
        assertTrue(splts.contains("https://farm1.domain.com/one"));
        assertTrue(splts.contains("https://farm1.domain.com/one/two"));
        assertTrue(splts.contains("https://farm1.domain.com/one/two/three"));
        assertTrue(splts.contains("https://farm1.domain.com/one/two/three/four.jpg"));
        assertTrue(splts.contains("one/two/three/four.jpg"));
        assertTrue(splts.contains("two/three/four.jpg"));
        assertTrue(splts.contains("three/four.jpg"));
        assertTrue(splts.contains("one/two/three"));
        assertTrue(splts.contains("one/two"));
        assertTrue(splts.contains("two/three"));
    }

    @Test
    public void correctly_splits_slightly_shorter_url_with_params() throws MalformedURLException {
        String url = "https://farm1.domain.com/one/two/four.jpg?param1=value1&param2=value2";
        List<String> splts = urlTokenizer.splitUrl(url);
        Collections.sort(splts);
        for (String part : splts) {
            System.out.println(part);
        }

        assertTrue(splts.contains("farm1.domain.com"));
        assertTrue(splts.contains("one"));
        assertTrue(splts.contains("two"));
        assertTrue(splts.contains("four.jpg"));
        assertTrue(splts.contains("one/two/four.jpg"));
        assertTrue(splts.contains("param1=value1&param2=value2"));
        assertTrue(splts.contains("param1=value1"));
        assertTrue(splts.contains("param2=value2"));
        assertTrue(splts.contains("https://farm1.domain.com/one"));
        assertTrue(splts.contains("https://farm1.domain.com/one/two"));
        assertTrue(splts.contains("https://farm1.domain.com/one/two/four.jpg"));
        assertTrue(splts.contains("one/two/four.jpg"));
        assertTrue(splts.contains("two/four.jpg"));
        assertTrue(splts.contains("one/two"));
    }

    @Test
    public void correctly_splits_url_with_no_path() throws MalformedURLException {
        String url = "https://farm1.domain.com/four.jpg?param1=value1&param2=value2";
        List<String> splts = urlTokenizer.splitUrl(url);
        Collections.sort(splts);
        for (String part : splts) {
            System.out.println(part);
        }

        assertTrue(splts.contains("farm1.domain.com"));
        assertTrue(splts.contains("four.jpg"));
        assertTrue(splts.contains("param1=value1&param2=value2"));
        assertTrue(splts.contains("param1=value1"));
        assertTrue(splts.contains("param2=value2"));

    }
//
//        private List<String> splitUrl(String url) {
//            Set<String> parts = new HashSet<>();
//            URL realUrl;
//            try {
//                realUrl = new URL(url);
//                String host = realUrl.getHost();
//                parts.add(host);
//
//                String query = realUrl.getQuery();
//                if (query != null && !"".equals(query.trim())) {
//                    parts.addAll(Arrays.asList(realUrl.getQuery().split("&")));
//                    parts.add(realUrl.getQuery());
//                    parts.add(url.substring(0, url.length() - (query.length() + 1)));
//                }
//
//                String path = realUrl.getPath();
//                if (path != null && !"".equals(path.trim())) {
//                    parts.add(realUrl.getPath());
//                    String[] pathSegments = realUrl.getPath().substring(1).split("/");
//
//                    int idx = url.indexOf(realUrl.getHost() + "/") + (realUrl.getHost() + "/").length();
//                    String upToHost = url.substring(0, idx);
//
//                    for (int i = 0; i < pathSegments.length; i++) {
//                        parts.add(pathSegments[i]);
//                        for (int j = i+1; j <= pathSegments.length; j++) {
//                            String permutation = join(Arrays.copyOfRange(pathSegments, i, j), "/"); // + (j == pathSegments.length ? "" : "/");
//                            parts.add(/* "/" + */permutation);
//                            if (i == 0) {
//                                parts.add(upToHost + permutation);
//                                parts.add(host + "/" + permutation);
//                            }
//                        }
//                    }
//                }
//            } catch (MalformedURLException e) {
//                // Ok, we'll do it the old skool way:
//                // Let's assume that there's no hippity tippities
//                String path = null;
//                if (url.indexOf('?') != -1) {
//                    if (!url.startsWith("?")) {
//                        path = url.substring(0, url.indexOf('?'));
//                    }
//                    if (!url.endsWith("?")) {
//                        String queryString = url.substring(url.indexOf('?') + 1);
//                        parts.addAll(Arrays.asList(queryString.split("&")));
//
//                    }
//
//                } else {
//                    path = url;
//                }
//
//                if (path != null) {
//                    String[] pathSegments = path.split("/");
//                    for (int i = 0; i < pathSegments.length; i++) {
//                        parts.add(pathSegments[i]);
//                        for (int j = i+1; j <= pathSegments.length; j++) {
//                            String permutation = join(Arrays.copyOfRange(pathSegments, i, j), "/"); // + (j == pathSegments.length ? "" : "/");
//                            parts.add(/* "/" + */permutation);
//                        }
//
//                    }
//                }
//            }
//
//            return new ArrayList<>(parts);
//        }
//
//        public String join(String[] values, String delimiter) {
//            StringBuffer strbuf = new StringBuffer();
//
//            boolean first = true;
//
//            for (String value : values) {
//                if (!first) { strbuf.append(delimiter); } else { first = false; }
//                strbuf.append(value);
//            }
//
//            return strbuf.toString();
//        }
}
