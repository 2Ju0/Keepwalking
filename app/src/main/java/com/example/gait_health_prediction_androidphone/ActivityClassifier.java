package com.example.gait_health_prediction_androidphone;

import android.content.Context;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.InputStream;

public class ActivityClassifier {

    private static final String MODEL_FILE = "file:///android_asset/juu.pb";
    private static final String INPUT_NODE = "lstm_1_input";
    private static final String[] OUTPUT_NODES = {"output/Softmax"};
    private static final String OUTPUT_NODE = "output/Softmax";
    private static final long[] INPUT_SIZE = {1, 128, 6, 1};
    private static final int OUTPUT_SIZE = 1;

    static {
        System.loadLibrary("tensorflow_inference");
    }

    private TensorFlowInferenceInterface inferenceInterface;

    public ActivityClassifier(Context context) {
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);
    }

    public float[] predictProbabilities(float[] data) {
        float[] result = new float[OUTPUT_SIZE];
        inferenceInterface.feed(INPUT_NODE, data, INPUT_SIZE);
        inferenceInterface.run(OUTPUT_NODES);
        inferenceInterface.fetch(OUTPUT_NODE, result);
        return result;
    }
}