package Common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

public class Utils {

	public static TreeMap<Integer, TreeSet<Integer>> getInvertedFile(File dir, Normalizer normalizer,
			LinkedHashMap<String, Integer> postingsMap, String wordList, File wordListFile) throws IOException {
		TreeMap<Integer, TreeSet<Integer>> invertedFileMap = new TreeMap<>();
		if (dir.isDirectory()) {
			String[] fileNames = dir.list();
			ArrayList<String> words;
			String wordLC;
			TreeSet<Integer> fileList;
			for (String fileName : fileNames) {
				int postinId = postingsMap.get(fileName);
				System.err.println("Analyse du fichier " + fileName);
				words = normalizer.normalize(new File(dir, fileName));
				for (String word : words) {
					wordLC = word;
					wordLC = wordLC.toLowerCase();
					int wordIndex = wordList.indexOf(wordLC);
					if (wordIndex == -1) {
						wordIndex = wordList.length();
						wordList = wordList.concat(wordLC);
					}
					fileList = invertedFileMap.get(wordIndex);
					if (fileList == null) {
						fileList = new TreeSet<>();
						fileList.add(postinId);
						invertedFileMap.put(wordIndex, fileList);
					} else {
						if (!fileList.contains(postinId)) {
							fileList.add(postinId);
							invertedFileMap.put(wordIndex, fileList);
						}

					}
				}

			}
		}
		saveWordList(wordList, wordListFile);
		return invertedFileMap;
	}

