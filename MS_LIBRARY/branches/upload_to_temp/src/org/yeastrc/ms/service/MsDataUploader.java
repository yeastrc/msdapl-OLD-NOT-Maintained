package org.yeastrc.ms.service;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.yeastrc.ms.domain.general.MsExperiment;
import org.yeastrc.ms.domain.search.MsSearch;
import org.yeastrc.ms.domain.search.Program;
import org.yeastrc.ms.service.UploadException.ERROR_CODE;
import org.yeastrc.ms.upload.dao.UploadDAOFactory;
import org.yeastrc.ms.upload.dao.analysis.MsSearchAnalysisUploadDAO;
import org.yeastrc.ms.upload.dao.general.MsExperimentUploadDAO;
import org.yeastrc.ms.upload.dao.search.MsSearchUploadDAO;

public class MsDataUploader {

    private static final Logger log = Logger.getLogger(MsDataUploader.class);
    
    private int uploadedAnalysisId;
    private int uploadedSearchId;
    private int uploadedExptId;
    private List<UploadException> uploadExceptionList = new ArrayList<UploadException>();
    
    private String comments;
    private String remoteServer;
    private String spectrumDataDirectory;
    private String remoteSpectrumDataDirectory;
    private String searchDirectory;
    private String remoteSearchDataDirectory;
    private java.util.Date searchDate;
    private String analysisDirectory;
    private String protinferDirectory;
    private boolean doScanChargeMassCheck = false; // For MacCoss lab data
    
