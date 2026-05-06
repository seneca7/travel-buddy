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

---

## Implementation Status (snapshot 2026-05-04)

Honest read of what exists in the codebase today vs. what the product spec above describes. Use this to brief reviewers and keep README claims aligned with reality.

### What is real
- **Build & toolchain**: Kotlin 2.0.21, AGP 8.5.2, Compose Compiler Gradle plugin, JDK 17, `compileSdk 34`, `minSdk 26`, Compose BOM `2024.09.00`, Material3, Navigation Compose 2.8.2, lifecycle-viewmodel-compose 2.8.6.
- **Navigation shell**: bottom-bar tabs (Home, Matches, Chat, Safety) plus stack routes for Profile, Join Request, and two Safety placeholder screens. Routes centralized in `navigation/AppRoute.kt`.
- **Trip publish loop**: `TravelViewModel.publishTrip()` validates `TripDraft` (destination + ISO dates), then drives Home headline + readiness via the pure functions in `trips/TripFormatting.kt` (parse / `headlinePreview` / `readiness01`).
- **Matches feed**: 3 hardcoded `MatchProfile` rows displayed as cards.
- **Join request → chat bridge**: `rememberJoinSent()` stashes a `PendingChatPreview` so the next visit to Chat shows a fake echo thread.
- **Safety center**: principles card, SOS card, two placeholder sub-screens (block list, report intake).
- **CI**: `Build Debug APK` workflow assembles `app-debug.apk`, uploads gradle log + APK as artifacts, optionally publishes to Appetize.io when `APPETIZE_API_TOKEN` (and optionally `APPETIZE_PUBLIC_KEY`) are set as repo secrets.

### What is fake or missing
- **No persistence anywhere.** Every state lives in `TravelViewModel` and dies with the process. Trip publish, join requests, and chat input are all in-memory.
- **No backend / no Firebase.** The "Tech Stack" lists Firebase Auth + Firestore + FCM; none of it is integrated. No `google-services.json`, no auth screens, no Firestore wiring, no FCM.
- **No real matching scorer.** `MatchProfile.score` is a precomputed integer baked into sample data. The spec weights (40 / 25 / 15 / 10 / 5 / 5) are not implemented as a function.
- **Domain model is 3 classes (`MatchProfile`, `PendingChatPreview`, `TripDraft`)** — spec lists 13 entities. `User`, `Profile`, `Trip`, `JoinRequest`, `Match`, `ChatThread`, `Message`, `Notification`, `Badge`, `UserBadge`, `Quest`, `UserProgress`, `Report` do not exist as types yet.
- **No tests.** No `app/src/test/` or `app/src/androidTest/`. CI never runs `./gradlew test`.
- **No Onboarding / Signup / Login / Profile Setup / Settings screens.** Listed in MVP Features and Key Screens; not present.
- **Notifications screen is unreachable.** `AppRoute.Notifications` route is registered but no UI navigates to it (no tab, no link). Dead code.
- **Safety report + block list are static placeholder screens** — no intake form, no moderator queue, no block-list state.
- **Chat is a fake local echo.** Sends append a `ChatTurn` to a `mutableStateListOf` that is rebuilt whenever the preview changes. No realtime, no persistence, no peer.
- **Date inputs are free-text strings** with manual ISO parsing. No `DatePickerDialog`.
- **All UI strings inlined in Compose code.** Only `app_name` is in `strings.xml`. i18n / language-overlap-as-feature is not even hookable.
- **Single ~865-line file `ui/TravelBuddyScreens.kt`** holds every screen + helpers. Will not scale past Sprint A.
- **No DI** (Hilt / Koin). Manual `viewModel()` only. Will hurt as soon as repositories arrive.
- **No release signing config**, no Play Console pipeline, no internal-track flavor.
- **No `LICENSE`, `CONTRIBUTING.md`, issue / PR templates** in the repo.

### Risks
- **Backend decision is still "Firestore or Supabase" — left unmade.** Every week this slips, more screens get written against in-memory state and will need rewrites.
- **Auth is a hard prerequisite for matches, profiles, chat, reports, badges.** The longer the prototype runs without it, the more sample-data scaffolding accumulates.
- **The matching scorer is the product.** Shipping screens before a tested scorer means the only differentiator is unverified.
- **README claims "matching" and "join request flow" as implemented** — true UI-wise, false data-wise. Demo viewers may overestimate maturity.

---

## Next Steps — prioritized

Ordered so each sprint unblocks the next. Sprint A is the smallest unit that lets every later sprint move without rewrites.

