package com.cameraforensics.elasticsearch.plugins;

import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.script.AbstractFloatSearchScript;

import java.util.Map;

public class AutoColourCorrelogramDistanceScript extends AbstractFloatSearchScript {

    private static final int SIZE_X = 64;
    private static final int SIZE_Y = 4;

    private float[][] queryCorrelogram = new float[SIZE_X][SIZE_Y];

    public AutoColourCorrelogramDistanceScript(Map<String, Object> params) {
        super();
        queryCorrelogram = parseCorrelogram((String) params.get("param_acc"));
    }

    /**
     * EG: This:
     * ```
     * 11.0,9.0,7.0,7.0;13.0,12.0,12.0,11.0;8.0,5.0,4.0,3.0;10.0,7.0,6.0,6.0;5.0,3.0,2.0,1.0;3.0,0.0,0.0,0.0;2.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;10.0,8.0,6.0,5.0;8.0,4.0,3.0,2.0;12.0,10.0,9.0,8.0;10.0,7.0,6.0,5.0;8.0,5.0,4.0,3.0;10.0,6.0,3.0,1.0;1.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;6.0,3.0,1.0,1.0;3.0,0.0,0.0,0.0;3.0,1.0,0.0,0.0;0.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;7.0,3.0,2.0,2.0;4.0,1.0,0.0,0.0;3.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;1.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;1.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;7.0,3.0,1.0,1.0;15.0,14.0,13.0,13.0;10.0,7.0,6.0,4.0;15.0,15.0,14.0,14.0;13.0,11.0,9.0,8.0;15.0,15.0,15.0,15.0;1.0,0.0,0.0,0.0;10.0,7.0,5.0,3.0;11.0,8.0,6.0,5.0;12.0,10.0,9.0,9.0;13.0,11.0,10.0,9.0;4.0,1.0,0.0,0.0;5.0,2.0,1.0,0.0;0.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;8.0,5.0,3.0,2.0;9.0,7.0,5.0,5.0;5.0,2.0,1.0,0.0;0.0,0.0,0.0,0.0;1.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;11.0,8.0,7.0,6.0;11.0,8.0,7.0,7.0;6.0,2.0,1.0,0.0;2.0,0.0,0.0,0.0;2.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;0.0,0.0,0.0,0.0;
     * ```
     * will produce:
     * [[11.0, 9.0, 7.0, 7.0], [13.0, 12.0, 12.0, 11.0] ... ]
     * @param correlogramStr
     * @return
     */
    private float[][] parseCorrelogram(String correlogramStr) {
        if (correlogramStr == null) {
            return null;
        }

        float[][] correlogram = new float[SIZE_X][SIZE_Y];
        String[] rows = correlogramStr.split(";");
        if (rows.length != SIZE_X) {
            return null;
        }

        for (int i = 0; i < rows.length; i++) {
            String[] cols = rows[i].split(",");
            if (cols.length != SIZE_Y) {
                return null;
            }

            for (int j = 0; j < cols.length; j++) {
                try {
                    correlogram[i][j] = Integer.parseInt(cols[j]);
                } catch (NumberFormatException nfe) {
                    return null;
                }
            }
        }
        return correlogram;
    }

    private float getDistance(float[][] correlogram, float[][] queryCorrelogram) {
        float result = 0;
        for (int i = 0; i < correlogram.length; i++) {
            float[] ints = correlogram[i];
            for (int j = 0; j < ints.length; j++) {
                result += (correlogram[i][j] > 0 ? (correlogram[i][j] / 2f) * Math.log((2f * correlogram[i][j]) / (correlogram[i][j] + queryCorrelogram[i][j])) : 0) +
                        (queryCorrelogram[i][j] > 0 ? (queryCorrelogram[i][j] / 2f) * Math.log((2f * queryCorrelogram[i][j]) / (correlogram[i][j] + queryCorrelogram[i][j])) : 0);
            }
        }

        return (float) Integer.MAX_VALUE - result;
    }

    @Override
    public float runAsFloat() {
        if (queryCorrelogram == null) {
            return Integer.MAX_VALUE;
        }

        float[][] correlogram = parseCorrelogram(((ScriptDocValues.Strings) doc().get("correlogram")).getValue());
        if (correlogram == null) {
            return Integer.MAX_VALUE;
        }

        return getDistance(correlogram, queryCorrelogram);
    }
}
