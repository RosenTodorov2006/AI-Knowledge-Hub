package org.example.utils;

import java.util.List;
public final class VectorUtils {
    private VectorUtils() {}

    public static float[] toFloatArray(List<Double> doubles) {
        if (doubles == null) return new float[0];
        float[] floats = new float[doubles.size()];
        for (int i = 0; i < doubles.size(); i++) {
            floats[i] = doubles.get(i).floatValue();
        }
        return floats;
    }
}
