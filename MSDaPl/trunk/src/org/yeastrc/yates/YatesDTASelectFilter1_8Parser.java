/*
 * YatesDTASelectFilter1_8Parser.java
 * Created on Oct 14, 2004
 * Created by Michael Riffle <mriffle@u.washington.edu>
 */

package org.yeastrc.yates;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.yeastrc.nr_seq.NRDatabaseUtils;
import org.yeastrc.nr_seq.NRProtein;
import org.yeastrc.nr_seq.NRReference;

/**
 * Description of class goes here.
 * 
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version Oct 14, 2004
 */

public class YatesDTASelectFilter1_8Parser implements IYatesDTASelectFilterParser {

/* (non-Javadoc)
* @see org.yeastrc.yates.IYatesDTASelectFilterParser#parseFile(java.io.BufferedReader)
*/
public List parseFile(BufferedReader br, int databaseID) throws Exception {

	List<YatesResult> results = new ArrayList<YatesResult>();		// The list we're returning
	String line;						// The line we're parsing
	List<YatesPeptide> peptides = new ArrayList<YatesPeptide>();	// The list of peptides we're assocating with a result
	Set<YatesResult> tempResults = new HashSet<YatesResult>();	// The results we're currently parsing for peptides
	Set<NRProtein> tempProteins = new HashSet<NRProtein>();	// The proteins set in the above results
	String lastLineType = "";			// The last type of line we parsed
	
	// Our NR database search utils
	NRDatabaseUtils dbutils = NRDatabaseUtils.getInstance();
	
	/*
	 * Format of two types of lines we're interested in:
	 * 
	 * FASTA Header line: Describes the result (identified protein) and experimental values:
	 * Locus	Sequence Count	Spectrum Count	Sequence Coverage	Length	MolWt	pI	Validation Status	Descriptive Name
	 * ORFP:YKR009C	2	2	1.4%	900	98703	9.0	M	FOX2 SGDID:S0001717, Chr XI from 456692-453990, reverse complement 
	 * 
	 * Peptide line: Describes the peptide used in the identification of a protein:
	 * Unique	FileName	XCorr	DeltCN	M+H+	TotalIntensity	SpRank	IonProportion	Redundancy	Sequence	pI
	 * *	092302Ti3E375-614-05.1393.1393.2	2.9826	0.4235	1043.35	5207.0	1	81.2%	1	K.LHMGTDDVR.L	5.6328125
	 * 
	 */
	
	while ((line = br.readLine()) != null) {
		String[] fields = line.split("\\t");
		
		if (fields.length == 9 || (fields.length > 2 && fields[1].equals("Proteins"))) {
			if (fields[0].equals("Locus")) continue;
	
			// we're on a fasta header line or have passed the data in the file
			
			if (lastLineType.equals("peptide")) {
				
				// we last encountered a peptide line.  we should save the peptides to the results, and reset the results,proteins
				for ( YatesResult result : tempResults ) {
					result.setPeptides(peptides);
					results.add(result);
				}
				
				// reset these
				tempResults = new HashSet<YatesResult>();
				tempProteins = new HashSet<NRProtein>();
				peptides = new ArrayList<YatesPeptide>();
				
				// If this isn't a fasta line, but actually a line past the data, just stop
				if (fields.length != 9) break;
			}
			
			// get the accession string from the fasta line
			String[] tmp = fields[0].split(" ");
			String acc = tmp[0];
				
			// attempt to get the reference to this accession string from the database
			NRReference ref = dbutils.getCurrentReferenceBeginWithAccessionString(acc, databaseID);
			if (ref == null)
				throw new Exception ("Unable to map a protein ID to " + acc);
				
			NRProtein protein = ref.getProtein();
			if (!tempProteins.contains(protein)) {
					
				// Add this to the set, it's not in there yet
				YatesResult result = new YatesResult();
				result.setHitProtein(ref.getProtein());
				result.setSequenceCount(Integer.parseInt(fields[1]));
				result.setSpectrumCount(Integer.parseInt(fields[2]));
				
				fields[3] = fields[3].replaceAll("\\%", "");
				result.setSequenceCoverage(Double.parseDouble(fields[3]));
				
				result.setLength(Integer.parseInt(fields[4]));
				result.setMolecularWeight(Integer.parseInt(fields[5]));
				result.setPI(Double.parseDouble(fields[6]));
				result.setValidationStatus(fields[7]);
				result.setDescription(fields[8]);
				
				tempResults.add(result);
				tempProteins.add(protein);
			}
			
			lastLineType = "fasta";
			
		} else if (fields.length == 11) {
			if (fields[0].equals("Unique")) continue;
			
			// we're on a peptide line
			
			if (lastLineType.equals("fasta")) {
				// we've encountered a peptide line after a fasta
				peptides = new ArrayList<YatesPeptide>();	
			}
			
			YatesPeptide pep = new YatesPeptide();
			if (fields[0].equals("*"))
				pep.setUnique(true);
			else
				pep.setUnique(false);
			
			pep.setFilename(fields[1]);
			pep.setXCorr(Double.parseDouble(fields[2]));
			pep.setDeltaCN(Double.parseDouble(fields[3]));
			pep.setMH(Double.parseDouble(fields[4]));
			pep.setTotalIntensity(Double.parseDouble(fields[5]));
			pep.setSpRank(Integer.parseInt(fields[6]));
	
			fields[7] = fields[7].replaceAll("\\%", "");
			pep.setIonProportion(Double.parseDouble(fields[7]));
	
			pep.setRedundancy(Integer.parseInt(fields[8]));
			pep.setSequence(fields[9]);
			pep.setPI(Double.parseDouble(fields[10]));
			
			// Save this peptide to the list
			peptides.add(pep);
			
			lastLineType = "peptide";				
		} else {
			
			// we're on squat
			lastLineType = "squat";
			continue;
		}
	}
	
	
	return results;
	}

}