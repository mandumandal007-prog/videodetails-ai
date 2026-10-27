# VideoDetails AI — Android App

Share sheet me dikhne wala app: Instagram / YouTube / TikTok / X par **Share → VideoDetails AI** dabao,
aur niche aadhi screen (bottom panel) me video ki details + AI summary aa jayegi.

## Features
- **Share target**: kisi bhi app se text/plain link share karo → app option list me aata hai
- **Half-screen panel**: transparent activity, neeche 78% height ka rounded bottom sheet (previous app ke upar)
- **Metadata fetch**: YouTube/TikTok/X ke liye oEmbed API, Instagram/Facebook/other ke liye OpenGraph (`og:title`, `og:image`, `og:description`) parsing
- **AI summary (optional)**: Google Gemini API (`gemini-flash-latest`, key `X-goog-api-key` header me) — key Settings me daalo, phone me hi save hoti hai
- **Hindi + English**: default Hindi, English device par English (`values-en`)
- **Dark mode** support (Compose Material 3 color schemes)
- **History**: last 20 videos locally (SharedPreferences)
- **Offline smart parsing**: platform, video ID, domain, type (Short/Reel/Video)

## Tech stack
- Kotlin 2.0.21 + Jetpack Compose (Material 3)
- OkHttp 4.12 (no Retrofit needed — 3 network calls total)
- Coil 2.7 (thumbnails)
- minSdk 26, targetSdk 35, compileSdk 35, JDK 17

## APK download (ready-made)
GitHub Actions har push par APK banata hai: **Releases → latest → `videodetails-ai.apk`**
(https://github.com/mandumandal007-prog/videodetails-ai/releases/latest).
Phone par kholo → "Install unknown apps" allow karo → Install.

Release APK ek repo secret (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`) wale keystore se sign hota hai,
isliye naye versions purane ke upar update ho jaate hain. Keystore repo me commit nahi hai.

## Build kaise karein (APK)
1. [Android Studio](https://developer.android.com/studio) (Ladybug ya naya) install karo
2. `File → Open` → `VideoDetailsAI` folder select karo
3. Gradle sync hone do (Gradle wrapper 8.9 repo me included hai)
4. Phone connect karo (USB debugging on) → `Run ▶`
5. APK banane ke liye: `Build → Build Bundle(s)/APK(s) → Build APK(s)`
   APK milega: `app/build/outputs/apk/debug/app-debug.apk`
6. Release signed APK ke liye: `Build → Generate Signed Bundle / APK → APK`

## AI summary on karna
1. https://aistudio.google.com/apikey se free Gemini API key lo
2. App kholo → upar right **Settings** → key paste → Save
3. Key `SharedPreferences` me locally rehti hai, kahin bheji nahi jaati (sirf Google ke endpoint par)

Key na daalo to bhi app kaam karta hai — bas AI summary ki jagah hint dikhega.

## Kaise test karein
1. YouTube kholo → koi video → Share → **VideoDetails AI**
2. Neeche panel khulega: thumbnail, title, channel, AI summary, Open / Copy buttons

## Customize
- Panel ki height: `ui/ShareSheet.kt` me `fillMaxHeight(0.78f)` → `0.5f` karo to theek aadha screen
- AI model: `data/AiAnalyzer.kt` me `MODEL` constant
- Colors: `ui/Theme.kt`

## Structure
```

app/src/main/java/com/videodetails/ai/
├── MainActivity.kt          # launcher + link paste + history + settings
├── (ui/Actions.kt)         # open / copy / share / clipboard helpers
├── ShareActivity.kt         # transparent share-target activity
├── data/
│   ├── VideoInfo.kt         # model + URL/platform/id parsing
│   ├── MetadataFetcher.kt   # oEmbed + OpenGraph fetch
│   ├── AiAnalyzer.kt        # Gemini summary
│   ├── HistoryStore.kt      # local history
│   └── SettingsStore.kt     # API key storage
└── ui/
├── Theme.kt
├── DetailsFlow.kt       # fetch pipeline (Flow<DetailsUiState>)
├── DetailsContent.kt    # shared details UI
├── ShareSheet.kt        # half-screen overlay
├── HomeScreen.kt
└── SettingsSheet.kt

```

## Note
Instagram/Facebook apni HTML pages ko login-wall se protect karte hain — kuch reels ki details
nahi milengi (app gracefully platform + link + open button dikha dega).

## Settings
- Gemini API key (phone me hi save)
- AI summary ki bhasha: हिंदी (default) / English
