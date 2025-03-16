#!/bin/bash

# Compile the Java files
mvn clean compile

# Run the program with test parameters
mvn exec:java -Dexec.mainClass="com.pizzascheduling.PizzaScheduler" \
    -Dexec.args="--input-file src/main/tests/small-order.txt \
    --available-chefs 2 \
    --available-ovens 2 \
    --available-drivers 2 \
    --bake-time 5 \
    --chef-time 3 \
    --chef-strategy FOCUSED \
    --chef-quantum 9"