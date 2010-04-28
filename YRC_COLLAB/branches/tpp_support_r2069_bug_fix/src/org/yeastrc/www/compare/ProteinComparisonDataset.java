/**
 * ComparisonDataset.java
 * @author Vagisha Sharma
 * Apr 11, 2009
 * @version 1.0
 */
package org.yeastrc.www.compare;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.yeastrc.ms.dao.ProteinferDAOFactory;
import org.yeastrc.ms.dao.nrseq.NrSeqLookupUtil;
import org.yeastrc.ms.dao.protinfer.ibatis.ProteinferProteinDAO;
import org.yeastrc.ms.dao.protinfer.idpicker.ibatis.IdPickerProteinBaseDAO;
import org.yeastrc.ms.dao.protinfer.proteinProphet.ProteinProphetProteinDAO;
import org.yeastrc.ms.domain.protinfer.ProteinferProtein;
import org.yeastrc.ms.domain.protinfer.SORT_BY;
import org.yeastrc.ms.domain.protinfer.idpicker.IdPickerProteinBase;
import org.yeastrc.ms.domain.search.SORT_ORDER;
import org.yeastrc.ms.util.ProteinUtils;
import org.yeastrc.nrseq.ProteinListing;
import org.yeastrc.nrseq.ProteinListingBuilder;
import org.yeastrc.nrseq.ProteinReference;
import org.yeastrc.www.compare.dataset.Dataset;
import org.yeastrc.www.compare.dataset.DatasetProteinInformation;
import org.yeastrc.www.compare.dataset.DatasetSource;
import org.yeastrc.www.compare.util.FastaDatabaseLookupUtil;
import org.yeastrc.www.misc.Pageable;
import org.yeastrc.www.misc.ResultsPager;
import org.yeastrc.www.misc.TableCell;
import org.yeastrc.www.misc.TableHeader;
import org.yeastrc.www.misc.TableRow;
import org.yeastrc.www.misc.Tabular;
import org.yeastrc.www.project.SORT_CLASS;

/**
 * 
 */
public class ProteinComparisonDataset implements Tabular, Pageable {

	private List<? extends Dataset> datasets;
	private List<Integer> fastaDatabaseIds; // for protein name lookup

	// FILTERED proteins
	private List<ComparisonProtein> proteins;

	// counts BEFORE filtering
	private int[][] proteinCounts;
	private int totalProteinCount;

	private float minNormalizedSpectrumCount;
	private float maxNormalizedSpectrumCount;

	private int rowCount = 50;
	private int currentPage = 1;
	private int pageCount = 1;
	private List<Integer> displayPageNumbers;

	private SORT_BY sortBy = SORT_BY.NUM_PEPT;
	private SORT_ORDER sortOrder = SORT_ORDER.DESC;

	private static final Logger log = Logger.getLogger(ProteinComparisonDataset.class.getName());



	public ProteinComparisonDataset() {
		this.datasets = new ArrayList<Dataset>();
		this.proteins = new ArrayList<ComparisonProtein>();
		this.displayPageNumbers = new ArrayList<Integer>();
	}

	private int  getOffset() {
		return (this.currentPage - 1)*rowCount;
	}

	public void setRowCount(int count) {
		this.rowCount = count;
	}

	public List<? extends Dataset> getDatasets() {
		return datasets;
	}

	public List<ComparisonProtein> getProteins() {
		return proteins;
	}

	public void setDatasets(List<? extends Dataset> datasets) {
		this.datasets = datasets;
	}

	public void addProtein(ComparisonProtein protein) {
		this.proteins.add(protein);
	}

	public int getDatasetCount() {
		return datasets.size();
	}

	public int getTotalProteinCount() {
		return totalProteinCount;
	}

	public int getFilteredProteinCount() {
		return proteins.size();
	}

	public void setMinNormalizedSpectrumCount(int minNormalizedSpectrumCount) {
		this.minNormalizedSpectrumCount = minNormalizedSpectrumCount;
	}

	public void setMaxNormalizedSpectrumCount(int maxNormalizedSpectrumCount) {
		this.maxNormalizedSpectrumCount = maxNormalizedSpectrumCount;
	}

	public void initSummary() {
		initProteinCounts();
		this.totalProteinCount = proteins.size();
		calculateSpectrumCountNormalization();
		getMinMaxSpectrumCounts();
	}

	private void getMinMaxSpectrumCounts() {
		float minCount = Float.MAX_VALUE;
		float maxCount = 1.0f;
		for(Dataset dataset: datasets) {
			minCount = Math.min(minCount, dataset.getNormMinProteinSpectrumCount());
			maxCount = Math.max(maxCount, dataset.getNormMaxProteinSpectrumCount());
		}
		this.minNormalizedSpectrumCount = minCount;
		this.maxNormalizedSpectrumCount = maxCount;
	}

