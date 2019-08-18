package org.probe.rls.test.data.fold;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.probe.rls.data.DataAttribute;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.FileDataManager;
import org.probe.rls.data.FileType;
import org.probe.rls.data.fold.FoldGenerator;
import org.probe.rls.data.fold.ManualFoldGenerator;

public class TestManualFoldGenerator {

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
	public void testManualFoldGenerator() throws Exception{
		DataModel dataModel = dataManager.getDataModel();
		
		List<Integer> trainIDs = getIntegerExampleTrainSet();
		FoldGenerator foldGenerator = ManualFoldGenerator.fromInstanceIndices(trainIDs, null);
		foldGenerator.generateFolds(dataModel);
		
		assertEquals(foldGenerator.numFolds(), 1);
		
		DataModel trainingDataModel = foldGenerator.getFold(0).get(0);
		assertEquals(trainingDataModel.isTrainData(),true);
		assertEquals(trainingDataModel.size(), 6);
		assertTrue(checkTrainSampleIDs1(trainingDataModel));
		
		DataModel validationDataModel = foldGenerator.getFold(0).get(1);
		assertEquals(validationDataModel.isValidationData(), true);
		assertEquals(validationDataModel.size(), 4);
		assertTrue(checkTestSampleIDs1(validationDataModel));
		
		assertTrue(trainingDataModel.doesNotContain(validationDataModel));
	}
	
	@Test
	public void testManualFoldGenerator2() throws Exception{
		DataModel dataModel = dataManager.getDataModel();
		
		List<String> trainIDs = getStringExampleTrainSet();
		FoldGenerator foldGenerator = ManualFoldGenerator.fromInstanceNames(trainIDs, null);
		foldGenerator.generateFolds(dataModel);
		
		assertEquals(foldGenerator.numFolds(), 1);
		
		DataModel trainingDataModel = foldGenerator.getFold(0).get(0);
		assertEquals(trainingDataModel.isTrainData(),true);
		assertEquals(trainingDataModel.size(), 6);
		assertTrue(checkTrainSampleIDs1(trainingDataModel));
		
		DataModel validationDataModel = foldGenerator.getFold(0).get(1);
		assertEquals(validationDataModel.isValidationData(), true);
		assertEquals(validationDataModel.size(), 4);
		assertTrue(checkTestSampleIDs1(validationDataModel));
		
		assertTrue(trainingDataModel.doesNotContain(validationDataModel));
	}
	
	private List<Integer> getIntegerExampleTrainSet(){
		List<Integer> trainIDs = new LinkedList<Integer>();
		
		trainIDs.add(0);
		trainIDs.add(1);
		trainIDs.add(2);
		trainIDs.add(3);
		trainIDs.add(4);
		trainIDs.add(5);
		
		return trainIDs;
	}
	
	private boolean checkTrainSampleIDs1(DataModel data){
		boolean perfectMatch = true;
		DataAttribute idAttribute = data.getInstanceAttribute();
		List<String> sampleIDs = data.getColumnItemsByAttribute(idAttribute);
		
		if(!sampleIDs.get(0).contentEquals("1")) perfectMatch = false;
		if(!sampleIDs.get(1).contentEquals("2")) perfectMatch = false;
		if(!sampleIDs.get(2).contentEquals("3")) perfectMatch = false;
		if(!sampleIDs.get(3).contentEquals("4")) perfectMatch = false;
		if(!sampleIDs.get(4).contentEquals("5")) perfectMatch = false;
		if(!sampleIDs.get(5).contentEquals("6")) perfectMatch = false;
		
		return perfectMatch;
	}
	
	private boolean checkTestSampleIDs1(DataModel data){
		boolean perfectMatch = true;
		DataAttribute idAttribute = data.getInstanceAttribute();
		List<String> sampleIDs = data.getColumnItemsByAttribute(idAttribute);
		
		if(!sampleIDs.get(0).contentEquals("7")) perfectMatch = false;
		if(!sampleIDs.get(1).contentEquals("8")) perfectMatch = false;
		if(!sampleIDs.get(2).contentEquals("9")) perfectMatch = false;
		if(!sampleIDs.get(3).contentEquals("10")) perfectMatch = false;
		
		return perfectMatch;
	}
	
