
import java.awt.Dimension;
import java.io.*;
import java.util.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ilog.concert.*;

import java.util.Random;

/**
 * This program will generate a sensor network graph
 * and observe the graph's status (cost, data item's amount received / send / saved, dead or live).
 *
 * Different constraints and algorithm used to complete the task depends on users' input.
 *
 * @author  Yeswanth Polu
 * @since   2020-09-09
 */

public class SensorNetworkCPLX {

	static Random rand = new Random();
	static XSSFWorkbook energyworkbook = new XSSFWorkbook();
	static XSSFSheet energysheet = energyworkbook.createSheet("EnergyCostData");
	static Map<Integer, Axis> nodes = new LinkedHashMap<Integer, Axis>();
	Map<Integer, Boolean> discovered = new HashMap<Integer, Boolean>();
	Map<Integer, Boolean> explored = new HashMap<Integer, Boolean>();
	Map<Integer, Integer> parent = new HashMap<Integer, Integer>();
	Stack<Integer> s = new Stack<Integer>();
	static Map<String, Link> links = new HashMap<String, Link>();
	static Map<String, Link> linkstest = new HashMap<String, Link>();
	static HashMap<Integer, List<Integer>> close = new HashMap<>();

	static int minCapacity;
	static int biconnectcounter = 1;
	static int[] dataGens;
	static int[] storageNodes;
	static int numberOfDG;
	static int numberOfDataItemsPerDG;
	static int numberOfStoragePerSN;
	static int numberOfNodes;
	// 1 = milli, 1000 = nero
	static int baseUnit = 1;

