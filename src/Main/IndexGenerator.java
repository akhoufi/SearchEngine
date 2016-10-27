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
import Common.FrenchStemmer;
import Common.Normalizer;

public class SearchEngineGenerator {
	
	//Comment these and put yours if you want to test
	protected static String textDir = "C:/Users/Younes/Desktop/M2 AIC/TC3 - Recherche et extraction d'informations dans les textes/github/text/2015";
	protected static String dataDir = "C:/Users/Younes/Desktop/M2 AIC/TC3 - Recherche et extraction d'informations dans les textes/github/data/2015";
	protected static String invertedFilesDir = "C:/Users/Younes/Desktop/M2 AIC/TC3 - Recherche et extraction d'informations dans les textes/github/invertedFiles";
	protected static String indexDir = "C:/Users/Younes/Desktop/M2 AIC/TC3 - Recherche et extraction d'informations dans les textes/github/index"; 	// where to store the final invertedFile
	protected static String weightsDir = "C:/Users/Younes/Desktop/M2 AIC/TC3 - Recherche et extraction d'informations dans les textes/github/weights"; 
	protected static String corpusDir = "C:/Users/Younes/Desktop/M2 AIC/TC3 - Recherche et extraction d'informations dans les textes/github/corpus"; 	
	protected static String resultsDir = "C:/Users/Younes/Desktop/M2 AIC/TC3 - Recherche et extraction d'informations dans les textes/github/results"; 