	public static void saveInvertedFile(TreeMap<Integer, TreeSet<Integer>> invertedFile, File outFile)
			throws IOException {
		try {
			FileWriter fw = new FileWriter(outFile);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			// Ecriture des mots
			for (int word : invertedFile.keySet()) {
				TreeSet<Integer> fileList = invertedFile.get(word);
				String fileListString = "";
				for (int file : fileList) {
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
		int word1 = -1;
		int word2 = -1;

		if (sc.hasNext()) {
			line1 = sc.nextLine();
			words1 = line1.split("\t");
			word1 = Integer.valueOf(words1[0]);
		}
		String line2 = null;
		if (sc2.hasNext()) {
			line2 = sc2.nextLine();
			words2 = line2.split("\t");
			word2 = Integer.valueOf(words2[0]);
		}

		while (line1 != null && line2 != null) {
			if (word1 == word2) {
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
					word1 = Integer.valueOf(words1[0]);
				} else {
					line1 = null;
				}
				if (sc2.hasNext()) {
					line2 = sc2.nextLine();
					words2 = line2.split("\t");
					word2 = Integer.valueOf(words2[0]);
				} else {
					line2 = null;
				}
			} else if (word1 < word2) {
				out.println(line1);
				if (sc.hasNext()) {
					line1 = sc.nextLine();
					words1 = line1.split("\t");
					word1 = Integer.valueOf(words1[0]);
				} else {
					line1 = null;
				}
			} else if (word1 > word2) {
				out.println(line2);
				if (sc2.hasNext()) {
					line2 = sc2.nextLine();
					words2 = line2.split("\t");
					word2 = Integer.valueOf(words2[0]);
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
				FileWriter fw = new FileWriter(new File(outDir, file.getName().replaceAll(".txt$", ".poids")));
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw);
				// Ecriture des mots
				for (String word : words) {
					out.println(word + "\t" + tfIdfs.get(word));
				}
				out.close();
				bw.close();
				fw.close();
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}
	}

	// http://stackoverflow.com/questions/106770/standard-concise-way-to-copy-a-file-in-java
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	// Construit le dictionnaire indice/nom du fichier
	public static LinkedHashMap<String, Integer> ConstructPostingsMap(File inDir) throws IOException {
		LinkedHashMap<String, Integer> postingsMap = new LinkedHashMap<>();
		if (inDir.isDirectory()) {
			File[] subDirs = inDir.listFiles();

			for (File subDir : subDirs) {
				File[] subDirs2 = subDir.listFiles();

				for (File subDir2 : subDirs2) {
					File[] files = subDir2.listFiles();
					for (int i = 0; i < files.length; i++) {
						postingsMap.put(files[i].getName(), i);
					}
				}
			}
			return postingsMap;
		}

		return null;

	}

	// Enregistre le dictionnaire indice/nom du fichier
	public static void savePostingsMap(LinkedHashMap<String, Integer> postingsMap, File outFile) throws IOException {
		FileWriter fw = new FileWriter(outFile);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
		for (String posting_file : postingsMap.keySet()) {
			out.println(postingsMap.get(posting_file) + "\t" + posting_file);
		}
		out.close();
		bw.close();
		fw.close();
	}

	// Récupère le dictionnaire indice/nom du fichier
	public static LinkedHashMap<Integer, String> getPostingsMap(File inFile) throws IOException {
		LinkedHashMap<Integer, String> postingsMap = new LinkedHashMap<>();
		BufferedReader br1 = new BufferedReader(new FileReader(inFile));
		String line;
		while ((line = br1.readLine()) != null) {
			String[] postings = line.split("\t");
			postingsMap.put(Integer.valueOf(postings[0]), postings[1]);

		}
		return postingsMap;
	}

	// Récupérer la chaine wordList de son fichier
	public static String getWordList(File inFile) throws IOException {
		FileInputStream fis = new FileInputStream(inFile);
		byte[] data = new byte[(int) inFile.length()];

		fis.read(data);
		fis.close();

		String wordList = new String(data, "UTF-8");
		return wordList;
	}

	// Enregistre le fichier WordList
	public static void saveWordList(String wordList, File outFile) throws IOException {
		PrintWriter writer = new PrintWriter(outFile, "UTF-8");
		writer.println(wordList);
		writer.close();
	}

	// Récupérer un mot à partir de sa postion et de la position du mot suivant
	// (pour délimiter le mot)
	public static String getWordWithPosition(int postion, int postion_next, String wordList) {
		return wordList.substring(postion, postion_next - 1);
	}

	// Récupérer le DF à partir de l'index des DFs généré grâce à la fonction
	// getDFIndex
	public static int getDF(int position, HashMap<Integer, Integer> dfIndex) {
		return dfIndex.get(position);
	}
	
	//Récupérer l'index nécessaire pour avoir les DF (sans la liste des fichiers)

	public static HashMap<Integer, Integer> getDFIndex(File inFile) throws IOException {
		HashMap<Integer, Integer> dfIndex = new LinkedHashMap<>();
		BufferedReader br1 = new BufferedReader(new FileReader(inFile));
		String line;
		while ((line = br1.readLine()) != null) {
			String[] postings = line.split("\t");
			dfIndex.put(Integer.valueOf(postings[0]), Integer.valueOf(postings[1]));

		}
		return dfIndex;
	}

	//Récupérer l'index (position du mot/ liste des indices des fichiers) 
	//J'ai choisi de stocker la liste des indices des fichiers dans un tableau de int plutôt que dans un TreeSet pour que ça prenne
	//moins d'espace mémoire (voir cours Objets java pour la RI)
	
	public static TreeMap<Integer, int[]> getIndex(File inFile) throws IOException {
		TreeMap<Integer, int[]> index = new TreeMap<>();
		BufferedReader br1 = new BufferedReader(new FileReader(inFile));
		String line;
		while ((line = br1.readLine()) != null) {
			String[] indexLine = line.split("\t");
			String[] postingsString = indexLine[2].split(",");
			int postingsLength = Integer.valueOf(indexLine[1]);
			int[] postings = new int[postingsLength];
			for (int i = 0; i < postingsLength; i++) {
				postings[i] = Integer.valueOf(postingsString[i]);
			}
			index.put(Integer.valueOf(indexLine[0]), postings);

		}
		return index;
	}
	

}
