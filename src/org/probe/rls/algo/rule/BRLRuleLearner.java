package org.probe.rls.algo.rule;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.probe.rls.algo.rule.brl.BRLSearch;
import org.probe.rls.algo.rule.brl.BRLSearchParameters;
import org.probe.rls.algo.rule.brl.datastructures.ConstrainedBayesianNetwork;
import org.probe.rls.algo.rule.brl.datastructures.LeafNode;
import org.probe.rls.algo.rule.brl.parameters.EnsembleAggregationType;
import org.probe.rls.data.DataAttribute;
import org.probe.rls.data.DataModel;
import org.probe.rls.models.rulemodel.RandomRuleModel;
import org.probe.rls.models.rulemodel.Rule;
import org.probe.rls.models.rulemodel.RuleModel;
import org.probe.rls.models.rulemodel.ensemble.EnsembleRuleModel;
import org.probe.rls.models.rulemodel.general.GeneralRule;
import org.probe.rls.util.stats.distribution.Dirichlet;


public class BRLRuleLearner implements RuleLearner {
	
	public BRLRuleLearner(DataModel dataModel) {
		this.searchParams = new BRLSearchParameters();
		this.dataModel = dataModel;
	}
	
	public BRLRuleLearner(BRLSearchParameters searchParams, DataModel dataModel) {
		this.searchParams = searchParams;
		this.dataModel = dataModel;
	}

