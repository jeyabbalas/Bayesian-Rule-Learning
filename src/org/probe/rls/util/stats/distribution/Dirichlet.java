package org.probe.rls.util.stats.distribution;


import org.apache.commons.math3.distribution.GammaDistribution;


/**
 * Draws from a Dirichlet distribution per method in— 
 * https://en.wikipedia.org/wiki/Dirichlet_distribution#Random_number_generation
 * 
 * @author jeya
 *
 */
public class Dirichlet {

	public Dirichlet(double[] alphas) {
		this.alphas = alphas;
	}
	
	public double[] drawSample() {
		double[] sample = new double[alphas.length];
		
		for(int i=0; i<sample.length; i++) {
			GammaDistribution gamma = new GammaDistribution(alphas[i], 1.0);
			sample[i] = gamma.sample();
		}
		
		normalize(sample);
		
		return sample;
	}
	
	private void normalize(double[] items) {
		double sum = computeSum(items);
		
		for(int i=0; i<items.length; i++) {
			items[i] = items[i]/sum;
		}
	}
	
	private double computeSum(double[] items) {
		double sum = 0.0;
		
		for(int i=0; i<items.length; i++) {
			sum += items[i];
		}
		
		return sum;
	}
	
	private double[] alphas;
}
