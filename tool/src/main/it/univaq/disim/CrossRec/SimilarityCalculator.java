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
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

public class SimilarityCalculator {

	/*
	 * Compute similarity between every testing project and all training projects
	 * using Cosine Similarity with Weight
	 */

	private String srcDir;
	private String groundTruth;
	private String simDir;
	private String subFolder;

	private int trainingStartPos1;
	private int trainingEndPos1;
	private int trainingStartPos2;
	private int trainingEndPos2;
	private int testingStartPos;
	private int testingEndPos;

	public SimilarityCalculator(String sourceDir, String subFolder, int trStartPos1, int trEndPos1, int trStartPos2,
			int trEndPos2, int teStartPos, int teEndPos) {
		this.srcDir = sourceDir;
		this.subFolder = subFolder;
		this.groundTruth = Paths.get(this.srcDir, this.subFolder, "GroundTruth").toString();
		this.simDir = Paths.get(this.srcDir, this.subFolder, "Similarities").toString();

		this.trainingStartPos1 = trStartPos1;
		this.trainingEndPos1 = trEndPos1;
		this.trainingStartPos2 = trStartPos2;
		this.trainingEndPos2 = trEndPos2;
		this.testingStartPos = teStartPos;
		this.testingEndPos = teEndPos;

	}

	/*
	 * Compute similarity between every testing project and all training projects
	 * using Cosine Similarity with Weight
	 */

