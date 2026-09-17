# GuessRoll

GuessRoll is an Android party-game MVP for creating a room, sharing an invite, preparing photos, and playing collaborative guessing rounds.

## Features

- Create or join a room with a room code.
- Share room invites through a deep-link payload or QR code; scan QR codes with the device camera.
- Choose photos with the Android Photo Picker or prepare a filtered random selection from the local gallery.
- Upload room media to Supabase Storage and load it with time-limited signed URLs.
- Keep room, player, media, round, guess, game-session, and reaction state in Supabase; the client uses anonymous Supabase sessions.
- Play rounds, submit guesses and reactions, view results, and start a rematch.
- Store local feedback and win-streak preferences on the device.

## Architecture

The app is a single Android module. Jetpack Compose renders the UI, `GuessRollViewModel` coordinates UI state and game operations, domain packages hold game and invite rules, and data packages provide media, local-preference, and Supabase implementations.

```
app/src/main/java/com/guessroll/
├── data/       Android media, local preferences, and Supabase repositories
├── domain/     Game and invite rules
├── ui/         Compose app, navigation, screens, components, and theme
└── MainActivity.kt

supabase/migrations/  SQL schema and migration files
```

## Tech stack

- Kotlin 2.3.21
- Jetpack Compose with Material 3
- Android Gradle Plugin 9.2.0 and Gradle 9.4.1
- Android SDK: `minSdk 26`, `compileSdk 36`, `targetSdk 36`
- Supabase Kotlin: Auth, PostgREST, Realtime, and Storage
- CameraX, ML Kit barcode scanning, ZXing, Coil, and Kotlin coroutines

## Requirements

- Android Studio or a compatible Android SDK installation
- JDK 17
- An Android SDK configured by Android Studio or through `ANDROID_HOME`
- A Supabase project configured as described below

## Configuration

Copy [`local.properties.example`](local.properties.example) to `local.properties`, then add the values from your Supabase project settings:

```properties
SUPABASE_URL=https://your-project-ref.supabase.co
SUPABASE_ANON_KEY=your_publishable_or_anon_key
```

`local.properties` is intentionally ignored. Android Studio may also place `sdk.dir` in that file. The anonymous/publishable client key is distributed with the Android app by design; it is not a place for privileged credentials.

Never add a Supabase service-role key, database password, JWT signing secret, private key, keystore, or signing password to this repository or to an Android build.

## Supabase backend

Apply the SQL files in [`supabase/migrations`](supabase/migrations) to a Supabase project. The app code expects the room/game data model represented by those migrations and the `game-media` Storage bucket configured with access rules that allow the client flow. The included Supabase notes are in [`supabase/README.md`](supabase/README.md).

Anonymous sign-in must be enabled for the current client flow. Row-level security and Storage policies are part of the backend setup and should be reviewed before using a shared Supabase project.

## Run

1. Open the project in Android Studio.
2. Create your ignored `local.properties` from the example above.
3. Sync Gradle and select an Android device or emulator.
4. Run the `app` configuration.

From a terminal on Windows:

```powershell
.\gradlew.bat assembleDebug
```

## Tests and checks

The project contains JVM unit tests for game, invite, media, local-preference, and Supabase-related logic.

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

Instrumented tests require a configured Android device or emulator when they are added or run.

## Permissions and privacy

- `INTERNET` connects the app to Supabase.
- `CAMERA` is requested for QR invite scanning.
- Image-reading permissions support the gallery-based random-photo flow. The standard Android Photo Picker is also available for choosing explicit photos.

Selected photos are uploaded to the configured Supabase Storage bucket for room gameplay. The app stores feedback settings and win-streak state locally. Runtime sessions, selected media, and local configuration are not part of this repository.

## Limitations

- GuessRoll is an MVP; a working Supabase project and its policies are required for room and media flows.
- No release signing configuration or release artifact is included.
- App-link domain hosting and verification are environment-specific and are not included in this repository.

## License

No license has been selected or added automatically.
