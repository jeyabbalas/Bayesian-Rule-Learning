package org.probe.rls.algo;

import org.probe.rls.data.DataModel;

public interface DataLearner {
	void setDataModel(DataModel dataModel);
	void runAlgo() throws Exception;
}
