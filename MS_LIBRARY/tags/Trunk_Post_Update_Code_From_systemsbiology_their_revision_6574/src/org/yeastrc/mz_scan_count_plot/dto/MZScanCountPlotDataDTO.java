package org.yeastrc.mz_scan_count_plot.dto;


/**
 * 
 * for table mz_scan_count_plot_data
 */
public class MZScanCountPlotDataDTO {

	private int experimentId;
	private String plotData;
	private int scanCount;
	private int createTimeInSeconds;
	private int dataVersion;

	public int getExperimentId() {
		return experimentId;
	}
	public void setExperimentId(int experimentId) {
		this.experimentId = experimentId;
	}
	public String getPlotData() {
		return plotData;
	}
	public void setPlotData(String plotData) {
		this.plotData = plotData;
	}
	public int getScanCount() {
		return scanCount;
	}
	public void setScanCount(int scanCount) {
		this.scanCount = scanCount;
	}
	public int getCreateTimeInSeconds() {
		return createTimeInSeconds;
	}
	public void setCreateTimeInSeconds(int createTimeInSeconds) {
		this.createTimeInSeconds = createTimeInSeconds;
	}	
	public int getDataVersion() {
		return dataVersion;
	}
	public void setDataVersion(int dataVersion) {
		this.dataVersion = dataVersion;
	}
}