### Sprint A — foundations (do first)
1. **Decide Firestore vs. Supabase.** Recommendation: **Firestore** — FCM is already in scope, Firebase Auth is the cheapest path to email + Google sign-in, and Compose / KMP samples lean Firebase. Document the decision and stop revisiting it.
2. **Introduce a `Repository` interface + `InMemoryRepository` impl.** Move `matches` / `tripDraft` / `pendingChatPreview` off the ViewModel onto the repo. Keep the in-memory impl for previews + tests; the Firestore impl ships in Sprint B.
3. **Build the matching scorer as a pure function.** New package `matching/`, new file `Scorer.kt`, signature roughly `fun score(viewer: Profile, viewerTrip: Trip, candidate: Profile, candidateTrip: Trip): MatchBreakdown`. Use the spec weights (40/25/15/10/5/5). Return a breakdown so the UI can show "why".
4. **Add `app/src/test/` with JUnit + Truth (or kotlin-test).** First tests cover the scorer: identical trips, no overlap, partial date overlap, missing fields. Wire `./gradlew test` into the CI workflow before `assembleDebug`.
5. **Split `ui/TravelBuddyScreens.kt`** into `ui/screens/{HomeScreen,CreateTripScreen,MatchesScreen,ProfileScreen,JoinRequestScreen,ChatScreen,NotificationsScreen,SafetyScreen}.kt` plus shared `ui/components/` for `SectionTitle`, `EmptyStateCard`, `TimelineDot`. Mechanical refactor; no behavior change.
6. **Flesh out the domain model** — Kotlin data classes for `User`, `Profile`, `Trip`, `JoinRequest`, `ChatThread`, `Message`, `Notification`, `Report` in `model/`. Match spec entity names exactly so Firestore collections map 1:1 later.

### Sprint B — auth + persistence
7. **Firebase Auth (email + Google).** Add Onboarding / Signup / Login screens. Gate the rest of the app behind an auth state.
8. **Profile Setup screen** — photo (Firebase Storage), bio, languages, budget, travel style, interests. Writes to `users/{uid}/profile`.
9. **`FirestoreRepository` impl behind the same `Repository` interface** introduced in Sprint A. Trips, profiles, matches read/write through it. The `InMemoryRepository` stays for previews.
10. **Real Matches feed** — read peer trips overlapping the user's trip, score with the Sprint A scorer, sort, render.
11. **Add `google-services.json` handling** (template committed, real file via CI secret or local-only — never commit prod credentials).

### Sprint C — chat + safety + notifications
12. **Firestore-backed chat** under `chats/{threadId}/messages`, with `chats/{threadId}` carrying `participants` and `unlockedAt`. Chat input stays disabled until the corresponding `join_requests` doc has `status == ACCEPTED`.
13. **FCM** for join-request events, accepts, and match-momentum reminders. Tokens stored on `users/{uid}.fcmTokens`.
14. **Wire the Notifications tab.** Either add it to the bottom bar or surface via a top-bar bell — it currently has zero entry points.
15. **Real Safety toolkit:** report intake form (writes to `reports/`), block list backed by `users/{uid}.blocked`, emergency-contact opt-in stored on the profile, meetup-confirmation field on `chats`.

### Sprint D — gamification + polish
16. **Replace single readiness float** with sub-goal checklist (profile completeness, dates set, visa noted, packing items). Real readiness rolls up from sub-goals.
17. **Badges + city stamps**: `badges/` master list, `user_badges/` per-user. Award on first publish, first accepted match, first trip completed, etc. Post-trip recap card built from `user_badges` deltas.
18. **Quest system** thin slice: 2-3 quests max, server-defined.

### Cross-cutting (do alongside Sprint A/B as cheap wins)
- **DatePickerDialog** in `CreateTripScreen` — replace free-text dates.
- **Move all strings to `strings.xml`.** Required for the "language overlap" feature to mean anything.
- **Add `LICENSE` (MIT or Apache-2.0), `CONTRIBUTING.md`, GitHub issue + PR templates.**
- **Add a `release` signing config** (CI keystore via secret) once Sprint B closes — needed for closed beta on Play Console internal track.
- **DI**: introduce Hilt at the start of Sprint B, before Firebase repos arrive. Adding it later costs more.
- **Accessibility pass**: content descriptions on Match cards / chat bubbles, semantic roles on tap targets, large-text smoke test.
- **README correction**: state plainly that Phase 1 UI is done but matching + join request are still sample data. Avoid overclaiming until Sprint B lands.

### Definition of "Phase 1 done" (revised)
Phase 1 is complete when **all** of these are true:
- Real auth (Sprint B item 7).
- Profile Setup writes to Firestore (Sprint B item 8).
- Trip publish writes to Firestore (Sprint B item 9).
- Matches list is computed by the Sprint A scorer over real Firestore data (Sprint B item 10).
- Join request writes a `join_requests` doc and Chat unlocks only after accept (Sprint C item 12).
- Scorer has unit tests covering all six dimensions (Sprint A item 4).

Until then, calling Phase 1 "done" is misleading.

---

## Web prototype — now branded as Mappal (snapshot 2026-05-05)

