/**
 * PercolatorQCAnalysisGetter.java
 * @author Vagisha Sharma
 * Jan 22, 2011
 */
package org.yeastrc.experiment.stats;

import java.util.List;

import org.apache.log4j.Logger;
import org.yeastrc.www.project.experiment.stats.PlotUrlCache;

/**
 * 
 */
public class PercolatorQCStatsGetter {

	private static final Logger log = Logger.getLogger(PercolatorQCStatsGetter.class.getName());
	
	private boolean getPsmRtStats = false;
	private FileStats psmAnalysisStats;		// stats at the analysis level
	private List<FileStats> psmFileStats; 	// stats for individual files in the analysis
	private String psmDistrUrl;
	
	private boolean getSpectraRtStats = false;
	private FileStats spectraAnalysisStats; 	// stats at the analysis level
	private List<FileStats> spectraFileStats;	// stats for the individual files in the analysis
	private String spectraDistrUrl;
	
	public static final double QVAL = 0.01;
	
	public PercolatorQCStatsGetter () {}
	
	public void getStats(int analysisId, double qvalue) {
		
		PlotUrlCache cache = PlotUrlCache.getInstance();
		if(getPsmRtStats)
			getPsmRtStats(analysisId, qvalue, cache);
		if(getSpectraRtStats)
			getSpectraRtStats(analysisId, qvalue, cache);
	}
	
	public void setGetPsmRtStats(boolean getPsmRtStats) {
		this.getPsmRtStats = getPsmRtStats;
	}

	public void setGetSpectraRtStats(boolean getSpectraRtStats) {
		this.getSpectraRtStats = getSpectraRtStats;
	}

	public FileStats getPsmAnalysisStats() {
		return psmAnalysisStats;
	}

	public List<FileStats> getPsmFileStats() {
		return psmFileStats;
	}

	public String getPsmDistrUrl() {
		return psmDistrUrl;
	}

	public FileStats getSpectraAnalysisStats() {
		return spectraAnalysisStats;
	}

	public List<FileStats> getSpectraFileStats() {
		return spectraFileStats;
	}

	public String getSpectraDistrUrl() {
		return spectraDistrUrl;
	}

	// -----------------------------------------------------------------------------
    // PSM-RT stats
    // -----------------------------------------------------------------------------
	private void getPsmRtStats(int analysisId, double qvalue, PlotUrlCache cache) {
		
        psmDistrUrl = cache.getPsmRtPlotUrl(analysisId, qvalue);
        psmFileStats = cache.getPsmRtFileStats(analysisId, qvalue);
        if(psmDistrUrl == null) {
        	PercolatorPsmRetTimeDistributionGetter distrGetter = new PercolatorPsmRetTimeDistributionGetter(analysisId, qvalue);
        	PercolatorPsmRetTimeDistribution result = distrGetter.getDistribution();
            psmFileStats = result.getFileStatsList();
            psmDistrUrl = result.getGoogleChartUrl();
            cache.addPsmRtPlotUrl(analysisId, qvalue, psmDistrUrl, psmFileStats);
        }
        
        log.info("#PSM-RT Plot URL: "+psmDistrUrl);
        
        // stats at the experiment (analysis) level
        psmAnalysisStats = new FileStats(analysisId, "none");
        int totalCount = 0; int goodCount = 0;
        for(FileStats stat: psmFileStats) { totalCount += stat.getTotalCount(); goodCount += stat.getGoodCount();}
    	psmAnalysisStats.setTotalCount(totalCount);
    	psmAnalysisStats.setGoodCount(goodCount);
    	
        if(psmFileStats.get(0).getHasPopulationStats()) {
        	FileStats st = psmFileStats.get(0);
        	psmAnalysisStats.setPopulationMin(st.getPopulationMin());
        	psmAnalysisStats.setPopulationMax(st.getPopulationMax());
        	psmAnalysisStats.setPopulationMean(st.getPopulationMean());
        	psmAnalysisStats.setPopulationStandardDeviation(st.getPopulationStandardDeviation());
        }
	}
	
	// -----------------------------------------------------------------------------
    // Spectra-RT stats
    // -----------------------------------------------------------------------------
	private void getSpectraRtStats(int analysisId, double qvalue, PlotUrlCache cache) {
		
		spectraDistrUrl = cache.getSpectraRtPlotUrl(analysisId, qvalue);
		spectraFileStats = cache.getSpectraRtFileStats(analysisId, qvalue);
        
        if(spectraDistrUrl == null) {
        	PercolatorSpectraRetTimeDistributionGetter distrGetter = new PercolatorSpectraRetTimeDistributionGetter(analysisId, qvalue);
        	PercolatorSpectraRetTimeDistribution result = distrGetter.getDistribution();
            spectraFileStats = result.getFileStatsList();
            spectraDistrUrl = result.getGoogleChartUrl();
            PlotUrlCache.getInstance().addSpectraRtPlotUrl(analysisId, qvalue, spectraDistrUrl, spectraFileStats);
        }
        
        log.info("#Spectra-RT Plot URL: "+spectraDistrUrl);
        
        // stats at the experiment (analysis) level
        spectraAnalysisStats = new FileStats(analysisId, "none");
        int totalSpectraCount = 0; int goodSpectraCount = 0;
        for(FileStats stat: spectraFileStats) { totalSpectraCount += stat.getTotalCount(); goodSpectraCount += stat.getGoodCount();}
        spectraAnalysisStats.setTotalCount(totalSpectraCount);
        spectraAnalysisStats.setGoodCount(goodSpectraCount);
    	
        if(spectraFileStats.get(0).getHasPopulationStats()) {
        	FileStats st = spectraFileStats.get(0);
        	spectraAnalysisStats.setPopulationMin(st.getPopulationMin());
        	spectraAnalysisStats.setPopulationMax(st.getPopulationMax());
        	spectraAnalysisStats.setPopulationMean(st.getPopulationMean());
        	spectraAnalysisStats.setPopulationStandardDeviation(st.getPopulationStandardDeviation());
        }
	}
	
}