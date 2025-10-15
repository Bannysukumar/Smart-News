# 🔥 Firebase Setup Guide for Way2News Clone

This guide will help you set up Firebase for the Way2News Clone Android app.

## 📋 Prerequisites

- Google account
- Android Studio
- Firebase project access

## 🚀 Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project"
3. Enter project name: `way2news-clone`
4. Enable Google Analytics (recommended)
5. Choose or create Analytics account
6. Click "Create project"

## 📱 Step 2: Add Android App

1. In Firebase Console, click "Add app" → Android
2. Enter package name: `com.way2.news`
3. Enter app nickname: `Way2News Clone`
4. Enter SHA-1 fingerprint (optional for now)
5. Click "Register app"

## 📄 Step 3: Download Configuration File

1. Download `google-services.json`
2. Place it in `app/` directory of your project
3. Make sure it's in the correct location: `app/google-services.json`

## 🔧 Step 4: Enable Firebase Services

### Authentication
1. Go to Authentication → Sign-in method
2. Enable the following providers:
   - **Email/Password**: Enable
   - **Phone**: Enable (optional)
   - **Google**: Enable (optional)

### Firestore Database
1. Go to Firestore Database
2. Click "Create database"
3. Choose "Start in test mode" (for development)
4. Select a location (choose closest to your users)

### Cloud Messaging
1. Go to Cloud Messaging
2. No additional setup required for basic functionality

### Analytics
1. Analytics is automatically enabled
2. No additional configuration needed

## 🗄️ Step 5: Firestore Database Structure

Create the following collections in Firestore:

### News Collection (`news`)
```json
{
  "id": "auto-generated",
  "title": "News Title",
  "summary": "News summary...",
  "content": "Full news content...",
  "imageUrl": "https://example.com/image.jpg",
  "category": "politics",
  "language": "en",
  "sourceUrl": "https://example.com/article",
  "author": "Author Name",
  "timestamp": "2024-01-01T00:00:00Z",
  "isBookmarked": false,
  "viewCount": 0,
  "isBreaking": false
}
```

### Categories Collection (`categories`)
```json
{
  "id": "auto-generated",
  "name": "politics",
  "displayName": "Politics",
  "iconUrl": "https://example.com/icon.png",
  "color": "#2196F3",
  "isEnabled": true,
  "order": 1,
  "supportedLanguages": ["en", "hi", "te"]
}
```

### Users Collection (`users`)
```json
{
  "uid": "firebase-auth-uid",
  "email": "user@example.com",
  "displayName": "User Name",
  "photoUrl": "https://example.com/photo.jpg",
  "phoneNumber": "+1234567890",
  "preferredLanguage": "en",
  "favoriteCategories": ["politics", "sports"],
  "savedNewsIds": ["news-id-1", "news-id-2"],
  "notificationsEnabled": true,
  "darkModeEnabled": false,
  "createdAt": "2024-01-01T00:00:00Z",
  "lastLoginAt": "2024-01-01T00:00:00Z",
  "fcmToken": "firebase-messaging-token"
}
```

## 🔐 Step 6: Firestore Security Rules

Update your Firestore security rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // News collection - read-only for authenticated users
    match /news/{document} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
        request.auth.token.admin == true; // Only admins can write
    }
    
    // Categories collection - read-only for authenticated users
    match /categories/{document} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
        request.auth.token.admin == true; // Only admins can write
    }
    
    // Users collection - users can read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && 
        request.auth.uid == userId;
    }
  }
}
```

## 🔔 Step 7: Cloud Messaging Setup

### Server Key (for sending notifications)
1. Go to Project Settings → Cloud Messaging
2. Copy the "Server key" (you'll need this for your backend)

### Client Setup
The app is already configured to receive notifications. No additional setup needed.

## 📊 Step 8: Analytics Configuration

Analytics is automatically configured. You can:
1. View analytics in Firebase Console
2. Set up custom events in your app
3. Configure conversion tracking

## 🧪 Step 9: Test Firebase Integration

### Test Authentication
1. Run the app
2. Try to sign up with email
3. Check Firebase Console → Authentication for new users

### Test Firestore
1. Add some test data to Firestore
2. Run the app and check if data loads
3. Verify offline functionality

### Test Notifications
1. Send a test notification from Firebase Console
2. Verify the app receives notifications
3. Test notification actions

## 🚨 Troubleshooting

### Common Issues

#### 1. App not connecting to Firebase
- Check if `google-services.json` is in the correct location
- Verify package name matches Firebase project
- Ensure internet connectivity

#### 2. Authentication not working
- Check if Authentication is enabled in Firebase Console
- Verify sign-in methods are enabled
- Check Firebase rules

#### 3. Firestore access denied
- Update security rules
- Check if user is authenticated
- Verify collection names match

#### 4. Notifications not received
- Check if Cloud Messaging is enabled
- Verify FCM token is generated
- Check notification permissions

### Debug Steps
1. Check Android Studio Logcat for Firebase errors
2. Verify Firebase project configuration
3. Test with Firebase Console tools
4. Check network connectivity

## 📚 Additional Resources

- [Firebase Android Documentation](https://firebase.google.com/docs/android/setup)
- [Firestore Documentation](https://firebase.google.com/docs/firestore)
- [Firebase Auth Documentation](https://firebase.google.com/docs/auth/android/start)
- [Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging/android/client)

## 🔄 Next Steps

After Firebase setup:
1. Test all Firebase features
2. Set up your backend API (if needed)
3. Configure AdMob (optional)
4. Deploy to production
5. Set up monitoring and analytics

---

**Need help?** Check the Firebase documentation or create an issue in the repository.
