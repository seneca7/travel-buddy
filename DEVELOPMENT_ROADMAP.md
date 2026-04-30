# Development Roadmap

## Done

- App shell, navigation, sample-data prototype flows.
- CI pipeline: `Build Debug APK` workflow produces a downloadable APK on every push and (optionally) publishes a browser-playable demo to Appetize.io. See [README.md](README.md) for setup.

## Next

1. Lock down the data model: Kotlin classes for `User`, `Profile`, `Trip`, `JoinRequest`, `Match`, `ChatThread`, `Message` in `model/`. Decide Firestore vs Supabase before more screens land.
2. Wire Firebase Auth + Firestore behind a `Repository` interface (with a fake in-memory impl so screens stay testable).
3. Build Profile Setup — gating screen for the matching scorer (photo, bio, languages, budget, travel style, interests).
4. Implement the matching scorer as a pure function: destination 40 / dates 25 / style 15 / budget 10 / language 5 / interests 5. Unit-test it.
5. Real join-request flow: `join_requests` collection, accept/decline, chat unlocks only after acceptance.
6. Replace the chat stub with Firestore-backed messages + FCM for match-momentum notifications.
7. Safety toolkit backend: report intake, block list, emergency contact, meetup confirmation.
8. Ethical gamification: trip-readiness sub-goals, badges, city stamps, post-trip recap card (peak-end driver of word-of-mouth).
9. QA + closed beta via the Appetize URL and Play Console internal track.

## Phased view (matches spec)

### Phase 1 — App shell, profile + trip creation, matching, join request flow.
### Phase 2 — Chat, safety tools, notifications, basic gamification.
### Phase 3 — Group trips, reputation signals (not star ratings), premium features, improved recommendations.