	private void calculateSpectrumCountNormalization() {
		Dataset maxDataset = null;
		for(Dataset dataset: datasets) {
			if(maxDataset == null || maxDataset.getSpectrumCount() < dataset.getSpectrumCount())
				maxDataset = dataset;
		}
		for(Dataset dataset: datasets) {
			float normFactor = (float)maxDataset.getSpectrumCount() / (float)dataset.getSpectrumCount();
			dataset.setSpectrumCountNormalizationFactor(normFactor);
		}
	}

	private void initProteinCounts() {

		proteinCounts = new int[datasets.size()][datasets.size()];
		for(int i = 0; i < datasets.size(); i++) {
			for(int j = 0; j < datasets.size(); j++)
				proteinCounts[i][j] = 0;
		}

		for(ComparisonProtein protein: proteins) {

			for(int i = 0; i < datasets.size(); i++) {

				Dataset dsi = datasets.get(i);
				if(protein.isInDataset(dsi)) {

					proteinCounts[i][i]++;

					for(int j = i+1; j < datasets.size(); j++) {

						Dataset dsj = datasets.get(j);
						if(protein.isInDataset(dsj)) {
							proteinCounts[i][j]++;
							proteinCounts[j][i]++;
						}
					}
				}
			}
		}
	}

	public int getProteinCount(int datasetIndex) {

		if(proteinCounts == null) {
			initProteinCounts();
		}
		return proteinCounts[datasetIndex][datasetIndex];
	}

	public int getCommonProteinCount(int datasetIndex1, int datasetIndex2) {

		if(proteinCounts == null) {
			initProteinCounts();
		}
		return proteinCounts[datasetIndex1][datasetIndex2];
	}


	/**
	 * Fraction of dataset_1 proteins that were also found in dataset_2
	 * @param datasetIndex1
	 * @param datasetIndex2
	 * @return
	 */
	 public float getCommonProteinsPerc(int datasetIndex1, int datasetIndex2) {

		if(proteinCounts == null) {
			initProteinCounts();
		}

		int ds1Count = proteinCounts[datasetIndex1][datasetIndex1];
		int commonCount = proteinCounts[datasetIndex1][datasetIndex2];

		if(ds1Count <= 0)
			return 0;
		return calculatePercent(commonCount, ds1Count);
	}

	private static float calculatePercent(int num1, int num2) {
		return (float) (Math.round(((float)(num1*100.0)/(float)num2) * 10.0)/10.0);
	}

	@Override
	public int columnCount() {
		return datasets.size() + 4;
	}

	@Override
	public TableRow getRow(int index) {

		ComparisonProtein protein = proteins.get(index + this.getOffset());

		TableRow row = new TableRow();

		// Present / not present in each dataset
		int dsIndex = 0;
		for(Dataset dataset: datasets) {
			DatasetProteinInformation dpi = protein.getDatasetProteinInformation(dataset);
			TableCell cell = new TableCell();
			cell.setId(String.valueOf(dsIndex));
			dsIndex++;

			if(dpi == null || !dpi.isPresent()) { // dataset does not contain this protein
				cell.setClassName("prot-not-found");
			}
			else {
				String className = "prot-found";
				if(dpi.isParsimonious())
					className += "  prot-parsim centered ";
				if(dpi.isGrouped()) {
					className += " faded prot-group clickable ";
					cell.setName(String.valueOf(protein.getNrseqId()));
				}

				cell.setClassName(className);

				if(dpi.isParsimonious()) {
					cell.setData("*");
				}
			}
			row.addCell(cell);
		}

		// Protein accession
		TableCell protName = new TableCell(getAccessionContents(protein));
		protName.setHyperlink("viewProtein.do?id="+protein.getNrseqId());
		protName.setClassName("prot_accession");
		row.addCell(protName);

		// Protein common name
		TableCell protCommonName = new TableCell(getCommonNameContents(protein));
		row.addCell(protCommonName);

//		// External Links
//		TableCell extLinks = new TableCell(getExternalLinks(protein));
//		row.addCell(extLinks);

		// Protein molecular wt.
		TableCell molWt = new TableCell();
		molWt.setData(protein.getMolecularWeight()+"");
		row.addCell(molWt);

		// Protein pI
		TableCell pi = new TableCell();
		pi.setData(protein.getPi()+"");
		row.addCell(pi);

		// Protein description
		TableCell protDescr = new TableCell();
		protDescr.setClassName("prot_descr");
		protDescr.setData(getDescriptionContents(protein));
		row.addCell(protDescr);

		// Peptide count
		TableCell peptCount = new TableCell(String.valueOf(protein.getMaxPeptideCount()));
		peptCount.setClassName("pept_count clickable underline");
		peptCount.setId(String.valueOf(protein.getNrseqId()));
		row.addCell(peptCount);

		// Spectrum counts in each dataset
		for(Dataset dataset: datasets) {
			DatasetProteinInformation dpi = protein.getDatasetProteinInformation(dataset);
			TableCell cell = new TableCell();

			if(dpi == null || !dpi.isPresent()) { // dataset does not contain this protein
				cell.setClassName("prot-not-found");
			}
			else {
				String className = "prot-found";
				cell.setClassName(className);
				cell.setData(dpi.getSpectrumCount()+"("+dpi.getNormalizedSpectrumCountRounded()+")");
				cell.setTextColor("#FFFFFF");
				float scaledCount = getScaledSpectrumCount(dpi.getNormalizedSpectrumCount());
				cell.setBackgroundColor(getScaledColor(scaledCount));
			}
			row.addCell(cell);
		}

		return row;
	}

