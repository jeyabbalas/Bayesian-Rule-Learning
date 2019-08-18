package org.probe.rls.data.discretize;

import java.util.List;

import org.probe.rls.data.DataAttribute;
import org.probe.rls.data.DataModel;

public interface BinGenerator {
	List<String> generate(DataModel dataModel, DataAttribute attribute, String[] options) throws Exception;
	List<String> generate(DataModel dataModel, DataAttribute attribute) throws Exception;
}
