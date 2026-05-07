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
- **CI**: `Build Debug APK` workflow runs `testDebugUnitTest` first (fail-fast), then assembles `app-debug.apk`, uploads gradle log + test reports + APK as artifacts, optionally publishes to Appetize.io when `APPETIZE_API_TOKEN` (and optionally `APPETIZE_PUBLIC_KEY`) are set as repo secrets.
- **Matching scorer** (`matching/Scorer.kt`): pure-function port of the web prototype's `src/lib/scorer.ts`. Same six dimensions (40 / 25 / 15 / 10 / 5 / 5), same `bucketFor` helper. Both clients now rank an identical Firestore corpus identically.
- **Domain model — first slice** (`model/`): `Vibe`, `Budget`, `Profile`, `Trip` data classes. Field names match the web prototype's `types.ts` for 1:1 Firestore parity (Sprint B).
- **Unit tests**: `ScorerTest.kt` (14 cases) covers all six scoring dimensions, edge cases (zero overlap, empty vibes, language cap), and bucket boundaries (75/3, 55, !sameDestination). Run by CI via `./gradlew testDebugUnitTest`.

### What is fake or missing
- **No persistence anywhere.** Every state lives in `TravelViewModel` and dies with the process. Trip publish, join requests, and chat input are all in-memory.
- **No backend / no Firebase.** The "Tech Stack" lists Firebase Auth + Firestore + FCM; none of it is integrated. No `google-services.json`, no auth screens, no Firestore wiring, no FCM.
- **Scorer not yet wired into the UI.** `Scorer.score(...)` exists and is tested, but the Matches feed still renders three hardcoded `MatchProfile` rows. The wiring lands when the repository (Sprint A item 2) replaces sample data.
- **Domain model still 5 of 13 entities.** Done: `Profile`, `Trip`, `JoinRequest`, `Vibe` (enum support type), `Budget` (enum support type). Still missing: `User`, `Match`, `ChatThread`, `Message`, `Notification`, `Badge`, `UserBadge`, `Quest`, `UserProgress`, `Report`. Legacy UI DTOs (`MatchProfile`, `PendingChatPreview`, `TripDraft`) coexist and will be retired or repurposed when Sprint B replaces sample data. `JoinRequest` mirrors the web prototype's TS type 1:1 (proposal/status enums, optional activityId/Label, sentAt as epoch millis) for Firestore parity.
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
3. ✅ **Build the matching scorer as a pure function.** Done in `matching/Scorer.kt`. Pure-function port of the web prototype's `src/lib/scorer.ts`; same weights (40/25/15/10/5/5), same `bucketFor` helper. Returns a `ScoreBreakdown` so the UI can show "why."
4. ✅ **Add `app/src/test/` with JUnit.** Done. `ScorerTest.kt` has 14 cases covering all six dimensions + edge cases + bucket boundaries. Wired into CI as a `testDebugUnitTest` step that runs before `assembleDebug` (fail-fast).
5. **Split `ui/TravelBuddyScreens.kt`** into `ui/screens/{HomeScreen,CreateTripScreen,MatchesScreen,ProfileScreen,JoinRequestScreen,ChatScreen,NotificationsScreen,SafetyScreen}.kt` plus shared `ui/components/` for `SectionTitle`, `EmptyStateCard`, `TimelineDot`. Mechanical refactor; no behavior change.
6. ⏳ **Flesh out the domain model** — Kotlin data classes for `User`, `Profile`, `Trip`, `JoinRequest`, `ChatThread`, `Message`, `Notification`, `Report` in `model/`. Match spec entity names exactly so Firestore collections map 1:1 later. **Status:** `Profile`, `Trip`, `Vibe`, `Budget` done (needed by the Sprint A scorer). `User`, `JoinRequest`, `ChatThread`, `Message`, `Notification`, `Report` still pending.

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

## Web prototype — competitive positioning + open-plans on map (snapshot 2026-05-07 late)

**Strategic update against Overlap (getoverlap.app) — the closest competitor**, acquired by Pangea NYC. Overlap is built on a **friend-graph** premise: it tells you when *your existing connections* overlap your trip. That's its strength (warm community) and its structural weakness (cold-start fails, leisure-traveler audience underserved).

