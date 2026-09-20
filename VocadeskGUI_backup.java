package com.vocadesk;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * VocadeskGUI - Modern, powerful Swing-based GUI for VOcadesk
 * Features sleek design, smooth animations, and professional styling
 */
public class VocadeskGUI extends JFrame {
    
    // Modern color scheme
    private static final Color PRIMARY_BG = new Color(18, 18, 18);           // Dark background
    private static final Color SECONDARY_BG = new Color(30, 30, 30);         // Card background
    private static final Color ACCENT_GREEN = new Color(76, 175, 80);        // Success green
    private static final Color ACCENT_RED = new Color(244, 67, 54);          // Error red
    private static final Color ACCENT_BLUE = new Color(33, 150, 243);        // Info blue
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);      // White text
    private static final Color TEXT_SECONDARY = new Color(158, 158, 158);    // Gray text
    private static final Color BORDER_COLOR = new Color(60, 60, 60);         // Subtle borders
    
    // GUI Components
    private JButton toggleButton;
    private JLabel statusLabel;
    private JLabel lastCommandLabel;
    private JTextArea appListArea;
    private JTextPane logPane;
    private JScrollPane logScrollPane;
    private JProgressBar waveformBar;
    private JLabel micIconLabel;
    private JPanel headerPanel;
    private JCheckBoxMenuItem alwaysOnTopMenuItem;
    
    // Voice recognition controller
    private VoiceRecognitionController voiceController;
    
    // State tracking
    private boolean isListening = false;
    private Timer waveformTimer;
    
    /**
     * Constructor - Initialize the modern GUI
     */
    public VocadeskGUI(AppLauncher appLauncher) {
        this.voiceController = new VoiceRecognitionController(appLauncher, this);
        
        setupWindow();
        createMenuBar();
        createComponents();
        layoutComponents();
        startWaveformAnimation();
        
        setVisible(true);
        
        appendLog("🚀 VOcadesk initialized successfully", "INFO");
        appendLog("📦 Loaded " + appLauncher.getAppCount() + " applications", "INFO");
        appendLog("🎤 Click 'Start Listening' to begin voice control", "INFO");
    }
    
    /**
     * Setup main window with modern styling
     */
    private void setupWindow() {
        setTitle("VOcadesk - AI Voice Launcher v2.0 NEW");
        setSize(900, 700);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Optional: Always on top (uncomment to enable)
        // setAlwaysOnTop(true);
        
        // Dark theme
        getContentPane().setBackground(PRIMARY_BG);
        
        // Window close handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
        
        // Set system look and feel with dark theme
        try {
            UIManager.put("control", SECONDARY_BG);
            UIManager.put("text", TEXT_PRIMARY);
            UIManager.put("nimbusBase", PRIMARY_BG);
            UIManager.put("nimbusFocus", ACCENT_BLUE);
        } catch (Exception e) {
            // Use default if fails
        }
    }
    
    /**
     * Create menu bar with options
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(SECONDARY_BG);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        
        // View menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setForeground(TEXT_PRIMARY);
        
        // Always on top option
        alwaysOnTopMenuItem = new JCheckBoxMenuItem("📌 Pin to Top");
        alwaysOnTopMenuItem.setForeground(TEXT_PRIMARY);
        alwaysOnTopMenuItem.setBackground(SECONDARY_BG);
        alwaysOnTopMenuItem.addActionListener(e -> {
            setAlwaysOnTop(alwaysOnTopMenuItem.isSelected());
            if (alwaysOnTopMenuItem.isSelected()) {
                appendLog("📌 Window pinned to top", "INFO");
            } else {
                appendLog("📌 Window unpinned", "INFO");
            }
        });
        
        viewMenu.add(alwaysOnTopMenuItem);
        
        // Minimize to tray option
        JMenuItem minimizeItem = new JMenuItem("➖ Minimize");
        minimizeItem.setForeground(TEXT_PRIMARY);
        minimizeItem.setBackground(SECONDARY_BG);
        minimizeItem.addActionListener(e -> setState(JFrame.ICONIFIED));
        viewMenu.add(minimizeItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setForeground(TEXT_PRIMARY);
        
        JMenuItem aboutItem = new JMenuItem("ℹ️ About");
        aboutItem.setForeground(TEXT_PRIMARY);
        aboutItem.setBackground(SECONDARY_BG);
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        JMenuItem commandsItem = new JMenuItem("💬 Voice Commands");
        commandsItem.setForeground(TEXT_PRIMARY);
        commandsItem.setBackground(SECONDARY_BG);
        commandsItem.addActionListener(e -> showCommandsDialog());
        helpMenu.add(commandsItem);
        
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Show about dialog
     */
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
            this,
            "VOcadesk - AI Voice Launcher\n\n" +
            "Version: 1.0.0\n" +
            "Offline voice-controlled application launcher\n\n" +
            "Powered by Vosk Speech Recognition\n" +
            "© 2025 VOcadesk Project",
            "About VOcadesk",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Show commands dialog
     */
    private void showCommandsDialog() {
        JOptionPane.showMessageDialog(
            this,
            "Available Voice Commands:\n\n" +
            "• 'open [app name]' - Launch an application\n" +
            "• 'launch [app name]' - Alternative launch command\n" +
            "• 'start [app name]' - Alternative launch command\n" +
            "• 'list apps' - Show all available apps\n" +
            "• 'help' - Show help information\n\n" +
            "Examples:\n" +
            "• 'open calculator'\n" +
            "• 'launch notepad'\n" +
            "• 'start paint'",
            "Voice Commands",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Create all GUI components with modern styling
     */
    private void createComponents() {
        // Header panel with gradient effect
        headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(33, 150, 243),
                    0, getHeight(), new Color(76, 175, 80)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(900, 120));
        headerPanel.setLayout(new BorderLayout(10, 10));
        
        // Status label with icon
        statusLabel = new JLabel("● Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        statusLabel.setForeground(TEXT_PRIMARY);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Microphone icon
        micIconLabel = new JLabel("🎤");
        micIconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        micIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Toggle button with modern styling
        toggleButton = new JButton("START LISTENING");
        toggleButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        toggleButton.setPreferredSize(new Dimension(250, 50));
        toggleButton.setBackground(ACCENT_GREEN);
        toggleButton.setForeground(TEXT_PRIMARY);
        toggleButton.setFocusPainted(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleButton.addActionListener(e -> toggleVoiceRecognition());
        
        // Hover effect
        toggleButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isListening) {
                    toggleButton.setBackground(new Color(67, 160, 71));
                } else {
                    toggleButton.setBackground(new Color(229, 57, 53));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (!isListening) {
                    toggleButton.setBackground(ACCENT_GREEN);
                } else {
                    toggleButton.setBackground(ACCENT_RED);
                }
            }
        });
        
        // Last command label with modern card style
        lastCommandLabel = new JLabel("Last Command: None");
        lastCommandLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lastCommandLabel.setForeground(TEXT_SECONDARY);
        lastCommandLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lastCommandLabel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        // Waveform visualization bar
        waveformBar = new JProgressBar(0, 100);
        waveformBar.setValue(0);
        waveformBar.setStringPainted(false);
        waveformBar.setBackground(SECONDARY_BG);
        waveformBar.setForeground(ACCENT_BLUE);
        waveformBar.setBorderPainted(false);
        waveformBar.setPreferredSize(new Dimension(0, 4));
        
        // App list area with modern styling
        appListArea = new JTextArea();
        appListArea.setEditable(false);
        appListArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        appListArea.setBackground(SECONDARY_BG);
        appListArea.setForeground(TEXT_PRIMARY);
        appListArea.setBorder(new EmptyBorder(15, 15, 15, 15));
        appListArea.setLineWrap(false);
        
        // Log pane with styled text support
        logPane = new JTextPane();
        logPane.setEditable(false);
        logPane.setFont(new Font("Consolas", Font.PLAIN, 12));
        logPane.setBackground(PRIMARY_BG);
        logPane.setForeground(TEXT_PRIMARY);
        logPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        logScrollPane = new JScrollPane(logPane);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        logScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        logScrollPane.getVerticalScrollBar().setBackground(SECONDARY_BG);
    }
    
    /**
     * Layout all components with modern card-based design
     */
    private void layoutComponents() {
        setLayout(new BorderLayout(0, 0));
        
        // Header section
        JPanel headerContent = new JPanel(new BorderLayout(10, 10));
        headerContent.setOpaque(false);
        headerContent.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);
        topHeader.add(micIconLabel, BorderLayout.WEST);
        topHeader.add(statusLabel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(toggleButton);
        
        headerContent.add(topHeader, BorderLayout.NORTH);
        headerContent.add(buttonPanel, BorderLayout.CENTER);
        headerContent.add(waveformBar, BorderLayout.SOUTH);
        
        headerPanel.add(headerContent, BorderLayout.CENTER);
        
        // Main content area
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(PRIMARY_BG);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Last command card
        JPanel commandCard = createCard("Last Command", lastCommandLabel);
        
        // Split pane for apps and logs
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.3);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        splitPane.setBackground(PRIMARY_BG);
        
        // App list card
        JPanel appCard = createCard("📱 Available Applications", new JScrollPane(appListArea));
        
        // Log card
        JPanel logCard = createCard("📊 Activity Monitor", logScrollPane);
        
        splitPane.setTopComponent(appCard);
        splitPane.setBottomComponent(logCard);
        
        // Assemble main panel
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(PRIMARY_BG);
        centerPanel.add(commandCard, BorderLayout.NORTH);
        centerPanel.add(splitPane, BorderLayout.CENTER);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Footer with instructions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(SECONDARY_BG);
        footerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel instructionsLabel = new JLabel("💡 Say: 'open [app]' | 'list apps' | 'help'");
        instructionsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instructionsLabel.setForeground(TEXT_SECONDARY);
        footerPanel.add(instructionsLabel);
        
        // Add all to frame
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Create a modern card panel
     */
    private JPanel createCard(String title, Component content) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(SECONDARY_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Start waveform animation
     */
    private void startWaveformAnimation() {
        waveformTimer = new Timer(50, e -> {
            if (isListening) {
                int value = (int) (Math.random() * 100);
                waveformBar.setValue(value);
            } else {
                waveformBar.setValue(0);
            }
        });
        waveformTimer.start();
    }
    
    /**
     * Toggle voice recognition
     */
    private void toggleVoiceRecognition() {
        if (!isListening) {
            startVoiceRecognition();
        } else {
            stopVoiceRecognition();
        }
    }
    
    /**
     * Start voice recognition with animations
     */
    private void startVoiceRecognition() {
        appendLog("🎙️ Starting voice recognition...", "ACTION");
        
        toggleButton.setText("STOP LISTENING");
        toggleButton.setBackground(ACCENT_RED);
        statusLabel.setText("● Listening...");
        statusLabel.setForeground(ACCENT_RED);
        micIconLabel.setText("🔴");
        isListening = true;
        
        new Thread(() -> {
            try {
                voiceController.startListening();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    appendLog("❌ Voice recognition failed: " + e.getMessage(), "ERROR");
                    stopVoiceRecognition();
                    JOptionPane.showMessageDialog(
                        this,
                        "Failed to start voice recognition:\n" + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                });
            }
        }).start();
        
        appendLog("✅ Voice recognition started successfully", "SUCCESS");
    }
    
    /**
     * Stop voice recognition
     */
    private void stopVoiceRecognition() {
        appendLog("⏹️ Stopping voice recognition...", "ACTION");
        
        voiceController.stopListening();
        
        toggleButton.setText("START LISTENING");
        toggleButton.setBackground(ACCENT_GREEN);
        statusLabel.setText("● Ready");
        statusLabel.setForeground(ACCENT_GREEN);
        micIconLabel.setText("🎤");
        isListening = false;
        
        appendLog("✅ Voice recognition stopped", "SUCCESS");
    }
    
    /**
     * Update last command with animation
     */
    public void updateLastCommand(String command) {
        SwingUtilities.invokeLater(() -> {
            lastCommandLabel.setText("💬 Last Command: " + command);
            lastCommandLabel.setForeground(ACCENT_BLUE);
            
            // Fade back to normal color
            Timer timer = new Timer(2000, e -> {
                lastCommandLabel.setForeground(TEXT_SECONDARY);
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
    
    /**
     * Append styled log message
     */
    public void appendLog(String message, String type) {
        SwingUtilities.invokeLater(() -> {
            try {
                javax.swing.text.StyledDocument doc = logPane.getStyledDocument();
                javax.swing.text.Style style = logPane.addStyle("Style", null);
                
                // Set color based on type
                Color color = TEXT_PRIMARY;
                String icon = "ℹ️";
                
                switch (type) {
                    case "SUCCESS":
                        color = ACCENT_GREEN;
                        icon = "✅";
                        break;
                    case "ERROR":
                        color = ACCENT_RED;
                        icon = "❌";
                        break;
                    case "WARNING":
                        color = new Color(255, 193, 7);
                        icon = "⚠️";
                        break;
                    case "ACTION":
                        color = ACCENT_BLUE;
                        icon = "▶️";
                        break;
                    case "INFO":
                        color = TEXT_SECONDARY;
                        icon = "ℹ️";
                        break;
                }
                
                javax.swing.text.StyleConstants.setForeground(style, color);
                doc.insertString(doc.getLength(), icon + " " + message + "\n", style);
                
                // Auto-scroll
                logPane.setCaretPosition(doc.getLength());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Append log message (backward compatibility)
     */
    public void appendLog(String message) {
        if (message.contains("[SUCCESS]") || message.contains("✅")) {
            appendLog(message.replace("[SUCCESS]", "").replace("✅", "").trim(), "SUCCESS");
        } else if (message.contains("[ERROR]") || message.contains("❌")) {
            appendLog(message.replace("[ERROR]", "").replace("❌", "").trim(), "ERROR");
        } else if (message.contains("[WARNING]") || message.contains("⚠️")) {
            appendLog(message.replace("[WARNING]", "").replace("⚠️", "").trim(), "WARNING");
        } else if (message.contains("[ACTION]") || message.contains("▶️")) {
            appendLog(message.replace("[ACTION]", "").replace("▶️", "").trim(), "ACTION");
        } else {
            appendLog(message.replace("[INFO]", "").replace("ℹ️", "").trim(), "INFO");
        }
    }
    
    /**
     * Update app list
     */
    public void updateAppList(String[] appNames) {
        SwingUtilities.invokeLater(() -> {
            appListArea.setText("");
            appListArea.append("Total Applications: " + appNames.length + "\n");
            appListArea.append("━".repeat(40) + "\n\n");
            for (int i = 0; i < appNames.length; i++) {
                appListArea.append(String.format("  %d. %s\n", i + 1, appNames[i]));
            }
        });
    }
    
    /**
     * Handle exit
     */
    private void handleExit() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit VOcadesk?",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            appendLog("👋 Shutting down VOcadesk...", "INFO");
            
            if (isListening) {
                voiceController.stopListening();
            }
            
            if (waveformTimer != null) {
                waveformTimer.stop();
            }
            
            voiceController.cleanup();
            System.exit(0);
        }
    }
    
    /**
     * Check if listening
     */
    public boolean isListening() {
        return isListening;
    }
    
    /**
     * Modern scroll bar UI
     */
    private class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = BORDER_COLOR;
            this.trackColor = SECONDARY_BG;
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }
        
        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }
        
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
            g2.dispose();
        }
    }
}
