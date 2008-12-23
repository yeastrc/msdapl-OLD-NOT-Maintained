package org.yeastrc.www.proteinfer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.www.proteinfer.MsSearchSummary.RunSearchFile;
import org.yeastrc.www.proteinfer.ProgramParameters.Param;


import edu.uwpr.protinfer.ProteinInferenceProgram;
import edu.uwpr.protinfer.database.dao.ProteinferDAOFactory;
import edu.uwpr.protinfer.database.dao.ibatis.ProteinferInputDAO;
import edu.uwpr.protinfer.database.dao.ibatis.ProteinferRunDAO;
import edu.uwpr.protinfer.database.dao.idpicker.ibatis.IdPickerFilterDAO;
import edu.uwpr.protinfer.database.dto.ProteinferInput;
import edu.uwpr.protinfer.database.dto.idpicker.IdPickerFilter;

public class ProteinferJobSaver {

    private static final Logger log = Logger.getLogger(ProteinferJobSaver.class);
    
    private static final ProteinferDAOFactory fact = ProteinferDAOFactory.instance();
    private static final ProteinferRunDAO pinferDao = fact.getProteinferRunDao();
    private static final ProteinferInputDAO pinferInputDao = fact.getProteinferInputDao();
    private static final IdPickerFilterDAO pinferFilterDao = fact.getProteinferFilterDao();
    
    
    private static final ProteinferJobSaver saver = new ProteinferJobSaver();
    
    private ProteinferJobSaver() {}
    
    public static ProteinferJobSaver instance() {
        return saver;
    }
    
    public void saveJobToDatabase(int submitterId, MsSearchSummary searchSummary, ProgramParameters params) throws Exception {
        
        // create an entry in the main protein inference table and get an id 
        // for this protein inference run
        ProteinInferenceProgram program = ProteinInferenceProgram.getProgramForName(params.getProgramName());
        if(program == null) {
            log.error("Could not find protein inference program with name: "+params.getProgramName());
            throw new Exception("Could not find protein inference program with name: "+params.getProgramName());
        }
        int pinferId = pinferDao.saveNewProteinferRun(program);
        if(pinferId <= 0) {
            log.error("Error saving a new entry for Protein Inference");
            throw new Exception("Error saving a new entry for Protein Inference");
        }
        
        // save the input file information
        for(RunSearchFile runSearch: searchSummary.getFiles()) {
            if(!runSearch.getIsSelected()) continue; // if this file was not selected don't save it
            ProteinferInput input = new ProteinferInput();
            input.setProteinferId(pinferId);
            input.setRunSearchId(runSearch.getRunSearchId());
            pinferInputDao.saveProteinferInput(input);
        }
        
        // save the filters
        if(program == ProteinInferenceProgram.IDPICKER ||
           program == ProteinInferenceProgram.IDPICKER_PERC) {
            saveIdPickerFilters(pinferId, params);
        }
        
        // create and entry in tblJobs
        int jobId = createEntryInTblJobs(submitterId);
        
        // finally save info in tblProteinferJobs;
        createEntryInTblProteinferJobs(jobId, pinferId);
    }

    private void saveIdPickerFilters(int pinferId, ProgramParameters params) {
        for(Param param: params.getParamList()) {
            IdPickerFilter filter = new IdPickerFilter();
            filter.setProteinferId(pinferId);
            filter.setFilterName(param.getName());
            filter.setFilterValue(param.getValue());
            pinferFilterDao.saveProteinferFilter(filter);
        }
    }

    private int createEntryInTblJobs(int submitterId) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            
            conn = DBConnectionManager.getConnection("yrc");
            String sql = "SELECT * FROM YRC_JOB_QUEUE.tblJobs WHERE id = 0";
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            
            rs = stmt.executeQuery( sql );
            rs.moveToInsertRow();
            
            rs.updateInt( "submitter", submitterId);
            rs.updateInt( "type", 1); // This is a Protein Inference Job
            rs.updateDate( "submitDate", new java.sql.Date( (new java.util.Date()).getTime() ) );
            rs.updateInt( "status", 0 );
            rs.updateInt( "attempts", 0 );
            
            rs.insertRow();
            rs.last();
            
            int id = rs.getInt( "id" );
            
            rs.close(); rs = null;
            stmt.close(); stmt = null;
            conn.close(); conn = null;
            
            return id;
            
        } finally {
            
            if (rs != null) {
                try { rs.close(); rs = null; } catch (Exception e) { ; }
            }

            if (stmt != null) {
                try { stmt.close(); stmt = null; } catch (Exception e) { ; }
            }
            
            if (conn != null) {
                try { conn.close(); conn = null; } catch (Exception e) { ; }
            }           
        }
    }

    private void createEntryInTblProteinferJobs(int jobId, int pinferId) throws Exception {
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        boolean newRow = false;
        
        try {
            
            conn = DBConnectionManager.getConnection("yrc");
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            
            String sql = "SELECT * FROM YRC_JOB_QUEUE.tblProteinInferJobs WHERE jobID = " + jobId;
            rs = stmt.executeQuery( sql );
            
            if (!rs.next()) {
                rs.moveToInsertRow();
                newRow = true;
            }
            
            rs.updateInt( "jobID", jobId);
            rs.updateInt( "piRunID", pinferId);
            
            
            if (newRow)
                rs.insertRow();
            else
                rs.updateRow();
            
            rs.close(); rs = null;
            stmt.close(); stmt = null;
            conn.close(); conn = null;
            
        } finally {
            
            if (rs != null) {
                try { rs.close(); rs = null; } catch (Exception e) { ; }
            }

            if (stmt != null) {
                try { stmt.close(); stmt = null; } catch (Exception e) { ; }
            }
            
            if (conn != null) {
                try { conn.close(); conn = null; } catch (Exception e) { ; }
            }           
        }
    }
}