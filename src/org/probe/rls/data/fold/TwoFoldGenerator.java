package org.probe.rls.data.fold;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.probe.rls.data.DataModel;
import org.probe.rls.data.DefaultDataModel;

public class TwoFoldGenerator implements FoldGenerator {
	
	public TwoFoldGenerator(){
		this.train_fraction = DEFAULT_TRAIN_FRACTION;
		this.foldsIndices = new LinkedList<List<List<Integer>>>();
	}
	
	/**
	 * @param train_fraction: fraction of data assigned to training set.
	 * Remaining data is assigned as test set. Valid value range = (0,1).
	 */
	public TwoFoldGenerator(double train_fraction){
		super();
		this.train_fraction = train_fraction;
	}

	@Override
	public void generateFolds(DataModel dataModel) throws Exception {
		checkParameters();
		this.dataModel = dataModel;
		
		List<List<Integer>> stratifiedIndices = stratifyIndices(dataModel);
		permuteIndices(stratifiedIndices);
		List<List<Integer>> binnedIndices = distributeIndicesInto2Bins(stratifiedIndices);
		
		List<Integer> trainIndices = binnedIndices.get(0);
		List<Integer> validationIndices = binnedIndices.get(1);
		
		List<List<Integer>> fold = new LinkedList<List<Integer>>();
		fold.add(trainIndices);
		fold.add(validationIndices);
		
		foldsIndices.add(fold);
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
		if(train_fraction <= 0.0 || train_fraction >= 1.0) 
			throw new Exception("FoldGenerator: Fraction of data, set as train "
				+ " should be in range (0.0, 1.0).");
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
	
	private List<List<Integer>> distributeIndicesInto2Bins(List<List<Integer>> indices){
		List<List<Integer>> bins = new LinkedList<List<Integer>>();
		
		List<Integer> trainIndices = new LinkedList<Integer>();
		List<Integer> validationIndices = new LinkedList<Integer>();
		
		for(int label = 0; label < indices.size(); label++){
			List<Integer> classIndices = indices.get(label);
			int cut_point = (int) Math.rint(classIndices.size()*train_fraction);
			trainIndices.addAll(classIndices.subList(0, cut_point));
			validationIndices.addAll(classIndices.subList(cut_point, classIndices.size()));
		}
		
		bins.add(0, trainIndices);
		bins.add(1, validationIndices);
		
		return bins;
	}
	
	public void setSeed(int seed){
		this.seed = seed;
	}
	
	
	
	private double train_fraction;
	private long seed = 1;
	public static final double DEFAULT_TRAIN_FRACTION = 0.5;
	
	private List<List<List<Integer>>> foldsIndices = new LinkedList<List<List<Integer>>>();
	private DataModel dataModel;
}
