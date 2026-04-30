# Travel Buddy Project Spec

## Overview
Travel Buddy is an Android app that matches travelers by destination, date overlap, travel style, budget, and language so they can join a person or group on the same journey.

## Goals
- Help users find compatible travel buddies quickly.
- Keep the app safe, trustworthy, and easy to use.
- Use ethical gamification to encourage progress and return visits without manipulative dark patterns.

## MVP Features
- Signup and login.
- Profile creation with photo, bio, languages, budget, travel style, and interests.
- Trip creation with destination, dates, vibe, and intent.
- Match feed and ranked recommendations.
- Join request flow.
- 1:1 and group chat.
- Safety tools: report, block, verification, emergency contact, meetup confirmation.
- Progress meter, badges, and city stamps.

## Key Screens
- Onboarding
- Login / Signup
- Profile Setup
- Home Dashboard
- Create Trip
- Matches
- Profile Detail
- Join Request
- Chat
- Notifications
- Safety Center
- Settings

## Matching Logic
- Destination match: 40
- Date overlap: 25
- Travel style fit: 15
- Budget fit: 10
- Language overlap: 5
- Shared interests: 5

## Data Model
- users
- profiles
- trips
- trip_members
- join_requests
- matches
- chats
- messages
- notifications
- badges
- user_badges
- quests
- user_progress
- reports

## Tech Stack
- Android Kotlin
- Jetpack Compose
- Firebase Auth
- Firestore or Supabase
- Firebase Cloud Messaging
- Google Maps later

## Build & Distribution
- GitHub Actions workflow `Build Debug APK` runs on every push, uploads `app-debug.apk` as a workflow artifact (sideloadable).
- Optional: Appetize.io publishing for a browser-playable demo URL when the `APPETIZE_API_TOKEN` repo secret is set. See README for setup.
- Production distribution (signed release APK / Play Store internal track) is deferred until Firebase Auth + Firestore are wired.

## Retention and Gamification
- Trip readiness meter
- Quest system
- Match momentum notifications
- Badge unlocks
- City stamps
- Post-trip recap cards

## Safety Principles
- No fake scarcity.
- No confirm-shaming.
- Users control notifications.
- Chat unlocks after approval.
- Sensitive info hidden by default.

## Roadmap
### Phase 1
- App shell
- Profile and trip creation
- Matching
- Join request flow

### Phase 2
- Chat
- Safety tools
- Notifications
- Basic gamification

### Phase 3
- Group trips
- Reputation score
- Premium features
- Improved recommendations
