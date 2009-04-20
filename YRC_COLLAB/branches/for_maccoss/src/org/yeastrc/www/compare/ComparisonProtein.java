/**
 * ComparisonProtein.java
 * @author Vagisha Sharma
 * Apr 11, 2009
 * @version 1.0
 */
package org.yeastrc.www.compare;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class ComparisonProtein {

    private final int nrseqId;
    private String systematicName;
    private String name;
    private String description;
    private int maxPeptideCount;
    
    private List<DatasetProteinInformation> datasetInfo;
    
    public ComparisonProtein(int nrseqId) {
        this(nrseqId, null, null);
    }
    
    public ComparisonProtein(int nrseqId, String name, String description) {
        this.nrseqId = nrseqId;
        this.name = name;
        this.description = description;
        datasetInfo = new ArrayList<DatasetProteinInformation>();
    }

    public int getNrseqId() {
        return nrseqId;
    }

    public List<DatasetProteinInformation> getDatasetInfo() {
        return datasetInfo;
    }
    
    public void setDatasetInformation(List<DatasetProteinInformation> infoList) {
        this.datasetInfo = infoList;
    }
    
    public void addDatasetInformation(DatasetProteinInformation info) {
        datasetInfo.add(info);
    }
    
    public DatasetProteinInformation getDatasetProteinInformation(Dataset dataset) {
        
        for(DatasetProteinInformation dsInfo: datasetInfo) {
            if(dataset.equals(dsInfo.getDataset())) {
                return dsInfo;
            }
        }
        return null;
    }
    
    public boolean isInDataset(Dataset dataset) {
        DatasetProteinInformation dpi = getDatasetProteinInformation(dataset);
        if(dpi != null)
            return dpi.isPresent();
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxPeptideCount() {
        return maxPeptideCount;
    }

    public void setMaxPeptideCount(int maxPeptideCount) {
        this.maxPeptideCount = maxPeptideCount;
    }

    public String getSystematicName() {
        return systematicName;
    }

    public void setSystematicName(String systematicName) {
        this.systematicName = systematicName;
    }
}
