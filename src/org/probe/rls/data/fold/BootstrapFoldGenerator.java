package org.probe.rls.data.fold;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.probe.rls.data.DataModel;
import org.probe.rls.data.DefaultDataModel;

public class BootstrapFoldGenerator implements FoldGenerator {
	
	public BootstrapFoldGenerator() {
		this.n_datasets = DEFAULT_N_DATASETS;
		this.foldsIndices = new LinkedList<List<List<Integer>>>();
	}
	
	/**
	 * @param n_datasets: Number of bootstrap datasets.
	 */
	public BootstrapFoldGenerator(int n_datasets){
		super();
		this.n_datasets = n_datasets;
	}

	@Override
	public void generateFolds(DataModel dataModel) throws Exception {
		checkParameters();
		this.dataModel = dataModel;
		
		List<Integer> dataIndices = new ArrayList<Integer>();
		for(int id = 0; id < dataModel.size(); id++) dataIndices.add(id);
		
		
		for(int foldID = 0; foldID < this.n_datasets; foldID++){
			List<Integer> trainIDs = bootstrapSampleIndices(dataIndices, foldID);
			List<Integer> testIDs = dataIDsNotInList(dataIndices, trainIDs);
			
			List<List<Integer>> foldIndices = new LinkedList<List<Integer>>();
			foldIndices.add(trainIDs); 
			foldIndices.add(testIDs);
			
			foldsIndices.add(foldIndices);
		}
	}
	
	@Override
	public int numFolds() {
		return foldsIndices.size();
	}

	@Override
	public List<DataModel> getFold(int foldID) {
		List<Integer> trainIDs = foldsIndices.get(foldID).get(0);
		List<Integer> testIDs = foldsIndices.get(foldID).get(1);
		
		DataModel trainDataModel = createFoldDataModel(trainIDs, dataModel);
		trainDataModel.setDataSetAsTraining();
		
		DataModel valDataModel = createFoldDataModel(testIDs, dataModel);
		valDataModel.setDataSetAsValidation();
		
		List<DataModel> fold = new LinkedList<DataModel>();
		fold.add(trainDataModel);
		fold.add(valDataModel);
		
		return fold;
	}
	
	private void checkParameters() throws Exception{
		if(this.n_datasets < 1) 
			throw new Exception("FoldGenerator: Number of Bootstrap datasets"
				+ " cannot be less than 1.");
	}
	
	private List<Integer> bootstrapSampleIndices(List<Integer> dataIndices, int sampleIteration){
		List<Integer> sampledIndices = new LinkedList<Integer>();
		
		Random rand = new Random(this.seed + sampleIteration);
		for(int i = 0; i < dataIndices.size(); i++){
			int sampledID = rand.nextInt(dataIndices.size());
			sampledIndices.add(dataIndices.get(sampledID));
		}
		
		return sampledIndices;
	}
	
	private List<Integer> dataIDsNotInList(List<Integer> dataIndices, 
			List<Integer> listIDs){
		List<Integer> listComplement = new LinkedList<Integer>();
		
		for(Integer rowID: dataIndices){
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
	
	
	private int n_datasets;
	private long seed = 1;
	public static final int DEFAULT_N_DATASETS = 10;
	
	private List<List<List<Integer>>> foldsIndices = new LinkedList<List<List<Integer>>>();
	private DataModel dataModel;
}
