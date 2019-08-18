package org.probe.rls.test.algo;

import org.junit.Test;
import org.probe.rls.algo.evaluator.Evaluator;
import org.probe.rls.algo.rule.BRLRuleLearner;
import org.probe.rls.algo.rule.brl.BRLSearchParameters;
import org.probe.rls.algo.rule.brl.parameters.BRLSearchType;
import org.probe.rls.algo.rule.brl.parameters.CPTRepresentationType;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.FileDataManager;
import org.probe.rls.data.FileType;
import org.probe.rls.models.rulemodel.RuleModel;

public class TestBayesianRuleLearning {

	@Test
	public void testBrl() throws Exception{
		// Input the training dataset
		FileDataManager dataManager = new FileDataManager();
		dataManager.loadFromFile("Test//id3dataset.csv", FileType.CSV.getSeparator());
		
		// Get data
		DataModel dataModel = dataManager.getDataModel();
		
		// Get BRL classifier and set learner parameters
		BRLSearchParameters params = new BRLSearchParameters();
		params.setCptRepresentation(CPTRepresentationType.Local_CB); //Learn local decision trees
		params.setSearchAlgorithm(BRLSearchType.GreedyBestFirstWithComplexityPenalty); // Greedy best-first search
		params.setUseComplexityPenaltyPrior(true); // use kappa structure prior
		params.setKappa(0.01); //set kappa value for structure prior
		BRLRuleLearner brlRuleLearner = new BRLRuleLearner(params, dataModel);
		
		// Learn BRL model
		brlRuleLearner.runAlgo();
		RuleModel ruleModel = brlRuleLearner.getRuleModel();
		
		// Evaluate BRL model on training data itself
		Evaluator eval = new Evaluator(dataModel);
		eval.evaluateClassifierOnTest(ruleModel, dataModel);
		
		// Show learned model
		System.out.println(ruleModel.toString());
		
		// Show performance metrics
		System.out.println(eval.toString());
	}
}
