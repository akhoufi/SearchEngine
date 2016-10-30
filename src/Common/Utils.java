package Common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

public class Utils {
	final static Runtime runtime = Runtime.getRuntime();
	private static HashMap<Integer, Long> startTimes = new HashMap<Integer, Long>();
	private static int chronoId = 0;
	/**
     * Returns a String representation of memory information 
     * @return a String representation of memory information
     */
	public static String memoryInfo() {
		final long maxMemory = runtime.maxMemory();
		final long allocatedMemory = runtime.totalMemory();
		final long freeMemory = runtime.freeMemory();
		return "  free memory: "
				+ (freeMemory / 1024.0 / 1024.0)
				+ "\n  allocated memory: "
				+ (allocatedMemory / 1024.0 / 1024.0)
				+ "\n  max memory: "
				+ (maxMemory / 1024.0 / 1024.0)
				+ "\n  total free memory: "
				+ ((freeMemory + (maxMemory - allocatedMemory)) / 1024.0 / 1024.0);
	} 
	
	/**
	 * Returns used memory (in bytes)
	 * @return used memory (in bytes)
	 */
	public static long getUsedMemory() {
		final long allocatedMemory = runtime.totalMemory();
		final long freeMemory = runtime.freeMemory();
		return allocatedMemory - freeMemory;
	}

	/**
	 * Returns <code>true</code> if the memory is 90% full, <code>false</code> otherwise
	 * @return <code>true</code> if the memory is 90% full, <code>false</code> otherwise
	 */
	public static boolean isMemoryFull() {
		return isMemoryFull(0.9);
	}
	
