package com.pizzascheduling;

public class Driver {
    private final int id;
    private Order currentOrder;

    public Driver(int id) {
        this.id = id;
        this.currentOrder = null;
    }

    public boolean isFree() {
        return currentOrder == null;
    }

    public void assignOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Cannot assign null order to driver");
        }
        
        if (currentOrder != null) {
            throw new IllegalStateException("Driver is already delivering an order");
        }

        this.currentOrder = order;
        // The delivery time is already set in the order from creation
    }

    public void work() {
        if (currentOrder == null) {
            return;
        }

        currentOrder.decrementRemainingDeliveryTime();
        
        if (currentOrder.getRemainingDeliveryTime() == 0) {
            currentOrder.setState(Order.State.DELIVERED);
            currentOrder = null;
        }
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }

    @Override
    public String toString() {
        if (currentOrder == null) {
            return String.format("Driver%d,None", id);
        }
        return String.format("Driver%d,%s", id, currentOrder.getPerson());
    }
} 