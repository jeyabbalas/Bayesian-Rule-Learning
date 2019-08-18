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
import org.probe.rls.data.fold.KFoldGenerator;

public class TestKFoldGenerator {

	@Before
	public void init(){
		dataManager = new FileDataManager();
		try {
			dataManager.loadFromFile("Test//testDataFoldsFile.csv", FileType.CSV.getSeparator());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testKFoldGenerator() throws Exception{
		DataModel dataModel = dataManager.getDataModel();
		
		FoldGenerator foldGenerator = new KFoldGenerator();
		foldGenerator.generateFolds(dataModel);
		
		assertEquals(foldGenerator.numFolds(), 10);
		
		for(int foldID=0; foldID<foldGenerator.numFolds(); foldID++){
			DataModel trainingDataModel = foldGenerator.getFold(foldID).get(0);
			assertEquals(trainingDataModel.isTrainData(),true);
			assertEquals(trainingDataModel.size(), 9);
			
			DataModel validationDataModel = foldGenerator.getFold(foldID).get(1);
			assertEquals(validationDataModel.isValidationData(), true);
			assertEquals(validationDataModel.size(), 1);
			
			assertTrue(trainingDataModel.doesNotContain(validationDataModel));
		}
	}
	
	@Test
	public void testKFoldGenerator2() throws Exception{
		DataModel dataModel = dataManager.getDataModel();
		
		FoldGenerator foldGenerator = new KFoldGenerator(2);
		foldGenerator.generateFolds(dataModel);
		
		assertEquals(foldGenerator.numFolds(), 2);
		
		for(int foldID=0; foldID<foldGenerator.numFolds(); foldID++){
			DataModel trainingDataModel = foldGenerator.getFold(foldID).get(0);
			assertEquals(trainingDataModel.isTrainData(),true);
			assertEquals(trainingDataModel.size(), 5);
			assertTrue(foldIsStratifiedByClass(trainingDataModel));
			
			DataModel validationDataModel = foldGenerator.getFold(foldID).get(1);
			assertEquals(validationDataModel.isValidationData(), true);
			assertEquals(validationDataModel.size(), 5);
			assertTrue(foldIsStratifiedByClass(validationDataModel));
			
			assertTrue(trainingDataModel.doesNotContain(validationDataModel));
		}
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
	
	
	private FileDataManager dataManager;
}
