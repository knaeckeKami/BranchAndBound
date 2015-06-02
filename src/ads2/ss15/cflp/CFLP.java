package ads2.ss15.cflp;

import java.util.*;

/**
 * @author Martin Kamleithner 1425827
 *
 * Klasse zum Berechnen der Lösung mittels Branch-and-Bound.
 * Hier sollen Sie Ihre Lösung implementieren.
 *
 */
public class CFLP extends AbstractCFLP {

	private CFLPInstance instance;

	private Integer[] cheapesFacilites;
	private int[][] bestFacilities;
	private Integer[] biggestCustomers;

	public CFLP(final CFLPInstance instance) {
		LinkedHashMap<Integer, ArrayList<Integer>> bestFacilitiesForCostumer;

		// TODO: Hier ist der richtige Platz fuer Initialisierungen
		this.instance = instance;
		//TODO test this magic
		bestFacilitiesForCostumer = new LinkedHashMap<Integer, ArrayList<Integer>>();
		System.out.println(instance.distances.length+ " " + instance.getNumCustomers());
		for(int i =0 ; i < instance.getNumCustomers(); i++){
			final int finalI = i;
			ArrayList<Integer> magicList = new ArrayList<Integer>();
			for(int j= 0; j < instance.getNumFacilities(); j++){
				magicList.add(j);
			}
			Collections.sort(magicList, new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					System.out.println(finalI + " " + o1 + " " + o2);
					return CFLP.compare(instance.distances[o1][finalI], instance.distances[o2][finalI]);
				}
			});
			bestFacilitiesForCostumer.put(i,magicList);
		}
		//convert data structure back to array for max performance
		bestFacilities = new int[instance.getNumCustomers()][instance.getNumFacilities()];
		for (int i = 0; i < instance.getNumCustomers() ; i++) {
			int j=0;
			for (int facility : bestFacilitiesForCostumer.get(i)){
				bestFacilities[i][j++] = facility;
			}

		}

		biggestCustomers = new Integer[instance.getNumCustomers()];
		for (int i = 0; i <  biggestCustomers.length; i++) {
			biggestCustomers[i] = i;
		}
		Arrays.sort(biggestCustomers, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return CFLP.compare(instance.bandwidthOf(o2), instance.bandwidthOf(o1));
			}
		});

		cheapesFacilites = new Integer[instance.getNumFacilities()];
		for (int i = 0; i < cheapesFacilites.length ; i++) {
			cheapesFacilites[i] = i;
		}
		Arrays.sort(cheapesFacilites, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return CFLP.compare(instance.openingCostsFor(o1), instance.openingCostsFor(o2));
			}
		});

		System.out.println(bestFacilitiesForCostumer.toString());
		System.out.println(Arrays.toString(biggestCustomers));

		//for(int x = 0; x <  instance.distances.length; x++){
		//	for(int y = 0; y< instance.distances[x].length; y++){
		//		instance.distances[x][y] *= instance.distanceCosts;
		//	}
		//}


	}

	/**
	 * Diese Methode bekommt vom Framework maximal 30 Sekunden Zeit zur
	 * Verfügung gestellt um eine gültige Lösung
	 * zu finden.
	 * 
	 * <p>
	 * Fügen Sie hier Ihre Implementierung des Branch-and-Bound Algorithmus
	 * ein.
	 * </p>
	 *
	 */
	@Override
	public void run() {
		Main.printDebug("num customers " + instance.getNumCustomers());
		Main.printDebug("num facilities " + instance.getNumFacilities());
		int[] solution = new int[instance.getNumCustomers()];
		int[] bandwidthAvailable = new int[instance.getNumFacilities()];
		int[] customerSlotsAvailable = instance.maxCustomers.clone();
		Arrays.fill(solution, -1);
		Arrays.fill(bandwidthAvailable, instance.maxBandwidth);
		branchAndBound(solution, -1, 0, bandwidthAvailable, customerSlotsAvailable);


	}

	private void branchAndBound(int[] solution, int solutionSize,
								int lowerBound, int[] bandwidthAvailable,
								int[] customerSlotsAvailable){

		Main.printDebug("branchAndBound" + solutionSize + " " + Arrays.toString(solution));
		//berechne für solution untere schranke
		//System.out.println(Arrays.toString(solution));
		int newLowerBound = instance.calcObjectiveValue(solution);
		for(int i = solutionSize+1; i < instance.distances.length; i++){
			int indexOfSmallestPossibleFacilty = 0;
			int smallestPossibleFacility = bestFacilities[biggestCustomers[i]][indexOfSmallestPossibleFacilty];
			while(bandwidthAvailable[smallestPossibleFacility] < instance.bandwidthOf(biggestCustomers[i]) ||
					customerSlotsAvailable[smallestPossibleFacility] == 0){
				indexOfSmallestPossibleFacilty++;

				smallestPossibleFacility = bestFacilities[biggestCustomers[i]][indexOfSmallestPossibleFacilty];
			}
			newLowerBound+=(instance.distances[smallestPossibleFacility][biggestCustomers[i]])*instance.distanceCosts;

		}
		//determine if more facilities need to be opened
		int sumAvailableBandwidth = 0;
		int sumCustomersAvailable = 0;
		for (int i = 0; i <  instance.getNumFacilities(); i++) {
			if(bandwidthAvailable[i] != instance.maxBandwidth){
				sumAvailableBandwidth += bandwidthAvailable[i];
				sumCustomersAvailable += customerSlotsAvailable[i];
			}
		}
		int bandWidthNeeded = 0;
		int slotsNeeded = solution.length - solutionSize +1;
		for(int i = solutionSize+1; i < solution.length; i++){
			bandWidthNeeded=instance.bandwidthOf(biggestCustomers[i]);
		}
		ArrayList<Integer> usedFacts = new ArrayList<Integer>();
		while (bandWidthNeeded > sumAvailableBandwidth){
			for(int i = 0; i< cheapesFacilites.length; i++){
				if(bandwidthAvailable[cheapesFacilites[i]] == instance.maxBandwidth && !usedFacts.contains(i)){
					newLowerBound += instance.openingCostsFor(cheapesFacilites[i]);
					usedFacts.add(i);
					sumAvailableBandwidth+=instance.maxBandwidth;
					break;
				}
			}
		}


		if(getBestSolution() == null || getBestSolution().getUpperBound() > newLowerBound) {
			//berechne Lösung
			int[] newSolution = heuristicSolution(solution, solutionSize + 1, bandwidthAvailable.clone(), customerSlotsAvailable.clone());
			if (newSolution != null) {
				int upperbound = instance.calcObjectiveValue(newSolution);
				//Main.printDebug("upperbound: " + upperbound);
				if(this.setSolution(upperbound, newSolution.clone())){
					Main.printDebug("Found Solution:" +this.getBestSolution().getUpperBound());
				}
			}
		}else{
			//Main.printDebug("too bad!");
			return;
		}

		solutionSize++;
		if(solutionSize == solution.length){
			setSolution(instance.calcObjectiveValue(solution),solution);
			return;
		}

		for (int i : bestFacilities[biggestCustomers[solutionSize]]) {

				if ( customerSlotsAvailable[i] > 0 && bandwidthAvailable[i] >= instance.bandwidthOf(biggestCustomers[solutionSize])) {
					int[] copySolution = solution.clone();
					copySolution[biggestCustomers[solutionSize]] = i;
					int[] newBandWidthAvailable = bandwidthAvailable.clone();
					int[] newCustomerSlotsAvailable = customerSlotsAvailable.clone();
					newBandWidthAvailable[i] -= instance.bandwidthOf(biggestCustomers[solutionSize]);
					newCustomerSlotsAvailable[i]--;
					branchAndBound(copySolution, solutionSize, newLowerBound, newBandWidthAvailable, newCustomerSlotsAvailable);
				}

			}

		}


	private static int min(int[] arr){
		int min = arr[0];
		for (int i = 1; i <arr.length; i++) {
			if(arr[i] < min){
				min = arr[i];

			}
		}
		return min;
	}

	private int[] heuristicSolution(int[] solution, int solutionSize, int[] bandwidthAvailable, int[] customerSlotsAvailable){
		//Main.printDebug("heuristicSolution " + solutionSize);
		int[] newSolution = solution.clone();
		/*
		Integer[] customers = new Integer[solution.length-solutionSize];
		for (int i = 0; i < customers.length; i++) {
			customers[i] = i+solutionSize;
		}
		Arrays.sort(customers, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return CFLP.compare(instance.bandwidthOf(o2), instance.bandwidthOf(o1));
			}
		});
*/
		out:
		for(int i = solutionSize; i < biggestCustomers.length; i++){

			int customer = biggestCustomers[i];
			for(int facility : bestFacilities[customer]){
				if(bandwidthAvailable[facility] != instance.maxBandwidth && customerSlotsAvailable[facility] > 0 &&
						bandwidthAvailable[facility] > instance.bandwidthOf(customer)){
					bandwidthAvailable[facility] -= instance.bandwidthOf(customer);
					customerSlotsAvailable[facility]--;
					//Main.printDebug("choose facility " + facility + " for customer " + i);
					newSolution[customer] = facility;
					continue out;
				}
			}

			for(int facility : bestFacilities[customer]){
				if(customerSlotsAvailable[facility] > 0 &&
						bandwidthAvailable[facility] > instance.bandwidthOf(customer)){
					bandwidthAvailable[facility] -= instance.bandwidthOf(customer);
					customerSlotsAvailable[facility]--;
					//Main.printDebug("choose facility " + facility + " for customer " + i);
					newSolution[customer] = facility;
					continue out;
				}
			}


			/*for(int facility : cheapesFacilites){
				if(bandwidthAvailable[facility] == instance.maxBandwidth){
					bandwidthAvailable[facility] -= instance.bandwidthOf(customer);
					customerSlotsAvailable[facility]--;
					//Main.printDebug("choose facility " + facility + " for customer " + i);
					newSolution[customer] = facility;
					continue out;
				}*/
			//}
			Main.printDebug("no heuristic solution :(");
			return null;
		}
		return newSolution;
	}


	/**
	 * copied from java source since we have to work with the super modern
	 * version 1.6
	 * Compares two {@code int} values numerically.
	 * The value returned is identical to what would be returned by:
	 * <pre>
	 *    Integer.valueOf(x).compareTo(Integer.valueOf(y))
	 * </pre>
	 *
	 * @param  x the first {@code int} to compare
	 * @param  y the second {@code int} to compare
	 * @return the value {@code 0} if {@code x == y};
	 *         a value less than {@code 0} if {@code x < y}; and
	 *         a value greater than {@code 0} if {@code x > y}
	 * @since 1.7
	 */
	public static int compare(int x, int y) {
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}



}
