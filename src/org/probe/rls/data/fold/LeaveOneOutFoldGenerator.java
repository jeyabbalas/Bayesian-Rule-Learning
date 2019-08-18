package org.probe.rls.data.fold;

import java.util.LinkedList;
import java.util.List;

import org.probe.rls.data.DataModel;
import org.probe.rls.data.DefaultDataModel;

public class LeaveOneOutFoldGenerator implements FoldGenerator {
	
	public LeaveOneOutFoldGenerator() {
	}

	@Override
	public void generateFolds(DataModel dataModel) {
		this.dataModel = dataModel;
	}
	
	@Override
	public int numFolds() {
		return dataModel.getNumRows();
	}

	@Override
	public List<DataModel> getFold(int foldID) {
		DataModel trainDataModel = createTrainingDataModel(foldID, dataModel);
		trainDataModel.setDataSetAsTraining();
		
		DataModel valDataModel = createValidationDataModel(foldID, dataModel);
		valDataModel.setDataSetAsValidation();
		
		List<DataModel> fold = new LinkedList<DataModel>();
		fold.add(trainDataModel);
		fold.add(valDataModel);
		
		return fold;
	}

	private DataModel createValidationDataModel(int rowIndex,
			DataModel dataModel) {
		DataModel valDataModel = new DefaultDataModel(dataModel);

		List<String> items = dataModel.getRow(rowIndex);
		
		valDataModel.addItemsAsRow(new LinkedList<String>(items));
		valDataModel.setDataSetAsValidation();

		return valDataModel;
	}

	private DataModel createTrainingDataModel(int rowIndex,
			DataModel dataModel) {
		DataModel trainDataModel = new DefaultDataModel(dataModel);

		for (int x = 0; x < dataModel.size(); x++) {
			if (x == rowIndex)
				continue;

			List<String> items = dataModel.getRow(x);
			
			trainDataModel.addItemsAsRow(new LinkedList<String>(items));
		}
		trainDataModel.setDataSetAsTraining();
		
		return trainDataModel;
	}
	
	
	private DataModel dataModel;
}
