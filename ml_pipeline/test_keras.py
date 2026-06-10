import numpy as np
import tensorflow as tf

def test_keras_model(model_path):
    model = tf.keras.models.load_model(model_path)
    print("Model loaded.")
    
    input_shape = model.input_shape
    print("Input shape:", input_shape)
    
    # Generate random noise
    input_data = np.random.uniform(0, 255, size=(1, 224, 224, 3)).astype(np.float32)
    
    output = model.predict(input_data)
    print("Random Noise Output:", output)
    
if __name__ == '__main__':
    test_keras_model('c:/Users/onkar/Desktop/aurelix/Projects/Anar_Lens/ml_pipeline/saved_models/farmlens_model.keras')
