/**
 * ProteinPropertiesStore.java
 * @author Vagisha Sharma
 * Nov 3, 2009
 * @version 1.0
 */
package org.yeastrc.www.proteinfer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.yeastrc.ms.dao.ProteinferDAOFactory;
import org.yeastrc.ms.dao.nrseq.NrSeqLookupUtil;
import org.yeastrc.ms.dao.protinfer.idpicker.ibatis.IdPickerProteinDAO;
import org.yeastrc.ms.dao.protinfer.proteinProphet.ProteinProphetProteinDAO;
import org.yeastrc.ms.domain.protinfer.ProteinInferenceProgram;
import org.yeastrc.ms.domain.protinfer.ProteinferRun;
import org.yeastrc.ms.domain.protinfer.idpicker.IdPickerProtein;
import org.yeastrc.ms.domain.protinfer.proteinProphet.ProteinProphetProtein;
import org.yeastrc.ms.util.ProteinUtils;
import org.yeastrc.ms.util.TimeUtils;

/**
 * 
 */
public class ProteinPropertiesStore {

    private LinkedHashMap<Integer, Map<Integer, ProteinProperties>> store;
    private final int size = 3;
    
    private final Logger log = Logger.getLogger(ProteinPropertiesStore.class.getName());
    
    private static ProteinPropertiesStore instance = null;
    
    private ProteinPropertiesStore() {
        int capacity = (int)Math.ceil(size/0.75f) + 1;
        store = new LinkedHashMap<Integer, Map<Integer,ProteinProperties>>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry (Map.Entry<Integer, Map<Integer, ProteinProperties>> eldest) {
                // This method is invoked by put and putAll after inserting a new entry into the map.
                return store.size() > size;  
            }
        };
    }
    
    public static ProteinPropertiesStore getInstance() {
        if(instance == null)
            instance = new ProteinPropertiesStore();
        return instance;
    }
    
    public Map<Integer, ProteinProperties> getPropertiesMapForProteinInference(int pinferId) {
        return getPropertiesMapForProteinInference(pinferId, true);
    }
    
    private Map<Integer, ProteinProperties> getPropertiesMapForProteinInference(int pinferId, boolean createNew) {
        Map<Integer, ProteinProperties> map = store.get(pinferId);
        if(map == null) {
            if(createNew) {
                map = buildMap(pinferId);
                store.put(pinferId, map);
            }
        }
        return map;
    }

    private Map<Integer, ProteinProperties> buildMap(int pinferId) {
        
        Map<Integer, ProteinProperties> map = null;
        log.info("Building Protein Properties map");
        long s = System.currentTimeMillis();
        
        ProteinferRun run = ProteinferDAOFactory.instance().getProteinferRunDao().loadProteinferRun(pinferId);
        
        // If this is a IDPicker run we will load the protein from the IDPicker tables so that 
        // we have the IDPicker groupIDs.
        if(ProteinInferenceProgram.isIdPicker(run.getProgram())) {
            
            IdPickerProteinDAO proteinDao = ProteinferDAOFactory.instance().getIdPickerProteinDao();
            
            List<IdPickerProtein> proteins = proteinDao.loadProteins(pinferId);
            map = new HashMap<Integer, ProteinProperties>((int) (proteins.size() * 1.5));
            long e = System.currentTimeMillis();
            log.info("Time to get all proteins: "+TimeUtils.timeElapsedSeconds(s, e)+" seconds");
            
            s = System.currentTimeMillis();
            for(IdPickerProtein protein: proteins) {
                String sequence = NrSeqLookupUtil.getProteinSequence(protein.getNrseqProteinId());
                
                ProteinProperties props = new ProteinProperties(protein.getId(), protein.getGroupId());
                props.setNrseqId(protein.getNrseqProteinId());
                props.setMolecularWt(ProteinUtils.calculateMolWt(sequence));
                props.setPi(ProteinUtils.calculatePi(sequence));
                
                map.put(protein.getId(), props);
            }
            e = System.currentTimeMillis();
            log.info("Time to assign protein properties: "+TimeUtils.timeElapsedSeconds(s, e)+" seconds");
        }
        
        // If this a ProteinProphet run we will load proteins from the ProteinProphet tables 
        // so that we have ProteinProphet protein group Ids.
        else if(run.getProgram() == ProteinInferenceProgram.PROTEIN_PROPHET) {
            
            ProteinProphetProteinDAO proteinDao = ProteinferDAOFactory.instance().getProteinProphetProteinDao();
            
            List<ProteinProphetProtein> proteins = proteinDao.loadProteins(pinferId);
            map = new HashMap<Integer, ProteinProperties>((int) (proteins.size() * 1.5));
            long e = System.currentTimeMillis();
            log.info("Time to get all proteins: "+TimeUtils.timeElapsedSeconds(s, e)+" seconds");
            
            s = System.currentTimeMillis();
            for(ProteinProphetProtein protein: proteins) {
                String sequence = NrSeqLookupUtil.getProteinSequence(protein.getNrseqProteinId());
                
                ProteinProperties props = new ProteinProperties(protein.getId(), protein.getGroupId());
                props.setNrseqId(protein.getNrseqProteinId());
                props.setMolecularWt(ProteinUtils.calculateMolWt(sequence));
                props.setPi(ProteinUtils.calculatePi(sequence));
                
                map.put(protein.getId(), props);
            }
            e = System.currentTimeMillis();
            log.info("Time to assign protein properties: "+TimeUtils.timeElapsedSeconds(s, e)+" seconds");
        }
        
        return map;
    }
}
