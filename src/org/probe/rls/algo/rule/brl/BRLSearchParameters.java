package org.probe.rls.algo.rule.brl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.probe.rls.algo.rule.brl.datastructures.BRLStructurePrior;
import org.probe.rls.algo.rule.brl.parameters.BRLSearchType;
import org.probe.rls.algo.rule.brl.parameters.CPTRepresentationType;
import org.probe.rls.algo.rule.brl.parameters.EnsembleAggregationType;
import org.probe.rls.algo.rule.brl.parameters.HeuristicScoreType;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.FileType;

public class BRLSearchParameters {
	
	public BRLSearchParameters() {
		
	}
	
	public CPTRepresentationType getCptRepresentation() {
		return cptRepresentation;
	}

	public void setCptRepresentation(CPTRepresentationType cptRepresentation) {
		this.cptRepresentation = cptRepresentation;
	}

	public BRLSearchType getSearchAlgorithm() {
		return searchAlgorithm;
	}

	public void setSearchAlgorithm(BRLSearchType searchAlgorithm) {
		this.searchAlgorithm = searchAlgorithm;
		
		if(this.searchAlgorithm.equals(BRLSearchType.Bagging)) {
			this.setDoBagging(true);
		} else if(this.searchAlgorithm.equals(BRLSearchType.Boosting)) {
			this.setDoBoosting(true);
		}
	}

	public HeuristicScoreType getHeuristicScore() {
		return heuristicScore;
	}

	public void setHeuristicScore(HeuristicScoreType heuristicScore) {
		this.heuristicScore = heuristicScore;
	}

	public int getBeamWidth() {
		return beamWidth;
	}

	public void setBeamWidth(int beamWidth) {
		this.beamWidth = beamWidth;
	}

	public int getMaxParents() {
		return maxParents;
	}

	public void setMaxParents(int maxParents) {
		this.maxParents = maxParents;
	}

	public String getStructurePriorsFile() {
		return structurePriorsFile;
	}

	public void setStructurePriorsFile(String structurePriorsFile) {
		this.structurePriorsFile = structurePriorsFile;
	}

	public String getCostMatrixFile() {
		return costMatrixFile;
	}

	public void setCostMatrixFile(String costMatrixFile, DataModel dataModel) throws Exception {
		this.costMatrixFile = costMatrixFile;
		this.parseCostMatrixFile(dataModel);
		this.setDoCostSensitiveSearch(true);
	}
	
	private void parseCostMatrixFile(DataModel dataModel) throws Exception {
		Map<String, Double> rawCosts = new TreeMap<String, Double>();
		
		BufferedReader br = new BufferedReader(new FileReader(this.costMatrixFile));
		String itemSeparator = FileType.CSV.getSeparator();
		
		String line = "";
		boolean header = true;
		List<String> classValues = new LinkedList<String>(); 
		while ((line = br.readLine()) != null) {
			String[] items = line.split(itemSeparator);
			if(header) {
				for(int i=1; i<items.length; i++) {
					if(items[i]==null) continue;
					rawCosts.put(items[i], new Double(0.0));
					classValues.add(items[i]);
				}
				header = false;
			} else {
				for(int i=1; i<items.length; i++) {
					if(items[i]==null) continue;
					Double classCost = rawCosts.get(classValues.get(i-1));
					classCost += Double.parseDouble(items[i]);
					rawCosts.put(classValues.get(i-1), classCost);
				}
			}
		}
		
		// normalizing costs
		this.misclassificationCost = new TreeMap<String, Double>();
		double sum_costs = 0.0;
		for(String classValue: dataModel.getClassLabels()) {
			double class_count = dataModel.getAttributeLabelIndices(dataModel.getClassAttribute()).get(classValue).size();
			sum_costs += rawCosts.get(classValue)*class_count;
		}
		for(String classValue: dataModel.getClassLabels()) {
			double norm_class_cost = (rawCosts.get(classValue)*dataModel.getNumRows())/sum_costs;
			this.misclassificationCost.put(classValue, norm_class_cost);
		}
		
		br.close();
	}
	
