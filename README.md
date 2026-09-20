# Vocadesk - AI Voice Launcher

Control your computer with your voice! VOcadesk is an **offline voice recognition application** that lets you launch applications and perform web searches using voice commands.

**Optimized for Indian English accent!**

## Important: Model Setup Required
- 🎨 **Beautiful GUI** - Swing-based graphical interface with visual feedback
- 🚀 **Fast & Lightweight** - Launches applications instantly
- 📝 **JSON Configuration** - Easy to add/remove applications
- 🔧 **Modular Design** - Clean separation of concerns

## 📁 Project Structure

```
VOcadesk/
├── pom.xml                                    # Maven configuration
├── apps.json                                  # Application registry (MUST be in project root)
├── README.md                                  # This file
├── GUI_GUIDE.md                               # GUI usage guide
├── ECLIPSE_SETUP.md                           # Eclipse setup instructions
└── src/
    └── main/
        └── java/
            └── com/
                └── vocadesk/
                    ├── VOcadeskMain.java              # Main entry point (GUI)
                    ├── VocadeskGUI.java               # Swing GUI interface
                    ├── VoiceRecognitionController.java # Voice controller for GUI
                    ├── AppLauncher.java               # App manager
                    └── VoiceLauncher.java             # Console version (original)
```

## 🛠️ Setup Instructions for Eclipse

### Step 1: Import Project into Eclipse
{{ ... }}

1. Open Eclipse IDE
2. Go to **File → Import → Maven → Existing Maven Projects**
3. Browse to `C:\Users\praka\eclipse-workspace\VOcadesk`
4. Click **Finish**

### Step 2: Download Dependencies

Eclipse will automatically download Maven dependencies. If not:

1. Right-click on project → **Maven → Update Project**
2. Check **Force Update of Snapshots/Releases**
3. Click **OK**

**Dependencies that will be downloaded:**
- `sphinx4-core` - CMUSphinx speech recognition engine
- `sphinx4-data` - Acoustic models, dictionary, and language models (all offline)
- `json` - JSON parsing library
- `slf4j-simple` - Logging

### Step 3: Verify apps.json Location

**CRITICAL:** The `apps.json` file MUST be in the project root folder:
```
C:\Users\praka\eclipse-workspace\VOcadesk\apps.json
```

If it's not there, the application will fail to load apps.

### Step 4: Configure Your Microphone

1. Make sure your microphone is connected and working
2. Go to **Windows Settings → Privacy → Microphone**
3. Enable microphone access for desktop apps

### Step 5: Run the Application

**Option A: Run GUI Version (Recommended)**
1. Open `VOcadeskMain.java`
2. Right-click → **Run As → Java Application**
3. The GUI window will appear
4. Click "Start Voice Recognition" button

**Option B: Run Console Version**
1. Open `VoiceLauncher.java`
2. Right-click → **Run As → Java Application**
3. Voice recognition starts automatically

**Option C: Run from Command Line**
```bash
cd C:\Users\praka\eclipse-workspace\VOcadesk
mvn clean package
java -jar target/vocadesk-1.0.0.jar
```

**For detailed GUI instructions, see [GUI_GUIDE.md](GUI_GUIDE.md)**

## 🎤 Voice Commands

### Launch Applications
- **"open calculator"** → Opens Calculator
- **"open notepad"** → Opens Notepad
- **"open paint"** → Opens Paint
- **"launch calculator"** → Alternative command
- **"start notepad"** → Alternative command

### System Commands
- **"list apps"** → Shows all available applications
- **"show apps"** → Shows all available applications
- **"help"** → Shows available commands
- **"exit"** → Closes VOcadesk
- **"quit"** → Closes VOcadesk

## 📝 Adding Your Own Applications

Edit `apps.json` in the project root:

```json
[
  {
    "name": "Calculator",
    "path": "C:/Windows/System32/calc.exe"
  },
  {
    "name": "Your App Name",
    "path": "C:/Path/To/Your/Application.exe"
  }
]
```

**Important Notes:**
- Use forward slashes `/` or double backslashes `\\` in paths
- The `name` field is case-insensitive when using voice commands
- Make sure the path is correct and the executable exists

### Example: Adding Chrome

