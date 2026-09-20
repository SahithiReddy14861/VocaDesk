package com.vocadesk;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.json.JSONObject;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;


/**
 * VoiceRecognitionController - Manages offline speech recognition for GUI
 * Uses Vosk for fully offline speech recognition (better than CMUSphinx!)
 * Integrates with VocadeskGUI for visual feedback
 */
public class VoiceRecognitionController {
    
    private AppLauncher appLauncher;
    private VocadeskGUI gui;
    private Model model;
    private Recognizer recognizer;
    private TargetDataLine microphone;
    private volatile boolean isRunning;
    private Thread recognitionThread;
    
    // Path to Vosk model (will be downloaded separately)
    private static final String MODEL_PATH = "model";
    
    /**
     * Constructor - Initialize the voice recognition controller
     * @param appLauncher The AppLauncher instance for managing applications
     * @param gui The GUI instance for visual feedback
     */
    public VoiceRecognitionController(AppLauncher appLauncher, VocadeskGUI gui) {
        this.appLauncher = appLauncher;
        this.gui = gui;
        this.isRunning = false;
        
        // Initialize Vosk speech recognizer
        initializeSpeechRecognizer();
        
        // Update GUI with app list
        gui.updateAppList(appLauncher.getAppNames());
    }
    
    /**
     * Initialize Vosk for offline speech recognition
     * Uses local model files (fully offline)
     */
    private void initializeSpeechRecognizer() {
        try {
            gui.appendLog("[INFO] Initializing Vosk speech recognition...");
            
            // Load Vosk model from local directory
            model = new Model(MODEL_PATH);
            
            // Setup audio format (16kHz, 16-bit, mono)
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            
            // Get microphone
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            
            // Create recognizer (simple, no grammar)
            recognizer = new Recognizer(model, 16000);
            
            gui.appendLog("[SUCCESS] Speech recognition initialized");
            gui.appendLog("[INFO] Vosk ready - Speak naturally");
            gui.appendLog("[TIP] Say: 'open chrome' or 'search India in chrome'");
            
        } catch (Exception e) {
            gui.appendLog("[ERROR] Failed to initialize speech recognizer: " + e.getMessage());
            gui.appendLog("[INFO] Make sure 'model' folder exists in project root");
            gui.appendLog("[INFO] Download model from: https://alphacephei.com/vosk/models");
            throw new RuntimeException("Failed to initialize speech recognizer", e);
        }
    }
    
    /**
     * Start listening for voice commands
     * This runs in a background thread to avoid blocking the GUI
     */
    public void startListening() {
        if (isRunning) {
            gui.appendLog("[WARNING] Voice recognition is already running");
            return;
        }
        
        isRunning = true;
        
        // Start microphone
        microphone.start();
        gui.appendLog("[INFO] Microphone activated - speak clearly");
        
        // Create recognition thread
        recognitionThread = new Thread(() -> {
            byte[] buffer = new byte[4096];
            
            // Main listening loop
            while (isRunning) {
                try {
                    // Read audio data from microphone
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    
                    if (bytesRead > 0 && isRunning) {
                        // Process audio with Vosk
                        if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                            // Get final result (complete sentence)
                            String result = recognizer.getResult();
                            
                            try {
                                JSONObject json = new JSONObject(result);
                                
                                // Check if "text" field exists
                                if (json.has("text")) {
                                    String text = json.getString("text");
                                    
                                    if (!text.isEmpty()) {
                                        // Update GUI with recognized command
                                        gui.updateLastCommand(text);
                                        
                                        // Process the command
                                        processCommand(text);
                                    } else {
                                        // Clear partial results if final result is empty
                                        gui.updateLastCommand("None");
                                    }
                                }
                            } catch (Exception jsonEx) {
                                // Ignore JSON parsing errors
                            }
                        }
                        // Partial results disabled to prevent leftover text
                    }
                    
                } catch (Exception e) {
                    if (isRunning) {
                        gui.appendLog("[ERROR] Error processing speech: " + e.getMessage());
                    }
                }
            }
            
            // Stop microphone when done
            microphone.stop();
            gui.appendLog("[INFO] Voice recognition stopped");
        });
        