**Mappal positioning sharpened to anti-Overlap**: "Plan your trip. Open one day. Find your crew." Cold-start friendly. No friends required. Activity-level matching, not trip-level. Same backend value-prop, completely different audience target — leisure travelers (vacationers, weekenders, solo women, families) are 20× the nomad audience Overlap serves.

### Landing copy rewrites
- **Hero headline**: `Travel like you have friends in every city` → `Plan your trip. Open one day. Find your crew.` Three concrete actions matching the actual app loop, not a vague aspiration.
- **Hero subhead** explicitly addresses the cold-start objection: *"No friends on the app yet? Doesn't matter."*
- **BentoGrid section header**: `Not another dating-style swipe app.` → `Not a swipe app. Not a friends-only network.` Two-axis differentiation.
- **First Bento cell** repositioned from generic "Trip-first matching" to **"No friend-graph required"** with cold-start callout.
- **"Plans, not vibes" cell** rewritten as **"Open one day, or all of them"** — moved the activity-level open-to-buddy mechanic to the visual front of the marketing argument. Demo chips show OPEN vs. private states.
- **Anti-pattern card** added "Friend-graph required" to the strikethrough list.
- **CTA section** bullet list changed from generic onboarding promises to four positioning beats: works for solo/weekend/full vacation, zero friends needed, activity-level matching, mutual consent.

### Open activities pinned on the map (deeper moat)
- `TravelersMap` accepts `mode: "travelers" | "plans" | "both"`.
- New marker style: terracotta drop-pin with vibe emoji centered. Clicking shows activity title + date + slot + time + place + owner avatar + **"Ask to join"** CTA that deep-links to `/app/join/<peer>?activityId=<id>` (skips proposal step, pre-fills meetup with activity location).
- `/app/map` now has 3-state toggle: Both / Open plans / Travelers. Default is Both.
- Header reads `N travelers · M open plans` for the user's city.
- Hash-based scatter so multiple activities in the same destination spread visually instead of stacking.
- This is the feature Overlap can't easily ship — they'd have to rebuild their plan model (currently free-text trips) into structured open-to-buddy activities.

### Sprint 4 still pending (called out for next push)
- "I met them" reciprocal confirmation → reputation points
- Onboarding wizard for first-time users
- Profile setup screen (bio/vibes/budget)
- Local-meets-traveler supply layer ("locals open up to show their city")
- Hostel partnership integration

---

## Web prototype — Mappal Sprint 1 + 2 + Map (snapshot 2026-05-07 evening)

**New since this morning** — applied the full UX re-architecture from the deep analysis:

