package it.univaq.disim.CrossRec;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class RecommendationEngine {

	private String srcDir;
	private String groundTruth;
	private String simDir;
	private String recDir;
	private String subFolder;

	private int testingStartPos;
	private int testingEndPos;

	private int numOfNeighbours;
	private int numOfRows;
	private int numOfCols;

	public RecommendationEngine(String sourceDir, String suFolder, int numOfNeighbours, int teStartPos, int teEndPos) {
		this.srcDir = sourceDir;
		this.subFolder = suFolder;
		this.numOfNeighbours = numOfNeighbours;
		this.groundTruth = Paths.get(this.srcDir, subFolder, "GroundTruth").toString();
		this.recDir = Paths.get(this.srcDir, subFolder, "Recommendations").toString();
		this.simDir = Paths.get(this.srcDir, subFolder, "Similarities").toString();
		this.testingStartPos = teStartPos;
		this.testingEndPos = teEndPos;
	}

	public double[][] BuildUserItemMatrix(String testingPro, List<String> libSet) {

		double UserItemMatrix[][] = null;
		DataReader reader = new DataReader();

		String testingFilename = "";
		Set<String> testingLibs = null;
		String filename = "", testingDictFilename = "";
		Set<Integer> keySet = null;

		Map<Integer, Set<String>> allNeighbourLibs = new HashMap<Integer, Set<String>>();
		Set<String> libraries = new HashSet<String>();
		Set<String> libs = new HashSet<String>();
		Map<Integer, String> simProjects = new HashMap<Integer, String>();

		System.out.println("CrossRec is computing recommendations for " + testingPro);
		// filename = testingPro.replace("git://github.com/", "").replace(".git",
		// "").replace("/", "__");
		filename = testingPro.replace("git://github.com/", "").replace("/", "__");
		// testingFilename = testingPro.replace("git://github.com/", "").replace(".git",
		// "").replace("/", "__");
		testingFilename = testingPro.replace("git://github.com/", "").replace("/", "__");
		testingDictFilename = Paths.get(this.srcDir, "dicth_" + testingFilename).toString();
		testingLibs = new HashSet<String>();
		testingLibs = reader.getHalfOfLibraries(testingDictFilename);

		String tmp = Paths.get(this.simDir, filename).toString();
		simProjects = reader.getMostSimilarProjects(tmp, numOfNeighbours);

		keySet = simProjects.keySet();
		for (Integer key : keySet) {
			String project = simProjects.get(key);
			filename = project.replace("git://github.com/", "").replace("/", "__");
			// filename = project.replace("git://github.com/", "").replace(".git",
			// "").replace("/", "__");
			tmp = Paths.get(this.srcDir, "dicth_" + filename).toString();
			libs = reader.getLibraries(tmp);
			allNeighbourLibs.put(key, libs);
			libraries.addAll(libs);
		}

		allNeighbourLibs.put(numOfNeighbours, testingLibs);

		/*
		 * The list of all libraries from the training projects and the testing project
		 */
		libraries.addAll(testingLibs);
		/* change the set to an ordered list */
		for (String l : libraries)
			libSet.add(l);

		/* Number of projects, including the test project */

		this.numOfRows = numOfNeighbours + 1;

		/* Number of libraries */
		this.numOfCols = libraries.size();

		UserItemMatrix = new double[this.numOfRows][this.numOfCols];

		/* assign the user-item matrix */
		for (int i = 0; i < numOfNeighbours; i++) {
			Set<String> tmpLibs = allNeighbourLibs.get(i);
			for (int j = 0; j < this.numOfCols; j++) {
				if (tmpLibs.contains(libSet.get(j))) {
					UserItemMatrix[i][j] = 1.0;
				} else
					UserItemMatrix[i][j] = 0;
			}
		}

		/*
		 * Here is the test project and it needs recommendation. It is located at the
		 * end of the list.
		 */

		Set<String> tmpLibs = allNeighbourLibs.get(numOfNeighbours);
		for (int j = 0; j < this.numOfCols; j++) {
			String str = libSet.get(j);
			if (tmpLibs.contains(str))
				UserItemMatrix[numOfNeighbours][j] = 1.0;
			else
				UserItemMatrix[numOfNeighbours][j] = -1.0;

		}

		/*
		 * calculate the missing ratings using item-based collaborative-filtering
		 * recommendation
		 */
		/*
		 * average rating is computed for the projects that include a library in the
		 * library set, so it is 1
		 */

		return UserItemMatrix;
	}

	/*
	 * Recommends libraries to test projects using the user-based
	 * collaborative-filtering techniques
	 */

	public void UserBasedRecommendation() {

		DataReader reader = new DataReader();
		Map<Integer, String> testingProjects = new HashMap<Integer, String>();
		testingProjects = reader.readProjectList(Paths.get(this.srcDir, "projects.txt").toString(),
				this.testingStartPos, this.testingEndPos);
		Set<Integer> keyTestingProjects = testingProjects.keySet();
		String testingPro = "", filename = "";

		for (Integer keyTesting : keyTestingProjects) {

			Map<String, Double> recommendations = new HashMap<String, Double>();
			Map<Integer, Double> similarities = new HashMap<Integer, Double>();
			List<String> libSet = new ArrayList<String>();

			double val1 = 0;

			testingPro = testingProjects.get(keyTesting);
			// filename = testingPro.replace("git://github.com/", "").replace(".git",
			// "").replace("/", "__");
			filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String tmp = Paths.get(this.simDir, filename).toString();
			similarities = reader.getSimilarityMatrix(tmp, numOfNeighbours);

			double UserItemMatrix[][] = BuildUserItemMatrix(testingPro, libSet);
			double avgRating = 1.0, tmpRating = 0.0;
			for (int k = 0; k < numOfNeighbours; k++)
				val1 += similarities.get(k);
			int N = this.numOfCols;

			for (int j = 0; j < N; j++) {
				if (UserItemMatrix[numOfNeighbours][j] == -1) {
					double val2 = 0;
					for (int k = 0; k < numOfNeighbours; k++) {
						tmpRating = 0;
						for (int l = 0; l < N; l++)
							tmpRating += UserItemMatrix[k][l];
						tmpRating = (double) tmpRating / N;
						val2 += (UserItemMatrix[k][j] - tmpRating) * similarities.get(k);
					}
					recommendations.put(Integer.toString(j), avgRating + val2 / val1);
//					System.out.println(val2 + "\t" + val1);
				}
			}

			ValueComparator bvc = new ValueComparator(recommendations);
			TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);
			sorted_map.putAll(recommendations);
			Set<String> keySet2 = sorted_map.keySet();

			// filename = testingPro.replace("git://github.com/", "").replace(".git",
			// "").replace("/", "__");
			filename = testingPro.replace("git://github.com/", "").replace("/", "__");

			try {
				tmp = this.recDir + filename;
				BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));
				for (String key : keySet2) {
					String content = libSet.get(Integer.parseInt(key)) + "\t" + recommendations.get(key);
					writer.append(content);
					writer.newLine();
					writer.flush();
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return;
	}

	/*
	 * Recommends libraries to test projects using the item-based
	 * collaborative-filtering technique
	 */

	public void newItemBasedRecommendation() {

		DataReader reader = new DataReader();
		Map<Integer, String> testingProjects = new HashMap<Integer, String>();
		testingProjects = reader.readProjectList(this.srcDir + "projects.txt", testingStartPos, testingEndPos);

		Set<Integer> keyTestingProjects = testingProjects.keySet();

		String testingPro = "";
		String filename = "";

		for (Integer keyTesting : keyTestingProjects) {

			Map<String, Double> recommendations = new HashMap<String, Double>();
			List<String> libSet = new ArrayList<String>();
			testingPro = testingProjects.get(keyTesting);
			double UserItemMatrix[][] = BuildUserItemMatrix(testingPro, libSet);

			/*
			 * calculate the missing ratings using the item-based collaborative-filtering
			 * recommendation technique
			 */
			int N = this.numOfCols;

			double vector1[] = new double[numOfNeighbours];
			double vector2[] = new double[numOfNeighbours];
			double sim = 0, tmp1 = 0, tmp2 = 0;
			double avgUserRating = 1.0, avgItemRating = 0.0;

			for (int j = 0; j < N; j++) {
				tmp1 = 0;
				tmp2 = 0;
				if (UserItemMatrix[numOfNeighbours][j] == -1) {
					avgItemRating = 0.0;

					int count = 0;

					for (int l = 0; l < numOfNeighbours; l++) {
						if (UserItemMatrix[l][j] != 0) {
							avgItemRating += UserItemMatrix[l][j];
							count++;
						}

					}

					avgItemRating = (double) avgItemRating / count;

//					System.out.println("avg Item rating is: " + avgItemRating);

					for (int k = 0; k < N; k++) {
						if ((k != j) && UserItemMatrix[numOfNeighbours][k] != -1) {
							for (int l = 0; l < numOfNeighbours; l++) {
								vector1[l] = UserItemMatrix[l][k];
								vector2[l] = UserItemMatrix[l][j];
							}
							/* calculate the similarity between two items */
//							sim=CosineSimilarity(vector1,vector2);

							int v1 = 0, v2 = 0, v3 = 0;
							for (int l = 0; l < numOfNeighbours; l++) {
								if (vector1[l] == 1 && vector2[l] == 1)
									v1++;
								if (vector1[l] == 1)
									v2++;
								if (vector2[l] == 1)
									v3++;
							}

							sim = (double) Math.sqrt(v1) / (Math.sqrt(v2) + Math.sqrt(v3));

							System.out.println("Sim is: " + UserItemMatrix[numOfNeighbours][k]);

							tmp1 += sim * (UserItemMatrix[numOfNeighbours][k] - avgUserRating);
							tmp2 += sim;

						}
					}
//					System.out.println(tmp1 + "\t" + tmp2);

					double val = 0;
					if (tmp1 != 0 && tmp2 != 0)
						val = (double) tmp1 / tmp2;
					else
						val = 0;

					val += avgItemRating;

					recommendations.put(Integer.toString(j), val);
				}
			}

			ValueComparator bvc = new ValueComparator(recommendations);
			TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);
			sorted_map.putAll(recommendations);
			Set<String> keySet2 = sorted_map.keySet();

			// filename = testingPro.replace("git://github.com/", "").replace(".git",
			// "").replace("/", "__");
			filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			try {
				String tmp = this.recDir + filename;
				BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));
				for (String key : keySet2) {
					String content = libSet.get(Integer.parseInt(key)) + "\t" + recommendations.get(key);
					writer.append(content);
					writer.newLine();
					writer.flush();
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return;
	}

	/*
	 * Recommends libraries to test projects using the item-based
	 * collaborative-filtering technique
	 */

	public void ItemBasedRecommendation() {

		DataReader reader = new DataReader();
		Map<Integer, String> testingProjects = new HashMap<Integer, String>();
		testingProjects = reader.readProjectList(this.srcDir + "projects.txt", testingStartPos, testingEndPos);

		Set<Integer> keyTestingProjects = testingProjects.keySet();

		String testingPro = "";
		String filename = "";

		for (Integer keyTesting : keyTestingProjects) {

			Map<String, Double> recommendations = new HashMap<String, Double>();
			List<String> libSet = new ArrayList<String>();
			testingPro = testingProjects.get(keyTesting);
			double UserItemMatrix[][] = BuildUserItemMatrix(testingPro, libSet);

			/*
			 * calculate the missing ratings using the item-based collaborative-filtering
			 * recommendation technique
			 */
			int N = this.numOfCols;

			double vector1[] = new double[numOfNeighbours];
			double vector2[] = new double[numOfNeighbours];

			double sim = 0, tmp1 = 0, tmp2 = 0;
			double avgUserRating = 1.00, avgItemRating = 0.0;

			for (int j = 0; j < N; j++) {
				tmp1 = 0;
				tmp2 = 0;
				if (UserItemMatrix[numOfNeighbours][j] == -1) {
					avgItemRating = 0.0;
					for (int l = 0; l < numOfNeighbours; l++) {
						avgItemRating += UserItemMatrix[l][j];
					}

					avgItemRating = (double) avgItemRating / numOfNeighbours;

					for (int k = 0; k < N; k++) {
						if ((k != j) && UserItemMatrix[numOfNeighbours][k] != -1) {
							for (int l = 0; l < numOfNeighbours; l++) {
								vector1[l] = UserItemMatrix[l][k];
								vector2[l] = UserItemMatrix[l][j];
							}
							/* calculate the similarity between two items */
							sim = CosineSimilarity(vector1, vector2);
							tmp1 += sim * (UserItemMatrix[numOfNeighbours][k] - avgUserRating);
//							System.out.println("average user rating: " + avgUserRating);
							tmp2 += sim;
						}
					}
					double val = 0;
					if (tmp1 != 0 && tmp2 != 0)
						val = (double) tmp1 / tmp2;
					else
						val = 0;

//					System.out.println(tmp1 + "\t" + tmp2);

					val += avgItemRating;
					recommendations.put(Integer.toString(j), val);
				}
			}

			ValueComparator bvc = new ValueComparator(recommendations);
			TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);
			sorted_map.putAll(recommendations);
			Set<String> keySet2 = sorted_map.keySet();

			// filename = testingPro.replace("git://github.com/", "").replace(".git",
			// "").replace("/", "__");
			filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			try {
				String tmp = this.recDir + filename;
				BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));
				for (String key : keySet2) {
					String content = libSet.get(Integer.parseInt(key)) + "\t" + recommendations.get(key);
					writer.append(content);
					writer.newLine();
					writer.flush();
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return;
	}

	public double CosineSimilarity(double[] vector1, double[] vector2) {
		double sclar = 0, norm1 = 0, norm2 = 0;
		int length = vector1.length;
		for (int i = 0; i < length; i++)
			sclar += vector1[i] * vector2[i];
		for (int i = 0; i < length; i++)
			norm1 += vector1[i] * vector1[i];
		for (int i = 0; i < length; i++)
			norm2 += vector2[i] * vector2[i];
		double ret = 0;
		double norm = norm1 * norm2;
		if (norm > 0 && sclar > 0)
			ret = (double) sclar / Math.sqrt(norm);
		else
			ret = 0;
		return ret;
	}

}
