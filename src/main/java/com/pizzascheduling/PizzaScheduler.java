package com.pizzascheduling;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PizzaScheduler {
    private int availableChefs;
    private int availableOvens;
    private int availableDrivers;
    private int bakeTime;
    private int chefTime;
    private String chefStrategy;
    private int chefQuantum;
    private String inputFile;
    private List<Order> orders;
    private List<Chef> chefs;
    private List<Oven> ovens;
    private List<Driver> drivers;
    private int currentMinute;

    public PizzaScheduler(String[] args) {
        parseArguments(args);
        orders = new ArrayList<>();
        chefs = new ArrayList<>();
        ovens = new ArrayList<>();
        drivers = new ArrayList<>();
        currentMinute = 1;
    }

    private void parseArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            String value;
            
            if (arg.contains("=")) {
                // Handle --key=value format
                String[] parts = arg.split("=", 2);
                arg = parts[0];
                value = parts[1];
            } else {
                // Handle --key value format
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("Missing value for argument: " + arg);
                }
                value = args[++i];
            }
            
            switch (arg) {
                case "--input-file":
                    inputFile = value;
                    break;
                case "--available-chefs":
                    availableChefs = Integer.parseInt(value);
                    break;
                case "--available-ovens":
                    availableOvens = Integer.parseInt(value);
                    break;
                case "--available-drivers":
                    availableDrivers = Integer.parseInt(value);
                    break;
                case "--bake-time":
                    bakeTime = Integer.parseInt(value);
                    break;
                case "--chef-time":
                    chefTime = Integer.parseInt(value);
                    break;
                case "--chef-strategy":
                    chefStrategy = value;
                    break;
                case "--chef-quantum":
                    chefQuantum = Integer.parseInt(value);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }
        validateArguments();
    }

    private void validateArguments() {
        if (inputFile == null || availableChefs <= 0 || availableOvens <= 0 || 
            availableDrivers <= 0 || bakeTime <= 0 || chefTime <= 0) {
            throw new IllegalArgumentException("Missing or invalid required arguments");
        }
        if (!chefStrategy.equals("FOCUSED") && !chefStrategy.equals("RR")) {
            throw new IllegalArgumentException("Chef strategy must be either FOCUSED or RR");
        }
        if (chefStrategy.equals("RR") && chefQuantum <= 0) {
            throw new IllegalArgumentException("Chef quantum must be positive for RR strategy");
        }
    }

    private void loadOrders() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 4) {
                    throw new IllegalArgumentException("Invalid order format: " + line);
                }
                String person = parts[0];
                int numPizzas = Integer.parseInt(parts[1]);
                int deliveryTime = Integer.parseInt(parts[2]);
                int priority = Integer.parseInt(parts[3]);
                Order order = new Order(person, numPizzas, deliveryTime, priority);
                order.setChefTime(chefTime);
                order.setBakeTime(bakeTime);
                orders.add(order);
            }
        }
    }

    private void initializeResources() {
        // Initialize chefs
        for (int i = 0; i < availableChefs; i++) {
            chefs.add(new Chef(i, chefStrategy, chefTime));
        }
        
        // Initialize ovens
        for (int i = 0; i < availableOvens; i++) {
            ovens.add(new Oven(i, bakeTime));
        }
        
        // Initialize drivers
        for (int i = 0; i < availableDrivers; i++) {
            drivers.add(new Driver(i));
        }
    }

    private void assignOrdersToChefs() {
        // Get all orders that need chef work and sort by priority
        List<Order> waitingOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.getCurrentState() == Order.State.PENDING ||
                order.getCurrentState() == Order.State.CHEF_WAITING) {
                waitingOrders.add(order);
            }
        }
        waitingOrders.sort(Comparator.comparingInt(Order::getPriority));

        // In RR mode, we want to assign orders to all free chefs
        // In FOCUSED mode, we want to assign the same order to all free chefs
        if (chefStrategy.equals("FOCUSED")) {
            // In FOCUSED mode, assign the same order to all free chefs
            if (!waitingOrders.isEmpty()) {
                Order highestPriorityOrder = waitingOrders.get(0);
                for (Chef chef : chefs) {
                    if (chef.isFree()) {
                        chef.assignOrder(highestPriorityOrder);
                    }
                }
            }
        } else {
            // In RR mode, each free chef gets the highest priority order available
            for (Chef chef : chefs) {
                if (chef.isFree() && !waitingOrders.isEmpty()) {
                    // Always pick the highest priority order (index 0 since list is sorted)
                    Order highestPriorityOrder = waitingOrders.get(0);
                    chef.setRemainingQuantum(chefQuantum);
                    chef.assignOrder(highestPriorityOrder);
                    // Only remove the order from waiting list if all its pizzas are being prepared
                    int pizzasBeingPrepared = (int) chefs.stream()
                        .filter(c -> c.getCurrentOrder() != null && 
                                   c.getCurrentOrder().getPerson().equals(highestPriorityOrder.getPerson()))
                        .count();
                    if (pizzasBeingPrepared >= highestPriorityOrder.getNumPizzas()) {
                        waitingOrders.remove(0);
                    }
                }
            }
        }
    }

    private void assignOrdersToOvens() {
        // Get all orders waiting for ovens and sort by priority (lower number = higher priority)
        List<Order> waitingOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.getCurrentState() == Order.State.OVEN_WAITING) {
                // Add the order multiple times based on remaining pizzas to bake
                int remainingPizzas = order.getNumPizzas() - order.getPizzasBaked();
                for (int i = 0; i < remainingPizzas; i++) {
                    waitingOrders.add(order);
                }
            }
        }
        waitingOrders.sort(Comparator.comparingInt(Order::getPriority));

        // Try to assign orders to free ovens
        for (Order order : waitingOrders) {
            for (Oven oven : ovens) {
                if (oven.isFree()) {
                    oven.assignOrder(order);
                        break;
                    }
                }
            }
        }

    private void assignOrdersToDrivers() {
        // Get all orders waiting for drivers and sort by priority
        List<Order> waitingOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.getCurrentState() == Order.State.DRIVER_WAITING) {
                waitingOrders.add(order);
            }
        }
        waitingOrders.sort(Comparator.comparingInt(Order::getPriority));

        // Try to assign orders to free drivers
        for (Order order : waitingOrders) {
            for (Driver driver : drivers) {
                if (driver.isFree()) {
                    driver.assignOrder(order);
                    break;
                }
            }
        }
    }

    private void simulateMinute() {

        System.out.println("==== MINUTE " + currentMinute);
        
        // Let resources work
        for (Chef chef : chefs) {
            chef.work();
        }
        for (Oven oven : ovens) {
            oven.work();
        }
        for (Driver driver : drivers) {
            driver.work();
        }

        // Print order status
        for (Order order : orders) {
            System.out.println(order.toString());
        }

        // Print resource status
        for (Chef chef : chefs) {
            System.out.println(chef.toString());
        }
        for (Oven oven : ovens) {
            System.out.println(oven.toString());
        }
        for (Driver driver : drivers) {
            System.out.println(driver.toString());
        }

        currentMinute++;
    }

    private boolean isSimulationComplete() {
        for (Order order : orders) {
            if (order.getCurrentState() != Order.State.DELIVERED) {
                return false;
            }
        }
        return true;
    }

    public void runSimulation() {
        initializeResources();
        
        while (!isSimulationComplete()) {
            assignOrdersToChefs();
            assignOrdersToOvens();
            assignOrdersToDrivers();
            simulateMinute();
        }
    }

    public static void main(String[] args) {
        try {
            PizzaScheduler scheduler = new PizzaScheduler(args);
            scheduler.loadOrders();
            scheduler.runSimulation();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
} 