### Plan as a social object (Sprint 1)
- **`ItineraryActivity` extended**: optional `startTime`, `location`, `note`, plus the social signal **`openToBuddy`** (default off; opt-in when planning) and `maxBuddies` cap. `buddies[]` array tracks confirmed companions once a request is accepted.
- **`ActivitySheet` bottom-sheet modal** ([src/components/ActivitySheet.tsx](https://github.com/mikeLackovcan/travel-buddy-web/blob/main/src/components/ActivitySheet.tsx)): full custom activity input with quick-pick suggestions on top, free-text title, slot picker, time, place, vibe chips, note, and the open-to-buddy toggle with cap (1 / 2 / 3 / 5 / any). Used for both add and edit; framer-motion spring slide-up.
- **Plan screen rewritten** to use the sheet — every slot has its own "+ Add activity" button; tapping any activity reopens the sheet in edit mode. Header now reports `N planned · M open to buddies`.
- **`SAMPLE_ITINERARIES`** seeded for sample travelers (Sara, Marek, Yuna in Lisbon) with a mix of open and private activities — gives Profile and Map something real to show.

### Two-sided social loop (Sprint 2)
- **`/app/inbox`**: the *receiving* side that didn't exist before. Lists pending incoming requests with traveler photo + verified badge + the specific activity they want to join (resolved from peer's plan), the message, and the suggested meetup. Accept / Decline buttons. On Accept: status flips, the proposed activity drops into the user's own itinerary (auto-add via store reducer), and a system "Accepted" message lands in the chat thread.
- **Profile shows "Sara's open plans · 3"** section — tappable cards that deep-link into the Join flow with `?activityId=…` so the request carries the activity reference.
- **Join flow updated**: when arriving with `?activityId=…`, step 0 (proposal type) is skipped, the proposed activity is rendered in a primary-soft preview card, and the JoinRequest stores `activityId` + `activityLabel` snapshot.
- **Match cards** now show `N open` accent badge so discovery surfaces "people with open plans" prominently.
- **Store extended**: separate `incoming[]` from `joinRequests[]`, new `acceptIncoming` and `declineIncoming` actions, auto-add behavior on accept. One demo incoming request seeded so the Inbox is never empty on first run.

### Map view (Sprint 3 brand-aligned)
- The product is named **Mappal** — having no map was on-brand failure. Fixed:
- **Leaflet + `react-leaflet` + OpenStreetMap tiles** (free, no API key).
- **`/app/map`**: city-focused map centered on the user's destination with avatar markers for all sample travelers. The user's own location ringed in primary terracotta; others ringed in accent green. Tap a marker → popup with name, dates, and "See profile" link. Bottom-sheet horizontal carousel of in-city travelers below the map.
- **Lat/lng added** to all 12 destinations in `sample-data.ts`.
- **`<MapContainer>` dynamically imported with `ssr: false`** (Leaflet uses `window`).
- **Bottom nav re-organized**: `Home / Map / Plan / Chat / Safety` — *Matches* removed from primary nav (still reachable as "See all" from Home; the Map and Profile-level open-plans cover discovery better).
- **TopBar "bell" → Inbox** — repurposed the icon to point at `/app/inbox` with a count badge of pending requests.

### Deferred to Sprint 4 (call out in roadmap)
- Real-time location sharing during meetups
- "I met them" reciprocal confirmation → reputation signals
- Today feed combining map + open activities (the map currently does this job)
- Onboarding wizard for first-time users
- Profile setup screen (bio, vibes, languages, budget) — currently implicit from Google sign-in



**New since 2026-05-05:**
- **Auth.js v5 (`next-auth@5.0.0-beta.31`) wired** with **Google + Facebook + Instagram** OAuth providers, all env-gated. Facebook (added 2026-05-07) uses the standard Auth.js Facebook provider — easiest of the Meta-family providers because it doesn't depend on the deprecated Basic Display API. Same Meta Developer App can host Facebook Login + Instagram Login products simultaneously. Google works once `AUTH_GOOGLE_ID/SECRET` + `NEXT_PUBLIC_GOOGLE_AUTH_ENABLED=true` set; Instagram works once `AUTH_INSTAGRAM_ID/SECRET` + `NEXT_PUBLIC_INSTAGRAM_AUTH_ENABLED=true` set. Without env vars both buttons stay mocked and show a small "demo" badge.
- **Apple sign-in dropped** from scope ($99/yr Apple Developer subscription not justified at prototype stage).
- **Instagram caveat:** Auth.js's built-in Instagram provider hits the legacy Basic Display API which Meta deprecated late 2024. Works for testers registered in the Meta App's Roles panel today; production access requires Meta App Review (Privacy Policy URL, Data Deletion endpoint, screencast walkthrough — 2-4 weeks). May need migration to custom Meta Login for Business OAuth flow once Basic Display is fully off.
- **WhatsApp OTP** still candidate for v2 (phone-OTP via Twilio Verify, ~$0.005-0.05 per code) — not yet implemented; user requested wiring it after Google + Instagram are confirmed working.
- **Google Analytics 4** loaded conditionally via `@next/third-parties/google` when `NEXT_PUBLIC_GA_MEASUREMENT_ID` is set.
- **TopBar `AccountMenu`** — shows avatar + name + email dropdown with Sign out when session present; small "Sign in" link otherwise.
- **API routes** — `/api/auth/[...nextauth]` exports `handlers` from `src/auth.ts`. `/api/auth/{providers,csrf,session}` all return 200.
- **Dev fallback** — `auth.ts` uses a `DEV_SECRET` constant when `AUTH_SECRET` isn't set so the prototype boots without env vars; throws in production if missing.
- **`.env.example`** with full setup instructions for each provider.



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
- **2026-05-07** Home countdown forward-framed ("X days until departure" replaces "X days to go" — neutral framing instead of mild loss frame).
- **2026-05-07** Join request flow rewritten to fight templated openers: empty default message + structured placeholder asking for one specific shared moment (self-disclosure reciprocity, the mechanism Hinge built a moat on); meetup field now prompts for an explicit if-then plan (Gollwitzer's implementation intentions roughly double follow-through); explicit "no pressure either way" block above the send button — paradoxically raises response quality because honest declines stop being socially costly.

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
