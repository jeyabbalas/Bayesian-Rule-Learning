package org.probe.rls.models.rulemodel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.probe.rls.data.DataModel;
import org.probe.rls.models.Classifier;
import org.probe.rls.models.Prediction;


public class RuleModel implements Classifier {
	
	public RuleModel() {
		this.setIdentifier();
		this.rules = new LinkedList<Rule>();
	}
	
	private void setIdentifier() {
		this.identifier = "Identifier "+this.getTimeStampString()+" "+this.getCreator();
	}
	
	private String getTimeStampString() {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Date now = new Date();
	    
		return sdfDate.format(now);
	}
	
	private String getCreator() {
		return "Jeya Balasubramanian <jeya@pitt.edu>";
	}
	
	public String getIdentifier() {
		return this.identifier;
	}

	public static RuleModel createEmptyRuleModel() {
		return null;
	}

	public void addRule(Rule rule) {
		rules.add(rule);
	}

	public boolean containsField(String field) {
		for (Rule rule : rules) {
			if (rule.containsField(field))
				return true;
		}

		return false;
	}
	
	public List<Rule> getRules() {
		return rules;
	}

	public int getTotalNumberOfRules() {
		return this.getRules().size();
	}

	public List<String> getAttributesUsed() {
		if(attributesUsed == null) {
			return tallyAttributesUsed();
		}
		
		return attributesUsed;
	}
	
	private List<String> tallyAttributesUsed() {
		Set<String> setOfAttributesUsed = new TreeSet<String>();
		
		for(Rule rule: rules) {
			setOfAttributesUsed.addAll(rule.getAttributesUsed());
		}
		
		return new LinkedList<String>(setOfAttributesUsed);
	}
	
	public void setAttributesUsed(List<String> attributesUsed) {
		this.attributesUsed = attributesUsed;
	}

	public int getTotalAttributesUsed() {
		return this.getAttributesUsed().size();
	}
	
	public void setModelScore(double modelScore) {
		this.modelScore = modelScore;
	}
	
	@Override
	public double getModelScore() {
		return this.modelScore;
	}
	
	@Override
	public int getNumRules() {
		return rules.size();
	}

	@Override
	public int getNumVariables() {
		return attributesUsed.size();
	}
	
	/**
	 * Assumes a decision list of rules. Returns prediction 
	 * of the first rule that matches the instance. Override 
	 * this method for a different prediction strategy.
	 * 
	 * @param dataModel
	 * @param rowIndex
	 * @return
	 */
	public String classifyInstance(DataModel dataModel, int rowIndex) {
		for(Rule rule: rules) {
			String predictedLabel = rule.classifyInstance(dataModel, rowIndex);
			if(predictedLabel != null) {
				return predictedLabel;
			}
		}
		
		return null;
	}
	
	public Map<String, Double> getProbabilities(DataModel dataModel, int rowIndex) {
		for(Rule rule: rules) {
			String predictedLabel = rule.classifyInstance(dataModel, rowIndex);
			if(predictedLabel != null) {
				return rule.getProbabilities();
			}
		}
		
		return null;
	}
	
	@Override
	public Prediction predictInstance(DataModel dataModel, int rowIndex) {
		Map<String, Double> predictedProbs = null;
		String predictedLabel = null;
		
		for(Rule rule: rules) {
			predictedLabel = rule.classifyInstance(dataModel, rowIndex);
			if(predictedLabel != null) {
				predictedProbs = rule.getProbabilities();
				break;
			}
		}
		
		return new Prediction(predictedLabel, predictedProbs);
	}
	
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		int ruleCount = 0;
		for(Rule rule : rules){
			sb.append(++ruleCount).append(".\t");
			sb.append(rule.toString());
			sb.append("\n");
		}
		
		sb.append("\nVariables used (").append(getAttributesUsed().size()).append("): ");
		boolean first = true;
		for(String attribute: getAttributesUsed()) {
			if(!first) sb.append(", ");
			sb.append(attribute);
			first = false;
		}
		sb.append(".\n");
		
		return sb.toString().trim();
	}

	private String identifier;
	
	protected List<Rule> rules;
	private List<String> attributesUsed;
	private double modelScore = 1.0;
	
}
