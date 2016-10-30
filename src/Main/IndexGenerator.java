package Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import Common.Utils;
import Common.Constants;
import Common.FrenchStemmer;
import Common.Normalizer;

public class IndexGenerator implements Constants {

	// Save inverted files by pack = 1 day
	public static void saveInvertedFileByPack(File dir, Normalizer normalizer, File outDir) throws IOException {

		TreeMap<String, TreeSet<String>> invertedFile = new TreeMap<>();

		if (!outDir.exists()) {
			outDir.mkdirs();
		}

		if (dir.isDirectory()) {
			File[] subDirs = dir.listFiles();

			for (File subDir : subDirs) {
				File[] subDirs2 = subDir.listFiles();

				for (File subDir2 : subDirs2) {
					
					File outFile = new File(
							outDir.getPath() + "/" + subDir.getName() + "_" + subDir2.getName() + ".ind");
					invertedFile = Utils.getInvertedFile(subDir2, normalizer);
					Utils.saveInvertedFile(invertedFile, outFile);

				}

			}

		}

	}

	
	// merge inverted files from dir directory to mergedInvertedFile file
	// J'utilise 2 fichiers car sinon je vais utiliser le mm fichier comme
	// entree et sortie de la fusion !
	// TODO : changer cette méthode ça aprend bcp de temps !!!!
	public static void mergeManyInvertedFiles(File dir, File mergedInvertedFile) throws IOException {

		int i = 2;

		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			// fichier temp pour ne pas utiliser mergedInvertedFile comme entree
			// et sortie de mergeInvertedFiles
			// sinon on va ecraser du contenu
			File temp = new File(mergedInvertedFile.getParentFile() + "/temp.ind");
			Utils.mergeInvertedFiles(files[0], files[1], mergedInvertedFile);
			// Files.copy( mergedInvertedFile.toPath(), temp.toPath());
			// copyFile(mergedInvertedFile, temp);
			while (i <= files.length - 2) {
				Utils.mergeInvertedFiles(mergedInvertedFile, files[i], temp);
				Utils.mergeInvertedFiles(temp, files[i + 1], mergedInvertedFile);
				System.out.println("Fusion avec " + files[i].getName() + " et " + files[i + 1].getName());
				i = i + 2;
			}
			Utils.copyFile(mergedInvertedFile, temp);
			Utils.mergeInvertedFiles(temp, files[i], mergedInvertedFile);
			System.out.println("Fusion avec " + files[i].getName());

		}
	}
	
		
	// Gets the number of documents in the corpus
	public static int getNbDocuments(File dir){
		int nb = 0;
		if (dir.isDirectory()) {
			File[] subDirs = dir.listFiles();

			for (File subDir : subDirs) {
				File[] subDirs2 = subDir.listFiles();

				for (File subDir2 : subDirs2) {
					
					nb+= subDir2.listFiles().length;

				}

			}

		}
		return nb;
		
	}
	
	// Gets document frequency of words using the index generated, unlike what we did in TP
	public static HashMap<String, Integer> getDft(File invertedFile) throws IOException {
		HashMap<String, Integer> hits = new HashMap<String, Integer>();
		String line = null;
		String[] words = null;
		Scanner sc = new java.util.Scanner(invertedFile);

		while (sc.hasNext()) {
			line = sc.nextLine();
			words = line.split("\t");
			hits.put(words[0],  Integer.parseInt(words[1]));

		}
		sc.close();
		
		return hits;
	}
	
	
	// Gets document frequency of a word using the index generated
	public static Integer getDfOfWord(File invertedFile, String word) throws IOException {
		String line = null;
		String[] words = null;
		Scanner sc = new java.util.Scanner(invertedFile);
		Integer dft = 0;
		while (sc.hasNext()) {
			line = sc.nextLine();
			words = line.split("\t");
			if(word.equals(words[0])){
				dft = Integer.parseInt(words[1]);
				sc.close();
				return dft;
			}		
		}
		if(dft == 0){
			System.out.println(word +" getDfOfT() = 0 ");
		}
		sc.close();
		
		return dft;
	}
	
	// Creates the .poids files of each page in the corpus 
	// inDir : textDir
	// outDir : weightsDir
	// invertedFile : index file of the whole corpus
	public static void saveWeightFiles(File inDir, File outDir, File invertedFile, Normalizer normalizer) throws IOException {
	
		File[] files = null;
		// calcul des dfs
		System.out.println("Calcul des dfs \n");
		HashMap<String, Integer> dfs = getDft(invertedFile);
		// Nombre de documents	
		System.out.println("Calcul du nb des documents \n");
		int documentNumber = getNbDocuments(inDir);
		// Creation de la destination 
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		
		File[] subDirs = inDir.listFiles();
		
		System.out.println("Creation des .poids \n");

		for (File subDir : subDirs) {
			File[] subDirs2 = subDir.listFiles();

			for (File subDir2 : subDirs2) {				
				files = subDir2.listFiles();
				// TfIdfs 				
				System.out.println("Creation des .poids \n");
				for (File file : files) {
					HashMap<String, Double> tfIdfs = Utils.getTfIdf(file, dfs, documentNumber, normalizer);
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

		}
		
		
	}
	

	public static void main(String[] args) throws IOException {
		// SaveInvertedFilesByPacks
		try {
			Normalizer stemmer = new FrenchStemmer();
			saveInvertedFileByPack(new File(TEXT_DIR), stemmer, new File(INVERTED_INDEXES_DIR));

			// MergeManyInvertedFiles
			String mergedFile = FINAL_INDEX_DIR + "/index.ind";
			mergeManyInvertedFiles(new File(INVERTED_INDEXES_DIR), new File(mergedFile));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
