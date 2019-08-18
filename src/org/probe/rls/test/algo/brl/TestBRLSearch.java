package org.probe.rls.test.algo.brl;


import org.junit.Before;
import org.junit.Test;
import org.probe.rls.algo.rule.brl.BRLSearch;
import org.probe.rls.algo.rule.brl.BRLSearchParameters;
import org.probe.rls.algo.rule.brl.datastructures.ConstrainedBayesianNetwork;
import org.probe.rls.algo.rule.brl.parameters.CPTRepresentationType;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.FileDataManager;
import org.probe.rls.data.FileType;

public class TestBRLSearch {

	@Before
	public void init() {
		dataManager = new FileDataManager();
		try {
			dataManager.loadFromFile("Test//train_disc.txt", FileType.TSV.getSeparator());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGreedyBestFirstSearch() {
		BRLSearchParameters params = new BRLSearchParameters();
		params.setCptRepresentation(CPTRepresentationType.Global);
		DataModel dataModel = dataManager.getDataModel();
		
		BRLSearch brlSearch = new BRLSearch(params, dataModel);
		ConstrainedBayesianNetwork bestModel = brlSearch.runGreedyBestFirstSearch();
		
		System.out.println(bestModel.toString());
	}
	
	private FileDataManager dataManager;
}
