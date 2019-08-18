package org.probe.rls.models.rulemodel.deterministic;

import java.util.List;
import java.util.Map;

import org.probe.rls.data.DataModel;
import org.probe.rls.models.rulemodel.Rule;
import org.probe.rls.models.rulemodel.RuleStatistics;
import org.probe.rls.util.RuleFormatter;

/**
 * A deterministic clause rule (conjuncts only).
 * 
 * @author jeya
 *
 */
public class DeterministicRule implements Rule {

	public DeterministicRule(Conjunction lhs, Conjunct rhs) {
		this.setIdentifier();
		this.lhs = lhs;
		this.rhs = rhs;
		this.ruleStatistics = new RuleStatistics(this);
	}
	
	private void setIdentifier() {
		this.identifer = "Rule:"+(DeterministicRule.callCounter++);
	}
	
	public void setIdentifier(String identifierStr) {
		this.identifer = identifierStr;
	}
	
	public String getIdentifier() {
		return this.identifer;
	}
	
	public static DeterministicRule parseString(String ruleStr) {
		String formattedString = RuleFormatter.removeAllWhiteSpace(ruleStr);
		
		String[] ruleElements = formattedString.split("->");
		Conjunction lhs = Conjunction.parseString(ruleElements[0]);
		Conjunct rhs = Conjunct.parseString(ruleElements[1]);
		
		DeterministicRule rule = new DeterministicRule(lhs,rhs);
		return rule;
	}
	
	public Conjunction getLHS() {
		return lhs;
	}
	
	public Conjunct getRHS() {
		return rhs;
	}
	
	@Override
	public void computeRuleStatistics(DataModel dataModel) {
		this.ruleStatistics.computeRuleStatistics(dataModel);
	}
	
	public void setRuleStatistics(RuleStatistics ruleStatistics) {
		this.ruleStatistics = ruleStatistics;
	}
	
	public RuleStatistics getRuleStatistics() {
		return this.ruleStatistics;
	}
	
	public boolean lhsMatchesInstance(DataModel dataModel, int rowIndex) {
		return lhs.matchesInstance(dataModel, rowIndex);
	}

	public boolean containsField(String field) {
		return lhs.containsField(field) || rhs.containsField(field);
	}
	
	@Override
	public List<String> getAttributesUsed() {
		return lhs.getFieldsUsed();
	}

	@Override
	public int getNumberOfAttributesUsed() {
		return getAttributesUsed().size();
	}
	
	@Override
	public void setProbabilities(Map<String, Double> probabilities) {
		this.ruleStatistics.setProbabilities(probabilities);
	}
	
	@Override
	public Map<String, Double> getProbabilities() {
		return ruleStatistics.getProbabilities();
	}
	
	@Override
	public String getRHSValue() {
		return rhs.getValue();
	}
	
	@Override
	public String classifyInstance(DataModel dataModel, int rowIndex) {
		if(this.lhsMatchesInstance(dataModel, rowIndex)) {
			return rhs.getValue();
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(lhs).append(" -> ").append(rhs);
		sb.append("\n").append("\t").append(ruleStatistics.toString());
		
		return sb.toString();
	}
	
	private String identifer;
	private static int callCounter = 1;
	
	protected Conjunction lhs;
	protected Conjunct rhs;
	protected RuleStatistics ruleStatistics;
}
