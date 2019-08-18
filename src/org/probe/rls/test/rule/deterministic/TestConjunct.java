package org.probe.rls.test.rule.deterministic;

import org.junit.Test;
import org.probe.rls.models.rulemodel.LiteralRelation;
import org.probe.rls.models.rulemodel.deterministic.Conjunct;

import static org.junit.Assert.assertEquals;

public class TestConjunct {

	@Test
	public void testParseStringNoSpaces(){
		String conjunctStr = "(a>500)";
		
		Conjunct conjunct = Conjunct.parseString(conjunctStr);
		
		assertEquals("a",conjunct.getField());
		assertEquals(LiteralRelation.GREATER_THAN,conjunct.getRelation());
		assertEquals("500",conjunct.getValue());
	}
	
	@Test
	public void testParseStringSpaces(){
		String conjunctStr = "( a   < 500   )";
		
		Conjunct conjunct = Conjunct.parseString(conjunctStr);
		
		assertEquals("a",conjunct.getField());
		assertEquals(LiteralRelation.LESSER_THAN,conjunct.getRelation());
		assertEquals("500",conjunct.getValue());
	}
}
