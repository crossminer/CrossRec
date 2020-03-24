package it.univaq.disim.crossrec.validation;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import it.univaq.disim.crossrec.DataReader;

public class BayeisanValidator {
	final static Logger logger = Logger.getLogger(BayeisanValidator.class);

	private String srcDir;
	private DataReader reader;

	public BayeisanValidator(String srcDir) {
		reader = new DataReader(srcDir);
		this.srcDir = srcDir;
	}

	public Multimap<String, String> getRealRepoTopic() {
		Multimap<String, String> multimap = reader.getEASEOutput();
		Multimap<String, String> keepThem = HashMultimap.create();// ArrayListMultimap.create();
		for (String repo : multimap.keySet()) {
			String parsedRepo = repo.replace("/", "__");
			String filePath = Paths.get(this.srcDir, "dicth_" + parsedRepo).toString();
			Set<String> topics = reader.getLibraries(filePath);
			if (topics.size() != 0)
				keepThem.putAll(repo, topics);
		}
		return keepThem;
	}

	public void precision() {
		Map<Integer, Double> precision = initMap();
		Map<Integer, Double> recall = initMap();
		Map<Integer, Double> successRate = initMap();
		Multimap<String, String> real = getRealRepoTopic();
		Multimap<String, String> eASEResult = reader.getEASEOutput();
		for (String reponame : real.keySet()) {
			List<String> reporesult = Lists.newArrayList(eASEResult.get(reponame));
			for (int i = 1; i < 21; i++) {
				Set<String> iList = Sets.newHashSet(reporesult.subList(0, i));
				Set<String> realTopics = Sets.newHashSet(real.get(reponame));
				SetView<String> intersec = Sets.intersection(iList, realTopics);
				precision.put(i, precision.get(i) + intersec.size() / (i * 1.0));
				recall.put(i, recall.get(i) + (intersec.size() / (1.0 * realTopics.size())));
				successRate.put(i, successRate.get(i) + (intersec.size()>0?1:0));
			}
		}
		for (int i = 1; i < 21; i++) {
			precision.put(i, precision.get(i)/real.keySet().size());
			recall.put(i, recall.get(i)/real.keySet().size());
			successRate.put(i, successRate.get(i)/real.keySet().size());
		}
		for(int i = 1; i < 21; i++) logger.info("PR: " + precision.get(i));
		for(int i = 1; i < 21; i++) logger.info("REC: " + recall.get(i));
		for(int i = 1; i < 21; i++) logger.info("SR: " + successRate.get(i));

	}

	private Map<Integer, Double> initMap() {
		Map<Integer, Double> recall = Maps.newHashMap();
		for (int i = 1; i < 21; i++) {
			recall.put(i, 0.0);
		}
		return recall;
	}

}
