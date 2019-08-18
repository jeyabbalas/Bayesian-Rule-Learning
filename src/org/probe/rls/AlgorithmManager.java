package org.probe.rls;

import org.probe.rls.algo.rule.RuleLearner;
import org.probe.rls.data.DataManager;
import org.probe.rls.data.DataModel;
import org.probe.rls.models.rulemodel.RuleModel;
import org.probe.rls.models.rulemodel.RuleModel;
import org.probe.rls.report.ReportManager;

public class AlgorithmManager {

	public AlgorithmManager(DataManager dataManager, RuleLearner ruleLearner,
			ReportManager reportManager) {
		this.dataManager = dataManager;
		this.ruleLearner = ruleLearner;
		this.reportManager = reportManager;
	}
	
	public void runAlgoOnData() throws Exception {
		DataModel dataModel = dataManager.getDataModel();
		
		ruleLearner.setDataModel(dataModel);
		ruleLearner.runAlgo();
	}
	
	public RuleModel getRuleModel() {
		if(ruleLearner.hasLearntRules()){
			return ruleLearner.getRuleModel();
		}
		else return EMPTY_RULE_MODEL;
	}
	
	private final DataManager dataManager;
	private final RuleLearner ruleLearner;
	private final ReportManager reportManager;
	
	public static final RuleModel EMPTY_RULE_MODEL = RuleModel.createEmptyRuleModel();
}
