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

import org.yeastrc.ms.domain.search.Program;
import org.yeastrc.ms.domain.search.SORT_BY;
import org.yeastrc.ms.domain.search.SORT_ORDER;
import org.yeastrc.ms.service.ModifiedSequenceBuilderException;
import org.yeastrc.www.misc.HyperlinkedData;
import org.yeastrc.www.misc.Pageable;
import org.yeastrc.www.misc.TableCell;
import org.yeastrc.www.misc.TableHeader;
import org.yeastrc.www.misc.TableRow;
import org.yeastrc.www.misc.Tabular;
import org.yeastrc.www.util.RoundingUtils;

public class TabularPeptideProphetResults implements Tabular, Pageable {

        private SORT_BY[] columns;
        
        private SORT_BY sortColumn;
        private SORT_ORDER sortOrder = SORT_ORDER.ASC;
        
        
        private List<? extends PeptideProphetResultPlus> results;
        private Program searchProgram;
        
        private int currentPage;
        private int numPerPage;
        private int lastPage = currentPage;
        private List<Integer> displayPageNumbers;
        
        private RoundingUtils rounder;
        
        public TabularPeptideProphetResults(List<? extends PeptideProphetResultPlus> results, 
                Program searchProgram) {
            
            this.results = results;
            displayPageNumbers = new ArrayList<Integer>();
            displayPageNumbers.add(currentPage);
            this.searchProgram = searchProgram;
            
            rounder = RoundingUtils.getInstance();
            
            if(searchProgram == Program.SEQUEST) {
                columns = new SORT_BY[] {
                        SORT_BY.FILE_ANALYSIS,
                        SORT_BY.SCAN, 
                        SORT_BY.CHARGE, 
                        SORT_BY.MASS, 
                        SORT_BY.RT, 
                        SORT_BY.PEPTP_PROB, 
                        SORT_BY.XCORR_RANK,
                        SORT_BY.XCORR,
//                        SORT_BY.DELTACN,
                        SORT_BY.PEPTIDE,
                        SORT_BY.PROTEIN
                    };
            }
            else if(searchProgram == Program.MASCOT) {
                columns = new SORT_BY[] {
                        SORT_BY.FILE_ANALYSIS,
                        SORT_BY.SCAN, 
                        SORT_BY.CHARGE, 
                        SORT_BY.MASS, 
                        SORT_BY.RT, 
                        SORT_BY.PEPTP_PROB, 
                        SORT_BY.MASCOT_RANK,
                        SORT_BY.ION_SCORE,
                        SORT_BY.IDENTITY_SCORE,
                        SORT_BY.HOMOLOGY_SCORE,
                        SORT_BY.MASCOT_EXPECT,
//                        SORT_BY.DELTACN,
                        SORT_BY.PEPTIDE,
                        SORT_BY.PROTEIN
                    };
            }
            
            else if(searchProgram == Program.XTANDEM) {
                columns = new SORT_BY[] {
                        SORT_BY.FILE_ANALYSIS,
                        SORT_BY.SCAN, 
                        SORT_BY.CHARGE, 
                        SORT_BY.MASS, 
                        SORT_BY.RT, 
                        SORT_BY.PEPTP_PROB, 
                        SORT_BY.XTANDEM_RANK,
                        SORT_BY.HYPER_SCORE,
                        SORT_BY.NEXT_SCORE,
                        SORT_BY.B_SCORE,
                        SORT_BY.Y_SCORE,
                        SORT_BY.XTANDEM_EXPECT,
                        SORT_BY.PEPTIDE,
                        SORT_BY.PROTEIN
                    };
            }
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
                
                if(col.getTooltip() != null)
                	header.setTitle(col.getTooltip());
                
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
            
            // row.addCell(new TableCell(String.valueOf(result.getPeptideProphetResultId())));
            row.addCell(new TableCell(result.getFilename()));
            row.addCell(makeRightAlignCell(String.valueOf(result.getScanNumber())));
            row.addCell(makeRightAlignCell(String.valueOf(result.getCharge())));
            row.addCell(makeRightAlignCell(rounder.roundFourFormat(result.getObservedMass())));
            
            // Retention time
            BigDecimal temp = result.getRetentionTime();
            if(temp == null) {
                row.addCell(new TableCell(""));
            }
            else
                row.addCell(makeRightAlignCell(rounder.roundTwoFormat(temp)));
            
            
            row.addCell(makeRightAlignCell(String.valueOf(result.getProbabilityRounded())));
            
            // Sequest data
            if(searchProgram == Program.SEQUEST) {
                row.addCell(makeRightAlignCell(String.valueOf(((PeptideProphetResultPlusSequest)result).getSequestData().getxCorrRank())));
                row.addCell(makeRightAlignCell(rounder.roundTwoFormat(((PeptideProphetResultPlusSequest)result).getSequestData().getxCorr())));
//            row.addCell(new TableCell(String.valueOf(round(result.getSequestData().getDeltaCN()))));
            }
            
            // Mascot data
            else if(searchProgram == Program.MASCOT) {
                row.addCell(makeRightAlignCell(String.valueOf(((PeptideProphetResultPlusMascot)result).getMascotData().getRank())));
                row.addCell(makeRightAlignCell(rounder.roundFourFormat(((PeptideProphetResultPlusMascot)result).getMascotData().getIonScore())));
                row.addCell(makeRightAlignCell(rounder.roundFourFormat(((PeptideProphetResultPlusMascot)result).getMascotData().getIdentityScore())));
                row.addCell(makeRightAlignCell(rounder.roundFourFormat(((PeptideProphetResultPlusMascot)result).getMascotData().getHomologyScore())));
                row.addCell(makeRightAlignCell(rounder.roundFourFormat(((PeptideProphetResultPlusMascot)result).getMascotData().getExpect())));
            }
            
            // Xtandem data
            else if(searchProgram == Program.XTANDEM) {
                row.addCell(makeRightAlignCell(String.valueOf(((PeptideProphetResultPlusXtandem)result).getXtandemData().getRank())));
                row.addCell(makeRightAlignCell(rounder.roundFourFormat(((PeptideProphetResultPlusXtandem)result).getXtandemData().getHyperScore())));
                row.addCell(makeRightAlignCell(rounder.roundFourFormat(((PeptideProphetResultPlusXtandem)result).getXtandemData().getNextScore())));
                row.addCell(makeRightAlignCell(rounder.roundFourFormat(((PeptideProphetResultPlusXtandem)result).getXtandemData().getBscore())));
                row.addCell(makeRightAlignCell(rounder.roundFourFormat(((PeptideProphetResultPlusXtandem)result).getXtandemData().getYscore())));
                row.addCell(makeRightAlignCell(rounder.roundFourFormat(((PeptideProphetResultPlusXtandem)result).getXtandemData().getExpect())));
            }
            
            
            TableCell cell = null;
            String modifiedSequence = null;
            try {
            	// get modified peptide of the form: K.PEP[+80]TIDE.L
            	modifiedSequence = result.getResultPeptide().getFullModifiedPeptide(true); 
            }
            catch (ModifiedSequenceBuilderException e) {
                cell = new TableCell("Error building peptide sequence");
                modifiedSequence = null;
            }
            if(modifiedSequence != null) {
            	cell = new TableCell();
            	// link to Java spectrum viewer
            	HyperlinkedData javaLink = new HyperlinkedData("<span style='font-size:8pt;' title='Java Spectrum Viewer'>(J)</span>");
            	String url = "viewSpectrum.do?scanID="+result.getScanId()+"&runSearchResultID="+result.getId()
            	+"&runSearchAnalysisID="+result.getRunSearchAnalysisId()
            	+"&java=true";
            	javaLink.setHyperlink(url, true);
            	javaLink.setTargetName("spec_view_java");
            	cell.addData(javaLink);
            	
            	// link to JavaScript spectrum viewer
            	HyperlinkedData jsLink = new HyperlinkedData("<span style='font-size:8pt;' title='JavaScript Spectrum Viewer'>(JS)</span>");
            	url = "viewSpectrum.do?scanID="+result.getScanId()+"&runSearchResultID="+result.getId()
            	+"&runSearchAnalysisID="+result.getRunSearchAnalysisId();
            	jsLink.setHyperlink(url, true);
            	jsLink.setTargetName("spec_view_js");
            	cell.addData(jsLink);
            	
            	if(result.getResultPeptide().hasDynamicModification())
            		cell.addData("<span class=\"peptide\">"+modifiedSequence+"</span>");
            	else
            		cell.addData(modifiedSequence);
            }
            row.addCell(cell);
            
            
            String cellContents = result.getOneProteinShort();
            if(result.getProteinCount() > 1) {
                cellContents += " <span class=\"underline clickable\" "+
                "onClick=javascript:toggleProteins("+result.getPeptideProphetResultId()+") "+
                ">("+result.getProteinCount()+")</span>";
                cellContents += " \n<div style=\"display: none;\" id=\"proteins_for_"
                			 +result.getPeptideProphetResultId()+"\">"+result.getOtherProteinsShortHtml()+"</div>";
            }
            cell = new TableCell(cellContents);
            row.addCell(cell);
            
            return row;
        }
        
        private TableCell makeRightAlignCell(String content) {
        	TableCell cell = new TableCell(content);
        	cell.setClassName("right_align");
        	return cell;
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
        
        @Override
    	public int getNumPerPage() {
    		return numPerPage;
    	}

    	@Override
    	public void setNumPerPage(int num) {
    		this.numPerPage = num;
    	}

}
