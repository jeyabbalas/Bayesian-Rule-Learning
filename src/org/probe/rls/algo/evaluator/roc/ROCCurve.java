package org.probe.rls.algo.evaluator.roc;

import java.util.List;
import java.util.Stack;

public class ROCCurve {
	
	public ROCCurve(String positiveClass, List<String> actual, List<Double> predicted) {
		testInstances = new double[actual.size()][2];
		numPositive = 0;
		numNegative = 0;
		
		for(int i=0; i<actual.size(); i++) {
			testInstances[i][0] = (actual.get(i).equalsIgnoreCase(positiveClass)) ? 1.0 : 0.0;
			testInstances[i][1] = predicted.get(i);
			if(testInstances[i][0] == 1.0) {
				numPositive++;
			}
			else {
				numNegative++;
			}
		}
		
		rocCurve = new Stack<double[]>();
		auroc = 0.0;
		testInstances = rowReverseQuickSort(testInstances, 1);
		
		generateROCCurve();
	}
	
	/**
	 * row-wise quick-sort (descending) with respect to a column of a 2D array. -Jeya
	 * 
	 * @param da: The 2D array.
	 * @param col: Column to be sorted.
	 * @return
	 */
	public static double[][] rowReverseQuickSort(double[][] arr, int col) {
		double[] inArr = new double[arr.length];
		int[] temp = new int[arr.length];
		for (int i = 0; i < arr.length; i++){
			temp[i] = i;
			inArr[i] = arr[i][col];
		}
		quickSort(inArr, temp, 0, inArr.length - 1);
		
		double[][] newArr = new double[arr.length][arr[0].length];
		int ptr = 0;
		for (int j = (arr.length - 1); j >= 0; j--){
			for (int k = 0; k < arr[j].length; k++){
				newArr[ptr][k] = arr[temp[j]][k];
			}
			ptr++;
		}
		
		return newArr;
	}
	
	private static void quickSort(double[] a, int[] s, int left, int right) {
		int lo = left;
		int hi = right;
		double mid;
		int help;

		if (right > left) {
			mid = a[s[(left + right) / 2]];

			// Loop through the array until indices cross
			while (lo <= hi) {
				while ((a[s[lo]] < mid) && (lo < right)) {
					++lo;
				}

				// Find an element that is smaller than or equal to
				// the partition element starting from the right Index.
				while ((a[s[hi]] > mid) && (hi > left)) {
					--hi;
				}

				// If the indexes have not crossed, swap
				if (lo <= hi) {
					help = s[lo];
					s[lo] = s[hi];
					s[hi] = help;
					++lo;
					--hi;
				}
			}
			if (left < hi) {
				quickSort(a, s, left, hi);
			}
			if (lo < right) {
				quickSort(a, s, lo, right);
			}
		}
	}
	
	private void generateROCCurve() {
		if(numPositive > 0 && numNegative > 0){
			int tp = 0, fp = 0;
			double prevScore = -1.0;
			int i = 0;
			while(i < testInstances.length){
				if(testInstances[i][1] != prevScore){
					double[] rocPoint = new double[2];
					//FPR
					rocPoint[0] = (double) fp/numNegative;
					//TPR
					rocPoint[1] = (double) tp/numPositive;
					rocCurve.push(rocPoint);
					prevScore = testInstances[i][1];
				}
				
				if(((int) testInstances[i][0]) == 1.0) {
					tp++;
				}
				else{
					fp++;
				}
				i++;
			}//End while
			//(1,1)
			double[] rocPoint = new double[2];
			rocPoint[0] = (double) fp/numNegative;
			rocPoint[1] = (double) tp/numPositive;
			rocCurve.push(rocPoint);
			
			computeAreaUnderROC();
		}
	}
	
	private void computeAreaUnderROC() {
		computeMannWhitneyAUC();
	}
	
	private void computeMannWhitneyAUC() {
		int[] labels = new int[testInstances.length];
		double[] scores = new double[testInstances.length];
		
		for (int i=0; i<labels.length; ++i) {
			int thisLabel = (int) testInstances[i][0];
			if(thisLabel == 1.0) {
				labels[i] = 1;
			}
			else {
				labels[i] = 0;
			}
			scores[i] = testInstances[i][1];
		}
		
		double n1 = numPositive;
		double n2 = numNegative;
		double U1 = computeUPositive();
		auroc = U1/(n1*n2);
	}
	
	private double computeUPositive() {
		double[] ranks = new double[testInstances.length];
		double[][] ascendingTestInstances = ascendingTestInstances();
		
		for(int i=0; i<ranks.length;) {
			double thisRank = i+1;
			double count = 1;
			int j = i+1;
			while(j<ranks.length){
				if(ascendingTestInstances[i][1] == ascendingTestInstances[j][1]) {
					thisRank+= j+1;
					count+= 1.0;
					j++;
				}
				else {
					break;
				}
			}
			for(int k=i; k<j; k++) {
				ranks[k] = thisRank/count;
			}
			i=j;
		}
		
		double RPositive = 0.0;
		for(int i=0; i<ascendingTestInstances.length; i++) {
			if((int) ascendingTestInstances[i][0] == 1.0) {
				RPositive+= ranks[i];
			}
		}
		
		double U = RPositive - (((double)this.numPositive*((double)this.numPositive + 1.0))/2.0);
		
		return U;
	}
	
	private double[][] ascendingTestInstances() {
		double[][] ascendingTestInstances = new double[testInstances.length][2];
		int count = 0;
		for(int i=testInstances.length-1; i>=0; i--) {
			ascendingTestInstances[count][0] = testInstances[i][0];
			ascendingTestInstances[count][1] = testInstances[i][1];
			count++;
		}
		
		return ascendingTestInstances;
	}
	
	public Stack<double[]> getROCCurve() {
		return rocCurve;
	}
	
	public double getAUROC() {
		return auroc;
	}
	
	
	private double[][] testInstances; //testInstance[#instances][0: actual; 1: predicted probability]
	private int numPositive;
	private int numNegative;
	
	private Stack<double[]> rocCurve;
	private double auroc;
}
