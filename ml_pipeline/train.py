import os
import glob
import numpy as np
import tensorflow as tf
from model import build_dual_head_model, DISEASE_CLASSES

def calculate_heuristic_severity(img_tensor, is_healthy):
    if is_healthy:
        return 0
    # img_tensor is float32 [0, 255] or uint8, we will make sure it's float32 [0, 255]
    img = tf.cast(img_tensor, tf.float32)
    
    # Calculate a proxy for "dark spots" (blemishes). 
    # We convert to grayscale and look for pixels below a certain brightness threshold.
    gray = tf.image.rgb_to_grayscale(img)
    # Threshold for dark spots (e.g. less than 80 out of 255)
    dark_pixels = tf.reduce_sum(tf.cast(gray < 80.0, tf.float32))
    total_pixels = tf.cast(tf.size(gray), tf.float32)
    
    dark_ratio = dark_pixels / total_pixels
    
    # Bucket into severity levels: 1 (Early), 2 (Moderate), 3 (Severe)
    # Ratios are arbitrary heuristics for this dataset.
    if dark_ratio < 0.05:
        return 1
    elif dark_ratio < 0.20:
        return 2
    else:
        return 3

def load_real_data(base_dir='../Pomegranate Diseases Dataset', batch_size=16):
    # Map dataset folder names to DISEASE_CLASSES
    folder_mapping = {
        'Healthy': 'Healthy',
        'Bacterial_Blight': 'Bacterial Blight',
        'Anthracnose': 'Anthracnose',
        'Cercospora': 'Cercospora Fruit Spot',
        'Alternaria': 'Alternaria Fruit Spot'
    }
    
    image_paths = glob.glob(f"{base_dir}/*/*.jpg")
    np.random.shuffle(image_paths)
    
    paths_list = []
    disease_labels = []
    is_healthy_list = []
    
    for path in image_paths:
        folder_name = os.path.basename(os.path.dirname(path))
        class_name = folder_mapping.get(folder_name, 'Healthy')
        disease_idx = DISEASE_CLASSES.index(class_name)
        paths_list.append(path)
        disease_labels.append(disease_idx)
        is_healthy_list.append(disease_idx == 0)
        
    def process_path(path_tensor, disease_idx, is_healthy):
        # Load and preprocess image
        img_raw = tf.io.read_file(path_tensor)
        img = tf.image.decode_jpeg(img_raw, channels=3)
        img_resized = tf.image.resize(img, [224, 224])
        
        severity = calculate_heuristic_severity(img_resized, is_healthy)
        
        # Convert severity to ordinal array [1.0, 1.0, 0.0]
        # Since we can't easily use variable array indexing in pure TF map without tf.while,
        # we can just use simple conditionals.
        s1 = tf.cast(severity > 0, tf.float32)
        s2 = tf.cast(severity > 1, tf.float32)
        s3 = tf.cast(severity > 2, tf.float32)
        severity_ordinal = tf.stack([s1, s2, s3])
        
        return img_resized, (disease_idx, severity_ordinal)

    dataset = tf.data.Dataset.from_tensor_slices((paths_list, disease_labels, is_healthy_list))
    # Filter out corrupted images by reading them inside map, but decode_jpeg throws error.
    # We will just map it. If there is a corrupted image, it will crash, but hopefully dataset is clean.
    dataset = dataset.map(process_path, num_parallel_calls=tf.data.AUTOTUNE)
    
    # Create train and val splits (80/20)
    total_images = len(paths_list)
    train_size = int(0.8 * total_images)
    
    train_dataset = dataset.take(train_size).cache().shuffle(1000).batch(batch_size).prefetch(tf.data.AUTOTUNE)
    val_dataset = dataset.skip(train_size).cache().batch(batch_size).prefetch(tf.data.AUTOTUNE)
    
    return train_dataset, val_dataset

def train_model():
    print("Loading real dataset...")
    train_dataset, val_dataset = load_real_data()
    
    print("Building model...")
    # Use imagenet weights since we have real data to fine-tune
    model = build_dual_head_model()
    
    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=1e-4),
        loss={
            'disease_output': 'sparse_categorical_crossentropy',
            'severity_output': 'binary_crossentropy'
        },
        loss_weights={'disease_output': 1.0, 'severity_output': 0.5},
        metrics={
            'disease_output': ['accuracy'],
            'severity_output': ['accuracy']
        }
    )
    
    print("Training model...")
    # Train for 5 epochs for demonstration on the real dataset
    model.fit(
        train_dataset, 
        validation_data=val_dataset,
        epochs=5
    )
    
    os.makedirs('saved_models', exist_ok=True)
    model.save('saved_models/farmlens_model.keras')
    print("Model saved to saved_models/farmlens_model.keras")

if __name__ == '__main__':
    train_model()