	public static void main(String[] args) throws IOException, IloException {
		Scanner scan = new Scanner(System.in);
		System.out.println("The width (meters) is set to: e.g. 1000");
//		double width = scan.nextDouble();
		double width = 1000.0;
//        System.out.println(width);

		System.out.println("The height (meters) is set to: e.g. 1000");
//		double height = scan.nextDouble();
		double height = 1000.0;
//        System.out.println(height);
 
		System.out.println("Number of nodes is set to: e.g. 50");
//		numberOfNodes = scan.nextInt();
		numberOfNodes = 100;

		System.out.println("Transmission range (in meters) is set to: e.g. 250");
//		int transmissionRange = scan.nextInt();
		int transmissionRange = 250;
//        System.out.println(transmissionRange);

		System.out.println("Data Generators' amount is set to: e.g.10");
//		numberOfDG = scan.nextInt();
		numberOfDG = 20;

		dataGens = new int[numberOfDG];
		System.out.println("Assuming the first " + numberOfDG + " nodes are DGs\n");
		for (int i=1; i <= dataGens.length; i++) {
			dataGens[i-1] = i;
		}

		storageNodes = new int[numberOfNodes-numberOfDG];
		for (int i=0; i < storageNodes.length; i++){
			storageNodes[i] = i + 1 + numberOfDG;
		}

		System.out.println("Data items per DG to send out is set to: e.g. 50");
//		numberOfDataItemsPerDG = scan.nextInt();
		numberOfDataItemsPerDG = 50;

		System.out.println("Data storage per storage node is set to: e.g. 50");
		//numberOfStoragePerSN = scan.nextInt();
		// CHANGE
		numberOfStoragePerSN = 50;

		System.out.println("Please enter the initial energy capacity (micro-J e.g.: 2500):");
		System.out.println("**Note: The energy will be a random amount between your input and your input + 1000.");
		// max energy calculation use nano-J (leave out decimal point to reduce the precess time)
		minCapacity = scan.nextInt() * baseUnit;

		int numberOfSupDem = numberOfDataItemsPerDG * numberOfDG;
		int numberOfstorage = numberOfStoragePerSN * (numberOfNodes-numberOfDG);
		System.out.println("The total number of data items overloading: " + numberOfSupDem);
		System.out.println("The total number of data items storage: " + numberOfstorage);

		if (numberOfSupDem > numberOfstorage) {
			System.out.println("No enough storage");
			return;
		} else {
			System.out.println("Starting...");
		}

		SensorNetworkCPLX sensor = new SensorNetworkCPLX();

		// random generate the graph
//		populateNodes(numberOfNodes, width, height);

		// For the testing use the existing file
//
		File myfile = new File("input100.txt");
		readfileNodes(myfile);

		System.out.println("\nNode List:");
		for(int key : nodes.keySet()) {
			Axis ax = nodes.get(key);
			System.out.println("Node:" + key + ", xAxis:" + ax.getxAxis() + ", yAxis:" + ax.getyAxis() +
					", energycapacity:" + ax.getcapa());
		}

		Map<Integer, Set<Integer>> adjacencyList1 = new LinkedHashMap<>();
		Map<Integer, Integer> aoi = new HashMap<Integer,Integer>();
		ArrayList<Integer> list = new ArrayList<Integer>(numberOfNodes);

		for(int i = 1; i <= numberOfNodes; i++) {
			list.add(i);
		}

		sensor.populateAdjacencyList(numberOfNodes, transmissionRange, adjacencyList1);

		while(list.size() > 0) {
			int index = rand.nextInt(list.size());
			aoi.put(list.size(),list.remove(index));
		}

		System.out.println("\nAdjacency List: ");

		for(int i: adjacencyList1.keySet()) {
			System.out.print(i);
			System.out.print(": {");
			int adjSize = adjacencyList1.get(i).size();

			if(!adjacencyList1.isEmpty()){
				int adjCount = 0;
				for(int j: adjacencyList1.get(i)) {
					adjCount+=1;
					if(adjCount==adjSize){
						System.out.print(j);
					} else {
						System.out.print(j + ", ");
					}
				}
			}
			System.out.println("}");
		}

		System.out.println("\nOriginal Graph:");
		sensor.executeDepthFirstSearchAlg(width, height, adjacencyList1);
		System.out.println();

		if(biconnectcounter == 1) {
			System.out.println("\nAll of the Graph is fully connected!");
		} else {
			System.out.println("\nSome Graph is not fully connected!!");
			return;
		}

		//sorting
		Map<String, Link> treeMap = new TreeMap<>(linkstest);

		Map<String, Link> treeMap2 = new TreeMap<>(linkstest);

		Map<Integer, Double> sortMap = new HashMap<>();

		// -------------- set to random energy base on user input ---------------
		for (Map.Entry<Integer, Set<Integer>> node : adjacencyList1.entrySet()) {
			int temp = (rand.nextInt(1000 * baseUnit)) + minCapacity;
//			int temp = minCapacity;
			int i = node.getKey();
			if (i <= numberOfDG) {
				for (Link link : treeMap.values()) {
					// if the node is data generator, the energy will be max (minCapacity + max random number)
					if (i == link.getEdge().getHead()) {
						link.setEnergy(minCapacity + (1000 * baseUnit));
//						link.setEnergy(minCapacity);
						sortMap.put(link.getEdge().getHead(), (double) minCapacity + (1000 * baseUnit));
					}
				}
			} else {
				for (Link link : treeMap.values()) {
					if (i == link.getEdge().getHead()) {
						link.setEnergy(temp);
						sortMap.put(link.getEdge().getHead(), (double) temp);
					}
				}
			}
		}

		StringBuilder totalenergycost = new StringBuilder();
		totalenergycost.append("Sensor Network Edges with Distance, Cost and Capacity:\n");
		System.out.println("\nSensor Network Edges with Distance, Cost and Capacity:");
		for (Link link : treeMap.values()){
			for (Link innerlink : treeMap.values()) {
				if ((innerlink.getEdge().getHead() == link.getEdge().getHead()) &&
						(innerlink.getEdge().getTail() == link.getEdge().getTail())) {
					System.out.println(innerlink.toString());
					totalenergycost.append(innerlink.toString() + "\n");
				}
			}
		}

		// write original energy to file

		File EnergyFile = new File("Edge_cost_Original.txt");
		BufferedWriter writer_energy = new BufferedWriter(new FileWriter(EnergyFile));
		writer_energy.write(totalenergycost.toString());
		writer_energy.close();

		// file for energy sorting

		PriorityQueue<Map.Entry<Integer, Double>> copyEnergy = new PriorityQueue<>((a,b) ->
				b.getValue().intValue() - a.getValue().intValue());
		for (Map.Entry<Integer, Double> entry : sortMap.entrySet()) {
			copyEnergy.offer(entry);
		}
		while (!copyEnergy.isEmpty()) {
			copyEnergy.poll();
		}

		// create first row
		int rowcounter = 0;
		Row energyrow = energysheet.createRow(rowcounter++);

		// name for cols
		String[] row = new String[]{"RemoveNode", "C_{V-{i}}", "C_V", "dataitems", "CPLEX_energycost",
				"CPLEX_Obj", "Network_base_energycost", "Network_base_data_resilience", "MinCostFlow_energycost",
				"MinCostFlow_resilience", "CPLEX_Dead", "Algo_Dead", "MinCost_Dead", "Base_energy_capacity",
				"Base_storage_capacity", "Gap_tolerance(%)"};
		// write
		for (int i = 0; i < row.length; i++) {
			Cell energycell = energyrow.createCell(i);
			energycell.setCellValue(row[i]);
		}


		int runMethod = 3;

		if (runMethod == 0) {
			System.out.println("This program will run 5 times to generate 5 different gap tolerance.");
			for (int i = 0; i < 5; i++) {
				System.out.println("Input the gap tolerance (Percentage e.g: 0.8% = 0.008, 1% = 0.01):");
				double gapTolerance = scan.nextDouble();

				// create new row to store the data
				energyrow = energysheet.createRow(rowcounter++);

				/* ------------------------------- network base --------------------------------- */
				// run Algorithm
				NodeBase networkAlgo = new NodeBase(dataGens, storageNodes, numberOfDataItemsPerDG, numberOfStoragePerSN, treeMap);
				boolean algOk = networkAlgo.solve();

				Cell cell6 = energyrow.createCell(6);
				cell6.setCellValue(networkAlgo.getTotalEnergyCost());
				Cell cell7 = energyrow.createCell(7);
				cell7.setCellValue(networkAlgo.getDataResilience());

				System.out.println();
				if (!algOk) {
					System.out.println("have problem: network base");
				}
				// check dead
				PriorityQueue<Integer> deadAlgo = networkAlgo.getDeadnodes();
				if (!deadAlgo.isEmpty()) {
					ArrayList<Integer> deadAlgoNodes = new ArrayList<>();
					while (!deadAlgo.isEmpty()) {
						deadAlgoNodes.add(deadAlgo.poll());
					}
					Cell AlgoDead = energyrow.createCell(11);
					AlgoDead.setCellValue(Arrays.toString(deadAlgoNodes.toArray()));
				}

				/* --------------------------  input information ------------------------------ */
				Cell baseEnergy = energyrow.createCell(13);
				baseEnergy.setCellValue(minCapacity);
				Cell baseStorage = energyrow.createCell(14);
				baseStorage.setCellValue(numberOfStoragePerSN);
				Cell BaseGap = energyrow.createCell(15);
				BaseGap.setCellValue(gapTolerance);
			}
		}  else if (runMethod == 1) {
			System.out.println("Running priority formulation, please input the initial energy:");
			System.out.println("Still Under construction....");
			System.out.println("This part is preserved for further data priority research");

		} else if (runMethod == 2 || runMethod == 3) {
			System.out.println("Input the gap tolerance (Percentage e.g: 0.8% = 0.008, 1% = 0.01):");
			double gapTolerance = 0.03 ;
			for (int i = 0; i < 4; i++) {
				if (runMethod == 2) {
					System.out.println("Now running storage capacity: " + numberOfStoragePerSN);
				} else {
					System.out.println("Now running total data items: " + numberOfDataItemsPerDG);
				}

				// create new row to store nre data
				energyrow = energysheet.createRow(rowcounter++);

				/* --------------------------- node base part ------------------------------- */
				// run Algorithm
				NodeBase nodeAlgo = new NodeBase(dataGens, storageNodes, numberOfDataItemsPerDG, numberOfStoragePerSN, treeMap);
				boolean algOk = nodeAlgo.solve();

				Cell cell6 = energyrow.createCell(6);
				cell6.setCellValue(nodeAlgo.getTotalEnergyCost());
				Cell cell7 = energyrow.createCell(7);
				cell7.setCellValue(nodeAlgo.getDataResilience());

				System.out.println();
				if (!algOk) {
					System.out.println("have problem: node base");
				}
				// check dead
				PriorityQueue<Integer> deadAlgo = nodeAlgo.getDeadnodes();
				if (!deadAlgo.isEmpty()) {
					ArrayList<Integer> deadAlgoNodes = new ArrayList<>();
					while (!deadAlgo.isEmpty()) {
						deadAlgoNodes.add(deadAlgo.poll());
					}
					Cell AlgoDead = energyrow.createCell(11);
					AlgoDead.setCellValue(Arrays.toString(deadAlgoNodes.toArray()));
				}

				/* --------------------------- Network base part ------------------------------- */
				// run Algorithm
				NetworkBase networkAlgo = new NetworkBase(dataGens, storageNodes, numberOfDataItemsPerDG, numberOfStoragePerSN, treeMap);
				boolean algOk1 = networkAlgo.solve();

				Cell cell18 = energyrow.createCell(18);
				cell18.setCellValue(networkAlgo.getTotalEnergyCost());
				Cell cell19 = energyrow.createCell(19);
				cell19.setCellValue(networkAlgo.getDataResilience());

				System.out.println();
				if (!algOk1) {
					System.out.println("have problem: node base");
				}

				// check dead
				PriorityQueue<Integer> deadAlgo1 = networkAlgo.getDeadnodes();
				if (!deadAlgo1.isEmpty()) {
					ArrayList<Integer> deadAlgoNodes = new ArrayList<>();
					while (!deadAlgo1.isEmpty()) {
						deadAlgoNodes.add(deadAlgo1.poll());
					}
					Cell AlgoDead = energyrow.createCell(20);
					AlgoDead.setCellValue(Arrays.toString(deadAlgoNodes.toArray()));
					System.out.println(deadAlgoNodes.toArray());
				}



				/*-------------------------- extra information -----------------------*/
				// base energy / storage / gap
				Cell baseEnergy = energyrow.createCell(13);
				baseEnergy.setCellValue(minCapacity);
				Cell baseStorage = energyrow.createCell(14);
				baseStorage.setCellValue(numberOfStoragePerSN);
				Cell BaseGap = energyrow.createCell(15);
				BaseGap.setCellValue(gapTolerance);

				if (runMethod == 2) {
					numberOfStoragePerSN += 25;
				} else {
					numberOfDataItemsPerDG += 25;
				}
			}
		} else if (runMethod == 4) {
			System.out.println("the program will run 4 times. Please set the common difference for each run:");
			//int commonDifference = scan.nextInt();
			int commonDifference = 25;
			double gapTolerance = 0.03;
			for (int i = 0; i < 4; i++) {
				System.out.println("Now running storage capacity: " + numberOfStoragePerSN);

				// create new row to store nre data
				energyrow = energysheet.createRow(rowcounter++);

				/* --------------------------- node base part ------------------------------- */
				// run Algorithm
				NodeBase networkAlgo = new NodeBase(dataGens, storageNodes, numberOfDataItemsPerDG, numberOfStoragePerSN, treeMap);
				boolean algOk = networkAlgo.solve();

				Cell cell6 = energyrow.createCell(6);
				cell6.setCellValue(networkAlgo.getTotalEnergyCost());
				Cell cell7 = energyrow.createCell(7);
				cell7.setCellValue(networkAlgo.getDataResilience());

				System.out.println();
				if (!algOk) {
					System.out.println("have problem: network base");
				}
				// check dead
				PriorityQueue<Integer> deadAlgo = networkAlgo.getDeadnodes();
				if (!deadAlgo.isEmpty()) {
					ArrayList<Integer> deadAlgoNodes = new ArrayList<>();
					while (!deadAlgo.isEmpty()) {
						deadAlgoNodes.add(deadAlgo.poll());
					}
					Cell AlgoDead = energyrow.createCell(11);
					AlgoDead.setCellValue(Arrays.toString(deadAlgoNodes.toArray()));
				}


				/*-------------------------- extra information -----------------------*/
				// base energy / storage / gap
				Cell baseEnergy = energyrow.createCell(13);
				baseEnergy.setCellValue(minCapacity);
				Cell baseStorage = energyrow.createCell(14);
				baseStorage.setCellValue(numberOfStoragePerSN);
				Cell BaseGap = energyrow.createCell(15);
				BaseGap.setCellValue(gapTolerance);

				// each run add the common difference
				numberOfStoragePerSN += commonDifference;
			}
		} else if (runMethod == 5) {
			System.out.println("the program will run 4 times. Please set the common difference for each run:");
			int commonDifference = scan.nextInt();
			double gapTolerance = 0.03;
			for (int i = 0; i < 4; i++) {
				System.out.println("Now running data items per DG: " + numberOfDataItemsPerDG);

				// create new row to store nre data
				energyrow = energysheet.createRow(rowcounter++);

				/* --------------------------- network base part ------------------------------- */
				// run Algorithm
				NodeBase networkAlgo = new NodeBase(dataGens, storageNodes, numberOfDataItemsPerDG, numberOfStoragePerSN, treeMap);
				boolean algOk = networkAlgo.solve();

				Cell cell6 = energyrow.createCell(6);
				cell6.setCellValue(networkAlgo.getTotalEnergyCost());
				Cell cell7 = energyrow.createCell(7);
				cell7.setCellValue(networkAlgo.getDataResilience());

				System.out.println();
				if (!algOk) {
					System.out.println("have problem: network base");
				}
				// check dead
				PriorityQueue<Integer> deadAlgo = networkAlgo.getDeadnodes();
				if (!deadAlgo.isEmpty()) {
					ArrayList<Integer> deadAlgoNodes = new ArrayList<>();
					while (!deadAlgo.isEmpty()) {
						deadAlgoNodes.add(deadAlgo.poll());
					}
					Cell AlgoDead = energyrow.createCell(11);
					AlgoDead.setCellValue(Arrays.toString(deadAlgoNodes.toArray()));
				}


				/*-------------------------- extra information -----------------------*/
				// base energy / storage / gap
				Cell baseEnergy = energyrow.createCell(13);
				baseEnergy.setCellValue(minCapacity);
				Cell baseStorage = energyrow.createCell(14);
				baseStorage.setCellValue(numberOfStoragePerSN);
				Cell BaseGap = energyrow.createCell(15);
				BaseGap.setCellValue(gapTolerance);

				// each run add the common difference
				numberOfDataItemsPerDG += commonDifference;
			}
		} else if (runMethod == 6){
			// for a same graph, run twice
			double gapTolerance = 0.03;

			for (int i = 0 ; i < 2; i++) {
				int tempmincapa = minCapacity;

				// copy the tree so we don't effect the original one
				Map<String, Link> copyTree = new TreeMap<>();
				for (Map.Entry<String, Link> pair : treeMap.entrySet()) {
					Link link = new Link(new Edge(pair.getValue().getEdge().getTail(), pair.getValue().getEdge().getHead(), 0),
							pair.getValue().getDistance(), pair.getValue().getRCost(), pair.getValue().getTCost(), pair.getValue().getSCost(),
							pair.getValue().getEnergy());
					copyTree.put(pair.getKey(), link);
				}

				for (Link link : copyTree.values()) {
					sortMap.put(link.getEdge().getHead(), link.getEnergy());
				}

				while (minCapacity > 0) {
					System.out.println("Now running energy capacity (in nano-J): " + minCapacity);

					// create new row to store nre data
					energyrow = energysheet.createRow(rowcounter++);

					/* --------------------------- node base part ------------------------------- */
					// run Algorithm
					NodeBase nodeAlgo = new NodeBase(dataGens, storageNodes, numberOfDataItemsPerDG, numberOfStoragePerSN, treeMap);
					boolean algOk = nodeAlgo.solve();

					System.out.println();
					if (algOk) {
						Cell cell6 = energyrow.createCell(6);
						cell6.setCellValue(nodeAlgo.getTotalEnergyCost());
						Cell cell7 = energyrow.createCell(7);
						cell7.setCellValue(nodeAlgo.getDataResilience());
					} else {
						System.out.println("have problem: network base");
						Cell cell6 = energyrow.createCell(6);
						cell6.setCellValue("X");
						Cell cell7 = energyrow.createCell(7);
						cell7.setCellValue("X");
					}

					// check dead
					PriorityQueue<Integer> deadAlgo = nodeAlgo.getDeadnodes();
					if (algOk) {
						if (!deadAlgo.isEmpty()) {
							ArrayList<Integer> deadAlgoNodes = new ArrayList<>();
							while (!deadAlgo.isEmpty()) {
								deadAlgoNodes.add(deadAlgo.poll());
							}
							Cell AlgoDead = energyrow.createCell(11);
							AlgoDead.setCellValue(Arrays.toString(deadAlgoNodes.toArray()));
						}
					} else {
						Cell AlgoDead = energyrow.createCell(11);
						AlgoDead.setCellValue("X");
					}

					/* --------------------------- Network base part ------------------------------- */
					// run Algorithm
					NetworkBase networkAlgo = new NetworkBase(dataGens, storageNodes, numberOfDataItemsPerDG, numberOfStoragePerSN, treeMap);
					boolean algOk1 = networkAlgo.solve();

					Cell cell18 = energyrow.createCell(18);
					cell18.setCellValue(networkAlgo.getTotalEnergyCost());
					Cell cell19 = energyrow.createCell(19);
					cell19.setCellValue(networkAlgo.getDataResilience());

					System.out.println();
					if (!algOk1) {
						System.out.println("have problem: node base");
					}

					// check dead
					PriorityQueue<Integer> deadAlgo1 = networkAlgo.getDeadnodes();
					if (!deadAlgo1.isEmpty()) {
						ArrayList<Integer> deadAlgoNodes = new ArrayList<>();
						while (!deadAlgo1.isEmpty()) {
							deadAlgoNodes.add(deadAlgo1.poll());
						}
						Cell AlgoDead = energyrow.createCell(20);
						AlgoDead.setCellValue(Arrays.toString(deadAlgoNodes.toArray()));
						System.out.println(deadAlgoNodes.toArray());
					}


					/*-------------------------- extra information -----------------------*/
					// base energy / storage / gap
					Cell baseEnergy = energyrow.createCell(13);
					baseEnergy.setCellValue(minCapacity);
					Cell baseStorage = energyrow.createCell(14);
					baseStorage.setCellValue(numberOfStoragePerSN);
					Cell BaseGap = energyrow.createCell(15);
					BaseGap.setCellValue(gapTolerance);

					for (Link link : copyTree.values()) {
						link.setEnergy(link.getEnergy() - 200 * baseUnit);
						sortMap.put(link.getEdge().getHead(), link.getEnergy() - 200 * baseUnit);
					}
					// each run, change base capacity
					minCapacity -= 200 * baseUnit;
				}

				// generate data for another storage capacity
				rowcounter++;
				if (i < 1) {
					System.out.println("Input another capacity: ");
					//numberOfStoragePerSN = scan.nextInt();
					numberOfStoragePerSN = 13;
				}
				minCapacity = tempmincapa;
				copyTree.clear();
				sortMap.clear();
			}
		} else if (runMethod == 7) {
			// for a same graph, run twice
			double gapTolerance = 0.03;
			for (int i = 0 ; i < 2; i++) {
				if (i == 0) {
					// create new row to store nre data
					energyrow = energysheet.createRow(rowcounter++);

					/* ------------------------------- network base --------------------------------- */
					// run Algorithm
					NodeBase networkAlgo = new NodeBase(dataGens, storageNodes, numberOfDataItemsPerDG, numberOfStoragePerSN, treeMap);
					boolean algOk = networkAlgo.solve();

					Cell cell6 = energyrow.createCell(6);
					cell6.setCellValue(networkAlgo.getTotalEnergyCost());
					Cell cell7 = energyrow.createCell(7);
					cell7.setCellValue(networkAlgo.getDataResilience());

					System.out.println();
					if (!algOk) {
						System.out.println("have problem: network base");
					}
					// check dead
					PriorityQueue<Integer> deadAlgo = networkAlgo.getDeadnodes();
					if (!deadAlgo.isEmpty()) {
						ArrayList<Integer> deadAlgoNodes = new ArrayList<>();
						while (!deadAlgo.isEmpty()) {
							deadAlgoNodes.add(deadAlgo.poll());
						}
						Cell AlgoDead = energyrow.createCell(11);
						AlgoDead.setCellValue(Arrays.toString(deadAlgoNodes.toArray()));
					}

					/* --------------------------  input information ------------------------------ */
					Cell baseEnergy = energyrow.createCell(13);
					baseEnergy.setCellValue(minCapacity);
					Cell baseStorage = energyrow.createCell(14);
					baseStorage.setCellValue(numberOfStoragePerSN);
					Cell BaseGap = energyrow.createCell(15);
					BaseGap.setCellValue(gapTolerance);

					rowcounter++;
				} else {
				}
			}
		} else {
			System.out.println("Wrong input!");
		}

		// for .txt files data output
//		generateFiles(treeMap, adjacencyList1);

		// write to csv file
		FileOutputStream out = new FileOutputStream(new File("data.xlsx"));
		energyworkbook.write(out);
		out.close();

		scan.close();

		System.out.println("Finish!");

	}

