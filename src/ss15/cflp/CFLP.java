package ss15.cflp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeMap;

/**
 * @author Martin Kamleithner 1425827
 *
 * Klasse zum Berechnen der Lösung mittels Branch-and-Bound.
 * Hier sollen Sie Ihre Lösung implementieren.
 *
 */
public class CFLP extends AbstractCFLP {

	private CFLPInstance instance;

	private double upperBound = Double.POSITIVE_INFINITY;
	private int sum_bandwidth_customers=0;
	private int min_num_facilities;
	private Integer[] bestFacilities;

	public CFLP(final CFLPInstance instance) {

		// TODO: Hier ist der richtige Platz fuer Initialisierungen
		this.instance = instance;
		for(int x = 0; x <  instance.distances.length; x++){
			for(int y = 0; y< instance.distances[x].length; y++){
				instance.distances[x][y] *= instance.distanceCosts;
			}
		}
		bestFacilities = new Integer[instance.getNumFacilities()];
		for (int i = 0; i < bestFacilities.length ; i++) {
			bestFacilities[i] = i;
		}

		Arrays.sort(bestFacilities, new Comparator<Integer>() {
			public int compare(Integer i1, Integer i2) {
				return Integer.compare(instance.openingCostsFor(i1), instance.openingCostsFor(i2));
			}
		});
		for(int i : instance.bandwidths){
			sum_bandwidth_customers+= i;
		}
		min_num_facilities = (int)(Math.ceil((double) sum_bandwidth_customers) /instance.maxBandwidth);

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
		Arrays.fill(solution, -1);
		Arrays.fill(bandwidthAvailable, instance.maxBandwidth);
		branchAndBound(solution, 0, 0, bandwidthAvailable);


	}

	private void branchAndBound(int[] solution, int solutionSize, int lowerBound, int[] bandwidthAvailable){
		//berechne für solution untere schranke
		int newLowerBound = 0;
		int bandwidthStillNeeded = 0;
		for(int i = solutionSize; i < instance.distances.length; i++){
			newLowerBound+=min(instance.distances[i]);
			bandwidthStillNeeded += instance.bandwidthOf(i);
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




}
