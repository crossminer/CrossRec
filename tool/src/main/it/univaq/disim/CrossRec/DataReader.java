package it.univaq.disim.CrossRec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataReader {

	public DataReader() {

	}
	
	public int getNumberOfProjects(String filename) {
		int count = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))){
			while (reader.readLine() != null) count++;
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
		try (BufferedReader reader = new BufferedReader(new FileReader(filename));){
			
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
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))){
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
		int pos = filename.lastIndexOf("/");
		String fname = filename.substring(pos + 1, filename.length());
		fname = fname.replace("dicth_", "");
		
		try (BufferedReader reader = new BufferedReader(new FileReader(filename));
				BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(groundTruthPath, fname).toString()));){
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
			
			/*
			 * Read a half of the dictionary and all users, the other half is put into the
			 * ground-truth data
			 */

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

	public Set<String> getHalfOfLibraries(String filename) {
		Map<Integer, String> dict = new HashMap<Integer, String>();
		Set<String> ret = new HashSet<String>();
		String line = null;
		String[] vals = null;
		int libCount = 0, half = 0;

		try (BufferedReader reader = new BufferedReader(new FileReader(filename))){
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
			libCount = 0;
			for (Integer key : keySet) {
				String artifact = dict.get(key);
				if (artifact.contains("#DEP#")) {
					ret.add(artifact);
					libCount++;
				}
				if (libCount == half)
					break;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/* get the list of libraries for one project */

	public Set<String> getLibraries(String filename) {
		Set<String> vector = new HashSet<String>();
		String line = null;
		String[] vals = null;

		try (BufferedReader reader = new BufferedReader(new FileReader(filename))){
			while ((line = reader.readLine()) != null) {
				vals = line.split("\t");
				String library = vals[1].trim();
				if (library.contains("#DEP#"))
					vector.add(library);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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

		try (BufferedReader reader = new BufferedReader(new FileReader(filename))){
			
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
	//TODO SI
	public Map<Integer, String> readRecommendationFile(String filename) {
		Map<Integer, String> ret = new HashMap<Integer, String>();
		String line = null;
		String[] vals = null;
		int id = 1;
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))){
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

	
	//TODO SI
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
	//TODO SI
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

	//TODO SI
	public Set<String> readRecommendationFile(String filename, int size) {
		Set<String> ret = new HashSet<String>();
		String line = null;
		String[] vals = null;
		int count = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(filename));){
			
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

	//TODO SI
	public Map<String, Double> readRecommendationScores(String filename) {
		Map<String, Double> ret = new HashMap<String, Double>();
		String line = null;
		String[] vals = null;

		try (BufferedReader reader = new BufferedReader(new FileReader(filename));){
			
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

	//TODO SI
	public Set<String> readGroundTruthFile(String filename) {
		Set<String> ret = new HashSet<String>();
		String line = null;
		String[] vals = null;
		try (BufferedReader reader = new BufferedReader(new FileReader(filename));){
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
	//TODO SI
	public Map<String, Double> readGroundTruthScore(String filename) {

		Map<String, Double> ret = new HashMap<String, Double>();

		String line = null;
		String[] vals = null;

		try (BufferedReader reader = new BufferedReader(new FileReader(filename))){
			
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