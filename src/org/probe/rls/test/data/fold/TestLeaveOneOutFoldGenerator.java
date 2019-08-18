package org.probe.rls.test.data.fold;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.FileDataManager;
import org.probe.rls.data.FileType;
import org.probe.rls.data.fold.FoldGenerator;
import org.probe.rls.data.fold.LeaveOneOutFoldGenerator;

public class TestLeaveOneOutFoldGenerator {

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
	public void testLeaveOneOutFoldGenerator() throws Exception{
		DataModel dataModel = dataManager.getDataModel();
		
		FoldGenerator foldGenerator = new LeaveOneOutFoldGenerator();
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
	
	
	
	private FileDataManager dataManager;
}
