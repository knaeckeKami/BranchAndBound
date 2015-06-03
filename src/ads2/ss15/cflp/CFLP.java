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

	private Integer[] facilitiesWithBestCostCustomerRatio;
	private int[][] bestFacilities;
	private int[] biggestCustomers;;
	private int[] dummySolutionArray;
	private int[] dummyAvailableBandwitdh;
	private int[] dummyCustomerSlotsAvailable;
	private Integer[] cheapestFacilites;
	private boolean[] openedFacilites;
	private int[][] realDistanceCosts;

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
				public int compare(Integer fac1, Integer fac2) {
					//System.out.println(finalI + " " + o1 + " " + o2);

					return CFLP.compare(instance.distances[fac1][finalI], instance.distances[fac2][finalI]);
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

		Integer[] biggestCustomersI = new Integer[instance.getNumCustomers()];
		for (int i = 0; i <  biggestCustomersI.length; i++) {
			biggestCustomersI[i] = i;
		}
		Arrays.sort(biggestCustomersI, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return CFLP.compare(instance.bandwidthOf(o2), instance.bandwidthOf(o1));
			}
		});

		biggestCustomers = new int[biggestCustomersI.length];
		for (int i = 0; i < biggestCustomers.length ; i++) {
			biggestCustomers[i] = biggestCustomersI[i];
		}

		facilitiesWithBestCostCustomerRatio = new Integer[instance.getNumFacilities()];

		for (int i = 0; i < facilitiesWithBestCostCustomerRatio.length ; i++) {
			facilitiesWithBestCostCustomerRatio[i] = i;
		}
		cheapestFacilites = facilitiesWithBestCostCustomerRatio.clone();
		Arrays.sort(facilitiesWithBestCostCustomerRatio, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return CFLP.compare(((double)instance.openingCostsFor(o1))/instance.maxCustomersFor(o1),
						((double)instance.openingCostsFor(o2))/instance.maxCustomersFor(o2));
			}
		});

		Arrays.sort(cheapestFacilites, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return CFLP.compare(instance.openingCostsFor(o1),
						((double) instance.openingCostsFor(o2)));
			}
		});

		System.out.println(bestFacilitiesForCostumer.toString());
		System.out.println(Arrays.toString(biggestCustomers));

		//for(int x = 0; x <  instance.distances.length; x++){
		//	for(int y = 0; y< instance.distances[x].length; y++){
		//		instance.distances[x][y] *= instance.distanceCosts;
		//	}
		//}
		dummySolutionArray = new int[instance.getNumCustomers()];
		dummyAvailableBandwitdh = new int[instance.getNumFacilities()];
		dummyCustomerSlotsAvailable = new int[instance.getNumFacilities()];
		openedFacilites = new boolean[instance.getNumFacilities()];

		realDistanceCosts = new int[instance.distances.length][instance.distances[0].length];
		System.out.println(instance.distanceCosts);
		for (int i = 0; i< realDistanceCosts.length; i++){
			for(int j =0; j< realDistanceCosts[i].length; j++){

				realDistanceCosts[i][j] =instance.distances[i][j] * instance.distanceCosts;
			}
		}



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
		branchAndBound(solution, -1, 0, Integer.MAX_VALUE,bandwidthAvailable, customerSlotsAvailable);


	}

	private void branchAndBound(int[] solution, int solutionSize,
								int costForPartialSolution, int lowerBoundForPartialSolution, int[] bandwidthAvailable,
								int[] customerSlotsAvailable){

		//Main.printDebug("branchAndBound" + solutionSize + " " + Arrays.toString(solution));
		//berechne für solution untere schranke
		//System.out.println(Arrays.toString(solution));
		System.arraycopy(solution, 0, dummySolutionArray, 0, solution.length);
		int newLowerBound = costForPartialSolution;
		//int calc = instance.calcObjectiveValue(solution);
		//Main.printDebug(""+newLowerBound+" "+calc);

		//System.arraycopy(bandwidthAvailable, 0, dummyAvailableBandwitdh, 0, bandwidthAvailable.length);
		//System.arraycopy(customerSlotsAvailable,0, dummyCustomerSlotsAvailable,0, dummyCustomerSlotsAvailable.length);
		Arrays.fill(openedFacilites, false);
		//Main.printDebug("lowb before: " + Arrays.toString(dummySolutionArray));
		for(int i = solutionSize+1; i < solution.length; i++){
			int indexOfSmallestPossibleFacilty = 0;
			int customer = getCurrentCustomerStrat(i);
			int smallestPossibleFacility = bestFacilities[customer][indexOfSmallestPossibleFacilty];
			while((bandwidthAvailable[smallestPossibleFacility] < instance.bandwidthOf(customer) ||
					customerSlotsAvailable[smallestPossibleFacility] == 0)  &&
							indexOfSmallestPossibleFacilty < bestFacilities[customer].length-1){
				indexOfSmallestPossibleFacilty++;
				smallestPossibleFacility = bestFacilities[customer][indexOfSmallestPossibleFacilty];
			}
			newLowerBound+= realDistanceCosts[smallestPossibleFacility][customer];
			//dummySolutionArray[customer] = smallestPossibleFacility;
			//openedFacilites[smallestPossibleFacility] = bandwidthAvailable[smallestPossibleFacility] == instance.maxBandwidth;
		}
		//Main.printDebug("lowb       "+Arrays.toString(dummySolutionArray));
		//newLowerBound = instance.calcObjectiveValue(dummySolutionArray);
		//Main.printDebug(Arrays.toString(openedFacilites));
		/*
		for (int i = 0; i<openedFacilites.length; i++){
			if(openedFacilites[i] ){
				newLowerBound-=instance.openingCostsFor(i);
				//Main.printDebug("subtract vlaue " + i );
			}
		}
		*/

		//Main.printDebug("do good lower bound");
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
		int slotsNeeded = solution.length - solutionSize -1;
		for(int i = solutionSize+1; i < solution.length; i++){
			int customer = getCurrentCustomerStrat(i);
			bandWidthNeeded=instance.bandwidthOf(customer);
		}
		ArrayList<Integer> usedFacts = new ArrayList<Integer>();
		int minCostForBandWidth=0;
		//Main.printDebug("do bandwidth lowbound");
		while (bandWidthNeeded > sumAvailableBandwidth){
			for(int i = 0; i< cheapestFacilites.length; i++){
				if(bandwidthAvailable[cheapestFacilites[i]] == instance.maxBandwidth && !usedFacts.contains(i)){

					 minCostForBandWidth += instance.openingCostsFor(cheapestFacilites[i]);
					usedFacts.add(i);
					sumAvailableBandwidth+=instance.maxBandwidth;
					break;
				}
			}
		}
		/*
		//Main.printDebug("do slots lowbound" + sumCustomersAvailable + " " + slotsNeeded);
		//Main.printDebug(Arrays.toString(facilitiesWithBestCostCustomerRatio));
		usedFacts.clear();
		int minCostForCustomerSlots =0;


		while (sumCustomersAvailable < slotsNeeded){
			for(int i = 0; i< facilitiesWithBestCostCustomerRatio.length; i++){

				if(bandwidthAvailable[facilitiesWithBestCostCustomerRatio[i]] == instance.maxBandwidth && !usedFacts.contains(i)){

					if(sumCustomersAvailable + instance.maxCustomersFor(facilitiesWithBestCostCustomerRatio[i]) >slotsNeeded){
						minCostForCustomerSlots += instance.openingCostsFor(facilitiesWithBestCostCustomerRatio[i]) * slotsNeeded / instance.maxCustomersFor(facilitiesWithBestCostCustomerRatio[i]);
					}else{
						minCostForCustomerSlots += instance.openingCostsFor(facilitiesWithBestCostCustomerRatio[i]);
					}


					sumCustomersAvailable+=instance.maxCustomersFor(facilitiesWithBestCostCustomerRatio[i]);

					break;
				}
			}
		}



		*/
		//Main.printDebug("end slots lowbound" +costForPartialSolution);
		newLowerBound+= minCostForBandWidth;

		if((getBestSolution() == null || getBestSolution().getUpperBound() > newLowerBound)) {
			/*
			//berechne Lösung
			System.arraycopy(bandwidthAvailable, 0, dummyAvailableBandwitdh, 0, bandwidthAvailable.length);
			System.arraycopy(customerSlotsAvailable, 0, dummyCustomerSlotsAvailable, 0,customerSlotsAvailable.length );
			System.arraycopy(solution, 0, dummySolutionArray, 0, dummySolutionArray.length);
			int[] newSolution = heuristicSolution(dummySolutionArray, solutionSize + 1, dummyAvailableBandwitdh , dummyCustomerSlotsAvailable);
			if (newSolution != null) {
				int upperbound = instance.calcObjectiveValue(newSolution);
				//Main.printDebug("upperbound: " + upperbound);
				if(this.setSolution(upperbound, newSolution.clone())){
					//Main.printDebug("Found Solution:" +this.getBestSolution().getUpperBound());
				}
			}
			*/
		}else{
			//Main.printDebug("too bad on LEVEL !+ "+solutionSize +"   |" +getBestSolution().getUpperBound() + " " +newLowerBound );
			return;
		}

		solutionSize++;
		if(solutionSize == solution.length){
			//Main.printDebug("final:" + Arrays.toString(solution));
			setSolution(costForPartialSolution, solution);
			return;
		}

		int customer = getCurrentCustomerStrat(solutionSize);

		for (int facility : bestFacilities[customer]) {

				if (customerSlotsAvailable[facility] > 0 && bandwidthAvailable[facility] >= instance.bandwidthOf(customer)) {

					int nextLowerBound = instance.distance(facility, customer)*instance.distanceCosts;
					if(bandwidthAvailable[facility] == instance.maxBandwidth){
						nextLowerBound+= instance.openingCostsFor(facility);
					}
					solution[customer] = facility;
					bandwidthAvailable[facility] -= instance.bandwidthOf(customer);
					customerSlotsAvailable[facility]--;

					branchAndBound(solution, solutionSize, nextLowerBound+costForPartialSolution, newLowerBound, bandwidthAvailable, customerSlotsAvailable);
					bandwidthAvailable[facility] += instance.bandwidthOf(customer);
					customerSlotsAvailable[facility]++;

				}
			}
		}




	private int[] heuristicSolution(int[] solution, int solutionSize, int[] bandwidthAvailable, int[] customerSlotsAvailable){


		//Main.printDebug("heuristicSolution " + solutionSize);


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

			int customer = getCurrentCustomerStrat(i);
			for(int facility : bestFacilities[customer]){
				if(customerSlotsAvailable[facility] > 0 &&
						bandwidthAvailable[facility] > instance.bandwidthOf(customer)){
					bandwidthAvailable[facility] -= instance.bandwidthOf(customer);
					customerSlotsAvailable[facility]--;
					//Main.printDebug("choose facility " + facility + " for customer " + i);
					solution[customer] = facility;
					continue out;
				}
			}
			/*
			for(int facility : bestFacilities[customer]){
				if(customerSlotsAvailable[facility] > 0 &&
						bandwidthAvailable[facility] > instance.bandwidthOf(customer)){
					bandwidthAvailable[facility] -= instance.bandwidthOf(customer);
					customerSlotsAvailable[facility]--;
					//Main.printDebug("choose facility " + facility + " for customer " + i);
					solution[customer] = facility;
					continue out;
				}
			}
			*/

			/*for(int facility : facilitiesWithBestCostCustomerRatio) {
				if (bandwidthAvailable[facility] == instance.maxBandwidth) {
					bandwidthAvailable[facility] -= instance.bandwidthOf(customer);
					customerSlotsAvailable[facility]--;
					//Main.printDebug("choose facility " + facility + " for customer " + i);
					solution[customer] = facility;
					continue out;
				}
			}
			*/
			Main.printDebug("no heuristic solution :(");
			return null;
		}
		return solution;
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

	public static int compare(double d1, double d2) {
		if (d1 < d2)
			return -1;           // Neither val is NaN, thisVal is smaller
		if (d1 > d2)
			return 1;            // Neither val is NaN, thisVal is larger

		// Cannot use doubleToRawLongBits because of possibility of NaNs.
		long thisBits    = Double.doubleToLongBits(d1);
		long anotherBits = Double.doubleToLongBits(d2);

		return (thisBits == anotherBits ?  0 : // Values are equal
				(thisBits < anotherBits ? -1 : // (-0.0, 0.0) or (!NaN, NaN)
						1));                          // (0.0, -0.0) or (NaN, !NaN)
	}

	public final int getCurrentCustomerStrat(int i){
		return biggestCustomers[i];
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


}
