# 🍎 Anar X

> **AI-powered pomegranate disease detection and farm management system for farmers in Maharashtra, India.**

Anar X is an on-device AI agriculture application designed to help pomegranate farmers detect crop diseases, estimate disease severity, receive treatment recommendations, estimate yield, manage farm activities, and access market and weather information from a single Android application.

The project combines **edge AI, computer vision, multi-task deep learning, on-device LLMs, and farm-management tools** with a focus on offline-first operation.

---

## ✨ Features

### 🤖 AI & Computer Vision

- **Disease Detection**
  - Detects:
    - Healthy
    - Bacterial Blight
    - Anthracnose
    - Cercospora Fruit Spot
    - Alternaria Fruit Spot
  - Runs locally on the Android device using TensorFlow Lite.

- **Disease Severity Estimation**
  - Four severity levels:
    - Healthy
    - Early
    - Moderate
    - Severe
  - Uses ordinal severity estimation rather than treating severity as unrelated classes.

- **On-Device AI Farming Assistant**
  - Google Gemma running through MediaPipe GenAI.
  - Provides contextual farming advice without requiring a cloud LLM.

- **Yield Estimation**
  - Detects ripe red pomegranates from tree images.
  - Uses lightweight color-based computer vision and connected-region detection.
  - Estimates approximate fruit weight from detected fruit count.

### 🌾 Farm Management

- 📅 Spray and treatment scheduling
- ⏰ Treatment reminders and alarms
- 💰 Farm expense tracking
- 📊 Pomegranate market prices
- 🌤️ GPS-based weather information
- 📜 Disease scan history
- 💬 Farmer community forum

### 📱 Offline-First AI

The core disease-detection pipeline runs completely on-device:

```text
Camera / Gallery
      ↓
Image Preprocessing
      ↓
EfficientNet-B0
      ↓
Disease + Severity
      ↓
Treatment Recommendation
```

No network connection is required for the core disease inference pipeline.

---

# 🧠 AI Architecture

Anar X uses a **dual-head multi-task learning architecture** built around EfficientNet-B0.

```text
                    Input Image
                 224 × 224 × 3 RGB
                         │
                         ▼
              ┌─────────────────────┐
              │   EfficientNet-B0   │
              │ ImageNet pretrained  │
              │   + fine-tuning      │
              └──────────┬──────────┘
                         │
                 Global Average
                    Pooling
                         │
                      Dropout
                         │
              ┌──────────┴──────────┐
              │                     │
              ▼                     ▼
      Disease Classification   Severity Estimation
           5 classes              Ordinal
           Softmax                3 Sigmoid
              │                     │
              ▼                     ▼
        Disease Label          Severity Level
```

### Model configuration

| Component | Details |
|---|---|
| Backbone | EfficientNet-B0 |
| Pretraining | ImageNet |
| Architecture | Dual-head multi-task learning |
| Disease Head | 5-class softmax |
| Severity Head | Ordinal regression |
| Input | 224 × 224 × 3 RGB |
| Framework | TensorFlow 2.15 / Keras |
| Mobile Runtime | TensorFlow Lite |
| Export | Float32 TFLite |

---

# 🦠 Disease Classes

| Class | Disease |
|---:|---|
| 0 | Healthy |
| 1 | Bacterial Blight |
| 2 | Anthracnose |
| 3 | Cercospora Fruit Spot |
| 4 | Alternaria Fruit Spot |

---

# 📈 Severity Estimation

Severity is modeled as an ordinal prediction problem.

The model predicts:

```text
P(severity > 0)
P(severity > 1)
P(severity > 2)
```

These probabilities are converted into four levels:

| Level | Meaning | Encoding |
|---:|---|---|
| 0 | Healthy | `[0, 0, 0]` |
| 1 | Early | `[1, 0, 0]` |
| 2 | Moderate | `[1, 1, 0]` |
| 3 | Severe | `[1, 1, 1]` |

The current training pipeline generates severity labels heuristically using the ratio of dark pixels in the image:

- **Early:** `< 5%`
- **Moderate:** `5–20%`
- **Severe:** `> 20%`
- Healthy images always receive severity `0`.

> **Note:** Replacing these heuristic labels with expert-annotated severity data is a planned improvement.

---

# 📊 Dataset

The current dataset contains **5,099 images**.

| Class | Images |
|---|---:|
| Healthy | 1,450 |
| Anthracnose | 1,166 |
| Bacterial Blight | 966 |
| Alternaria | 886 |
| Cercospora | 631 |
| **Total** | **5,099** |

Images are organized into disease-specific directories under:

```text
Pomegranate Diseases Dataset/
```

---

# 🏋️ Training Pipeline

The ML pipeline is implemented in Python using TensorFlow and Keras.

### Training configuration

