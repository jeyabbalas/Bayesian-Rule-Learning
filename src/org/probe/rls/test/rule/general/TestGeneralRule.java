package org.probe.rls.test.rule.general;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.probe.rls.models.rulemodel.general.Expression;
import org.probe.rls.models.rulemodel.general.GeneralRule;
import org.probe.rls.models.rulemodel.general.Literal;

public class TestGeneralRule {
	
	@Test
	public void testConstruction(){
		String testRule = "(((Urine Pushing=no)(Urethra Problems=no)),((Urine Pushing=yes)(Micturition pains=no)(Urethra Problems=yes))) -> (Inflammation=no)";
		String testLHS = "(((Urine Pushing=no)(Urethra Problems=no)),((Urine Pushing=yes)(Micturition pains=no)(Urethra Problems=yes)))";
		String testRHS = "(Inflammation=no)";
		
		Expression ruleLHS = Expression.parseString(testLHS);
		Literal ruleRHS = Literal.parseString(testRHS);
		GeneralRule rule = new GeneralRule(ruleLHS, ruleRHS);
		
		String ruleString = rule.toString().split("\n")[0];
		
		assertTrue(testRule.equalsIgnoreCase(ruleString));
		//System.out.println(rule);
	}

	@Test
	public void testParse(){
		String testRule = "(((Urine Pushing=no)(Urethra Problems=no)),((Urine Pushing=yes)(Micturition pains=no)(Urethra Problems=yes))) -> (Inflammation=no)";
		
		GeneralRule rule = GeneralRule.parseString(testRule);
		
		String ruleString = rule.toString().split("\n")[0];
		
		assertTrue(testRule.equalsIgnoreCase(ruleString));
		//System.out.println(rule);
	}
	//
}
