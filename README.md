# 2D Particle Sandbox (Kotlin)

---

## 🇷🇺 Русский (Russian)

### 📋 Описание
Это продвинутая физическая песочница, написанная на Kotlin для Android. Проект базируется на системе клеточных автоматов и моделирует взаимодействие более чем 85 различных элементов. В приложении реализована сложная физика, химия и термодинамика, а также поддержка акселерометра для динамического изменения гравитации.

### ✨ Основные возможности
* **85+ элементов**: От песка и воды до антиматерии, вирусов и электрических схем.
* **Реалистичные состояния**: Твердые тела, сыпучие материалы, жидкости, газы и плазма.
* **Химические реакции**: Кислотная коррозия, горение топлива, распространение вирусов и мутации.
* **Термодинамика**: Плавление льда, испарение воды под воздействием огня и лавы.
* **Акселерометр**: Направление гравитации меняется при наклоне устройства (физика частиц следует за наклоном телефона).
* **Мультитач**: Возможность рисовать или спавнить элементы несколькими пальцами одновременно.

### 🛠️ Призыв к доработке (Open Source)
Проект содержит огромный список элементов (в файле Element.kt), но уникальная логика для многих из них еще не прописана в методе updatePhysics(). 
Я приглашаю сообщество разработчиков помочь в доработке всех предметов:
* **Электричество**: Реализовать передачу заряда для WIRE, TESLA, BATTERY и зажигание LED.
* **Порталы**: Создать механику телепортации частиц между точками входа и выхода PORTAL.
* **Оптимизация**: Улучшить алгоритмы обработки сетки для стабильного FPS при заполнении всего экрана.
* **Новые реакции**: Добавить свойства экзотическим материалам, таким как Темная материя, Спора и другие.

---

## 🇺🇸 English (Английский)

### 📋 Description
A sophisticated 2D physics sandbox built with Kotlin for Android. Based on cellular automata, this project simulates the interactions of over 85 unique elements. It features complex physics, chemistry, thermodynamics, and accelerometer-based gravity.

### ✨ Key Features
* **85+ Elements**: From basic sand and water to antimatter, viruses, and electrical circuits.
* **Realistic States**: Solids, powders, liquids, gases, and plasma.
* **Chemical Reactions**: Acid corrosion, fuel combustion, virus spreading, and mutations.
* **Thermodynamics**: Ice melting, water evaporation, and heat transfer logic.
* **Accelerometer Support**: Gravity direction adjusts dynamically as you tilt your device.
* **Multi-touch**: Draw or interact with particles using multiple fingers at once.

### 🛠️ Contribution (Help us build every element!)
The project defines a vast array of elements in Element.kt, but the specific logic for many is still pending in updatePhysics(). 
We encourage developers to help complete all items:
* **Electricity**: Implementing functional logic for WIRE, TESLA, BATTERY, and LED.
* **Portals**: Creating teleportation mechanics between PORTAL nodes.
* **Optimization**: Enhancing grid update algorithms for better performance with high particle counts.
* **New Behaviors**: Writing unique interaction rules for exotic materials like Dark Matter, Spore, etc.

---

## ⚙️ Installation / Установка
1. Клонируйте репозиторий / Clone the repository.
2. Откройте в Android Studio / Open in Android Studio.
3. Скомпилируйте и запустите на Android устройстве / Build and Run on an Android device.

## 📄 License
This project is licensed under the MIT License.