```text
Optimizer:        Adam
Learning Rate:    1e-4
Disease Loss:     Sparse Categorical Crossentropy
Severity Loss:    Binary Crossentropy
Disease Weight:   1.0
Severity Weight:  0.5
Epochs:           5
Train/Val Split:  80/20
Batch Size:       16
```

### Training flow

```text
Dataset
   │
   ▼
Image Loading
   │
   ▼
Resize → 224×224
   │
   ▼
tf.data Pipeline
   │
   ▼
EfficientNet-B0
   │
   ├───────────────┐
   ▼               ▼
Disease Head   Severity Head
   │               │
   └───────┬───────┘
           ▼
       Joint Loss
           │
           ▼
      Trained Model
           │
           ▼
      TFLite Export
```

---

# 📱 Android Inference

The Android application loads the TFLite model from the application assets.

```text
Camera / Gallery
       ↓
Bitmap
       ↓
Resize to 224×224
       ↓
RGB Float32 ByteBuffer
       ↓
TFLite Interpreter
       ↓
Disease Probabilities
+
Severity Probabilities
       ↓
PredictionResult
```

The application uses:

- Kotlin
- Jetpack Compose
- CameraX
- TensorFlow Lite
- Room Database
- Retrofit
- MediaPipe GenAI

---

# 🧠 On-Device Gemma

Anar X also integrates Google Gemma through MediaPipe GenAI.

```text
Farmer Question
      ↓
On-Device Gemma
      ↓
Contextual Farming Advice
```

Configuration:

| Feature | Details |
|---|---|
| Model | Google Gemma |
| Runtime | MediaPipe `LlmInference` |
| Model File | `gemma.bin` |
| Max Tokens | 512 |
| Temperature | 0.7 |
| Primary Use | Farming advice and recommendations |

This provides an additional AI layer without requiring every farming question to be sent to a cloud service.

---

# 🍎 Yield Estimation

Anar X includes a lightweight computer-vision-based yield estimator.

The current implementation detects ripe red pomegranates using color segmentation.

```text
Tree Image
    ↓
Image Downscaling
    ↓
Red-Pixel Detection
    ↓
BFS Flood Fill
    ↓
Fruit Regions
    ↓
Noise Filtering
    ↓
Fruit Count
    ↓
Approximate Weight
```

Current detection heuristic:

```text
R > 90
R > G × 1.4
R > B × 1.4
```

The current estimate uses approximately:

```text
1 fruit ≈ 0.25 kg
```

A future version can replace this approach with a dedicated object-detection model such as YOLO or SSD.

---

# 🌦️ Weather Intelligence

Anar X uses weather information to provide contextual farming alerts.

Examples include:

| Condition | Recommendation |
|---|---|
| Humidity > 75% | Consider fungal-disease prevention measures |
| Temperature > 35°C | Ensure irrigation and avoid spraying during peak sun |
| Temperature < 15°C | Protect crops from cold stress |
| Normal conditions | Continue routine crop care |

---

# 💰 Market Prices

The application retrieves pomegranate market-price information through the Agmarknet service.

Farmers can use the market-price screen to view available price information for pomegranate markets across Maharashtra.

---

# 🗂️ Project Structure

```text
Anar_X/
│
├── Pomegranate Diseases Dataset/
│   ├── Alternaria/
│   ├── Anthracnose/
│   ├── Bacterial_Blight/
│   ├── Cercospora/
│   └── Healthy/
│
├── ml_pipeline/
│   ├── model.py
│   ├── train.py
│   ├── export.py
│   ├── export_tflite.py
│   ├── test_keras.py
│   ├── test_tflite.py
│   ├── dummy_data.py
│   ├── requirements.txt
│   ├── saved_models/
│   └── tflite_model/
│
└── android_app/
    └── app/src/main/java/com/farmlens/anarai/
        ├── MainActivity.kt
        ├── AlarmScreenActivity.kt
        │
        ├── api/
        │   └── OllamaApiService.kt
        │
        ├── data/
        │   ├── AppDatabase.kt
        │   ├── ScanHistoryEntity.kt
        │   ├── ScanHistoryDao.kt
        │   ├── SprayScheduleEntity.kt
        │   ├── SprayScheduleDao.kt
        │   ├── ExpenseEntity.kt
        │   ├── ExpenseDao.kt
        │   ├── ForumPostEntity.kt
        │   ├── ForumDao.kt
        │   ├── AlarmReceiver.kt
        │   ├── WeatherService.kt
        │   ├── MarketPriceService.kt
        │   └── remote/
        │
        ├── ml/
        │   ├── MLService.kt
        │   ├── OnDeviceLLMService.kt
        │   └── TFLiteHelper.java
        │
        ├── ui/
        │   ├── theme/
        │   ├── screens/
        │   └── viewmodels/
        │
        └── util/
            └── TimeUtils.kt
```

---

# 🛠️ Technology Stack

