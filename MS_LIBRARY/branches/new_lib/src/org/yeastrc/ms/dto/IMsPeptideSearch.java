package org.yeastrc.ms.dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public interface IMsPeptideSearch {

    /**
     * @return the originalFileType
     */
    public abstract String getOriginalFileType();

    /**
     * @return the searchEngineName
     */
    public abstract String getSearchEngineName();

    /**
     * @return the searchEngineVersion
     */
    public abstract String getSearchEngineVersion();

    /**
     * @return the searchDate
     */
    public abstract Date getSearchDate();

    /**
     * @return the searchDuration
     */
    public abstract int getSearchDuration();

    /**
     * @return the precursorMassType
     */
    public abstract String getPrecursorMassType();

    /**
     * @return the precursorMassTolerance
     */
    public abstract BigDecimal getPrecursorMassTolerance();

    /**
     * @return the fragmentMassType
     */
    public abstract String getFragmentMassType();

    /**
     * @return the fragmentMassTolerance
     */
    public abstract BigDecimal getFragmentMassTolerance();

    /**
     * @return the searchDatabases
     */
    public abstract List<? extends IMsSequenceDatabase> getSearchDatabases();

    /**
     * @return the staticModifications
     */
    public abstract List<? extends IMsSearchMod> getStaticModifications();

    /**
     * @return the dynamicModifications
     */
    public abstract List<? extends IMsSearchDynamicMod> getDynamicModifications();

}