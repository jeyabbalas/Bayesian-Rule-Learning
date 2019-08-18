package org.probe.rls.models.rulemodel.general;

import java.util.List;
import java.util.Map;

import org.probe.rls.data.DataModel;
import org.probe.rls.models.rulemodel.Rule;
import org.probe.rls.models.rulemodel.RuleStatistics;

/**
 * A general clause rule (allows disjuncts).
 * 
 * @author jeya
 *
 */
public class GeneralRule implements Rule {

	public GeneralRule(Expression lhs, Literal rhs) {
		this.setIdentifier();
		this.lhs = lhs;
		this.rhs = rhs;
		this.ruleStatistics = new RuleStatistics(this);
	}
	
	private void setIdentifier() {
		this.identifer = "Rule:"+(GeneralRule.callCounter++);
	}
	
	public void setIdentifier(String identifierStr) {
		this.identifer = identifierStr;
	}
	
	public String getIdentifier() {
		return this.identifer;
	}
	
	public static GeneralRule parseString(String ruleStr) {
		checkBadArguments(ruleStr);
		
		ruleStr=ruleStr.trim();
		String[] ruleLhsRhs = ruleStr.split("->");
		Expression ruleLhs = Expression.parseString(ruleLhsRhs[0]);
		Literal ruleRhs = Literal.parseString(ruleLhsRhs[1]);
		
		GeneralRule rule = new GeneralRule(ruleLhs, ruleRhs);
		
		return rule;
	}
	
	private static void checkBadArguments(String ruleStr) {
		if((ruleStr==null) || 
				(ruleStr.length()==0) ||
				(ruleStr.split("->").length != 2)) {
			throw new IllegalArgumentException("The input rule string is not syntactically correct.");
		}
	}
	
	public Expression getLHS() {
		return this.lhs;
	}
	
	public Literal getRHS() {
		return this.rhs;
	}
	
	@Override
	public void computeRuleStatistics(DataModel dataModel) {
		this.ruleStatistics.computeRuleStatistics(dataModel);
	}
	
	@Override
	public RuleStatistics getRuleStatistics() {
		return ruleStatistics;
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
	
	public boolean lhsMatchesInstance(DataModel dataModel, int rowIndex) {
		return lhs.matchesInstance(dataModel, rowIndex);
	}

	@Override
	public boolean containsField(String field) {
		return getAttributesUsed().contains(field);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(lhs.toString()).append(" -> ").append(rhs.toString());
		sb.append("\n").append("\t").append(ruleStatistics.toString());
		
		return sb.toString();
	}
	
	
	private String identifer;
	private static int callCounter = 1;
	
	protected Expression lhs;
	protected Literal rhs;
	protected RuleStatistics ruleStatistics;
	
}
