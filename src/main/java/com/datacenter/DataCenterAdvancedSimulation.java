package com.datacenter;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class DataCenterAdvancedSimulation extends JFrame {

    // Define constants
    private static final int MAX_SERVERS = 20;
    private static final int MAX_SCHEDULERS = 10;

    // User configurable parameters
    private int numServers = 10;
    private int numSchedulers = 5;
    private double energyCostPerWatt = 0.12;
    private double slaPenaltyPerMs = 0.05;
    private double revenuePerTask = 0.5;

    // UI Components - Controls
    private JPanel serverPanel;
    private JLabel energyLabel;
    private JLabel responseTimeLabel;
    private JLabel activeServersLabel;
    private JLabel costLabel;
    private JLabel revenueLabel;
    private JLabel profitLabel;
    private JLabel equilibriumLabel;
    private JTextArea logArea;
    private JTextArea gameTheoryArea;
    private JProgressBar[] serverLoadBars;
    private JLabel[] serverStatusLabels;
    private JLabel[] serverUtilityLabels;
    private JButton runButton;
    private JButton resetButton;
    private JButton stepButton;
    private JButton advancedSettingsButton;

    // New control components
    private JSlider schedulerSlider;
    private JSlider serverSlider;
    private JLabel schedulerValueLabel;
    private JLabel serverValueLabel;
    private JComboBox<String> strategyComboBox;
    private JComboBox<String> loadPatternComboBox;
    private JComboBox<String> serverTypeComboBox;
    private JComboBox<String> allocationAlgorithmComboBox;
    private JComboBox<String> optimizationGoalComboBox;
    private JCheckBox enableDynamicPricingCheckBox;
    private JCheckBox enableServerFailuresCheckBox;
    private JCheckBox enableQoSCheckBox;
    private JSlider convergenceThresholdSlider;
    private JSlider maxIterationsSlider;

    // Simulation objects
    private Server[] servers;
    private Scheduler[] schedulers;
    private GameTheoryEngine gameEngine;
    private int currentStep = 0;
    private boolean isStepMode = false;

    // Configuration
    private String loadPattern = "Uniform";
    private String serverType = "Homogeneous";
    private String allocationAlgorithm = "Best Response";
    private String optimizationGoal = "Cost Minimization";
    private boolean dynamicPricing = false;
    private boolean serverFailures = false;
    private boolean qosEnabled = true;
    private double convergenceThreshold = 0.01;
    private int maxIterations = 5;

    public DataCenterAdvancedSimulation() {
        setTitle("Advanced Data Center Game Theory Simulation");
        setSize(1600, 950);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Create panels
        add(createControlPanel(), BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new GridLayout(1, 2, 10, 0));
        centerContainer.add(createServerPanel());
        centerContainer.add(createGameTheoryPanel());
        add(centerContainer, BorderLayout.CENTER);

        add(createStatsPanel(), BorderLayout.EAST);
        add(createLogPanel(), BorderLayout.SOUTH);

        // Initialize simulation
        initializeSimulation();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createControlPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(44, 62, 80));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Advanced Control Panel - Stackelberg Game with Multiple Options");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel controlBox = new JPanel(new GridLayout(2, 1, 5, 5));
        controlBox.setBackground(new Color(44, 62, 80));

        // Row 1: Basic controls
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        row1.setBackground(new Color(44, 62, 80));

        // Server slider
        JPanel serverPanel = createLabeledControl("Servers:",
                serverSlider = new JSlider(5, MAX_SERVERS, numServers),
                serverValueLabel = new JLabel(numServers + " servers"));
        serverSlider.addChangeListener(e -> {
            numServers = serverSlider.getValue();
            serverValueLabel.setText(numServers + " servers");
            updateServerPanelVisibility();
        });

        // Scheduler slider
        JPanel schedulerPanel = createLabeledControl("Schedulers:",
                schedulerSlider = new JSlider(1, MAX_SCHEDULERS, numSchedulers),
                schedulerValueLabel = new JLabel(numSchedulers + " schedulers"));
        schedulerSlider.addChangeListener(e -> {
            numSchedulers = schedulerSlider.getValue();
            schedulerValueLabel.setText(numSchedulers + " schedulers");
        });

        // Leader strategy
        JPanel strategyPanel = createLabeledComboBox("Leader Strategy:",
                strategyComboBox = new JComboBox<>(new String[]{
                        "Energy Efficient", "Load Balanced", "Profit Maximizing",
                        "QoS Focused", "Adaptive", "Conservative", "Aggressive"
                }));

        // Allocation algorithm
        JPanel algoPanel = createLabeledComboBox("Allocation Algorithm:",
                allocationAlgorithmComboBox = new JComboBox<>(new String[]{
                        "Best Response", "Proportional Fair", "Water Filling",
                        "Min-Max Fair", "Random"
                }));

        row1.add(serverPanel);
        row1.add(schedulerPanel);
        row1.add(strategyPanel);
        row1.add(algoPanel);

        // Row 2: Advanced controls
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        row2.setBackground(new Color(44, 62, 80));

        // Load pattern
        JPanel loadPanel = createLabeledComboBox("Load Pattern:",
                loadPatternComboBox = new JComboBox<>(new String[]{
                        "Uniform", "Bursty", "Peak Hours", "Random", "Decreasing"
                }));

        // Server type
        JPanel serverTypePanel = createLabeledComboBox("Server Type:",
                serverTypeComboBox = new JComboBox<>(new String[]{
                        "Homogeneous", "Heterogeneous", "High-Performance Mix", "Energy-Efficient Mix"
                }));

        // Optimization goal
        JPanel goalPanel = createLabeledComboBox("Optimization Goal:",
                optimizationGoalComboBox = new JComboBox<>(new String[]{
                        "Cost Minimization", "Profit Maximization", "QoS Maximization",
                        "Energy Minimization", "Balanced"
                }));

        // Checkboxes
        JPanel checkPanel = new JPanel(new GridLayout(3, 1));
        checkPanel.setBackground(new Color(44, 62, 80));
        enableDynamicPricingCheckBox = new JCheckBox("Dynamic Pricing");
        enableDynamicPricingCheckBox.setForeground(Color.WHITE);
        enableDynamicPricingCheckBox.setBackground(new Color(44, 62, 80));
        enableServerFailuresCheckBox = new JCheckBox("Server Failures");
        enableServerFailuresCheckBox.setForeground(Color.WHITE);
        enableServerFailuresCheckBox.setBackground(new Color(44, 62, 80));
        enableQoSCheckBox = new JCheckBox("QoS Constraints");
        enableQoSCheckBox.setForeground(Color.WHITE);
        enableQoSCheckBox.setBackground(new Color(44, 62, 80));
        enableQoSCheckBox.setSelected(true);
        checkPanel.add(enableDynamicPricingCheckBox);
        checkPanel.add(enableServerFailuresCheckBox);
        checkPanel.add(enableQoSCheckBox);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(44, 62, 80));

        runButton = new JButton("Run Full Simulation");
        runButton.setBackground(new Color(39, 174, 96));
        runButton.setForeground(Color.WHITE);
        runButton.setFont(new Font("Arial", Font.BOLD, 11));
        runButton.setFocusPainted(false);
        runButton.addActionListener(e -> runFullSimulation());

        stepButton = new JButton("Step Mode");
        stepButton.setBackground(new Color(52, 152, 219));
        stepButton.setForeground(Color.WHITE);
        stepButton.setFont(new Font("Arial", Font.BOLD, 11));
        stepButton.setFocusPainted(false);
        stepButton.addActionListener(e -> runStepMode());

        advancedSettingsButton = new JButton("Advanced Settings");
        advancedSettingsButton.setBackground(new Color(155, 89, 182));
        advancedSettingsButton.setForeground(Color.WHITE);
        advancedSettingsButton.setFont(new Font("Arial", Font.BOLD, 11));
        advancedSettingsButton.setFocusPainted(false);
        advancedSettingsButton.addActionListener(e -> showAdvancedSettings());

        resetButton = new JButton("Reset");
        resetButton.setBackground(new Color(231, 76, 60));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFont(new Font("Arial", Font.BOLD, 11));
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> resetSimulation());

        buttonPanel.add(runButton);
        buttonPanel.add(stepButton);
        buttonPanel.add(advancedSettingsButton);
        buttonPanel.add(resetButton);

        row2.add(loadPanel);
        row2.add(serverTypePanel);
        row2.add(goalPanel);
        row2.add(checkPanel);
        row2.add(buttonPanel);

        controlBox.add(row1);
        controlBox.add(row2);

        mainPanel.add(title, BorderLayout.NORTH);
        mainPanel.add(controlBox, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createLabeledControl(String labelText, JSlider slider, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(3, 3));
        panel.setBackground(new Color(44, 62, 80));
        panel.setPreferredSize(new Dimension(150, 60));

        JLabel label = new JLabel(labelText);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.PLAIN, 11));

        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 11));

        slider.setBackground(new Color(44, 62, 80));
        slider.setForeground(Color.WHITE);

        panel.add(label, BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);
        panel.add(valueLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLabeledComboBox(String labelText, JComboBox<String> comboBox) {
        JPanel panel = new JPanel(new BorderLayout(3, 3));
        panel.setBackground(new Color(44, 62, 80));
        panel.setPreferredSize(new Dimension(180, 50));

        JLabel label = new JLabel(labelText);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.PLAIN, 11));

        comboBox.setFont(new Font("Arial", Font.PLAIN, 11));

        panel.add(label, BorderLayout.NORTH);
        panel.add(comboBox, BorderLayout.CENTER);

        return panel;
    }

    private void showAdvancedSettings() {
        JDialog dialog = new JDialog(this, "Advanced Settings", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(500, 400);

        JPanel settingsPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        settingsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Energy cost
        settingsPanel.add(new JLabel("Energy Cost ($/W):"));
        JTextField energyCostField = new JTextField(String.valueOf(energyCostPerWatt));
        settingsPanel.add(energyCostField);

        // SLA penalty
        settingsPanel.add(new JLabel("SLA Penalty ($/ms):"));
        JTextField slaPenaltyField = new JTextField(String.valueOf(slaPenaltyPerMs));
        settingsPanel.add(slaPenaltyField);

        // Revenue per task
        settingsPanel.add(new JLabel("Revenue per Task ($):"));
        JTextField revenueField = new JTextField(String.valueOf(revenuePerTask));
        settingsPanel.add(revenueField);

        // Convergence threshold
        settingsPanel.add(new JLabel("Convergence Threshold ($):"));
        convergenceThresholdSlider = new JSlider(1, 100, (int)(convergenceThreshold * 100));
        JLabel thresholdLabel = new JLabel(String.format("%.2f", convergenceThreshold));
        convergenceThresholdSlider.addChangeListener(e -> {
            convergenceThreshold = convergenceThresholdSlider.getValue() / 100.0;
            thresholdLabel.setText(String.format("%.2f", convergenceThreshold));
        });
        JPanel thresholdPanel = new JPanel(new BorderLayout());
        thresholdPanel.add(convergenceThresholdSlider, BorderLayout.CENTER);
        thresholdPanel.add(thresholdLabel, BorderLayout.EAST);
        settingsPanel.add(thresholdPanel);

        // Max iterations
        settingsPanel.add(new JLabel("Max Iterations:"));
        maxIterationsSlider = new JSlider(1, 20, maxIterations);
        JLabel iterLabel = new JLabel(String.valueOf(maxIterations));
        maxIterationsSlider.addChangeListener(e -> {
            maxIterations = maxIterationsSlider.getValue();
            iterLabel.setText(String.valueOf(maxIterations));
        });
        JPanel iterPanel = new JPanel(new BorderLayout());
        iterPanel.add(maxIterationsSlider, BorderLayout.CENTER);
        iterPanel.add(iterLabel, BorderLayout.EAST);
        settingsPanel.add(iterPanel);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                energyCostPerWatt = Double.parseDouble(energyCostField.getText());
                slaPenaltyPerMs = Double.parseDouble(slaPenaltyField.getText());
                revenuePerTask = Double.parseDouble(revenueField.getText());
                log("Advanced settings updated");
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number format!");
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(new JLabel("Configure Economic Parameters", SwingConstants.CENTER),
                BorderLayout.NORTH);
        dialog.add(settingsPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createServerPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(236, 240, 241));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Server Status, Properties & Utilities");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        serverPanel = new JPanel();
        serverPanel.setLayout(new BoxLayout(serverPanel, BoxLayout.Y_AXIS));
        serverPanel.setBackground(Color.WHITE);

        serverLoadBars = new JProgressBar[MAX_SERVERS];
        serverStatusLabels = new JLabel[MAX_SERVERS];
        serverUtilityLabels = new JLabel[MAX_SERVERS];

        for (int i = 0; i < MAX_SERVERS; i++) {
            JPanel serverBox = new JPanel(new BorderLayout(5, 2));
            serverBox.setBackground(Color.WHITE);
            serverBox.setBorder(new CompoundBorder(
                    new LineBorder(new Color(189, 195, 199), 1),
                    new EmptyBorder(3, 5, 3, 5)
            ));

            // Top row: Status, ID, Load Bar, Utility
            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            topRow.setBackground(Color.WHITE);

            JLabel statusIndicator = new JLabel("●");
            statusIndicator.setFont(new Font("Arial", Font.BOLD, 16));
            statusIndicator.setForeground(Color.GRAY);
            serverStatusLabels[i] = statusIndicator;

            JLabel serverLabel = new JLabel("S" + i);
            serverLabel.setPreferredSize(new Dimension(25, 18));
            serverLabel.setFont(new Font("Arial", Font.BOLD, 10));

            JProgressBar loadBar = new JProgressBar(0, 100);
            loadBar.setPreferredSize(new Dimension(120, 18));
            loadBar.setStringPainted(true);
            loadBar.setString("0%");
            loadBar.setFont(new Font("Arial", Font.PLAIN, 9));
            serverLoadBars[i] = loadBar;

            JLabel utilityLabel = new JLabel("$0.00");
            utilityLabel.setPreferredSize(new Dimension(80, 18));
            utilityLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            serverUtilityLabels[i] = utilityLabel;

            topRow.add(statusIndicator);
            topRow.add(serverLabel);
            topRow.add(loadBar);
            topRow.add(utilityLabel);

            // Bottom row: Server properties (will be populated after initialization)
            JLabel propertiesLabel = new JLabel("Properties: Loading...");
            propertiesLabel.setFont(new Font("Arial", Font.PLAIN, 9));
            propertiesLabel.setForeground(new Color(100, 100, 100));
            propertiesLabel.setName("props_" + i); // For easy lookup later

            serverBox.add(topRow, BorderLayout.NORTH);
            serverBox.add(propertiesLabel, BorderLayout.SOUTH);

            serverPanel.add(serverBox);
        }

        JScrollPane scrollPane = new JScrollPane(serverPanel);
        scrollPane.setPreferredSize(new Dimension(480, 450));

        mainPanel.add(title, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createGameTheoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Game Theory Analysis");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        gameTheoryArea = new JTextArea();
        gameTheoryArea.setEditable(false);
        gameTheoryArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        gameTheoryArea.setBackground(new Color(255, 255, 240));
        gameTheoryArea.setText("Configure your simulation parameters and click 'Run Full Simulation'\n\n" +
                "AVAILABLE OPTIONS:\n" +
                "• 7 Leader Strategies\n" +
                "• 5 Allocation Algorithms\n" +
                "• 5 Load Patterns\n" +
                "• 4 Server Type Configurations\n" +
                "• Dynamic Pricing & Server Failures\n" +
                "• Customizable Economic Parameters\n\n" +
                "Each combination creates different equilibria!");

        JScrollPane scrollPane = new JScrollPane(gameTheoryArea);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(52, 73, 94));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(220, 0));

        JLabel title = new JLabel("System Metrics");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        energyLabel = createMetricLabel("Energy: 0 W", new Color(52, 152, 219));
        costLabel = createMetricLabel("Cost: $0.00", new Color(230, 126, 34));
        revenueLabel = createMetricLabel("Revenue: $0.00", new Color(26, 188, 156));
        profitLabel = createMetricLabel("Profit: $0.00", new Color(46, 204, 113));
        responseTimeLabel = createMetricLabel("Response: 0 ms", new Color(155, 89, 182));
        activeServersLabel = createMetricLabel("Active: 0/" + numServers, new Color(243, 156, 18));
        equilibriumLabel = createMetricLabel("Equilibrium: Pending", new Color(149, 165, 166));

        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(energyLabel);
        panel.add(costLabel);
        panel.add(revenueLabel);
        panel.add(profitLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(responseTimeLabel);
        panel.add(activeServersLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(equilibriumLabel);

        return panel;
    }

    private JLabel createMetricLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(5, 0, 0, 0));
        return label;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(0, 110));

        JLabel title = new JLabel("Simulation Log");
        title.setFont(new Font("Arial", Font.BOLD, 12));
        title.setBorder(new EmptyBorder(0, 0, 5, 0));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        logArea.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane(logArea);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void initializeSimulation() {
        Random random = new Random();

        // Initialize servers based on type
        servers = new Server[MAX_SERVERS];
        String serverType = (String) serverTypeComboBox.getSelectedItem();

        for (int i = 0; i < MAX_SERVERS; i++) {
            double procRate, idlePow, busyPow;
            String type;

            switch (serverType) {
                case "Heterogeneous":
                    procRate = 80 + random.nextInt(80);
                    idlePow = 40 + random.nextInt(30);
                    busyPow = 150 + random.nextInt(100);
                    type = (i % 3 == 0) ? "High-Perf" : (i % 3 == 1) ? "Standard" : "Efficient";
                    break;
                case "High-Performance Mix":
                    procRate = 120 + random.nextInt(40);
                    idlePow = 60 + random.nextInt(20);
                    busyPow = 200 + random.nextInt(50);
                    type = "High-Perf";
                    break;
                case "Energy-Efficient Mix":
                    procRate = 90 + random.nextInt(30);
                    idlePow = 30 + random.nextInt(20);
                    busyPow = 120 + random.nextInt(40);
                    type = "Efficient";
                    break;
                default: // Homogeneous
                    procRate = 100 + random.nextInt(20);
                    idlePow = 50;
                    busyPow = 200;
                    type = "Standard";
            }

            servers[i] = new Server(i, procRate, idlePow, busyPow, type);

            // Simulate random failures if enabled
            if (enableServerFailuresCheckBox.isSelected() && random.nextDouble() < 0.1) {
                servers[i].failed = true;
            }
        }

        // Initialize schedulers
        schedulers = new Scheduler[numSchedulers];
        String pattern = (String) loadPatternComboBox.getSelectedItem();

        for (int i = 0; i < numSchedulers; i++) {
            double arrivalRate = 40 + random.nextInt(60);
            schedulers[i] = new Scheduler(i, arrivalRate, MAX_SERVERS, pattern);
        }

        // Update server properties display
        updateServerPropertiesDisplay();

        // Update visibility to match current server count
        updateServerPanelVisibility();

        log("System initialized: " + numServers + " servers, " + numSchedulers + " schedulers");
        log("Configuration: " + serverType + " servers, " + pattern + " load pattern");
    }

    private void updateServerPropertiesDisplay() {
        for (int i = 0; i < MAX_SERVERS && i < servers.length; i++) {
            Server server = servers[i];

            // Find the properties label for this server
            Component serverBox = serverPanel.getComponent(i);
            if (serverBox instanceof JPanel) {
                JPanel panel = (JPanel) serverBox;
                Component[] components = panel.getComponents();
                for (Component comp : components) {
                    if (comp instanceof JLabel) {
                        JLabel label = (JLabel) comp;
                        if (label.getName() != null && label.getName().equals("props_" + i)) {
                            // Build properties string
                            String props = String.format(
                                    "Type: %s | Rate: %.0f t/s | Idle: %.0fW | Busy: %.0fW | Reliability: %.2f | Temp: %.1f°C",
                                    server.type,
                                    server.processingRate,
                                    server.idlePower,
                                    server.busyPower,
                                    server.reliability,
                                    server.temperature
                            );
                            label.setText(props);

                            // Color code by type
                            if (server.type.contains("High-Perf")) {
                                label.setForeground(new Color(231, 76, 60)); // Red for high-performance
                            } else if (server.type.contains("Efficient")) {
                                label.setForeground(new Color(39, 174, 96)); // Green for efficient
                            } else {
                                label.setForeground(new Color(52, 152, 219)); // Blue for standard
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private void runFullSimulation() {
        log("=== Starting Full Simulation ===");
        log("Strategy: " + strategyComboBox.getSelectedItem());
        log("Algorithm: " + allocationAlgorithmComboBox.getSelectedItem());

        // Update configurations
        loadPattern = (String) loadPatternComboBox.getSelectedItem();
        allocationAlgorithm = (String) allocationAlgorithmComboBox.getSelectedItem();
        optimizationGoal = (String) optimizationGoalComboBox.getSelectedItem();
        dynamicPricing = enableDynamicPricingCheckBox.isSelected();
        qosEnabled = enableQoSCheckBox.isSelected();

        // Reinitialize with current settings
        initializeSimulation();

        // Update scheduler loads
        for (int i = 0; i < numSchedulers; i++) {
            schedulers[i].updateLoad(loadPattern, 0);
        }

        gameEngine = new GameTheoryEngine(servers, schedulers,
                (String) strategyComboBox.getSelectedItem(),
                optimizationGoal, dynamicPricing,
                energyCostPerWatt, slaPenaltyPerMs, revenuePerTask);

        StringBuilder fullLog = new StringBuilder();

        // Step 1: Leader Decision
        fullLog.append(gameEngine.step1_LeaderDecision()).append("\n\n");

        // Step 2: Followers Observe
        fullLog.append(gameEngine.step2_FollowersObserve()).append("\n\n");

        // Step 3: Best Response (multiple iterations)
        for (int i = 1; i <= maxIterations; i++) {
            fullLog.append(gameEngine.step3_FollowersBestResponse(i, allocationAlgorithm)).append("\n\n");
        }

        // Step 4: Nash Equilibrium
        fullLog.append(gameEngine.step4_NashEquilibrium(convergenceThreshold)).append("\n\n");

        // Step 5: Results
        fullLog.append(gameEngine.step5_SystemResults());

        gameTheoryArea.setText(fullLog.toString());
        updateUI();

        log("Simulation completed - " + maxIterations + " iterations");
        equilibriumLabel.setText("Equilibrium: REACHED ✓");
        equilibriumLabel.setForeground(new Color(39, 174, 96));
    }

    private void runStepMode() {
        if (!isStepMode) {
            isStepMode = true;
            currentStep = 0;

            loadPattern = (String) loadPatternComboBox.getSelectedItem();
            allocationAlgorithm = (String) allocationAlgorithmComboBox.getSelectedItem();
            optimizationGoal = (String) optimizationGoalComboBox.getSelectedItem();
            dynamicPricing = enableDynamicPricingCheckBox.isSelected();

            initializeSimulation();

            for (int i = 0; i < numSchedulers; i++) {
                schedulers[i].updateLoad(loadPattern, 0);
            }

            gameEngine = new GameTheoryEngine(servers, schedulers,
                    (String) strategyComboBox.getSelectedItem(),
                    optimizationGoal, dynamicPricing,
                    energyCostPerWatt, slaPenaltyPerMs, revenuePerTask);

            stepButton.setText("Next Step");
            log("Step-by-step mode activated");
        }

        String stepResult = "";
        switch (currentStep) {
            case 0:
                stepResult = gameEngine.step1_LeaderDecision();
                updateUI();
                log("Step 1: Leader decision");
                break;
            case 1:
                stepResult = gameEngine.step2_FollowersObserve();
                log("Step 2: Followers observe");
                break;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                if (currentStep - 2 < maxIterations) {
                    stepResult = gameEngine.step3_FollowersBestResponse(currentStep - 1, allocationAlgorithm);
                    updateUI();
                    log("Step " + (currentStep + 1) + ": Best response iteration " + (currentStep - 1));
                } else {
                    currentStep = maxIterations + 1;
                }
                break;
            case 7:
                stepResult = gameEngine.step4_NashEquilibrium(convergenceThreshold);
                equilibriumLabel.setText("Equilibrium: REACHED ✓");
                equilibriumLabel.setForeground(new Color(39, 174, 96));
                log("Equilibrium check completed");
                break;
            case 8:
                stepResult = gameEngine.step5_SystemResults();
                updateUI();
                log("Final results calculated");
                stepButton.setText("Restart Steps");
                isStepMode = false;
                currentStep = -1;
                break;
        }

        gameTheoryArea.setText(stepResult);
        currentStep++;
    }

    private void updateUI() {
        double totalEnergy = 0;
        double totalResponseTime = 0;
        double totalCost = 0;
        double totalRevenue = 0;
        int activeCount = 0;

        for (int i = 0; i < numServers && i < MAX_SERVERS; i++) {
            Server server = servers[i];

            if (server.failed) {
                serverLoadBars[i].setValue(0);
                serverLoadBars[i].setString("FAILED");
                serverStatusLabels[i].setForeground(Color.BLACK);
                serverUtilityLabels[i].setText("OFFLINE");
                serverUtilityLabels[i].setForeground(Color.BLACK);
                continue;
            }

            if (server.isActive) {
                activeCount++;
                double serverLoad = 0;
                for (int j = 0; j < numSchedulers; j++) {
                    serverLoad += schedulers[j].allocation[server.id];
                }
                server.currentLoad = serverLoad;

                double power = server.utilizationPower(serverLoad);
                totalEnergy += power;
                totalResponseTime += serverLoad / server.processingRate;
                totalCost += power * energyCostPerWatt;

                server.utility = server.calculateUtility(serverLoad, energyCostPerWatt,
                        slaPenaltyPerMs, revenuePerTask);
                server.updateTemperature(serverLoad);

                double utilization = Math.min(serverLoad / server.processingRate, 1.0);
                int percentage = (int) (utilization * 100);
                serverLoadBars[i].setValue(percentage);
                serverLoadBars[i].setString(percentage + "%");
                serverStatusLabels[i].setForeground(Color.GREEN);
                serverUtilityLabels[i].setText("$" + String.format("%.2f", server.utility));

                if (server.utility >= 0) {
                    serverUtilityLabels[i].setForeground(new Color(39, 174, 96));
                } else {
                    serverUtilityLabels[i].setForeground(new Color(231, 76, 60));
                }
            } else {
                serverLoadBars[i].setValue(0);
                serverLoadBars[i].setString("OFF");
                serverStatusLabels[i].setForeground(Color.RED);
                serverUtilityLabels[i].setText("$0.00");
                serverUtilityLabels[i].setForeground(Color.GRAY);
            }

            // Update properties display with current temperature
            updateSingleServerProperties(i, server);
        }

        for (int i = 0; i < numSchedulers; i++) {
            totalRevenue += schedulers[i].currentLoad * revenuePerTask;
        }

        double profit = totalRevenue - totalCost;

        energyLabel.setText("Energy: " + String.format("%.2f", totalEnergy) + " W");
        costLabel.setText("Cost: $" + String.format("%.2f", totalCost));
        revenueLabel.setText("Revenue: $" + String.format("%.2f", totalRevenue));
        profitLabel.setText("Profit: $" + String.format("%.2f", profit));

        if (profit >= 0) {
            profitLabel.setForeground(new Color(46, 204, 113));
        } else {
            profitLabel.setForeground(new Color(231, 76, 60));
        }

        responseTimeLabel.setText("Response: " +
                String.format("%.4f", (totalResponseTime / numSchedulers) * 1000) + " ms");
        activeServersLabel.setText("Active: " + activeCount + "/" + numServers);
    }

    private void updateSingleServerProperties(int index, Server server) {
        // Update just the properties label for a single server with current values
        Component serverBox = serverPanel.getComponent(index);
        if (serverBox instanceof JPanel) {
            JPanel panel = (JPanel) serverBox;
            Component[] components = panel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    if (label.getName() != null && label.getName().equals("props_" + index)) {
                        String props = String.format(
                                "Type: %s | Rate: %.0f t/s | Idle: %.0fW | Busy: %.0fW | Reliability: %.2f | Temp: %.1f°C | Load: %.1f t/s",
                                server.type,
                                server.processingRate,
                                server.idlePower,
                                server.busyPower,
                                server.reliability,
                                server.temperature,
                                server.currentLoad
                        );
                        label.setText(props);

                        // Temperature-based color coding
                        if (server.temperature > 70) {
                            label.setForeground(new Color(231, 76, 60)); // Red - hot
                        } else if (server.temperature > 50) {
                            label.setForeground(new Color(230, 126, 34)); // Orange - warm
                        } else if (server.type.contains("Efficient")) {
                            label.setForeground(new Color(39, 174, 96)); // Green - efficient
                        } else {
                            label.setForeground(new Color(52, 152, 219)); // Blue - normal
                        }
                        break;
                    }
                }
            }
        }
    }

    private void updateServerPanelVisibility() {
        // Show/hide server panels based on current numServers value
        for (int i = 0; i < MAX_SERVERS; i++) {
            Component comp = serverPanel.getComponent(i);
            comp.setVisible(i < numServers);
        }
        serverPanel.revalidate();
        serverPanel.repaint();

        // Update the active servers label
        activeServersLabel.setText("Active: 0/" + numServers);

        log("Server count adjusted to: " + numServers);
    }

    private void resetSimulation() {
        log("Resetting simulation");

        for (int i = 0; i < MAX_SERVERS; i++) {
            serverLoadBars[i].setValue(0);
            serverLoadBars[i].setString("0%");
            serverStatusLabels[i].setForeground(Color.GRAY);
            serverUtilityLabels[i].setText("$0.00");
            serverUtilityLabels[i].setForeground(Color.GRAY);
        }

        energyLabel.setText("Energy: 0 W");
        costLabel.setText("Cost: $0.00");
        revenueLabel.setText("Revenue: $0.00");
        profitLabel.setText("Profit: $0.00");
        responseTimeLabel.setText("Response: 0 ms");
        activeServersLabel.setText("Active: 0/" + numServers);
        equilibriumLabel.setText("Equilibrium: Pending");
        equilibriumLabel.setForeground(new Color(149, 165, 166));

        gameTheoryArea.setText("Configure your simulation parameters and click 'Run Full Simulation'\n\n" +
                "AVAILABLE OPTIONS:\n" +
                "• 7 Leader Strategies\n" +
                "• 5 Allocation Algorithms\n" +
                "• 5 Load Patterns\n" +
                "• 4 Server Type Configurations\n" +
                "• Dynamic Pricing & Server Failures\n" +
                "• Customizable Economic Parameters\n\n" +
                "Each combination creates different equilibria!");

        isStepMode = false;
        currentStep = 0;
        stepButton.setText("Step Mode");

        initializeSimulation();
        log("Reset complete");
    }

    private void log(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        logArea.append("[" + sdf.format(new Date()) + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DataCenterAdvancedSimulation());
    }
}