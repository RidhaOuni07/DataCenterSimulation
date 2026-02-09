package com.datacenter;

import junit.framework.TestCase;

public class ServerTest extends TestCase {
    private Server server;
    private final double processingRate = 100.0;
    private final double idlePower = 50.0;
    private final double busyPower = 200.0;

    protected void setUp() {
        server = new Server(1, processingRate, idlePower, busyPower, "Standard");
    }

    public void testInitialization() {
        assertEquals(1, server.id);
        assertEquals(processingRate, server.processingRate);
        assertEquals(idlePower, server.idlePower);
        assertEquals(busyPower, server.busyPower);
        assertTrue(server.isActive);
        assertFalse(server.failed);
        assertEquals(20.0, server.temperature);
    }

    public void testUtilizationPowerAtIdle() {
        // At 0 load, utilization is 0. Power should be idlePower * tempFactor.
        // tempFactor = 1.0 + (20.0 - 20.0) * 0.01 = 1.0
        double power = server.utilizationPower(0);
        assertEquals(idlePower, power, 0.001);
    }

    public void testUtilizationPowerAtFullLoad() {
        // At full load, utilization is 1.0. Power should be busyPower * tempFactor.
        double power = server.utilizationPower(processingRate);
        assertEquals(busyPower, power, 0.001);
    }

    public void testUtilizationPowerWithTemperature() {
        server.temperature = 30.0;
        // tempFactor = 1.0 + (30.0 - 20.0) * 0.01 = 1.1
        // At idle: 50 * 1.1 = 55
        double power = server.utilizationPower(0);
        assertEquals(55.0, power, 0.001);
    }

    public void testCalculateUtilityWhenFailed() {
        server.failed = true;
        double energyCost = 0.1;
        // Should return -idlePower * energyCost
        double utility = server.calculateUtility(10, energyCost, 0.05, 0.5);
        assertEquals(-50.0 * 0.1, utility);
    }

    public void testCalculateUtilityNormal() {
        double load = 50.0; // 50% load
        double energyCost = 0.12;
        double slaPenalty = 0.05;
        double revenue = 0.5;
        
        // power = (50 + 0.5 * (200-50)) * 1.0 = 125.0
        // energyCostTotal = 125 * 0.12 = 15.0
        // responseTime = 50 / 100 = 0.5
        // slaPenaltyTotal = 0.5 * 1000 * 0.05 = 25.0
        // revenueTotal = 50 * 0.5 = 25.0
        // qosBonus = (reliability > 0.95) ? 50 * 0.1 : 0 = 5.0 (approx, reliability is random but > 0.95)
        
        server.reliability = 0.96;
        double utility = server.calculateUtility(load, energyCost, slaPenalty, revenue);
        
        double expectedUtility = 25.0 + 5.0 - 15.0 - 25.0; // -10.0
        assertEquals(expectedUtility, utility, 0.001);
    }

    public void testUpdateTemperature() {
        server.updateTemperature(100.0);
        // temperature = 20.0 + 1.0 * 60.0 + random(-2.5, 2.5) = 80.0 +/- 2.5
        assertTrue(server.temperature >= 77.5 && server.temperature <= 82.5);
    }
}
