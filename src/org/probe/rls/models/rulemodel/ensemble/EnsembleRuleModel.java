package org.probe.rls.models.rulemodel.ensemble;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.probe.rls.models.rulemodel.RuleModel;
import org.probe.rls.util.map.MapUtils;
import org.probe.rls.data.DataModel;
import org.probe.rls.models.Classifier;
import org.probe.rls.models.Prediction;

public class EnsembleRuleModel implements Classifier {

	public EnsembleRuleModel() {
		this.ruleModels = new LinkedList<RuleModel>();
		this.modelWeights = new LinkedList<Double>();
	}
	
	public void addRuleModel(RuleModel ruleModel, Double weight) {
		this.ruleModels.add(ruleModel);
		this.modelWeights.add(weight);
	}
	
	public List<RuleModel> getListOfRuleModels() {
		return ruleModels;
	}
	
	@Override
	public String classifyInstance(DataModel dataModel, int rowIndex) {
		Map<String, Double> aggregatedClassDistribution = getProbabilities(dataModel, rowIndex);
		
		double max = 0.0;
		String predictedLabel = null;
		for(String classLabel: aggregatedClassDistribution.keySet()) {
			if(aggregatedClassDistribution.get(classLabel) > max) {
				max = aggregatedClassDistribution.get(classLabel);
				predictedLabel = classLabel;
			}
		}
		
		return predictedLabel;
	}

	@Override
	public Map<String, Double> getProbabilities(DataModel dataModel, int rowIndex) {
		Map<String, Double> aggregatedClassDistribution = initAggregatedClassDist(dataModel, rowIndex);
		
		for(int h=0; h<ruleModels.size(); h++) {
			double weight = modelWeights.get(h);
			
			Map<String, Double> ruleModelDistribution = ruleModels.get(h).getProbabilities(dataModel, rowIndex);
			for(String classLabel: ruleModelDistribution.keySet()) {
				double classDistribution = aggregatedClassDistribution.get(classLabel);
				classDistribution += ruleModelDistribution.get(classLabel)*weight;
				aggregatedClassDistribution.put(classLabel, classDistribution);
			}
		}
		
		return aggregatedClassDistribution;
	}
	
	private Map<String, Double> initAggregatedClassDist(DataModel dataModel, int rowIndex) {
		Map<String, Double> aggregatedClassDistribution = new TreeMap<String, Double>();
		
		Map<String, Double> ruleModelDistribution = ruleModels.get(0).getProbabilities(dataModel, rowIndex);
		for(String classLabel: ruleModelDistribution.keySet()) {
			aggregatedClassDistribution.put(classLabel, new Double(0.0));
		}
		
		return aggregatedClassDistribution;
	}
	
	public List<Double> getModelWeights() {
		return modelWeights;
	}
	
	@Override
	public double getModelScore() {
		return this.modelScore;
	}

	@Override
	public int getNumRules() {
		return 0;
	}

	@Override
	public int getNumVariables() {
		return 0;
	}
	
	@Override
	public Prediction predictInstance(DataModel dataModel, int rowIndex) {
		Map<String, Double> aggregatedClassDistribution = getProbabilities(dataModel, rowIndex);
		
		double max = 0.0;
		String predictedLabel = null;
		for(String classLabel: aggregatedClassDistribution.keySet()) {
			if(aggregatedClassDistribution.get(classLabel) > max) {
				max = aggregatedClassDistribution.get(classLabel);
				predictedLabel = classLabel;
			}
		}
		
		return new Prediction(predictedLabel, aggregatedClassDistribution);
	}
	
	public Map<String, Double> getVariableImportance() {
		if(variableImportance == null) this.computeVariableImportance();
		return variableImportance;
	}
	
	private void computeVariableImportance() {
		variableImportance = new TreeMap<String, Double>();
		
		Set<String> variablesUsedByEnsemble = new HashSet<String>();
		for(RuleModel ruleModel: ruleModels) {
			variablesUsedByEnsemble.addAll(ruleModel.getAttributesUsed());
		}
		
		for(String variable: variablesUsedByEnsemble) {
			double importance = 0.0;
			for(int h=0; h<ruleModels.size(); h++) {
				if(ruleModels.get(h).containsField(variable)) {
					importance += modelWeights.get(h);
				}
			}
			variableImportance.put(variable, importance);
		}
		
		variableImportance = MapUtils.sortByValue(variableImportance, false);
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		int count = 0;
		for(RuleModel ruleModel: ruleModels) {
			sb.append("Model number: "+(++count)).append("\n\n");
			sb.append(ruleModel.toString()).append("\n\n");
			sb.append("===========================================================================================\n");
		}
		
		sb.append("\n\nVariable importance\n");
		sb.append("-------------------\n");
		if(this.variableImportance == null) {
			this.computeVariableImportance();
		}
		count = 0;
		for(String variable: variableImportance.keySet()) {
			sb.append(++count).append(". ").append(variable).append("\t");
			sb.append(df4.format(variableImportance.get(variable))).append("\n");
		}
		
		return sb.toString();
	}
	
	
	private List<RuleModel> ruleModels;
	private List<Double> modelWeights;
	private Map<String, Double> variableImportance;
	private double modelScore = 1.0;
	
	private static DecimalFormat df4 = new DecimalFormat("#.####");
}
