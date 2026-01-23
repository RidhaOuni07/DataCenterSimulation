package com.datacenter;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class DataCenterSimulationSwing extends JFrame {

    // Define constants
    private static final int NUM_SERVERS = 10;
    private static final int NUM_SCHEDULERS = 5;

    // UI Components
    private JPanel serverPanel;
    private JLabel energyLabel;
    private JLabel responseTimeLabel;
    private JLabel activeServersLabel;
    private JTextArea logArea;
    private JProgressBar[] serverLoadBars;
    private JLabel[] serverStatusLabels;
    private JButton runButton;
    private JButton resetButton;
    private JSlider schedulerSlider;
    private JSlider serverSlider;
    private JLabel schedulerValueLabel;
    private JLabel serverValueLabel;

    // Simulation objects
    private Server[] servers;
    private Scheduler[] schedulers;

    // Server class
    static class Server {
        int id;
        double processingRate;
        double idlePower;
        double busyPower;
        boolean isActive;

        public Server(int id, double processingRate, double idlePower, double busyPower) {
            this.id = id;
            this.processingRate = processingRate;
            this.idlePower = idlePower;
            this.busyPower = busyPower;
            this.isActive = true;
        }

        public double utilizationPower(double taskLoad) {
            double utilization = Math.min(taskLoad / processingRate, 1.0);
            return idlePower + utilization * (busyPower - idlePower);
        }
    }

    // Scheduler class
    static class Scheduler {
        int id;
        double arrivalRate;
        double[] allocation;

        public Scheduler(int id, double arrivalRate, int numServers) {
            this.id = id;
            this.arrivalRate = arrivalRate;
            this.allocation = new double[numServers];
        }

        public void allocateTasks(Server[] servers) {
            int activeCount = 0;
            for (Server server : servers) {
                if (server.isActive) activeCount++;
            }

            if (activeCount == 0) return;

            for (int i = 0; i < servers.length; i++) {
                if (servers[i].isActive) {
                    allocation[i] = arrivalRate / activeCount;
                } else {
                    allocation[i] = 0;
                }
            }
        }
    }

    public DataCenterSimulationSwing() {
        setTitle("Data Center Simulation Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Create panels
        add(createControlPanel(), BorderLayout.NORTH);
        add(createServerPanel(), BorderLayout.CENTER);
        add(createStatsPanel(), BorderLayout.EAST);
        add(createLogPanel(), BorderLayout.SOUTH);

        // Initialize simulation
        initializeSimulation();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(44, 62, 80));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Data Center Control Panel");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel controlBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlBox.setBackground(new Color(44, 62, 80));

        // Scheduler control
        JPanel schedulerPanel = new JPanel(new BorderLayout(5, 5));
        schedulerPanel.setBackground(new Color(44, 62, 80));
        schedulerValueLabel = new JLabel("Number of Schedulers: " + NUM_SCHEDULERS);
        schedulerValueLabel.setForeground(Color.WHITE);
        schedulerSlider = new JSlider(1, 10, NUM_SCHEDULERS);
        schedulerSlider.setMajorTickSpacing(1);
        schedulerSlider.setPaintTicks(true);
        schedulerSlider.setPaintLabels(true);
        schedulerSlider.setBackground(new Color(44, 62, 80));
        schedulerSlider.setForeground(Color.WHITE);
        schedulerSlider.addChangeListener(e -> {
            schedulerValueLabel.setText("Number of Schedulers: " + schedulerSlider.getValue());
        });
        schedulerPanel.add(schedulerValueLabel, BorderLayout.NORTH);
        schedulerPanel.add(schedulerSlider, BorderLayout.CENTER);

        // Server control
        JPanel serverPanel = new JPanel(new BorderLayout(5, 5));
        serverPanel.setBackground(new Color(44, 62, 80));
        serverValueLabel = new JLabel("Active Servers: " + NUM_SERVERS);
        serverValueLabel.setForeground(Color.WHITE);
        serverSlider = new JSlider(1, NUM_SERVERS, NUM_SERVERS);
        serverSlider.setMajorTickSpacing(1);
        serverSlider.setPaintTicks(true);
        serverSlider.setPaintLabels(true);
        serverSlider.setBackground(new Color(44, 62, 80));
        serverSlider.setForeground(Color.WHITE);
        serverSlider.addChangeListener(e -> {
            serverValueLabel.setText("Active Servers: " + serverSlider.getValue());
        });
        serverPanel.add(serverValueLabel, BorderLayout.NORTH);
        serverPanel.add(serverSlider, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(44, 62, 80));
        runButton = new JButton("Run Simulation");
        runButton.setBackground(new Color(39, 174, 96));
        runButton.setForeground(Color.WHITE);
        runButton.setFont(new Font("Arial", Font.BOLD, 12));
        runButton.setFocusPainted(false);
        runButton.addActionListener(e -> runSimulation());

        resetButton = new JButton("Reset");
        resetButton.setBackground(new Color(231, 76, 60));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFont(new Font("Arial", Font.BOLD, 12));
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> resetSimulation());

        buttonPanel.add(runButton);
        buttonPanel.add(resetButton);

        controlBox.add(schedulerPanel);
        controlBox.add(serverPanel);
        controlBox.add(buttonPanel);

        panel.add(title, BorderLayout.NORTH);
        panel.add(controlBox, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createServerPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(236, 240, 241));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Server Status");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        serverPanel = new JPanel();
        serverPanel.setLayout(new BoxLayout(serverPanel, BoxLayout.Y_AXIS));
        serverPanel.setBackground(Color.WHITE);

        serverLoadBars = new JProgressBar[NUM_SERVERS];
        serverStatusLabels = new JLabel[NUM_SERVERS];

        for (int i = 0; i < NUM_SERVERS; i++) {
            JPanel serverBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            serverBox.setBackground(Color.WHITE);
            serverBox.setBorder(new LineBorder(new Color(189, 195, 199), 1));

            JLabel statusIndicator = new JLabel("●");
            statusIndicator.setFont(new Font("Arial", Font.BOLD, 20));
            statusIndicator.setForeground(Color.GRAY);
            serverStatusLabels[i] = statusIndicator;

            JLabel serverLabel = new JLabel("Server " + i);
            serverLabel.setPreferredSize(new Dimension(80, 20));

            JProgressBar loadBar = new JProgressBar(0, 100);
            loadBar.setPreferredSize(new Dimension(300, 20));
            loadBar.setStringPainted(true);
            loadBar.setString("0%");
            serverLoadBars[i] = loadBar;

            serverBox.add(statusIndicator);
            serverBox.add(serverLabel);
            serverBox.add(loadBar);
            serverPanel.add(serverBox);
        }

        JScrollPane scrollPane = new JScrollPane(serverPanel);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        mainPanel.add(title, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(52, 73, 94));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(300, 0));

        JLabel title = new JLabel("Performance Metrics");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        energyLabel = new JLabel("Total Energy: 0 W");
        energyLabel.setFont(new Font("Arial", Font.BOLD, 14));
        energyLabel.setForeground(new Color(52, 152, 219));
        energyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        energyLabel.setBorder(new EmptyBorder(20, 0, 0, 0));

        responseTimeLabel = new JLabel("Avg Response Time: 0 ms");
        responseTimeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        responseTimeLabel.setForeground(new Color(46, 204, 113));
        responseTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        responseTimeLabel.setBorder(new EmptyBorder(20, 0, 0, 0));

        activeServersLabel = new JLabel("Active Servers: 0/" + NUM_SERVERS);
        activeServersLabel.setFont(new Font("Arial", Font.BOLD, 14));
        activeServersLabel.setForeground(new Color(243, 156, 18));
        activeServersLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        activeServersLabel.setBorder(new EmptyBorder(20, 0, 0, 0));

        panel.add(title);
        panel.add(energyLabel);
        panel.add(responseTimeLabel);
        panel.add(activeServersLabel);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(0, 150));

        JLabel title = new JLabel("Simulation Log");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setBorder(new EmptyBorder(0, 0, 5, 0));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane(logArea);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void initializeSimulation() {
        Random random = new Random();
        servers = new Server[NUM_SERVERS];

        for (int i = 0; i < NUM_SERVERS; i++) {
            servers[i] = new Server(
                i,
                100 + random.nextInt(50),
                50,
                200
            );
        }

        schedulers = new Scheduler[NUM_SCHEDULERS];
        for (int i = 0; i < NUM_SCHEDULERS; i++) {
            schedulers[i] = new Scheduler(i, 50 + random.nextInt(50), NUM_SERVERS);
        }

        log("Simulation initialized with " + NUM_SERVERS + " servers and " + NUM_SCHEDULERS + " schedulers.");
    }

    private void runSimulation() {
        log("Running simulation...");

        int numActiveServers = serverSlider.getValue();
        int numSchedulersToUse = schedulerSlider.getValue();

        // Update server status
        for (int i = 0; i < NUM_SERVERS; i++) {
            servers[i].isActive = i < numActiveServers;
        }

        // Reinitialize schedulers
        Random random = new Random();
        schedulers = new Scheduler[numSchedulersToUse];
        for (int i = 0; i < numSchedulersToUse; i++) {
            schedulers[i] = new Scheduler(i, 50 + random.nextInt(50), NUM_SERVERS);
        }

        // Allocate tasks
        for (Scheduler scheduler : schedulers) {
            scheduler.allocateTasks(servers);
        }

        // Calculate metrics
        double totalEnergyConsumption = 0;
        double totalResponseTime = 0;
        int activeCount = 0;

        for (int i = 0; i < NUM_SERVERS; i++) {
            Server server = servers[i];

            if (server.isActive) {
                activeCount++;
                double serverLoad = 0;
                for (Scheduler scheduler : schedulers) {
                    serverLoad += scheduler.allocation[server.id];
                }

                totalEnergyConsumption += server.utilizationPower(serverLoad);
                totalResponseTime += serverLoad / server.processingRate;

                // Update UI
                double utilization = Math.min(serverLoad / server.processingRate, 1.0);
                int percentage = (int) (utilization * 100);
                serverLoadBars[i].setValue(percentage);
                serverLoadBars[i].setString(percentage + "%");
                serverStatusLabels[i].setForeground(Color.GREEN);
            } else {
                serverLoadBars[i].setValue(0);
                serverLoadBars[i].setString("OFF");
                serverStatusLabels[i].setForeground(Color.RED);
            }
        }

        // Update statistics
        energyLabel.setText(String.format("Total Energy: %.2f W", totalEnergyConsumption));
        responseTimeLabel.setText(String.format("Avg Response Time: %.4f ms",
            (totalResponseTime / numSchedulersToUse) * 1000));
        activeServersLabel.setText("Active Servers: " + activeCount + "/" + NUM_SERVERS);

        log("Simulation completed. Active servers: " + activeCount);
        log("Energy consumption: " + String.format("%.2f", totalEnergyConsumption) + " W");
    }

    private void resetSimulation() {
        log("Resetting simulation...");

        for (int i = 0; i < NUM_SERVERS; i++) {
            serverLoadBars[i].setValue(0);
            serverLoadBars[i].setString("0%");
            serverStatusLabels[i].setForeground(Color.GRAY);
            servers[i].isActive = true;
        }

        energyLabel.setText("Total Energy: 0 W");
        responseTimeLabel.setText("Avg Response Time: 0 ms");
        activeServersLabel.setText("Active Servers: 0/" + NUM_SERVERS);

        serverSlider.setValue(NUM_SERVERS);
        schedulerSlider.setValue(NUM_SCHEDULERS);

        log("Simulation reset complete.");
    }

    private void log(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        logArea.append("[" + sdf.format(new Date()) + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DataCenterSimulationSwing());
    }
}
