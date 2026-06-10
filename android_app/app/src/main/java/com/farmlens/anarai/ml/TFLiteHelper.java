package com.farmlens.anarai.ml;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.InterpreterApi;
import java.util.Map;
import java.util.HashMap;

public class TFLiteHelper {
    public static void runForMultipleInputs(Interpreter interpreter, Object[] inputs, Object diseaseOutput, Object severityOutput) {
        Map<Integer, Object> outputs = new HashMap<>();
        outputs.put(0, severityOutput);
        outputs.put(1, diseaseOutput);
        ((InterpreterApi) interpreter).runForMultipleInputsOutputs(inputs, outputs);
    }
}
