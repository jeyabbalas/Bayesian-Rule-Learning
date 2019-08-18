package org.probe.rls.algo.rule;

import org.probe.rls.algo.DataLearner;
import org.probe.rls.models.rulemodel.RuleModel;
import org.probe.rls.models.rulemodel.ensemble.EnsembleRuleModel;

public interface RuleLearner extends DataLearner {
	boolean hasLearntRules();
	RuleModel getRuleModel();
	EnsembleRuleModel getEnsembleRuleModel();
}
