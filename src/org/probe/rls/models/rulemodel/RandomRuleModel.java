package org.probe.rls.models.rulemodel;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.probe.rls.data.DataModel;
import org.probe.rls.models.Prediction;

public class RandomRuleModel extends RuleModel {
	
	public RandomRuleModel() {
		
	}

	@Override
	// Only designed for gene-expression experiments for EBRL paper
	public String classifyInstance(DataModel dataModel, int rowIndex) {
		int numLabels = dataModel.getClassLabels().size();
		return dataModel.getClassLabels().get(new Random().nextInt(numLabels));
	}
	
	@Override
	// Only designed for gene-expression experiments for EBRL paper
	public Map<String, Double> getProbabilities(DataModel dataModel, int rowIndex){
		Map<String, Double> classProb = new TreeMap<String, Double>();
		
		double randDouble = new Random().nextDouble();
		double complement = 1.0 - randDouble;
		
		classProb.put(dataModel.getClassLabels().get(0), randDouble);
		classProb.put(dataModel.getClassLabels().get(1), complement);
		return classProb;
	}
	
	@Override
	public Prediction predictInstance(DataModel dataModel, int rowIndex) {
		return new Prediction(classifyInstance(dataModel, rowIndex), getProbabilities(dataModel, rowIndex));
	}
	
	@Override
	public String getDescription() {
		return "Experimental rule model that predicts randomly.";
	}
	
	@Override
	public double getModelScore() {
		return 0.0;
	}
	
	@Override
	public int getNumRules() {
		return 0;
	}

	@Override
	public int getNumVariables() {
		return 0;
	}
}
