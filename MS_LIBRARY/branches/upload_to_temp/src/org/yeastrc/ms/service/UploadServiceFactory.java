/**
 * UploadServiceFactory.java
 * @author Vagisha Sharma
 * Mar 25, 2009
 * @version 1.0
 */
package org.yeastrc.ms.service;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.yeastrc.ms.domain.run.RunFileFormat;
import org.yeastrc.ms.domain.search.SearchFileFormat;
import org.yeastrc.ms.parser.sqtFile.SQTFileReader;
import org.yeastrc.ms.service.ms2file.MS2DataUploadService2;
import org.yeastrc.ms.service.mzxml.MzXmlDataUploadService;
import org.yeastrc.ms.service.pepxml.PepXmlMascotDataUploadService;
import org.yeastrc.ms.service.pepxml.PepXmlSequestDataUploadService;
import org.yeastrc.ms.service.pepxml.PepxmlAnalysisDataUploadService;
import org.yeastrc.ms.service.protxml.ProtxmlDataUploadService;
import org.yeastrc.ms.service.sqtfile.PercolatorSQTDataUploadService;
import org.yeastrc.ms.service.sqtfile.ProlucidSQTDataUploadService;
import org.yeastrc.ms.service.sqtfile.SequestSQTDataUploadService2;

/**
 * 
 */
public class UploadServiceFactory {

    private static final UploadServiceFactory instance = new UploadServiceFactory();
    
    private UploadServiceFactory() {}
    
    public static UploadServiceFactory instance() {
        return instance;
    }
    
    
    // ------------------------------------------------------------------------------------------------------
    // Spectrum Data Upload service
    // ------------------------------------------------------------------------------------------------------
    public SpectrumDataUploadService getSpectrumDataUploadService(String dataDirectory) throws UploadServiceFactoryException {
        
        if(dataDirectory == null) {
            throw new UploadServiceFactoryException("dataDirectory is null");
        }
        
        File dir = new File(dataDirectory);
        if(!dir.exists()) {
            throw new UploadServiceFactoryException("dataDirectory does not exist: "+dataDirectory);
        }
        
        if(!dir.isDirectory()) {
            throw new UploadServiceFactoryException(dataDirectory+" is not a directory");
        }
        
        File[] files = dir.listFiles();
        Set<RunFileFormat> formats = new HashSet<RunFileFormat>();
        for (int i = 0; i < files.length; i++) {
            if(files[i].isDirectory())
                continue;
            RunFileFormat format = RunFileFormat.forFile(files[i].getName());
            if(format == RunFileFormat.UNKNOWN) 
                continue;
            
            formats.add(format);
        }
        
        if(formats.size() == 0) {
            throw new UploadServiceFactoryException("No valid spectrum data file format found in directory: "+dataDirectory);
        }
        
        if(formats.size() > 1) {
            // If multiple formats are found it may be that we have a combination of .ms2 and .cms2 files in the 
            // same directory.  In that case, we don't throw an exception.
            if(!isMs2Format(formats))
                throw new UploadServiceFactoryException("Multiple spectrum data file formats found in directory: "+dataDirectory);
        }
        
        RunFileFormat format = formats.iterator().next();
        if(format == RunFileFormat.MS2 || format == RunFileFormat.CMS2) {
            SpectrumDataUploadService service = new MS2DataUploadService2();
            service.setDirectory(dataDirectory);
            return service;
        }
        else if(format == RunFileFormat.MZXML) {
            SpectrumDataUploadService service = new MzXmlDataUploadService();
            service.setDirectory(dataDirectory);
            return service;
        }
        else {
            throw new UploadServiceFactoryException("We do not currently have support for the format: "+format.toString());
        }
    }
    
    private boolean isMs2Format(Set<RunFileFormat> formats) {
        for(RunFileFormat fmt: formats) {
            if(fmt != RunFileFormat.MS2 && fmt != RunFileFormat.CMS2)
                return false;
        }
        return true;
    }
    