	private List<String> getStringExampleTrainSet(){
		List<String> trainIDs = new LinkedList<String>();
		
		trainIDs.add("1");
		trainIDs.add("2");
		trainIDs.add("3");
		trainIDs.add("4");
		trainIDs.add("5");
		trainIDs.add("6");
		
		return trainIDs;
	}
	
	@Test
	public void testManualFoldGenerator3() throws Exception{
		DataModel dataModel = dataManager.getDataModel();
		
		List<Integer> testIDs = getIntegerExampleTestSet();
		FoldGenerator foldGenerator = ManualFoldGenerator.fromInstanceIndices(null, testIDs);
		foldGenerator.generateFolds(dataModel);
		
		assertEquals(foldGenerator.numFolds(), 1);
		
		DataModel trainingDataModel = foldGenerator.getFold(0).get(0);
		assertEquals(trainingDataModel.isTrainData(),true);
		assertEquals(trainingDataModel.size(), 7);
		assertTrue(checkTrainSampleIDs2(trainingDataModel));
		
		DataModel validationDataModel = foldGenerator.getFold(0).get(1);
		assertEquals(validationDataModel.isValidationData(), true);
		assertEquals(validationDataModel.size(), 3);
		assertTrue(checkTestSampleIDs2(validationDataModel));
		
		assertTrue(trainingDataModel.doesNotContain(validationDataModel));
	}
	
	@Test
	public void testManualFoldGenerator4() throws Exception{
		DataModel dataModel = dataManager.getDataModel();
		
		List<String> testIDs = getStringExampleTestSet();
		FoldGenerator foldGenerator = ManualFoldGenerator.fromInstanceNames(null, testIDs);
		foldGenerator.generateFolds(dataModel);
		
		assertEquals(foldGenerator.numFolds(), 1);
		
		DataModel trainingDataModel = foldGenerator.getFold(0).get(0);
		assertEquals(trainingDataModel.isTrainData(),true);
		assertEquals(trainingDataModel.size(), 7);
		assertTrue(checkTrainSampleIDs2(trainingDataModel));
		
		DataModel validationDataModel = foldGenerator.getFold(0).get(1);
		assertEquals(validationDataModel.isValidationData(), true);
		assertEquals(validationDataModel.size(), 3);
		assertTrue(checkTestSampleIDs2(validationDataModel));
		
		assertTrue(trainingDataModel.doesNotContain(validationDataModel));
	}
	
	private List<Integer> getIntegerExampleTestSet(){
		List<Integer> testIDs = new LinkedList<Integer>();
		
		testIDs.add(7);
		testIDs.add(8);
		testIDs.add(9);
		
		return testIDs;
	}
	
	private boolean checkTrainSampleIDs2(DataModel data){
		boolean perfectMatch = true;
		DataAttribute idAttribute = data.getInstanceAttribute();
		List<String> sampleIDs = data.getColumnItemsByAttribute(idAttribute);
		
		if(!sampleIDs.get(0).contentEquals("1")) perfectMatch = false;
		if(!sampleIDs.get(1).contentEquals("2")) perfectMatch = false;
		if(!sampleIDs.get(2).contentEquals("3")) perfectMatch = false;
		if(!sampleIDs.get(3).contentEquals("4")) perfectMatch = false;
		if(!sampleIDs.get(4).contentEquals("5")) perfectMatch = false;
		if(!sampleIDs.get(5).contentEquals("6")) perfectMatch = false;
		if(!sampleIDs.get(6).contentEquals("7")) perfectMatch = false;
		
		return perfectMatch;
	}
	
	private boolean checkTestSampleIDs2(DataModel data){
		boolean perfectMatch = true;
		DataAttribute idAttribute = data.getInstanceAttribute();
		List<String> sampleIDs = data.getColumnItemsByAttribute(idAttribute);
		
		if(!sampleIDs.get(0).contentEquals("8")) perfectMatch = false;
		if(!sampleIDs.get(1).contentEquals("9")) perfectMatch = false;
		if(!sampleIDs.get(2).contentEquals("10")) perfectMatch = false;
		
		return perfectMatch;
	}
	
