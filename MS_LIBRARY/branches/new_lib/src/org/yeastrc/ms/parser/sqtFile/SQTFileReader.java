package org.yeastrc.ms.parser.sqtFile;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;

import org.yeastrc.ms.domain.MsSearchModification;
import org.yeastrc.ms.parser.ParserException;


public class SQTFileReader {

    private BufferedReader reader;
    private String currentLine;
    private int currentLineNum = 0;
    private List<MsSearchModification> searchDynamicMods;

    public void open(String filePath) {
        try {
            reader = new BufferedReader(new FileReader(filePath));
            advanceLine();
        }
        catch (FileNotFoundException e) {
            closeAndThrowException("File does not exist: "+filePath, e);
        }
        catch (IOException e) {
            closeAndThrowException("Error reading file: "+filePath, e);
        }
    }


    public void open(InputStream inStream) {
        try {
            reader = new BufferedReader(new InputStreamReader(inStream));
            advanceLine();
        }
        catch (IOException e) {
            closeAndThrowException("Error reading file from input stream", e);
        }
    }

    public Header getHeader() {
        
        Header header = new Header();
        while (isHeaderLine(currentLine)) {
            String[] tokens = currentLine.split("\\t");
            if (tokens.length >= 3) {
                StringBuilder val = new StringBuilder();
                // the value for the header may be a tab separated list; get the entire string
                // e.g. H       Alg-MaxDiffMod  3H      Alg-DisplayTop  5
                for (int i = 2; i < tokens.length; i++)
                    val.append(tokens[i]);
                header.addHeaderItem(tokens[1], val.toString());
            }
            else if (tokens.length >= 2){
                // maybe the header and value are separated by a space rather than a tab
                String temp = tokens[1].trim(); // remove any trailing space first
                int i = temp.indexOf(' '); // look for the first space character
                if (i != -1)
                    header.addHeaderItem(temp.substring(0, i), temp.substring(i+1));
                else
                    // if the value for this header is missing, add the header with a empty String
                    header.addHeaderItem(tokens[1], "");
            }
            else {
                // ignore if both label and value for this header item are missing
                //throw new Exception("Invalid header: "+currentLine);
            }

            try {
                advanceLine();
            }
            catch (IOException e) {
                closeAndThrowException(e);
            }
        }
        this.searchDynamicMods = header.getDynamicModifications();
        return header;
    }


    private void advanceLine() throws IOException {
        currentLineNum++;
        currentLine = reader.readLine(); // advance first
        // skip over blank lines
        while(currentLine != null && currentLine.trim().length() == 0) {
            currentLineNum++;
            currentLine = reader.readLine();
        }
    }
    
    public boolean hasScans() {
        return currentLine != null;
    }

    public ScanResult getNextScan() {

        ScanResult scan = parseScanResult();
        PeptideResult result = null; // current result

        try {
            advanceLine();

            while(currentLine != null) {
                
                // is this one of the charge states of the scan?
                if (isResultLine(currentLine)) {
                    // if this is NOT the first time we are seeing a 'M' line add the previous result
                    if (result != null)
                        scan.addPeptideResult(result);
                    result = parsePeptideResult(scan.getStartScan(), scan.getCharge());
                }
                // is this one of the charge independent analysis for this scan?
                else if (isLocusLine(currentLine)) {
                    
                    DbLocus locus = parseLocus();
                    // add this to the current result
                    result.addMatchingLocus(locus);
                }
                // it is neither throw an exception
                else {
                   closeAndThrowException("Don't know how to parse line: "+currentLine);
                }
                advanceLine();
                if (currentLine == null || isScanLine(currentLine))
                    break;
            }
            
            // add the last peptide result
            if (result != null)
                scan.addPeptideResult(result);
        }
        catch (IOException e) {
            closeAndThrowException(e);
        }

        return scan;
    }


    private ScanResult parseScanResult() {

        // make sure we have a scan line
        if (!isScanLine(currentLine))
            closeAndThrowException("Error parsing scan. Expected line starting with \"S\"\n"+currentLine);

        String[] tokens = currentLine.split("\\t");
        if (tokens.length < 10)
            closeAndThrowException("Expected 10 fields in scan line: "+currentLine);


        ScanResult scan = new ScanResult();
        scan.setStartScan(Integer.parseInt(tokens[1]));
        scan.setEndScan(Integer.parseInt(tokens[2]));
        scan.setCharge(Integer.parseInt(tokens[3]));
        scan.setProcessingTime(Integer.parseInt(tokens[4]));
        scan.setServer(tokens[5]);
        scan.setObservedMass(new BigDecimal(tokens[6]));
        scan.setTotalIntensity(new BigDecimal(tokens[7]));
        scan.setLowestSp(new BigDecimal(tokens[8]));
        scan.setNumMatching(Integer.parseInt(tokens[9]));

        return scan;
    }

    private PeptideResult parsePeptideResult(int scanNumber, int charge) {
        
        String[] tokens = currentLine.split("\\t");
        if (tokens.length < 11)
            closeAndThrowException("Expected 11 fields in scan line: "+currentLine);
        
        for (int i = 0; i < tokens.length; i++)
            tokens[i] = tokens[i].replaceAll("\\s+", "");
        
        PeptideResult result = new PeptideResult(searchDynamicMods);
        result.setxCorrRank(Integer.parseInt(tokens[1]));
        result.setSpRank(Integer.parseInt(tokens[2]));
        result.setMass(new BigDecimal(tokens[3]));
        result.setDeltaCN(new BigDecimal(tokens[4]));
        result.setXcorr(new BigDecimal(tokens[5]));
        result.setSp(new BigDecimal(tokens[6]));
        result.setNumMatchingIons(Integer.parseInt(tokens[7]));
        result.setNumPredictedIons(Integer.parseInt(tokens[8]));
        result.setResultSequence(tokens[9]);
        result.setValidationStatus(tokens[10].charAt(0));
        result.setCharge(charge);
        result.setScanNumber(scanNumber);
        return result;
    }
    
    private DbLocus parseLocus() {
        String[] tokens = currentLine.split("\\t");
        if (tokens.length < 2)
            closeAndThrowException("2 fields expected for line: "+currentLine);

        if (tokens.length > 2)
            return new DbLocus(tokens[1], tokens[2]);
        else
            return new DbLocus(tokens[1], null);
    }


    private boolean isScanLine(String line) {
        if (line == null)   return false;
        return line.startsWith("S");
    }

    private boolean isHeaderLine(String line) {
        if (line == null)   return false;
        return line.startsWith("H");
    }

    private boolean isResultLine(String line) {
        if (line == null)   return false;
        return line.startsWith("M");
    }

    private boolean isLocusLine(String line) {
        if (line == null)   return false;
        return line.startsWith("L");
    }

    /**
     * This method should be called explicitly after the file has been read.
     */
    public void close() {
        if (reader != null) 
            try {reader.close();}
        catch (IOException e) {}
    }

    private void closeAndThrowException(Exception e) {
        closeAndThrowException("Error reading file.", e);
    }

    private void closeAndThrowException(String message, Exception e) {
        close();
        throw new ParserException(currentLineNum, message, e);
    }

    private void closeAndThrowException(String message) {
        close();
        throw new ParserException(currentLineNum, message);
    }
}