```json
{
  "name": "Chrome",
  "path": "C:/Program Files/Google/Chrome/Application/chrome.exe"
}
```

Then say: **"open chrome"**

### Example: Adding Visual Studio Code

```json
{
  "name": "VS Code",
  "path": "C:/Users/YourUsername/AppData/Local/Programs/Microsoft VS Code/Code.exe"
}
```

Then say: **"open vs code"**

## 🔧 Troubleshooting

### Problem: "Failed to initialize speech recognizer"

**Solution:**
- Make sure Maven dependencies are downloaded
- Right-click project → **Maven → Update Project**
- Check that `sphinx4-core` and `sphinx4-data` are in Maven Dependencies

### Problem: "Failed to read apps.json"

**Solution:**
- Verify `apps.json` is in the project root: `C:\Users\praka\eclipse-workspace\VOcadesk\apps.json`
- Check JSON syntax is valid (use a JSON validator)
- Make sure file is not corrupted

### Problem: "Application not found in apps.json"

**Solution:**
- Check spelling in `apps.json` matches your voice command
- Names are case-insensitive, so "Calculator" = "calculator"
- Say "list apps" to see all available applications

### Problem: "Failed to launch [app]"

**Solution:**
- Verify the path in `apps.json` is correct
- Make sure the executable exists at that location
- Try running the executable manually first

### Problem: Microphone not working

**Solution:**
- Check Windows microphone settings
- Make sure microphone is not muted
- Test microphone in Windows Voice Recorder
- Grant microphone permissions to Java applications

### Problem: Voice recognition is inaccurate

**Solution:**
- Speak clearly and at a normal pace
- Reduce background noise
- Use simple commands like "open calculator"
- CMUSphinx works best with clear, distinct words

## 📚 Technical Details

### CMUSphinx Models (All Offline)

The application uses built-in models from `sphinx4-data`:

- **Acoustic Model:** `en-us` (American English)
- **Dictionary:** `cmudict-en-us.dict` (Carnegie Mellon University Pronouncing Dictionary)
- **Language Model:** `en-us.lm.bin` (English language model)

All models are packaged in the JAR and require **NO internet connection**.

### How It Works

1. **VoiceLauncher** initializes CMUSphinx with offline models
2. Microphone input is captured continuously
3. Speech is recognized using the acoustic and language models
4. Recognized text is parsed for commands
5. **AppLauncher** loads apps from `apps.json`
6. Matching applications are launched using `ProcessBuilder`

### Class Responsibilities

**AppLauncher.java:**
- Loads applications from JSON file
- Maintains app name → path mapping
- Launches applications using ProcessBuilder
- Provides app registry queries

**VoiceLauncher.java:**
- Initializes CMUSphinx speech recognizer
- Listens for voice commands continuously
- Processes recognized speech
- Triggers app launches via AppLauncher
- Handles system commands (exit, help, list)

## 🚀 Building Executable JAR

To create a standalone executable JAR:

```bash
cd C:\Users\praka\eclipse-workspace\VOcadesk
mvn clean package
```

The JAR will be created at: `target/vocadesk-1.0.0.jar`

**To run the JAR:**
```bash
java -jar target/vocadesk-1.0.0.jar
```

**Important:** Make sure `apps.json` is in the same directory as the JAR file!

## 📋 System Requirements

- **Java:** JDK 17 or higher
- **OS:** Windows (tested on Windows 10/11)
- **RAM:** 256 MB minimum
- **Microphone:** Any working microphone
- **Internet:** NOT required (fully offline)

## 🎓 Learning Resources

### CMUSphinx Documentation
- Official Site: https://cmusphinx.github.io/
- Java Tutorial: https://cmusphinx.github.io/wiki/tutorialsphinx4/

### JSON Format
- JSON Validator: https://jsonlint.com/

## 📄 License

This project is free to use and modify for personal and educational purposes.

## 🤝 Contributing

Feel free to:
- Add more applications to `apps.json`
- Improve voice command parsing
- Add new features
- Report bugs

## 📞 Support

If you encounter issues:
1. Check the Troubleshooting section above
2. Verify all setup steps are completed
3. Check Eclipse console for error messages
4. Ensure `apps.json` is valid JSON

---

**Enjoy using Vocadesk! 🎉**

Say "open calculator" to get started!
