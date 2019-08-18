package org.probe.rls.test.utils;


import org.junit.Test;
import org.probe.rls.util.stats.distribution.Dirichlet;


public class TestDirichlet {

	@Test
	public void testDirichletSampling() {
		double[] alphas = new double[10];
		
		for(int i=0; i<alphas.length; i++) {
			alphas[i] = 1.0;
		}
		
		Dirichlet dir = new Dirichlet(alphas);
		
		for(int j=0; j<3; j++) {
			double[] sample = dir.drawSample();
			for(int i=0; i<sample.length; i++) {
				System.out.print(sample[i]+"\t");
			}
			System.out.println();
		}
	}
}
