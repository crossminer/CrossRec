package it.univaq.disim.crossrec.validation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import it.univaq.disim.crossrec.DataReader;

public class Metrics {
	final static Logger logger = Logger.getLogger(Metrics.class);

	private String srcDir;
	private String groundTruth;
	private String recDir;
	private String resDir;
	private String prDir;
	private String prDirB;
	private String succesRateDir;
	private String succesRateDirB;
	private String succesRateDirN;
	private String fsDir;

	private int fold;
	private int numLibs;
	private int trainingStartPos1;
	private int trainingEndPos1;
	private int trainingStartPos2;
	private int trainingEndPos2;
	private int testingStartPos;
	private int testingEndPos;
	private DataReader reader;
	private Map<Integer, String> testingProjects;

	public Metrics(int k, int numLibs, String srcDir, String subFolder, int trStartPos1, int trEndPos1, int trStartPos2,
			int trEndPos2, int teStartPos, int teEndPos) {

		this.fold = k;
		this.numLibs = numLibs;
		this.srcDir = srcDir;
		this.groundTruth = Paths.get(this.srcDir, subFolder, "GroundTruth").toString();
		this.recDir = Paths.get(this.srcDir, subFolder, "Recommendations").toString();
		this.prDir = Paths.get(this.srcDir, subFolder, "PrecisionRecall").toString();
		this.prDirB = Paths.get(this.srcDir, subFolder, "PrecisionRecallB").toString();
		this.succesRateDir = Paths.get(this.srcDir, subFolder, "SuccesRate").toString();
		this.succesRateDirB = Paths.get(this.srcDir, subFolder, "SuccesRateB").toString();
		this.succesRateDirN = Paths.get(this.srcDir, subFolder, "SuccesRateN").toString();
		this.fsDir = Paths.get(this.srcDir, subFolder, "FScore" + "/").toString();
		this.resDir = Paths.get(this.srcDir, "Results").toString();
		this.reader = new DataReader(this.srcDir);
		this.trainingStartPos1 = trStartPos1;
		this.trainingEndPos1 = trEndPos1;
		this.trainingStartPos2 = trStartPos2;
		this.trainingEndPos2 = trEndPos2;
		this.testingStartPos = teStartPos;
		this.testingEndPos = teEndPos;
		testingProjects = reader.readProjectList(Paths.get(this.srcDir, "projects.txt").toString(),
				this.testingStartPos, this.testingEndPos);
	}

	public void meanAbsoluteError() {

		Set<Integer> keyTestingProjects = testingProjects.keySet();

		Map<String, Double> recommendations = new HashMap<String, Double>();
		Map<String, Double> groundtruth = new HashMap<String, Double>();

		Map<String, Double> results = new HashMap<String, Double>();

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String tmp = this.recDir + filename;
			recommendations = reader.readRecommendationScores(tmp);
			tmp = this.groundTruth + filename;
			groundtruth = reader.readGroundTruthScore(tmp);

			Set<String> keySet = groundtruth.keySet();

			double gScore = 0;
			double rScore = 0;
			double Score = 0;

			for (String key : keySet) {

				gScore = groundtruth.get(key);

				if (recommendations.containsKey(key)) {
					rScore = recommendations.get(key);
				} else
					rScore = 0;

				double temp = Math.abs(gScore - rScore);
				Score += temp;
			}

			if (groundtruth.size() != 0) {
				Score = (double) Score / groundtruth.size();
				results.put(Integer.toString(keyTesting), Score);
				logger.info(testingPro + " \t" + Score);
			}

		}