        recognitionThread.setDaemon(true);
        recognitionThread.start();
    }
    
    /**
     * Stop listening for voice commands
     */
    public void stopListening() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        
        // Interrupt the recognition thread if it's waiting
        if (recognitionThread != null && recognitionThread.isAlive()) {
            recognitionThread.interrupt();
        }
    }
    
    /**
     * Find best matching app name using fuzzy matching
     */
    private String findBestMatch(String input, String[] appNames) {
        String bestMatch = null;
        int bestScore = Integer.MAX_VALUE;
        
        for (String appName : appNames) {
            int distance = levenshteinDistance(input.toLowerCase(), appName.toLowerCase());
            
            // If distance is small enough, consider it a match
            if (distance < 4 && distance < bestScore) {
                bestScore = distance;
                bestMatch = appName;
            }
        }
        
        return bestMatch;
    }
    
    /**
     * Calculate Levenshtein distance (edit distance) between two strings
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Apply smart corrections for commonly misheard words
     */
    private String applySmartCorrections(String command) {
        // Common misheard patterns
        command = command.replace("oh went", "open");
        command = command.replace("oh pen", "open");
        command = command.replace("opened", "open");
        command = command.replace("opening", "open");
        
        // Browser names
        command = command.replace("chrome", "chrome");
        command = command.replace("crome", "chrome");
        command = command.replace("krome", "chrome");
        command = command.replace("from", "chrome");
        
        command = command.replace("spot if i", "spotify");
        command = command.replace("spot if", "spotify");
        command = command.replace("spotty", "spotify");
        
        command = command.replace("what's up", "whatsapp");
        command = command.replace("watts up", "whatsapp");
        
        command = command.replace("calculator", "calculator");
        command = command.replace("calc", "calculator");
        
        // Search commands
        command = command.replace("search", "search");
        command = command.replace("such", "search");
        command = command.replace("surge", "search");
        
        // Log if correction was made
        if (!command.equals(command)) {
            gui.appendLog("[CORRECTED] \"" + command + "\"");
        }
        
        return command;
    }
    
    /**
     * Process voice commands and trigger appropriate actions
     * @param command The recognized voice command
     */
    private void processCommand(String command) {
        // Normalize command to lowercase for easier matching
        String normalizedCommand = command.toLowerCase().trim();
        
        // Log what was heard
        gui.appendLog("[HEARD] \"" + command + "\"");
        
        // Apply smart corrections for common misheard words
        normalizedCommand = applySmartCorrections(normalizedCommand);
        
        // Check for exit command
        if (normalizedCommand.equals("exit") || 
            normalizedCommand.equals("quit") || 
            normalizedCommand.equals("close") ||
            normalizedCommand.equals("stop")) {
            
            gui.appendLog("[ACTION] Exit command received");
            gui.appendLog("[INFO] Please use the GUI to exit the application");
            return;
        }
        
        // Check for "search [query] in [browser]" or "open [browser] and search [query]"
        if (normalizedCommand.contains("search") && (normalizedCommand.contains("chrome") || 
            normalizedCommand.contains("edge") || normalizedCommand.contains("firefox"))) {
            handleBrowserSearch(normalizedCommand);
            return;
        }
        
        // Check for "open [app]" command
        if (normalizedCommand.startsWith("open ")) {
            // Extract app name after "open "
            String appName = normalizedCommand.substring(5).trim();
            
            if (appName.isEmpty()) {
                gui.appendLog("[ERROR] No application name specified");
                return;
            }
            
            // Check if it's a browser with search query
            if ((appName.contains("chrome") || appName.contains("edge") || appName.contains("firefox")) 
                && appName.contains("search")) {
                handleBrowserSearch(appName);
                return;
            }
            
            // Try to launch the application
            gui.appendLog("[ACTION] Attempting to open: " + appName);
            boolean success = appLauncher.launchApp(appName);
            
            if (!success) {
                // Try fuzzy matching
                String matchedApp = findBestMatch(appName, appLauncher.getAppNames());
                if (matchedApp != null) {
                    gui.appendLog("[SMART MATCH] Did you mean: " + matchedApp + "?");
                    success = appLauncher.launchApp(matchedApp);
                    if (success) {
                        gui.appendLog("[SUCCESS] Launched: " + matchedApp);
                        return;
                    }
                }
                gui.appendLog("[ERROR] Failed to launch: " + appName);
                gui.appendLog("[HINT] Try: " + String.join(", ", appLauncher.getAppNames()));
            } else {
                gui.appendLog("[SUCCESS] Launched: " + appName);
            }
            
            return;
        }
        
        // Check for "launch [app]" command (alternative)
        if (normalizedCommand.startsWith("launch ")) {
            String appName = normalizedCommand.substring(7).trim();
            
            if (!appName.isEmpty()) {
                gui.appendLog("[ACTION] Attempting to launch: " + appName);
                boolean success = appLauncher.launchApp(appName);
                
                if (success) {
                    gui.appendLog("[SUCCESS] Launched: " + appName);
                } else {
                    gui.appendLog("[ERROR] Failed to launch: " + appName);
                    gui.appendLog("[HINT] Available apps: " + String.join(", ", appLauncher.getAppNames()));
                }
            }
            
            return;
        }
        
        // Check for "start [app]" command (alternative)
        if (normalizedCommand.startsWith("start ")) {
            String appName = normalizedCommand.substring(6).trim();
            
            if (!appName.isEmpty()) {
                gui.appendLog("[ACTION] Attempting to start: " + appName);
                boolean success = appLauncher.launchApp(appName);
                
                if (success) {
                    gui.appendLog("[SUCCESS] Launched: " + appName);
                } else {
                    gui.appendLog("[ERROR] Failed to launch: " + appName);
                    gui.appendLog("[HINT] Available apps: " + String.join(", ", appLauncher.getAppNames()));
                }
            }
            
            return;
        }
        
        // Check for "list apps" or "show apps" command
        if (normalizedCommand.contains("list") || normalizedCommand.contains("show")) {
            gui.appendLog("[INFO] Available applications:");
            int count = appLauncher.getAppCount();
            for (String appName : appLauncher.getAppNames()) {
                gui.appendLog("  • " + appName);
            }
            return;
        }
        
        // Check for "help" command
        if (normalizedCommand.contains("help")) {
            gui.appendLog("[HELP] Available voice commands:");
            gui.appendLog("  • 'open [app]' - Launch an application");
            gui.appendLog("  • 'launch [app]' - Launch an application (alternative)");
            gui.appendLog("  • 'start [app]' - Launch an application (alternative)");
            gui.appendLog("  • 'list apps' - Show all available apps");
            gui.appendLog("  • 'help' - Show this help message");
            gui.appendLog("  • Use the GUI button to stop voice recognition");
            return;
        }
        
        // Unknown command
        gui.appendLog("[WARNING] Command not recognized: " + command);
        gui.appendLog("[HINT] Say 'help' for available commands");
    }
    
    /**
     * Handle browser search commands
     * Examples: "open chrome and search india", "search india in edge"
     */
    private void handleBrowserSearch(String command) {
        try {
            String browser = "";
            String searchQuery = "";
            
            // Determine browser
            if (command.contains("chrome")) {
                browser = "chrome";
            } else if (command.contains("edge")) {
                browser = "edge";
            } else if (command.contains("firefox")) {
                browser = "firefox";
            }
            
            // Extract search query
            if (command.contains("search for ")) {
                int startIndex = command.indexOf("search for ") + 11;
                searchQuery = command.substring(startIndex).trim();
                // Remove browser name if it appears after search query
                searchQuery = searchQuery.replace("in " + browser, "")
                                       .replace("on " + browser, "")
                                       .replace(browser, "").trim();
            } else if (command.contains("search ")) {
                int startIndex = command.indexOf("search ") + 7;
                searchQuery = command.substring(startIndex).trim();
                // Remove "in browser" or "on browser"
                searchQuery = searchQuery.replace("in " + browser, "")
                                       .replace("on " + browser, "")
                                       .replace(browser, "").trim();
            }
            
            if (searchQuery.isEmpty()) {
                gui.appendLog("[ERROR] No search query specified");
                return;
            }
            
            // Build Google search URL
            String encodedQuery = searchQuery.replace(" ", "+");
            String searchUrl = "https://www.google.com/search?q=" + encodedQuery;
            
            gui.appendLog("[ACTION] Opening " + browser.toUpperCase() + " and searching for: " + searchQuery);
            
            // Open browser with search URL
            Desktop.getDesktop().browse(new URI(searchUrl));
            
            gui.appendLog("[SUCCESS] Opened " + browser.toUpperCase() + " with search results");
            
        } catch (Exception e) {
            gui.appendLog("[ERROR] Failed to open browser: " + e.getMessage());
        }
    }
    
    /**
     * Cleanup resources when shutting down
     */
    public void cleanup() {
        stopListening();
        
        // Give the recognition thread time to stop
        try {
            if (recognitionThread != null && recognitionThread.isAlive()) {
                recognitionThread.join(1000); // Wait up to 1 second
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Close microphone
        if (microphone != null && microphone.isOpen()) {
            microphone.close();
        }
        
        // Close model
        if (model != null) {
            model.close();
        }
        
        gui.appendLog("[INFO] Voice recognition resources cleaned up");
    }
    
    /**
     * Check if voice recognition is currently running
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
}
