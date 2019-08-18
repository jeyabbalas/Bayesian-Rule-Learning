package org.probe.rls.test.data.fold;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.FileDataManager;
import org.probe.rls.data.FileType;
import org.probe.rls.data.fold.FoldGenerator;
import org.probe.rls.data.fold.TwoFoldGenerator;

public class TestTwoFoldGenerator {
	
	@Before
	public void init() {
		dataManager = new FileDataManager();
		try {
			dataManager.loadFromFile("Test//testDataFoldsFile.csv", FileType.CSV.getSeparator());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testTwoFoldGenerator() throws Exception{
		DataModel dataModel = dataManager.getDataModel();
		
		FoldGenerator foldGenerator = new TwoFoldGenerator();
		foldGenerator.generateFolds(dataModel);
		
		assertEquals(foldGenerator.numFolds(), 1);
		
		DataModel trainingDataModel = foldGenerator.getFold(0).get(0);
		assertEquals(trainingDataModel.isTrainData(),true);
		assertEquals(trainingDataModel.size(), 5);
		
		DataModel validationDataModel = foldGenerator.getFold(0).get(1);
		assertEquals(validationDataModel.isValidationData(), true);
		assertEquals(validationDataModel.size(), 5);
		
		assertTrue(trainingDataModel.doesNotContain(validationDataModel));
	}
	
	@Test
	public void testTwoFoldGenerator2() throws Exception{
		DataModel dataModel = dataManager.getDataModel();
		
		FoldGenerator foldGenerator = new TwoFoldGenerator();
		foldGenerator.generateFolds(dataModel);
		
		assertEquals(foldGenerator.numFolds(), 1);
		
		DataModel trainingDataModel = foldGenerator.getFold(0).get(0);
		assertEquals(trainingDataModel.isTrainData(),true);
		assertEquals(trainingDataModel.size(), 5);
		assertTrue(foldIsStratifiedByClass(trainingDataModel));
		
		DataModel validationDataModel = foldGenerator.getFold(0).get(1);
		assertEquals(validationDataModel.isValidationData(), true);
		assertEquals(validationDataModel.size(), 5);
		assertTrue(foldIsStratifiedByClass(validationDataModel));
		
		assertTrue(trainingDataModel.doesNotContain(validationDataModel));
	}
	
	private boolean foldIsStratifiedByClass(DataModel fold){
		boolean foldIsStratified = false;
		Map<String, Integer> labelDistribution = countLabelDistribution(fold.getClassColumn());
		
		if(labelDistribution.get("Pos") == 3 && 
				labelDistribution.get("Neg") == 2){
			foldIsStratified = true;
		}
		
		return foldIsStratified;
	}
	
	private Map<String, Integer> countLabelDistribution(List<String> labelsList){
		Map<String, Integer> labelDistribution = new HashMap<String,Integer>();
		labelDistribution.put("Pos", 0);
		labelDistribution.put("Neg", 0);
		
		for(String label: labelsList){
			if(label.equalsIgnoreCase("Pos")){
				labelDistribution.put("Pos", labelDistribution.get("Pos") + 1);
			}
			else if(label.equalsIgnoreCase("Neg")){
				labelDistribution.put("Neg", labelDistribution.get("Neg") + 1);
			}
		}
		
		return labelDistribution;
	}
	
	@Test
	public void testTwoFoldGenerator3() throws Exception{
		DataModel dataModel = dataManager.getDataModel();
		
		FoldGenerator foldGenerator = new TwoFoldGenerator(0.7);
		foldGenerator.generateFolds(dataModel);
		
		assertEquals(foldGenerator.numFolds(), 1);
		
		DataModel trainingDataModel = foldGenerator.getFold(0).get(0);
		assertEquals(trainingDataModel.isTrainData(),true);
		assertEquals(trainingDataModel.size(), 7);
		
		DataModel validationDataModel = foldGenerator.getFold(0).get(1);
		assertEquals(validationDataModel.isValidationData(), true);
		assertEquals(validationDataModel.size(), 3);
		
		assertTrue(trainingDataModel.doesNotContain(validationDataModel));
	}

	
	private FileDataManager dataManager;
}
