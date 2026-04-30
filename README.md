# Travel Buddy Android Starter

Jetpack Compose MVP skeleton for the destination-based travel matching app.

Includes:

- Navigation between core MVP screens
- Sample data and stateful prototype flows
- Ethical gamification placeholders
- Firebase-ready repository structure
- CI workflow that produces a debug APK on every push

Open in Android Studio and wire in Firebase/Auth/Firestore.

## Try the app

Three paths, in order of "least setup required."

### A. Browser-playable demo via GitHub Actions + Appetize.io (no install)

1. Push to `main` or open a PR. The [Build Debug APK](.github/workflows/build-apk.yml) workflow builds `app-debug.apk` automatically.
2. To enable the browser demo: sign up at https://appetize.io, copy your API token, then add it to the repo at **Settings → Secrets and variables → Actions** as `APPETIZE_API_TOKEN`.
3. Re-run the workflow. The job logs print an Appetize URL like `https://appetize.io/app/<publicKey>` — open it in any browser to play the app on an emulated Android phone.
4. Save the printed `publicKey` as a second secret called `APPETIZE_PUBLIC_KEY` so subsequent builds update the same demo instead of creating new ones.

### B. Sideload the APK on your own Android device

1. Push to `main`. Wait for the workflow to finish.
2. Open the run in GitHub Actions, download the `travel-buddy-debug-apk` artifact.
3. On your phone, allow "Install unknown apps" for your file manager / browser, then open the APK to install.

### C. Run locally in Android Studio

1. Install [Android Studio](https://developer.android.com/studio) (bundles JDK 17 + Android SDK).
2. Open `travel-buddy-android-folder/` as a project. Let Gradle sync.
3. Run on a connected device or the built-in emulator.

## Notes

- The app is currently a UI-only skeleton with sample data — no auth, no real matches, no real chat. Deploys are useful for design review, not user testing.
- Real deployment to Google Play needs signing keys, a Play Console account, and the Firebase + auth work tracked in [DEVELOPMENT_ROADMAP.md](DEVELOPMENT_ROADMAP.md).
