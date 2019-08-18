package org.probe.rls.test.data.fold;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.probe.rls.data.DataAttribute;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.FileDataManager;
import org.probe.rls.data.FileType;
import org.probe.rls.data.fold.FoldGenerator;
import org.probe.rls.data.fold.BootstrapFoldGenerator;

public class TestBootstrapFoldGenerator {

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
	public void testBootstrapFoldGenerator() throws Exception{
		DataModel dataModel = dataManager.getDataModel();
		
		FoldGenerator foldGenerator = new BootstrapFoldGenerator(5);
		foldGenerator.generateFolds(dataModel);
		
		assertEquals(foldGenerator.numFolds(), 5);
		
		for(int foldID=0; foldID<foldGenerator.numFolds(); foldID++) {
			DataModel trainingDataModel = foldGenerator.getFold(foldID).get(0);
			assertEquals(trainingDataModel.isTrainData(),true);
			assertEquals(trainingDataModel.size(), 10);
			
			DataModel validationDataModel = foldGenerator.getFold(foldID).get(1);
			assertEquals(validationDataModel.isValidationData(), true);
			assertTrue(sizeEqualsTotalRepeatsInTrain(trainingDataModel, validationDataModel));
			
			assertTrue(trainingDataModel.doesNotContain(validationDataModel));
		}
	}
	
	private boolean sizeEqualsTotalRepeatsInTrain(DataModel train, DataModel validation){
		DataAttribute trainIDAttribute = train.getInstanceAttribute();
		List<String> trainSampleIDs = train.getColumnItemsByAttribute(trainIDAttribute);
		int repeats = countRepeats(trainSampleIDs);
		
		return (validation.size() == repeats);
	}
	
	private int countRepeats(List<String> items){
		int repeats = 0;
		Set<Integer> idsSeen = new HashSet<Integer>();
		
		for(int i = 0; i < items.size(); i++){
			idsSeen.add(i);
			for(int j = 0; j < items.size(); j++){
				if(idsSeen.contains(j)) continue;
				if(items.get(i).contentEquals(items.get(j))){ 
					repeats++;
					idsSeen.add(j);
				}
			}
		}
		
		return repeats;
	}
	
	private FileDataManager dataManager;
}
