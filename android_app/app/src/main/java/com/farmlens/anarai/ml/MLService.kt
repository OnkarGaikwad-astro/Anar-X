package com.farmlens.anarai.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MLService(context: Context) {
    private var interpreter: Interpreter? = null
    
    val diseaseClasses = listOf(
        "Healthy",
        "Bacterial_Blight",
        "Anthracnose",
        "Cercospora_Fruit_Spot",
        "Alternaria_Fruit_Spot"
    )

    init {
        val modelBuffer = FileUtil.loadMappedFile(context, "farmlens_model.tflite")
        val options = Interpreter.Options()
        interpreter = Interpreter(modelBuffer, options)
    }

    fun predict(bitmap: Bitmap): PredictionResult? {
        val tflite = interpreter ?: return null
        
        // 1. Resize and normalize image
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        
        val intValues = IntArray(224 * 224)
        resizedBitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)
        
        for (pixelValue in intValues) {
            // Extract RGB (0-255)
            byteBuffer.putFloat(((pixelValue shr 16) and 0xFF).toFloat())
            byteBuffer.putFloat(((pixelValue shr 8) and 0xFF).toFloat())
            byteBuffer.putFloat((pixelValue and 0xFF).toFloat())
        }
        val inputs = arrayOf<Any>(byteBuffer)
        
        // 2. Prepare outputs
        val diseaseOutput = Array(1) { FloatArray(5) }
        val severityOutput = Array(1) { FloatArray(3) }
        
        // 3. Run Inference
        TFLiteHelper.runForMultipleInputs(interpreter, inputs, diseaseOutput, severityOutput)
        
        // 4. Parse Results
        val diseaseProbs = diseaseOutput[0]
        val severityProbs = severityOutput[0]
        
        var maxProb = 0f
        var bestIdx = 0
        for (i in diseaseProbs.indices) {
            if (diseaseProbs[i] > maxProb) {
                maxProb = diseaseProbs[i]
                bestIdx = i
            }
        }
        
        var severityScore = 0
        for (prob in severityProbs) {
            if (prob > 0.5f) severityScore++
        }
        
        return PredictionResult(
            disease = diseaseClasses[bestIdx],
            confidence = maxProb * 100f,
            severity = severityScore
        )
    }
    
    fun close() {
        interpreter?.close()
    }
}

data class PredictionResult(
    val disease: String,
    val confidence: Float,
    val severity: Int
)
