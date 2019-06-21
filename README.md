#ElasticSearch Native Script Plugins

[![Build Status](https://travis-ci.org/CameraForensics/elasticsearch-plugins.svg?branch=master)](https://travis-ci.org/CameraForensics/elasticsearch-plugins)

# Introduction
This repository contains native scoring scripts for use with elasticsearch. 

**Disclaimer:** They have only been tested with elasticsearch versions 2.3.1 and 6.2.4, and even then against a very strict document set.

## Hamming Distance
This script will calculate the hamming distance between two hex-encoded bit-strings (ie: strings made up of 1’s and 0’s and then hexidecimally encoded), one hash being passed in as a parameter: `param_hash`, and the other being stored in a field also identified by a parameter: `param_field`.

It will then return your search results scored accordingly, where the smallest distance (ie: most similar strings) appear nearer the top of the results list.

**Note:** If the parameter hash and the document hash are not the same length, the result will be a score of `0.0f`.

### Caveats
The hashes must be the same length in every document, or the relative scoring won’t be accurate.

## Euclidean Distance
This script compares the euclidean distance of two hexadecimal-encoded hashes by first decoding them into strings of integers, and then calculating the distance between them by comparing each integer in the string.

The result is subtracted from Double.MAX_VALUE to create a descending scoring value. So documents with less of a euclidean distance from the parameter hash shoudl appear nearer the top of the results list.

The first hash is sent in as a parameter: `param_hash`, the other is expected to be stored in a field also identified by a parameter: `param_field`.

## AutoColourCorrelogram
This script will calculate a distance between a given 64x4 correlogram (4 distance sets) in the format:
```
X0Y0,X0Y1,X0Y2,X0Y4;X1Y0,X1Y1,X1Y2,X1Y3;...;X64Y0,X64Y1,X64Y2,X64Y3
```
Because in distance terms, the smaller the distance the more likely the match, an in elastic terms, the greater the score the more likely
the match, we need to turn the score on its head. So the result returned is actually `Integer.MAX_VALUE - distance`.

If the parameter is incorrect, or the document does not have all the expected fields for the correlogram, the result will be `Integer.MAX_VALUE`.

The script expects that the correlogram is stored in a field called `correlogram` in the same format as the parameter is provided (see above).

## URL Tokenizer
This script splits a URL into a set of perumtations of URL segmentation. For example, given the URL: `https://farm1.domain.com/one/two/three/four.jpg?param1=value1&param2=value2`,
 the following tokens are created:
 
* `farm1.domain.com`
* `one`
* `two`
* `three`
* `four.jpg`
* `param1=value1&param2=value2`
* `param1=value1`
* `param2=value2`
* `https://farm1.domain.com/one`
* `https://farm1.domain.com/one/two`
* `https://farm1.domain.com/one/two/three`
* `https://farm1.domain.com/one/two/three/four.jpg`
* `farm1.domain.com/one`
* `farm1.domain.com/one/two`
* `farm1.domain.com/one/two/three`
* `farm1.domain.com/one/two/three/four.jpg`
* `one/two/three/four.jpg`
* `two/three/four.jpg`
* `three/four.jpg`
* `one/two/three`
* `one/two`
* `two/three`
 
The main purpose of this plugin is to cater for URL path/sub-path querying without the need of wildcards. Performance testing showed that it generated
more accurate results than the standard tokenizer, although it does take up more database.

**See Usage Examples below for clarity.**

# Setup
**Note:** If you only want to use one of the plugins, modify the following to exclude instructions for the other.

## Step 1: Configuration

Include the following in your elasticsearch.yml config file:

    script.native:
        hamming_distance.type: com.cameraforensics.elasticsearch.plugins.AutoColourCorrelogramDistanceScriptFactory
        euclidean_distance.type: com.cameraforensics.elasticsearch.plugins.EuclideanDistanceScriptFactory

**Note:** If you don’t do this, they still show up on the plugins list (see later) but you’ll get errors when you try to use either of them saying that elasticsearch can’t find the plugin.

## Step 2: Build
To build the plugins simply run: `gradle build`.

The results will be located in: `PLUGIN_NAME/build/libs/`

For example:

* `hammingdistance/build/libs/hammingdistance-2.3.1.0.jar`
* `euclideandistance/build/libs/euclideandistance-2.3.1.0.jar`

**Note:** This has been built using Gradle v2.7.

## Step 3: Deployment
Don't bother using the elasticsearch plugin script to install it - it's just a pain the ass and all it seems to do is unpack your stuff - a bit pointless.

Instead put the .jar file in `%ELASTICSEARCH_HOME%/plugins/hamming_distance` for hamming distance, and `%ELASTICSEARCH_HOME%/plugins/euclidean_distance` for euclidean distance, then restart elasticsearch.

**Note:** If you installed elasticsearch according to docs, this `%ELASTICSEARCH_HOME%` will default to `/usr/share/elasticsearch/`

If all has gone well, you'll see them being loaded on elasticsearch startup:

    [1982-07-06 12:02:43,765][INFO ][plugins                  ] [Junta] loaded [marvel, hamming_distance, euclidean_distance, urltokenizer], sites [marvel]

AND when you call the list of plugins they’ll be there:

    curl http://localhost:9200/_cat/plugins?v

produces something like:

    name        component                version   type url
    Junta       hamming_distance         2.3.1.0   j
    Junta       euclidean_distance       2.3.1.0   j
    Junta       urltokenizer             2.3.1.0   j


# Usage Examples

### Hamming Distance

    curl -XPOST 'http://localhost:9200/twitter/_search?pretty' -d '{
      "query": {
        "function_score": {     
          "min_score": IDEAL MIN SCORE HERE,
          "query":{
           "match_all":{}
          },
          "functions": [
            {
              "script_score": {
                "script": "hamming_distance",
                "lang" : "native",
                "params": {
                  "param_hash": "HASH TO COMPARE WITH",
                  "param_field":"FIELD WHERE HASH IS STORED"
                }
              }
            }
          ]
        }
      }
    }'
    
    
### Euclidean Distance

    curl -XPOST 'http://localhost:9200/twitter/_search?pretty' -d '{
      "query": {
        "function_score": {     
          "min_score": IDEAL MIN SCORE HERE,
          "query":{
           "match_all":{}
          },
          "functions": [
            {
              "script_score": {
                "script": "euclidean_distance",
                "lang" : "native",
                "params": {
                  "param_hash": "HASH TO COMPARE WITH",
                  "param_field":"FIELD WHERE HASH IS STORED"
                }
              }
            }
          ]
        }
      }
    }'
    
### Auto Colour Correlogram

    curl -XPOST 'http://localhost:9200/twitter/_search?pretty' -d '{
      "query": {
        "function_score": {     
          "min_score": IDEAL MIN SCORE HERE,
          "query":{
           "match_all":{}
          },
          "functions": [
            {
              "script_score": {
                "script": "autocolourcorrelogram",
                "lang" : "native",
                "params": {
                  "param_acc": "CORRELOGRAM TO COMPARE WITH"
                }
              }
            }
          ]
        }
      }
    }'
    
### URL Tokenizer
In your index mappings/settings file:
```
    ...
    "settings": {
        "index.requests.cache.enable": true,
        "analysis":{
            "analyzer":{
                "urlanalyzer":{ 
                    "type": "custom",
                    "tokenizer": "urltokenizer"
                }
            }
        }
    },
    ...
    "url": {
        "type": "string",
        "analyzer": "urlanalyzer"
    },
    ...
```
