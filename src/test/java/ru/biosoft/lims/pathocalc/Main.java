package ru.biosoft.lims.pathocalc;

import java.util.Map;

import static ru.biosoft.lims.pathocalc.ACMGCriteria.*;
import static ru.biosoft.lims.pathocalc.ACMGCriteriaValue.State.*;
import static ru.biosoft.lims.pathocalc.ACMGClassification.*;

public class Main {
	
	// See https://pmc.ncbi.nlm.nih.gov/articles/PMC4544753/ Table5
	public static ACMGClassification classify(Map<ACMGCriteria, ACMGCriteriaValue> ACMG)
	{
		int psCount = 0;
		if(ACMG.get(PS1).state == MET)
			psCount++;
		if(ACMG.get(PS2).state == MET)
			psCount++;
		if(ACMG.get(PS3).state == MET)
			psCount++;
		if(ACMG.get(PS4).state == MET)
			psCount++;
		
		int pmCount = 0;
		if(ACMG.get(PM1).state == MET)
			pmCount++;
		if(ACMG.get(PM2).state == MET)
			pmCount++;
		if(ACMG.get(PM3).state == MET)
			pmCount++;
		if(ACMG.get(PM4).state == MET)
			pmCount++;
		if(ACMG.get(PM5).state == MET)
			pmCount++;
		if(ACMG.get(PM6).state == MET)
			pmCount++;
		
		int ppCount = 0;
		if(ACMG.get(PP1).state == MET)
			ppCount++;
		if(ACMG.get(PP2).state == MET)
			ppCount++;
		if(ACMG.get(PP3).state == MET)
			ppCount++;
		if(ACMG.get(PP4).state == MET)
			ppCount++;
		if(ACMG.get(PP5).state == MET)
			ppCount++;
		
		int bsCount = 0;
		if(ACMG.get(BS1).state == MET)
			bsCount++;
		if(ACMG.get(BS2).state == MET)
			bsCount++;
		if(ACMG.get(BS3).state == MET)
			bsCount++;
		if(ACMG.get(BS4).state == MET)
			bsCount++;
		
		int bpCount = 0;
		if(ACMG.get(BP1).state == MET)
			bpCount++;
		if(ACMG.get(BP2).state == MET)
			bpCount++;
		if(ACMG.get(BP3).state == MET)
			bpCount++;
		if(ACMG.get(BP4).state == MET)
			bpCount++;
		if(ACMG.get(BP5).state == MET)
			bpCount++;
		if(ACMG.get(BP6).state == MET)
			bpCount++;
		if(ACMG.get(BP7).state == MET)
			bpCount++;

		
		boolean isPathogenic = false;
		if(ACMG.get(PVS1).state == MET)
		{
			//Very Strong (PVS1) AND
			// a. ≥1 Strong (PS1–PS4) OR
		    // b. ≥2 Moderate (PM1–PM6) OR
		    // c. 1 Moderate (PM1–PM6) and 1 Supporting (PP1–PP5) OR
		    // d. ≥2 Supporting (PP1–PP5)

			if(psCount >= 1 || pmCount >= 2 || (pmCount==1 && ppCount==1) || ppCount >= 2)
				isPathogenic = true;
		}
		
		//≥2 Strong (PS1–PS4)
		if(psCount >= 2)
			isPathogenic = true;
		
		//1 Strong (PS1–PS4) AND
		if(psCount == 1)
		{
			//a. ≥3 Moderate (PM1–PM6) OR
		    //b. 2 Moderate (PM1–PM6) AND ≥2 Supporting (PP1–PP5) OR
		    //c. 1 Moderate (PM1–PM6) AND ≥4 Supporting (PP1–PP5)
			if(pmCount >= 3 || (pmCount == 2 && ppCount >= 2) || (pmCount == 1 && ppCount >= 4) )
				isPathogenic = true;
		}
		
		boolean isLikelyPathogenic = false;
		//1 Very Strong (PVS1) AND 1 Moderate (PM1–PM6)
		if(ACMG.get(PVS1).state == MET && pmCount == 1)
			isLikelyPathogenic = true;
		
		//1 Strong (PS1–PS4) AND 1–2 Moderate (PM1–PM6)
		if(psCount == 1 && (pmCount == 1 && pmCount == 2) )
			isLikelyPathogenic = true;
		
		//1 Strong (PS1–PS4) AND ≥2 Supporting (PP1–PP5)
		if(psCount == 1 && ppCount >= 2)
			isLikelyPathogenic = true;
		
		//≥3 Moderate (PM1–PM6) OR
		if(pmCount >= 3)
			isLikelyPathogenic = true;
		
		//2 Moderate (PM1–PM6) AND ≥2 Supporting (PP1–PP5)
		if(pmCount == 2 && ppCount >= 2)
			isLikelyPathogenic = true;
		
		//1 Moderate (PM1–PM6) AND ≥4 Supporting (PP1–PP5)
		if(pmCount == 1 && ppCount >= 4)
			isLikelyPathogenic = true;
		
		boolean isBenign = false;
		//1 Stand-Alone (BA1) OR
		//≥2 Strong (BS1–BS4)
		if(ACMG.get(BA1).state == MET || bsCount >= 2)	
			isBenign = true;
		
		boolean isLikelyBenign = false;
		//1 Strong (BS1–BS4) and 1 Supporting (BP1–BP7)
		if(bsCount == 1 && bpCount == 1)
			isLikelyBenign = true;
		
		//≥2 Supporting (BP1–BP7)
		if(bpCount >= 2)
			isLikelyBenign = true;
		
		
		//Variants should be classified as Uncertain Significance if other criteria are unmet or the criteria for benign and pathogenic are contradictory.
		if((isPathogenic || isLikelyPathogenic) && (isBenign || isLikelyPathogenic))
			return UNCERTAIN_SIGNIFICANCE;
		
		if(isPathogenic)
			return PATHOGENIC;
		if(isLikelyPathogenic)
			return LIKELY_PATHOGENIC;
		if(isBenign)
			return BENIGN;
		if(isLikelyBenign)
			return LIKELY_BENIGN;
		
		return UNCERTAIN_SIGNIFICANCE;
	}
	
	public static void main(String[] args) {
		
	}
}
