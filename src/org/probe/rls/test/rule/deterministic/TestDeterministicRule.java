package org.probe.rls.test.rule.deterministic;

import org.junit.Test;
import org.probe.rls.models.rulemodel.deterministic.Conjunct;
import org.probe.rls.models.rulemodel.deterministic.Conjunction;
import org.probe.rls.models.rulemodel.deterministic.DeterministicRule;

import static org.junit.Assert.assertTrue;

public class TestDeterministicRule {
	
	@Test
	public void testConstruction(){
		Conjunction lhs = Conjunction.parseString("((a>500)(b<300))");
		Conjunct rhs = Conjunct.parseString("class=1");
		
		DeterministicRule rule = new DeterministicRule(lhs,rhs);

		assertTrue(rule.containsField("a"));
		assertTrue(rule.containsField("b"));
		assertTrue(rule.containsField("class"));
	}
	
	@Test
	public void testParse(){
		String ruleStr = "((a>500)(b<300))->(class=1)";
		
		DeterministicRule rule = DeterministicRule.parseString(ruleStr);
		
		assertTrue(rule.containsField("a"));
		assertTrue(rule.containsField("b"));
		assertTrue(rule.containsField("class"));
	}
}
