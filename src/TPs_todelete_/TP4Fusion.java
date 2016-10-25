package TPs_todelete_;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class TP4Fusion {



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
			if(word2.equals("500")){
				System.out.println(line2);
			}
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
	}

	public static void main(String[] args) {
		try {
			String inFileName = "/home/tp-home010/akhoufi/Téléchargements/index1.ind";
			String inFileName2 = "/home/tp-home010/akhoufi/Téléchargements/index2.ind";
			String inFileName3 = "/home/tp-home010/akhoufi/Téléchargements/index_sortie.ind";
			mergeInvertedFiles(new File(inFileName), new File(inFileName2), new File(inFileName3));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