	private String getDescriptionContents(ComparisonProtein protein) {
		
    	String shortId = "short_desc_"+protein.getNrseqId();
    	String fullId = "full_desc_"+protein.getNrseqId();
    	
		String fullContents = "";
        fullContents += "<span style=\"display:none;\" class=\"full_description\" id=\""+fullId+"\">";
        String shortContents = "";
        shortContents += "<span class=\"short_description\" id=\""+shortId+"\">";
        List<ProteinReference> allReferences;
        // List<ProteinReference> uniqueDbRefs;
        ProteinReference oneRef;
		try {
			allReferences = protein.getProteinListing().getDescriptionReferences();
			oneRef = protein.getOneDescriptionReference();
		} catch (SQLException e) {
			log.error("Error getting description", e);
			return "ERROR";
		}
        for(ProteinReference ref: allReferences) {
        	String dbName = null;
        	try {dbName = ref.getDatabaseName();}
        	catch(SQLException e){log.error("Error getting database name"); dbName="ERROR";}
        	fullContents += "<span style=\"color:#000080;\"<b>";//["+dbName+"]</b></span>&nbsp;&nbsp;"+ref.getDescription();
        	if(ref.getHasExternalLink()) {
        		fullContents += "<a href=\""+ref.getUrl()+"\" style=\"font-size:8pt;\" target=\"External Link\">["+dbName+"]</a>";
        	}
        	else {
        		fullContents += "["+dbName+"]";
        	}
        	fullContents += "</b></span>&nbsp;&nbsp;"+ref.getDescription();
        	fullContents += "<br>";
        }
        if(allReferences.size() > 1) { // uniqueDbRefs.size()) {
        	fullContents += "<b><span class=\"clickable\" onclick=\"hideAllDescriptionsForProtein("+
        	protein.getNrseqId()+")\">[-]</span></b>";
        }
        if(oneRef != null)
        	shortContents += oneRef.getShortDescription();
        shortContents += "<br>";
        	
        if(allReferences.size() > 1) {
        	shortContents += "<b><span class=\"clickable\" onclick=\"showAllDescriptionsForProtein("+
        	protein.getNrseqId()+")\">[+]</span></b>";
        }
        
        fullContents += "</span>";
        shortContents += "</span>";
        return fullContents+"\n"+shortContents;
	}


	private String getCommonNameContents(ComparisonProtein protein) {

		String contents = "";
		contents += "<span onclick=\"showProteinDetails("+protein.getNrseqId()+"\")";
		contents += " class=\"clickable underline\">";
		List<ProteinReference> commonRefs = protein.getProteinListing().getCommonReferences();
		for(ProteinReference ref: commonRefs) {
			contents += ref.getCommonReference().getName()+"<br>";
		}
		contents += "</span>";
		return contents;
	}

	private String getAccessionContents(ComparisonProtein protein) {

		String fullContents = "";
		fullContents += "<span onclick=\"showProteinDetails("+protein.getNrseqId()+"\")";
		fullContents += " style=\"display:none;\" class=\"full_name clickable underline\">";
		String shortContents = "";
		shortContents += "<span onclick=\"showProteinDetails("+protein.getNrseqId()+"\")";
		shortContents += " class=\"short_name clickable underline\">";
		List<ProteinReference> references;
		references = protein.getProteinListing().getFastaReferences();
		
		for(ProteinReference ref: references) {
			fullContents += ref.getAccession()+"<br>";
			shortContents += ref.getShortAccession()+"<br>";
		}
		fullContents += "</span>";
		shortContents += "</span>";
		return fullContents+"\n"+shortContents;
	}

