package it.univaq.disim.CrossRec.validation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

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

public class Runner {

	private String srcDir;
	private String subFolder;
	private int numOfProjects;
	private int numOfLibraries;

	public Runner() {

	}

	private String loadConfigurations() throws FileNotFoundException, IOException {
		Properties prop = new Properties();

		prop.load(new FileInputStream("evaluation.properties"));
		this.srcDir = prop.getProperty("sourceDirectory");

		return this.srcDir;
	}

	public void run(String srcDir) {

		System.out.println("Ten-fold cross validation");

//		numOfProjects = 11692;//BookCrossing
//		numOfProjects = 10325;//Goodbooks
//		numOfProjects = 1472;//IOT

		try {
			loadConfigurations();
			this.srcDir = srcDir;
			DataReader reader = new DataReader();
			String inputFile = "projects.txt";
			numOfProjects = reader.getNumberOfProjects(Paths.get(this.srcDir, inputFile).toString());
			numOfLibraries = 20;
			computeEvaluationMetrics(inputFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

//	printRecommendationList();		
//	readLibFinderDataset();

//	protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption) {
//		init();
//	    this.options = options;
//	    Iterator iter = Arrays.asList(arguments).iterator();
//	    while (iter.hasNext()) {
//	    	String token = (String) iter.next();
//	    	if (token.startsWith("--")){
//	    		int pos = token.indexOf('=');
//	    		String opt = pos == -1 ? token : token.substring(0, pos);
//	    		if (!options.hasOption(opt)){
//	    			processNonOptionToken(token, stopAtNonOption);
//	    		} else {
//	    			currentOption = options.getOption(opt);
//	    			tokens.add(opt);
//	    			if (pos != -1){
//	    				tokens.add(token.substring(pos + 1));	                    }
//	                }
//	            } else if ("-".equals(token)) {
//	                tokens.add(token);
//	            } else if (token.startsWith("-")) {
//	                if (token.length() == 2 || options.hasOption(token)) {
//	                    processOptionToken(token, stopAtNonOption);
//	                } else {
//	                    burstToken(token, stopAtNonOption);
//	                }
//	            } else {
//	                processNonOptionToken(token, stopAtNonOption);
//	            }
//	            gobble(iter);
//	       	}
//	        return (String[]) tokens.toArray(new String[tokens.size()]);
//	 }

	public void computeEvaluationMetrics(String inputFile) {

		int step = (int) numOfProjects / 10;

		int cutOffValue = numOfLibraries;

		double recallRate = 0;
		Map<String, Double> FScore = new HashMap<String, Double>();
		Map<String, Double> Recall = new HashMap<String, Double>();
		Map<String, Double> Precision = new HashMap<String, Double>();

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

//			System.out.println("==============Catalog Coverage==============");					
//			metrics.CatalogCoverage();	
//						
//			System.out.println("==============Entropy==============");
//			metrics.Entropy();
//			
//			
//			System.out.println("==============EPC==============");
//			metrics.EPC();
//
//			System.out.println("==============Long tail==============");
////			metrics.LongTail();			
//			metrics.nDCG();
			metrics.successRate();
			metrics.successRateN();
			metrics.PrecisionRecall();
			recallRate += metrics.RecallRate();
			metrics.computeAveragePrecisionRecall(inputFile);
			metrics.computeAverageSuccessRateN(inputFile);
			metrics.computeAverageSuccessRate(inputFile);
			System.out.println("Average success rate: " + recallRate / 10);

			System.out.println("==============Catalog Coverage==============");
			metrics.CatalogCoverage();

			System.out.println("==============Entropy==============");
			metrics.Entropy();

			System.out.println("==============EPC==============");
			metrics.EPC();
//
			System.out.println("==============Long tail==============");
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

//			System.out.println("==============Entropy-based Novelty==============");
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

//		System.out.println("average: " + recallRate/10);

		return;
	}

	/* read the dataset utilized in the LibFinder paper */

	public void readLibFinderDataset() {
		Map<String, Double> ret = new HashMap<String, Double>();

		String[] vals = null;
		String line = "", system = "", version = "", name = "", groupID = "", lib = "";
		double num = 0;
		int count = 0;
		Set<String> libraries = new HashSet<String>();

		String filename = "/home/utente/CROSSMINER/SourceCode/LibFinder/dependencyFactSet.csv";

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));

			while (((line = reader.readLine()) != null)) {
				line = line.trim();
				vals = line.split(",");
				lib = vals[1].trim();
				groupID = vals[4].trim();
				system = vals[5].trim();
				version = vals[6].trim();
				libraries.add(lib);
				name = groupID + system + version;
				if (ret.containsKey(name))
					num = ret.get(name) + 1;
				else
					num = 1;
				ret.put(name, num);
				count++;
			}

			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ValueComparator bvc = new ValueComparator(ret);
		TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);
		sorted_map.putAll(ret);
		Set<String> keySet = sorted_map.keySet();

		try {
			BufferedWriter writer = new BufferedWriter(
					new FileWriter("/home/utente/CROSSMINER/SourceCode/LibFinder/systems.csv"));
			for (String key : keySet) {
				String content = key + "\t" + ret.get(key);
				writer.append(content);
				writer.newLine();
				writer.flush();
//				System.out.println(content);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("The number of systems is: " + ret.size());
		System.out.println("The number of libraries is: " + libraries.size());

	}

	public void printRecommendationList() {

		int step = (int) 5200 / 10;
		Map<String, Double> freqlib = new HashMap<String, Double>();

//		double val=0;						
//		Set<String> mostFreqDeps = new HashSet<String>();

//		mostFreqDeps.add("junit:junit");
//		
//		mostFreqDeps.add("log4j:log4j");
//		mostFreqDeps.add("com.google.guava:guava");
//		mostFreqDeps.add("commons-io:commons-io");
//		mostFreqDeps.add("org.slf4j:slf4j-log4j12");
//		mostFreqDeps.add("commons-lang:commons-lang");
//		mostFreqDeps.add("commons-codec:commons-codec");
//		mostFreqDeps.add("org.mockito:mockito-core");
//		mostFreqDeps.add("javax.servlet:servlet-api");
//		mostFreqDeps.add("joda-time:joda-time");
//		mostFreqDeps.add("org.apache.httpcomponents:httpclient");
//		mostFreqDeps.add("org.mockito:mockito-all");
//		mostFreqDeps.add("org.apache.commons:commons-lang3");
//		mostFreqDeps.add("com.google.code.gson:gson");
//		mostFreqDeps.add("com.fasterxml.jackson.core:jackson-databind");
//		mostFreqDeps.add("commons-logging:commons-logging");
//		mostFreqDeps.add("ch.qos.logback:logback-classic");
//		mostFreqDeps.add("org.springframework:spring-context");
//		mostFreqDeps.add("org.springframework:spring-test");

//		mostFreqDeps.add("junit:junit");
//		mostFreqDeps.add("org.slf4j:slf4j-api");
//		mostFreqDeps.add("log4j:log4j");
//		mostFreqDeps.add("com.android.support:appcompat-v7");
//		mostFreqDeps.add("commons-io:commons-io");
//		mostFreqDeps.add("com.google.guava:guava");
//		mostFreqDeps.add("org.slf4j:slf4j-log4j12");
//		mostFreqDeps.add("com.google.code.gson:gson");
//		mostFreqDeps.add("com.android.support:design");
//		mostFreqDeps.add("org.springframework:spring-context");
//		mostFreqDeps.add("commons-lang:commons-lang");
//		mostFreqDeps.add("javax.servlet:servlet-api");
//		mostFreqDeps.add("org.mockito:mockito-core");
//		mostFreqDeps.add("org.springframework:spring-test");
//		mostFreqDeps.add("mysql:mysql-connector-java");
//		mostFreqDeps.add("ch.qos.logback:logback-classic");
//		mostFreqDeps.add("org.apache.commons:commons-lang3");
//		mostFreqDeps.add("org.apache.httpcomponents:httpclient");
//		mostFreqDeps.add("com.fasterxml.jackson.core:jackson-databind");
//		mostFreqDeps.add("commons-codec:commons-codec");		

//		mostFreqDeps.add("org.apache.logging.log4j:log4j-api");
//		mostFreqDeps.add("javax.enterprise:cdi-api");
//		mostFreqDeps.add("io.dropwizard.metrics:metrics-core");
//		mostFreqDeps.add("org.springframework:org.springframework");
//		mostFreqDeps.add("org.springframework:spring");

//		mostFreqDeps.add("org.postgresql:postgresql");
//		mostFreqDeps.add("org.jdom:org.jdom");

//		mostFreqDeps.add("jdom:jdom");
//		mostFreqDeps.add("org.springframework.security:spring-security-taglibs");

//		mostFreqDeps.add("junit:junit");	
//		mostFreqDeps.add("hsqldb:hsqldb");
//		mostFreqDeps.add("org.aspectj:org.aspectj");
//		mostFreqDeps.add("org.hibernate:org.hibernate");
//		mostFreqDeps.add("org.apache.avro:avro");		
//		mostFreqDeps.add("org.antlr:antlr-runtime");
//		mostFreqDeps.add("com.google.dagger:dagger-compiler");		
//		mostFreqDeps.add("io.reactivex.rxjava2:rxjava");
//		mostFreqDeps.add("org.codehaus.groovy:org.codehaus.groovy");
//		mostFreqDeps.add("org.apache.hadoop:hadoop-client");
//		mostFreqDeps.add("javax.inject:javax.inject");
//		mostFreqDeps.add("org.slf4j:slf4j-api");		
//		System.out.println(mostFreqDeps.size());

//		mostFreqDeps.add("com.sun.jersey:jersey-json");
//		mostFreqDeps.add("com.sun.jersey:jersey-core");
//		mostFreqDeps.add("com.google.dagger:dagger");
//		mostFreqDeps.add("javax.servlet:jsp-api");
//		mostFreqDeps.add("org.apache.commons:commons-collections4");
//		mostFreqDeps.add("com.jayway.jsonpath:json-path");
//		mostFreqDeps.add("c3p0:c3p0");
//		mostFreqDeps.add("com.squareup.retrofit2:converter-gson");
//		mostFreqDeps.add("com.google.code.gson:com.google.code.gson");

		String subFolder = "";

//		int numRatings = 0;

		for (int i = 0; i < 10; i++) {
			int testingStartPos = 1 + i * step;
			int testingEndPos = (i + 1) * step;
			int k = i + 1;
			subFolder = "Round" + Integer.toString(k);
//			this.groundTruth = this.srcDir + subFolder + "/" + "GroundTruth" + "/";
//			this.recDir = this.srcDir + subFolder + "/" + "Recommendations" + "/";
//			this.resDir = this.srcDir + "/" + "Results" + "/";
//			this.prDir = this.srcDir + subFolder + "/" + "PrecisionRecall" + "/";
//			this.simDir = this.srcDir + subFolder + "/" +"Similarities" +"/";						
			int numberOfLibraries = 10;
			Frequency(k, testingStartPos, testingEndPos, numberOfLibraries, freqlib);
		}

		ValueComparator bvc = new ValueComparator(freqlib);
		TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);
		sorted_map.putAll(freqlib);
		Set<String> keySet = sorted_map.keySet();

//		Set<String> freqkeySet = freqlib.keySet();

		System.out.println("==================================================");
		int count = 0;

		try {
			BufferedWriter writer = new BufferedWriter(
					new FileWriter("/home/utente/Documents/Journals/EMSE/Longtail.txt"));
			for (String key : keySet) {
				// if(mostFreqDeps.contains(key))
//					System.out.println(key + "\t" + freqlib.get(key));
//					count++;
				if (freqlib.get(key) < 420) {
					writer.append("#DEP#" + key + "\t" + freqlib.get(key));
					writer.newLine();
					writer.flush();
				}

				// if(count>20)break;
			}

			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("The number of all training libraries: " + freqlib.size());
//		System.out.println("The number of ratings: " + numRatings);
		System.out.println("==================================================");
	}

	public int Frequency(int k, int startPos, int endPos, int numOfLibraries, Map<String, Double> freqlib) {

		DataReader reader = new DataReader();

		Map<Integer, String> testingProjects = new HashMap<Integer, String>();
		testingProjects = reader.readProjectList(this.srcDir + "projects.txt", startPos, endPos);
		Set<Integer> keyTestingProjects = testingProjects.keySet();
		Map<Integer, String> libraries = null;
		Set<Integer> keySet = null;

		List<Integer> sortedList = null;
		int size = 0;

		double val = 0;
		int numLib = 0;

		Set<String> freqkeySet = freqlib.keySet();

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String str = this.srcDir + "dicth_" + filename;
//			String str = this.recDir +filename;

			libraries = reader.getAllLibraries(str);
//			libraries = reader.readRecommendation(str, numOfLibraries);

			/* number of ratings */

			numLib += libraries.size();
			keySet = libraries.keySet();

			sortedList = new ArrayList<Integer>(keySet);
			Collections.sort(sortedList);
			size = sortedList.size();

//			System.out.println("size: " + size);

			for (int i = 0; i < size; i++) {
				int index = sortedList.get(i);
				String lib = libraries.get(index);
				lib = lib.replace("#DEP#", "");

				if (freqkeySet.contains(lib)) {
					val = freqlib.get(lib) + 1;
				} else {
					val = 1;
				}
				freqlib.put(lib, val);
			}
		}
		return numLib;
	}

	public static void main(String[] args) {
		Runner runner = new Runner();
		try {
			runner.run(runner.loadConfigurations());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
