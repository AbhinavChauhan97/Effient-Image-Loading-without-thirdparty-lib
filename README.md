# Image Loading Android Application

## Overview

This Android application is designed to efficiently load and display images in a scrollable grid. It implements asynchronous image loading without the use of any third-party image loading library. The application fetches images from a provided API endpoint, constructs image URLs using response data, and handles caching, error handling, and graceful fallbacks for failed image loads.

## Features

- Display a 3-column square image grid.
- Asynchronously load images from a specified API endpoint.
- Construct image URLs using thumbnail data from the API response.
- Cache images in both memory and disk for efficient retrieval.
- Gracefully handle network errors and image loading failures.
- Supports scrolling through at least 100 images.

## Technology Stack

- Language: Kotlin
- Architecture: MVVM (Model-View-ViewModel)
- Dependency Injection: None (No third-party libraries used)
- Network Communication: Retrofit
- Image Caching: LruCache (memory), DiskLruCache (disk)

## Implementation Details

- The application fetches images from the provided API endpoint using Retrofit.
- Image URLs are constructed using thumbnail data from the API response.
- Images are cached in memory using LruCache and on disk using DiskLruCache.
- Asynchronous image loading is implemented using coroutines.
- Network errors and image loading failures are gracefully handled, displaying informative error messages or placeholders.

## Prerequisites

- Android Studio Iguana
- Android SDK 34


## Usage

- Launch the application on an Android device.
- Scroll through the image grid to view images loaded from the API.
- Check caching behavior by scrolling back and forth through the grid.
- Verify error handling by toggling network connectivity and observing error messages.

