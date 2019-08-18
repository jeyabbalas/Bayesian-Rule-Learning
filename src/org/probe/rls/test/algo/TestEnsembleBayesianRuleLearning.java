package org.probe.rls.test.algo;

import org.junit.Test;
import org.probe.rls.algo.evaluator.Evaluator;
import org.probe.rls.algo.rule.BRLRuleLearner;
import org.probe.rls.algo.rule.brl.BRLSearchParameters;
import org.probe.rls.algo.rule.brl.parameters.BRLSearchType;
import org.probe.rls.algo.rule.brl.parameters.CPTRepresentationType;
import org.probe.rls.algo.rule.brl.parameters.EnsembleAggregationType;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.FileDataManager;
import org.probe.rls.data.FileType;
import org.probe.rls.models.rulemodel.ensemble.EnsembleRuleModel;

public class TestEnsembleBayesianRuleLearning {

	@Test
	public void testBrlLc() throws Exception{
		// Running Bagged-BRL-LC

		// Input the training dataset
		FileDataManager dataManager = new FileDataManager();
		dataManager.loadFromFile("Test//id3dataset.csv", FileType.CSV.getSeparator());

		// Get data
		DataModel dataModel = dataManager.getDataModel();

		// Get EBRL classifier and set learner parameters for Bagged-BRL-LC
		BRLSearchParameters params = new BRLSearchParameters();
		params.setCptRepresentation(CPTRepresentationType.Local_CB); // Learn local decision trees
		params.setSearchAlgorithm(BRLSearchType.GreedyBestFirstWithComplexityPenalty); // Greedy best-first search
		params.setUseComplexityPenaltyPrior(true); // use kappa structure prior
		params.setKappa(0.01); // kappa value
		params.setSearchAlgorithm(BRLSearchType.Bagging); // Base models generation method
		params.setAggregationType(EnsembleAggregationType.DefaultLinearCombination); //LC aggregation method
		params.setNumBaseModels(10); // number of base models

		// Learn EBRL model
		BRLRuleLearner ruleLearner = new BRLRuleLearner(params, dataModel);
		ruleLearner.runAlgo();
		EnsembleRuleModel eRM = ruleLearner.getEnsembleRuleModel();

		// Evaluate BRL model on training data itself
		Evaluator eval = new Evaluator(dataModel);
		eval.evaluateClassifierOnTest(eRM, dataModel);

		// Show learned model
		System.out.println(eRM.toString());

		// Show performance metrics
		System.out.println(eval.toString());
	}

	@Test
	public void testBrlBma() throws Exception{
		// Running Bagged-BRL-BMA

		// Input the training dataset
		FileDataManager dataManager = new FileDataManager();
		dataManager.loadFromFile("Test//id3dataset.csv", FileType.CSV.getSeparator());

		// Get data
		DataModel dataModel = dataManager.getDataModel();

		// Get EBRL classifier and set learner parameters for Bagged-BRL-LC
		BRLSearchParameters params = new BRLSearchParameters();
		params.setCptRepresentation(CPTRepresentationType.Local_CB); // Learn local decision trees
		params.setSearchAlgorithm(BRLSearchType.GreedyBestFirstWithComplexityPenalty); // Greedy best-first search
		params.setUseComplexityPenaltyPrior(true); // use kappa structure prior
		params.setKappa(0.01); // kappa value
		params.setSearchAlgorithm(BRLSearchType.Bagging); // Base models generation method
		params.setAggregationType(EnsembleAggregationType.SelectiveBayesianModelAveraging); //BMA aggregation method
		params.setNumBaseModels(10); // number of base models

		// Learn EBRL model
		BRLRuleLearner ruleLearner = new BRLRuleLearner(params, dataModel);
		ruleLearner.runAlgo();
		EnsembleRuleModel eRM = ruleLearner.getEnsembleRuleModel();

		// Evaluate BRL model on training data itself
		Evaluator eval = new Evaluator(dataModel);
		eval.evaluateClassifierOnTest(eRM, dataModel);

		// Show learned model
		System.out.println(eRM.toString());

		// Show performance metrics
		System.out.println(eval.toString());
	}

	@Test
	public void testBrlBmc() throws Exception{
		// Running Bagged-BRL-BMC

		// Input the training dataset
		FileDataManager dataManager = new FileDataManager();
		dataManager.loadFromFile("Test//id3dataset.csv", FileType.CSV.getSeparator());

		// Get data
		DataModel dataModel = dataManager.getDataModel();

		// Get EBRL classifier and set learner parameters for Bagged-BRL-LC
		BRLSearchParameters params = new BRLSearchParameters();
		params.setCptRepresentation(CPTRepresentationType.Local_CB); // Learn local decision trees
		params.setSearchAlgorithm(BRLSearchType.GreedyBestFirstWithComplexityPenalty); // Greedy best-first search
		params.setUseComplexityPenaltyPrior(true); // use kappa structure prior
		params.setKappa(0.01); // kappa value
		params.setSearchAlgorithm(BRLSearchType.Bagging); // Base models generation method
		params.setAggregationType(EnsembleAggregationType.SelectiveBayesianModelCombination); //LC aggregation method
		params.setNumEnsembles(100); // Number of ensembles
		params.setNumBaseModels(10); // number of base models

		// Learn EBRL model
		BRLRuleLearner ruleLearner = new BRLRuleLearner(params, dataModel);
		ruleLearner.runAlgo();
		EnsembleRuleModel eRM = ruleLearner.getEnsembleRuleModel();

		// Evaluate BRL model on training data itself
		Evaluator eval = new Evaluator(dataModel);
		eval.evaluateClassifierOnTest(eRM, dataModel);

		// Show learned model
		System.out.println(eRM.toString());

		// Show performance metrics
		System.out.println(eval.toString());
	}
}