	public void setDataModel(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	@Override
	public void runAlgo() throws Exception {
		BRLSearch brlSearch = new BRLSearch(searchParams, dataModel);
		if(searchParams.doEnsemble()) {
			List<ConstrainedBayesianNetwork> brlModels = brlSearch.runEnsembleBRLSearch();
			this.ensembleRuleModel = translateIntoEnsembleRuleModel(brlModels);
		}
		else {
			ConstrainedBayesianNetwork brlModel = brlSearch.runBRLSearch();
			this.ruleModel = translateIntoRuleModel(brlModel);
		}
	}
	
	public List<EnsembleRuleModel> runAllEnsembleMethods() throws Exception {
		List<EnsembleRuleModel> allEnsembles = new LinkedList<EnsembleRuleModel>();
		
		BRLSearch brlSearch = new BRLSearch(searchParams, dataModel);
		List<ConstrainedBayesianNetwork> brlModels = brlSearch.runEnsembleBRLSearch();
		
		EnsembleRuleModel eRM1 = new EnsembleRuleModel();
		createEnsembleWithDefaultWeights(eRM1, brlModels);
		allEnsembles.add(eRM1);
		
		EnsembleRuleModel eRM2 = new EnsembleRuleModel();
		createEnsembleWithBmaWeights(eRM2, brlModels);
		allEnsembles.add(eRM2);
		
		EnsembleRuleModel eRM3 = new EnsembleRuleModel();
		createEnsembleWithBmcWeights(eRM3, brlModels);
		allEnsembles.add(eRM3);
		for(Double wt: eRM3.getModelWeights()) {
			System.out.println(wt+"\t");
		}
		return allEnsembles;
	}
	
	public List<EnsembleRuleModel> runLcBmcEnsembleMethods() throws Exception {
		List<EnsembleRuleModel> allEnsembles = new LinkedList<EnsembleRuleModel>();
		
		BRLSearch brlSearch = new BRLSearch(searchParams, dataModel);
		List<ConstrainedBayesianNetwork> brlModels = brlSearch.runEnsembleBRLSearch();
		
		EnsembleRuleModel eRM1 = new EnsembleRuleModel();
		createEnsembleWithDefaultWeights(eRM1, brlModels);
		allEnsembles.add(eRM1);
		
		EnsembleRuleModel eRM2 = new EnsembleRuleModel();
		createEnsembleWithBmcWeights(eRM2, brlModels);
		allEnsembles.add(eRM2);

		try(FileWriter fw = new FileWriter("weights_file.txt", true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {

			for(Double wt: eRM1.getModelWeights()) {
				out.print(wt+"\t");
			}
			for(Double wt: eRM2.getModelWeights()) {
				out.print(wt+"\t");
			}
			out.println();
		} catch (IOException e) {
			
		}

		
		return allEnsembles;
	}
	
	private RuleModel translateIntoRuleModel(ConstrainedBayesianNetwork brlModel) {
		// Created for experimental purpose, see EBRL paper.
		if(brlModel.isRandomModel()) {
			return new RandomRuleModel();
		}
		
		RuleModel translatedRuleModel = new RuleModel();
		
		List<LeafNode> leaves = brlModel.getClassLocalStructure().getLeafNodes();
		for(LeafNode leaf: leaves) {
			StringBuilder leafRuleStr = new StringBuilder();
			String[] leafLhsRhs = leaf.toString().split(" -> ");
			leafRuleStr.append(leafLhsRhs[0]).append(" -> ");
			
			Map<String, Double> parameterPosteriorDistribution = leaf.getParameterPosteriorDistribution();
			String maxClassLabel = "";
			double maxProbability = -1.0;
			for(String classValue: parameterPosteriorDistribution.keySet()) {
				if(parameterPosteriorDistribution.get(classValue) > maxProbability) {
					maxProbability = parameterPosteriorDistribution.get(classValue);
					maxClassLabel = classValue;
				}
			}
			leafRuleStr.append("(").append(leaf.getDataAttribute().getAttributeName());
			leafRuleStr.append("=").append(maxClassLabel).append(")");
			
			Rule leafRule = GeneralRule.parseString(leafRuleStr.toString());
			leafRule.setProbabilities(parameterPosteriorDistribution);
			leafRule.computeRuleStatistics(dataModel);
			translatedRuleModel.addRule(leafRule);
		}
		
		List<String> attributesUsed = convertDataAttributesToStringNames(brlModel.getClassParents());
		translatedRuleModel.setAttributesUsed(attributesUsed);
		translatedRuleModel.setModelScore(brlModel.getNetworkPosteriorProbability());
		
		
		return translatedRuleModel;
	}
	
	private List<String> convertDataAttributesToStringNames(Set<DataAttribute> dataAttributes) {
		List<String> stringNames = new LinkedList<String>();
		
		for(DataAttribute dataAttribute: dataAttributes) {
			stringNames.add(dataAttribute.getAttributeName());
		}
		
		return stringNames;
	}
	
	private EnsembleRuleModel translateIntoEnsembleRuleModel(
			List<ConstrainedBayesianNetwork> brlModels) {
		EnsembleRuleModel eRM = new EnsembleRuleModel();
		
		if(searchParams.getAggregationType().equals(EnsembleAggregationType.DefaultLinearCombination)) {
			createEnsembleWithDefaultWeights(eRM, brlModels);
		} else if(searchParams.getAggregationType().equals(EnsembleAggregationType.SelectiveBayesianModelAveraging)) {
			createEnsembleWithBmaWeights(eRM, brlModels);
		} else if(searchParams.getAggregationType().equals(EnsembleAggregationType.SelectiveBayesianModelCombination)) {
			createEnsembleWithBmcWeights(eRM, brlModels);
		}
		
		return eRM;
	}
	
	private void createEnsembleWithDefaultWeights(EnsembleRuleModel eRM, List<ConstrainedBayesianNetwork> brlModels) {
		for(ConstrainedBayesianNetwork brlModel: brlModels) {
			eRM.addRuleModel(translateIntoRuleModel(brlModel), brlModel.getModelWeight());
		}
	}
	
	private void createEnsembleWithBmaWeights(EnsembleRuleModel eRM, List<ConstrainedBayesianNetwork> brlModels) {
		double[] modelWeights = getBayesianScoresOfModels(brlModels);
		normalizeWeights(modelWeights);
		
		for(int h=0; h<brlModels.size(); h++) {
			eRM.addRuleModel(translateIntoRuleModel(brlModels.get(h)), modelWeights[h]);
		}
	}
	
	private double[] getBayesianScoresOfModels(List<ConstrainedBayesianNetwork> brlModels) {
		double[] modelWeights = new double[brlModels.size()];
		
		for(int h=0; h<brlModels.size(); h++) {
			modelWeights[h] = Math.pow(Math.E, brlModels.get(h).getNetworkPosteriorProbability());
		}
		
		return modelWeights;
	}
	
	private void normalizeWeights(double[] weights) {
		double sum = 0.0;
		for(double weight: weights) sum += weight;
		
		for(int h=0; h<weights.length; h++) weights[h] = weights[h]/sum;
	}
	
	private void createEnsembleWithBmcWeights(EnsembleRuleModel eRM, List<ConstrainedBayesianNetwork> brlModels) {
		List<RuleModel> ruleModels = getListOfTranslatedModels(brlModels);
		
		Map<Double, List<Double>> ensembleWtModelWt = getEnsembleWeightAndModelWeights(ruleModels);
		double[] ensembleAveragedModelWeights = computeEnsembleAveragedModelWeights(ensembleWtModelWt, ruleModels.size());
		
		for(int h=0; h<ruleModels.size(); h++) {
			eRM.addRuleModel(ruleModels.get(h), ensembleAveragedModelWeights[h]);
		}
	}
	
	private double[] computeEnsembleAveragedModelWeights(Map<Double, List<Double>> ensembleWtModelWt, int nModels) {
		double[] ensembleAveragedModelWeights = new double[nModels];
		
		for(Double ensembleWt: ensembleWtModelWt.keySet()) {
			for(int h=0; h<ensembleAveragedModelWeights.length; h++) {
				ensembleAveragedModelWeights[h] += ensembleWtModelWt.get(ensembleWt).get(h)*ensembleWt;
			}
		}
		
		return ensembleAveragedModelWeights;
	}

	private List<RuleModel> getListOfTranslatedModels(List<ConstrainedBayesianNetwork> brlModels) {
		List<RuleModel> ruleModels = new LinkedList<RuleModel>();
		for(ConstrainedBayesianNetwork brlModel: brlModels) ruleModels.add(translateIntoRuleModel(brlModel));
		
		return ruleModels;
	}
	
	private Map<Double, List<Double>> getEnsembleWeightAndModelWeights(List<RuleModel> ruleModels) {
		Map<Double, List<Double>> ensembleWtModelWt;
		Map<Double, List<Double>> tempMap = new HashMap<Double, List<Double>>();
		
		double[][][] instHypClassProbs = getHypPredProbabilities(ruleModels);
		int[] actualClass = getActualClassLabels();
		
		//initialize Dirichlet parameters
		double[] alphas = new double[ruleModels.size()];
		for(int i=0; i<alphas.length; i++) alphas[i] = 1.0;
		
		int numSamplesPerIter = 100;
			
		for(int e=0; e<searchParams.getNumEnsembles(); e++) {
			Dirichlet dir = new Dirichlet(alphas);
			
			double bestEnsemblePosterior = 0.0;
			double[] bestWeights = new double[alphas.length];
			for(int samp=0; samp<numSamplesPerIter; samp++) {
				double[] weights = dir.drawSample();
				double epsilon = computeErrorRate(instHypClassProbs, weights, actualClass);
				double ensemblePosterior = computeEnsemblePosterior(epsilon);
				
				List<Double> weightsAsList = new LinkedList<Double>(); 
				for(double weight: weights) weightsAsList.add(weight);
				tempMap.put(ensemblePosterior, weightsAsList);
				
				if(ensemblePosterior > bestEnsemblePosterior) {
					bestWeights = weights;
					bestEnsemblePosterior = ensemblePosterior;
				}
			}
			
			updateDirichletAlphas(alphas, bestWeights);
		}
		
		ensembleWtModelWt = normalizeEnsembleWeights(tempMap);
		
		return ensembleWtModelWt;
	}

	private int[] getActualClassLabels() {
		int[] actualClass = new int[dataModel.size()];
		
		List<String> dataClassLabels = dataModel.getClassColumn();
		List<String> classValues = dataModel.getClassLabels();
		
		for(int row=0; row<dataClassLabels.size(); row++) {
			for(int c=0; c<dataModel.getClassLabels().size(); c++) {
				if(dataClassLabels.get(row).equalsIgnoreCase(classValues.get(c))) {
					actualClass[row] = c;
				}
			}
		}
		
		return actualClass;
	}

	private double[][][] getHypPredProbabilities(List<RuleModel> ruleModels) {
		List<String> classValues = dataModel.getClassLabels();
		double[][][] instHypClassProbs = new double[dataModel.size()][ruleModels.size()][classValues.size()];
		
		for(int row=0; row<dataModel.size(); row++) {
			for(int hyp=0; hyp<ruleModels.size(); hyp++) {
				Map<String, Double> hypClassProbs = ruleModels.get(hyp).getProbabilities(dataModel, row);
				for(int c=0; c<dataModel.getClassLabels().size(); c++) {
					instHypClassProbs[row][hyp][c] = hypClassProbs.get(classValues.get(c));
				}
			}
		}
		
		return instHypClassProbs;
	}

	private Map<Double, List<Double>> normalizeEnsembleWeights(Map<Double, List<Double>> tempMap) {
		Map<Double, List<Double>> ensembleWtModelWt = new HashMap<Double, List<Double>>();
		
		double sum = 0.0;
		for(Double ensembleWeight: tempMap.keySet()) sum += ensembleWeight;
		
		for(Double ensembleWeight: tempMap.keySet()) {
			ensembleWtModelWt.put(ensembleWeight/sum, tempMap.get(ensembleWeight));
		}
		
		return ensembleWtModelWt;
	}

	/** Cleaner code dropped for a significantly faster but slightly unreadable code.
	private double computeErrorRate(List<RuleModel> ruleModels, double[] weights) {
		EnsembleRuleModel eRM = new EnsembleRuleModel();
		
		for(int h=0; h<ruleModels.size(); h++) {
			eRM.addRuleModel(ruleModels.get(h), weights[h]);
		}
		
		int errors = 0;
		int total = dataModel.getNumRows();
		for(int r=0; r<dataModel.getNumRows(); r++) {
			if(!eRM.classifyInstance(dataModel, r).equalsIgnoreCase(dataModel.getClassLabelForRow(r))) {
				errors += 1;
			}
		}
		
		return errors/total;
	}
	*/
	
	private double computeErrorRate(double[][][] instHypClassProbs, double[] weights, int[] actualClass) {
		double[][] hypAveragedClassProbs = new double[instHypClassProbs.length][instHypClassProbs[0][0].length];
		int[] predictedClass = new int[instHypClassProbs.length];
		
		int errors = 0;
		int total = hypAveragedClassProbs.length;
		
		//weighted average of predicted class distribution
		for(int i=0; i<instHypClassProbs.length; i++) {
			double[][] hypClassProbs = instHypClassProbs[i];
			for(int h=0; h<hypClassProbs.length; h++) {
				for(int c=0; c<hypClassProbs[h].length; c++) {
					hypAveragedClassProbs[i][c] += hypClassProbs[h][c]*weights[h];
				}
			}
		}
		
		
		//predicting class
		for(int i=0; i<hypAveragedClassProbs.length; i++) {
			int maxIndex = 0;
			for(int c=1; c<hypAveragedClassProbs[i].length; c++) {
				if(hypAveragedClassProbs[i][c] > hypAveragedClassProbs[i][maxIndex]) {
					maxIndex = c;
				}
			}
			predictedClass[i] = maxIndex;
			
			if(predictedClass[i] != actualClass[i]) {//error in prediction
				errors++;
			}
		}
		
		return ((double)errors/(double)total);
	}
	
	private double computeEnsemblePosterior(double epsilon) {
		double ensemblePosterior = 0.0;
		double ensemblePrior = 1.0/(double)searchParams.getNumEnsembles(); //uniform prior
		
		int errors = (int) (dataModel.getNumRows()*epsilon);
		
		ensemblePosterior = ensemblePrior*
				Math.pow((1.0 - epsilon), (dataModel.getNumRows() - errors))*
				Math.pow(epsilon, errors);
		
		return ensemblePosterior;
	}
	
	private void updateDirichletAlphas(double[] alphas, double[] bestWeights) {
		for(int i=0; i<alphas.length; i++) {
			alphas[i] = alphas[i] + bestWeights[i];
		}
	}

	@Override
	public boolean hasLearntRules() {
		return ruleModel != null;
	}

	@Override
	public RuleModel getRuleModel() {
		return ruleModel;
	}
	
	@Override
	public EnsembleRuleModel getEnsembleRuleModel() {
		return ensembleRuleModel;
	}
	
	
	private BRLSearchParameters searchParams;
	private DataModel dataModel = null;
	
	private RuleModel ruleModel = null;
	private EnsembleRuleModel ensembleRuleModel = null;
}
