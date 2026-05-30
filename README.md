# rokiglassescoffee-03052026

## Useful Links
* https://github.com/eikachiu/rokid-glass-skill/

## Project Structure
app/
├── src/main/
│   ├── java/com/hackathon/rokidpoc/
│   │   ├── MainActivity.kt
│   │   └── GlassPresentation.kt
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml
│   │   │   └── presentation_glass.xml
│   │   └── values/
│   │       └── colors.xml
│   └── AndroidManifest.xml

# Build & install in one shot
./gradlew installDebug

# Launch the activity
adb shell am start -n com.hackathon.rokidpoc/.MainActivity

# If glasses aren't detected automatically, force presentation display
adb shell settings put global overlay_display_devices 1280x720/160
# (simulates a second screen for testing without the glasses)