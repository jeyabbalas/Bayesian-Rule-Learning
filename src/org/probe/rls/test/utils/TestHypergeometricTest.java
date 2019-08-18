package org.probe.rls.test.utils;

import org.junit.Test;
import org.apache.commons.math3.distribution.HypergeometricDistribution;;

public class TestHypergeometricTest {

	@Test
	public void testHGT() {
		// Substitute values for N, K and n
		HypergeometricDistribution hgt = new HypergeometricDistribution(114736, 48, 42);
		
		//System.out.println(hgt.probability(4)+hgt.probability(5));
		// Substitute k value here, make sure you subtract 1.0
		System.out.println(1.0 - hgt.cumulativeProbability(1-1));
	}
}
