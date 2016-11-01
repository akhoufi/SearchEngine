package Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import Common.Utils;
import Common.Constants;
import Common.FrenchStemmer;
import Common.FrenchTokenizer;
import Common.Normalizer;

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
			String wordList="";
			for (File subDir : subDirs) {
				File[] subDirs2 = subDir.listFiles();
				for (File subDir2 : subDirs2) {
					File outFile = new File(
							outDir.getPath() + "/" + subDir.getName() + "_" + subDir2.getName() + ".ind");
					wordList = Utils.getInvertedFile(subDir2, normalizer, postingsMap, wordList, wordListFile,outFile);

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

//	// Gets document frequency of words using the index generated, unlike what
//	// we did in TP
//	//Je n'ai pas changé cette méthode (à toi de l'adapter, j'ai créé une fonction dans Utils qui récupère l'index nécessaire pour les DF (getDFIndex) 
//	//Mais il est en <Integer, Integer> car je garde le mot en tant que position (comme on en a parlé) 
//	public static HashMap<String, Integer> getDft(File invertedFile) throws IOException {
//		HashMap<String, Integer> hits = new HashMap<String, Integer>();
//		String line = null;
//		String[] words = null;
//		Scanner sc = new java.util.Scanner(invertedFile);
//
//		while (sc.hasNext()) {
//			line = sc.nextLine();
//			words = line.split("\t");
//			hits.put(words[0], Integer.parseInt(words[1]));
//
//		}
//		sc.close();
//
//		return hits;
//	}
//
//	// Gets document frequency of a word using the index generated
//	public static Integer getDfOfWord(File invertedFile, String word) throws IOException {
//		String line = null;
//		String[] words = null;
//		Scanner sc = new java.util.Scanner(invertedFile);
//		Integer dft = 0;
//		while (sc.hasNext()) {
//			line = sc.nextLine();
//			words = line.split("\t");
//			if (word.equals(words[0])) {
//				dft = Integer.parseInt(words[1]);
//				sc.close();
//				return dft;
//			}
//		}
//		if (dft == 0) {
//			System.out.println(word + " getDfOfT() = 0 ");
//		}
//		sc.close();
//
//		return dft;
//	}

	// Creates the .poids files of each page in the corpus
	// inDir : textDir
	// outDir : weightsDir
	// invertedFile : index file of the whole corpus
	public static void saveWeightFiles(File inDir, File outDir, File invertedFile, String wordList, Normalizer normalizer)
			throws IOException {

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
//		// SaveInvertedFilesByPacks
//		try {
//			Normalizer stemmerNoStopWords = new FrenchStemmer(new File(STOPWORDS_FILENAME));
//			Normalizer tokenizerNoStopWords = new FrenchTokenizer(new File(STOPWORDS_FILENAME));
//
//			// Construct and save postings (files) index
//			LinkedHashMap<String, Integer> postingsMap = Utils.ConstructPostingsMap(new File(TEXT_DIR));
//			Utils.savePostingsMap(postingsMap, new File(POSTING_INDEX_FILE));
//			File wordListStemFile=new File(WORD_LIST_STEM_FILE);
//			if(wordListStemFile.exists()){
//				wordListStemFile.delete();
//			}
//			wordListStemFile.createNewFile();
//			
//			File wordListTokenFile=new File(WORD_LIST_TOKEN_FILE);
//			if(wordListTokenFile.exists()){
//				wordListTokenFile.delete();
//			}
//			wordListTokenFile.createNewFile();
////			saveInvertedFileByPack(new File(TEXT_DIR), stemmerNoStopWords, new File(INVERTED_INDEXES_STEM_DIR),
////					postingsMap, wordListStemFile);
//			saveInvertedFileByPack(new File(TEXT_DIR), tokenizerNoStopWords, new File(INVERTED_INDEXES_TOKEN_DIR),
//					postingsMap, wordListTokenFile);
////
//			 //MergeManyInvertedFiles
//			 File outStemDir = new File(FINAL_INDEX_STEM_DIR);
//			 if (!outStemDir.exists()) {
//			 outStemDir.mkdir();
//			 }
//			
//			 File outTokenDir = new File(FINAL_INDEX_TOKEN_DIR);
//			 if (!outTokenDir.exists()) {
//			 outTokenDir.mkdir();
//			 }
//			
////			 mergeManyInvertedFiles(new File(INVERTED_INDEXES_STEM_DIR), new
////			 File(FINAL_STEM_INDEX));
//			 mergeManyInvertedFiles(new File(INVERTED_INDEXES_TOKEN_DIR), new
//			 File(FINAL_TOKEN_INDEX));
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		
		if(true){
			//Creation des poids  token
			String wordList = Utils.getWordList(new File(WORD_LIST_TOKEN_FILE));
			Normalizer tokenizerNoStopWords = new FrenchTokenizer(new File(STOPWORDS_FILENAME));
			IndexGenerator.saveWeightFiles(new File(TEXT_DIR), new File(WEIGHT_FILES_TOKEN_DIR), new File(FINAL_TOKEN_INDEX), wordList, tokenizerNoStopWords);
			
		}
		
		if(false){
			//Creation des poids  stem
			String wordList = Utils.getWordList(new File(WORD_LIST_STEM_FILE));
			Normalizer stemmerNoStopWords = new FrenchStemmer(new File(STOPWORDS_FILENAME));
			IndexGenerator.saveWeightFiles(new File(TEXT_DIR), new File(WEIGHT_FILES_STEM_DIR), new File(FINAL_STEM_INDEX), wordList, stemmerNoStopWords);
			
		}
		
	}
}
