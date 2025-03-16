package com.pizzascheduling;

public class Oven {
    private final int id;
    private Order currentOrder;
    private final int bakeTime;

    public Oven(int id, int bakeTime) {
        this.id = id;
        this.bakeTime = bakeTime;
        this.currentOrder = null;
    }

    public boolean isFree() {
        return currentOrder == null;
    }

    public void assignOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Cannot assign null order to oven");
        }
        
        if (currentOrder != null) {
            throw new IllegalStateException("Oven is already baking a pizza");
        }

        this.currentOrder = order;
        if (order.getCurrentState() == Order.State.OVEN_WAITING) {
            order.setState(Order.State.OVEN_PREPARING);
        }
        
        // Set initial bake time for the pizza
        if (order.getRemainingBakeTime() == 0) {
            order.setRemainingBakeTime(bakeTime);
        }
    }

    public void work() {
        if (currentOrder == null) {
            return;
        }

        // Release the order if it's moved past OVEN_PREPARING state
        if (currentOrder.getCurrentState() != Order.State.OVEN_PREPARING) {
            currentOrder = null;
            return;
        }

        currentOrder.decrementRemainingBakeTime();
        
        if (currentOrder.getRemainingBakeTime() == 0) {
            currentOrder.incrementPizzasBaked();
            
            // If all pizzas are baked, move to next state and release the order
            if (currentOrder.getPizzasBaked() == currentOrder.getNumPizzas()) {
                currentOrder.setState(Order.State.DRIVER_WAITING);
                currentOrder = null;
            } else {
                // Start next pizza
                currentOrder.setRemainingBakeTime(bakeTime);
            }
        }
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }

    @Override
    public String toString() {
        if (currentOrder == null) {
            return String.format("Oven%d,None", id);
        }
        return String.format("Oven%d,%s", id, currentOrder.getPerson());
    }
} 