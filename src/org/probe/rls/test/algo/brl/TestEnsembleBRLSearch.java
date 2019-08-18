package org.probe.rls.test.algo.brl;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.probe.rls.algo.rule.brl.BRLSearch;
import org.probe.rls.algo.rule.brl.BRLSearchParameters;
import org.probe.rls.algo.rule.brl.datastructures.ConstrainedBayesianNetwork;
import org.probe.rls.algo.rule.brl.parameters.BRLSearchType;
import org.probe.rls.algo.rule.brl.parameters.CPTRepresentationType;
import org.probe.rls.algo.rule.brl.parameters.EnsembleAggregationType;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.FileDataManager;
import org.probe.rls.data.FileType;

public class TestEnsembleBRLSearch {

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
	public void testBaggingSearch() {
		BRLSearchParameters params = new BRLSearchParameters();
		params.setCptRepresentation(CPTRepresentationType.Global);
		params.setSearchAlgorithm(BRLSearchType.Bagging);
		DataModel dataModel = dataManager.getDataModel();
		
		BRLSearch brlSearch = new BRLSearch(params, dataModel);
		List<ConstrainedBayesianNetwork> baggedModels;
		try {
			baggedModels = brlSearch.runEnsembleBRLSearch();
			
			for(ConstrainedBayesianNetwork baggedModel: baggedModels) {
				System.out.println(baggedModel.toString());
				System.out.println("=======================================================");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBoostingSearch() {
		BRLSearchParameters params = new BRLSearchParameters();
		params.setCptRepresentation(CPTRepresentationType.Global);
		params.setSearchAlgorithm(BRLSearchType.Boosting);
		DataModel dataModel = dataManager.getDataModel();
		
		BRLSearch brlSearch = new BRLSearch(params, dataModel);
		List<ConstrainedBayesianNetwork> boostedModels;
		try {
			boostedModels = brlSearch.runEnsembleBRLSearch();
			
			for(ConstrainedBayesianNetwork boostedModel: boostedModels) {
				System.out.println(boostedModel.toString());
				System.out.println("=======================================================");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBMABaggingSearch() {
		BRLSearchParameters params = new BRLSearchParameters();
		params.setCptRepresentation(CPTRepresentationType.Global);
		params.setSearchAlgorithm(BRLSearchType.Bagging);
		params.setAggregationType(EnsembleAggregationType.SelectiveBayesianModelAveraging);
		DataModel dataModel = dataManager.getDataModel();
		
		BRLSearch brlSearch = new BRLSearch(params, dataModel);
		List<ConstrainedBayesianNetwork> bmaBaggedModels;
		try {
			bmaBaggedModels = brlSearch.runEnsembleBRLSearch();
			
			for(ConstrainedBayesianNetwork bmaBaggedModel: bmaBaggedModels) {
				System.out.println(bmaBaggedModel.toString());
				System.out.println("=======================================================");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBMCBaggingSearch() {
		BRLSearchParameters params = new BRLSearchParameters();
		params.setCptRepresentation(CPTRepresentationType.Global);
		params.setSearchAlgorithm(BRLSearchType.Bagging);
		params.setAggregationType(EnsembleAggregationType.SelectiveBayesianModelCombination);
		params.setNumEnsembles(10);
		DataModel dataModel = dataManager.getDataModel();
		
		BRLSearch brlSearch = new BRLSearch(params, dataModel);
		List<ConstrainedBayesianNetwork> bmcBaggedModels;
		try {
			bmcBaggedModels = brlSearch.runEnsembleBRLSearch();
			
			for(ConstrainedBayesianNetwork bmcBaggedModel: bmcBaggedModels) {
				System.out.println(bmcBaggedModel.toString());
				System.out.println("=======================================================");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private FileDataManager dataManager;
}
