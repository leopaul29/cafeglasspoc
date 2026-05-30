# Rokid Barista POC
> AR-assisted recipe guidance for baristas — displayed directly on Rokid smart glasses.

A native Android app that turns Rokid AR glasses into a hands-free recipe assistant. The barista sees step-by-step instructions overlaid in their field of view while keeping both hands free to work. Steps advance with a single tap on the glasses touchpad.

## Demo

_[Add link to demo video here]_

## How It Works

1. The app launches and immediately displays the recipe on screen (mirrored to the glasses via USB-C)
2. The barista sees the active step highlighted in **cyan**, inactive steps dimmed in magenta
3. A tap anywhere on the glasses touchpad (or phone screen) advances to the next step
4. The RESET button returns to step 1

The black background renders as fully transparent on the Rokid glasses, making the text appear floating in the real world.

## Architecture

```
Android Activity (MainActivity)
│
├── Renders recipe steps directly on activity_main.xml
├── Black background → transparent on Rokid AR display
├── Tap/KeyEvent listener → advances currentStep
└── USB-C mirror → glasses display (DisplayPort Alt Mode)
```

No backend. No cloud. No dependencies beyond the Android SDK.

## Getting Started

**Prerequisites**
- Android Studio (Hedgehog or later)
- Android phone with USB-C DisplayPort Alt Mode support
- Rokid AR glasses (tested on YodaOS-Sprite)
- ADB enabled on the phone

**Run**
```bash
# Clone the repo
git clone https://github.com/[your-username]/rokid-barista-poc

# Open in Android Studio, then:
./gradlew installDebug

# Or via ADB directly:
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.cafeglasspoc/.MainActivity
```

**Connect the glasses**
1. Plug Rokid glasses into phone via USB-C
2. The glasses will mirror the phone screen automatically
3. Launch the app — the recipe appears instantly

## Recipe Loaded (POC)

**Chai Milk Tea — Size S**

| Step | Action |
|------|--------|
| 1 | 1/3 Royal Tea sachet in hot water — 50 sec |
| 2 | Chai powder: 5 taps |
| 3 | 150 ml hot milk |

## Built With

- Android (Kotlin) — native, no framework overhead
- `KeyEvent` / `OnClickListener` — glasses touchpad input
- DisplayPort Alt Mode over USB-C — zero-config screen mirroring
- AR color palette: Cyan `#00FFCC` on Black `#000000`

## Why It Works on AR Glasses

Rokid glasses render pure black (`#000000`) as transparent. By building the entire UI on a black background with high-contrast colored text, the recipe steps appear to float in the barista's field of view with no additional AR SDK required.

## License

MIT