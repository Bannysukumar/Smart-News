<!-- readme-seo: bannysukumar-professional-v4 -->

# Smart News

Smart News is an Android news app. The Java package is `com.way2.news`. Activities cover the news list and detail, login, signup, comments, notifications, language selection, profiles, and an admin dashboard.

## Overview

`MainActivity` is the launcher class. Feature screens are activities under `app/src/main/java/com/way2/news/activities`. The repository includes `FIREBASE_SETUP.md` and `AUTHENTICATION_GUIDE.md`. The package name contains `way2`, which is the code namespace. This README uses Smart News as the project title and does not present the app as an official Way2News product.

## Features

Activity classes in the source:

- News details, add news, and comments
- Login, signup, forgot password, and language selection
- Notifications, settings, and user profile
- Admin login, admin dashboard, and pending submissions
- Followers and following

## Tech Stack

| Technology | Where it shows up |
|---|---|
| Java | `com.way2.news` |
| Android Gradle | `build.gradle.kts`, `gradlew` |
| Firebase setup notes | `FIREBASE_SETUP.md` |

## Architecture

Android activities in `app` → Firebase, as described by `FIREBASE_SETUP.md` in the repository.

## Project Structure

```text
Smart-News/
├── app/src/main/java/com/way2/news/
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── FIREBASE_SETUP.md
└── AUTHENTICATION_GUIDE.md
```

## Prerequisites

- Android Studio, or a JDK plus the Gradle wrapper

## Installation

```bash
git clone https://github.com/Bannysukumar/Smart-News.git
cd Smart-News
```

Open the project in Android Studio. Follow `FIREBASE_SETUP.md` before using Firebase-backed screens.

## Configuration

Firebase setup steps are written in `FIREBASE_SETUP.md`. Do not commit a production `google-services.json` key material beyond what you intend to publish.

## Usage

Run the `app` module. Language choice is `LanguageSelectionActivity`. News reading is `NewsDetailsActivity`. Admin entry is `AdminLoginActivity`.

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

## License

Licensed under MIT. See [LICENSE](LICENSE).

## Author

Banny Sukumar

GitHub: https://github.com/Bannysukumar
