package TPs_todelete_;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import Common.FrenchStemmer;
import Common.Normalizer;

public class TP3FichierInverse {
	protected static String DIRNAME = "/home/tp-home010/akhoufi/Téléchargements/lemonde-utf8/";

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
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public static void main(String[] args) {
		try {
			String out2FileName = "/home/tp-home010/akhoufi/Téléchargements/InvertedFileWithWeights.txt";
			Normalizer stemmer = new FrenchStemmer();
//			TreeMap<String, TreeSet<String>> invertedFile = getInvertedFile(new File(DIRNAME), stemmer);
//			System.out.println(invertedFile.size());
//			System.out.println(invertedFile.get("achet"));
//			saveInvertedFile(invertedFile, new File(outFileName));

			 TreeMap<String, TreeMap<String, Integer>> invertedFileWithWeights
			 = getInvertedFileWithWeights(new File(DIRNAME), stemmer);
			 saveInvertedFileWithWeights(invertedFileWithWeights, new
			 File(out2FileName));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