	/**
	 * Return <code>true</code> if the ratio of the memory is full.
	 * @param ratio
	 * @return <code>true</code> if the ratio of the memory is full.
	 */
	public static boolean isMemoryFull(double ratio) {
		final long maxMemory = runtime.maxMemory();
		final long allocatedMemory = runtime.totalMemory();
		final long freeMemory = runtime.freeMemory();
		final double r = ((double)(allocatedMemory - freeMemory) / (double)maxMemory);
		return r > ratio;
	}

	
    /**
     * Waits until a key has been pressed.
     */
    public static void waitKeyPressed() {
    	try {
    		System.out.println("\nPress a key to continue... \n");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Starts the chrono.
     * @return the identifier of the chrono
     */
	public static int startChrono() {
		startTimes.put(++chronoId, System.currentTimeMillis());
		return chronoId;
	}
	
	/**
	 * Ends the chrono and returns the time in seconds since last start
	 * @param chronoId the identifier of the chrono to end.
	 * @return the time in seconds since last start of the identified chrono
	 */
	public static double endChrono(int chronoId) {
		long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTimes.get(chronoId);
        return ((double)elapsed/1000.0);
	}
	
	/**
	 * Ends the chrono and returns a String representation of the time since last start
	 * @param chronoId the identifier of the chrono to end.
	 * @return a String representation of the time since last start
	 */
	public static String formatEndChrono(int chronoId) {
        long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTimes.get(chronoId);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");            
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(new Date(elapsed));
	}
	
	/**
	 * Returns a String representation of the current date and time 
	 * @return a String representation of the current date and time
	 */
	public static String logTime() {
		Calendar now = Calendar.getInstance();
		int hh = now.get(Calendar.HOUR_OF_DAY);         
		int mm = now.get(Calendar.MINUTE);         
		int ss = now.get(Calendar.SECOND);         
		int mois = now.get(Calendar.MONTH) +  1;         
		int jour= now.get(Calendar.DAY_OF_MONTH);         
		int annee = now.get(Calendar.YEAR);                    
		return jour+" / "+mois+" / " +annee+ "   "+ hh+":"+mm+":"+ss + "\n";    
	}
	
	public static TreeMap<String, TreeSet<String>> getInvertedFile(File dir, Normalizer normalizer) throws IOException {
		TreeMap<String, TreeSet<String>> invertedFileMap = new TreeMap<>();
		if (dir.isDirectory()) {
			String[] fileNames = dir.list();
			ArrayList<String> words;
			String wordLC;
			TreeSet<String> fileList;
			for (String fileName : fileNames) {
				System.err.println("Analyse du fichier " + fileName);
				words = normalizer.normalize(new File(dir, fileName));
				for (String word : words) {
					wordLC = word;
					wordLC = wordLC.toLowerCase();
					fileList = invertedFileMap.get(word);
					if (fileList == null) {
						fileList = new TreeSet<>();
						fileList.add(fileName);
						invertedFileMap.put(wordLC, fileList);
					} else {
						if (!fileList.contains(fileName)) {
							fileList.add(fileName);
							invertedFileMap.put(word, fileList);
						}

					}
				}

			}
		}
		return invertedFileMap;
	}

	public static void saveInvertedFile(TreeMap<String, TreeSet<String>> invertedFile, File outFile)
			throws IOException {
		try {
			FileWriter fw = new FileWriter(outFile);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			// Ecriture des mots
			for (String word : invertedFile.keySet()) {
				TreeSet<String> fileList = invertedFile.get(word);
				String fileListString = "";
				for (String file : fileList) {
					if (fileListString.isEmpty()) {
						fileListString += file;
					} else {
						fileListString += "," + file;
					}
				}

				out.println(word + "\t" + fileList.size() + "\t" + fileListString);
			}
			out.close();
			bw.close();
			fw.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public static TreeMap<String, TreeMap<String, Integer>> getInvertedFileWithWeights(File dir, Normalizer normalizer)
			throws IOException {
		TreeMap<String, TreeMap<String, Integer>> invertedFileMap = new TreeMap<>();
		if (dir.isDirectory()) {
			String[] fileNames = dir.list();
			ArrayList<String> words;
			String wordLC;
			TreeMap<String, Integer> fileList;
			for (String fileName : fileNames) {
				System.err.println("Analyse du fichier " + fileName);
				words = normalizer.normalize(new File(dir, fileName));
				for (String word : words) {
					wordLC = word;
					wordLC = wordLC.toLowerCase();
					fileList = invertedFileMap.get(word);
					if (fileList == null) {
						fileList = new TreeMap<String, Integer>();
						fileList.put(fileName, 1);
						invertedFileMap.put(wordLC, fileList);
					} else {
						Integer number = fileList.get(fileName);
						if (number == null) {
							fileList.put(fileName, 1);
							invertedFileMap.put(word, fileList);
						} else {
							number++;
							fileList.put(fileName, number);
							invertedFileMap.put(word, fileList);
						}

					}
				}

			}
		}
		return invertedFileMap;
	}

	public static void saveInvertedFileWithWeights(TreeMap<String, TreeMap<String, Integer>> invertedFile, File outFile)
			throws IOException {
		try {
			FileWriter fw = new FileWriter(outFile);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			for (String word : invertedFile.keySet()) {
				TreeMap<String, Integer> fileList = invertedFile.get(word);
				String fileListString = "";
				for (String file : fileList.keySet()) {
					if (fileListString.isEmpty()) {
						fileListString += file + " " + fileList.get(file);
					} else {
						fileListString += "," + file + " " + fileList.get(file);
					}
				}

				out.println(word + "\t" + fileList.size() + "\t" + fileListString + "\n");
			}
			out.close();
			bw.close();
			fw.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public static void mergeInvertedFiles(File invertedFile1, File invertedFile2, File mergedInvertedFile)
			throws IOException {

		Scanner sc = new java.util.Scanner(invertedFile1);
		Scanner sc2 = new java.util.Scanner(invertedFile2);

		FileWriter fw = new FileWriter(mergedInvertedFile);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);

		String line1 = null;
		String[] words2 = null;
		String[] words1 = null;
		String word1 = null;
		String word2 = null;

		if (sc.hasNext()) {
			line1 = sc.nextLine();
			words1 = line1.split("\t");
			word1 = words1[0];
		}
		String line2 = null;
		if (sc2.hasNext()) {
			line2 = sc2.nextLine();
			words2 = line2.split("\t");
			word2 = words2[0];
		}

		while (line1 != null && line2 != null) {
			if (word1.equals(word2)) {
				Set<String> files = new HashSet<String>(Arrays.asList(words1[2].split(",")));
				files.addAll(Arrays.asList(words2[2].split(",")));
				ArrayList<String> sortedFileList = new ArrayList<String>(files);
				Collections.sort(sortedFileList);

				TreeSet<String> fileslist = new TreeSet<String>(sortedFileList);
				String fileListString = "";

				for (String file : fileslist) {
					if (fileListString.isEmpty()) {
						fileListString += file;
					} else {
						fileListString += "," + file;
					}
				}
				out.println(word1 + "\t" + files.size() + "\t" + fileListString);
				if (sc.hasNext()) {
					line1 = sc.nextLine();
					words1 = line1.split("\t");
					word1 = words1[0];
				} else {
					line1 = null;
				}
				if (sc2.hasNext()) {
					line2 = sc2.nextLine();
					words2 = line2.split("\t");
					word2 = words2[0];
				} else {
					line2 = null;
				}
			} else if (word1.compareTo(word2) < 0) {
				out.println(line1);
				if (sc.hasNext()) {
					line1 = sc.nextLine();
					words1 = line1.split("\t");
					word1 = words1[0];
				} else {
					line1 = null;
				}
			} else if (word1.compareTo(word2) > 0) {
				out.println(line2);
				if (sc2.hasNext()) {
					line2 = sc2.nextLine();
					words2 = line2.split("\t");
					word2 = words2[0];
				} else {
					line2 = null;
				}
			}
		}

		while (line1 != null) {
			out.println(line1);
			if (sc.hasNext()) {
				line1 = sc.nextLine();
			} else {
				line1 = null;
			}
		}
		while (line2 != null) {
			out.println(line2);
			if (sc2.hasNext()) {
				line2 = sc2.nextLine();
			} else {
				line2 = null;
			}
		}
		out.close();
		sc.close();
		sc2.close();
		bw.close();
		fw.close();
	}

	public static double getSimilarity(File file1, File file2) throws IOException {
		HashMap<String, Double> hits1 = new HashMap<String, Double>();
		HashMap<String, Double> hits2 = new HashMap<String, Double>();
		TreeSet<String> wordsList = new TreeSet<String>();
		BufferedReader br1 = new BufferedReader(new FileReader(file1));
		BufferedReader br2 = new BufferedReader(new FileReader(file2));
		String line = br1.readLine();
		String[] wordTfId = null;
		double num = 0.0, denom1 = 0.0, denom2 = 0.0, w1, w2;

		while ((line = br1.readLine()) != null) {
			wordTfId = line.split("\t");
			wordsList.add(wordTfId[0]);
			hits1.put(wordTfId[0], Double.parseDouble(wordTfId[1]));
		}
		while ((line = br2.readLine()) != null) {
			wordTfId = line.split("\t");
			wordsList.add(wordTfId[0]);
			hits2.put(wordTfId[0], Double.parseDouble(wordTfId[1]));
		}
		br1.close();
		br2.close();
		
		for (String word : wordsList) {
			if (hits1.get(word) != null) {
				w1 = hits1.get(word);

			} else {
				w1 = 0.0;
			}

			if (hits2.get(word) != null) {
				w2 = hits2.get(word);

			} else {
				w2 = 0.0;
			}

			num += w1 * w2;
			denom1 += w1 * w1;
			denom2 += w2 * w2;

		}

		return num / (Math.sqrt(denom1) * Math.sqrt(denom2));

	}

	public static void getSimilarDocuments(File file, Set<File> fileList, File outFile) throws IOException {
		HashMap<String, Double> hits = new HashMap<String, Double>();
		String fileName = null;
		double similarity;

		for (File file2 : fileList) {
			hits.put(file2.getName(), getSimilarity(file, file2));
		}

		Comparator<String> comparator = new ValueComparator<String, Double>(hits);
		TreeMap<String, Double> sortedHits = new TreeMap<String, Double>(comparator);
		sortedHits.putAll(hits);

		FileWriter fw = new FileWriter(outFile);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
		for (Map.Entry<String, Double> hit : sortedHits.entrySet()) {
			fileName = hit.getKey();
			similarity = hit.getValue();
			out.println(fileName + "\t" + similarity);
		}
		out.close();
		bw.close();
		fw.close();
	}

	public static HashMap<String, Integer> getTermFrequencies(File file, Normalizer normalizer) throws IOException {
		HashMap<String, Integer> hits = new HashMap<String, Integer>();
		ArrayList<String> words = normalizer.normalize(file);
		Integer number;

		for (String word : words) {
			word = word.toLowerCase();
			number = hits.get(word);
			if (number == null) {
				hits.put(word, 1);
			} else {
				hits.put(word, ++number);
			}
		}
		return hits;
	}

	public static HashMap<String, Integer> getDocumentFrequency(File dir, Normalizer normalizer) throws IOException {
		HashMap<String, Integer> hits = new HashMap<String, Integer>();
		ArrayList<String> wordsInFile;
		ArrayList<String> words;
		String wordLC;
		if (dir.isDirectory()) {
			String[] fileNames = dir.list();

			Integer number;
			for (String fileName : fileNames) {
				System.err.println("Analyse du fichier " + fileName);
				wordsInFile = new ArrayList<String>();
				words = normalizer.normalize(new File(dir, fileName));
				for (String word : words) {
					wordLC = word;
					wordLC = wordLC.toLowerCase();
					if (!wordsInFile.contains(wordLC)) {
						number = hits.get(wordLC);
						if (number == null) {
							hits.put(wordLC, 1);
						} else {
							hits.put(wordLC, number + 1);
						}
						wordsInFile.add(wordLC);
					}
				}
			}
		}
		return hits;
	}
	

	public static HashMap<String, Double> getTfIdf(File file, HashMap<String, Integer> dfs, int documentNumber,
			Normalizer normalizer) throws IOException {
		HashMap<String, Integer> hits = new HashMap<String, Integer>();
		ArrayList<String> words = normalizer.normalize(file);
		Integer number;

		for (String word : words) {
			word = word.toLowerCase();
			number = hits.get(word);
			if (number == null) {
				hits.put(word, 1);
			} else {
				hits.put(word, ++number);
			}
		}

		Integer tf;
		Double tfIdf;
		String word;
		HashMap<String, Double> tfIdfs = new HashMap<String, Double>();

		for (Map.Entry<String, Integer> hit : hits.entrySet()) {
			tf = hit.getValue();
			word = hit.getKey();
			tfIdf = (double) tf * Math.log((double) documentNumber / (double) dfs.get(word));
			tfIdfs.put(word, tfIdf);
		}
		return tfIdfs;
	}

	public static void getWeightFiles(File inDir, File outDir, Normalizer normalizer) throws IOException {
		// calcul des dfs
		HashMap<String, Integer> dfs = getDocumentFrequency(inDir, normalizer);
		// Nombre de documents
		File[] files = inDir.listFiles();
		int documentNumber = files.length;
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		
		// TfIdfs 
		for (File file : files) {
			HashMap<String, Double> tfIdfs = getTfIdf(file, dfs, documentNumber, normalizer);
			TreeSet<String> words = new TreeSet<String>(tfIdfs.keySet());
			// on écrit dans un fichier
			try {
				FileWriter fw = new FileWriter (new File(outDir, file.getName().replaceAll(".txt$", ".poids")));
				BufferedWriter bw = new BufferedWriter (fw);
				PrintWriter out = new PrintWriter (bw);
				// Ecriture des mots
				for (String word : words) {
					out.println(word + "\t" + tfIdfs.get(word)); 
				}
				out.close();
				bw.close();
				fw.close();
			}
			catch (Exception e){
				System.out.println(e.toString());
			}		
		}
	}
		
	
	//http://stackoverflow.com/questions/106770/standard-concise-way-to-copy-a-file-in-java
		public static void copyFile(File sourceFile, File destFile) throws IOException {
		    if(!destFile.exists()) {
		        destFile.createNewFile();
		    }

		    FileChannel source = null;
		    FileChannel destination = null;

		    try {
		        source = new FileInputStream(sourceFile).getChannel();
		        destination = new FileOutputStream(destFile).getChannel();
		        destination.transferFrom(source, 0, source.size());
		    }
		    finally {
		        if(source != null) {
		            source.close();
		        }
		        if(destination != null) {
		            destination.close();
		        }
		    }
		}
}
