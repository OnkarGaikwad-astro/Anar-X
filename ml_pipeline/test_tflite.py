import numpy as np
import tensorflow as tf

def test_tflite_model(model_path):
    interpreter = tf.lite.Interpreter(model_path=model_path)
    interpreter.allocate_tensors()

    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    print("Input details:", input_details)
    print("Output details:", output_details)

    # Create dummy image [0, 255] float32
    input_data = np.random.uniform(0, 255, size=input_details[0]['shape']).astype(np.float32)
    interpreter.set_tensor(input_details[0]['index'], input_data)
    interpreter.invoke()

    disease_out = interpreter.get_tensor(output_details[1]['index'])
    severity_out = interpreter.get_tensor(output_details[0]['index'])

    print("Random Noise Output (Disease):", disease_out)
    
    # Create blank image [0]
    input_data = np.zeros(shape=input_details[0]['shape']).astype(np.float32)
    interpreter.set_tensor(input_details[0]['index'], input_data)
    interpreter.invoke()
    disease_out = interpreter.get_tensor(output_details[1]['index'])
    print("Zero Image Output (Disease):", disease_out)

if __name__ == '__main__':
    test_tflite_model('c:/Users/onkar/Desktop/aurelix/Projects/Anar_Lens/ml_pipeline/tflite_model/farmlens.tflite')
