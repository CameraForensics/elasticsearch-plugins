# Introduction
This repository contains native scoring scripts for use with elasticsearch. 

**Disclaimer:** They have only been tested with elasticsearch v1.7.3, and even then against a very strict document set.

## Hamming Distance
This script will calculate the hamming distance between two hex-encoded bit-strings (ie: strings made up of 1’s and 0’s and then hexidecimally encoded), one hash being passed in as a parameter: `param_hash`, and the other being stored in a field also identified by a parameter: `param_field`.

It will then return your search results scored accordingly, where the smallest distance (ie: most similar strings) appear nearer the top of the results list.

### Caveats
The hashes must be the same length in every document, or the relative scoring won’t be accurate.

## Euclidean Distance
This script compares the euclidean distance of two hexadecimal-encoded hashes by first decoding them into strings of integers, and then calculating the distance between them by comparing each integer in the string.

The result is subtracted from Double.MAX_VALUE to create a descending scoring value. So documents with less of a euclidean distance from the parameter hash shoudl appear nearer the top of the results list.

The first hash is sent in as a parameter: `param_hash`, the other is expected to be stored in a field also identified by a parameter: `param_field`.

**See Usage Examples below for clarity.**

# Setup
**Note:** If you only want to use one of the plugins, modify the following to exclude instructions for the other.

## Step 1: Configuration

Include the following in your elasticsearch.yml config file:

    script.native:
        hamming_distance.type: com.example.elasticsearch.plugins.HammingDistanceScriptFactory
        euclidean_distance.type: com.example.elasticsearch.plugins.EuclideanDistanceScriptFactory

**Note:** If you don’t do this, they still show up on the plugins list (see later) but you’ll get errors when you try to use either of them saying that elasticsearch can’t find the plugin.

## Step 2: Build
To build the plugins simply run: `gradle build`.

The results will be located in: `PLUGIN_NAME/build/libs/`

For example:

* `hammingdistance/build/libs/hammingdistance-0.1.0.jar`
* `euclideandistance/build/libs/euclideandistance-0.1.0.jar`

**Note:** This has been built using Gradle v2.7.

## Step 3: Deployment
Don't bother using the elasticsearch plugin script to install it
It's just a pain the ass and all it seems to do is unpack your stuff - a bit pointless.

Instead put the .jar file in `%ELASTICSEARCH_HOME%/plugins/hamming_distance` for hamming distance, and `%ELASTICSEARCH_HOME%/plugins/euclidean_distance` for euclidean distance, then restart elasticsearch.

If all has gone well, you'll see them being loaded on elasticsearch startup:

    [1982-07-06 12:02:43,765][INFO ][plugins                  ] [Junta] loaded [marvel, hamming_distance, euclidean_distance], sites [marvel]

AND when you call the list of plugins they’ll be there:

    curl http://localhost:9200/_cat/plugins?v

produces something like:

    name        component                version type url
    Junta       hamming_distance         0.1.0   j
    Junta       euclidean_distance       0.1.0   j


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