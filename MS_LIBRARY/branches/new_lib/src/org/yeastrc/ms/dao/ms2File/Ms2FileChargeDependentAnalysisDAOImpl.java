/**
 * Ms2FileChargeDependentAnalysisDAOImpl.java
 * @author Vagisha Sharma
 * Jun 18, 2008
 * @version 1.0
 */
package org.yeastrc.ms.dao.ms2File;

import java.util.List;

import org.yeastrc.ms.dao.BaseSqlMapDAO;
import org.yeastrc.ms.dto.ms2File.Ms2FileChargeDependentAnalysis;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * 
 */
public class Ms2FileChargeDependentAnalysisDAOImpl extends BaseSqlMapDAO
        implements Ms2FileChargeDependentAnalysisDAO {

    public Ms2FileChargeDependentAnalysisDAOImpl(SqlMapClient sqlMap) {
        super(sqlMap);
    }

    public List<Ms2FileChargeDependentAnalysis> loadAnalysisForScanCharge(int scanChargeId) {
        return queryForList("Ms2FileChargeDependentAnalysis.selectAnalysisForCharge", scanChargeId);
    }

    public boolean save(Ms2FileChargeDependentAnalysis analysis) {
        return save("Ms2FileChargeDependentAnalysis.insert", analysis);
    }

}
