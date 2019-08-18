package org.probe.rls.data.fold;

import java.util.List;

import org.probe.rls.data.DataModel;

public interface FoldGenerator {
	void generateFolds(DataModel dataModel) throws Exception;
	int numFolds();
	List<DataModel> getFold(int foldID);
}
