package org.yeastrc.ms.dao.search.sqtfile;

import java.util.ArrayList;
import java.util.List;

import org.yeastrc.ms.dao.search.MsRunSearchDAOImplTest.MsRunSearchTest;
import org.yeastrc.ms.domain.search.sqtfile.SQTField;
import org.yeastrc.ms.domain.search.sqtfile.SQTRunSearch;
import org.yeastrc.ms.domain.search.sqtfile.SQTRunSearchDb;

public class SQTRunSearchDAOTest extends SQTBaseDAOTestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testOperationsOnSQTPeptideSearch () {
        
        // no saved search exists right now
        assertNull(sqtRunSearchDao.loadRunSearch(1));
        
        // save a search (don't add any SQT headers)
        SQTRunSearch runSearch_1 = makeSQTRunSearch(false);
        assertEquals(0, runSearch_1.getHeaders().size());
        int runSearchId_1 = sqtRunSearchDao.saveRunSearch(runSearch_1, 1, 1); // runId = 1; // searchId = 1
        
        // load the search using the general MsRunSearch DAO and make sure
        // the class of the returned object is SQTRunSearch
        assertTrue(runSearchDao.loadRunSearch(runSearchId_1) instanceof SQTRunSearchDb);
        assertFalse(null instanceof SQTRunSearchDb);
        
        // load using our specialized SQTRunSearchDAO
        SQTRunSearchDb runSearchDb_1 = sqtRunSearchDao.loadRunSearch(runSearchId_1);
        assertNotNull(runSearchDb_1);
        assertEquals(0, runSearchDb_1.getHeaders().size());
        checkSQTSearch(runSearch_1, runSearchDb_1);
        
        // save another search (add SQT headers)
        SQTRunSearch runSearch_2 = makeSQTRunSearch(true);
        assertEquals(2, runSearch_2.getHeaders().size());
        int runSearchId_2 = sqtRunSearchDao.saveRunSearch(runSearch_2, 1, 1); // runId = 1; // searchId = 1
        
        // load the search with headers and check values
        SQTRunSearchDb runSearchDb_2 = sqtRunSearchDao.loadRunSearch(runSearchId_2);
        assertNotNull(runSearchDb_2);
        assertEquals(2, runSearchDb_2.getHeaders().size());
        assertEquals(1,  runSearchDb_2.getRunId());
        checkSQTSearch(runSearch_2, runSearchDb_2);
        
        // load both searches; make sure the right object types are returned
        List<Integer> runSearchIds = sqtRunSearchDao.loadRunSearchIdsForRun(1);
        assertEquals(2, runSearchIds.size());
        assertTrue(sqtRunSearchDao.loadRunSearch(runSearchIds.get(0)) instanceof SQTRunSearchDb);
        assertTrue(sqtRunSearchDao.loadRunSearch(runSearchIds.get(1)) instanceof SQTRunSearchDb);
        
        // delete the searches
        sqtRunSearchDao.deleteRunSearch(runSearchId_1);
        assertNull(sqtRunSearchDao.loadRunSearch(runSearchId_1));
        assertEquals(0, sqtHeaderDao.loadSQTHeadersForRunSearch(runSearchId_1).size());
        
        sqtRunSearchDao.deleteRunSearch(runSearchId_2);
        assertNull(sqtRunSearchDao.loadRunSearch(runSearchId_2));
        assertEquals(0, sqtHeaderDao.loadSQTHeadersForRunSearch(runSearchId_2).size());
        
    }
    
    private void checkSQTSearch(SQTRunSearch input, SQTRunSearchDb output) {
        super.checkRunSearch(input, output);
        assertEquals(input.getHeaders().size(), output.getHeaders().size());
    }
    
    public static final class SQTRunSearchTest extends MsRunSearchTest implements SQTRunSearch {

        private List<SQTField> headers = new ArrayList<SQTField>();

        public List<SQTField> getHeaders() {
            return headers ;
        }

        public void setHeaders(List<SQTField> headers) {
            this.headers = headers;
        }

        public void addHeader(SQTField header) {
            headers.add(header);
        }
    }
}
