import tensorflow as tf
import os
import numpy as np

def convert_to_tflite():
    model_path = 'saved_models/farmlens_model.keras'
    if not os.path.exists(model_path):
        print("Model not found. Please run train.py first.")
        return

    print("Loading saved model...")
    model = tf.keras.models.load_model(model_path)
    
    print("Converting to TFLite (INT8 Quantization)...")
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    
    # Enable INT8 Quantization
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    
    # Representative dataset for quantization
    def representative_dataset():
        for _ in range(100):
            # Generate random representative data
            # EfficientNetB0 takes 224x224x3
            data = np.random.rand(1, 224, 224, 3)
            yield [data.astype(np.float32)]
            
    converter.representative_dataset = representative_dataset
    
    # Restrict supported types to INT8
    converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
    # Keep input/output as float32 for easier integration in Flutter, 
    # or uint8 if you want full integer pipeline.
    # We will keep them as float32 for now to match default TFLite flutter plugins easily,
    # but the internal ops will be INT8.
    converter.inference_input_type = tf.float32
    converter.inference_output_type = tf.float32

    tflite_model = converter.convert()
    
    os.makedirs('tflite_model', exist_ok=True)
    tflite_path = 'tflite_model/farmlens.tflite'
    with open(tflite_path, 'wb') as f:
        f.write(tflite_model)
        
    print(f"TFLite model saved to {tflite_path}")
    print(f"Model size: {os.path.getsize(tflite_path) / (1024 * 1024):.2f} MB")

if __name__ == '__main__':
    convert_to_tflite()
