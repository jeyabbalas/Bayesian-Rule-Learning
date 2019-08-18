package org.probe.rls.data;

import java.util.List;

import org.probe.rls.data.fold.FoldGenerator;

public interface DataManager {
	DataModel getDataModel();

	List<List<DataModel>> createFolds(FoldGenerator foldGenerator) throws Exception;
}
