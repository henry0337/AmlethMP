# AmlethMP – Linux

Ứng dụng Linux native, viết bằng Qt 6 (QML + C++), build bằng CMake.

## Yêu cầu

- Qt ≥ 6.4, CMake ≥ 3.21, Ninja, trình biên dịch C++17

Ubuntu/Debian:

```bash
sudo apt install build-essential cmake ninja-build \
    qt6-base-dev qt6-declarative-dev \
    qml6-module-qtquick qml6-module-qtquick-controls \
    qml6-module-qtquick-window qml6-module-qtquick-templates \
    qml6-module-qtquick-layouts qml6-module-qtqml-workerscript
```

## Build và chạy

```bash
cmake -S . -B build -G Ninja
cmake --build build
./build/amlethmp
```
