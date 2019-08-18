package org.probe.rls.algo.rule.brl.util;

import org.apache.commons.math3.special.Gamma;

public class BayesianScore {
	
	
	private static double lnGammaRatio(double numer, double denom) {
    	if (numer == 0.0 || denom == 0.0) return 0.0;
        return Gamma.logGamma(numer) - Gamma.logGamma(denom);
    }
	
	public static double lnBDeuScoreForLeaf(double[] classCounts, double[] alpha) {
		double score = 0.0;
		
		for (int k = 0; k < classCounts.length; k++) {
			score += lnGammaRatio((alpha[k] + classCounts[k]), 
					alpha[k]);
		}
		score += lnGammaRatio(sumDoubleArray(alpha), 
				(sumDoubleArray(alpha) + sumDoubleArray(classCounts)));
		
		return score;
	}
	
	public static double[] bdeuParameterPosteriorForLeaf(double[] classCounts, double[] alpha) {
		double[] parameterPosterior = new double[classCounts.length];
		
		for(int i=0; i<classCounts.length; i++) {
			parameterPosterior[i] = 
					(classCounts[i] + alpha[i])/(sumDoubleArray(classCounts) + sumDoubleArray(alpha));
		}
		
		return parameterPosterior;
	}
	
	private static double sumDoubleArray(double[] doubleArray) {
		double sum = 0.0;
		for(double element: doubleArray) {
			sum += element;
		}
		
		return sum;
	}
	
	
	
}
