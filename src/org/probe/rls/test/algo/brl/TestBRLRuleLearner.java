package org.probe.rls.test.algo.brl;

import org.junit.Before;
import org.junit.Test;
import org.probe.rls.algo.rule.BRLRuleLearner;
import org.probe.rls.algo.rule.brl.BRLSearch;
import org.probe.rls.algo.rule.brl.BRLSearchParameters;
import org.probe.rls.algo.rule.brl.datastructures.ConstrainedBayesianNetwork;
import org.probe.rls.algo.rule.brl.parameters.CPTRepresentationType;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.FileDataManager;
import org.probe.rls.data.FileType;
import org.probe.rls.models.rulemodel.RuleModel;
import org.probe.rls.output.JsonWriters;

public class TestBRLRuleLearner {

	@Before
	public void init() {
		dataManager = new FileDataManager();
		try {
			dataManager.loadFromFile("Test//id3dataset.csv", FileType.CSV.getSeparator());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBRLRuleModelOutput() {
		
	}
	
	private FileDataManager dataManager;
}
