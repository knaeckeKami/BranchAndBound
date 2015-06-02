package ss15.cflp;

import javax.swing.*;
import java.lang.reflect.Array;
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


	private int sum_bandwidth_customers=0;
	private int min_num_facilities;
	private LinkedHashMap<Integer, TreeSet<Integer>> bestFacilitiesForCostumer;

	public CFLP(final CFLPInstance instance) {

		// TODO: Hier ist der richtige Platz fuer Initialisierungen
		this.instance = instance;
		//TODO test this magic
		bestFacilitiesForCostumer = new LinkedHashMap<Integer, TreeSet<Integer>>();
		System.out.println(instance.distances.length+ " " + instance.getNumCustomers());
		for(int i =0 ; i < instance.getNumCustomers(); i++){
			final int finalI = i;
			TreeSet<Integer> magicTreeSet = new TreeSet<Integer>(new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					System.out.println(finalI+" "+o1 + " "+ o2);
					return Integer.compare(instance.distances[o1][finalI], instance.distances[o2][finalI]);
				}
			});
			for(int j= 0; j < instance.getNumFacilities(); j++){
				magicTreeSet.add(j);
			}
			bestFacilitiesForCostumer.put(i,magicTreeSet);
		}
		System.out.println(bestFacilitiesForCostumer.toString());


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
		int[] solution = new int[instance.getNumCustomers()];
		int[] bandwidthAvailable = new int[instance.getNumFacilities()];
		int[] customerSlotsAvailable = instance.maxCustomers.clone();
		Arrays.fill(solution, -1);
		Arrays.fill(bandwidthAvailable, instance.maxBandwidth);
		branchAndBound(solution, -1, 0, bandwidthAvailable, customerSlotsAvailable);


	}

	private void branchAndBound(int[] solution, int solutionSize, int lowerBound, int[] bandwidthAvailable, int[] customerSlosAvailable){
		Main.printDebug("branchAndBound" + solutionSize);
		//berechne für solution untere schranke
		//System.out.println(Arrays.toString(solution));
		int newLowerBound = instance.calcObjectiveValue(solution);
		for(int i = solutionSize+1; i < instance.distances.length; i++){
			newLowerBound+=min(instance.distances[i])*instance.distanceCosts;

		}

		if(getBestSolution() == null || getBestSolution().getUpperBound() > newLowerBound) {
			//berechne Lösung
			int[] newSolution = heuristicSolution(solution, solutionSize+1, bandwidthAvailable.clone(), customerSlosAvailable.clone());
			if (newSolution != null) {
				int upperbound = instance.calcObjectiveValue(newSolution);
				Main.printDebug("upperbound: " + upperbound);
				this.setSolution(upperbound, newSolution.clone());
			}
		}else{
			Main.printDebug("too bad!");
			return;
		}
		//branching
		solutionSize++;
		if(solutionSize == solution.length){

			return;
		}


		for (int i : bestFacilitiesForCostumer.get(solutionSize)){
			if(customerSlosAvailable[i] > 0 && bandwidthAvailable[i] >= instance.bandwidthOf(solutionSize)) {
				int[] copySolution = solution.clone();
				copySolution[solutionSize] = i;
				int[] newBandWidthAvailable = bandwidthAvailable.clone();
				int[] newCustomerSlotsAvailable = customerSlosAvailable.clone();
				newBandWidthAvailable[i] -= instance.bandwidthOf(solutionSize);
				newCustomerSlotsAvailable[i] --;
				branchAndBound(copySolution, solutionSize, lowerBound + newLowerBound, newBandWidthAvailable, newCustomerSlotsAvailable);
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
		Main.printDebug("heuristicSolution " + solutionSize);
		int[] newSolution = solution.clone();
		int count = 0;

		Integer[] customers = new Integer[solution.length-solutionSize];
		for (int i = 0; i < customers.length; i++) {
			customers[i] = i+solutionSize;
		}
		Arrays.sort(customers, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return Integer.compare(instance.bandwidthOf(o2), instance.bandwidthOf(o1));
			}
		});

		out:for(int i : customers){
			for(int facility : bestFacilitiesForCostumer.get(i)){
				if(bandwidthAvailable[facility] != instance.maxBandwidth && customerSlotsAvailable[facility] > 0 &&
						bandwidthAvailable[facility] > instance.bandwidthOf(i)){
					bandwidthAvailable[facility] -= instance.bandwidthOf(i);
					customerSlotsAvailable[facility]--;
					//Main.printDebug("choose facility " + facility + " for customer " + i);
					newSolution[i] = facility;
					continue out;
				}
			}
			for(int facility : bestFacilitiesForCostumer.get(i)){
				if(customerSlotsAvailable[facility] > 0 &&
						bandwidthAvailable[facility] > instance.bandwidthOf(i)){
					bandwidthAvailable[facility] -= instance.bandwidthOf(i);
					customerSlotsAvailable[facility]--;
					//Main.printDebug("choose facility " + facility + " for customer " + i);
					newSolution[i] = facility;
					continue out;
				}
			}
			Main.printDebug("no heuristic solution :(");
			return null;
		}
		return newSolution;
	}


}