	static void readfileNodes(File file) throws IOException {
		// if user want to fix the graphic, enter a number in Random()
		//Random random = new Random();
		// original 1312
		Scanner scane = new Scanner(System.in);
		System.out.println("Please enter the energy capacity:");
		minCapacity = scane.nextInt(); //max energy

		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line;

		while ((line = bufferedReader.readLine()) != null) {
			Axis axis = new Axis();
			String[] words = line.split("	");
			//int scale = (int) Math.pow(10, 1);
			double xAxis = Double.parseDouble(words[1]);
			double yAxis =Double.parseDouble(words[2]);

			//xAxis = (double)Math.floor(xAxis * scale) / scale;
			//yAxis = (double)Math.floor(yAxis * scale) / scale;

			axis.setxAxis(xAxis);
			axis.setyAxis(yAxis);
			axis.setcapa(minCapacity); //each nodes energy capacity

			nodes.put(Integer.parseInt(words[0]) + 1, axis);
		}

		fileReader.close();
		scane.close();

	}

	// receive and save cost
	double getRSCost(){
		final int K = 512; // k = 512B (from paper0)
		final double E_elec = 100 * Math.pow(10,-9); // E_elec = 100nJ/bit (from paper 1)
		double Erx = 8 * E_elec * K; // Receiving energy consumption assume is same as saving
		return Erx * 1000 * baseUnit; // make it pico J now for better number visualization during calculation
	}

