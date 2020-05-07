package it.univaq.disim.crossrec;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import it.univaq.disim.crossrec.validation.Validator;


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
	private static final int numOfNeighbours = 25;
	private static String _propFile = "evaluation.properties";
	final static Logger logger = Logger.getLogger(Runner.class);
	public Runner(){
		
	}
	
	public String loadConfigurations() throws FileNotFoundException, IOException{		
		Properties prop = new Properties();				
		prop.load(getClass().getClassLoader().getResourceAsStream(_propFile));		
		return prop.getProperty("sourceDirectory");
	}
	
	public void run(boolean bayesian) throws FileNotFoundException, IOException{
		
		logger.info("CrossRec: Recommender System!");
		String srcDir = loadConfigurations();
		this.srcDir = srcDir;
		DataReader dr = new DataReader(srcDir);
		numOfProjects = dr.getNumberOfProjects(Paths.get(this.srcDir, "projects.txt").toString());		
		tenFoldCrossValidation(bayesian);
		logger.info(System.currentTimeMillis());		
		Validator validator = new Validator(srcDir, bayesian);
		validator.run();
	}
	public void tenFoldCrossValidation(boolean bayesian) {
		int step = (int)numOfProjects/10;								
								
		for(int i=0;i<10;i++) {
			
			int trainingStartPos1 = 1;			
			int trainingEndPos1 = i*step;			
			int trainingStartPos2 = (i+1)*step+1;
			int trainingEndPos2 = numOfProjects;
			int testingStartPos = 1+i*step;
			int testingEndPos =   (i+1)*step;
			
			int k=i+1;
			subFolder = "Round" + Integer.toString(k);			
	
			logger.info("Computing similarities fold " + i);
			SimilarityCalculator calculator = new SimilarityCalculator(this.srcDir,this.subFolder,
					trainingStartPos1,
					trainingEndPos1,
					trainingStartPos2,
					trainingEndPos2,
					testingStartPos,
					testingEndPos,
					bayesian);
			
			calculator.computeWeightCosineSimilarity();
			logger.info("\tComputed similarities fold " + i);
			logger.info("Computing recommendations fold " + i);
			RecommendationEngine engine = new RecommendationEngine(this.srcDir,this.subFolder,numOfNeighbours,testingStartPos,testingEndPos, bayesian);
//		    engine.ItemBasedRecommendation();					   		    
			engine.userBasedRecommendation();	
			logger.info("\tComputed recommendations fold " + i);
		}
		
	}
	
	public static void main(String[] args) {	
		Runner runner = new Runner();			
		try {
			runner.run(false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}				    		    
		return;
	}	
	
}
