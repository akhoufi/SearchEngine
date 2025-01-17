package Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import Common.Constants;
import Common.FrenchStemmer;
import Common.FrenchTokenizer;
import Common.Normalizer;
import Common.Utils;

public class IndexGenerator implements Constants {

	// Save inverted files by pack = 1 day
	public static void saveInvertedFileByPack(File dir, Normalizer normalizer, File outDir,
			LinkedHashMap<String, Integer> postingsMap, File wordListFile) throws IOException {

		TreeMap<Integer, TreeSet<Integer>> invertedFile = new TreeMap<>();

		if (!outDir.exists()) {
			outDir.mkdirs();
		}

		if (dir.isDirectory()) {
			File[] subDirs = dir.listFiles();
			String wordList = "";
			for (File subDir : subDirs) {
				File[] subDirs2 = subDir.listFiles();
				for (File subDir2 : subDirs2) {
					File outFile = new File(
							outDir.getPath() + "/" + subDir.getName() + "_" + subDir2.getName() + ".ind");
					wordList = Utils.getInvertedFile(subDir2, normalizer, postingsMap, wordList, wordListFile, outFile);

				}

			}
			Utils.saveWordList(wordList, wordListFile);

		}

	}

	// merge inverted files from dir directory to mergedInvertedFile file
	public static void mergeManyInvertedFiles(File dir, File mergedInvertedFile) throws IOException {
		if (dir.isDirectory()) {

			File[] files = dir.listFiles();
			ArrayList<File> subListFiles = new ArrayList<File>();
			int k = 0;
			while (files.length != 1) {
				subListFiles = new ArrayList<File>();
				for (int i = 0, j = 0; i < files.length; i = i + 2, j++) {
					File temp = new File(mergedInvertedFile.getParentFile() + "/" + j + "_" + k + ".ind");
					if (i + 1 == files.length) {
						System.out.println("Adding " + files[i].getName());
						subListFiles.add(files[i]);
						break;
					} else {
						System.out.println("Merging " + files[i].getName() + " and " + files[i + 1].getName());
						Utils.mergeInvertedFiles(files[i], files[i + 1], temp);
						subListFiles.add(temp);

					}

				}
				files = new File[subListFiles.size()];
				files = subListFiles.toArray(files);
				k++;
			}
			subListFiles.get(0).renameTo(mergedInvertedFile);
		}
	}

	// Gets the number of documents in the corpus
	public static int getNbDocuments(File dir) {
		int nb = 0;
		if (dir.isDirectory()) {
			File[] subDirs = dir.listFiles();

			for (File subDir : subDirs) {
				File[] subDirs2 = subDir.listFiles();

				for (File subDir2 : subDirs2) {

					nb += subDir2.listFiles().length;

				}

			}

		}
		return nb;

	}

	public static void saveWeightFiles(File inDir, File outDir, File invertedFile, String wordList,
			Normalizer normalizer) throws IOException {

		File[] files = null;
		// calcul des dfs
		System.out.println("Recuperation des dfs a partir de l'index \n");
		HashMap<Integer, Integer> dfs = Utils.getDFIndex(invertedFile);
		// Nombre de documents
		System.out.println("Calcul du nb des documents \n");
		int documentNumber = getNbDocuments(inDir);
		// Creation de la destination
		if (!outDir.exists()) {
			outDir.mkdirs();
		}

		File[] subDirs = inDir.listFiles();

		System.out.println("Debut de Creation des .poids \n");

		for (File subDir : subDirs) {
			File[] subDirs2 = subDir.listFiles();

			for (File subDir2 : subDirs2) {
				files = subDir2.listFiles();
				// TfIdfs
				for (File file : files) {
					System.out.println("Creation de .poids de : " + file.getName());
					HashMap<Integer, Double> tfIdfs = Utils.getTfIdf(file, wordList, dfs, documentNumber, normalizer);
					TreeSet<Integer> wordsInd = new TreeSet<Integer>(tfIdfs.keySet());
					// on écrit dans un fichier
					try {
						FileWriter fw = new FileWriter(new File(outDir, file.getName().replaceAll(".txt$", ".poids")));
						BufferedWriter bw = new BufferedWriter(fw);
						PrintWriter out = new PrintWriter(bw);
						// Ecriture des mots
						for (Integer wordInd : wordsInd) {
							out.println(wordInd + "\t" + tfIdfs.get(wordInd));
						}
						out.close();
						bw.close();
						fw.close();
					} catch (Exception e) {
						System.out.println(e.toString());
					}
				}
			}

		}

	}

	public static void main(String[] args) throws IOException {
		// SaveInvertedFilesByPacks
		try {
			Normalizer stemmerNoStopWords = new FrenchStemmer(new File(STOPWORDS_FILENAME));
			Normalizer tokenizerNoStopWords = new FrenchTokenizer(new File(STOPWORDS_FILENAME));

			// Construct and save postings (files) index
			LinkedHashMap<String, Integer> postingsMap = Utils.ConstructPostingsMap(new File(TEXT_DIR));
			Utils.savePostingsMap(postingsMap, new File(POSTING_INDEX_FILE));

			


			File outStemDir = new File(FINAL_INDEX_STEM_DIR);
			if (!outStemDir.exists()) {
				outStemDir.mkdir();
			}
			File outTokenDir = new File(FINAL_INDEX_TOKEN_DIR);
			if (!outTokenDir.exists()) {
				outTokenDir.mkdir();
			}

			

			// Process for Stemming
			 int chronoId=Utils.startChrono();
				File wordListStemFile = new File(WORD_LIST_STEM_FILE);
				if (wordListStemFile.exists()) {
					wordListStemFile.delete();
				}
				wordListStemFile.createNewFile();
			 saveInvertedFileByPack(new File(TEXT_DIR), stemmerNoStopWords,
			 new File(INVERTED_INDEXES_STEM_DIR),
			 postingsMap, wordListStemFile);
			 String stemWordList = Utils.getWordList(new
			 File(WORD_LIST_STEM_FILE));
			 mergeManyInvertedFiles(new File(INVERTED_INDEXES_STEM_DIR), new
			 File(FINAL_STEM_INDEX));
			 IndexGenerator.saveWeightFiles(new File(TEXT_DIR), new
			 File(WEIGHT_FILES_STEM_DIR),
			 new File(FINAL_STEM_INDEX), stemWordList, stemmerNoStopWords);
			 System.out.println("Stemming indexation time:"+Utils.formatEndChrono(chronoId));
			// Process for Tokenizing
//			int chronoId = Utils.startChrono();
//				File wordListTokenFile = new File(WORD_LIST_TOKEN_FILE);
//				if (wordListTokenFile.exists()) {
//					wordListTokenFile.delete();
//				}
//			 wordListTokenFile.createNewFile();
//			saveInvertedFileByPack(new File(TEXT_DIR), tokenizerNoStopWords, new File(INVERTED_INDEXES_TOKEN_DIR),
//					postingsMap, wordListTokenFile);
//			mergeManyInvertedFiles(new File(INVERTED_INDEXES_TOKEN_DIR), new File(FINAL_TOKEN_INDEX));
//			String toeknWordList = Utils.getWordList(new File(WORD_LIST_TOKEN_FILE));
//			IndexGenerator.saveWeightFiles(new File(TEXT_DIR), new File(WEIGHT_FILES_TOKEN_DIR),
//					new File(FINAL_TOKEN_INDEX), toeknWordList, tokenizerNoStopWords);
//			System.out.println("Tokeninzing indexation time:" + Utils.formatEndChrono(chronoId));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