	public void computeWeightCosineSimilarity() {

		DataReader reader = new DataReader();

		Map<Integer, String> trainingProjects = new HashMap<Integer, String>();

		if (trainingStartPos1 < trainingEndPos1)
			trainingProjects = reader.readProjectList(Paths.get(this.srcDir, "projects.txt").toString(),
					trainingStartPos1, trainingEndPos1);

		if (trainingStartPos2 < trainingEndPos2) {
			Map<Integer, String> tempoProjects = reader.readProjectList(
					Paths.get(this.srcDir, "projects.txt").toString(), trainingStartPos2, trainingEndPos2);
			trainingProjects.putAll(tempoProjects);
		}

		Map<Integer, String> testingProjects = new HashMap<Integer, String>();
		testingProjects = reader.readProjectList(Paths.get(this.srcDir, "projects.txt").toString(), testingStartPos,
				testingEndPos);

		Set<Integer> keyTrainingProjects = trainingProjects.keySet();
		Set<Integer> keyTestingProjects = testingProjects.keySet();

		String trainingPro = "", testingPro = "", content = "";
		String testingFilename = "";
		String trainingFilename = "";

		Set<String> testingLibs = null;
		Set<String> trainingLibs = null;
		Set<String> allTrainingLibs = null;
		Set<String> allLibs = null;

		Graph graph = null, trainingGraph = null, testingGraph = null;
		Map<Integer, String> trainingDictionary = new HashMap<Integer, String>();
		Map<Integer, String> testingDictionary = new HashMap<Integer, String>();

		graph = new Graph();
		String trainingDictFilename = "", trainingGraphFilename = "", filename = "";
		allLibs = new HashSet<String>();
		allTrainingLibs = new HashSet<String>();

		for (Integer keyTraining : keyTrainingProjects) {
			trainingPro = trainingProjects.get(keyTraining);
			// trainingFilename = trainingPro.replace("git://github.com/",
			// "").replace(".git", "").replace("/", "__");
			trainingFilename = trainingPro.replace("git://github.com/", "").replace("/", "__");
			trainingGraphFilename = Paths.get(this.srcDir, "graph_" + trainingFilename).toString();
			trainingDictFilename = Paths.get(this.srcDir, "dicth_" + trainingFilename).toString();

			/* read all libraries for each project */
			trainingLibs = reader.getLibraries(trainingDictFilename);

			allTrainingLibs.addAll(trainingLibs);

			/* readDictionary4: read only libraries, ignore stars */
			trainingDictionary = reader.readDictionary(trainingDictFilename);
			trainingGraph = new Graph(trainingGraphFilename, trainingDictionary);

			if (graph == null) {
				graph = new Graph(trainingGraph);
			} else {
				graph.combine(trainingGraph, trainingDictionary);
			}
		}

		/* getAlsoUsers = true if you want to get star events */

		boolean getAlsoUsers = false;
		String testingGraphFilename = "", testingDictFilename = "";

		Map<Integer, Double> libWeight = new HashMap<Integer, Double>();
		Map<Integer, Set<Integer>> graphEdges = null;

		Set<Integer> outlinks = new HashSet<Integer>();
		Set<Integer> keySet = null;
		Map<String, Integer> combinedDictionary = null;

		for (Integer keyTesting : keyTestingProjects) {
			try {
				/* reset the buffer */
				allLibs = new HashSet<String>();
				/* add all libraries from the training set */
				allLibs.addAll(allTrainingLibs);
				Graph combinedGraph = new Graph(graph);
				Map<String, Double> sim = new HashMap<String, Double>();
				testingPro = testingProjects.get(keyTesting);
				filename = testingPro.replace("git://github.com/", "").replace("/", "__");
				// filename = testingPro.replace("git://github.com/", "").replace(".git",
				// "").replace("/", "__");
				testingFilename = filename;
				testingGraphFilename = Paths.get(this.srcDir, "graph_" + testingFilename).toString();
				testingDictFilename = Paths.get(this.srcDir, "dicth_" + testingFilename).toString();

				
				testingLibs = new HashSet<String>();
				testingDictionary = reader.extractHalfDictionary(testingDictFilename, this.groundTruth, getAlsoUsers);
				testingLibs = Sets.newHashSet(testingDictionary.values());
				testingLibs = testingLibs.stream().filter(z -> z.startsWith("#DEP#")).collect(Collectors.toSet());
				allLibs.addAll(testingLibs);
				testingGraph = new Graph(testingGraphFilename, testingDictionary);

//				testingLibs = new HashSet<String>();
//				testingLibs = reader.getHalfOfLibraries(testingDictFilename);
//				allLibs.addAll(testingLibs);
//				testingDictionary = reader.extractHalfDictionary(testingDictFilename, this.groundTruth, getAlsoUsers);
//				testingGraph = new Graph(testingGraphFilename, testingDictionary);

				combinedGraph.combine(testingGraph, testingDictionary);
				combinedDictionary = combinedGraph.getDictionary();

				System.out.println("Similarity computation for " + testingPro);

				graphEdges = combinedGraph.getOutLinks();
				keySet = graphEdges.keySet();

				/* explore the graph and count the number of occurrences of each library */
				/* start nodes are projects and end nodes are libraries */

				double freq = 0;
				for (Integer startNode : keySet) {
					outlinks = graphEdges.get(startNode);
					for (Integer endNode : outlinks) {

						if (libWeight.containsKey(endNode)) {
							freq = libWeight.get(endNode) + 1;
						} else
							freq = 1;
						libWeight.put(endNode, freq);
					}
				}

				/* get the number of projects in the whole graph */
				int numberOfProjects = keySet.size();
				keySet = libWeight.keySet();
				double weight = 0, idf = 0;
				for (Integer libID : keySet) {
					freq = libWeight.get(libID);
					weight = (double) numberOfProjects / freq;
					idf = Math.log(weight);
					libWeight.put(libID, idf);
				}

				for (Integer keyTraining : keyTrainingProjects) {
					trainingPro = trainingProjects.get(keyTraining);
					// trainingFilename = trainingPro.replace("git://github.com/",
					// "").replace(".git", "").replace("/", "__");
					trainingFilename = trainingPro.replace("git://github.com/", "").replace("/", "__");
					trainingDictFilename = Paths.get(this.srcDir, "dicth_" + trainingFilename).toString();
					/* read all libraries for each project */
					trainingLibs = reader.getLibraries(trainingDictFilename);

					Set<String> union = Sets.union(testingLibs, trainingLibs);
					List<String> libSet = new ArrayList<String>();

					/* change the set to an ordered list */
					for (String lib : union)
						libSet.add(lib);
					int size = union.size();
					if (size != libSet.size())
						System.out.println("Something went wrong!");

					/* Using Cosine Similarity */
					double vector1[] = new double[size];
					double vector2[] = new double[size];
					double val = 0;

					for (int i = 0; i < size; i++) {
						String lib = libSet.get(i);
						if (testingLibs.contains(lib)) {

							// System.out.println("lib is: " + lib);

							int libID = combinedDictionary.get(lib);
							vector1[i] = libWeight.get(libID);
						} else
							vector1[i] = 0;

						if (trainingLibs.contains(lib)) {
							int libID = combinedDictionary.get(lib);
							vector2[i] = libWeight.get(libID);
						} else
							vector2[i] = 0;
//						System.out.println(vector1[i] + ":" + vector2[i]);
					}
					val = CosineSimilarity(vector1, vector2);
					sim.put(keyTraining.toString(), val);
				}

				ValueComparator bvc = new ValueComparator(sim);
				TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);
				sorted_map.putAll(sim);
				Set<String> keySet2 = sorted_map.keySet();

				BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(this.simDir, filename).toString()));
				for (String key : keySet2) {
					content = testingPro + "\t" + trainingProjects.get(Integer.parseInt(key)) + "\t" + sim.get(key);
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

	/* Compute the cosine similarity between two vectors */

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
		ret = (double) sclar / Math.sqrt(norm);
		return ret;
	}

}
