package org.yeastrc.ms.dao.search.sequest;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.yeastrc.ms.dao.search.MsSearchDAOImplTest.MsSearchTest;
import org.yeastrc.ms.dao.search.sqtfile.SQTBaseDAOTestCase;
import org.yeastrc.ms.domain.search.sequest.SequestParam;
import org.yeastrc.ms.domain.search.sequest.SequestSearch;
import org.yeastrc.ms.domain.search.sequest.SequestSearchDb;


public class SequestSearchDAOImplTest extends SQTBaseDAOTestCase {

   
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testOperationsOnSequestSearch () {
        
        // no saved search exists right now
        assertNull(sequestSearchDao.loadSearch(1));
        
        // save a search (don't add any extra information)
        SequestSearch search_1 = makeSequestSearch(false, false, false);
        assertEquals(0, search_1.getSearchDatabases().size());
        int searchId_1 = sequestSearchDao.saveSearch(search_1);
        
        // load using our specialized SequestSearchDAO
        SequestSearchDb search_1_db = sequestSearchDao.loadSearch(searchId_1);
        assertNotNull(search_1_db);
        assertEquals(searchId_1, search_1_db.getId());
        assertEquals(0, search_1_db.getDynamicResidueMods().size());
        assertEquals(0, search_1_db.getDynamicTerminalMods().size());
        assertEquals(0, search_1_db.getStaticResidueMods().size());
        assertEquals(0, search_1_db.getStaticTerminalMods().size());
        assertEquals(0, search_1_db.getSearchDatabases().size());
        assertEquals(0, search_1_db.getEnzymeList().size());
        checkSequestSearch(search_1, search_1_db);
        
        // save another search (add extra information)
        SequestSearch search_2 = makeSequestSearch(true, true, true);
        int searchId_2 = sequestSearchDao.saveSearch(search_2);
        
        // load the search and check values
        SequestSearchDb search_2_db = sequestSearchDao.loadSearch(searchId_2);
        assertNotNull(search_2_db);
        assertEquals(searchId_2, search_2_db.getId());
        assertTrue(search_2_db.getDynamicResidueMods().size() > 0);
        assertTrue(search_2_db.getDynamicTerminalMods().size() > 0);
        assertTrue(search_2_db.getStaticResidueMods().size() > 0);
        assertTrue(search_2_db.getStaticTerminalMods().size() > 0);
        assertTrue(search_2_db.getSearchDatabases().size() > 0);
        assertTrue(search_2_db.getEnzymeList().size() == 0);
        checkSequestSearch(search_2, search_2_db);
        
        // delete the searches
        sequestSearchDao.deleteSearch(searchId_1);
        assertNull(sequestSearchDao.loadSearch(searchId_1));
        
        sequestSearchDao.deleteSearch(searchId_2);
        assertNull(sequestSearchDao.loadSearch(searchId_2));
        
    }
    
    protected void checkSequestSearch(SequestSearch input, SequestSearchDb output) {
        super.checkSearch(input, output);
        
        List<SequestParam> inputParams = input.getSequestParams();
        List<SequestParam> outputParams = output.getSequestParams();
        
        assertEquals(inputParams.size(), outputParams.size());
        
        Collections.sort(inputParams, new Comparator<SequestParam>() {
            public int compare(SequestParam o1, SequestParam o2) {
                return o1.getParamName().compareTo(o2.getParamName());
            }});
        Collections.sort(outputParams, new Comparator<SequestParam>() {
            public int compare(SequestParam o1, SequestParam o2) {
                return o1.getParamName().compareTo(o2.getParamName());
            }});
        
        for (int i = 0; i < inputParams.size(); i++) {
            SequestParam ip = inputParams.get(i);
            SequestParam op = outputParams.get(i);
            assertEquals(ip.getParamName(), op.getParamName());
            assertEquals(op.getParamValue(), op.getParamValue());
        }
    }
    
    public static final class SequestSearchTest extends MsSearchTest implements SequestSearch {

        private List<SequestParam> params;
        
        public void setSequestParams(List<SequestParam> params) {
            this.params = params;
        }
        @Override
        public List<SequestParam> getSequestParams() {
            return params;
        }
        
    }
}
