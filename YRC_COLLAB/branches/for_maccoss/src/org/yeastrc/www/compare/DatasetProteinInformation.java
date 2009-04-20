/**
 * DataSetInformation.java
 * @author Vagisha Sharma
 * Apr 11, 2009
 * @version 1.0
 */
package org.yeastrc.www.compare;

/**
 * 
 */
public class DatasetProteinInformation {

    private boolean present;
    private boolean parsimonious;
    private boolean grouped;
    private int normalizedSpectrumCount;
    private final Dataset dataset;
    
    public DatasetProteinInformation(Dataset dataset) {
        this.dataset = dataset;
    }
    
    public Dataset getDataset() {
        return dataset;
    }
    
    public int getDatasetId() {
        return dataset.getDatasetId();
    }
    
    public DatasetSource getDatasetSource() {
        return dataset.getSource();
    }
    
    public boolean isPresent() {
        return present;
    }
    public void setPresent(boolean present) {
        this.present = present;
    }
    public boolean isParsimonious() {
        return parsimonious;
    }
    public void setParsimonious(boolean parsimonious) {
        this.parsimonious = parsimonious;
    }

    public int getNormalizedSpectrumCount() {
        return normalizedSpectrumCount;
    }

    public void setNormalizedSpectrumCount(int normalizedSpectrumCount) {
        this.normalizedSpectrumCount = normalizedSpectrumCount;
    }

    public boolean isGrouped() {
        return grouped;
    }

    public void setGrouped(boolean grouped) {
        this.grouped = grouped;
    }
}