package com.datacenter;

import java.util.Random;

public class Server {
    public int id;
    public double processingRate;
    public double idlePower;
    public double busyPower;
    public boolean isActive;
    public double currentLoad;
    public double utility;
    public String type; // "Standard", "High-Performance", "Energy-Efficient"
    public double reliability; // 0.0 to 1.0
    public double temperature; // Simulated temperature
    public boolean failed;

    public Server(int id, double processingRate, double idlePower, double busyPower, String type) {
        this.id = id;
        this.processingRate = processingRate;
        this.idlePower = idlePower;
        this.busyPower = busyPower;
        this.type = type;
        this.isActive = true;
        this.currentLoad = 0;
        this.utility = 0;
        this.reliability = 0.95 + Math.random() * 0.05;
        this.temperature = 20.0;
        this.failed = false;
    }

    public double utilizationPower(double taskLoad) {
        double utilization = Math.min(taskLoad / processingRate, 1.0);
        double basePower = idlePower + utilization * (busyPower - idlePower);

        // Temperature affects power consumption
        double tempFactor = 1.0 + (temperature - 20.0) * 0.01;
        return basePower * tempFactor;
    }

    public double calculateUtility(double load, double energyCost, double slaPenalty, double revenue) {
        if (failed) return -idlePower * energyCost;

        double energyCostTotal = utilizationPower(load) * energyCost;
        double responseTime = load / processingRate;
        double slaPenaltyTotal = responseTime * 1000 * slaPenalty;
        double revenueTotal = load * revenue;

        // QoS bonus for maintaining good service
        double qosBonus = (reliability > 0.95) ? load * 0.1 : 0;

        return revenueTotal + qosBonus - energyCostTotal - slaPenaltyTotal;
    }

    public void updateTemperature(double load) {
        double utilization = Math.min(load / processingRate, 1.0);
        // Temperature increases with utilization, decreases when idle
        temperature = 20.0 + utilization * 60.0 + (Math.random() - 0.5) * 5.0;
    }
}
