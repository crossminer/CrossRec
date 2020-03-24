package it.univaq.disim.crossrec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class DataReader {
	final static Logger logger = Logger.getLogger(DataReader.class);

	private String srcDir;

	public DataReader(String srcDir) {
		this.srcDir = srcDir;
	}

	public int getNumberOfProjects(String filename) {
		int count = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			while (reader.readLine() != null)
				count++;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return count;
	}

	public Map<Integer, String> readRepositoryList(String filename) {
		Map<Integer, String> ret = new HashMap<Integer, String>();
		String[] vals = null;
		String line = "", uri = "";
		int id = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(filename));) {

			while ((line = reader.readLine()) != null) {
				line = line.trim();
				vals = line.split("\t");
				id = Integer.parseInt(vals[0].trim());
				uri = vals[1].trim();// .replace("http://dbpedia.org/resource/", "");
				ret.put(id, uri);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Map<Integer, String> readProjectList(String filename, int startPos, int endPos) {
		Map<Integer, String> ret = new HashMap<Integer, String>();
		String line = "", repo = "";
		int count = 1;
		int id = startPos;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));

			while (count < startPos) {
				line = reader.readLine();
				count++;
			}

			while (((line = reader.readLine()) != null)) {
				line = line.trim();
				repo = line.split(",")[0].trim();
				ret.put(id, repo);
				id++;
				count++;
				if (count > endPos)
					break;
			}

			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Map<Integer, String> readDictionary(String filename) {
		Map<Integer, String> vector = new HashMap<Integer, String>();
		String line = null;
		String[] vals = null;
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			while ((line = reader.readLine()) != null) {
				vals = line.split("\t");
				int ID = Integer.parseInt(vals[0].trim());
				String artifact = vals[1].trim();
				if (ID == 1 || artifact.contains("#DEP#"))
					vector.put(ID, artifact);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return vector;
	}

	public Map<Integer, String> extractHalfDictionary(String filename, String groundTruthPath, boolean getAlsoUsers) {
		Map<Integer, String> dict = new HashMap<Integer, String>();
		Map<Integer, String> ret = new HashMap<Integer, String>();
		String line = null;
		String[] vals = null;
		int half = 0;
		int libCount = 0;
		int pos = filename.lastIndexOf(File.separator);
		String fname = filename.substring(pos + 1, filename.length());
		fname = fname.replace("dicth_", "");

		try (BufferedReader reader = new BufferedReader(new FileReader(filename));
				BufferedWriter writer = new BufferedWriter(
						new FileWriter(Paths.get(groundTruthPath, fname).toString()));) {
			while ((line = reader.readLine()) != null) {
				vals = line.split("\t");
				int ID = Integer.parseInt(vals[0].trim());
				String artifact = vals[1].trim();
				dict.put(ID, artifact);
				if (artifact.contains("#DEP#"))
					libCount++;
			}
			int size = libCount;
			Set<Integer> keySet = dict.keySet();
			// TODO half
			half = Math.round(size / 2);
//			half = size > 1 ? 1 : size;
			boolean enoughLib = false;
			libCount = 0;

			for (Integer key : keySet) {
				String artifact = dict.get(key);
				if (libCount == half)
					enoughLib = true;
				if (artifact.contains("#DEP#")) {
					if (!enoughLib) {
						ret.put(key, artifact);
					} else {
						String content = key + "\t" + artifact;
						writer.append(content);
						writer.newLine();
						writer.flush();
					}
					libCount++;
				} else {
					/* put users into the dictionary */
					if (getAlsoUsers || !artifact.contains("#DEP#"))
						ret.put(key, artifact);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	private Multimap<String, String> eASEOutput = ArrayListMultimap.create();
	public List<String> getEASETopic(String projectName, int n) {
		if (eASEOutput.size()==0)
			loadEASEOutput();
		return eASEOutput.get(projectName).stream().limit(n).collect(Collectors.toList());
	}
	public void loadEASEOutput() {
		String filename = Paths.get(this.srcDir, "training_data2.csv").toString();
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] values = line.split(",");
				String repoName = values[0].replace(".txt", "");
				Arrays.asList(Arrays.copyOfRange(values, 1, values.length)).forEach(z -> eASEOutput.put(repoName, "#DEP#" + z.trim()));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Multimap<String, String> getEASEOutput(){
		if (eASEOutput.size()==0)
			loadEASEOutput();
		return eASEOutput;
	}

	/**
	 * Use the output from ESEM
	 * 
	 * @param filename
	 * @param groundTruthPath
	 * @param getAlsoUsers
	 * @return
	 */
	public Map<Integer, String> extractEASEDictionary(String filename, int numberOfTopic, String groundTruthPath) {
		if (eASEOutput.size()==0)
			loadEASEOutput();
		Map<Integer, String> dict = new HashMap<Integer, String>();
		Map<Integer, String> ret = new HashMap<Integer, String>();
		String reponame = filename.substring(filename.lastIndexOf(File.separator) + 1, filename.length())
				.replace("dicth_", "").replace("__", "/");
		List<String> topics = eASEOutput.get(reponame).stream().limit(numberOfTopic).collect(Collectors.toList());
		reponame = "git://github.com/" + reponame;
		ret.put(1, reponame);
		int i = 2;
		for (String topic : topics) {
			ret.put(i, "#DEP#" + topic.trim());
			i++;
		}
		String line = null;
		String[] vals = null;
		int pos = filename.lastIndexOf(File.separator);
		String fname = filename.substring(pos + 1, filename.length());
		fname = fname.replace("dicth_", "");
		try (BufferedReader reader = new BufferedReader(new FileReader(filename));
				BufferedWriter writer = new BufferedWriter(new FileWriter(
						Paths.get(groundTruthPath, fname).toString()))) {
			while ((line = reader.readLine()) != null) {
				vals = line.split("\t");
				int ID = Integer.parseInt(vals[0].trim());
				String artifact = vals[1].trim();
				if(artifact.startsWith("#DEP#"))
						dict.put(ID, artifact);
			}
			Set<Integer> keySet = dict.keySet();
			for (Integer key : keySet) {
				String artifact = dict.get(key);
				String content = key + "\t" + artifact;
				writer.append(content);
				writer.newLine();
				writer.flush();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public Set<String> getLibraries(String filename) {
		Set<String> vector = Sets.newHashSet();
		String line = null;
		String[] vals = null;

		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			while ((line = reader.readLine()) != null) {
				vals = line.split("\t");
				String library = vals[1].trim();
				if (library.contains("#DEP#"))
					vector.add(library);
			}
		} catch (FileNotFoundException e) {
//			logger.error("File not found: " + filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return vector;
	}

	/* read the whole file */

	public Map<Integer, String> getMostSimilarProjects(String filename, int size) {
		Map<Integer, String> projects = new HashMap<Integer, String>();
		String line = null;
		String[] vals = null;
		int count = 0;

		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {

			while ((line = reader.readLine()) != null) {
				vals = line.split("\t");
				String URI = vals[1].trim();
				projects.put(count, URI);
				count++;
				if (count == size)
					break;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return projects;
	}

	public Map<Integer, Double> getSimilarityMatrix(String filename, int size) {
		Map<Integer, Double> sim = new HashMap<Integer, Double>();
		String line = null;
		String[] vals = null;
		int count = 0;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			while ((line = reader.readLine()) != null) {
				vals = line.split("\t");
				Double val = Double.parseDouble(vals[2].trim());
				sim.put(count, val);
				count++;
				if (count == size)
					break;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sim;
	}

	/* read the whole file */
	public Map<Integer, String> readRecommendationFile(String filename) {
		Map<Integer, String> ret = new HashMap<Integer, String>();
		String line = null;
		String[] vals = null;
		int id = 1;
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			while ((line = reader.readLine()) != null) {
				vals = line.split("\t");
				String library = vals[0].trim();
				ret.put(id, library);
				id++;
				if (id > 50)
					break;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Map<Integer, String> readAllRecommendations(String filename) {
		Map<Integer, String> ret = new HashMap<Integer, String>();
		String line = null;
		String[] vals = null;
		int id = 1;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			while ((line = reader.readLine()) != null) {
				vals = line.split("\t");
				double val = Double.parseDouble(vals[1].trim());
				if (val != 0) {
					String library = vals[0].trim();
					ret.put(id, library);
					id++;
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Set<String> readLongTailItems(String filename) {
		Set<String> ret = new HashSet<String>();
		String line = null;
		String[] vals = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			while ((line = reader.readLine()) != null) {
				vals = line.split("\t");
				String library = vals[0].trim();
				ret.add(library);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public Set<String> readRecommendationFile(String filename, int size) {
		Set<String> ret = new HashSet<String>();
		String line = null;
		String[] vals = null;
		int count = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(filename));) {

			while ((line = reader.readLine()) != null) {
				vals = line.split("\t");
				String library = vals[0].trim();
				ret.add(library);
				count++;
				if (count == size)
					break;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Map<String, Double> readRecommendationScores(String filename) {
		Map<String, Double> ret = new HashMap<String, Double>();
		String line = null;
		String[] vals = null;

		try (BufferedReader reader = new BufferedReader(new FileReader(filename));) {

			while ((line = reader.readLine()) != null) {
				vals = line.split("\t");
				String item = vals[0].trim();
				double score = Double.parseDouble(vals[1].trim());
				ret.put(item, score);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Set<String> readGroundTruthFile(String filename) {
		Set<String> ret = new HashSet<String>();
		String line = null;
		String[] vals = null;
		try (BufferedReader reader = new BufferedReader(new FileReader(filename));) {
			while ((line = reader.readLine()) != null) {
				vals = line.split("\t");
				String library = vals[1].trim();
				ret.add(library);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/*
	 * read ground-truth file including the rating given by users.
	 */
	public Map<String, Double> readGroundTruthScore(String filename) {

		Map<String, Double> ret = new HashMap<String, Double>();

		String line = null;
		String[] vals = null;

		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {

			while ((line = reader.readLine()) != null) {
				vals = line.split("\t");
				String[] temp = vals[1].trim().split("%");
				String item = temp[0].trim();
				double rating = Double.parseDouble(temp[1].trim());
				ret.put(item, rating);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

}