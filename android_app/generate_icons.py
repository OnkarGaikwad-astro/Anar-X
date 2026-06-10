import os
import sys
from PIL import Image

def resize_icon(source_path, res_dir):
    try:
        img = Image.open(source_path)
    except Exception as e:
        print(f"Error opening image: {e}")
        return

    # Android icon sizes
    sizes = {
        'mipmap-mdpi': 48,
        'mipmap-hdpi': 72,
        'mipmap-xhdpi': 96,
        'mipmap-xxhdpi': 144,
        'mipmap-xxxhdpi': 192
    }

    for folder, size in sizes.items():
        folder_path = os.path.join(res_dir, folder)
        if not os.path.exists(folder_path):
            os.makedirs(folder_path)
        
        resized_img = img.resize((size, size), Image.Resampling.LANCZOS)
        
        # Save standard icon
        out_path = os.path.join(folder_path, "ic_launcher.png")
        resized_img.save(out_path, format="PNG")
        
        # Save round icon (Android 7.1+)
        # For simplicity, we just save the same square icon for round. Real round icons would clip it, but this is fine.
        out_path_round = os.path.join(folder_path, "ic_launcher_round.png")
        resized_img.save(out_path_round, format="PNG")
        
        print(f"Saved {size}x{size} to {folder}")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python generate_icons.py <source_image> <res_dir>")
        sys.exit(1)
        
    source = sys.argv[1]
    res_directory = sys.argv[2]
    resize_icon(source, res_directory)
