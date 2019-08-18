package org.probe.rls.test.rule.deterministic;

import org.junit.Test;
import org.probe.rls.models.rulemodel.RuleModel;
import org.probe.rls.models.rulemodel.deterministic.DeterministicRule;

import static org.junit.Assert.assertTrue;

public class TestDeterministicRuleModel {
	
	@Test
	public void test(){
		RuleModel ruleModel = new RuleModel();
		
		ruleModel.addRule(DeterministicRule.parseString("((a>500)(b<300))->(class=1)"));
		ruleModel.addRule(DeterministicRule.parseString("((a<500)(b<300))->(class=2)"));
		ruleModel.addRule(DeterministicRule.parseString("((a<500)(b>300))->(class=1)"));
		
		assertTrue(ruleModel.containsField("a"));
		assertTrue(ruleModel.containsField("b"));
		assertTrue(ruleModel.containsField("class"));
		
		System.out.println(ruleModel);
	}
}