    // ------------------------------------------------------------------------------------------------------
    // SEARCH Data Upload service
    // ------------------------------------------------------------------------------------------------------
    public SearchDataUploadService getSearchDataUploadService(String dataDirectory) throws UploadServiceFactoryException {
        
        if(dataDirectory == null) {
            throw new UploadServiceFactoryException("dataDirectory is null");
        }
        
        File dir = new File(dataDirectory);
        if(!dir.exists()) {
            throw new UploadServiceFactoryException("dataDirectory does not exist: "+dataDirectory);
        }
        
        if(!dir.isDirectory()) {
            throw new UploadServiceFactoryException(dataDirectory+" is not a directory");
        }
        
        File[] files = dir.listFiles();
        Set<SearchFileFormat> formats = new HashSet<SearchFileFormat>();
        Set<String> filenames = new HashSet<String>();
        for (int i = 0; i < files.length; i++) {
            if(files[i].isDirectory())
                continue;
            String fileName = files[i].getName();
            
            String ext = null;
            if(fileName.toLowerCase().endsWith("pep.xml"))
                ext = "pep.xml";
            else {
                int idx = fileName.lastIndexOf(".");
                if(idx == -1)   continue;

                ext = fileName.substring(idx);
            }
           
            SearchFileFormat format = SearchFileFormat.forFileExtension(ext);
            if(format == SearchFileFormat.UNKNOWN) 
                continue;
            
            filenames.add(fileName);
            formats.add(format);
        }
        
        if(formats.size() == 0) {
            throw new UploadServiceFactoryException("No valid search data file format found in directory: "+dataDirectory);
        }
        
        if(formats.size() > 1) {
            throw new UploadServiceFactoryException("Multiple search data file formats found in directory: "+dataDirectory);
        }
        
        SearchFileFormat format = formats.iterator().next();
        if(format ==  SearchFileFormat.SQT) {
            // we know that we have SQT files in this directory
            // now figure out which program generated these files.
            SearchFileFormat sqtFormat = getSqtType(dataDirectory, filenames);
            if (sqtFormat == SearchFileFormat.SQT_SEQ) {
                SearchDataUploadService service = new SequestSQTDataUploadService2(sqtFormat);
                service.setDirectory(dataDirectory);
                return service;
            }
            else if (sqtFormat == SearchFileFormat.SQT_PLUCID) {
                SearchDataUploadService service = new ProlucidSQTDataUploadService();
                service.setDirectory(dataDirectory);
                return service;
            }
            else {
                throw new UploadServiceFactoryException("We do not currently have support for the format: "+format.toString());
            }
        }
        else if(format == SearchFileFormat.PEPXML) {
            // we know that we have pepxml files in this directory
            // now we need to figure out which program (Sequest, Mascot etc.) results these files contain.
            SearchFileFormat pepxmlFormat = getPepxmlType(dataDirectory, filenames);
            
            if(pepxmlFormat == SearchFileFormat.PEPXML_SEQ) {
                SearchDataUploadService service = new PepXmlSequestDataUploadService();
                service.setDirectory(dataDirectory);
                return service;
            }
            else if(pepxmlFormat == SearchFileFormat.PEPXML_MASCOT) {
                SearchDataUploadService service = new PepXmlMascotDataUploadService();
                service.setDirectory(dataDirectory);
                return service;
            }
            else {
                throw new UploadServiceFactoryException("We do not currently have support for the format: "+format.toString());
            }
        }
        else {
            throw new UploadServiceFactoryException("We do not currently have support for the format: "+format.toString());
        }
    }
    
    // ------------------------------------------------------------------------------------------------------
    // ANALYSIS Data Upload service
    // ------------------------------------------------------------------------------------------------------
    public AnalysisDataUploadService getAnalysisDataUploadService(String dataDirectory) throws UploadServiceFactoryException {
        
        if(dataDirectory == null) {
            throw new UploadServiceFactoryException("dataDirectory is null");
        }
        
        File dir = new File(dataDirectory);
        if(!dir.exists()) {
            throw new UploadServiceFactoryException("dataDirectory does not exist: "+dataDirectory);
        }
        
        if(!dir.isDirectory()) {
            throw new UploadServiceFactoryException(dataDirectory+" is not a directory");
        }
        
        File[] files = dir.listFiles();
        Set<SearchFileFormat> formats = new HashSet<SearchFileFormat>();
        Set<String> filenames = new HashSet<String>();
        
        for (int i = 0; i < files.length; i++) {
            if(files[i].isDirectory())
                continue;
            String fileName = files[i].getName();
            
            String ext = null;
            if(fileName.toLowerCase().endsWith("pep.xml"))
                ext = "pep.xml";
            else {
                int idx = fileName.lastIndexOf(".");
                if(idx == -1)   continue;

                ext = fileName.substring(idx);
            }
            
            SearchFileFormat format = SearchFileFormat.forFileExtension(ext);
            if(format == SearchFileFormat.UNKNOWN) 
                continue;
            
            filenames.add(fileName);
            formats.add(format);
        }
        
        if(formats.size() == 0) {
            throw new UploadServiceFactoryException("No valid search data file format found in directory: "+dataDirectory);
        }
        
        if(formats.size() > 1) {
            throw new UploadServiceFactoryException("Multiple search data file formats found in directory: "+dataDirectory);
        }
        
        SearchFileFormat format = formats.iterator().next();
        if(format ==  SearchFileFormat.SQT) {
            // we know that we have SQT files in this directory
            // now figure out which program generated these files.
            SearchFileFormat sqtFormat = getSqtType(dataDirectory, filenames);
            if (sqtFormat == SearchFileFormat.SQT_PERC) {
                AnalysisDataUploadService service = new PercolatorSQTDataUploadService();
                service.setDirectory(dataDirectory);
                return service;
            }
            else {
                throw new UploadServiceFactoryException("We do not currently have support for the format: "+format.toString());
            }
        }
        else if(format == SearchFileFormat.PEPXML) {
            AnalysisDataUploadService service = new PepxmlAnalysisDataUploadService();
            service.setDirectory(dataDirectory);
            return service;
        }
        else {
            throw new UploadServiceFactoryException("We do not currently have support for the format: "+format.toString());
        }
    }
    
