# DRE2-Node-Algorithm

# Project Description
This project is used to simulate the results for data resilience problems.

We propose two time-efficient heuristics based on different network metrics and compare their efficiency.

- Node-Based Algorithm.
- Network-Based Algorithm.

Users can run the following methods to observe the performance between the three algorithms.
- observe Cplex's gap tolerance's influence on Quadratic programming's result.
- data resilience problem: change the storage node's storage, and fix the data item's amount.
- data resilience problem: fix the storage node's storage, and change the data item's amount.
- data resilience problem: observe the fault tolerance level for each algorithm.

# How to Run
The following external libraries are required to run this program:
- Apache XSSF: https://poi.apache.org/components/spreadsheet/
- IBM ILOG CPLEX Optimization: https://www.ibm.com/products/ilog-cplex-optimization-studio

Users will need to input basic information of the sensor network such as width, height, numbers of data generators, numbers of storage nodes, and the amount of overflow data.

1. Download the required libraries.
2. Place the libraries (.jar files) into the project.
3. Run SensorNetworkCPLX.java and follow the program steps to input the network's information.
4. Input the required network parameters.
5. The program will randomly generate nodes and provide the edge information automatically. (Note: if the graph is not connected, please run the program again)
6. Select a method that you like to run. (Note: Methods may required another input parameter)
7. The program will start sovling until it gets all the results. (Note: "Finish" will appear at the program log when the program finishes. Depend on the network's size and the inputed parameters, the program may take up to 30 minutes to complete the result.)

# Researchers
1. <a href="https://yashpolu.github.io/#/"> Yeswanth Polu </a>
2. <a href="http://csc.csudh.edu/btang/"> Dr. Bin Tang </a>
