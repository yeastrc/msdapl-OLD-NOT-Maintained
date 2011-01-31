/**
 * MsExperimentBean.java
 * @author Vagisha Sharma
 * Sep 18, 2008
 * @version 1.0
 */
package org.yeastrc.ms.domain.general.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.yeastrc.ms.domain.general.MsExperiment;
import org.yeastrc.ms.domain.general.MsExperimentStatus;

/**
 * 
 */
public class ExperimentBean implements MsExperiment {

    private int id;
    private String serverAddress;
    private String serverDirectory;
    private Date uploadDate;
    private Timestamp lastUpdateDate;
    private String comments;
    private int instrumentId;
    private List<Integer> speciesIds;
    private MsExperimentStatus status;
    
    public void setId(int id) {
        this.id = id;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }
    
    public void setServerDirectory(String serverDirectory) {
        this.serverDirectory = serverDirectory;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public void setLastUpdateDate(Timestamp lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
    
    
    public int getInstrumentId() {
        return this.instrumentId;
    }
    
    public void setInstrumentId(int instrumentId) {
        this.instrumentId = instrumentId;
    }
    
    @Override
    public int getId() {
        return id;
    }

    @Override
    public Timestamp getLastUpdateDate() {
        return lastUpdateDate;
    }

    @Override
    public String getServerAddress() {
        return serverAddress;
    }

    @Override
    public String getServerDirectory() {
        return serverDirectory;
    }
    
    @Override
    public Date getUploadDate() {
        return uploadDate;
    }

	@Override
	public List<Integer> getSpeciesIds() {
		if(this.speciesIds == null)
			return new ArrayList<Integer>(0);
		else
			return speciesIds;
	}

	@Override
	public void setSpeciesIds(List<Integer> speciesIds) {
		this.speciesIds = speciesIds;
	}

	@Override
	public MsExperimentStatus getStatus() {
		return status;
	}
	
	public void setStatus(MsExperimentStatus status) {
		this.status = status;
	}
}
