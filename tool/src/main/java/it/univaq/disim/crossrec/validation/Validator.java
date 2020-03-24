package it.univaq.disim.crossrec.validation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import it.univaq.disim.crossrec.DataReader;

class ValueComparator implements Comparator<String> {

	Map<String, Double> base;

	public ValueComparator(Map<String, Double> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with equals.
	public int compare(String a, String b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}

public class Validator {
	final static Logger logger = Logger.getLogger(Validator.class);

	private String srcDir;
	private String subFolder;
	private int numOfProjects;
	private int numOfLibraries;
	private int numOfEASEInput;
	private boolean bayesian;
	public Validator(String srcDir, boolean bayesian) {
		this.srcDir = srcDir;
		this.bayesian = bayesian;
		this.numOfLibraries = 20;
		this.numOfEASEInput = 5;
	}

	public void run() {
		logger.info("Ten-fold cross validation");
		DataReader reader = new DataReader(this.srcDir);
		String inputFile = "projects.txt";
		numOfProjects = reader.getNumberOfProjects(Paths.get(this.srcDir, inputFile).toString());
		computeEvaluationMetrics(inputFile);
	}

	public void computeEvaluationMetrics(String inputFile) {

		int step = (int) numOfProjects / 10;
		int cutOffValue = numOfLibraries;
		double recallRate = 0;
		Map<String, Double> Vals = new HashMap<String, Double>();
		String name = "EPC";

		for (int i = 0; i < 10; i++) {

			int trainingStartPos1 = 1;
			int trainingEndPos1 = i * step;
			int trainingStartPos2 = (i + 1) * step + 1;
			int trainingEndPos2 = numOfProjects;
			int testingStartPos = 1 + i * step;
			int testingEndPos = (i + 1) * step;
			int k = i + 1;
			subFolder = "Round" + Integer.toString(k);

			Metrics metrics = new Metrics(k, this.numOfLibraries, this.srcDir, this.subFolder, trainingStartPos1,
					trainingEndPos1, trainingStartPos2, trainingEndPos2, testingStartPos, testingEndPos);

//			this.groundTruth = this.srcDir + subFolder + "/" + "GroundTruth" + "/";
//			this.recDir = this.srcDir + subFolder + "/" + "Recommendations" + "/";
//			this.resDir = this.srcDir + "/" + "Results" + "/";
//			this.prDir = this.srcDir + subFolder + "/" + "PrecisionRecall" + "/";
//			this.simDir = this.srcDir + subFolder + "/";	

//			int numberOfNeighbours = 20;

//			logger.info("==============Catalog Coverage==============");					
//			metrics.CatalogCoverage();	
//						
//			logger.info("==============Entropy==============");
//			metrics.Entropy();
//			
//			
//			logger.info("==============EPC==============");
//			metrics.EPC();
//
//			logger.info("==============Long tail==============");
////			metrics.LongTail();			
//			metrics.nDCG();
			if (this.bayesian) {
				metrics.successRateB(numOfEASEInput);
				metrics.computeAverageSuccessRateB();
				metrics.precisionRecallB(numOfEASEInput);
				metrics.computeAveragePrecisionRecallB();
			}
			
			metrics.successRate();
			metrics.successRateN();
			metrics.precisionRecall();
			recallRate += metrics.recallRate();
			metrics.computeAveragePrecisionRecall();
			metrics.computeAverageSuccessRateN();
			metrics.computeAverageSuccessRate();
			logger.info("Average success rate: " + recallRate / 10);

			logger.info("==============Catalog Coverage==============");
			metrics.CatalogCoverage();

			logger.info("==============Entropy==============");
			metrics.entropy();

			logger.info("==============EPC==============");
			metrics.EPC();
//
			logger.info("==============Long tail==============");
////			metrics.LongTail();			
			metrics.nDCG();

//			metrics.MeanAbsoluteError();

//			FScore.putAll(metrics.getFScores(cutOffValue));			
//			metrics.getPrecisionRecallScores(cutOffValue, Recall, Precision);

			Vals.putAll(metrics.getSomeScores(cutOffValue, name));
			name = "Entropy";
			Vals.putAll(metrics.getSomeScores(cutOffValue, name));
			name = "Entropy";
			Vals.putAll(metrics.getSomeScores(cutOffValue, name));

//			logger.info("==============Entropy-based Novelty==============");
//			EBN(testingStartPos, testingEndPos);

		}

		Set<String> keySet = Vals.keySet();
		String resDir = this.srcDir + "/" + "Results" + "/";
		String tmp = resDir + name + "@" + Integer.toString(cutOffValue);
		double score = 0;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));

			for (String key : keySet) {
				score = Vals.get(key);
				String content = Double.toString(score);
				writer.append(content);
				writer.newLine();
				writer.flush();
			}
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}
}
