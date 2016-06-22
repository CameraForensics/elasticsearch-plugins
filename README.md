# Setup
**Note:** If you only want to use one of the plugins, modify the following to exclude instructions for the other.

## Step 1: Configuration

Include the following in your elasticsearch.yml config file:

    script.native:
        hamming_distance.type: com.example.elasticsearch.plugins.HammingDistanceScriptFactory
        euclidean_distance.type: com.example.elasticsearch.plugins.EuclideanDistanceScriptFactory

**Note:** If you don’t do this, they still show up on the plugins list (see later) but you’ll get errors when you try to use either of them saying that elasticsearch can’t find the plugin.

## Step 2: Deployment
Don't bother using the elasticsearch plugin script to install it
It's just a pain the ass and all it seems to do is unpack your stuff - a bit pointless.

Instead put the .jar file in `%ELASTICSEARCH_HOME%/plugins/hamming_distance` for hamming distance, and `%ELASTICSEARCH_HOME%/plugins/euclidean_distance` for euclidean distance, then restart elasticsearch.

If all has gone well, you'll see them being loaded on elasticsearch startup:

    [1982-07-06 12:02:43,765][INFO ][plugins                  ] [Junta] loaded [marvel, hamming_distance, euclidean_distance], sites [marvel]

AND when you call the list of plugins they’ll be there:

    curl http://localhost:9200/_cat/plugins?v

produces something like:

    name       component                     version type url
    Junta       hamming_distance         0.1.0   j
    Junta       euclidean_distance         0.1.0   j


# Hamming Distance Usage Example

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
    
    
# Euclidean Distance Usage Example

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