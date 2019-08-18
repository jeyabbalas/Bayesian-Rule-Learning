package org.probe.rls.test.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.FileDataManager;
import org.probe.rls.data.FileType;
import org.probe.rls.data.fold.FoldGenerator;
import org.probe.rls.data.fold.LeaveOneOutFoldGenerator;

public class TestDataManager {

	@Before
	public void init() {
		dataManager = new FileDataManager();
		try {
			dataManager.loadFromFile("Test//testDataFile.csv", FileType.CSV.getSeparator());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testLoadDataFromFiler() {
		FileDataManager dataManager = new FileDataManager();

		try {
			dataManager.loadFromFile("Test//testDataFile.csv", FileType.CSV.getSeparator());
		} catch (Exception e) {
			e.printStackTrace();
		}

		DataModel dataModel = dataManager.getDataModel();

		assertEquals("Down", dataModel.getItemAt(0, 3));
		assertEquals("Up", dataModel.getItemAt(2, 2));
		assertEquals("Pos", dataModel.getClassLabelForRow(0));

		List<String> itemsInColumn = dataModel.getColumnItemsByIndex(2);
		assertEquals("Up", itemsInColumn.get(0));
		assertEquals("Down", itemsInColumn.get(1));
		assertEquals("Up", itemsInColumn.get(2));
	}

	@Test
	public void testFoldCreation() throws Exception {
		FoldGenerator foldGenerator = new LeaveOneOutFoldGenerator();
		List<List<DataModel>> folds = dataManager.createFolds(foldGenerator);
		
		assertEquals(folds.size(), 10);
		
		for(List<DataModel> fold: folds){
			DataModel trainingDataModel = fold.get(0);
			assertEquals(trainingDataModel.isTrainData(),true);
			assertEquals(trainingDataModel.size(), 9);
			
			DataModel validationDataModel = fold.get(1);
			assertEquals(validationDataModel.isValidationData(), true);
			assertEquals(validationDataModel.size(), 1);
			
			assertTrue(trainingDataModel.doesNotContain(validationDataModel));
		}
		
		assert(false);
	}
	
	private FileDataManager dataManager;
}
