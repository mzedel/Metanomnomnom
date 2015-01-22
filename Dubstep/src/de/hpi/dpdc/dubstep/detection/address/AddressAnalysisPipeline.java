package de.hpi.dpdc.dubstep.detection.address;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class AddressAnalysisPipeline extends ArrayList<AddressAnalysisMetric> {
    
    /**
     * 
     */
    private static final long serialVersionUID = -48740574341234072L;
    
    private List<AddressAnalysisMetric> metrics;
    
    public AddressAnalysisPipeline() {
	this.metrics = new ArrayList<AddressAnalysisMetric>();
    }
    
    public List<Duplicate> execute() {
	for (AddressAnalysisMetric metric : metrics) {
	    
	}
	return null;
    }
}
