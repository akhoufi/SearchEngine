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