**Brand:** the web prototype has been rebranded to **Mappal** (tagline: *"Map your trip. Find your pal."*). The Android codebase still uses "Travel Buddy" — rebrand will happen at the same time as the Capacitor decision (one repo wins). Domain: [mappal.app](https://mappal.app). GitHub: [mikeLackovcan/travel-buddy-web](https://github.com/mikeLackovcan/travel-buddy-web).

**Naming research summary** — the travel-buddy namespace is brutally saturated. Killed candidates: Roamie (5+ existing brands), Trippa (`trippa.io` does identical product), Tripsy (established Apple-only planner), Tomo (Web3 social wallet), Buddi/TravelPal (6+ direct competitors), Tagalong, Sameway, Pathmate, Voyy, Trippl, Overlap (literally an acquired competitor), Pacto (Mexican fintech, $34M), Folk (CRM). Mappal survived because the only existing presence is a defunct 2016 hackathon project; `.com` is premium ($500-3k via HugeDomains) but `.app` is free at standard rate.



A Next.js 16 + TypeScript + Tailwind v4 prototype lives alongside the Android app at `C:/Perplexity/travel-buddy-web/` (separate codebase, separate GitHub repo). Built to (a) test product direction in a browser without sideloading APKs, (b) carry the same matching scorer + sample data so design decisions transfer 1:1 to Android, (c) double as a marketing/landing surface for the same product.

### Stack
- **Next.js 16.2 (App Router) + React 19**, TypeScript strict, Tailwind v4 with custom theme tokens (terracotta + sand + accent green).
- **Framer Motion** for hero magnetic heading, scroll-reveals, animated counters, ticker, hover lifts.
- **Inter (UI) + Fraunces (display)** via `next/font/google`.
- **lucide-react 1.x** icons + inline brand SVGs for Apple/Google/Instagram (lucide v1 dropped branded icons for trademark reasons).
- **No backend yet.** Same shape as Android: pure-TS scorer + in-memory React-Context store; Firebase wires in Sprint B for both.

### Routes
**Marketing (root layout, full-bleed):**
- `/` — landing page, AIDA structure (Hero → Bento Grid features → Counters + testimonials + press → CTA section + Footer).
- `/signup` — registration screen with three social-OAuth buttons (Continue with Google / Apple / Instagram) + Sign up with email. Mock for now: each button shows a 700 ms "Connecting…" spinner then routes to `/app`.

**App (separate `(/app)` layout: phone-frame + bottom nav, mobile-first):**
- `/app` — Home cockpit: destination hero photo, countdown, adaptive CTA, 4-chip readiness, photo carousel of fellow travelers in the user's city.
- `/app/create` — 3-step trip composer (destination picker with 12 city tiles, date range, vibe chips).
- `/app/matches` — bucketed feed (Strong fits / Worth a look) with photo cards + narrative compatibility ("In town Jul 12–17 — both into food walks, museum mornings"); filter chips for exact / ±3 days / other cities.
- `/app/profile/[id]` — hero photo, verification badges, 6-dimension compatibility bars, their trip card, sticky "Say hi" CTA.
- `/app/join/[id]` — 2-step join request (proposal type → personalize message + meetup suggestion).
- `/app/chat` and `/app/chat/[id]` — locked-until-accepted thread with simulated peer accept; tools row (location / propose meetup / unsafe) appears post-accept.
- `/app/plan` — per-day itinerary, morning/afternoon/evening slots, vibe-tagged activity chips.
- `/app/safety` — status-driven cards (verified / emergency contact / itinerary shared) + SOS + tools + principles.
- `/app/notifications` — earned bell, lists join requests.

### Real underneath
- **Pure TS matching scorer** at `src/lib/scorer.ts` using the spec weights (40/25/15/10/5/5). Returns a full breakdown so the UI renders the "why."
- **12 sample travelers** with `randomuser.me` portraits and 12 sample trips across 5 destinations (4 in Lisbon overlapping the demo user's dates, etc.). Destination hero photos via Unsplash.
- **React Context store** (`src/lib/store.tsx`) for trip draft, published trip, join requests, chat threads, itineraries.

### Design moves applied (from earlier UX critique)
- Cockpit-style Home (replaced list-style)
- Photo-first match cards with narrative compatibility (replaced numeric "Score 87")
- Visual destination picker with city tiles (replaced free-text destination input)
- Status-driven Safety cards (replaced principles wall)
- Bell-in-top-bar Notifications (Notifications no longer a dead route)
- Magnetic headline + Bento grid + AIDA flow on landing
- Three-button OAuth signup: Google / Apple / Instagram (the latter targets the 20–30 cohort that uses IG as primary identity)

### Still missing on web (mirror of Android gaps)
- No real auth wiring — OAuth buttons are visual-only.
- No persistence beyond in-memory React state.
- No tests yet (same Sprint A item).
- No deployment — runs `next dev` locally; Vercel deploy is one-click after first `gh repo create`.

### How web and Android stay in sync
The web app is a *design + scorer* prototype, not a separate product. As decisions land:
- Scorer changes → port both directions (TS ↔ Kotlin) until the Sprint A scorer ships in Kotlin tests.
- Firestore schema decisions (Sprint B) apply to both clients identically.
- Sample-data shape (`Profile` / `Trip` / `JoinRequest`) is the contract both will read from Firestore once auth lands.

If web traction is stronger after first user tests, candidate path is to wrap web with **Capacitor** for App Store / Play Store distribution and retire the Android Compose codebase. Decision deferred until at least one round of user feedback on each.
