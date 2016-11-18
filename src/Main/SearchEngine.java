
package Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import Common.Constants;
import Common.FrenchStemmer;
import Common.FrenchTokenizer;
import Common.Normalizer;
import Common.Utils;

public class SearchEngine implements Constants {
	// Cherche les pages contenant les mots clées et retournent un Set<File> des
	// .poids correspondant à ces pages
	public static Set<File> getPages(String query, File invertedFile, File postingFile, File weightsDir,
			String wordList, Normalizer normalizer) throws IOException {
		Set<File> pages = new HashSet<>();
		ArrayList<String> keyWordsList = normalizer.normalize(query.toLowerCase());
		ArrayList<Integer> keyWordsPos = new ArrayList<Integer>();
		LinkedHashMap<Integer, String> positionMap = Utils.getPostingsMap(postingFile);
		String line = null;
		String[] words = null;
		String wordInd = null;
		String[] documentsIndexes;
		String page;

		Scanner sc = new java.util.Scanner(invertedFile);

		for (String keyWord : keyWordsList) {
			keyWordsPos.add(Utils.getWordPosition(wordList, keyWord));
		}

		System.out.println("Recherche des fichiers contenant les mots clées");
		while (sc.hasNext()) {
			line = sc.nextLine();
			words = line.split("\t");
			wordInd = words[0];

			for (Integer wordPos : keyWordsPos) {
				if (wordInd.equals(String.valueOf(wordPos))) {
					documentsIndexes = words[2].split(",");
					for (String docIndex : documentsIndexes) {
						page = positionMap.get(Integer.valueOf(docIndex));
						page = weightsDir.getAbsolutePath() + "/" + page.replaceAll(".txt$", ".poids");
						pages.add(new File(page));
					}
				}
			}

		}

		sc.close();

		return pages;
	}

	// On considère la requete comme un document et on calcule son .poids et on
	// l'enregistre dans le dossier outDir
	public static void saveQueryWeights(String query, String wordList, File invertedFile, File textDirectory,
			File outDir, Normalizer normalizer) throws IOException {

		System.out.println("sauvegarde des poids de la requete");

		ArrayList<String> keyWords = normalizer.normalize(query.toLowerCase());
		HashMap<Integer, Integer> dfs = Utils.getDFIndex(invertedFile);
		int documentNumber = IndexGenerator.getNbDocuments(textDirectory);
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		// on écrit dans un fichier
		try {
			File queryFile = new File(outDir.getAbsolutePath() + "/"+query+".poids");
			if (queryFile.exists()) {
				queryFile.delete();
			}
			queryFile.createNewFile();
			FileWriter fw = new FileWriter(queryFile);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			Integer tf;
			Integer keyWordInd;
			Double tfIdf;
			// Ecriture des mots
			for (String keyWord : keyWords) {
				keyWordInd = Utils.getWordPosition(wordList, keyWord);
				tf = 1;
				tfIdf = (double) tf * Math.log((double) documentNumber / (double) dfs.get(keyWordInd));
				out.println(keyWordInd + "\t" + tfIdf);
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
	// wieghtsFilesList : Set<File> contenant les file .poids contenant un des
	// mots
	// clées de la requete
	public static void getSimilarPages(File queryWeights, Set<File> wieghtsFilesList, File outFile) throws IOException {
		System.out.println("calcul de similarité et classement");
		Utils.getSimilarDocuments(queryWeights, wieghtsFilesList, outFile);
		System.out.println("Resultats générés");
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

		try {
			// Generate Token results
			String[] query = { "Charlie Hebdo", "volcan", "playoffs NBA", "accidents d’avion", "laïcité",
					"elections l´egislatives", "Sepp Blatter", "budget de la d´efense", "Galaxy S6", "Kurdes" };
			Normalizer tokenizerNoStopWords = new FrenchTokenizer(new File(STOPWORDS_FILENAME));
			Normalizer stemmerNoStopWords = new FrenchStemmer(new File(STOPWORDS_FILENAME));
			File postingFile = new File(POSTING_INDEX_FILE);

			// Generate Token results
			File invertedTokenFile = new File(FINAL_TOKEN_INDEX);
			File weightsTokenDir = new File(WEIGHT_FILES_TOKEN_DIR);
			File wordListTokenFile = new File(WORD_LIST_TOKEN_FILE);
			File textDir = new File(TEXT_DIR);
			File queryWeightDir = new File(QUERY_WEIGHTS_DIR);
			File resultTokenDir = new File(RESULTS_DIR_TOKEN);
			if (!resultTokenDir.exists()) {
				resultTokenDir.mkdir();
			}
			String wordTokenList = Utils.getWordList(wordListTokenFile);

			for (int i = 0; i < query.length; i++) {
				File resultsFile = new File(RESULTS_DIR_TOKEN + "/" + query[i] + ".txt");
				Set<File> pagesList = getPages(query[i], invertedTokenFile, postingFile, weightsTokenDir, wordTokenList,
						tokenizerNoStopWords);

				saveQueryWeights(query[i], wordTokenList, invertedTokenFile, textDir, queryWeightDir, tokenizerNoStopWords);

				File queryWeightsFile = new File(queryWeightDir.getAbsolutePath() + "/"+query[i]+".poids");

				getSimilarPages(queryWeightsFile, pagesList, resultsFile);

			}
			// Generate STEM results
			File invertedStemFile = new File(FINAL_STEM_INDEX);
			File weightsStemDir = new File(WEIGHT_FILES_STEM_DIR);
			File wordListStemFile = new File(WORD_LIST_STEM_FILE);
			File resultStemDir = new File(RESULTS_DIR_STEM);
			if (!resultStemDir.exists()) {
				resultStemDir.mkdir();
			}
			String wordStemList = Utils.getWordList(wordListStemFile);

			for (int i = 0; i < query.length; i++) {
				File resultsFile = new File(RESULTS_DIR_STEM + "/" + query[i] + ".txt");
				Set<File> pagesList = getPages(query[i], invertedStemFile, postingFile, weightsStemDir, wordStemList,
						stemmerNoStopWords);

				saveQueryWeights(query[i], wordStemList, invertedStemFile, textDir, queryWeightDir, stemmerNoStopWords);

				File queryWeightsFile = new File(queryWeightDir.getAbsolutePath() + "/"+query[i]+".poids");

				getSimilarPages(queryWeightsFile, pagesList, resultsFile);

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
