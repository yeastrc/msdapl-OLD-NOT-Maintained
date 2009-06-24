/**
 * MzXmlScan.java
 * @author Vagisha Sharma
 * Jun 23, 2009
 * @version 1.0
 */
package org.yeastrc.ms.parser.mzxml;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.yeastrc.ms.domain.run.DataConversionType;
import org.yeastrc.ms.domain.run.MsScanIn;
import org.yeastrc.ms.domain.run.Peak;
import org.yeastrc.ms.parser.ms2File.Scan.PEAK_TYPE;

/**
 * 
 */
public class MzXmlScan implements MsScanIn {

    private int msLevel = 1;
    private int startScan = -1;
    private int endScan = -1;
    private int precursorScanNum = -1;

    private BigDecimal precursorMz;
    private BigDecimal retentionTime;
    private String activationType;

    private DataConversionType dataConversionType = DataConversionType.UNKNOWN;
    
    private int peakCount;
    private List<Peak> peakNList;
    private List<String[]> peakList;
    private PEAK_TYPE peakType;
    
    public MzXmlScan(PEAK_TYPE peakType) {
        this.peakType = peakType;
        
        if(peakType == PEAK_TYPE.STRING) {
            peakList = new ArrayList<String[]>();
        }
        else {
            peakNList = new ArrayList<Peak>();
        }
    }
    
    @Override
    public List<Peak> getPeaks() {
        if(peakNList != null)
            return peakNList;
        else {
            peakNList = new ArrayList<Peak>(peakList.size());
            for(String[] peakArr: peakList) {
                double mz = Double.parseDouble(peakArr[0]);
                float intensity = Float.parseFloat(peakArr[1]);
                peakNList.add(new Peak(mz, intensity));
            }
            return peakNList;
        }
    }

    @Override
    public List<String[]> getPeaksString() {
        if(peakList != null)
            return peakList;
        else {
            peakList = new ArrayList<String[]>(peakNList.size());
            for(Peak peak: peakNList) {
                peakList.add(new String[]{String.valueOf(peak.getMz()), String.valueOf(peak.getIntensity())});
            }
            return peakList;
        }
    }
    
    public void addPeak(double mz, float intensity) {
        if(this.peakType == PEAK_TYPE.STRING)
            throw new IllegalArgumentException("MzXmlScan does not accept peak data as numbers.");
        peakNList.add(new Peak(mz, intensity));
    }
    
    
    @Override
    public int getPeakCount() {
        return peakCount;
    }
    
    public void setPeakCount(int peakCount) {
        this.peakCount = peakCount;
    }

    @Override
    public int getMsLevel() {
        return msLevel;
    }
    
    public void setMsLevel(int msLevel) {
        this.msLevel = msLevel;
    }

    @Override
    public BigDecimal getPrecursorMz() {
        return precursorMz;
    }

    public void setPrecursorMz(BigDecimal precursorMz) {
        this.precursorMz = precursorMz;
    }
    
    @Override
    public int getPrecursorScanNum() {
        return precursorScanNum;
    }
    
    public void setPrecursorScanNum(int scanNum) {
        this.precursorScanNum = scanNum;
    }
    
    @Override
    public BigDecimal getRetentionTime() {
        return retentionTime;
    }
    
    public void setRetentionTime(BigDecimal retentionTime) {
        this.retentionTime = retentionTime;
    }

    @Override
    public int getStartScanNum() {
        return startScan;
    }
    
    public void setStartScanNum(int scanNum) {
        this.startScan = scanNum;
    }
    
    @Override
    public int getEndScanNum() {
        return endScan;
    }
    
    public void setEndScanNum(int scanNum) {
        this.endScan = scanNum;
    }

    @Override
    public DataConversionType getDataConversionType() {
        return this.dataConversionType;
    }
    
    public void setDataConversionType(DataConversionType type) {
        this.dataConversionType = type;
    }

    @Override
    public String getFragmentationType() {
        return this.activationType;
    }
    
    public void setFragmentationType(String fragmentationType) {
        this.activationType = fragmentationType;
    }
}
