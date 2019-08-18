package org.probe.rls.models.rulemodel;

import java.util.List;


public interface RuleLHS {

	public List<RuleLiteral> getLiteralsUsed();
	public String toString();
}
