package ss15.cflp;

/**
 * Abstrakte Klasse zum Berechnen der Lösung mittels Branch-and-Bound.
 * 
 * <p>
 * <b>WICHTIG:</b> Nehmen Sie keine Änderungen in dieser Klasse vor. Bei
 * der Abgabe werden diese Änderungen verworfen und es könnte dadurch
 * passieren, dass Ihr Programm somit nicht mehr korrekt funktioniert.
 * </p>
 */
public abstract class AbstractCFLP implements Runnable {
	
	/** Die bisher beste Lösung */
	private BnBSolution bestBnBSolution;

	final public synchronized boolean setSolution(int newUpperBound, int[] newSolution) {
		if (bestBnBSolution == null || newUpperBound < bestBnBSolution.getUpperBound()) {
			bestBnBSolution = new BnBSolution(newUpperBound, newSolution);
			return true;
		}
		return false;
	}
	
	/**
	 * Gibt die bisher beste gefundene Lösung zur&uuml;ck.
	 * 
	 * @return Die bisher beste gefundene Lösung.
	 */
	final public BnBSolution getBestSolution() {
		return bestBnBSolution;
	}
	
	public final class BnBSolution {

		private int upperBound;
		private int[] customersToFacility;
		
		public BnBSolution(int newUpperBound, int[] newSolution) {
			upperBound = newUpperBound;
			customersToFacility = newSolution.clone();
		}

		/**
		 * @return Die obere Schranke
		 */
		public int getUpperBound() {
			return upperBound;
		}

		/**
		 * @return Die Items der bisher besten Lösung
		 */
		public int[] getBestSolution() {
			return customersToFacility.clone();
		}
		
	}

}
