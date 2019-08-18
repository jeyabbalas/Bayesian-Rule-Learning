package org.probe.rls.test.algo.brl;

import org.junit.Before;
import org.junit.Test;
import org.probe.rls.algo.rule.brl.BRLSearchParameters;
import org.probe.rls.algo.rule.brl.datastructures.ConstrainedBayesianNetwork;
import org.probe.rls.algo.rule.brl.util.BRLUtils;
import org.probe.rls.data.DataAttribute;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.FileDataManager;
import org.probe.rls.data.FileType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

public class TestConstrainedBayesianNetwork {

	@Before
	public void init() {
		dataManager = new FileDataManager();
		try {
			dataManager.loadFromFile("Test//id3dataset.csv", FileType.CSV.getSeparator());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testConstrainedBayesianNetworkConstruction() {
		DataModel dataModel = dataManager.getDataModel();
		BRLSearchParameters params = new BRLSearchParameters();
		ConstrainedBayesianNetwork cbn = new ConstrainedBayesianNetwork(dataModel, params);
		
		//System.out.println(cbn.getClassLocalStructure().toString());
		
		assertEquals(cbn.getClassLocalStructure().getNumLeaves(), 1);
		//System.out.println(cbn.getNetworkPosteriorProbability());
	}

	@Test
	public void testConstrainedBayesianNetworkDeepCopy() {
		//40 times slower than split operations. :(
		DataModel dataModel = dataManager.getDataModel();
		BRLSearchParameters params = new BRLSearchParameters();
		ConstrainedBayesianNetwork cbn1 = new ConstrainedBayesianNetwork(dataModel, params);
		ConstrainedBayesianNetwork cbn2 = cbn1.copyByValue();
		
		//System.out.println("CBN 1:");
		//System.out.println(cbn1.getClassLocalStructure().toString());
		//System.out.println("CBN 2:");
		//System.out.println(cbn2.getClassLocalStructure().toString());
		
		assertFalse(cbn1.equals(cbn2));
	}
	
	@Test
	public void testCompleteSplit() {
		DataModel dataModel = dataManager.getDataModel();
		BRLSearchParameters params = new BRLSearchParameters();
		ConstrainedBayesianNetwork cbn = new ConstrainedBayesianNetwork(dataModel, params);
		//System.out.println("Before score: "+cbn.getNetworkPosteriorProbability());
		
		DataAttribute tempAttribute = dataModel.getAttributes().get(1);
		cbn.applyCompleteSplitOperatorOnLocalStructure(0, tempAttribute);
		//System.out.println(cbn.getClassLocalStructure().toString());
		//System.out.println("After temp parent score: "+cbn.getNetworkPosteriorProbability());
		
		assertEquals(cbn.getClassParents().size(), 1);
		assertTrue(cbn.getClassParents().contains(tempAttribute));
		
		assertFalse(cbn.applyCompleteSplitOperatorOnLocalStructure(0, tempAttribute));
		
		
		DataAttribute nauseaAttribute = dataModel.getAttributes().get(2);
		cbn.applyCompleteSplitOperatorOnLocalStructure(0, nauseaAttribute);
		//System.out.println(cbn.getClassLocalStructure().toString());
		//System.out.println("After temp and nausea parent score: "+cbn.getNetworkPosteriorProbability());
		
		assertEquals(cbn.getClassParents().size(), 2);
		assertTrue(cbn.getClassParents().contains(tempAttribute));
		assertTrue(cbn.getClassParents().contains(nauseaAttribute));
	}
	
	@Test
	public void testBinarySplit() {
		DataModel dataModel = dataManager.getDataModel();
		BRLSearchParameters params = new BRLSearchParameters();
		ConstrainedBayesianNetwork cbn = new ConstrainedBayesianNetwork(dataModel, params);
		
		DataAttribute tempAttribute = dataModel.getAttributes().get(1);
		
		List<List<List<String>>> combinations = 
				BRLUtils.partitionIntoOrderedBinarySetCombinations(tempAttribute.getAttributeValues());
		List<String> attributeValues1 = combinations.get(0).get(0);
		List<String> attributeValues2 = combinations.get(0).get(1);
		
		cbn.applyBinarySplitOperatorOnLocalStructure(0, tempAttribute, attributeValues1, attributeValues2);
		
		assertEquals(cbn.getClassParents().size(), 1);
		assertTrue(cbn.getClassParents().contains(tempAttribute));
		
		assertFalse(cbn.applyBinarySplitOperatorOnLocalStructure(0, tempAttribute, attributeValues1, attributeValues2));
		
		DataAttribute nauseaAttribute = dataModel.getAttributes().get(2);
		combinations = 
				BRLUtils.partitionIntoOrderedBinarySetCombinations(nauseaAttribute.getAttributeValues());
		attributeValues1 = combinations.get(0).get(0);
		attributeValues2 = combinations.get(0).get(1);
		cbn.applyBinarySplitOperatorOnLocalStructure(1, nauseaAttribute, attributeValues1, attributeValues2);
		
		assertEquals(cbn.getClassParents().size(), 2);
		assertTrue(cbn.getClassParents().contains(tempAttribute));
		assertTrue(cbn.getClassParents().contains(nauseaAttribute));
		//System.out.println(cbn.getClassLocalStructure().toString());
	}
	
	@Test
	public void testMerge() {
		DataModel dataModel = dataManager.getDataModel();
		BRLSearchParameters params = new BRLSearchParameters();
		ConstrainedBayesianNetwork cbn = new ConstrainedBayesianNetwork(dataModel, params);
		
		DataAttribute tempAttribute = dataModel.getAttributes().get(1);
		cbn.applyCompleteSplitOperatorOnLocalStructure(0, tempAttribute);
		DataAttribute nauseaAttribute = dataModel.getAttributes().get(2);
		cbn.applyCompleteSplitOperatorOnLocalStructure(0, nauseaAttribute);
		cbn.applyCompleteSplitOperatorOnLocalStructure(2, nauseaAttribute);
		cbn.applyCompleteSplitOperatorOnLocalStructure(4, nauseaAttribute);
		//System.out.println(cbn.getClassLocalStructure().toString());
		
		assertEquals(cbn.getClassLocalStructure().getNumLeaves(), 6);
		
		int numLeavesBefore = cbn.getClassLocalStructure().getNumLeaves();
		
		assertFalse(cbn.applyMergeOperatorOnLocalStructure(0, 1));
		assertFalse(cbn.applyMergeOperatorOnLocalStructure(2, 3));
		assertFalse(cbn.applyMergeOperatorOnLocalStructure(4, 5));
		assertTrue(cbn.applyMergeOperatorOnLocalStructure(1, 2));
		//System.out.println(cbn.getClassLocalStructure().toString());
		
		int numLeavesAfter = cbn.getClassLocalStructure().getNumLeaves();
		assertEquals((numLeavesBefore-1), numLeavesAfter);
		
		assertFalse(cbn.applyMergeOperatorOnLocalStructure(0, 1));
		assertFalse(cbn.applyMergeOperatorOnLocalStructure(1, 2));
		assertTrue(cbn.applyMergeOperatorOnLocalStructure(1, 3));
		//System.out.println(cbn.getClassLocalStructure().toString());
		
		DataAttribute lumbarAttribute = dataModel.getAttributes().get(3);
		cbn.applyCompleteSplitOperatorOnLocalStructure(1, lumbarAttribute);
		//System.out.println(cbn.getClassLocalStructure().toString());
	}
	
	@Test
	public void testConstrainedBayesianNetworkDeepCopy2() {
		DataModel dataModel = dataManager.getDataModel();
		BRLSearchParameters params = new BRLSearchParameters();
		ConstrainedBayesianNetwork cbn1 = new ConstrainedBayesianNetwork(dataModel, params);
		
		DataAttribute attribute = dataModel.getAttributes().get(1);
		cbn1.applyCompleteSplitOperatorOnLocalStructure(0, attribute);
		
		attribute = dataModel.getAttributes().get(2);
		cbn1.applyCompleteSplitOperatorOnLocalStructure(0, attribute);
		cbn1.applyCompleteSplitOperatorOnLocalStructure(2, attribute);
		cbn1.applyCompleteSplitOperatorOnLocalStructure(4, attribute);
		cbn1.applyMergeOperatorOnLocalStructure(1, 2);
		cbn1.applyMergeOperatorOnLocalStructure(1, 3);
		
		attribute = dataModel.getAttributes().get(3);
		cbn1.applyCompleteSplitOperatorOnLocalStructure(1, attribute);
		
		ConstrainedBayesianNetwork cbn2 = cbn1.copyByValue();
		
		//System.out.println("CBN 1:");
		//System.out.println(cbn1.toString());
		//System.out.println("CBN 2:");
		//System.out.println(cbn2.toString());
		
		assertFalse(cbn1.equals(cbn2));
	}
	
	
	private FileDataManager dataManager;
}
