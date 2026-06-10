import os
import tensorflow as tf
from train import load_real_data

def representative_data_gen():
    # Load just a few batches from training data to calibrate quantization
    train_dataset, _ = load_real_data(batch_size=1)
    # Take 100 images for calibration
    for img, _ in train_dataset.take(100):
        # TFLite representative dataset expects a list of inputs (since we have 1 input, a list of length 1)
        yield [img]

def export_to_tflite():
    print("Loading keras model...")
    model = tf.keras.models.load_model('saved_models/farmlens_model.keras')
    
    print("Initializing TFLite converter...")
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    
    # We will export the model as standard float32 without quantization
    # because INT8 quantization completely corrupted the model weights.
    
    print("Converting model...")
    tflite_model = converter.convert()
    
    output_path = 'saved_models/farmlens_model.tflite'
    with open(output_path, 'wb') as f:
        f.write(tflite_model)
        
    print(f"TFLite model exported successfully to {output_path}!")
    print(f"Size: {len(tflite_model) / (1024*1024):.2f} MB")

if __name__ == '__main__':
    export_to_tflite()
