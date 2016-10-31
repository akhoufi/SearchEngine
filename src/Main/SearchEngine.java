package Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import Common.Constants;
import Common.FrenchStemmer;
import Common.Normalizer;
import Common.Utils;

public class SearchEngine implements Constants {
	// Find pages containing the query keywords and returns their names
	// A optimiser ulterieurement, ne pas parcourir touuuuut l'index, A faire
	// plus tard si on arrive à avoir une première version du moteur
	public static Set<String> getPages(String query, File invertedFile, Normalizer normalizer)
			throws FileNotFoundException {
		Set<String> pages = new HashSet<>();
		ArrayList<String> keyWordsList = normalizer.normalize(query.toLowerCase());

		String line = null;
		String[] words = null;
		String word = null;
		String[] documents = null;

		Scanner sc = new java.util.Scanner(invertedFile);

		while (sc.hasNext()) {
			line = sc.nextLine();
			words = line.split("\t");
			word = words[0];

			for (String keyWord : keyWordsList) {
				if (word.equals(keyWord)) {
					documents = words[2].split(",");
					for (String page : documents) {
						pages.add(page);

					}

				}
			}

		}

		sc.close();

		return pages;
	}

	// On considère la requete comme un document et on calcul son .poids et on
	// le met a cote des autres (dans le dossier weights : query.poids)
	public static void saveQueryWeights(String query, File invertedFile, File textDirectory, File outDir,
			Normalizer normalizer) throws IOException {

		ArrayList<String> keyWords = normalizer.normalize(query);
		HashMap<String, Integer> dfs = IndexGenerator.getDft(invertedFile);
		int documentNumber = IndexGenerator.getNbDocuments(textDirectory);
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		// on écrit dans un fichier
		try {
			FileWriter fw = new FileWriter(new File(outDir.getAbsolutePath() + "/query.poids"));
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			Integer tf;
			Double tfIdf;
			// Ecriture des mots
			for (String keyWord : keyWords) {
				tf = 1;
				tfIdf = (double) tf * Math.log((double) documentNumber / (double) dfs.get(keyWord));
				out.println(keyWord + "\t" + tfIdf);
			}
			out.close();
			bw.close();
			fw.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	// Classement des pages repondant à la requete par similarite decroissante
	// dans un fichier outFile
	// inDir : contient tous les fichiers .poids
	public static void getSimilarPages(File inDir, File outFile) throws IOException {
		File query = new File(inDir.getAbsolutePath() + "/query.poids");
		File[] files = inDir.listFiles();
		Set<File> file_list = new HashSet<File>(Arrays.asList(files));
		file_list.remove(query); // On supprime query pour ne pas calculer la
									// similarite entre le mm fichier
		Utils.getSimilarDocuments(query, file_list, outFile);

	}

	// Choisir les pages les plus similaires à afficher
	// TODO
	public static void getPagesToDisplay() throws IOException {

	}

	// Faire la liaison entre les noms de hashage et le nom de ces pages pour
	// les afficher
	// TODO
	public static String mapPageHashToURL(String id, File dir) {
		try {
			if (dir.isDirectory()) {
				String[] fileNames = dir.list();

				Integer number;
				for (String fileName : fileNames) {
					File fXmlFile = new File(SUB_INDEX_DIR);
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder;
					dBuilder = dbFactory.newDocumentBuilder();
					Document docs = dBuilder.parse(fXmlFile);
					NodeList docList = docs.getElementsByTagName("doc");
					for (int i = 0; i < docList.getLength(); i++) {
						Node doc = docList.item(i);
						if (doc.getNodeType() == Node.ELEMENT_NODE) {

							Element eDoc = (Element) doc;
							if (eDoc.getAttribute("id").equals(id)) {
								return eDoc.getAttribute("url");
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws IOException {
		// test de la réponse à une requete
		Normalizer stemmer = new FrenchStemmer();
		String query = "charlie hebdo";
		Set<String> pagesList = new HashSet<>();
		File results = new File(RESULTS_DIR + "/results.txt");
		pagesList = getPages(query, new File(FINAL_INDEX_STEM_DIR + "/index.ind"), stemmer);
		saveQueryWeights(query, new File(FINAL_INDEX_STEM_DIR + "/index.ind"), new File(TEXT_DIR),
				new File(WEIGHT_FILES_FIR), stemmer);
		getSimilarPages(new File(WEIGHT_FILES_FIR), results);

	}
}