	//Save inverted files by pack = 1 day 
	public static void saveInvertedFilesByPacks(File dir, Normalizer normalizer, File outDir) throws IOException {
		
		TreeMap<String, TreeSet<String>> invertedF = new TreeMap<>();
		
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		
		if(dir.isDirectory()){
			File[] subDirs = dir.listFiles();
			
			for(File subDir : subDirs){
				File[] subDirs2 = subDir.listFiles();
				
					for(File subDir2 : subDirs2){
						
						File outFile = new File(outDir.getPath()+"/"+subDir.getName()+"_"+subDir2.getName()+".ind");
						invertedF = Utils.getInvertedFile(subDir2, normalizer);
						Utils.saveInvertedFile(invertedF, outFile);
		
					}
				
				
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
	
	//merge inverted files from dir directory to mergedInvertedFile file
	//J'utilise 2 fichiers car sinon je vais utiliser le mm fichier comme entree et sortie de la fusion ! 
	public static void mergeManyInvertedFiles(File dir, File mergedInvertedFile) throws IOException{
		
		int i = 2;
		
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			// fichier temp pour ne pas utiliser mergedInvertedFile comme entree et sortie de mergeInvertedFiles
			//  sinon on va ecraser du contenu  
			File temp = new File(mergedInvertedFile.getParentFile()+"/temp.ind");	
			Utils.mergeInvertedFiles(files[0], files[1], mergedInvertedFile);
			//Files.copy( mergedInvertedFile.toPath(), temp.toPath());
			//copyFile(mergedInvertedFile, temp);
			while(i<= files.length-2){
				Utils.mergeInvertedFiles(mergedInvertedFile, files[i], temp);
				Utils.mergeInvertedFiles(temp, files[i+1], mergedInvertedFile);
				System.out.println("Fusion avec "+files[i].getName()+" et "+files[i+1].getName());
				i = i+2;
			}
			copyFile(mergedInvertedFile, temp);
			Utils.mergeInvertedFiles(temp, files[i], mergedInvertedFile);
			System.out.println("Fusion avec "+files[i].getName());

			
			
		}
	}

	
	//Find pages containing the query keywords and returns their names
	// A optimiser ulterieurement, ne pas parcourir touuuuut l'index, A faire plus tard si on arrive à avoir une première version du moteur
	public static Set<String> getPages(String query, File invertedFile, Normalizer normalizer ) throws FileNotFoundException{
		Set<String> pages = new HashSet<>();
		ArrayList<String> keyWordsList = normalizer.normalize(query.toLowerCase());
		
		String line = null;
		String[] words = null;
		String word = null;
		String[] documents = null;
			
		Scanner sc = new java.util.Scanner(invertedFile);
		
		while(sc.hasNext()) {
			line = sc.nextLine();
			words = line.split("\t");
			word = words[0];
			
			for(String keyWord : keyWordsList){
				if (word.equals(keyWord)){
					documents = words[2].split(",");
					for(String page : documents){
						pages.add(page);
						
					}
					
				}
		}
			
		}
		
		sc.close();

		return pages;
	}
	
//	//Set a directory with the pages from the text folder (construction du corpus lié à la requete et transformation de la requete en document)
//	// indir : le chemin do dossier text 
//	public static void queryToDoc(String query,  File outDir, Normalizer normalizer ) throws IOException{ 
//		
//		if (!outDir.exists()) {
//			outDir.mkdirs();
//		}
//		
//		// on écrit la requete dans un fichier
//		try {
//			FileWriter fw = new FileWriter (new File(outDir.getAbsolutePath()+"/query.txt"));
//			BufferedWriter bw = new BufferedWriter (fw);
//			PrintWriter out = new PrintWriter (bw);
//			// Ecriture des mots			
//			out.println(query.toLowerCase()); 
//			out.close();
//		}
//		catch (Exception e){
//			System.out.println(e.toString());
//		}
//	}
	
	//Set a directory with the pages from the text folder (construction du corpus lié à la requete et transformation de la requete en document)
	// indir : le chemin do dossier text 
	public static void setPagesDir(Set<String> pages, String query, File inDir, File outDir) throws IOException{
		String pageDir = inDir.getAbsolutePath();
		String[] nameParts = null;
		String[] pageDate = null;
		String year = null;
		String month = null;
		String day = null; 
		
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		
		// on écrit la requete dans un fichier
		try {
			FileWriter fw = new FileWriter (new File(outDir.getAbsolutePath()+"/query.txt"));
			BufferedWriter bw = new BufferedWriter (fw);
			PrintWriter out = new PrintWriter (bw);
			// Ecriture des mots			
			out.println(query.toLowerCase()); 
			out.close();
		}
		catch (Exception e){
			System.out.println(e.toString());
		}

		
		//On copie le corpus dans le dossier outDir, à cote du document de la requete
		for(String page : pages){
			nameParts = page.split("_");
			pageDate = nameParts[0].split("");
			year = pageDate[0]+pageDate[1]+pageDate[2]+pageDate[3];
			month = pageDate[4]+pageDate[5];
			day = pageDate[6]+pageDate[7];
			pageDir = inDir.getAbsolutePath()+"/"+month+"/"+day+"/"+page;			
			copyFile(new File(pageDir), new File(outDir.getAbsolutePath()+"/"+page));
			
		}
		
	}

	
	// Pour calculer les fichier .poids, on utilise directement la fonction :
	// 	public static void getWeightFiles(File inDir, File outDir, Normalizer normalizer) throws IOException {
	// deja codé dans Utils.java , c'est pour cela que j'ai créé la fonction setPagesDir()
	// afin de l'utiliser directement
	// La requete aura aussi son fichier .poids puisqu'on l'a mis avec les documents du corpus 
	
	

	
	// Classement des pages repondant à la requete par similarite decroissante dans un fichier
	// La requete est considere comme un document, son .poids doit se trouver dans le dossier 
	//   inDir a cote des  .poids des autres pages
	// 
	public static void getSimilarPages(File inDir, File outFile) throws IOException {
		File query = new File(inDir.getAbsolutePath()+"/query.poids");
		File[] files = inDir.listFiles();
		Set<File> file_list = new HashSet<File>(Arrays.asList(files));
		file_list.remove(query); //On supprime query pour ne pas calculer la similarite entre le mm fichier
		Utils.getSimilarDocuments(query,file_list, outFile);
		
	}
	
	//Choisir les pages les plus similaires à afficher
	//TODO
	public static void getPagesToDisplay() throws IOException {
		
	}

	
	//Faire la liaison entre les noms de hashage et le nom de ces pages pour les afficher
	//TODO
	public static void mapPageHashToURL() throws IOException {
		
	}

	
	
	
	
	
	
	public static void main(String[] args) throws IOException {
		//test saveInvertedFilesByPacks
		if(false){
			try {
				Normalizer stemmer = new FrenchStemmer();
				saveInvertedFilesByPacks(new File(textDir),  stemmer, new File(invertedFilesDir));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//test mergeManyInvertedFiles
		if(false){
			try {
				String mergedFile = indexDir +"/index.ind";
				mergeManyInvertedFiles(new File(invertedFilesDir), new File(mergedFile));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(true){
			// test de la réponse à une requete
			Normalizer stemmer = new FrenchStemmer();
			String query = "charlie hebdo";
			Set<String> pagesList = new HashSet<>();
			File results = new File(resultsDir+"/results.txt");
			
			pagesList = getPages(query, new File(indexDir+"/index.ind"), stemmer);
			
			setPagesDir(pagesList, query, new File(textDir), new File(corpusDir));
			Utils.getWeightFiles( new File(corpusDir),  new File(weightsDir), stemmer);
			getSimilarPages(new File(weightsDir), results);

		}

		
		
	}
	
	
}