## Android

- **Kotlin**
- **Jetpack Compose**
- **Material 3**
- **CameraX**
- **Room / SQLite**
- **Retrofit 2**
- **OkHttp**
- **Gson**
- **Coil Compose**
- **TensorFlow Lite**
- **MediaPipe GenAI**

## Machine Learning

- **Python**
- **TensorFlow 2.15**
- **Keras**
- **EfficientNet-B0**
- **NumPy**
- **Pillow**
- **tf.data**

## Backend & Services

- **Supabase / PostgreSQL**
- **Agmarknet**
- **wttr.in**
- **Ollama**
- **LLaMA 3**

---

# 🔌 External Services

| Service | Purpose |
|---|---|
| Agmarknet | Pomegranate market prices |
| wttr.in | Weather information |
| Supabase | Community forum backend |
| Ollama | Local-network LLM fallback |

The core disease inference does not depend on these external services.

---

# 🚀 Getting Started

## 1. Clone the repository

```bash
git clone <YOUR_REPOSITORY_URL>
cd Anar_X
```

## 2. Set up the ML pipeline

```bash
cd ml_pipeline

pip install -r requirements.txt
```

Train the model:

```bash
python train.py
```

Export the trained model:

```bash
python export_tflite.py
```

Place the resulting TFLite model into:

```text
android_app/app/src/main/assets/
```

---

## 3. Open the Android project

Open:

```text
android_app/
```

in Android Studio.

Make sure an Android device running **API 24 or newer** is available.

---

## 4. Configure Supabase

Configure the required Supabase credentials in:

```text
local.properties
```

Do not commit private API keys or credentials to Git.

---

## 5. Build and run

Build the Android application from Android Studio and install it on a physical Android device.

---

# 🔐 Privacy & Offline AI

Anar X is designed around an **offline-first architecture**.

The primary disease-detection pipeline operates locally:

```text
Image
 ↓
Local ML Model
 ↓
Prediction
```

The application does not need to upload a crop image to a cloud AI service for disease inference.

The on-device Gemma integration also enables local AI-powered farming assistance.

---

# 🧪 Current AI Innovations

### 1. Multi-Task Learning

A shared EfficientNet-B0 backbone simultaneously learns disease classification and severity estimation.

### 2. Ordinal Severity Modeling

Severity levels have a natural ordering, so Anar X models severity using cumulative ordinal predictions rather than independent classes.

### 3. Edge AI

Disease inference is performed directly on the farmer's smartphone using TensorFlow Lite.

### 4. On-Device LLM

Gemma provides local AI-powered farming assistance through MediaPipe GenAI.

### 5. Lightweight Yield Estimation

The current yield estimator uses efficient computer vision instead of requiring another neural network.

---

# 🔮 Future Roadmap

The project can be extended in several directions:

- [ ] Train on a substantially larger real-world dataset
- [ ] Replace heuristic severity labels with expert annotations
- [ ] Add more pomegranate diseases
- [ ] Add disease detection for additional crops
- [ ] Replace color-based fruit detection with YOLO/SSD
- [ ] Fine-tune Gemma on agriculture-specific knowledge
- [ ] Add Marathi and Hindi UI
- [ ] Add Marathi/Hindi text-to-speech
- [ ] Add cloud synchronization
- [ ] Add soil-moisture sensor integration
- [ ] Add automated weather-based alerts
- [ ] Build a farmer marketplace
- [ ] Add more advanced agricultural forecasting

---

# 🎯 Vision

The long-term goal of **Anar X** is to turn a smartphone into an accessible AI agricultural assistant for pomegranate farmers.

Instead of providing only a disease label, the system is designed to connect:

```text
                ┌──────────────┐
                │ Crop Image   │
                └──────┬───────┘
                       ↓
                ┌──────────────┐
                │ Disease AI   │
                └──────┬───────┘
                       ↓
                ┌──────────────┐
                │ Severity AI  │
                └──────┬───────┘
                       ↓
             ┌─────────┴─────────┐
             ↓                   ↓
      Treatment Advice       Farm Actions
             │                   │
             └─────────┬─────────┘
                       ↓
              ┌────────────────┐
              │ Farm Assistant │
              └────────────────┘
```

The vision is an **AI-first farm companion**, rather than simply a disease-classification application.

---

# ⚠️ Disclaimer

Anar X is a research and software project. Disease predictions, severity estimates, treatment suggestions, yield estimates, weather alerts, and market information should be treated as decision-support information and should not replace advice from qualified agricultural experts.

---

## 📜 License

Add your preferred open-source license here, such as MIT, Apache-2.0, or GPL-3.0.

---

## 👨‍💻 Project

**Anar X**  
AI-powered pomegranate disease detection and farm intelligence system.

Built with ❤️ using **AI, computer vision, edge computing, and Android**.
