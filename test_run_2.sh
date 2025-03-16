#!/bin/bash

# Compile the Java files
mvn clean compile

# Run the program with test parameters
mvn exec:java -Dexec.mainClass="com.pizzascheduling.PizzaScheduler" \
    -Dexec.args="--input-file src/main/tests/medium-order.txt \
    --available-chefs 4 \
    --available-ovens 2 \
    --available-drivers 5 \
    --bake-time 2 \
    --chef-time 4 \
    --chef-strategy RR \
    --chef-quantum 3"