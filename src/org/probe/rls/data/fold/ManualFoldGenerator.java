package org.probe.rls.data.fold;

import java.util.LinkedList;
import java.util.List;

import org.probe.rls.data.DataAttribute;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.DefaultDataModel;

public class ManualFoldGenerator implements FoldGenerator {
	
	private ManualFoldGenerator() {
		
	}

	//Factory method that uses instance row numbers
	public static ManualFoldGenerator fromInstanceIndices(
			List<Integer> trainIDs, List<Integer> testIDs){
		ManualFoldGenerator foldGenerator = new ManualFoldGenerator();
		
		foldGenerator.setUseInstanceNames(false);
		foldGenerator.setTrainIDs(trainIDs);
		foldGenerator.setTestIDs(testIDs);
		
		return foldGenerator;
	}

	//Factory method that uses instance names
	public static ManualFoldGenerator fromInstanceNames(
			List<String> trainNames, List<String> testNames){
		ManualFoldGenerator foldGenerator = new ManualFoldGenerator();
		
		foldGenerator.setUseInstanceNames(true);
		foldGenerator.setTrainNames(trainNames);
		foldGenerator.setTestNames(testNames);
		
		return foldGenerator;
	}

	@Override
	public void generateFolds(DataModel dataModel) throws Exception {
		if(useInstanceNames) getIndicesFromInstanceNames(dataModel);
		checkParameters(dataModel);
		this.dataModel = dataModel;
		
		if(trainIDs == null || testIDs == null){
			if(trainIDs == null){
				trainIDs = dataIDsNotInList(dataModel, testIDs);
			}
			
			if(testIDs == null){
				testIDs = dataIDsNotInList(dataModel, trainIDs);
			}
		}
	}
	
	@Override
	public int numFolds() {
		if((trainIDs != null) && (testIDs != null)) {
			return 1;
		}
		else {
			return 0;
		}
	}

	@Override
	public List<DataModel> getFold(int foldID) {
		DataModel trainDataModel = createFoldDataModel(trainIDs, dataModel);
		trainDataModel.setDataSetAsTraining();
		
		DataModel valDataModel = createFoldDataModel(testIDs, dataModel);
		valDataModel.setDataSetAsValidation();
		
		List<DataModel> fold = new LinkedList<DataModel>();
		fold.add(trainDataModel);
		fold.add(valDataModel);
		
		return fold;
	}
	
	private void checkParameters(DataModel dataModel) throws Exception{
		if(trainIDs == null && testIDs == null)
			throw new Exception("FoldGenerator: Unspecified "
					+ "parameters for ManualFoldGenerator.");
	}
	
	private void getIndicesFromInstanceNames(DataModel dataModel){
		if(trainNames == null){
			this.setTrainIDs(null);
		} else{
			this.setTrainIDs(getIndicesOfNamesList(dataModel, trainNames));
		}
		
		if(testNames == null){
			this.setTestIDs(null);
		} else{
			this.setTestIDs(getIndicesOfNamesList(dataModel, testNames));
		}
	}
	
	private List<Integer> getIndicesOfNamesList(DataModel dataModel, List<String> selectedNames){
		List<Integer> instanceIndices = new LinkedList<Integer>();
		DataAttribute instanceNameAttribute = dataModel.getInstanceAttribute();
		List<String> instanceNames = dataModel.getColumnItemsByAttribute(instanceNameAttribute);
		
		for(String selectedName: selectedNames){
			int index = instanceNames.indexOf(selectedName);
			if (index >= 0) instanceIndices.add(index);
		}
		
		return instanceIndices;
	}
	
	private List<Integer> dataIDsNotInList(DataModel dataModel, 
			List<Integer> listIDs){
		List<Integer> listComplement = new LinkedList<Integer>();
		
		for(int rowID = 0; rowID < dataModel.size(); rowID++){
			if(!listIDs.contains(rowID)){
				listComplement.add(rowID);
			}
		}
		
		return listComplement;
	}
	
	private DataModel createFoldDataModel(List<Integer> rowIndices,
			DataModel dataModel) {
		DataModel foldDataModel = new DefaultDataModel(dataModel);

		for(int rowID: rowIndices){
			List<String> items = dataModel.getRow(rowID);
			foldDataModel.addItemsAsRow(new LinkedList<String>(items));
		}

		return foldDataModel;
	}
	
	private void setTrainIDs(List<Integer> trainIDs) {
		this.trainIDs = trainIDs;
	}

	private void setTestIDs(List<Integer> testIDs) {
		this.testIDs = testIDs;
	}

	private void setUseInstanceNames(boolean useInstanceNames) {
		this.useInstanceNames = useInstanceNames;
	}

	private void setTrainNames(List<String> trainNames) {
		this.trainNames = trainNames;
	}

	private void setTestNames(List<String> testNames) {
		this.testNames = testNames;
	}


	private boolean useInstanceNames;
	private List<String> trainNames;
	private List<String> testNames;
	private List<Integer> trainIDs;
	private List<Integer> testIDs;
	
	private DataModel dataModel;
}
