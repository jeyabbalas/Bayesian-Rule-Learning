package org.probe.rls.models.rulemodel;

import java.text.DecimalFormat;
import java.util.Map;

import org.probe.rls.data.DataModel;

public class RuleStatistics {
	
	public RuleStatistics(Rule rule, Map<String, Double> probabilities, 
			int truePositives, int falsePositives) {
		this.setIdentifier();
		this.rule = rule;
		this.probabilities = probabilities;
		this.truePositives = truePositives;
		this.falsePositives = falsePositives;
	}
	
	private void setIdentifier() {
		this.identifer = "RuleStatistics:"+(RuleStatistics.callCounter++);
	}
	
	public String getIdentifier() {
		return this.identifer;
	}
	
	public RuleStatistics(Rule rule) {
		this.rule = rule;
		truePositives = 0;
		falsePositives = 0;
	}
	
	public void setProbabilities(Map<String, Double> probabilities) {
		this.probabilities = probabilities;
	}
	
	public Map<String, Double> getProbabilities(){
		return probabilities;
	}
	
	public int getTruePositives(){
		return this.truePositives;
	}
	
	public int getFalsePositives(){
		return this.falsePositives;
	}
	
	public void computeRuleStatistics(DataModel dataModel) {
		computeTruePositivesFalsePositives(dataModel);
	}
	
	public void computeTruePositivesFalsePositives(DataModel dataModel) {
		truePositives = 0;
		falsePositives = 0;
		
		for(int rowIndex=0; rowIndex<dataModel.size(); rowIndex++) {
			String actualClass = dataModel.getClassLabelForRow(rowIndex);
			String predictedClass = rule.classifyInstance(dataModel, rowIndex);
			
			if(predictedClass == null) continue;
			
			if(actualClass.equalsIgnoreCase(predictedClass)) {
				truePositives++;
			} else {
				falsePositives++;
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if(probabilities != null) {
			sb.append("Confidence = ").append(df4.format(probabilities.get(rule.getRHSValue()))).append(", ");
		}
		sb.append("TP = ").append(truePositives).append(", ");
		sb.append("FP = ").append(falsePositives).append("\n");
		
		return sb.toString();
	}
	
	private String identifer;
	private static int callCounter = 1;
	
	private transient Rule rule;

	private Map<String, Double> probabilities;
	private int truePositives;
	private int falsePositives;
	
	private static DecimalFormat df4 = new DecimalFormat("#.####");
}