    private SearchFileFormat getSqtType(String fileDirectory, Set<String> filenames) throws UploadServiceFactoryException {
        
        SearchFileFormat sqtType = null;
        
        // make sure all files are of the same type
        for (String file: filenames) {
            String sqtFile = fileDirectory+File.separator+file;
            // first make sure the file exists
            if (!(new File(sqtFile).exists()))
                continue;
            SearchFileFormat myType = SQTFileReader.getSearchFileType(sqtFile);
            
            // For now we support only sequest, ee-normalized sequest and ProLuCID sqt files. 
            if (SearchFileFormat.SQT_SEQ != myType && 
//                    SearchFileFormat.SQT_NSEQ != myType &&
                    SearchFileFormat.SQT_PLUCID != myType &&
                    SearchFileFormat.SQT_PERC != myType) {
                throw new UploadServiceFactoryException("We do not currently have support for the SQT format: "+myType);
            }

            if (sqtType == null) sqtType = myType;
            if (myType != sqtType) {
                String errMsg = "Multiple file formats found in directory: "+fileDirectory+"\n"+
                "\tFound: "+sqtType.getFormatType()+" and "+myType.getFormatType();
                throw new UploadServiceFactoryException(errMsg);
            }
        }
        if (sqtType == null) {
            throw new UploadServiceFactoryException("No valid SQT file format found");
        }
        
        return sqtType;
    }
    
    private SearchFileFormat getPepxmlType(String fileDirectory, Set<String> filenames) throws UploadServiceFactoryException {
    
        SearchFileFormat pepxmlType = null;
        
        // make sure all files are of the same type
        for (String file: filenames) {
            String pepxmlFile = fileDirectory+File.separator+file;
            // first make sure the file exists
            if (!(new File(pepxmlFile).exists()))
                continue;
            if(!file.toLowerCase().endsWith("pep.xml"))
                continue;
            if(file.startsWith("interact")) 
                continue;
            
            SearchFileFormat myType = SQTFileReader.getSearchFileType(pepxmlFile);
            
            // For now we support only SEQUEST and MASCOT pepxml files. 
            if (SearchFileFormat.PEPXML_SEQ!= myType && 
                SearchFileFormat.PEPXML_MASCOT != myType) {
                throw new UploadServiceFactoryException("We do not currently have support for the PEPXML format: "+myType);
            }

            if (pepxmlType == null) pepxmlType = myType;
            if (myType != pepxmlType) {
                String errMsg = "Multiple file formats found in directory: "+fileDirectory+"\n"+
                "\tFound: "+pepxmlType.getFormatType()+" and "+myType.getFormatType();
                throw new UploadServiceFactoryException(errMsg);
            }
        }
        if (pepxmlType == null) {
            throw new UploadServiceFactoryException("No valid PEPXML file format found");
        }
        
        return pepxmlType;
    }
    
    // ------------------------------------------------------------------------------------------------------
    // PROTEIN INFERENCE Data Upload service
    // ------------------------------------------------------------------------------------------------------
    public ProtinferUploadService getProtinferUploadService(String dataDirectory) throws UploadServiceFactoryException {
        
        if(dataDirectory == null) {
            throw new UploadServiceFactoryException("dataDirectory is null");
        }
        
        File dir = new File(dataDirectory);
        if(!dir.exists()) {
            throw new UploadServiceFactoryException("dataDirectory does not exist: "+dataDirectory);
        }
        
        if(!dir.isDirectory()) {
            throw new UploadServiceFactoryException(dataDirectory+" is not a directory");
        }
        
        File[] files = dir.listFiles();
        Set<SearchFileFormat> formats = new HashSet<SearchFileFormat>();
        Set<String> filenames = new HashSet<String>();
        for (int i = 0; i < files.length; i++) {
            if(files[i].isDirectory())
                continue;
            String fileName = files[i].getName();
            
            String ext = null;
            if(fileName.toLowerCase().endsWith("prot.xml"))
                ext = "prot.xml";
            else {
                int idx = fileName.lastIndexOf(".");
                if(idx == -1)   continue;

                ext = fileName.substring(idx);
            }
           
            SearchFileFormat format = SearchFileFormat.forFileExtension(ext);
            if(format == SearchFileFormat.UNKNOWN) 
                continue;
            
            filenames.add(fileName);
            formats.add(format);
        }
        
        if(formats.size() == 0) {
            throw new UploadServiceFactoryException("No valid protein inference file format found in directory: "+dataDirectory);
        }
        
        if(formats.size() > 1) {
            throw new UploadServiceFactoryException("Multiple protein inference file formats found in directory: "+dataDirectory);
        }
        
        SearchFileFormat format = formats.iterator().next();
        if(format == SearchFileFormat.PROTXML) {
            ProtinferUploadService service = new ProtxmlDataUploadService();
            service.setDirectory(dataDirectory);
            return service;
        }
        else {
            throw new UploadServiceFactoryException("We do not currently have support for the format: "+format.toString());
        }
    }
}
