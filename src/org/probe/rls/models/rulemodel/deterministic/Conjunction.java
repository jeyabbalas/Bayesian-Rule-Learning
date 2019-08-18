package org.probe.rls.models.rulemodel.deterministic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.probe.rls.data.DataModel;
import org.probe.rls.models.rulemodel.RuleLHS;
import org.probe.rls.models.rulemodel.RuleLiteral;
import org.probe.rls.util.RuleFormatter;

public class Conjunction implements RuleLHS {
	
	public Conjunction() {
		this.setIdentifier();
	}
	
	private void setIdentifier() {
		this.identifer = "LHS:"+(Conjunction.callCounter++);
	}
	
	public String getIdentifier() {
		return this.identifer;
	}
	
	public static Conjunction parseString(String conjunctionStr) {
		Conjunction conjunction = new Conjunction();
		
		String parsedString = RuleFormatter.formatRuleString(conjunctionStr);
		List<String> conjunctStrs = getConjunctStrsFromConjunctionStr(parsedString);
		
		for(String conjunctStr : conjunctStrs){
			conjunction.addConjunct(Conjunct.parseString(conjunctStr));	
		}
		
		return conjunction;
	}
	
	public List<Conjunct> getConjuncts() {
		return this.conjuncts;
	}
	
	public void addConjunct(Conjunct conjunct) {
		int currentCount = conjuncts.size();
		
		conjuncts.add(conjunct);
		fieldToConjunctMap.put(conjunct.getField(), currentCount);
	}

	public boolean containsField(String field) {
		return fieldToConjunctMap.containsKey(field);
	}
	
	public List<String> getFieldsUsed() {
		return new LinkedList<String>(fieldToConjunctMap.keySet());
	}
	
	@Override
	public List<RuleLiteral> getLiteralsUsed() {
		List<RuleLiteral> literalsUsed = new LinkedList<RuleLiteral>();
		
		for(Conjunct conjunct: conjuncts) {
			literalsUsed.add(conjunct);
		}
		
		return literalsUsed;
	}

	public Conjunct getConjunctByField(String field) {
		if(!containsField(field)) 
			return null;
		
		int index = fieldToConjunctMap.get(field);
		return conjuncts.get(index);
	}
	
	public boolean matchesInstance(DataModel dataModel, int rowIndex) {
		for(Conjunct conjunct : conjuncts) {
			if(!conjunct.matchesInstance(dataModel, rowIndex))
				return false;
		}
		
		return true;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for(Conjunct conjunct : conjuncts){
			sb.append(conjunct);
		}
		sb.append(")");
		
		return sb.toString();
	}
	
	private static List<String> getConjunctStrsFromConjunctionStr(String parsedString) {
		List<String> conjunctStrs = new LinkedList<String>();
		
		boolean isConjunctStart = false;
		boolean isConjunctEnd = true;
		int conjunctStartIndex = 0;

		for(int x = 0; x < parsedString.length(); x++){
			if(parsedString.charAt(x) == '(') {
				if(isConjunctStart){ 
					return null; //ERROR
				}
				if(isConjunctEnd){
					isConjunctEnd = false;
					isConjunctStart = true;
					
					conjunctStartIndex = x;
				}
			}else if(parsedString.charAt(x) == ')'){
				if(isConjunctEnd){
					return null; //ERROR
				}
				if(isConjunctStart){
					isConjunctStart = false;
					isConjunctEnd = true;
					
					String currentConjunct = parsedString.substring(conjunctStartIndex,x+1);
					conjunctStrs.add(currentConjunct);
				}
			}
		}
		
		return conjunctStrs;
	}
	
	public int getNumberOfConjuncts() {
		return conjuncts.size();
	}

	
	private String identifer;
	private static int callCounter = 1;
	
	private final List<Conjunct> conjuncts = new LinkedList<Conjunct>();
	private final HashMap<String, Integer> fieldToConjunctMap = new HashMap<String, Integer>();
	
}
