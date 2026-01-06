🎵 Android Music Player Application
📌 Project Overview

This project is an Android music player application that allows users to browse local audio files, manage playlists (including nested playlists), and play music offline.
The application focuses on simplicity, offline usability, and efficient playlist management, while following modern Android development best practices.

🎯 Motivation

The motivation behind this project was to create a lightweight and offline-friendly music player that offers better control over playlists compared to many existing applications, which are often cluttered or dependent on internet connectivity.

✨ Main Features

Browse local audio files from device storage

Play, pause, and navigate through songs

Create, edit, and delete playlists

Support for nested playlists (playlist inside another playlist)

Prevents cyclic playlist relationships

Smooth and responsive UI using RecyclerView

Offline usage (no internet required)

🏗️ Technical Architecture

The application follows the MVVM (Model–View–ViewModel) architecture pattern to ensure a clean separation of concerns.

Architecture layers:

View (UI): Activities and Fragments

ViewModel: Handles UI-related logic and state

Repository: Manages data access and business logic

Data Sources: Room Database and Android MediaStore

🧠 Android Concepts Used

Activities & Fragments

Navigation Component

RecyclerView & ListAdapter

ViewModel & LiveData

Room Database

MediaStore API

Dialogs for user feedback