	// transfer cost
	static double getTCost(double l) {
		final int K = 512; // k = 512B (from paper0)
		final double E_elec = 100 * Math.pow(10,-9); // E_elec = 100nJ/bit (from paper 1)
		final double Epsilon_amp = 100 * Math.pow(10,-12); // Epsilon_amp = 100 pJ/bit/squared(m) (from paper 1)
		double Etx = E_elec * K * 8 + Epsilon_amp * K * 8 * l * l; //
		return Math.round(Etx * 1000 * baseUnit * 10000) / 10000.0; // make it pico J now for better number visualization during calculation
	}

	/**
	 * Check if the graph is connected and generate the graph
	 * @param width:
	 * @param height:
	 * @param adjList: adjacent list of each node
	 */
	void executeDepthFirstSearchAlg(double width, double height, Map<Integer, Set<Integer>> adjList) {
		s.clear();
		explored.clear();
		discovered.clear();
		parent.clear();
		List<Set<Integer>> connectedNodes = new ArrayList<Set<Integer>>();
		for(int node: adjList.keySet()) {
			Set<Integer> connectedNode = new LinkedHashSet<Integer>();
			recursiveDFS(node, connectedNode, adjList);

			if(!connectedNode.isEmpty()) {
				connectedNodes.add(connectedNode);
			}
		}

		if(connectedNodes.size() == 1) {
			System.out.println("Graph is fully connected.");
		} else {
			System.out.println("Graph is not fully connected.");
			biconnectcounter++;
		}

		// Draw first sensor network graph
		SensorNetworkGraph graph = new SensorNetworkGraph(dataGens);
		graph.setGraphWidth(width);
		graph.setGraphHeight(height);
		graph.setNodes(nodes);
		graph.setAdjList(adjList);
		graph.setPreferredSize(new Dimension(960, 800));
		Thread graphThread = new Thread(graph);
		graphThread.start();

	}

