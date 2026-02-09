package com.datacenter;

import junit.framework.TestCase;
import java.util.Arrays;

public class SchedulerTest extends TestCase {
    private Scheduler scheduler;
    private Server[] servers;
    private final int numServers = 2;
    private final double arrivalRate = 100.0;

    protected void setUp() {
        scheduler = new Scheduler(1, arrivalRate, numServers, "Uniform");
        servers = new Server[numServers];
        servers[0] = new Server(0, 100.0, 50.0, 200.0, "Standard");
        servers[1] = new Server(1, 100.0, 50.0, 200.0, "Standard");
    }

    public void testInitialization() {
        assertEquals(1, scheduler.id);
        assertEquals(arrivalRate, scheduler.arrivalRate);
        assertEquals(numServers, scheduler.allocation.length);
        assertEquals("Uniform", scheduler.loadPattern);
        assertEquals(arrivalRate, scheduler.currentLoad);
    }

    public void testUpdateLoadUniform() {
        scheduler.updateLoad("Uniform", 10);
        assertEquals(arrivalRate, scheduler.currentLoad);
    }

    public void testUpdateLoadDecreasing() {
        scheduler.updateLoad("Decreasing", 0);
        assertEquals(arrivalRate, scheduler.currentLoad);
        scheduler.updateLoad("Decreasing", 10);
        assertTrue(scheduler.currentLoad < arrivalRate);
    }

    public void testProportionalFairAllocation() {
        // Servers have same capacity, should split 50/50
        scheduler.bestResponse(servers, new Scheduler[]{}, "Proportional Fair");
        assertEquals(50.0, scheduler.allocation[0], 0.001);
        assertEquals(50.0, scheduler.allocation[1], 0.001);
    }

    public void testProportionalFairAllocationWithExistingLoad() {
        // Server 0 has 50 load already from other schedulers
        Scheduler other = new Scheduler(2, 50.0, numServers, "Uniform");
        other.allocation[0] = 50.0;
        
        // Available capacity: S0 = 100-50=50, S1 = 100-0=100
        // Total available capacity = 150
        // S0 share = 100 * (50/150) = 33.33
        // S1 share = 100 * (100/150) = 66.67
        
        scheduler.bestResponse(servers, new Scheduler[]{other}, "Proportional Fair");
        assertEquals(33.333, scheduler.allocation[0], 0.001);
        assertEquals(66.666, scheduler.allocation[1], 0.001);
    }

    public void testCalculateUtility() {
        scheduler.allocation[0] = 100.0;
        scheduler.allocation[1] = 0;
        scheduler.priority = 0.5; // No bonus
        
        // Revenue = 100 * 0.5 = 50.0
        // S0 Cost:
        // Power = 200 * 1.0 (at 20.C) = 200.0
        // Energy Cost = 200 * 0.12 = 24.0
        // Response time = 100 / 100 = 1.0s = 1000ms
        // SLA Penalty = 1000 * 0.05 = 50.0
        // Total Cost = 24.0 + 50.0 = 74.0
        // Utility = 50.0 - 74.0 = -24.0
        
        scheduler.calculateUtility(servers, 0.12, 0.05, 0.5);
        assertEquals(-24.0, scheduler.utility, 0.001);
    }
}
