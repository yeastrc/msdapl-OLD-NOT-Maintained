/**
 * PercolatorPsmFilteredResultDAO.java
 * @author Vagisha Sharma
 * Sep 29, 2010
 */
package org.yeastrc.ms.dao.analysis.percolator;

import org.yeastrc.ms.domain.analysis.percolator.impl.PercolatorFilteredPsmResult;

/**
 * 
 */
public interface PercolatorFilteredPsmResultDAO {

	public PercolatorFilteredPsmResult load(int runSearchAnalysisId);
	
	public void save(PercolatorFilteredPsmResult result);
	
	public void delete(int runSearchAnalysisId);
}
