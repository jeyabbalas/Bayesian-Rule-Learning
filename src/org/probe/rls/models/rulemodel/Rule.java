package org.probe.rls.models.rulemodel;

import java.util.List;
import java.util.Map;

import org.probe.rls.data.DataModel;

public interface Rule {

	public void computeRuleStatistics(DataModel dataModel);
	public void setProbabilities(Map<String, Double> probabilities);
	public Map<String, Double> getProbabilities();
	
	public String getIdentifier();
	public String getRHSValue();
	public RuleLHS getLHS();
	public RuleLiteral getRHS();
	public RuleStatistics getRuleStatistics();
	
	public String classifyInstance(DataModel dataModel, int rowIndex);
	public boolean containsField(String field);
	
	public List<String> getAttributesUsed();
	public int getNumberOfAttributesUsed();
	
}
