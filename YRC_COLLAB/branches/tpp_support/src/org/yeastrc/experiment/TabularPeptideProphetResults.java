/**
 * TabularPeptideProphetResult.java
 * @author Vagisha Sharma
 * Apr 5, 2009
 * @version 1.0
 */
package org.yeastrc.experiment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.yeastrc.ms.domain.search.SORT_BY;
import org.yeastrc.ms.domain.search.SORT_ORDER;
import org.yeastrc.www.misc.Pageable;
import org.yeastrc.www.misc.TableCell;
import org.yeastrc.www.misc.TableHeader;
import org.yeastrc.www.misc.TableRow;
import org.yeastrc.www.misc.Tabular;

public class TabularPeptideProphetResults implements Tabular, Pageable {

    private SORT_BY[] columns = new SORT_BY[] {
            SORT_BY.FILE_ANALYSIS,
            SORT_BY.SCAN, 
            SORT_BY.CHARGE, 
            SORT_BY.MASS, 
            SORT_BY.RT, 
            SORT_BY.PEPTP_PROB, 
            SORT_BY.XCORR_RANK,
            SORT_BY.XCORR,
//            SORT_BY.DELTACN,
            SORT_BY.PEPTIDE,
            SORT_BY.PROTEIN
        };
        
        private SORT_BY sortColumn;
        private SORT_ORDER sortOrder = SORT_ORDER.ASC;
        
        
        private List<PeptideProphetResultPlus> results;
        
        private int currentPage;
        private int lastPage = currentPage;
        private List<Integer> displayPageNumbers;
        
        
        public TabularPeptideProphetResults(List<PeptideProphetResultPlus> results) {
            
            this.results = results;
            displayPageNumbers = new ArrayList<Integer>();
            displayPageNumbers.add(currentPage);
        }
        
        @Override
        public int columnCount() {
            return columns.length;
        }

        public SORT_BY getSortedColumn() {
            return sortColumn;
        }
        
        public void setSortedColumn(SORT_BY column) {
            this.sortColumn = column;
        }
        
        public SORT_ORDER getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(SORT_ORDER sortOrder) {
            this.sortOrder = sortOrder;
        }

        @Override
        public List<TableHeader> tableHeaders() {
            List<TableHeader> headers = new ArrayList<TableHeader>(columns.length);
            for(SORT_BY col: columns) {
                TableHeader header = new TableHeader(col.getDisplayName(), col.name());
                
                if(col == SORT_BY.XCORR_RANK || col == SORT_BY.XCORR || col == SORT_BY.DELTACN)
                    header.setSortable(false);
                if(col == SORT_BY.PROTEIN)
                    header.setSortable(false);
                
                if(col == sortColumn) {
                    header.setSorted(true);
                    header.setSortOrder(sortOrder);
                }
                headers.add(header);
            }
            return headers;
        }
        
        @Override
        public TableRow getRow(int index) {
            
            if(index >= results.size())
                return null;
            PeptideProphetResultPlus result = results.get(index);
            TableRow row = new TableRow();
            
            // row.addCell(new TableCell(String.valueOf(result.getId())));
            TableCell cell = new TableCell(result.getFilename());
            cell.setClassName("left_align");
            row.addCell(cell);
            row.addCell(new TableCell(String.valueOf(result.getScanNumber())));
            row.addCell(new TableCell(String.valueOf(result.getCharge())));
            row.addCell(new TableCell(String.valueOf(round(result.getObservedMass()))));
            
            // Retention time
            BigDecimal temp = result.getRetentionTime();
            if(temp == null) {
                row.addCell(new TableCell(""));
            }
            else
                row.addCell(new TableCell(String.valueOf(round(temp))));
            
            
            row.addCell(new TableCell(String.valueOf(result.getProbabilityRounded())));
            
            // Sequest data
            row.addCell(new TableCell(String.valueOf(result.getSequestData().getxCorrRank())));
            row.addCell(new TableCell(String.valueOf(round(result.getSequestData().getxCorr()))));
//            row.addCell(new TableCell(String.valueOf(round(result.getSequestData().getDeltaCN()))));
            
            String url = "viewSpectrum.do?scanID="+result.getScanId()+"&runSearchResultID="+result.getId();
            cell = new TableCell(String.valueOf(result.getResultPeptide().getFullModifiedPeptidePS()), url, true);
            cell.setClassName("left_align");
            row.addCell(cell);
            
            cell = new TableCell(String.valueOf(result.getProteins()));
            cell.setClassName("left_align");
            row.addCell(cell);
            
            return row;
        }
        
        private static double round(BigDecimal number) {
            return round(number.doubleValue());
        }
        private static double round(double num) {
            return Math.round(num*100.0)/100.0;
        }

        @Override
        public int rowCount() {
            return results.size();
        }

        @Override
        public void tabulate() {
            // nothing to do here?
        }

        
        
        @Override
        public int getCurrentPage() {
            return currentPage;
        }
        
        public void setCurrentPage(int pageNum) {
            this.currentPage = pageNum;
        }

        @Override
        public List<Integer> getDisplayPageNumbers() {
            return this.displayPageNumbers;
        }
        
        public void setDisplayPageNumbers(List<Integer> pageNums) {
            this.displayPageNumbers = pageNums;
        }

        @Override
        public int getLastPage() {
            return this.lastPage;
        }
        
        public void setLastPage(int pageNum) {
            this.lastPage = pageNum;
        }

        @Override
        public int getPageCount() {
            return lastPage;
        }

}