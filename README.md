# Kernel Image Processing

## Overview
This project implements image processing using **convolution kernels**. It demonstrates how different execution models—**sequential, parallel, and distributed**—affect performance when applying filters to images.

The program applies common convolution filters, such as sharpening and edge detection, and compares computation times across different image sizes.

---

## Features
* **Kernel-based image convolution**: Core processing engine.
* **Sequential implementation**: Single-threaded baseline.
* **Parallel implementation**: Utilizes multiple CPU cores via `ExecutorService`.
* **Distributed implementation**: Uses **MPJ (Message Passing Interface for Java)** to spread tasks across nodes.
* **Performance Benchmarking**: Comparative analysis across different image resolutions.
* **Example Filters**: Includes sharpening and edge detection algorithms.

---

## How It Works
1.  **Loading**: The program loads an image into a pixel buffer.
2.  **Application**: A convolution kernel (a small matrix) is applied to each pixel.

3.  **Calculation**: New pixel values are calculated using a weighted sum of neighboring pixels based on the kernel values.
4.  **Buffering**: The processed image is written to an output buffer and exported.

---

## Execution Modes

| Mode | Strategy | Use Case |
| :--- | :--- | :--- |
| **Sequential** | Processes one pixel at a time. | Simple and effective for small images. |
| **Parallel** | Splits the image into segments processed by multiple threads. | Best for large images on multi-core systems. |
| **Distributed** | Divides the image across multiple nodes using MPJ. | Demonstrates horizontal scaling (subject to network latency). |

---

## Results Summary
* **Sequential** is fastest for small images where thread management overhead would outweigh processing gains.
* **Parallel** performs best for large images by utilizing available hardware concurrency.
* **Distributed** processing often introduces network and serialization overhead, which can make it slower than local parallel processing in smaller-scale tests.

---

## Technologies
* **Java**
* **Multithreading** (`ExecutorService`)
* **MPJ** (Message Passing Interface for Java)

## Purpose
The project demonstrates the fundamentals of convolution-based image processing and compares the performance trade-offs between sequential, parallel, and distributed approaches.
