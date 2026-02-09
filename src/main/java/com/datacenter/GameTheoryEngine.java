package com.datacenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class GameTheoryEngine {
    private Server[] servers;
    private Scheduler[] schedulers;
    private String leaderStrategy;
    private ArrayList<String> gameLog;
    private String optimizationGoal;
    private boolean dynamicPricing;
    private double energyCost;
    private double slaPenalty;
    private double revenue;

    public GameTheoryEngine(Server[] servers, Scheduler[] schedulers, String strategy,
                            String goal, boolean dynPricing, double eCost, double slaPen, double rev) {
        this.servers = servers;
        this.schedulers = schedulers;
        this.leaderStrategy = strategy;
        this.gameLog = new ArrayList<>();
        this.optimizationGoal = goal;
        this.dynamicPricing = dynPricing;
        this.energyCost = eCost;
        this.slaPenalty = slaPen;
        this.revenue = rev;
    }

    public String step1_LeaderDecision() {
        gameLog.clear();
        gameLog.add("=== STEP 1: LEADER DECISION ===");
        gameLog.add("Strategy: " + leaderStrategy);
        gameLog.add("Optimization Goal: " + optimizationGoal);
        gameLog.add("");

        switch (leaderStrategy) {
            case "Energy Efficient":
                energyEfficientStrategy();
                break;
            case "Load Balanced":
                loadBalancedStrategy();
                break;
            case "Profit Maximizing":
                profitMaximizingStrategy();
                break;
            case "QoS Focused":
                qosFocusedStrategy();
                break;
            case "Adaptive":
                adaptiveStrategy();
                break;
            case "Conservative":
                conservativeStrategy();
                break;
            case "Aggressive":
                aggressiveStrategy();
                break;
        }

        if (dynamicPricing) {
            updateDynamicPricing();
            gameLog.add("Dynamic pricing enabled: Updated energy cost and SLA penalty");
        }

        int activeCount = 0;
        for (Server s : servers) {
            if (s.isActive) activeCount++;
        }
        gameLog.add("Result: " + activeCount + " servers activated");

        return String.join("\n", gameLog);
    }

    private void energyEfficientStrategy() {
        double totalLoad = 0;
        for (Scheduler s : schedulers) {
            totalLoad += s.currentLoad;
        }

        int serversNeeded = (int) Math.ceil(totalLoad / 100.0);
        serversNeeded = Math.min(serversNeeded, servers.length);

        // Activate most energy-efficient servers first
        Arrays.sort(servers, Comparator.comparingDouble(s -> s.idlePower));

        for (int i = 0; i < servers.length; i++) {
            servers[i].isActive = i < serversNeeded;
        }

        gameLog.add("Decision: Activate " + serversNeeded + " most energy-efficient servers");
        gameLog.add("Criterion: Minimize total energy consumption");
    }

    private void loadBalancedStrategy() {
        int activeCount = (int) (servers.length * 0.7);

        // Activate servers with best processing rates
        Arrays.sort(servers, Comparator.comparingDouble(s -> -s.processingRate));

        for (int i = 0; i < servers.length; i++) {
            servers[i].isActive = i < activeCount;
        }

        gameLog.add("Decision: Activate 70% of servers (" + activeCount + ")");
        gameLog.add("Criterion: Balance load across multiple servers");
    }

    private void profitMaximizingStrategy() {
        double maxProfit = Double.NEGATIVE_INFINITY;
        boolean[] bestConfig = new boolean[servers.length];

        // Try different configurations (heuristic for large spaces)
        int numConfigs = Math.min(100, 1 << servers.length);
        Random rand = new Random();

        for (int trial = 0; trial < numConfigs; trial++) {
            // Random configuration
            for (int i = 0; i < servers.length; i++) {
                servers[i].isActive = rand.nextBoolean() || (trial == 0 && i < servers.length / 2);
            }

            double profit = simulateProfit();
            if (profit > maxProfit) {
                maxProfit = profit;
                for (int i = 0; i < servers.length; i++) {
                    bestConfig[i] = servers[i].isActive;
                }
            }
        }

        int activeCount = 0;
        for (int i = 0; i < servers.length; i++) {
            servers[i].isActive = bestConfig[i];
            if (bestConfig[i]) activeCount++;
        }

        gameLog.add("Decision: Activate " + activeCount + " servers");
        gameLog.add("Expected Profit: $" + String.format("%.2f", maxProfit));
        gameLog.add("Criterion: Maximize system profit");
    }

    private void qosFocusedStrategy() {
        // Activate high-reliability, high-performance servers
        Arrays.sort(servers, Comparator.comparingDouble(s ->
                -(s.processingRate * s.reliability)));

        double totalLoad = 0;
        for (Scheduler s : schedulers) {
            totalLoad += s.currentLoad;
        }

        int serversNeeded = (int) Math.ceil(totalLoad / 80.0); // Conservative capacity

        for (int i = 0; i < servers.length; i++) {
            servers[i].isActive = i < serversNeeded;
        }

        gameLog.add("Decision: Activate " + serversNeeded + " high-QoS servers");
        gameLog.add("Criterion: Maximize reliability and minimize response time");
    }

    private void adaptiveStrategy() {
        // Adapt based on current system state
        double avgLoad = 0;
        for (Scheduler s : schedulers) {
            avgLoad += s.currentLoad;
        }
        avgLoad /= schedulers.length;

        if (avgLoad < 50) {
            energyEfficientStrategy();
            gameLog.add("Adaptive: Low load → Energy efficient mode");
        } else if (avgLoad > 100) {
            qosFocusedStrategy();
            gameLog.add("Adaptive: High load → QoS focused mode");
        } else {
            loadBalancedStrategy();
            gameLog.add("Adaptive: Medium load → Load balanced mode");
        }
    }

    private void conservativeStrategy() {
        // Activate more servers than needed for safety
        double totalLoad = 0;
        for (Scheduler s : schedulers) {
            totalLoad += s.currentLoad;
        }

        int serversNeeded = (int) Math.ceil(totalLoad / 70.0); // Extra capacity
        serversNeeded = Math.min(servers.length, serversNeeded + 2);

        for (int i = 0; i < servers.length; i++) {
            servers[i].isActive = i < serversNeeded;
        }

        gameLog.add("Decision: Activate " + serversNeeded + " servers (conservative)");
        gameLog.add("Criterion: Ensure capacity margin for unexpected load");
    }

    private void aggressiveStrategy() {
        // Activate minimum servers, push to limits
        double totalLoad = 0;
        for (Scheduler s : schedulers) {
            totalLoad += s.currentLoad;
        }

        int serversNeeded = (int) Math.ceil(totalLoad / 120.0); // Tight capacity

        for (int i = 0; i < servers.length; i++) {
            servers[i].isActive = i < serversNeeded;
        }

        gameLog.add("Decision: Activate " + serversNeeded + " servers (aggressive)");
        gameLog.add("Criterion: Minimize costs, accept higher utilization");
    }

    private void updateDynamicPricing() {
        // Simulate time-of-day pricing
        int hour = (int) (System.currentTimeMillis() / 3600000) % 24;

        if (hour >= 9 && hour <= 17) {
            // Peak hours
            energyCost *= 1.5;
            slaPenalty *= 1.2;
            gameLog.add("Peak hours detected: Higher costs applied");
        } else {
            // Off-peak
            energyCost *= 0.8;
            gameLog.add("Off-peak hours: Reduced energy costs");
        }
    }

    private double simulateProfit() {
        double revenue = 0;
        double cost = 0;

        for (Scheduler s : schedulers) {
            revenue += s.currentLoad * this.revenue;
        }

        for (Server s : servers) {
            if (s.isActive) {
                cost += s.idlePower * energyCost;
            }
        }

        return revenue - cost;
    }

    public String step2_FollowersObserve() {
        gameLog.clear();
        gameLog.add("=== STEP 2: FOLLOWERS OBSERVE ===");

        StringBuilder activeList = new StringBuilder("Active Servers: [");
        int activeCount = 0;
        for (int i = 0; i < servers.length; i++) {
            if (servers[i].isActive && !servers[i].failed) {
                if (activeCount > 0) activeList.append(", ");
                activeList.append(i).append(" (").append(servers[i].type).append(")");
                activeCount++;
            }
        }
        activeList.append("]");

        gameLog.add(activeList.toString());
        gameLog.add("Available Capacity: " + (activeCount * 100) + " tasks/s");
        gameLog.add("");

        for (Scheduler scheduler : schedulers) {
            gameLog.add("Scheduler " + scheduler.id + ":");
            gameLog.add("  Load: " + String.format("%.1f", scheduler.currentLoad) + " tasks/s");
            gameLog.add("  Priority: " + String.format("%.2f", scheduler.priority));
            gameLog.add("  Pattern: " + scheduler.loadPattern);
        }

        return String.join("\n", gameLog);
    }

    public String step3_FollowersBestResponse(int iteration, String algorithm) {
        gameLog.clear();
        gameLog.add("=== STEP 3: ITERATION " + iteration + " ===");
        gameLog.add("Algorithm: " + algorithm);
        gameLog.add("");

        for (Scheduler scheduler : schedulers) {
            scheduler.bestResponse(servers, schedulers, algorithm);
            scheduler.calculateUtility(servers, energyCost, slaPenalty, revenue);

            gameLog.add("Scheduler " + scheduler.id + ":");
            gameLog.add("  Utility: $" + String.format("%.2f", scheduler.utility));
            gameLog.add("  Load: " + String.format("%.1f", scheduler.currentLoad) + " tasks/s");

            ArrayList<String> allocations = new ArrayList<>();
            for (int i = 0; i < servers.length; i++) {
                if (scheduler.allocation[i] > 1.0) {
                    allocations.add("S" + i + ":" + String.format("%.0f", scheduler.allocation[i]));
                }
            }
            if (!allocations.isEmpty()) {
                gameLog.add("  Allocations: " + String.join(", ", allocations));
            }
        }

        return String.join("\n", gameLog);
    }

    public String step4_NashEquilibrium(double threshold) {
        gameLog.clear();
        gameLog.add("=== STEP 4: EQUILIBRIUM CHECK ===");
        gameLog.add("Convergence Threshold: " + threshold);
        gameLog.add("");

        double[] serverLoads = new double[servers.length];
        for (Scheduler scheduler : schedulers) {
            for (int i = 0; i < servers.length; i++) {
                serverLoads[i] += scheduler.allocation[i];
            }
        }

        boolean isNashEquilibrium = true;
        double systemUtility = 0;
        double maxDeviation = 0;

        for (Scheduler scheduler : schedulers) {
            double currentUtility = scheduler.utility;
            systemUtility += currentUtility;

            double[] originalAllocation = scheduler.allocation.clone();
            scheduler.bestResponse(servers, schedulers, "Best Response");
            scheduler.calculateUtility(servers, energyCost, slaPenalty, revenue);

            double newUtility = scheduler.utility;
            double deviation = newUtility - currentUtility;
            maxDeviation = Math.max(maxDeviation, deviation);

            if (deviation > threshold) {
                isNashEquilibrium = false;
                gameLog.add("Scheduler " + scheduler.id + " can improve by $" +
                        String.format("%.2f", deviation));
            }

            scheduler.allocation = originalAllocation;
        }

        gameLog.add("");
        if (isNashEquilibrium) {
            gameLog.add("✓ NASH EQUILIBRIUM REACHED!");
            gameLog.add("All schedulers are satisfied with their allocation");
        } else {
            gameLog.add("✗ Not yet converged (max deviation: $" +
                    String.format("%.2f", maxDeviation) + ")");
        }

        gameLog.add("");
        gameLog.add("System Metrics:");
        gameLog.add("  Social Welfare: $" + String.format("%.2f", systemUtility));
        gameLog.add("  Load Balance: " + calculateLoadBalance(serverLoads));
        gameLog.add("  Avg Utilization: " + calculateAvgUtilization(serverLoads));

        return String.join("\n", gameLog);
    }

    private String calculateLoadBalance(double[] loads) {
        double sum = 0;
        double max = 0;
        int activeCount = 0;

        for (int i = 0; i < loads.length; i++) {
            if (servers[i].isActive) {
                sum += loads[i];
                max = Math.max(max, loads[i]);
                activeCount++;
            }
        }

        if (activeCount == 0 || max == 0) return "N/A";

        double avg = sum / activeCount;
        double balance = (avg / max) * 100;
        return String.format("%.1f%%", balance);
    }

    private String calculateAvgUtilization(double[] loads) {
        double totalUtil = 0;
        int activeCount = 0;

        for (int i = 0; i < loads.length; i++) {
            if (servers[i].isActive) {
                double util = Math.min(loads[i] / servers[i].processingRate, 1.0);
                totalUtil += util;
                activeCount++;
            }
        }

        if (activeCount == 0) return "N/A";
        return String.format("%.1f%%", (totalUtil / activeCount) * 100);
    }

    public String step5_SystemResults() {
        gameLog.clear();
        gameLog.add("=== STEP 5: FINAL RESULTS ===");

        double totalEnergy = 0;
        double totalResponseTime = 0;
        double totalCost = 0;
        double totalRevenue = 0;
        int activeCount = 0;
        int failedCount = 0;

        for (Server server : servers) {
            if (server.failed) {
                failedCount++;
                continue;
            }

            if (server.isActive) {
                activeCount++;
                double serverLoad = 0;
                for (Scheduler scheduler : schedulers) {
                    serverLoad += scheduler.allocation[server.id];
                }
                server.currentLoad = serverLoad;

                double power = server.utilizationPower(serverLoad);
                totalEnergy += power;
                totalResponseTime += serverLoad / server.processingRate;
                totalCost += power * energyCost;

                server.utility = server.calculateUtility(serverLoad, energyCost, slaPenalty, revenue);
                server.updateTemperature(serverLoad);
            }
        }

        for (Scheduler scheduler : schedulers) {
            totalRevenue += scheduler.currentLoad * revenue;
        }

        double profit = totalRevenue - totalCost;

        gameLog.add("Performance Metrics:");
        gameLog.add("  Energy: " + String.format("%.2f", totalEnergy) + " W");
        gameLog.add("  Cost: $" + String.format("%.2f", totalCost));
        gameLog.add("  Revenue: $" + String.format("%.2f", totalRevenue));
        gameLog.add("  Profit: $" + String.format("%.2f", profit));
        gameLog.add("  Avg Response: " + String.format("%.4f",
                (totalResponseTime / (schedulers.length > 0 ? schedulers.length : 1)) * 1000) + " ms");
        gameLog.add("  Active Servers: " + activeCount + "/" + servers.length);
        if (failedCount > 0) {
            gameLog.add("  Failed Servers: " + failedCount);
        }
        gameLog.add("");

        double socialWelfare = 0;
        for (Scheduler s : schedulers) {
            socialWelfare += s.utility;
        }
        gameLog.add("Social Welfare: $" + String.format("%.2f", socialWelfare));

        double efficiency = (profit > 0) ? (socialWelfare / profit) * 100 : 0;
        gameLog.add("System Efficiency: " + String.format("%.1f%%", efficiency));

        return String.join("\n", gameLog);
    }
}
