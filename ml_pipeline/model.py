import tensorflow as tf
from tensorflow.keras.applications import EfficientNetB0
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D, Dropout
from tensorflow.keras.models import Model

# Disease Classes
DISEASE_CLASSES = [
    'Healthy',
    'Bacterial Blight',
    'Anthracnose',
    'Cercospora Fruit Spot',
    'Alternaria Fruit Spot'
]
NUM_DISEASES = len(DISEASE_CLASSES)

# Severity Levels
# 0 = Healthy, 1 = Early, 2 = Moderate, 3 = Severe
NUM_SEVERITY_LEVELS = 4

def build_dual_head_model(input_shape=(224, 224, 3)):
    """
    Builds an EfficientNet-B0 based model with two output heads:
    1. Disease Classification (softmax)
    2. Severity Estimation (softmax, used as ordinal approximation)
    """
    inputs = tf.keras.Input(shape=input_shape, name='image_input')
    
    # EfficientNetB0 backbone
    # Weights='imagenet' might require internet access, we will use it for actual training.
    # For dummy testing, we can initialize randomly if no internet, but usually it caches.
    base_model = EfficientNetB0(
        include_top=False, 
        weights='imagenet',
        input_tensor=inputs
    )
    
    x = GlobalAveragePooling2D()(base_model.output)
    x = Dropout(0.2)(x)
    
    # Head 1: Disease Classification
    disease_out = Dense(NUM_DISEASES, activation='softmax', name='disease_output')(x)
    
    # Head 2: Severity Estimation (Ordinal Classification)
    # We use NUM_SEVERITY_LEVELS - 1 units with sigmoid activation.
    # Each unit predicts the probability of severity being strictly greater than k.
    severity_out = Dense(NUM_SEVERITY_LEVELS - 1, activation='sigmoid', name='severity_output')(x)
    
    model = Model(inputs=inputs, outputs=[disease_out, severity_out], name='FarmLens_Anar_Model')
    
    return model

if __name__ == '__main__':
    model = build_dual_head_model()
    model.summary()
