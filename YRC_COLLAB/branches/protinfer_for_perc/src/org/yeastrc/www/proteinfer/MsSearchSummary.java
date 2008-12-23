package org.yeastrc.www.proteinfer;

import java.util.ArrayList;
import java.util.List;

public class MsSearchSummary {

    private int searchId;
    private String searchProgram;
    private String searchProgramVersion;
    private String analysisProgram;
    private String analysisProgramVersion;
    private String searchDatabase;
    private List<RunSearchFile> files;
    
    public MsSearchSummary() {
        files = new ArrayList<RunSearchFile>();
    }
    
    public List<RunSearchFile> getFiles() {
        return files;
    }
    
    public int getSearchId() {
        return searchId;
    }

    public void setSearchId(int searchId) {
        this.searchId = searchId;
    }

    public void setFiles(List<RunSearchFile> files) {
        this.files = files;
    }
    
    // to be used by struts indexed properties
    public RunSearchFile getRunSearch(int index) {
        while(index >= files.size())
            files.add(new RunSearchFile());
        return files.get(index);
    }
    
    public void addRunSearch(RunSearchFile runSearch) {
        files.add(runSearch);
    }
    
    public String getSearchProgram() {
        return searchProgram;
    }

    public void setSearchProgram(String searchProgram) {
        this.searchProgram = searchProgram;
    }
    
    public String getSearchProgramVersion() {
        return searchProgramVersion;
    }

    public void setSearchProgramVersion(String searchProgramVersion) {
        this.searchProgramVersion = searchProgramVersion;
    }

    public String getAnalysisProgram() {
        return analysisProgram;
    }

    public void setAnalysisProgram(String analysisProgram) {
        this.analysisProgram = analysisProgram;
    }

    public String getAnalysisProgramVersion() {
        return analysisProgramVersion;
    }

    public void setAnalysisProgramVersion(String analysisProgramVersion) {
        this.analysisProgramVersion = analysisProgramVersion;
    }

    public String getSearchDatabase() {
        return searchDatabase;
    }

    public void setSearchDatabase(String searchDatabase) {
        this.searchDatabase = searchDatabase;
    }
    
    
    public static final class RunSearchFile {
        private int runSearchId;
        private String runName;
        private boolean selected = false;
        
        public RunSearchFile() {}
        
        public RunSearchFile(int runSearchId, String runName) {
            this.runSearchId = runSearchId;
            this.runName = runName;
        }
        
        public void setRunSearchId(int runSearchId) {
            this.runSearchId = runSearchId;
        }

        public void setRunName(String runName) {
            this.runName = runName;
        }

        public boolean getIsSelected() {
            return selected;
        }

        public void setIsSelected(boolean selected) {
            this.selected = selected;
        }

        public int getRunSearchId() {
            return runSearchId;
        }

        public String getRunName() {
            return runName;
        }
    }
}