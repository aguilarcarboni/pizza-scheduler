package com.pizzascheduling;

public class Chef {
    private final int id;
    private Order currentOrder;
    private final String strategy;
    private int remainingQuantum;
    private final int prepTime;

    public Chef(int id, String strategy, int prepTime) {
        this.id = id;
        this.strategy = strategy;
        this.prepTime = prepTime;
        this.currentOrder = null;
        this.remainingQuantum = 0;
    }

    public boolean isFree() {
        return currentOrder == null;
    }

    public void assignOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Cannot assign null order to chef");
        }
        
        if (currentOrder != null && strategy.equals("FOCUSED")) {
            throw new IllegalStateException("Chef is already working on an order in FOCUSED mode");
        }

        // Don't reassign if the order is already in OVEN_WAITING or later states
        if (order.getCurrentState() == Order.State.OVEN_WAITING ||
            order.getCurrentState() == Order.State.OVEN_PREPARING ||
            order.getCurrentState() == Order.State.DRIVER_WAITING ||
            order.getCurrentState() == Order.State.DELIVERED) {
            return;
        }

        this.currentOrder = order;
        // Set to PREPARING state for both PENDING and CHEF_WAITING states
        if (order.getCurrentState() == Order.State.PENDING || 
            order.getCurrentState() == Order.State.CHEF_WAITING) {
            order.setState(Order.State.PREPARING);
        }
        
        // Set initial prep time for the pizza if it's zero
        if (order.getRemainingPrepTime() == 0) {
            order.setRemainingPrepTime(prepTime);
        }
    }

    public void work() {
        if (currentOrder == null) {
            return;
        }

        // Release the order if it's moved past PREPARING state
        if (currentOrder.getCurrentState() != Order.State.PREPARING) {
            currentOrder = null;
            return;
        }

        currentOrder.decrementRemainingPrepTime();
        
        if (strategy.equals("RR")) {
            remainingQuantum--;
        }

        if (currentOrder.getRemainingPrepTime() == 0) {
            currentOrder.incrementPizzasPrepared();
            
            // If all pizzas are prepared, move to next state and release the order
            if (currentOrder.getPizzasPrepared() == currentOrder.getNumPizzas()) {
                currentOrder.setState(Order.State.OVEN_WAITING);
                currentOrder = null;
                return;
            }

            // If not all pizzas are prepared
            if (strategy.equals("FOCUSED")) {
                currentOrder.setRemainingPrepTime(prepTime);
            } else {
                // In RR mode, only release if quantum is expired
                currentOrder.setRemainingPrepTime(prepTime);
                if (remainingQuantum <= 0) {
                    currentOrder.setState(Order.State.CHEF_WAITING);
                    currentOrder = null;
                }
            }
        } else if (strategy.equals("RR") && remainingQuantum <= 0) {
            // Release order if quantum expired and pizza not completed
            currentOrder.setState(Order.State.CHEF_WAITING);
            currentOrder = null;
        }
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }

    public void setRemainingQuantum(int quantum) {
        this.remainingQuantum = quantum;
    }

    public int getRemainingQuantum() {
        return remainingQuantum;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        if (currentOrder == null) {
            return String.format("Chef%d,None", id);
        }
        if (strategy.equals("RR")) {
            return String.format("Chef%d,%s,%d", id, currentOrder.getPerson(), remainingQuantum);
        }
        return String.format("Chef%d,%s", id, currentOrder.getPerson());
    }
} 