	private float getScaledSpectrumCount(float count) {
		return ((count - minNormalizedSpectrumCount + 1)/maxNormalizedSpectrumCount)*100.0f;
	}

	private String getScaledColor(float scaledSpectrumCount) {
		int rounded = (int)Math.ceil(scaledSpectrumCount);
		int green = 255;
		green -= 255.0/100.0 * rounded;
		int red = 0;
		red  += 255.0/100.0 * rounded;
		return "#"+hexValue(red, green, 0);
	}

	private String hexValue(int r, int g, int b) {
		String red = Integer.toHexString(r);
		if(red.length() == 1)
			red = "0"+red;
		String green = Integer.toHexString(g);
		if(green.length() == 1)
			green = "0"+green;
		String blue = Integer.toHexString(b);
		if(blue.length() == 1)
			blue = "0"+blue;
		return red+green+blue;
	}

	@Override
	public int rowCount() {
		return Math.min(rowCount, this.getFilteredProteinCount() - this.getOffset());
	}

	@Override
	public List<TableHeader> tableHeaders() {
		List<TableHeader> headers = new ArrayList<TableHeader>(columnCount());
		TableHeader header = null;

		//        header = new TableHeader("ID");
		//        header.setWidth(5);
		//        header.setSortable(false);
		//        headers.add(header);

		for(Dataset dataset: datasets) {
			header = new TableHeader(String.valueOf(dataset.getDatasetId()));
			header.setWidth(2);
			header.setSortable(false);
			headers.add(header);
		}

		header = new TableHeader("Name");
		header.setWidth(10);
		header.setSortable(false);
		headers.add(header);

		header = new TableHeader("Common<br>Name");
		header.setWidth(10);
		header.setSortable(false);
		headers.add(header);

//		header = new TableHeader("Links");
//		header.setWidth(5);
//		header.setSortable(false);
//		headers.add(header);

		header = new TableHeader("Mol. Wt.");
		header.setWidth(8);
		header.setSortable(true);
		header.setSortClass(SORT_CLASS.SORT_FLOAT);
		header.setDefaultSortOrder(SORT_ORDER.ASC);
		header.setHeaderId(SORT_BY.MOL_WT.name());
		if(this.sortBy == SORT_BY.MOL_WT) {
			header.setSorted(true);
			header.setSortOrder(this.sortOrder);
		}
		headers.add(header);

		header = new TableHeader("pI");
		header.setWidth(5);
		header.setSortClass(SORT_CLASS.SORT_FLOAT);
		header.setDefaultSortOrder(SORT_ORDER.ASC);
		header.setHeaderId(SORT_BY.PI.name());
		if(this.sortBy == SORT_BY.PI) {
			header.setSorted(true);
			header.setSortOrder(this.sortOrder);
		}
		headers.add(header);

		header = new TableHeader("Description");
		header.setWidth(100 - (15 + datasets.size()*2));
		header.setSortable(false);
		headers.add(header);

		// Peptide count
		header = new TableHeader("#Pept");
		header.setWidth(5);
		header.setSortable(true);
		header.setSortClass(SORT_CLASS.SORT_INT);
		header.setDefaultSortOrder(SORT_ORDER.DESC);
		header.setHeaderId(SORT_BY.NUM_PEPT.name());
		if(this.sortBy == SORT_BY.NUM_PEPT) {
			header.setSorted(true);
			header.setSortOrder(this.sortOrder);
		}
		headers.add(header);

		// spectrum counts
		for(Dataset dataset: datasets) {
			header = new TableHeader("SC("+dataset.getDatasetId()+")");
			header.setWidth(2);
			header.setSortable(false);
			headers.add(header);
		}

		return headers;
	}

	@Override
	public void tabulate() {

		int max = Math.min((this.getOffset() + rowCount), this.getFilteredProteinCount());
		initializeInfo(this.getOffset(), max);
	}

	public void initializeInfo(int startIndex, int endIndex) {

		for(int i = startIndex; i < endIndex; i++) {
			ComparisonProtein protein = proteins.get(i);
			initializeProteinInfo(protein);
		}
	}