	/**
	 * recursiveDFS use to check connection
	 * @param u:
	 * @param connectedNode:
	 * @param adjList: adjacent list
	 */
	void recursiveDFS(int u, Set<Integer> connectedNode, Map<Integer, Set<Integer>> adjList) {
		if(!s.contains(u) && !explored.containsKey(u)) {
			s.add(u);
			discovered.put(u, true);
		}

		while(!s.isEmpty()) {
			if(!explored.containsKey(u)) {
				List<Integer> list = new ArrayList<>(adjList.get(u));
				for(int v: list) {

					if(!discovered.containsKey(v)) {
						s.add(v);
						discovered.put(v, true);

						if(parent.get(v) == null) {
							parent.put(v, u);
						}
						recursiveDFS(v, connectedNode, adjList);
					} else if(list.get(list.size()-1) == v) {
						if( parent.containsKey(u)) {
							explored.put(u, true);
							s.removeElement(u);

							connectedNode.add(u);
							recursiveDFS(parent.get(u), connectedNode, adjList);
						}
					}
				}
				if(!explored.containsKey(u))
					explored.put(u, true);
				s.removeElement(u);
				connectedNode.add(u);
			}
		}
	}

	/**
	 * generate nodes
	 * @param nodeCount: node's amount
	 * @param width:
	 * @param height:
	 */
	static void populateNodes(int nodeCount, double width, double height) {
		// if user want to fix the graphic, enter a number in Random()
		Random random = new Random();

		for(int i = 1; i <= nodeCount; i++) {
			Axis axis = new Axis();
			int scale = (int) Math.pow(10, 1);
			double xAxis =(0 + random.nextDouble() * (width - 0));
			double yAxis = 0 + random.nextDouble() * (height - 0);
			int capa = random.nextInt(10) + 1;

			xAxis = Math.floor(xAxis * scale) / scale;
			yAxis = Math.floor(yAxis * scale) / scale;

			axis.setxAxis(xAxis);
			axis.setyAxis(yAxis);
			axis.setcapa(capa); //each nodes energy capacity

			nodes.put(i, axis);
		}
	}