	public boolean doCostSensitiveSearch() {
		return doCostSensitiveSearch;
	}

	public void setDoCostSensitiveSearch(boolean doCostSensitiveSearch) {
		this.doCostSensitiveSearch = doCostSensitiveSearch;
	}

	public Map<String, Double> getMisclassificationCost() {
		return misclassificationCost;
	}

	public void setMisclassificationCost(Map<String, Double> misclassificationCost) {
		this.misclassificationCost = misclassificationCost;
	}
	
	public boolean doEnsemble() {
		return this.doEnsemble;
	}
	
	private void setDoEnsemble(boolean doEnsemble) {
		this.doEnsemble = doEnsemble;
	}

	public EnsembleAggregationType getAggregationType() {
		return aggregationType;
	}

	public void setAggregationType(EnsembleAggregationType aggregationType) {
		this.aggregationType = aggregationType;
	}

	public boolean doBagging() {
		return doBagging;
	}

	public void setDoBagging(boolean doBagging) {
		this.doBagging = doBagging;
		this.setDoEnsemble(true);
	}

	public boolean doBoosting() {
		return doBoosting;
	}

	public void setDoBoosting(boolean doBoosting) {
		this.doBoosting = doBoosting;
		this.setDoWeightedLearning(true);
		this.setDoEnsemble(true);
	}

	public boolean doWeightedLearning() {
		return doWeightedLearning;
	}

	public void setDoWeightedLearning(boolean doWeightedLearning) {
		this.doWeightedLearning = doWeightedLearning;
	}

	public int getNumBaseModels() {
		return numBaseModels;
	}

	public void setNumBaseModels(int numBaseModels) {
		this.numBaseModels = numBaseModels;
	}


	public int getNumEnsembles() {
		return numEnsembles;
	}

	public void setNumEnsembles(int numEnsembles) {
		this.numEnsembles = numEnsembles;
	}

	public boolean isUseComplexityPenaltyPrior() {
		return useComplexityPenaltyPrior;
	}

	public void setUseComplexityPenaltyPrior(boolean useComplexityPenaltyPrior) {
		this.useComplexityPenaltyPrior = useComplexityPenaltyPrior;
	}

	public double getKappa() {
		return kappa;
	}

	public void setKappa(double kappa) {
		this.kappa = kappa;
	}
	
	public boolean isUseInformativePrior() {
		return useInformativePrior;
	}

	public void setUseInformativePrior(boolean useInformativePrior) {
		this.useInformativePrior = useInformativePrior;
	}

	public BRLStructurePrior getInformativePrior() {
		return informativePrior;
	}

	public void setInformativePriorFile(String filename, String sep) {
		this.informativePrior = new BRLStructurePrior(filename, sep);
	}


	private CPTRepresentationType cptRepresentation = CPTRepresentationType.Global;
	private BRLSearchType searchAlgorithm = BRLSearchType.GreedyBestFirst;
	private HeuristicScoreType heuristicScore = HeuristicScoreType.BDeu;
	
	private int beamWidth = 100;
	private int maxParents = 8;
	
	//priors
	private String structurePriorsFile;
	private boolean useComplexityPenaltyPrior = true;
	private double kappa = 0.01;
	private boolean useInformativePrior = false;
	private BRLStructurePrior informativePrior = null;
	
	//cost sensitive methods
	private boolean doCostSensitiveSearch = false;
	private String costMatrixFile;
	private Map<String, Double> misclassificationCost;
	
	//ensemble methods
	private boolean doEnsemble = false;
	private EnsembleAggregationType aggregationType = EnsembleAggregationType.DefaultLinearCombination;
	private boolean doBagging = false;
	private boolean doBoosting = false;
	private boolean doWeightedLearning = false;
	private int numBaseModels = 10;
	private int numEnsembles = 100;
}
