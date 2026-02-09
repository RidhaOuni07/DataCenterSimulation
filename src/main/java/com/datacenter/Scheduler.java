package com.datacenter;

import java.util.Arrays;
import java.util.Random;

public class Scheduler {
    public int id;
    public double arrivalRate;
    public double[] allocation;
    public double utility;
    public String strategy;
    public double priority; // For QoS
    public String loadPattern;
    public double currentLoad; // Can vary over time

    public Scheduler(int id, double arrivalRate, int numServers, String pattern) {
        this.id = id;
        this.arrivalRate = arrivalRate;
        this.allocation = new double[numServers];
        this.utility = 0;
        this.strategy = "Best Response";
        this.priority = 0.5 + Math.random() * 0.5; // 0.5 to 1.0
        this.loadPattern = pattern;
        this.currentLoad = arrivalRate;
    }

    public void updateLoad(String pattern, int timeStep) {
        switch (pattern) {
            case "Uniform":
                currentLoad = arrivalRate;
                break;
            case "Bursty":
                // Periodic bursts
                currentLoad = arrivalRate * (1.0 + 0.5 * Math.sin(timeStep * 0.5));
                break;
            case "Peak Hours":
                // Simulate daily pattern
                double hour = (timeStep % 24) / 24.0;
                currentLoad = arrivalRate * (0.5 + 1.5 * Math.pow(Math.sin(Math.PI * hour), 2));
                break;
            case "Random":
                currentLoad = arrivalRate * (0.5 + Math.random());
                break;
            case "Decreasing":
                currentLoad = arrivalRate * Math.exp(-timeStep * 0.1);
                break;
        }
    }

    public void bestResponse(Server[] servers, Scheduler[] otherSchedulers, String algorithm) {
        double[] serverLoads = new double[servers.length];
        for (Scheduler other : otherSchedulers) {
            if (other.id != this.id) {
                for (int i = 0; i < servers.length; i++) {
                    serverLoads[i] += other.allocation[i];
                }
            }
        }

        switch (algorithm) {
            case "Best Response":
                bestResponseGreedy(servers, serverLoads);
                break;
            case "Proportional Fair":
                proportionalFairAllocation(servers, serverLoads);
                break;
            case "Water Filling":
                waterFillingAllocation(servers, serverLoads);
                break;
            case "Min-Max Fair":
                minMaxFairAllocation(servers, serverLoads);
                break;
            case "Random":
                randomAllocation(servers);
                break;
        }
    }

    private void bestResponseGreedy(Server[] servers, double[] serverLoads) {
        double remainingLoad = currentLoad;
        double[] newAllocation = new double[servers.length];

        while (remainingLoad > 0.01) {
            int bestServer = -1;
            double minMarginalCost = Double.MAX_VALUE;

            for (int i = 0; i < servers.length; i++) {
                if (servers[i].isActive && !servers[i].failed) {
                    double totalLoad = serverLoads[i] + newAllocation[i];
                    double marginalCost = calculateMarginalCost(servers[i], totalLoad);

                    if (marginalCost < minMarginalCost) {
                        minMarginalCost = marginalCost;
                        bestServer = i;
                    }
                }
            }

            if (bestServer == -1) break;

            double increment = Math.min(remainingLoad, 10.0);
            newAllocation[bestServer] += increment;
            remainingLoad -= increment;
        }

        this.allocation = newAllocation;
    }

    private void proportionalFairAllocation(Server[] servers, double[] serverLoads) {
        // Allocate proportional to available capacity
        double[] capacities = new double[servers.length];
        double totalCapacity = 0;

        for (int i = 0; i < servers.length; i++) {
            if (servers[i].isActive && !servers[i].failed) {
                capacities[i] = Math.max(0, servers[i].processingRate - serverLoads[i]);
                totalCapacity += capacities[i];
            }
        }

        if (totalCapacity > 0) {
            for (int i = 0; i < servers.length; i++) {
                allocation[i] = currentLoad * (capacities[i] / totalCapacity);
            }
        }
    }

    private void waterFillingAllocation(Server[] servers, double[] serverLoads) {
        // Water-filling algorithm: fill to equal levels
        Arrays.fill(allocation, 0);
        double remainingLoad = currentLoad;

        while (remainingLoad > 0.01) {
            int lowestServer = -1;
            double lowestLevel = Double.MAX_VALUE;

            for (int i = 0; i < servers.length; i++) {
                if (servers[i].isActive && !servers[i].failed) {
                    double currentLevel = (serverLoads[i] + allocation[i]) / servers[i].processingRate;
                    if (currentLevel < lowestLevel && currentLevel < 0.9) {
                        lowestLevel = currentLevel;
                        lowestServer = i;
                    }
                }
            }

            if (lowestServer == -1) break;

            double increment = Math.min(remainingLoad, 5.0);
            allocation[lowestServer] += increment;
            remainingLoad -= increment;
        }
    }

    private void minMaxFairAllocation(Server[] servers, double[] serverLoads) {
        // Minimize maximum server load
        Arrays.fill(allocation, 0);
        double increment = 1.0;

        for (int step = 0; step < currentLoad / increment; step++) {
            int minLoadServer = -1;
            double minLoad = Double.MAX_VALUE;

            for (int i = 0; i < servers.length; i++) {
                if (servers[i].isActive && !servers[i].failed) {
                    double load = serverLoads[i] + allocation[i];
                    if (load < minLoad) {
                        minLoad = load;
                        minLoadServer = i;
                    }
                }
            }

            if (minLoadServer != -1) {
                allocation[minLoadServer] += increment;
            }
        }
    }

    private void randomAllocation(Server[] servers) {
        // Random allocation for comparison
        Arrays.fill(allocation, 0);
        Random rand = new Random();

        for (double load = 0; load < currentLoad; load += 1.0) {
            int randomServer;
            do {
                randomServer = rand.nextInt(servers.length);
            } while (!servers[randomServer].isActive || servers[randomServer].failed);

            allocation[randomServer] += 1.0;
        }
    }

    private double calculateMarginalCost(Server server, double currentLoad) {
        double energyCost = server.utilizationPower(currentLoad) * 0.12;
        double responseTime = currentLoad / server.processingRate;
        return energyCost + (responseTime * 1000 * 0.05);
    }

    public void calculateUtility(Server[] servers, double energyCost, double slaPenalty, double revenue) {
        double totalCost = 0;
        double totalRevenue = currentLoad * revenue;

        for (int i = 0; i < servers.length; i++) {
            if (allocation[i] > 0) {
                double serverEnergyCost = servers[i].utilizationPower(allocation[i]) * energyCost;
                double responseTime = allocation[i] / servers[i].processingRate;
                double sla = responseTime * 1000 * slaPenalty;
                totalCost += serverEnergyCost + sla;
            }
        }

        // Priority bonus for high-priority schedulers
        double priorityBonus = (priority > 0.8) ? totalRevenue * 0.1 : 0;

        this.utility = totalRevenue + priorityBonus - totalCost;
    }
}
