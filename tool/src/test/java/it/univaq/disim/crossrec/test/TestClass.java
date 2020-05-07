package it.univaq.disim.crossrec.test;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.google.common.collect.Multimap;

import it.univaq.disim.crossrec.DataReader;
import it.univaq.disim.crossrec.validation.BayeisanValidator;

public class TestClass {
	final static Logger logger = Logger.getLogger(TestClass.class);

	private static final String path = "/Users/juri/PycharmProjects/"
				+ "GithubCollectionParser/TOPIC_RECOMMENDER/"
				+ "IR_DATASET/CROSSREC_METADATA";
	public void print() {
		DataReader dr = new DataReader(path);
		Multimap<String, String> multimap = dr.getEASEOutput();
		dr.extractEASEDictionary("/Users/juri/PycharmProjects/GithubCollectionParser/TOPIC_RECOMMENDER/IR_DATASET/CROSSREC_METADATA/dicth_akaJes__marlin-config", 2, "");
	}
	
	@Test
	public void bayesianValidator() {
		BayeisanValidator be = new BayeisanValidator(path);
		be.coverage();
		
	}
	
	
}
