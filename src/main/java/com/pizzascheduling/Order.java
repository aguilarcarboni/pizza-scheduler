package com.pizzascheduling;

public class Order {
    public enum State {
        PENDING,
        PREPARING,
        CHEF_WAITING,
        OVEN_WAITING,
        OVEN_PREPARING,
        DRIVER_WAITING,
        DELIVERED
    }

    private final String person;
    private final int numPizzas;
    private final int deliveryTime;
    private final int priority;
    private State currentState;
    
    // Tracking progress
    private int pizzasPrepared;  // Only incremented when a pizza fully completes preparation
    private int pizzasBaked;     // Only incremented when a pizza fully completes baking
    private int remainingPrepTime;  // For current pizza only
    private int remainingBakeTime;  // For current pizza only
    private int remainingDeliveryTime;  // For whole order
    private int chefTime;  // Store the time needed per pizza
    private int bakeTime;  // Store the time needed per pizza

    public Order(String person, int numPizzas, int deliveryTime, int priority) {
        this.person = person;
        this.numPizzas = numPizzas;
        this.deliveryTime = deliveryTime;
        this.priority = priority;
        this.currentState = State.PENDING;
        this.pizzasPrepared = 0;
        this.pizzasBaked = 0;
        this.remainingPrepTime = 0;
        this.remainingBakeTime = 0;
        this.remainingDeliveryTime = deliveryTime;
    }

    public String getPerson() {
        return person;
    }

    public int getNumPizzas() {
        return numPizzas;
    }

    public int getDeliveryTime() {
        return deliveryTime;
    }

    public int getPriority() {
        return priority;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setState(State state) {
        this.currentState = state;
        // When transitioning to OVEN_WAITING, set initial bake time
        if (state == State.OVEN_WAITING) {
            this.remainingBakeTime = bakeTime * numPizzas;
        }
    }

    public int getPizzasPrepared() {
        return pizzasPrepared;
    }

    public int getPizzasBaked() {
        return pizzasBaked;
    }

    public int getRemainingPrepTime() {
        return remainingPrepTime;
    }

    public int getRemainingBakeTime() {
        return remainingBakeTime;
    }

    public int getRemainingDeliveryTime() {
        return remainingDeliveryTime;
    }

    public void incrementPizzasPrepared() {
        this.pizzasPrepared++;
        // Reset prep time for next pizza
        if (pizzasPrepared < numPizzas) {
            this.remainingPrepTime = chefTime;
        }
    }

    public void incrementPizzasBaked() {
        this.pizzasBaked++;
    }

    public void setRemainingPrepTime(int time) {
        this.remainingPrepTime = time;
    }

    public void setRemainingBakeTime(int time) {
        this.remainingBakeTime = time;
    }

    public void decrementRemainingPrepTime() {
        if (this.remainingPrepTime > 0) {
            this.remainingPrepTime--;
        }
    }

    public void decrementRemainingBakeTime() {
        if (this.remainingBakeTime > 0) {
            this.remainingBakeTime--;
        }
    }

    public void decrementRemainingDeliveryTime() {
        if (this.remainingDeliveryTime > 0) {
            this.remainingDeliveryTime--;
        }
    }

    // Add methods to set the time requirements
    public void setChefTime(int chefTime) {
        this.chefTime = chefTime;
        if (currentState == State.PENDING) {
            this.remainingPrepTime = chefTime;
        }
    }

    public void setBakeTime(int bakeTime) {
        this.bakeTime = bakeTime;
    }

    public int getTotalRemainingTime() {
        switch (currentState) {
            case PENDING:
                return numPizzas * chefTime;
            case PREPARING:
                return remainingPrepTime + ((numPizzas - (pizzasPrepared + 1)) * chefTime);
            case OVEN_WAITING:
                return bakeTime * numPizzas;
            case OVEN_PREPARING:
                return remainingBakeTime;
            case DRIVER_WAITING:
                return deliveryTime;
            case DELIVERED:
                return remainingDeliveryTime;
            default:
                return 0;
        }
    }

    public int getDoneInCurrentState() {
        switch (currentState) {
            case PREPARING:
            case CHEF_WAITING:
                return 0;  // No pizzas are "done" until they complete the state
            case OVEN_PREPARING:
                return pizzasBaked;
            case DRIVER_WAITING:
                return 0;
            case DELIVERED:
                return numPizzas;
            default:
                return 0;
        }
    }

    public int getPendingInCurrentState() {
        switch (currentState) {
            case PENDING:
            case PREPARING:
            case CHEF_WAITING:
                return numPizzas;  // All pizzas are pending until they complete preparation
            case OVEN_WAITING:
            case OVEN_PREPARING:
                return numPizzas - pizzasBaked;
            case DRIVER_WAITING:
                return numPizzas;
            case DELIVERED:
                return 0;
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%d,%d,%d", 
            person, 
            currentState,
            getDoneInCurrentState(),
            getPendingInCurrentState(),
            getTotalRemainingTime()
        );
    }
} 