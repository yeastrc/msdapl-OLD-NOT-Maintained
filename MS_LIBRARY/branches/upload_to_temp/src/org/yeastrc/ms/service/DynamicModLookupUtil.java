package org.yeastrc.ms.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yeastrc.ms.dao.DAOFactory;
import org.yeastrc.ms.domain.search.MsResidueModification;
import org.yeastrc.ms.domain.search.MsResidueModificationIn;
import org.yeastrc.ms.domain.search.MsTerminalModification;
import org.yeastrc.ms.domain.search.MsTerminalModificationIn;
import org.yeastrc.ms.domain.search.MsTerminalModification.Terminal;
import org.yeastrc.ms.upload.dao.UploadDAOFactory;
import org.yeastrc.ms.upload.dao.search.MsSearchModificationUploadDAO;
import org.yeastrc.ms.util.AminoAcidUtils;

public class DynamicModLookupUtil {


    private MsSearchModificationUploadDAO modDao;

    private List<MsResidueModification> dynaResMods;
    private List<MsTerminalModification> dynaTermMods;
    
    private Map<String, Integer> residueModMap;
    private Map<String, Integer> terminalModMap;
    
    private List<MsResidueModification> staticResMods;
    
    private int searchId;

    public DynamicModLookupUtil(int searchId){
        modDao = UploadDAOFactory.getInstance().getMsSearchModDAO();
        residueModMap = new HashMap<String, Integer>();
        terminalModMap = new HashMap<String, Integer>();
        buildModLookups(searchId);
        this.searchId = searchId;
    }

    public int getSearchId() {
        return searchId;
    }
    
    private void buildModLookups(int searchId) {
        buildResidueModLookup(searchId);
        buildTerminalModLookup(searchId);
        loadStaticMods(searchId);
    }
    
    private void buildResidueModLookup(int searchId) {
        residueModMap.clear();
        dynaResMods = modDao.loadDynamicResidueModsForSearch(searchId);
        String key = null;
        for (MsResidueModification mod: dynaResMods) {
            key = mod.getModifiedResidue()+""+mod.getModificationMass().doubleValue();
            residueModMap.put(key, mod.getId());
        }
    }

    private void buildTerminalModLookup(int searchId) {
        terminalModMap.clear();
        dynaTermMods = modDao.loadDynamicTerminalModsForSearch(searchId);
        String key = null;
        for (MsTerminalModification mod: dynaTermMods) {
            key = mod.getModifiedTerminal()+""+mod.getModificationMass().doubleValue();
            terminalModMap.put(key, mod.getId());
        }
    }
    
    private void loadStaticMods(int searchId) {
        staticResMods.clear();
        staticResMods = DAOFactory.instance().getMsSearchDAO().loadSearch(searchId).getStaticResidueMods();
    }

    /**
     * @param aa
     * @return true if the given amino acid and modification mass is a static modification
     */
    public boolean isStaticModification(char aa, BigDecimal modMass, boolean isMassPlusCharMass) {
        double mass = modMass.doubleValue();
        if(isMassPlusCharMass) {
            mass -= AminoAcidUtils.monoMass(aa);
        }
        for(MsResidueModification mod: this.staticResMods) {
            if(mod.getModifiedResidue() == aa && (Math.abs(mass - mod.getModificationMass().doubleValue()) < 0.05))
                return true;
        }
        return false;
    }
    
    /**
     * @param searchId
     * @param mod
     * @return the database ID of the modification that exactly matches the 
     *         given modified character and mass
     */
    public int getDynamicResidueModificationId(MsResidueModificationIn mod) {
        return getDynamicResidueModificationId(mod.getModifiedResidue(), mod.getModificationMass());
    }
    
    /**
     * @param searchId
     * @param modChar
     * @param modMass
     * @return the database ID of the modification that exactly matches the 
     *         given modified character and mass
     */
    public int getDynamicResidueModificationId(char modChar, BigDecimal modMass) {
        Integer modId = residueModMap.get(modChar+""+modMass.doubleValue());
        if (modId != null)  return modId;
        return 0;
    }
    
    /**
     * 
     * @param modChar
     * @param modMass
     * @param isMassPlusCharMass -- true if the modMass includes the mass of the 
     *                              given modChar. 
     * @return the modification that matches (within 0.5) the given 
     *         modified character and mass.  
     */
    public MsResidueModification getDynamicResidueModification(char modChar, BigDecimal modMass, boolean isMassPlusCharMass) {
        double mass = modMass.doubleValue();
        if(isMassPlusCharMass) {
            mass -= AminoAcidUtils.monoMass(modChar);
        }
        for(MsResidueModification mod: this.dynaResMods) {
            double mm = mod.getModificationMass().doubleValue();
            if(Math.abs(mm - mass) < 0.5 && modChar == mod.getModifiedResidue())
                return mod;
        }
        return null;
    }
    
    /**
     * @param searchId
     * @param mod
     * @return
     */
    public int getDynamicTerminalModificationId(MsTerminalModificationIn mod) {
        return getDynamicTerminalModificationId(mod.getModifiedTerminal(), mod.getModificationMass());
    }
    
    /**
     * @param searchId
     * @param modTerminal
     * @param modMass
     * @return 0 if no match is found
     */
    public int getDynamicTerminalModificationId(Terminal modTerminal, BigDecimal modMass) {
        Integer modId = terminalModMap.get(modTerminal+""+modMass.doubleValue());
        if (modId != null)  return modId;
        return 0;
    }
    
    public MsTerminalModification getTerminalModification(Terminal modTerminal, BigDecimal modMass) {
        int modId = getDynamicTerminalModificationId(modTerminal, modMass);
        if(modId > 0) {
            for(MsTerminalModification mod: dynaTermMods) {
                if(mod.getId() == modId)
                    return mod;
            }
        }
        return null;
    }
}
