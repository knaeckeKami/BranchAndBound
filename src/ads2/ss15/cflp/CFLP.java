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

	/**
	 * this array contains the best facility for each customer
	 * e.q. : bestFacilities[1][0] is the number of the facility with the
	 * smalles cost for customer 1
	 * 	 	  bestFacilties[2][1] is the number of the facility with the
	 * second smalles cost for customer 2
	 * the first index  udetermines the customer, the second index x specifies the xth best
	 * facility for the i_th customer-
	 */
	private final int[][] bestFacilities;
	/**
	 * the array contains the numbers of all customers
	 * ordered by there need of bandwidth descending.
	 */
	private final int[] biggestCustomers;

	/**
	 * this is a copy of the distance costs multiplied with
	 * the distance cost factor.
	 */
	private final int[][] realDistanceCosts;

	public CFLP(final CFLPInstance instance) {

		// TODO: Hier ist der richtige Platz fuer Initialisierungen
		this.instance = instance;
		/**
		 * calculate best facilites using collections
		 */
		LinkedHashMap<Integer, ArrayList<Integer>> bestFacilitiesForCostumer;
		bestFacilitiesForCostumer = new LinkedHashMap<Integer, ArrayList<Integer>>();
		System.out.println(instance.distances.length+ " " + instance.getNumCustomers());
		for(int i =0 ; i < instance.getNumCustomers(); i++){
			final int customer= i;
			ArrayList<Integer> magicList = new ArrayList<Integer>();
			for(int j= 0; j < instance.getNumFacilities(); j++){
				magicList.add(j);
			}
		
			Collections.sort(magicList, new Comparator<Integer>() {
				@Override
				public int compare(Integer fac1, Integer fac2) {
					//System.out.println(finalI + " " + o1 + " " + o2);

					return CFLP.compare(instance.distances[fac1][customer], instance.distances[fac2][customer]);
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
		/**
		 * calculate the biggest customers by bandwidth
		 */
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

		/**
		 * calculate the real distance costs
		 */
		realDistanceCosts = new int[instance.distances.length][instance.distances[0].length];
		//System.out.println(instance.distanceCosts);
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
		//Main.printDebug("num customers " + instance.getNumCustomers());
		//Main.printDebug("num facilities " + instance.getNumFacilities());
		int[] solution = new int[instance.getNumCustomers()];
		int[] bandwidthAvailable = new int[instance.getNumFacilities()];
		int[] customerSlotsAvailable = instance.maxCustomers.clone();
		Arrays.fill(solution, -1);
		Arrays.fill(bandwidthAvailable, instance.maxBandwidth);
		branchAndBound(solution, -1, 0 ,bandwidthAvailable, customerSlotsAvailable);


	}

	private void branchAndBound(int[] solution, int solutionSize,
								int costForPartialSolution,  int[] bandwidthAvailable,
								int[] customerSlotsAvailable){


		int newLowerBound = calculateLowerBound(costForPartialSolution,
												solutionSize,
												solution,
												bandwidthAvailable,
												customerSlotsAvailable);

		if(!(getBestSolution() == null || getBestSolution().getUpperBound() > newLowerBound)) {
			//the solution is too bad, there is already a better one!
			return;
		}
		solutionSize++;
		if(solutionSize == solution.length){
			//Main.printDebug("final:" + Arrays.toString(solution));
			setSolution(costForPartialSolution, solution);
			return;
		}
		//get the number of the next customer by current strategy
		//currently the customers with the biggest bandwidth are processed first
		int customer = getCurrentCustomerStrat(solutionSize);
		//branch first into the best facilites for the customer
		//regardless if they are open or not
		for (int facility : bestFacilities[customer]) {
				//check if the branching is still a valid solution
				if (customerSlotsAvailable[facility] > 0 && bandwidthAvailable[facility] >= instance.bandwidthOf(customer)) {
					//calculate the new cost of the partial solution
					int nextLowerBound = instance.distance(facility, customer)*instance.distanceCosts;
					if(bandwidthAvailable[facility] == instance.maxBandwidth){
						nextLowerBound+= instance.openingCostsFor(facility);
					}
					//assign the facility to the customer
					solution[customer] = facility;
					//adapt available bandwidth and slots for the facility
					bandwidthAvailable[facility] -= instance.bandwidthOf(customer);
					customerSlotsAvailable[facility]--;
					//now, finally, branch!
					branchAndBound(solution, solutionSize, nextLowerBound+costForPartialSolution, bandwidthAvailable, customerSlotsAvailable);
					//reverse the adaption of the bandwidth and slots so the next branching
					//is done with valid values
					bandwidthAvailable[facility] += instance.bandwidthOf(customer);
					customerSlotsAvailable[facility]++;
				}
			}
		}


	/**
	 * CURRENTLY UNUSED BECAUSE IT IS SLOWER THAN PURE ENUMERATION
	 * Calculates a heuristic solution for the given partial solution.
	 * May return null if no solution is found. (A null return does not mean
	 * that there is no solution!)
	 * @param solution the current partial solution
	 * @param solutionSize the number of fixed entries in this solution
	 * @param bandwidthAvailable the available bandwidth for each facility
	 * @param customerSlotsAvailable the number of free customer slots in each facilty
	 * @return a valid heuristic solution or null
	 */
	private int[] heuristicSolution(int[] solution, int solutionSize, int[] bandwidthAvailable, int[] customerSlotsAvailable){


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


	/**
	 * Returns the i_th customer in the current strategy for processing customers.
	 * Because the sequence, in which the customers are processed, must be equal for the whole
	 * program, this method is useful. With this method, the order also can be changed quickly.
	 * Currently, the order in which the customers are processed is determinded by
	 * the bandwidth.
	 * @param i the i_th customer
	 * @return the number of the i_th
	 */
	public final int getCurrentCustomerStrat(int i){
		return biggestCustomers[i];
	}


	/**
	 * calcultes a (pretty bad, but fast) lower bound for this solution
	 * @param costForPartialSolution the cost of the partial solution
	 * @param solutionSize the number of fixed entries in the solution
	 * @param solution the partial solution
	 * @param bandwidthAvailable the available bandwidth for each facility
	 * @param customerSlotsAvailable the available slots for each facility
	 * @return a lower bound for this solutio
	 */
	public final int calculateLowerBound(int costForPartialSolution, int solutionSize, int[] solution, int[] bandwidthAvailable, int[] customerSlotsAvailable){

		//berechne für solution untere schranke
		int newLowerBound = costForPartialSolution;

		/**
		 * current strategy: for each customer, which is not fixed by the partial solution:
		 * 		add the cost of the facility, which has the smalles cost for this customer
		 * 		but still has room
		 */

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
		}
		return newLowerBound;
	}

}
