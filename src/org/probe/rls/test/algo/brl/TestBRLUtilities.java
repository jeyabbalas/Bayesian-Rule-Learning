package org.probe.rls.test.algo.brl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.probe.rls.algo.rule.brl.util.BRLUtils;
import org.probe.rls.algo.rule.brl.util.BayesianScore;

public class TestBRLUtilities {

	@Test
	public void testBDeuScore() {
		double[] classCounts = new double[2];
		double[] alpha = new double[2];
		
		classCounts[0] = 59.0;
		classCounts[1] = 61.0;
		alpha[0] = 1.0/(2.0*1.0);
		alpha[1] = 1.0/(2.0*1.0);
		
		double score = BayesianScore.lnBDeuScoreForLeaf(classCounts, alpha);
		
		assertEquals(-85.7826, score, 0.0001);
	}
	
	@Test
	public void testBrlGssExample() {
		double[] classCountsLeaf1 = new double[2];
		double[] classCountsLeaf2 = new double[2];
		double[] classCountsLeaf3 = new double[2];
		double[] classCountsLeaf4 = new double[2];
		double[] alpha = new double[2];
		
		alpha[0] = 1.0/(4.0*2.0);
		alpha[1] = 1.0/(4.0*2.0);
		
		classCountsLeaf1[0] = 25.0;
		classCountsLeaf1[1] = 0.0;
		classCountsLeaf2[0] = 10.0;
		classCountsLeaf2[1] = 20.0;
		classCountsLeaf3[0] = 5.0;
		classCountsLeaf3[1] = 10.0;
		classCountsLeaf4[0] = 5.0;
		classCountsLeaf4[1] = 10.0;
		
		
		double score = BayesianScore.lnBDeuScoreForLeaf(classCountsLeaf1, alpha) +
				BayesianScore.lnBDeuScoreForLeaf(classCountsLeaf2, alpha) +
				BayesianScore.lnBDeuScoreForLeaf(classCountsLeaf3, alpha) +
				BayesianScore.lnBDeuScoreForLeaf(classCountsLeaf4, alpha);
		
		System.out.println(score);
	}
	
	@Test
	public void testBrlLssDtExample() {
		double[] classCountsLeaf1 = new double[2];
		double[] classCountsLeaf2 = new double[2];
		double[] classCountsLeaf3 = new double[2];
		double[] alpha = new double[2];
		
		alpha[0] = 1.0/(4.0*2.0);
		alpha[1] = 1.0/(4.0*2.0);
		
		classCountsLeaf1[0] = 25.0;
		classCountsLeaf1[1] = 0.0;
		classCountsLeaf2[0] = 10.0;
		classCountsLeaf2[1] = 20.0;
		classCountsLeaf3[0] = 10.0;
		classCountsLeaf3[1] = 20.0;
		
		
		double score = BayesianScore.lnBDeuScoreForLeaf(classCountsLeaf1, alpha) +
				BayesianScore.lnBDeuScoreForLeaf(classCountsLeaf2, alpha) +
				BayesianScore.lnBDeuScoreForLeaf(classCountsLeaf3, alpha);
		
		System.out.println(score);
	}
	
	@Test
	public void testBrlLssDgExample() {
		double[] classCountsLeaf1 = new double[2];
		double[] classCountsLeaf2 = new double[2];
		double[] alpha = new double[2];
		
		alpha[0] = 1.0/(4.0*2.0);
		alpha[1] = 1.0/(4.0*2.0);
		
		classCountsLeaf1[0] = 25.0;
		classCountsLeaf1[1] = 0.0;
		classCountsLeaf2[0] = 20.0;
		classCountsLeaf2[1] = 40.0;
		
		
		double score = BayesianScore.lnBDeuScoreForLeaf(classCountsLeaf1, alpha) +
				BayesianScore.lnBDeuScoreForLeaf(classCountsLeaf2, alpha);
		
		System.out.println(score);
	}
	
	@Test
	public void testBDeuParameterExpectation() {
		double[] classCounts = new double[2];
		double[] alpha = new double[2];
		
		classCounts[0] = 9.0;
		classCounts[1] = 11.0;
		alpha[0] = 1.0/(5.0*2.0);
		alpha[1] = 1.0/(5.0*2.0);
		
		double[] parameterDistribution = BayesianScore.bdeuParameterPosteriorForLeaf(classCounts, alpha);
		
		assertEquals(0.4505, parameterDistribution[0], 0.0001);
		assertEquals(0.5495, parameterDistribution[1], 0.0001);
	}
	
	@Test
	public void testBinarySetSplit1() {
		List<String> itemsList = new LinkedList<String>();
		itemsList.add("Low");
		itemsList.add("Medium");
		itemsList.add("High");
		
		List<List<List<String>>> combinations = 
				BRLUtils.partitionIntoOrderedBinarySetCombinations(itemsList);
		
		assertEquals(2, combinations.size());
		
		assertEquals(1, combinations.get(0).get(0).size());
		assertTrue(combinations.get(0).get(0).contains("Low"));
		assertEquals(2, combinations.get(0).get(1).size());
		assertTrue(combinations.get(0).get(1).contains("Medium"));
		assertTrue(combinations.get(0).get(1).contains("High"));
		
		assertEquals(2, combinations.get(1).get(0).size());
		assertTrue(combinations.get(1).get(0).contains("Low"));
		assertTrue(combinations.get(1).get(0).contains("Medium"));
		assertEquals(1, combinations.get(1).get(1).size());
		assertTrue(combinations.get(1).get(1).contains("High"));
	}
	
	@Test
	public void testBinarySetSplit2() {
		List<String> itemsList = new LinkedList<String>();
		itemsList.add("Low");
		itemsList.add("Medium");
		itemsList.add("High");
		
		List<List<List<String>>> combinations = 
				BRLUtils.partitionIntoAllBinarySetCombinations(itemsList);
		
		assertEquals(3, combinations.size());
		
		assertEquals(1, combinations.get(0).get(0).size());
		assertTrue(combinations.get(0).get(0).contains("High"));
		assertEquals(2, combinations.get(0).get(1).size());
		assertTrue(combinations.get(0).get(1).contains("Low"));
		assertTrue(combinations.get(0).get(1).contains("Medium"));
		
		assertEquals(1, combinations.get(1).get(0).size());
		assertTrue(combinations.get(1).get(0).contains("Medium"));
		assertEquals(2, combinations.get(1).get(1).size());
		assertTrue(combinations.get(1).get(1).contains("Low"));
		assertTrue(combinations.get(1).get(1).contains("High"));
		
		assertEquals(2, combinations.get(2).get(0).size());
		assertTrue(combinations.get(2).get(0).contains("High"));
		assertTrue(combinations.get(2).get(0).contains("Medium"));
		assertEquals(1, combinations.get(2).get(1).size());
		assertTrue(combinations.get(2).get(1).contains("Low"));
		
	}
}