	/**
	 * create node's edge (link) information
	 * @param nodeCount: node's amount
	 * @param tr: transfer range (upper bound)
	 * @param adjList: use to record adjacent node
	 */
	void populateAdjacencyList(int nodeCount, int tr, Map<Integer, Set<Integer>> adjList) {
		for(int i = 1; i <= nodeCount; i++) {
			adjList.put(i, new HashSet<>());
		}

		for(int node1: nodes.keySet()) {
			Axis axis1 = nodes.get(node1);
			for(int node2: nodes.keySet()) {
				Axis axis2 = nodes.get(node2);

				if(node1 == node2) {
					continue;
				}

				double xAxis1 = axis1.getxAxis();
				double yAxis1 = axis1.getyAxis();
				double xAxis2 = axis2.getxAxis();
				double yAxis2 = axis2.getyAxis();
				double distance =  Math.sqrt(((xAxis1-xAxis2)*(xAxis1-xAxis2)) + ((yAxis1-yAxis2)*(yAxis1-yAxis2)));
				double energy = minCapacity;

				if(distance <= tr) {
					linkstest.put("(" + node2 + ", " + node1 + ")", new Link(new Edge(node2, node1, 0), distance, getRSCost(), getTCost(distance), getRSCost(), energy));
					if (!close.containsKey(node2)) {
						List<Integer> list = new ArrayList<>();
						list.add(node1);
						list.add((int) distance);
						close.put(node2, list);
					} else {
						if (close.get(node2).get(1) > distance) {
							close.get(node2).set(0, node1);
							close.get(node2).set(1, (int) distance);
						}
					}
					Set<Integer> tempList = adjList.get(node1);
					tempList.add(node2);
					adjList.put(node1, tempList);
					tempList = adjList.get(node2);
					tempList.add(node1);
					adjList.put(node2, tempList);
					if (node1 > node2){
						links.put("(" + node2 + ", " + node1 + ")", new Link(new Edge(node2, node1, 1), distance, getRSCost(), getTCost(distance), getRSCost(), energy));
					} else {
						links.put("(" + node1 + ", " + node2 + ")", new Link(new Edge(node1, node2, 1), distance, getRSCost(), getTCost(distance), getRSCost(), energy));
					}
				}
			}
		}
	}

	void populateRandom(){

	}
}
