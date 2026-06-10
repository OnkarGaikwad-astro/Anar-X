import os
import numpy as np
from PIL import Image

# Based on model.py
DISEASE_CLASSES = [
    'Healthy',
    'Bacterial_Blight',
    'Anthracnose',
    'Cercospora_Fruit_Spot',
    'Alternaria_Fruit_Spot'
]

def generate_dummy_dataset(base_dir='dataset', num_images_per_class=10):
    os.makedirs(base_dir, exist_ok=True)
    
    # We will encode severity in the filename for the dummy dataset loader to pick up.
    # Format: {class_name}_{severity}_{uuid}.jpg
    # Severity 0 for Healthy, 1-3 for diseases
    
    for cls in DISEASE_CLASSES:
        cls_dir = os.path.join(base_dir, cls)
        os.makedirs(cls_dir, exist_ok=True)
        
        for i in range(num_images_per_class):
            # Create a dummy RGB image
            img_array = np.random.randint(0, 255, (224, 224, 3), dtype=np.uint8)
            img = Image.fromarray(img_array)
            
            if cls == 'Healthy':
                severity = 0
            else:
                severity = np.random.randint(1, 4) # 1, 2, or 3
            
            filename = f"{cls}_{severity}_{i}.jpg"
            img.save(os.path.join(cls_dir, filename))
            
    print(f"Generated {len(DISEASE_CLASSES) * num_images_per_class} dummy images in '{base_dir}'.")

if __name__ == '__main__':
    generate_dummy_dataset()