	private List<String> getStringExampleTestSet(){
		List<String> testIDs = new LinkedList<String>();
		
		testIDs.add("8");
		testIDs.add("9");
		testIDs.add("10");
		
		return testIDs;
	}
	
	@Test
	public void testManualFoldGenerator5() throws Exception{
		DataModel dataModel = dataManager.getDataModel();
		
		List<Integer> trainIDs = getIntegerExampleTrainSet();
		List<Integer> testIDs = getIntegerExampleTestSet();
		FoldGenerator foldGenerator = ManualFoldGenerator.fromInstanceIndices(trainIDs, testIDs);
		foldGenerator.generateFolds(dataModel);
		
		assertEquals(foldGenerator.numFolds(), 1);
		
		DataModel trainingDataModel = foldGenerator.getFold(0).get(0);
		assertEquals(trainingDataModel.isTrainData(),true);
		assertEquals(trainingDataModel.size(), 6);
		assertTrue(checkTrainSampleIDs3(trainingDataModel));
		
		DataModel validationDataModel = foldGenerator.getFold(0).get(1);
		assertEquals(validationDataModel.isValidationData(), true);
		assertEquals(validationDataModel.size(), 3);
		assertTrue(checkTestSampleIDs3(validationDataModel));
		
		assertTrue(trainingDataModel.doesNotContain(validationDataModel));
	}
	
	@Test
	public void testManualFoldGenerator6() throws Exception{
		DataModel dataModel = dataManager.getDataModel();
		
		List<String> trainIDs = getStringExampleTrainSet();
		List<String> testIDs = getStringExampleTestSet();
		FoldGenerator foldGenerator = ManualFoldGenerator.fromInstanceNames(trainIDs, testIDs);
		foldGenerator.generateFolds(dataModel);
		
		assertEquals(foldGenerator.numFolds(), 1);
		
		DataModel trainingDataModel = foldGenerator.getFold(0).get(0);
		assertEquals(trainingDataModel.isTrainData(),true);
		assertEquals(trainingDataModel.size(), 6);
		assertTrue(checkTrainSampleIDs3(trainingDataModel));
		
		DataModel validationDataModel = foldGenerator.getFold(0).get(1);
		assertEquals(validationDataModel.isValidationData(), true);
		assertEquals(validationDataModel.size(), 3);
		assertTrue(checkTestSampleIDs3(validationDataModel));
		
		assertTrue(trainingDataModel.doesNotContain(validationDataModel));
	}
	
	private boolean checkTrainSampleIDs3(DataModel data){
		boolean perfectMatch = true;
		DataAttribute idAttribute = data.getInstanceAttribute();
		List<String> sampleIDs = data.getColumnItemsByAttribute(idAttribute);
		
		if(!sampleIDs.get(0).contentEquals("1")) perfectMatch = false;
		if(!sampleIDs.get(1).contentEquals("2")) perfectMatch = false;
		if(!sampleIDs.get(2).contentEquals("3")) perfectMatch = false;
		if(!sampleIDs.get(3).contentEquals("4")) perfectMatch = false;
		if(!sampleIDs.get(4).contentEquals("5")) perfectMatch = false;
		if(!sampleIDs.get(5).contentEquals("6")) perfectMatch = false;
		
		return perfectMatch;
	}
	
	private boolean checkTestSampleIDs3(DataModel data){
		boolean perfectMatch = true;
		DataAttribute idAttribute = data.getInstanceAttribute();
		List<String> sampleIDs = data.getColumnItemsByAttribute(idAttribute);
		
		if(!sampleIDs.get(0).contentEquals("8")) perfectMatch = false;
		if(!sampleIDs.get(1).contentEquals("9")) perfectMatch = false;
		if(!sampleIDs.get(2).contentEquals("10")) perfectMatch = false;
		
		return perfectMatch;
	}
	
	private FileDataManager dataManager;
}
