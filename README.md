# 🤖 Nector AI - Chatbot with Image Generation & Object Recognition

Nector AI is a powerful Android chatbot app built using Kotlin. It integrates AI text generation using the **DeepSeek-R1** model and supports image generation using the Hugging Face API. It also allows capturing images via camera and labeling objects using Google ML Kit, then asking the chatbot for related information.

## ✨ Features

- 💬 Chat with an AI assistant powered by **DeepSeek-R1**
- 🎨 Generate AI images using the `@imagine` command
- 📷 Capture photos using the camera and analyze them with Google ML Kit
- 🧠 Get AI explanations about objects detected in images
- 🖼️ Smooth chat UI with support for:
  - Text messages
  - Image messages
  - Typing indicator animation
- 📜 Markdown rendering in bot responses (powered by `Markwon`)

---

## 🛠️ Tech Stack

- **Kotlin**
- **Android ViewModel + LiveData**
- **Retrofit2** for API requests
- **ML Kit** for on-device image labeling
- **Glide** for image loading
- **Markwon** for Markdown text
- **Lottie** for typing animations

---

## 📸 Screenshots

| Chat UI | Chat with AI | AI Image Generation |
|---------------|--------------|---------------------|
| !(app/src/main/assets/ss1.jpg) | !(app/src/main/assets/ss2.jpg) | !(app/src/main/assets/ss3.jpg) |



---

## 📷 Usage

### 1. Start a Text Chat

Just enter a message and press send. The chatbot will reply with AI-generated content.

### 2. Generate Images

Type a prompt starting with `@imagine`. For example:


This sends a request to Hugging Face to generate an AI image.

### 3. Camera and Object Detection

- Click the 📷 icon to capture an image.
- The app uses ML Kit to label the image.
- It sends the most likely label to the chatbot for more info.

---

## 📦 API & Integration

### 🤖 Text Chat API
Powered by **OpenRouter** using the `deepseek/deepseek-r1-distill-llama-70b:free` model.

### 🖼️ Image Generation
Uses Hugging Face inference API with a `TextToImageRequest` payload.

### 🔍 Image Labeling
ML Kit's `ImageLabeler` is used for on-device image analysis.

---

## 🔐 Permissions

The app requires the following permissions:

- `CAMERA` - to capture images
- `INTERNET` - to access APIs


