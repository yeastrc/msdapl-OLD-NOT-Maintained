package org.yeastrc.ms.parser.mzxml;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.systemsbiology.jrap.stax.DataProcessingInfo;
import org.systemsbiology.jrap.stax.MSInstrumentInfo;
import org.systemsbiology.jrap.stax.MSXMLParser;
import org.systemsbiology.jrap.stax.MZXMLFileInfo;
import org.systemsbiology.jrap.stax.Scan;
import org.systemsbiology.jrap.stax.ScanHeader;
import org.systemsbiology.jrap.stax.SoftwareInfo;
import org.yeastrc.ms.domain.run.DataConversionType;
import org.yeastrc.ms.domain.run.MsRunIn;
import org.yeastrc.ms.domain.run.MsScanIn;
import org.yeastrc.ms.parser.DataProviderException;
import org.yeastrc.ms.parser.MzXmlDataProvider;
import org.yeastrc.ms.parser.ms2File.Scan.PEAK_TYPE;

public class MzXmlFileReader implements MzXmlDataProvider {

    private String sha1Sum;
    private String filename;
    private MSXMLParser parser;
    private int numScans = 0;
    private int numScansRead = 0;
    
    private static final Pattern rtPattern = Pattern.compile("^PT(\\d+\\.?\\d*)S$"); 
    
    
    @Override
    public void open(String filePath, String sha1Sum)
            throws DataProviderException {
        this.sha1Sum = sha1Sum;
        this.filename = new File(filePath).getName();
        parser = new MSXMLParser(filePath);
        numScans = parser.getScanCount();
    }
    
    @Override
    public void close() {
        // TODO
    }

    @Override
    public String getFileName() {
        return filename;
    }

    @Override
    public MsScanIn getNextScan() throws DataProviderException {
        if(numScansRead >= numScans)
            return null;
        Scan scan = parser.rap(++numScansRead);
        if(scan == null)
            return null;
        
        ScanHeader header = scan.getHeader();
        MzXmlScan mScan = new MzXmlScan(PEAK_TYPE.NUMBER);
        mScan.setMsLevel(header.getMsLevel());
        mScan.setStartScanNum(header.getNum());
        mScan.setEndScanNum(header.getNum());
        
        if(header.getPrecursorScanNum() > 0) {
            mScan.setPrecursorScanNum(header.getPrecursorScanNum());
            mScan.setPrecursorMz(new BigDecimal(header.getPrecursorMz()));
        }
        mScan.setRetentionTime(getRetentionTime(header));
        mScan.setDataConversionType(getDataConversionType(header.getCentroided()));
        
        mScan.setPeakCount(header.getPeaksCount());
        double[][] mzInt = scan.getMassIntensityList();
        // Peak 0 mass = list[0][0], peak 0 intensity = list[1][0]
        // Peak 1 mass = list[0][1], peak 1 intensity = list[1][1]
        for(int i = 0; i < header.getPeaksCount(); i++) {
            double mz = mzInt[0][i];
            double intensity = mzInt[1][i];
            mScan.addPeak(mz, (float)intensity);
        }
        return mScan;
    }
    
    public static void main(String[] args) {
        
        String rt = "PT60.1361S";
        rt = rt.trim();
        
        Matcher m = rtPattern.matcher(rt);
        if(m.matches()) {
            String time = m.group(1);
            if(time != null) {
                BigDecimal bd =  new BigDecimal(time);
                System.out.println(bd);
            }
        }
    }
    
    private BigDecimal getRetentionTime(ScanHeader header) {
        // In the schema, retentionTime is "xs:duration" 
        // http://www.w3schools.com/Schema/schema_dtypes_date.asp
        String rt = header.getRetentionTime();
        if(rt == null)  return null;
        rt = rt.trim();
        
        Matcher m = rtPattern.matcher(rt);
        if(m.matches()) {
            String time = m.group(1);
            if(time != null) {
                return new BigDecimal(time);
            }
        }
        return null;
    }

    @Override
    public MsRunIn getRunHeader() throws DataProviderException {
        MZXMLFileInfo info = parser.rapFileHeader();
        MzXmlHeader run = new MzXmlHeader();
        
        DataProcessingInfo dpInfo = info.getDataProcessing();
        
        DataConversionType dataConvType = getDataConversionType(dpInfo.getCentroided());
        run.setDataConversionType(dataConvType);
        
        List<SoftwareInfo> swList = dpInfo.getSoftwareUsed();
        // TODO handle multiple software info.
        if(swList.size() > 0) {
            for(SoftwareInfo si: swList) {
                if(si.type.equalsIgnoreCase("conversion")) {
                    run.setConversionSW(swList.get(0).name);
                    run.setConversionSWVersion(swList.get(0).version);
                }
            }
        }
        
        MSInstrumentInfo msiInfo = info.getInstrumentInfo();
        run.setInstrumentModel(msiInfo.getModel());
        run.setInstrumentVendor(msiInfo.getManufacturer());
      
        return run;
    }

    private DataConversionType getDataConversionType(int centroided) {
        
        if (centroided == DataProcessingInfo.NO)
            return DataConversionType.NON_CENTROID;
        else if (centroided == DataProcessingInfo.YES)
            return DataConversionType.CENTROID;
        else
            return DataConversionType.UNKNOWN;
        
    }
}