		String tmp2 = this.resDir + "MAE" + "_Round" + Integer.toString(fold);

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmp2));

			for (Double d : results.values()) {
				writer.append(Double.toString(d));
				writer.newLine();
				writer.flush();
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}

	public double recallRate() {

		Set<Integer> keyTestingProjects = testingProjects.keySet();

		/* Select top libraries */

		Set<String> recommendationFile = null;
		Set<String> groundTruthFile = null;
		int count = 0;

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String tmp = Paths.get(this.recDir, filename).toString();
			recommendationFile = reader.readRecommendationFile(tmp, numLibs);
			tmp = Paths.get(this.groundTruth, filename).toString();
			groundTruthFile = reader.readGroundTruthFile(tmp);
			Set<String> common = Sets.intersection(recommendationFile, groundTruthFile);
			int size = common.size();
			if (size == 0)
				count += 1;
		}

		String tmp2 = Paths.get(this.resDir, "Recall_Round" + Integer.toString(fold)).toString();

		int total = keyTestingProjects.size();
		double recallRate = (double) (total - count) / total;

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmp2));
			logger.info(recallRate);
			writer.append(Double.toString(recallRate));
			writer.newLine();
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmp2))) {
			bw.write(Double.toHexString(recallRate));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return recallRate;
	}

	public void successRate() {

		Set<Integer> keyTestingProjects = testingProjects.keySet();

		/* Select top libraries */

		Map<Integer, String> recommendationFile = null;
		Set<String> groundTruthFile = null;
		Set<String> temp = null;
		Set<Integer> keySet = null;

		for (Integer keyTesting : keyTestingProjects) {

			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String groundTruthData = Paths.get(this.recDir, filename).toString();

			recommendationFile = reader.readRecommendationFile(groundTruthData);
			groundTruthData = Paths.get(this.groundTruth, filename).toString();
			groundTruthFile = reader.readGroundTruthFile(groundTruthData);

			keySet = recommendationFile.keySet();
			int size = 0;
			int count = 1;
			double f_score = 0;

			temp = new HashSet<String>();

			try {
				File successRateFolder = new File(this.succesRateDir);
				if (!successRateFolder.exists())
					successRateFolder.mkdir();
				String successRatePath = Paths.get(this.succesRateDir, filename).toString();
				BufferedWriter writer = new BufferedWriter(new FileWriter(successRatePath));
//				BufferedWriter writer2 = new BufferedWriter(new FileWriter(tmp2));

				for (Integer key : keySet) {
					temp.add(recommendationFile.get(key));

					Set<String> common = Sets.intersection(temp, groundTruthFile);
					size = common.size();
					String content = key + "\t";
					if (size == 0)
						content = content + "0";
					else
						content = content + "1";
					writer.append(content);
					writer.newLine();
					writer.flush();
					content = key + "\t" + f_score;
					count++;
					if (count > numLibs)
						break;
				}
				writer.close();
//				writer2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return;
	}

	public void successRateB(int numberOfTopicsFromEASE) {

		Set<Integer> keyTestingProjects = testingProjects.keySet();

		/* Select top libraries */

		Map<Integer, String> recommendationData = null;
		Set<String> groundTruthData = null;
		Set<Integer> keySet = null;

		for (Integer keyTesting : keyTestingProjects) {

			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String recFile = Paths.get(this.recDir, filename).toString();
			List<String> eASETopic = reader.getEASETopic(testingPro, numberOfTopicsFromEASE);
			recommendationData = reader.readRecommendationFile(recFile);
			String groundTruthFile = Paths.get(this.groundTruth, filename).toString();
			groundTruthData = reader.readGroundTruthFile(groundTruthFile);

			keySet = recommendationData.keySet();
			int size = 0;
			int count = 1;
			double f_score = 0;

			Set<String> temp = new HashSet<String>();
			eASETopic.forEach(z -> temp.add(z));
			File successRateFolder = new File(this.succesRateDirB);
			if (!successRateFolder.exists())
				successRateFolder.mkdir();
			String successRatePath = Paths.get(this.succesRateDirB, filename).toString();
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(successRatePath))) {
				int i = 1;
				Set<String> tempissiomo = Sets.newHashSet();
				for (String element : eASETopic) {
					tempissiomo.add(element);
					Set<String> common = Sets.intersection(tempissiomo, groundTruthData);
					size = common.size();
					String content = i + "\t";
					if (size == 0)
						content = content + "0";
					else
						content = content + "1";
					writer.append(content);
					writer.newLine();
					writer.flush();
					i++;

				}

				for (Integer key : keySet) {
					temp.add(recommendationData.get(key));

					Set<String> common = Sets.intersection(temp, groundTruthData);
					size = common.size();
					String content = (key + numberOfTopicsFromEASE) + "\t";
					if (size == 0)
						content = content + "0";
					else
						content = content + "1";
					writer.append(content);
					writer.newLine();
					writer.flush();
					content = key + "\t" + f_score;
					count++;
					if (count > numLibs - numberOfTopicsFromEASE)
						break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return;
	}

	public void successRateN() {

		Set<Integer> keyTestingProjects = testingProjects.keySet();

		/* Select top libraries */

		Map<Integer, String> recommendationFile = null;
		Set<String> groundTruthFile = null;
		Set<String> temp = null;
		Set<Integer> keySet = null;

		for (Integer keyTesting : keyTestingProjects) {

			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String groundTruthData = Paths.get(this.recDir, filename).toString();

			recommendationFile = reader.readRecommendationFile(groundTruthData);
			groundTruthData = Paths.get(this.groundTruth, filename).toString();
			groundTruthFile = reader.readGroundTruthFile(groundTruthData);

			keySet = recommendationFile.keySet();
			int size = 0;
			int count = 1;
			double f_score = 0;

			temp = new HashSet<String>();

			try {
				File successRateFolder = new File(this.succesRateDirN);
				if (!successRateFolder.exists())
					successRateFolder.mkdir();
				String successRatePath = Paths.get(this.succesRateDirN, filename).toString();
				BufferedWriter writer = new BufferedWriter(new FileWriter(successRatePath));
				for (Integer key : keySet) {
					temp.add(recommendationFile.get(key));

					Set<String> common = Sets.intersection(temp, groundTruthFile);
					size = common.size();
					String content = key + "\t";
					content = content + size;
					writer.append(content);
					writer.newLine();
					writer.flush();
					content = key + "\t" + f_score;
					count++;
					if (count > numLibs)
						break;
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return;
	}
	/*
	 * 
	 */

	public void precisionRecall() {
		Set<Integer> keyTestingProjects = testingProjects.keySet();
		/* Select top libraries */
		Map<Integer, String> recommendationFile = null;
		Set<String> groundTruthFile = null;
		Set<String> temp = null;
		Set<Integer> keySet = null;
		double precision = 0, recall = 0;
		int totalOfRelevant = 0;
		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String tmp = Paths.get(this.recDir, filename).toString();
			recommendationFile = reader.readRecommendationFile(tmp);
			tmp = Paths.get(this.groundTruth, filename).toString();
			groundTruthFile = reader.readGroundTruthFile(tmp);
			totalOfRelevant = groundTruthFile.size();
			keySet = recommendationFile.keySet();
			int size = 0;
			int count = 1;
			double f_score = 0;
			temp = new HashSet<String>();
			try {
				tmp = Paths.get(this.prDir, filename).toString();
				BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));
				for (Integer key : keySet) {
					temp.add(recommendationFile.get(key));

					Set<String> common = Sets.intersection(temp, groundTruthFile);
					size = common.size();
					precision = 0;
					recall = 0;
					if (key != 0) {
						precision = (double) size / key;
					}
					if (totalOfRelevant != 0) {
						recall = (double) size / totalOfRelevant;
					}
					double val1 = 2 * recall * precision;
					double val2 = recall + precision;
					if (val1 != 0 && val2 != 0)
						f_score = (2 * recall * precision) / (recall + precision);
					else
						f_score = 0;
					String content = key + "\t" + recall + "\t" + precision;
					writer.append(content);
					writer.newLine();
					writer.flush();
					content = key + "\t" + f_score;
					count++;
					if (count > numLibs)
						break;
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return;
	}
	
	public void precisionRecallB(int numberOfTopicsFromEASE) {
		Set<Integer> keyTestingProjects = testingProjects.keySet();
		/* Select top libraries */
		Map<Integer, String> recommendationFile = null;
		Set<String> groundTruthData = null;
		Set<Integer> keySet = null;
		double precision = 0, recall = 0;
		int totalOfRelevant = 0;
		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String tmp = Paths.get(this.recDir, filename).toString();
			recommendationFile = reader.readRecommendationFile(tmp);
			tmp = Paths.get(this.groundTruth, filename).toString();
			groundTruthData = reader.readGroundTruthFile(tmp);
			totalOfRelevant = groundTruthData.size();
			List<String> eASETopic = reader.getEASETopic(testingPro, numberOfTopicsFromEASE);
			keySet = recommendationFile.keySet();
			int size = 0;
			int count = 1;
			Set<String> temp = Sets.newHashSet();
			eASETopic.forEach(z -> temp.add(z));
			tmp = Paths.get(this.prDirB, filename).toString();
			File successRateFolder = new File(this.prDirB);
			if (!successRateFolder.exists())
				successRateFolder.mkdir();
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmp))){
				/*
				 * 
				 */
				int i = 1;
				Set<String> tempissiomo = Sets.newHashSet();
				for (String element : eASETopic) {
					tempissiomo.add(element);
					Set<String> common = Sets.intersection(tempissiomo, groundTruthData);
					size = common.size();
					precision = 0;
					recall = 0;
					if (i != 0) {
						precision = (double) size / i;
					}
					if (totalOfRelevant != 0) {
						recall = (double) size / totalOfRelevant;
					}
					String content = i + "\t" + recall + "\t" + precision;
					writer.append(content);
					writer.newLine();
					writer.flush();
					i++;

				}
				
				/*
				 * 
				 */
				
				for (Integer key : keySet) {
					temp.add(recommendationFile.get(key));

					Set<String> common = Sets.intersection(temp, groundTruthData);
					size = common.size();
					precision = 0;
					recall = 0;
					if (key != 0) {
						precision = (double) size / (key + numberOfTopicsFromEASE);
					}
					if (totalOfRelevant != 0) {
						recall = (double) size / totalOfRelevant;
					}
					String content = (key + numberOfTopicsFromEASE) + "\t" + recall + "\t" + precision;
					writer.append(content);
					writer.newLine();
					writer.flush();
					count++;
					if (count > numLibs - numberOfTopicsFromEASE)
						break;
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return;
	}

	public void computeAveragePrecisionRecall() {
		Set<Integer> keyTestingProjects = testingProjects.keySet();

		double precision = 0, recall = 0;
		double val1 = 0, val2 = 0;
		String tmp = "";

		Map<Integer, Double> Precision = new HashMap<Integer, Double>();
		Map<Integer, Double> Recall = new HashMap<Integer, Double>();

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");

			try {

				tmp = Paths.get(this.prDir, filename).toString();
				String line = null;
				String[] vals = null;
				int id = 1;
				BufferedReader bufread = new BufferedReader(new FileReader(tmp));
				while ((line = bufread.readLine()) != null) {
					vals = line.split("\t");
					recall = Double.parseDouble(vals[1].trim());
					precision = Double.parseDouble(vals[2].trim());
					if (Precision.containsKey(id)) {
						val1 = Recall.get(id) + recall;
						;
						val2 = Precision.get(id) + precision;
					} else {
						val1 = recall;
						val2 = precision;
					}
					Recall.put(id, val1);
					Precision.put(id, val2);
					id++;
					if (id > numLibs)
						break;
				}

				bufread.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		double f_score = 0;
		Set<Integer> keySet = Precision.keySet();
		int size = testingProjects.size();
		tmp = Paths.get(this.resDir, "PRC" + "_Round" + Integer.toString(fold)).toString();

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmp))) {
			for (Integer key : keySet) {
				precision = 0;
				recall = 0;
				if (size != 0) {
					recall = Recall.get(key) / size;
					precision = Precision.get(key) / size;
				}

				String content = key + "\t" + recall + "\t" + precision;
				writer.append(content);
				writer.newLine();
				writer.flush();
				content = key + "\t" + f_score;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}

	
	public void computeAveragePrecisionRecallB() {
		Set<Integer> keyTestingProjects = testingProjects.keySet();

		double precision = 0, recall = 0;
		double val1 = 0, val2 = 0;
		String tmp = "";

		Map<Integer, Double> Precision = new HashMap<Integer, Double>();
		Map<Integer, Double> Recall = new HashMap<Integer, Double>();

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");

			tmp = Paths.get(this.prDirB, filename).toString();
			try (BufferedReader bufread = new BufferedReader(new FileReader(tmp))){
				String line = null;
				String[] vals = null;
				int id = 1;
				while ((line = bufread.readLine()) != null) {
					vals = line.split("\t");
					recall = Double.parseDouble(vals[1].trim());
					precision = Double.parseDouble(vals[2].trim());
					if (Precision.containsKey(id)) {
						val1 = Recall.get(id) + recall;
						val2 = Precision.get(id) + precision;
					} else {
						val1 = recall;
						val2 = precision;
					}
					Recall.put(id, val1);
					Precision.put(id, val2);
					id++;
					if (id > numLibs)
						break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		double f_score = 0;
		Set<Integer> keySet = Precision.keySet();
		int size = testingProjects.size();
		tmp = Paths.get(this.resDir, "PRCB" + "_Round" + Integer.toString(fold)).toString();

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmp))) {
			for (Integer key : keySet) {
				precision = 0;
				recall = 0;
				if (size != 0) {
					recall = Recall.get(key) / size;
					precision = Precision.get(key) / size;
				}

				String content = key + "\t" + recall + "\t" + precision;
				writer.append(content);
				writer.newLine();
				writer.flush();
				content = key + "\t" + f_score;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}

	private Map<Integer, Double> initSRMap() {
		Map<Integer, Double> result = Maps.newHashMap();
		for (int i = 1; i < 21; i++) {
			result.put(i, 0.0);
		}
		return result;
	}

	public void computeAverageSuccessRateN() {
		Map<Integer, String> testingProjects = new HashMap<Integer, String>();
		Set<Integer> keyTestingProjects = testingProjects.keySet();

		String tmp = "";

		Map<Integer, Double> successRateMap1 = initSRMap();
		Map<Integer, Double> successRateMap2 = initSRMap();
		Map<Integer, Double> successRateMap3 = initSRMap();
		Map<Integer, Double> successRateMap4 = initSRMap();

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			tmp = Paths.get(this.succesRateDirN, filename).toString();
			try (BufferedReader bufread = new BufferedReader(new FileReader(tmp))) {
				String line = null;
				String[] vals = null;
				int id = 1;

				while ((line = bufread.readLine()) != null) {
					double sr1 = successRateMap1.get(id), sr2 = successRateMap2.get(id), sr3 = successRateMap3.get(id),
							sr4 = successRateMap4.get(id);
					double successRate1 = 0;
					vals = line.split("\t");
					successRate1 = Double.parseDouble(vals[1].trim());
					if (successRateMap1.containsKey(id)) {
						if (successRate1 == 1)
							sr1 = successRateMap1.get(id) + 1;

						if (successRate1 > 1 & successRate1 < 3) {
							sr1 = successRateMap1.get(id) + 1;
							sr2 = successRateMap2.get(id) + 1;

						}
						if (successRate1 > 2 & successRate1 < 4) {
							sr1 = successRateMap1.get(id) + 1;
							sr2 = successRateMap2.get(id) + 1;
							sr3 = successRateMap3.get(id) + 1;
						}
						if (successRate1 > 3) {
							sr1 = successRateMap1.get(id) + 1;
							sr2 = successRateMap2.get(id) + 1;
							sr3 = successRateMap3.get(id) + 1;
							sr4 = successRateMap4.get(id) + 1;
						}

					}
					successRateMap1.put(id, sr1);
					successRateMap2.put(id, sr2);
					successRateMap3.put(id, sr3);
					successRateMap4.put(id, sr4);
					id++;
					if (id > numLibs)
						break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		double f_score = 0;
		Set<Integer> keySet = successRateMap1.keySet();
		int size = testingProjects.size();
		tmp = Paths.get(this.resDir, "SR_STAR" + "_Round" + Integer.toString(fold)).toString();

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmp))) {

			for (Integer key : keySet) {
				double successRate1 = 0;
				double successRate2 = 0;
				double successRate3 = 0;
				double successRate4 = 0;
				if (size != 0) {
					successRate1 = successRateMap1.get(key) / size;
					successRate2 = successRateMap2.get(key) / size;
					successRate3 = successRateMap3.get(key) / size;
					successRate4 = successRateMap4.get(key) / size;
				}
				String content = String.format("%s\t%.03f\t%.03f\t%.03f\t%.03f	", key, successRate1, successRate2,
						successRate3, successRate4);
				writer.append(content);
				writer.newLine();
				writer.flush();
				content = key + "\t" + f_score;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}
	
	public void computeAverageSuccessRateB() {
		Set<Integer> keyTestingProjects = testingProjects.keySet();

		double successRate = 0;
		double val2 = 0;

		Map<Integer, Double> successRateMap = new HashMap<Integer, Double>();

		for (Integer keyTesting : keyTestingProjects) {

			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String successRateFile = Paths.get(this.succesRateDirB, filename).toString();
			try (BufferedReader bufread = new BufferedReader(new FileReader(successRateFile))) {
				String line = null;
				String[] vals = null;
				int id = 1;

				while ((line = bufread.readLine()) != null) {
					vals = line.split("\t");
					successRate = Double.parseDouble(vals[1].trim());
					if (successRateMap.containsKey(id))
						val2 = successRateMap.get(id) + successRate;
					else val2 = successRate;
					successRateMap.put(id, val2);
					id++;
					if (id > numLibs)
						break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		double f_score = 0;
		Set<Integer> keySet = successRateMap.keySet();
		int size = testingProjects.size();
		String successRateAgrregatedResult = Paths.get(this.resDir, "SRB" + "_Round" + Integer.toString(fold)).toString();

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(successRateAgrregatedResult));
			for (Integer key : keySet) {
				successRate = 0;
				if (size != 0) {
					successRate = successRateMap.get(key) / size;
				}

				String content = key + "\t" + successRate + "\t";
				writer.append(content);
				writer.newLine();
				writer.flush();
				content = key + "\t" + f_score;
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}

	public void computeAverageSuccessRate() {
		Set<Integer> keyTestingProjects = testingProjects.keySet();

		double successRate = 0;
		double val2 = 0;
		String tmp = "";

		Map<Integer, Double> successRateMap = new HashMap<Integer, Double>();

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			tmp = Paths.get(this.succesRateDir, filename).toString();
			try (BufferedReader bufread = new BufferedReader(new FileReader(tmp))) {
				String line = null;
				String[] vals = null;
				int id = 1;

				while ((line = bufread.readLine()) != null) {
					vals = line.split("\t");
					successRate = Double.parseDouble(vals[1].trim());
					if (successRateMap.containsKey(id))
						val2 = successRateMap.get(id) + successRate;
					successRateMap.put(id, val2);
					id++;
					if (id > numLibs)
						break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		double f_score = 0;
		Set<Integer> keySet = successRateMap.keySet();
		int size = testingProjects.size();
		tmp = Paths.get(this.resDir, "SR" + "_Round" + Integer.toString(fold)).toString();

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));
			for (Integer key : keySet) {
				successRate = 0;
				if (size != 0) {
					successRate = successRateMap.get(key) / size;
				}

				String content = key + "\t" + successRate + "\t";
				writer.append(content);
				writer.newLine();
				writer.flush();
				content = key + "\t" + f_score;
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}

	public Map<String, Double> getFScores(int cutOffValue) {
		Set<Integer> keyTestingProjects = testingProjects.keySet();

		double f_score = 0;
		String tmp = "";

		Map<String, Double> FScore = new HashMap<String, Double>();

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");

			/*
			 * read the FScore folder to get all the corresponding f-scores of the testing
			 * projects
			 */

			try {
				tmp = this.fsDir + filename;
				String line = null;
				String[] vals = null;
				int id = 1;
				BufferedReader bufread = new BufferedReader(new FileReader(tmp));
				while ((line = bufread.readLine()) != null) {
					if (id == cutOffValue) {
						vals = line.split("\t");
						f_score = Double.parseDouble(vals[1].trim());
						FScore.put(filename, f_score);
						break;
					}
					id++;
				}
				bufread.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return FScore;
	}

	public void getPrecisionRecallScores(int cutOffValue, Map<String, Double> Recall, Map<String, Double> Precision) {
		Set<Integer> keyTestingProjects = testingProjects.keySet();

		double precision = 0, recall = 0;
		String tmp = "";

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");

			/*
			 * read the FScore folder to get all the corresponding f-scores of the testing
			 * projects
			 */

			try {
				tmp = this.prDir + filename;
				String line = null;
				String[] vals = null;
				int id = 1;

				BufferedReader bufread = new BufferedReader(new FileReader(tmp));
				while ((line = bufread.readLine()) != null) {
					if (id == cutOffValue) {
						vals = line.split("\t");
						recall = Double.parseDouble(vals[1].trim());
						precision = Double.parseDouble(vals[2].trim());
						Recall.put(filename, recall);
						Precision.put(filename, precision);
						break;
					}
					id++;
				}
				bufread.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return;
	}

	public Map<String, Double> getSomeScores(int cutOffValue, String name) {

		double val = 0;
		String tmp = "";
		Map<String, Double> Vals = new HashMap<String, Double>();

		try {
			tmp = Paths.get(this.resDir, name + "_Round" + Integer.toString(fold)).toString();
			String line = null;
			int id = 1;

			BufferedReader bufread = new BufferedReader(new FileReader(tmp));
			while ((line = bufread.readLine()) != null) {
				if (id == cutOffValue) {
					val = Double.parseDouble(line.trim());
					logger.info(val);
					Vals.put(tmp, val);
					break;
				}
				id++;
			}
			bufread.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Vals;
	}

	public Set<String> getAllItems() {
		Set<String> allItems = new HashSet<String>();
		String trainingDictFilename = "";
		String trainingPro = "", filename = "";

		Map<Integer, String> trainingProjects = reader
				.readProjectList(Paths.get(this.srcDir, "projects.txt").toString(), trainingStartPos1, trainingEndPos1);
		if (trainingStartPos2 != 0 && trainingEndPos2 != 0) {
			Map<Integer, String> tempoProjects = reader.readProjectList(
					Paths.get(this.srcDir, "projects.txt").toString(), trainingStartPos2, trainingEndPos2);
			trainingProjects.putAll(tempoProjects);
		}
		Set<Integer> keyTrainingProjects = trainingProjects.keySet();
		for (Integer keyTraining : keyTrainingProjects) {
			trainingPro = trainingProjects.get(keyTraining);
			filename = trainingPro.replace("git://github.com/", "").replace("/", "__");
			trainingDictFilename = Paths.get(this.srcDir, "dicth_" + filename).toString();
			allItems.addAll(reader.getLibraries(trainingDictFilename));
		}
		return allItems;
	}

	public void CatalogCoverage() {

		Map<Integer, Map<Integer, String>> map = new HashMap<Integer, Map<Integer, String>>();

		Set<Integer> keyTestingProjects = testingProjects.keySet();
		Set<String> allItems = new HashSet<String>();

		String content = "", filename = "";
		allItems = getAllItems();
		/* Select top libraries */
		Map<Integer, String> recommendations = null;
		Set<Integer> keySet = null;
		int size = 0;

		Map<Integer, Set<String>> topN = new HashMap<Integer, Set<String>>();

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String str = Paths.get(this.recDir, filename).toString();
			recommendations = reader.readAllRecommendations(str);
			map.put(keyTesting, recommendations);
		}

		Set<String> strSet = null;

		for (Integer keyTesting : keyTestingProjects) {
			recommendations = map.get(keyTesting);
			keySet = recommendations.keySet();
			int count = 1;

			for (Integer key : keySet) {
				content = recommendations.get(key);
				if (topN.containsKey(key))
					strSet = topN.get(key);
				else
					strSet = new HashSet<String>();
				strSet.add(content);
				topN.put(key, strSet);
				count++;
				if (count > numLibs)
					break;
			}
		}

		keySet = topN.keySet();
		List<Integer> sortedList = new ArrayList<Integer>(keySet);
		Collections.sort(sortedList);
		Set<String> strSet1 = new HashSet<String>();
		Set<String> strSet2 = new HashSet<String>();
		size = sortedList.size();

		for (int i = 0; i < size; i++) {
			int index = sortedList.get(i);
			/* get the content of the previous rank */
			if (index > 1) {
				strSet1 = topN.get(index - 1);
				strSet2 = topN.get(index);
				strSet2.addAll(strSet1);
				topN.put(index, strSet2);
			}
		}

		int size2 = allItems.size();
		logger.info("The number of items available: " + size2);
		String tmp2 = Paths.get(this.resDir, "Catalog" + "_Round" + Integer.toString(fold)).toString();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmp2));
			for (int i = 0; i < size; i++) {
				int index = sortedList.get(i);
				double coverage = (double) 100 * topN.get(index).size() / size2;
				logger.info(coverage);
				writer.append(Double.toString(coverage));
				writer.newLine();
				writer.flush();
				if (i == numLibs)
					break;
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

	/* Compute the long tail items */

	public double longTail(int N, Set<String> longTailItems, Map<String, Map<Integer, String>> rec) {

		Set<String> testPros = rec.keySet();
		Map<String, Integer> allRecs = new HashMap<String, Integer>();
		Set<String> allItems = new HashSet<String>();
		int total = 0;

		for (String project : testPros) {
			Map<Integer, String> recommendations = new HashMap<Integer, String>();
			Map<Integer, String> tmp = rec.get(project);
			Set<Integer> keySet = tmp.keySet();

			/* get the top-N libraries */
			for (int i = 1; i <= N; i++)
				recommendations.put(i, tmp.get(i));
			keySet = recommendations.keySet();
			for (Integer key : keySet) {
				String lib = recommendations.get(key);
				int val = 0;
				if (allRecs.containsKey(lib))
					val = allRecs.get(lib) + 1;
				else
					val = 1;
				allRecs.put(lib, val);
				allItems.add(lib);
				total++;
			}
		}

		logger.info("total is: " + total);

		int count = 0;
		Set<String> keySet2 = allRecs.keySet();
		for (String key : keySet2) {
			if (longTailItems.contains(key))
				count += allRecs.get(key);
		}
		double longtail = (double) count / total;
		return longtail;
	}

	public void longTail() {

		Set<Integer> keyTestingProjects = testingProjects.keySet();
		Set<String> longTailItems = reader.readLongTailItems("/home/utente/Documents/Journals/EMSE/Longtail.txt");

		/* Select top libraries */
		Map<Integer, String> recommendations = null;
		Map<String, Map<Integer, String>> rec = new HashMap<String, Map<Integer, String>>();

		/* Read all files here and provide input for the LongTail() function */

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String str = this.recDir + filename;
			recommendations = reader.readAllRecommendations(str);
			rec.put(filename, recommendations);
		}

		String tmp = this.resDir + "LongTail" + "_Round" + Integer.toString(fold);

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));

			for (int i = 1; i <= numLibs; i++) {
				double longtail = longTail(i, longTailItems, rec);
				writer.append(Double.toString(longtail));
				writer.newLine();
				writer.flush();
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* Compute nDCG */

	public double nDCG(int N, Map<String, Map<Integer, String>> rec, Map<String, Set<String>> gt) {
		Set<String> testPros = rec.keySet();
		Set<String> groundTruth = null;
		int NDCG = 0;

		for (String project : testPros) {
			double nDCG = 0, iDCG = 0;
			Map<Integer, Integer> relevances = new HashMap<Integer, Integer>();
			Map<Integer, Integer> idealRelevances = new HashMap<Integer, Integer>();

			Map<Integer, String> tmp = rec.get(project);
			groundTruth = gt.get(project);

			/* get the relevance of the top-N libraries */

			for (int i = 1; i <= N; i++) {
				if (groundTruth.contains(tmp.get(i))) {
					relevances.put(i, 1);
				} else
					relevances.put(i, 0);
			}

			int count = 0;

			for (int i = 1; i <= N; i++) {
				int rel = relevances.get(i);
				if (rel == 1)
					count++;
				double val = Math.log(i + 1) / Math.log(2);
				nDCG += (double) rel / val;
			}

			for (int i = 1; i <= count; i++) {
				idealRelevances.put(i, 1);
			}

			for (int i = count + 1; i <= N; i++) {
				idealRelevances.put(i, 0);
			}

			for (int i = 1; i <= N; i++) {
				int rel = idealRelevances.get(i);
				double val = Math.log(i + 1) / Math.log(2);
				if (rel != 0 && val != 0)
					iDCG += (double) rel / val;
			}
			double ret = 0;
			if (nDCG != 0 && iDCG != 0)
				ret = (double) nDCG / iDCG;
			else
				ret = 0;
			NDCG += (double) ret;
		}

		/* get the average nDCG score for all testing projects */
		return (double) NDCG / testPros.size();
	}

	/* compute normalized Discounted Cummulative Gain nDCG */

	public void nDCG() {

		Set<Integer> keyTestingProjects = testingProjects.keySet();

		/* Select top libraries */
		Map<Integer, String> recommendations = null;
		Map<String, Map<Integer, String>> rec = new HashMap<String, Map<Integer, String>>();
		Set<String> groundTruth = new HashSet<String>();
		Map<String, Set<String>> gt = new HashMap<String, Set<String>>();

		/* Read all files here and provide input for the LongTail() function */

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String str = Paths.get(this.recDir, filename).toString();
			recommendations = reader.readAllRecommendations(str);
			rec.put(filename, recommendations);
			str = Paths.get(this.groundTruth, filename).toString();
			groundTruth = reader.readGroundTruthFile(str);
			gt.put(filename, groundTruth);
		}

		String tmp = Paths.get(this.resDir, "nDCG" + "_Round" + Integer.toString(fold)).toString();

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));

			for (int i = 1; i <= numLibs; i++) {
				double ndcg = nDCG(i, rec, gt);
				writer.append(Double.toString(ndcg));
				writer.newLine();
				writer.flush();
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

	public double entropy(Set<String> allItems, Map<Integer, Map<Integer, String>> rec, int N) {

		Map<Integer, String> recommendations = null;
		Set<Integer> keySet = rec.keySet();
		Map<Integer, Set<String>> allRecs = new HashMap<Integer, Set<String>>();
		Map<String, Double> itemFreq = new HashMap<String, Double>();

		int total = 0;

		for (Integer key : keySet) {
			/* Check if item is recommended to a test project */
			recommendations = rec.get(key);

			Set<Integer> keySet2 = recommendations.keySet();
			Set<String> set = new HashSet<String>();
			int count2 = 0;
			for (Integer key2 : keySet2) {
				set.add(recommendations.get(key2));
				count2++;
				if (count2 == N)
					break;
			}
			total += count2;
			allRecs.put(key, set);
		}

		keySet = allRecs.keySet();

		for (String item : allItems) {
			int count = 0;
			for (Integer key : keySet) {
				Set<String> set = allRecs.get(key);
				if (set.contains(item))
					count++;
			}
			itemFreq.put(item, (double) count);
		}

		double entropy = 0;

		for (String item : allItems) {
			double val = (double) itemFreq.get(item) / total;
			if (val != 0)
				entropy += -val * Math.log(val);
		}

		return entropy;
	}

	/* Entropy: Main function */

	public void entropy() {

		Set<Integer> keyTestingProjects = testingProjects.keySet();
		Set<String> allItems = getAllItems();
		String filename = "";
		Map<Integer, String> recommendations = null;
		/* the overall number of top-N recommendations across all users */
		Map<Integer, Map<Integer, String>> rec = new HashMap<Integer, Map<Integer, String>>();

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String str = Paths.get(this.recDir, filename).toString();
			recommendations = reader.readAllRecommendations(str);
			rec.put(keyTesting, recommendations);
		}

		double entropy = 0;
		String content = "";

		String tmp2 = Paths.get(this.resDir, "Entropy" + "_Round" + Integer.toString(fold)).toString();

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmp2));

			for (int i = 1; i <= numLibs; i++) {
				entropy = entropy(allItems, rec, i);
				content = Double.toString(entropy);
				writer.append(content);
				writer.newLine();
				writer.flush();
				logger.info(entropy);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// entropy = Entropy(allItems, rec, 50, total);

		return;
	}

	public void EPC() {
		Set<Integer> keyTestingProjects = testingProjects.keySet();
		/* Select top libraries */
		Map<Integer, String> recommendations = null;
		Map<String, Map<Integer, String>> rec = new HashMap<String, Map<Integer, String>>();
		/* Read all files here and provide input for the LongTail() function */
		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String str = Paths.get(this.recDir, filename).toString();
			recommendations = reader.readAllRecommendations(str);
			rec.put(filename, recommendations);
		}

		String tmp = Paths.get(this.resDir, "EPC" + "_Round" + Integer.toString(fold)).toString();

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));

			for (int i = 1; i <= numLibs; i++) {
				double longtail = EPC(i, rec);
				writer.append(Double.toString(longtail));
				writer.newLine();
				writer.flush();
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public double EPC(int N, Map<String, Map<Integer, String>> rec) {

		Set<String> groundTruthFile = null;
		double val1 = 0, val2 = 0;
		Map<String, Double> pop = popularity();
		List<Integer> sortedList = null;
		int size = 0;

		Set<String> testPros = rec.keySet();

		for (String project : testPros) {
			String str = Paths.get(this.groundTruth, project).toString();
			groundTruthFile = reader.readGroundTruthFile(str);
			Map<Integer, String> recommendations = new HashMap<Integer, String>();
			Map<Integer, String> tmp = rec.get(project);

			/* get only the top-N libraries */

			for (int i = 1; i <= N; i++)
				recommendations.put(i, tmp.get(i));
			Set<Integer> keySet = recommendations.keySet();

			sortedList = new ArrayList<Integer>(keySet);
			Collections.sort(sortedList);
			size = sortedList.size();

			for (int i = 0; i < size; i++) {
				int index = sortedList.get(i);
				String lib = recommendations.get(index);
				double rel = 0, tmp1 = 0, tmp2 = 0;
				if (groundTruthFile.contains(lib)) {
					rel = 1;
					tmp1 = (double) rel * (1 - pop.get(lib));
					tmp2 = Math.log(index + 1) / Math.log(2);
					val1 += (double) tmp1 / tmp2;
					val2 += (double) rel / tmp2;
				}
			}
		}
		double epc = (double) val1 / val2;
		return epc;
	}

	public Map<String, Double> popularity() {

		Map<String, Double> pop = new HashMap<String, Double>();

		Set<Integer> keyTestingProjects = testingProjects.keySet();
		Map<Integer, String> recommendations = null;
		Set<Integer> keySet = null;

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String str = Paths.get(this.recDir, filename).toString();
			recommendations = reader.readRecommendationFile(str);
			keySet = recommendations.keySet();

			for (Integer key : keySet) {
				double val = 0;
				String lib = recommendations.get(key);
				if (pop.containsKey(lib))
					val = pop.get(lib) + 1;
				else
					val = 1;
				pop.put(lib, val);
			}
		}

		Set<String> libraries = pop.keySet();
		double max = 0;
		for (String lib : libraries)
			if (max < pop.get(lib))
				max = pop.get(lib);

		for (String lib : libraries) {
			double val = pop.get(lib) / max;
			pop.put(lib, val);
		}

		return pop;
	}

	public Map<String, Double> frequency() {

		Map<String, Double> pop = new HashMap<String, Double>();

		Set<Integer> keyTestingProjects = testingProjects.keySet();
		Map<Integer, String> recommendations = null;
		Set<Integer> keySet = null;

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String str = this.recDir + filename;
			recommendations = reader.readRecommendationFile(str);
			keySet = recommendations.keySet();

			for (Integer key : keySet) {
				double val = 0;
				String lib = recommendations.get(key);
				/* count the number of occurrence of a library */
				if (pop.containsKey(lib))
					val = pop.get(lib) + 1;
				else
					val = 1;
				pop.put(lib, val);
			}
		}

//		Set<String> libraries = pop.keySet();
//		double max = 0;
//		for(String lib:libraries)if(max<pop.get(lib))max = pop.get(lib);
//		
//		for(String lib:libraries) {		
//			double val = pop.get(lib);			
//			pop.put(lib, val);			
//		}	
		return pop;
	}

	/* Novelty: Entropy-based Novelty EBN@N */
	public double EBN(int startPos, int endPos) {

		Map<Integer, String> testingProjects = new HashMap<Integer, String>();
		testingProjects = reader.readProjectList(Paths.get(this.srcDir, "projects.txt").toString(), startPos, endPos);
		Set<Integer> keyTestingProjects = testingProjects.keySet();
		Map<Integer, String> recommendations = null;
		Set<Integer> keySet = null;
//		Set<String> groundTruthFile = null;

		Map<Integer, Set<String>> allRecs = new HashMap<Integer, Set<String>>();

		List<Integer> sortedList = null;
		int size = 0;

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String str = this.recDir + filename;
			recommendations = reader.readRecommendationFile(str);
			keySet = recommendations.keySet();
			Set<String> recs = new HashSet<String>();
			for (Integer key : keySet)
				recs.add(recommendations.get(key));
			allRecs.put(keyTesting, recs);
		}

		double ebn = 0;

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String str = this.recDir + filename;
			recommendations = reader.readRecommendationFile(str);
			keySet = recommendations.keySet();

			sortedList = new ArrayList<Integer>(keySet);
			Collections.sort(sortedList);
			size = sortedList.size();
			double tmp1 = 0;
			for (int i = 0; i < size; i++) {
				int index = sortedList.get(i);
				String lib = recommendations.get(index);
				double tmp2 = 0;

				for (Integer keyTesting2 : keyTestingProjects) {
					if (keyTesting2 != keyTesting) {
						Set<String> recs = allRecs.get(keyTesting2);
						if (recs.contains(lib))
							tmp2++;
					}
				}

				tmp2 = (double) tmp2 / testingProjects.size();
				if (tmp2 != 0)
					tmp1 += (0 - 1) * tmp2 * Math.log(tmp2) / Math.log(2);
			}
			ebn += tmp1;
		}

		double ret = (double) ebn / keyTestingProjects.size();
		logger.info("EBN is: " + ret);
		return ret;
	}

}
