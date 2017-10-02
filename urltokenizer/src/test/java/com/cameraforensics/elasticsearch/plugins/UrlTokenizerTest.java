package com.cameraforensics.elasticsearch.plugins;

import org.junit.Test;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UrlTokenizerTest {

    private UrlTokenizer urlTokenizer = new UrlTokenizer();

    @Test
    public void correctly_split_double_segment() {
        // given
        String url = "129136207330/good-times-only";

        // when
        List<String> splits = urlTokenizer.splitUrl(url);

        // then
        assertTrue(splits.contains("129136207330"));
        assertTrue(splits.contains("good-times-only"));
        assertTrue(splits.contains(url));
        assertEquals(3, splits.size());
    }

    @Test
    public void correctly_splits_single_segment() {
        // given
        String url = "good-times-only";

        // when
        List<String> splits = urlTokenizer.splitUrl(url);

        // then
        assertTrue(splits.contains("good-times-only"));
        assertEquals(1, splits.size());
    }

    @Test
    public void correctly_splits_url_without_domain_or_protocol() {
        // given
        String url = "129136207330/good-times-only?one=two&three=four";

        // when
        List<String> splits = urlTokenizer.splitUrl(url);

        // then
        assertTrue(splits.contains("129136207330"));
        assertTrue(splits.contains("129136207330/good-times-only"));
        assertTrue(splits.contains("good-times-only"));
        assertTrue(splits.contains("one=two"));
        assertTrue(splits.contains("three=four"));
        assertEquals(5, splits.size());
    }

    @Test
    public void correctly_splits_long_url_with_params() throws MalformedURLException {
        // given
        String url = "https://farm1.domain.com/one/two/three/four.jpg?param1=value1&param2=value2";

        // when
        List<String> splits = urlTokenizer.splitUrl(url);

        // then
        assertTrue(splits.contains("farm1.domain.com"));
        assertTrue(splits.contains("one"));
        assertTrue(splits.contains("two"));
        assertTrue(splits.contains("three"));
        assertTrue(splits.contains("four.jpg"));
        assertTrue(splits.contains("param1=value1&param2=value2"));
        assertTrue(splits.contains("param1=value1"));
        assertTrue(splits.contains("param2=value2"));
        assertTrue(splits.contains("https://farm1.domain.com/one"));
        assertTrue(splits.contains("https://farm1.domain.com/one/two"));
        assertTrue(splits.contains("https://farm1.domain.com/one/two/three"));
        assertTrue(splits.contains("https://farm1.domain.com/one/two/three/four.jpg"));
        assertTrue(splits.contains("farm1.domain.com/one"));
        assertTrue(splits.contains("farm1.domain.com/one/two"));
        assertTrue(splits.contains("farm1.domain.com/one/two/three"));
        assertTrue(splits.contains("farm1.domain.com/one/two/three/four.jpg"));
        assertTrue(splits.contains("one/two/three/four.jpg"));
        assertTrue(splits.contains("two/three/four.jpg"));
        assertTrue(splits.contains("three/four.jpg"));
        assertTrue(splits.contains("one/two/three"));
        assertTrue(splits.contains("one/two"));
        assertTrue(splits.contains("two/three"));
        assertEquals(22, splits.size());
    }

    @Test
    public void correctly_splits_slightly_shorter_url_with_params() throws MalformedURLException {
        // given
        String url = "https://farm1.domain.com/one/two/four.jpg?param1=value1&param2=value2";

        // when
        List<String> splits = urlTokenizer.splitUrl(url);

        // then
        assertTrue(splits.contains("farm1.domain.com"));
        assertTrue(splits.contains("one"));
        assertTrue(splits.contains("two"));
        assertTrue(splits.contains("four.jpg"));
        assertTrue(splits.contains("param1=value1&param2=value2"));
        assertTrue(splits.contains("param1=value1"));
        assertTrue(splits.contains("param2=value2"));
        assertTrue(splits.contains("https://farm1.domain.com/one"));
        assertTrue(splits.contains("https://farm1.domain.com/one/two"));
        assertTrue(splits.contains("https://farm1.domain.com/one/two/four.jpg"));
        assertTrue(splits.contains("farm1.domain.com/one"));
        assertTrue(splits.contains("farm1.domain.com/one/two"));
        assertTrue(splits.contains("farm1.domain.com/one/two/four.jpg"));
        assertTrue(splits.contains("one/two/four.jpg"));
        assertTrue(splits.contains("two/four.jpg"));
        assertTrue(splits.contains("one/two"));
        assertEquals(16, splits.size());
    }

    @Test
    public void correctly_splits_url_with_no_path() throws MalformedURLException {
        // given
        String url = "https://farm1.domain.com/four.jpg?param1=value1&param2=value2";

        // when
        List<String> splits = urlTokenizer.splitUrl(url);
        Collections.sort(splits);
        for (String part : splits) {
            System.out.println(part);
        }

        // then
        assertTrue(splits.contains("farm1.domain.com"));
        assertTrue(splits.contains("https://farm1.domain.com/four.jpg"));
        assertTrue(splits.contains("farm1.domain.com/four.jpg"));
        assertTrue(splits.contains("four.jpg"));
        assertTrue(splits.contains("param1=value1&param2=value2"));
        assertTrue(splits.contains("param1=value1"));
        assertTrue(splits.contains("param2=value2"));
        assertEquals(7, splits.size());
    }

}