    private boolean uploadSearch = false;
    private boolean uploadAnalysis = false;
    private boolean uploadProtinfer = false;
    
    
    private void resetUploader() {
        uploadExceptionList.clear();
        uploadedSearchId = 0;
        uploadedExptId = 0;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setRemoteServer(String remoteServer) {
        this.remoteServer = remoteServer;
    }

    public void setSpectrumDataDirectory(String rawDataDirectory) {
        this.spectrumDataDirectory = rawDataDirectory;
    }

    public void setRemoteSpectrumDataDirectory(String remoteRawDataDirectory) {
        this.remoteSpectrumDataDirectory = remoteRawDataDirectory;
    }

    public void setSearchDirectory(String searchDirectory) {
        this.searchDirectory = searchDirectory;
        if(searchDirectory != null)
            this.uploadSearch = true;
    }

    public void setRemoteSearchDataDirectory(String remoteSearchDataDirectory) {
        this.remoteSearchDataDirectory = remoteSearchDataDirectory;
    }

    public void setSearchDate(java.util.Date searchDate) {
        this.searchDate = searchDate;
    }

    public void setAnalysisDirectory(String analysisDirectory) {
        this.analysisDirectory = analysisDirectory;
        if(analysisDirectory != null)
            this.uploadAnalysis = true;
    }
    
    public void setProtinferDirectory(String protinferDirectory) {
        this.protinferDirectory = protinferDirectory;
        if(protinferDirectory != null)
            this.uploadProtinfer = true;
    }
    
    public void checkResultChargeMass(boolean doScanChargeMassCheck) {
        this.doScanChargeMassCheck = doScanChargeMassCheck;
    }
    
    public List<UploadException> getUploadExceptionList() {
        return this.uploadExceptionList;
    }
    
    public String getUploadWarnings() {
        StringBuilder buf = new StringBuilder();
        for (UploadException e: uploadExceptionList) {
            buf.append(e.getMessage()+"\n");
        }
        return buf.toString();
    }
    
    public int getUploadedSearchId() {
        return this.uploadedSearchId;
    }
    
    public int getUploadedExperimentId() {
        return this.uploadedExptId;
    }
    
    /**
     * @param remoteServer
     * @param remoteDirectory
     * @param fileDirectory
     * @return database id for experiment if it was uploaded successfully, 0 otherwise
     * @throws UploadException 
     */
    public void uploadData() {

        resetUploader();
        
        log.info("INITIALIZING EXPERIMENT UPLOAD"+
                "\n\tTime: "+(new Date().toString()));
        
        
        // ----- INITIALIZE THE EXPERIMENT UPLOADER
        MsExperimentUploader exptUploader = null;
        try {
            exptUploader = initializeExperimentUploader();
        }
        catch (UploadException e) {
            uploadExceptionList.add(e);
            log.error(e.getMessage(), e);
            return;
        }
        
        // ----- CHECKS BEFORE BEGINNING UPLOAD -----
        log.info("Starting pre-upload checks..");
        if(!exptUploader.preUploadCheckPassed()) {
            UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
            ex.appendErrorMessage(exptUploader.getPreUploadCheckMsg());
            uploadExceptionList.add(ex);
            log.error(ex.getMessage(), ex);
            return;
        }
        log.info(exptUploader.getPreUploadCheckMsg());
        
        
        // ----- NOW WE CAN BEGIN THE UPLOAD -----
        logBeginExperimentUpload();
        long start = System.currentTimeMillis();
        
        try {
            this.uploadedExptId = exptUploader.uploadSpectrumData();
        }
        catch (UploadException ex) {
            uploadExceptionList.add(ex);
            log.error(ex.getMessage(), ex);
            log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
            return;
        }
        
        // ----- UPLOAD SEARCH DATA
        if(uploadSearch) {
            
            // disable keys
            try {
                disableSearchTableKeys();
            }
            catch (SQLException e) {
                UploadException ex = new UploadException(ERROR_CODE.ERROR_SQL_DISABLE_KEYS, e);
                uploadExceptionList.add(ex);
                log.error(ex.getMessage(), ex);
                log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
                return;
            }
            
            try {
                this.uploadedSearchId = exptUploader.uploadSearchData(this.uploadedExptId);
            }
            catch (UploadException ex) {
                uploadExceptionList.add(ex);
                log.error(ex.getMessage(), ex);
                log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
                
                // enable keys
                try {
                    enableSearchTableKeys();
                }
                catch(SQLException e){log.error("Error enabling keys");}
                return;
            }
            
            // enable keys
            try {
                enableSearchTableKeys();
            }
            catch (SQLException e) {
                UploadException ex = new UploadException(ERROR_CODE.ERROR_SQL_ENABLE_KEYS, e);
                uploadExceptionList.add(ex);
                log.error(ex.getMessage(), ex);
                log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
                return;
            }
        }
        
        // ----- UPLOAD ANALYSIS DATA
        if(uploadAnalysis) {
            
            try {
                this.uploadedAnalysisId = exptUploader.uploadAnalysisData(this.uploadedSearchId);
            }
            catch (UploadException ex) {
                uploadExceptionList.add(ex);
                log.error(ex.getMessage(), ex);
                log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
                return;
            }
        }
        
        // ----- UPLOAD PROTEIN INFERENCE DATA
        if(uploadProtinfer) {
            try {
                exptUploader.uploadProtinferData(this.uploadedSearchId, this.uploadedAnalysisId);
            }
            catch (UploadException ex) {
                uploadExceptionList.add(ex);
                log.error(ex.getMessage(), ex);
                log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
                return;
            }
        }
        
        long end = System.currentTimeMillis();
        logEndExperimentUpload(exptUploader, start, end);
        
    }
    
    private void disableSearchTableKeys() throws SQLException {
        
        // disable keys on msRunSearchResult table
        log.info("Disabling keys on msRunSearchResult table");
        UploadDAOFactory.getInstance().getMsSearchResultDAO().disableKeys();
        
        // disable keys on SQTSearchResult
        log.info("Disabling keys on SQTSearchResult table");
        UploadDAOFactory.getInstance().getSequestResultDAO().disableKeys();
        
        // disable keys on SQTSpectrumData
        log.info("Disabling keys on SQTSpectrumData table");
        UploadDAOFactory.getInstance().getSqtSpectrumDAO().disableKeys();
        
        // disable keys on msProteinMatch
        log.info("Disabling keys on msProteinMatch table");
        UploadDAOFactory.getInstance().getMsProteinMatchDAO().disableKeys();
        
        log.info("Disabled keys");
    }
    
    private void enableSearchTableKeys() throws SQLException {
        
        // enable keys on msRunSearchResult table
        log.info("Enabling keys on msRunSearchResult table");
        UploadDAOFactory.getInstance().getMsSearchResultDAO().enableKeys();
        
        // enable keys on SQTSearchResult
        log.info("Enabling keys on SQTSearchResult table");
        UploadDAOFactory.getInstance().getSequestResultDAO().enableKeys();
        
        // enable keys on SQTSpectrumData
        log.info("Enabling keys on SQTSpectrumData table");
        UploadDAOFactory.getInstance().getSqtSpectrumDAO().enableKeys();
        
        // enable keys on msProteinMatch
        log.info("Enabling keys on msProteinMatch table");
        UploadDAOFactory.getInstance().getMsProteinMatchDAO().enableKeys();
        
        log.info("Enabled keys");
    }

    private MsExperimentUploader initializeExperimentUploader() throws UploadException  {
        
        MsExperimentUploader exptUploader = new MsExperimentUploader();
        exptUploader.setDirectory(spectrumDataDirectory);
        exptUploader.setRemoteDirectory(remoteSpectrumDataDirectory);
        exptUploader.setRemoteServer(remoteServer);
        exptUploader.setComments(comments);
        
        // Get the spectrum data uploader
        log.info("Initializing SpectrumDataUploadService");
        SpectrumDataUploadService rdus = getSpectrumDataUploader(spectrumDataDirectory, remoteSpectrumDataDirectory);
        exptUploader.setSpectrumDataUploader(rdus);
        
        // We cannot upload analysis data without uploading search data first.
        if(uploadAnalysis && !uploadSearch) {
            UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
            ex.appendErrorMessage("Cannot upload analysis results without serach results");
            throw ex;
        }
        
        // Get the search data uploader
        if(uploadSearch) {
            log.info("Initializing SearchDataUploadService");
            SearchDataUploadService sdus = getSearchDataUploader(searchDirectory, 
                    remoteServer, remoteSearchDataDirectory, searchDate);
            exptUploader.setSearchDataUploader(sdus);
            //sdus.setRawDataFileNames(rdus.getFileNames(), rdus.getFileFormat());
        }
        // Get the analysis data uploader
        if(uploadAnalysis) {
            log.info("Initializing AnalysisDataUploadService");
            AnalysisDataUploadService adus = getAnalysisDataUploader(analysisDirectory);
            exptUploader.setAnalysisDataUploader(adus);
        }
        // Get the protein inference uploader
        if(uploadProtinfer) {
            log.info("Initializing ProtinferUploadService");
            ProtinferUploadService pidus = getProtinferUploader(protinferDirectory);
            exptUploader.setProtinferUploader(pidus);
        }
        
        return exptUploader;
    }

    private ProtinferUploadService getProtinferUploader(String dataDirectory) throws UploadException {
        ProtinferUploadService pidus = null;
        try {
            pidus = UploadServiceFactory.instance().getProtinferUploadService(dataDirectory);
        }
        catch (UploadServiceFactoryException e1) {
            UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
            ex.appendErrorMessage("Error getting ProtinferUploadService: "+e1.getMessage());
            throw ex;
        }
        return pidus;
    }
    
    private AnalysisDataUploadService getAnalysisDataUploader(String dataDirectory) throws UploadException {
        AnalysisDataUploadService adus = null;
        try {
            adus = UploadServiceFactory.instance().getAnalysisDataUploadService(dataDirectory);
            adus.setSearchProgram(Program.SEQUEST);
        }
        catch (UploadServiceFactoryException e1) {
            UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
            ex.appendErrorMessage("Error getting AnalysisDataUploadService: "+e1.getMessage());
            throw ex;
        }
        return adus;
    }
    
    private SearchDataUploadService getSearchDataUploader(String dataDirectory,
            String remoteServer, String remoteDirectory, Date searchDate) throws UploadException {
        SearchDataUploadService sdus = null;
        try {
            sdus = UploadServiceFactory.instance().getSearchDataUploadService(dataDirectory);
            sdus.setRemoteServer(remoteServer);
            sdus.setRemoteDirectory(remoteDirectory);
            sdus.setSearchDate(searchDate);
            if(doScanChargeMassCheck)
                sdus.checkResultChargeMass(doScanChargeMassCheck);
        }
        catch (UploadServiceFactoryException e1) {
            UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
            ex.appendErrorMessage("Error getting SearchDataUploadService: "+e1.getMessage());
            throw ex;
        }
        return sdus;
    }

    private SpectrumDataUploadService getSpectrumDataUploader(String dataDirectory, String remoteDirectory) throws UploadException {
        
        SpectrumDataUploadService rdus = null;
        try {
            rdus = UploadServiceFactory.instance().getSpectrumDataUploadService(dataDirectory);
            rdus.setRemoteDirectory(remoteDirectory);
        }
        catch(UploadServiceFactoryException e1) {
            UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
            ex.appendErrorMessage("Error getting SpectrumDataUploadService: "+e1.getMessage());
            throw ex;
        }
        return rdus;
    }
    
    public void uploadData(int experimentId) {
        
        resetUploader();
        MsExperimentUploadDAO exptDao = UploadDAOFactory.getInstance().getMsExperimentDAO();
        MsExperiment expt = exptDao.loadExperiment(experimentId);
        if (expt == null) {
            UploadException ex = new UploadException(ERROR_CODE.EXPT_NOT_FOUND);
            ex.appendErrorMessage("Experiment ID: "+experimentId+" does not exist in the database.");
            ex.appendErrorMessage("!!!EXPERIMENT WILL NOT BE UPLOADED!!!");
            uploadExceptionList.add(ex);
            log.error(ex.getMessage(), ex);
            return;
        }
        this.remoteServer = expt.getServerAddress();
        this.uploadedExptId = experimentId;
        
        
        log.info("INITIALIZING EXPERIMENT UPLOAD"+
                "\n\tTime: "+(new Date().toString()));
        
        
        // ----- INITIALIZE THE EXPERIMENT UPLOADER
        MsExperimentUploader exptUploader = null;
        try {
            exptUploader = initializeExperimentUploader();
        }
        catch (UploadException e) {
            uploadExceptionList.add(e);
            log.error(e.getMessage(), e);
            return;
        }
        
        // ----- CHECKS BEFORE BEGINNING UPLOAD -----
        log.info("Starting pre-upload checks..");
        if(!exptUploader.preUploadCheckPassed()) {
            UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
            ex.appendErrorMessage(exptUploader.getPreUploadCheckMsg());
            uploadExceptionList.add(ex);
            log.error(ex.getMessage(), ex);
            return;
        }
        log.info(exptUploader.getPreUploadCheckMsg());
        
        
        // ----- NOW WE CAN BEGIN THE UPLOAD -----
        logBeginExperimentUpload();
        long start = System.currentTimeMillis();
        
        // ----- UPDATE THE LAST UPDATE DATE FOR THE EXPERIMENT
        updateLastUpdateDate(experimentId);
        
        // ----- UPLOAD SCAN DATA
        try {
            exptUploader.uploadSpectrumData(experimentId);
        }
        catch (UploadException ex) {
            uploadExceptionList.add(ex);
            log.error(ex.getMessage(), ex);
            log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
            return;
        }
        
        // ----- UPLOAD SEARCH DATA
        if(uploadSearch) {
            // If the search is already uploaded, don't re-upload it.
            int searchId = 0;
            try {
                // TODO We are looking for a Sequest search ID
                // An experiment can have multiple searches, even multiple Sequest searches. 
                // Need to think about how to handle this.
                searchId = getExperimentSequestSearchId(this.uploadedExptId);
            }
            catch (Exception e) {
                UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
                ex.appendErrorMessage(e.getMessage());
                uploadExceptionList.add(ex);
                log.error(ex.getMessage());
                log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
                return;
            }
            if(searchId == 0) {
                
                // disable keys
                try {
                    disableSearchTableKeys();
                }
                catch (SQLException e) {
                    UploadException ex = new UploadException(ERROR_CODE.ERROR_SQL_DISABLE_KEYS, e);
                    uploadExceptionList.add(ex);
                    log.error(ex.getMessage(), ex);
                    log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
                    return;
                }
                    
                try {
                    this.uploadedSearchId = exptUploader.uploadSearchData(this.uploadedExptId);
                }
                catch (UploadException ex) {
                    uploadExceptionList.add(ex);
                    log.error(ex.getMessage(), ex);
                    log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
                    
                    // enable keys
                    try {
                        enableSearchTableKeys();
                    }
                    catch(SQLException e){log.error("Error enabling keys");}
                    
                    return;
                }
                
                // enable keys
                try {
                    enableSearchTableKeys();
                }
                catch (SQLException e) {
                    UploadException ex = new UploadException(ERROR_CODE.ERROR_SQL_ENABLE_KEYS, e);
                    uploadExceptionList.add(ex);
                    log.error(ex.getMessage(), ex);
                    log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
                    return;
                }
            }
            else {
                this.uploadedSearchId = searchId;
                log.info("Search was uploaded previously. SearchID: "+uploadedSearchId);
            }
        }
        
        // ----- UPLOAD ANALYSIS DATA
        if(uploadAnalysis) {
            // If the search analysis is already uploaded, don't re-upload it.
            int searchAnalysisID = 0;
            // TODO We are looking for a Analysis id for our search ID. 
            // However, we can have multiple entries in the msSearchAnalysis for each searchId
            // Need to think about how to handle this.
            try {
                searchAnalysisID = getSearchAnalysisId(uploadedSearchId);
            }
            catch (Exception e) {
                UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
                ex.appendErrorMessage(e.getMessage());
                uploadExceptionList.add(ex);
                log.error(ex.getMessage());
                log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
            }
            if(searchAnalysisID == 0) {
                try {
                    searchAnalysisID = exptUploader.uploadAnalysisData(this.uploadedSearchId);
                }
                catch (UploadException ex) {
                    uploadExceptionList.add(ex);
                    log.error(ex.getMessage(), ex);
                    log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
                    return;
                }
            }
            else {
                log.info("Search Analysis was uploaded previously. SearchAnalysisID: "+searchAnalysisID);
            }
        }
        
        // ----- UPLOAD PROTEIN INFERENCE DATA
        if(uploadProtinfer) {
            try {
                exptUploader.uploadProtinferData(this.uploadedSearchId, this.uploadedAnalysisId);
            }
            catch (UploadException ex) {
                uploadExceptionList.add(ex);
                log.error(ex.getMessage(), ex);
                log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
                return;
            }
        }
        
        long end = System.currentTimeMillis();
        logEndExperimentUpload(exptUploader, start, end);
       
    }

    private int getSearchAnalysisId(int searchId) throws Exception {
        MsSearchAnalysisUploadDAO analysisDao = UploadDAOFactory.getInstance().getMsSearchAnalysisDAO();
        List<Integer> analysisIds = analysisDao.getAnalysisIdsForSearch(searchId);
        if(analysisIds.size() > 1) {
            throw new Exception("Multiple analysis ids for found for searchID: "+searchId);
        }
        if(analysisIds.size() == 0) return 0;
        return analysisIds.get(0);
    }

    private int getExperimentSequestSearchId(int uploadedExptId2) throws Exception {
       
        MsSearchUploadDAO searchDao = UploadDAOFactory.getInstance().getMsSearchDAO();
        List<Integer> searchIds = searchDao.getSearchIdsForExperiment(uploadedExptId2);
        
        if(searchIds.size() == 0)
            return 0;
        
        int sequestSearchId = 0;
        for(Integer id: searchIds) {
            MsSearch search = searchDao.loadSearch(id);
            if(search.getSearchProgram() == Program.SEQUEST) {
                if(sequestSearchId != 0)
                    throw new Exception("Multiple sequest search ids found for experimentID: "+uploadedExptId2);
                sequestSearchId = id;
            }
        }
        return sequestSearchId;
    }

    private void updateLastUpdateDate(int experimentId) {
        MsExperimentUploadDAO experimentDao = UploadDAOFactory.getInstance().getMsExperimentDAO();
        experimentDao.updateLastUpdateDate(experimentId);
        
    }

//    private void deleteOldSearchesForExperiment(int experimentId) {
//        MsSearchDAO searchDao = UploadDAOFactory.getInstance().getMsSearchDAO();
//        List<Integer> searchIds = searchDao.getSearchIdsForExperiment(experimentId);
//        for (Integer id: searchIds) {
//            if (id != null)
//                searchDao.deleteSearch(id);
//        }
//    }

    
    private void logEndExperimentUpload(MsExperimentUploader uploader, long start, long end) {
        log.info("END EXPERIMENT UPLOAD: "+((end - start)/(1000L))+"seconds"+
                "\n\tTime: "+(new Date().toString())+"\n"+
                uploader.getUploadSummary());
    }

    private void logBeginExperimentUpload() {
        
        StringBuilder msg = new StringBuilder();
        msg.append("BEGIN EXPERIMENT UPLOAD");
        msg.append("\n\tRemote server: "+remoteServer);
        msg.append("\n\tSPECTRUM DATA ");
        msg.append("\n\t\t Directory: "+spectrumDataDirectory);
        msg.append("\n\t\t Remote Directory: "+remoteSpectrumDataDirectory);
        if(uploadSearch) {
            msg.append("\n\tSEARCH DATA");
            msg.append("\n\t\t Directory: "+searchDirectory);
            msg.append("\n\t\t Remote Directory: "+remoteSearchDataDirectory);
        }
        if(uploadAnalysis) {
            msg.append("\n\tANALYSIS DATA");
            msg.append("\n\t\t Directory: "+analysisDirectory);
        }
        if(uploadProtinfer) {
            msg.append("\n\tPROTEIN INFERENCE");
            msg.append("\n\t\t Directory: "+protinferDirectory);
        }
        msg.append("\n\tTime: "+(new Date().toString()));
        log.info(msg.toString());
    }

   
   
    
//    // upload percolator sqt files
//    private int uploadPercolatorSearch(Set<String> filenames, Map<String, Integer> runIdMap, final Date searchDate) {
//        
//        // first upload the sqt files to populate the core search tables
//        SearchParamsDataProvider paramsProvider = new SequestParamsParser();
//        PeptideResultBuilder peptbuilder = SequestResultPeptideBuilder.instance();
//        
//        BaseSQTDataUploadService service = new BaseSQTDataUploadService(paramsProvider, peptbuilder, Program.PERCOLATOR);
//        if(isMacCossData) 
//            service.doScanChargeMassCheck(true);
//        
//        int searchId = service.uploadSearch(uploadedExptId, uploadDirectory, filenames, runIdMap, 
//                        remoteServer, remoteDirectory, new java.sql.Date(searchDate.getTime()));
//        
//        this.uploadExceptionList.addAll(service.getUploadExceptionList());
//        this.numSearchesToUpload = service.getNumSearchesToUpload();
//        this.numSearchesUploaded = service.getNumSearchesUploaded();
//        
//        // if the search information could not be uploaded don't go any further
//        if(uploadExceptionList.size() > 0) {
//            return searchId;
//        }
//        
//        // now upload the Percolator search results
//        PercolatorSQTDataUploadService percService = new PercolatorSQTDataUploadService();
//        MsRunSearchDAO runSearchDao = DAOFactory.instance().getMsRunSearchDAO();
//        List<Integer> runSearchIds = runSearchDao.loadRunSearchIdsForSearch(searchId);
//        Map<String, Integer> runSearchIdMap = new HashMap<String, Integer>(runSearchIds.size());
//        for(Integer id: runSearchIds) {
//            String filename = runSearchDao.loadFilenameForRunSearch(id);
//            runSearchIdMap.put(filename, id);
//        }
//        percService.uploadPostSearchAnalysis(searchId, Program.SEQUEST, uploadDirectory, filenames, 
//                runSearchIdMap, remoteDirectory);
//        
//        this.uploadExceptionList.addAll(percService.getUploadExceptionList());
//        
//        return searchId;
//    }
    
    public static void main(String[] args) throws UploadException {
        long start = System.currentTimeMillis();

        
//        for(int i = 0; i < 10; i++) {
        String directory = args[0];
        
        if(directory == null || directory.length() == 0 || !(new File(directory).exists()))
            System.out.println("Invalid directory: "+directory);
        
        boolean maccossData = Boolean.parseBoolean(args[1]);
        
        System.out.println("Directory: "+directory+"; Maccoss Data: "+maccossData);
        
        MsDataUploader uploader = new MsDataUploader();
        uploader.setRemoteServer("local");
        uploader.setSpectrumDataDirectory(directory);
        uploader.setSearchDirectory(directory);
        uploader.setProtinferDirectory(directory);
        
        uploader.setRemoteSpectrumDataDirectory(directory);
        uploader.setRemoteSearchDataDirectory(directory);
        uploader.setSearchDate(new Date());
        uploader.checkResultChargeMass(maccossData);
        
        uploader.uploadData(32);
//        uploader.uploadData();
//        }
        long end = System.currentTimeMillis();
        log.info("TOTAL TIME: "+((end - start)/(1000L))+"seconds.");
    }
}