	public void initializeProteinInfo(ComparisonProtein protein) {

		ProteinferProteinDAO protDao = ProteinferDAOFactory.instance().getProteinferProteinDao();
		IdPickerProteinBaseDAO idpProtDao = ProteinferDAOFactory.instance().getIdPickerProteinBaseDao();
		ProteinProphetProteinDAO ppProtDao = ProteinferDAOFactory.instance().getProteinProphetProteinDao();

		// Get the common name and description
		ProteinListing listing = ProteinListingBuilder.getInstance().build(protein.getNrseqId(), getFastaDatabaseIds());
		protein.setProteinListing(listing);

		// Get the group information for the different datasets
		for(DatasetProteinInformation dpi: protein.getDatasetInfo()) {
			if(dpi.getDatasetSource() == DatasetSource.PROTINFER) {
				boolean grouped = idpProtDao.isNrseqProteinGrouped(dpi.getDatasetId(), protein.getNrseqId());
				dpi.setGrouped(grouped);
			}
			else if(dpi.getDatasetSource() == DatasetSource.PROTEIN_PROPHET) {
				boolean grouped = ppProtDao.isNrseqProteinGrouped(dpi.getDatasetId(), protein.getNrseqId());
				dpi.setGrouped(grouped);
			}
			// TODO for DTASelect
		}

		// Get the spectrum count information for the protein in the different datasets
		ArrayList<Integer> nrseqIds = new ArrayList<Integer>(1);
		nrseqIds.add(protein.getNrseqId());
		for(DatasetProteinInformation dpi: protein.getDatasetInfo()) {
			if(dpi.getDatasetSource() != DatasetSource.PROTEIN_PROPHET) {
				List<Integer> piProteinIds = protDao.getProteinIdsForNrseqIds(dpi.getDatasetId(), nrseqIds);
				if(piProteinIds.size() == 1) {
					ProteinferProtein prot = protDao.loadProtein(piProteinIds.get(0));
					dpi.setSpectrumCount(prot.getSpectrumCount());
				}
				else {
					log.error("Number of proteinIds for nrseqId: "+protein.getNrseqId()+
							" is "+piProteinIds.size()+"; Expected only one.");
				}
			}
		}

		// get the (max)number of peptides identified for this protein
		if(this.sortBy != SORT_BY.NUM_PEPT)
			protein.setMaxPeptideCount(DatasetPeptideComparer.instance().getMaxPeptidesForProtein(protein));

		// Get the NSAF information for the protein in the different datasets
		// NSAF is available only for ProteinInference proteins
		for(DatasetProteinInformation dpi: protein.getDatasetInfo()) {
			if(dpi.getDatasetSource() == DatasetSource.PROTINFER) {
				List<Integer> piProteinIds = idpProtDao.getProteinIdsForNrseqIds(dpi.getDatasetId(), nrseqIds);
				if(piProteinIds.size() == 1) {
					IdPickerProteinBase prot = idpProtDao.loadProtein(piProteinIds.get(0));
					dpi.setNsaf(prot.getNsaf());
				}
			}
		}

		// get the protein properties
		if(!protein.molWtAndPiSet()) {
			String sequence = NrSeqLookupUtil.getProteinSequence(protein.getNrseqId());
			protein.setMolecularWeight( (float) (Math.round(ProteinUtils.calculateMolWt(sequence)*100) / 100.0));
			protein.setPi((float) (Math.round(ProteinUtils.calculatePi(sequence)*100) / 100.0));
		}
	}


	List<Integer> getFastaDatabaseIds() {
		if(this.fastaDatabaseIds == null) {
			fastaDatabaseIds = FastaDatabaseLookupUtil.getFastaDatabaseIds(this.datasets);
		}
		return fastaDatabaseIds;
	}

	public void setCurrentPage(int page) {
		this.currentPage = page;
		ResultsPager pager = ResultsPager.instance();
		this.pageCount = pager.getPageCount(this.proteins.size(), rowCount);
		this.displayPageNumbers = pager.getPageList(this.proteins.size(), currentPage, rowCount);
	}

	@Override
	public int getCurrentPage() {
		return currentPage;
	}

	@Override
	public List<Integer> getDisplayPageNumbers() {
		return this.displayPageNumbers;
	}

	@Override
	public int getLastPage() {
		return this.pageCount;
	}

	@Override
	public int getPageCount() {
		return this.pageCount;
	}

	public SORT_BY getSortBy() {
		return sortBy;
	}

	public void setSortBy(SORT_BY sortBy) {
		this.sortBy = sortBy;
	}

	public SORT_ORDER getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(SORT_ORDER sortOrder) {
		this.sortOrder = sortOrder;
	}

	@Override
	public void setDisplayPageNumbers(List<Integer> pageList) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLastPage(int pageCount) {
		throw new UnsupportedOperationException();
	}

}