package org.probe.rls.data.fold;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.probe.rls.data.DataModel;
import org.probe.rls.data.DefaultDataModel;

public class KFoldGenerator implements FoldGenerator {
	
	public KFoldGenerator(){
		this.k = DEFAULT_K;
		this.foldsIndices = new LinkedList<List<List<Integer>>>();
	}
	
	/**
	 * @param k: Number of folds. Valid value range = (0,N), 
	 * where N is the total number of instances in the data.
	 */
	public KFoldGenerator(int k){
		super();
		this.k = k;
	}

	@Override
	public void generateFolds(DataModel dataModel) throws Exception {
		checkParameters(dataModel);
		this.dataModel = dataModel;
		
		List<List<Integer>> stratifiedIndices = stratifyIndices(dataModel);
		permuteIndices(stratifiedIndices);
		List<List<Integer>> validationIndices = distributeIndicesIntoKBins(stratifiedIndices);
		
		
		for(int foldID = 0; foldID < this.k; foldID++){
			List<Integer> testIDs = validationIndices.get(foldID);
			List<Integer> trainIDs = dataIDsNotInList(dataModel, testIDs);
			
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
	
	private void checkParameters(DataModel dataModel) throws Exception{
		if(k < 2) 
			throw new Exception("FoldGenerator: Number of folds should be"
				+ " greater than or equal to 2.");
		if(k > dataModel.getNumRows())
			throw new Exception("FoldGenerator: Number of folds cannot be "
					+ "greater than number of data instances.");
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
	
	private void permuteIndices(List<List<Integer>> indices){
		Random rand = new Random(this.seed);
		for(int label = 0; label < indices.size(); label++){
			Collections.shuffle(indices.get(label), rand);
		}
	}
	
	private List<List<Integer>> stratifyIndices(DataModel dataModel){
		List<String> classLabels = dataModel.getClassLabels();
		Map<String, Integer> labelMap = new HashMap<String, Integer>();
		List<List<Integer>> stratifiedLabels = new LinkedList<List<Integer>>();
		
		for(int label = 0; label < classLabels.size(); label++){
			labelMap.put(classLabels.get(label), label);
			stratifiedLabels.add(label, new LinkedList<Integer>());
		}
		
		for(int i = 0; i < dataModel.size(); i++){
			String instanceClassLabel = dataModel.getClassLabelForRow(i); 
			stratifiedLabels.get(labelMap.get(instanceClassLabel)).add(i);
		}
		
		return stratifiedLabels;
	}
	
	private List<List<Integer>> distributeIndicesIntoKBins(List<List<Integer>> indices){
		List<List<Integer>> bins = new LinkedList<List<Integer>>();
		
		for(int bin = 0; bin < k; bin++){
			bins.add(bin, new LinkedList<Integer>());
		}
		
		int pointer = 0;
		
		for(int label = 0; label < indices.size(); label++){
			Iterator<Integer> indexIter = indices.get(label).iterator();
			while(indexIter.hasNext()){
				bins.get(pointer).add(indexIter.next());
				pointer++;
				if(pointer == k) pointer = 0;
			}
		}
		
		return bins;
	}
	
	public void setSeed(int seed){
		this.seed = seed;
	}
	
	
	private int k;
	private long seed = 1;
	public static final int DEFAULT_K = 10;
	
	private List<List<List<Integer>>> foldsIndices = new LinkedList<List<List<Integer>>>();
	private DataModel dataModel;
}
