package Main;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.TreeSet;
import Common.Utils;
import Common.FrenchStemmer;
import Common.Normalizer;

public class SearchEngineGenerator {
	
	//Comment these and put yours if you want to test
	protected static String textDir = "C:/Users/Younes/Desktop/M2 AIC/TC3 - Recherche et extraction d'informations dans les textes/github/text";
	protected static String dataDir = "C:/Users/Younes/Desktop/M2 AIC/TC3 - Recherche et extraction d'informations dans les textes/github/data";
	protected static String invertedFilesDir = "C:/Users/Younes/Desktop/M2 AIC/TC3 - Recherche et extraction d'informations dans les textes/github/invertedFiles";

	//Save inverted files by pack = 1 day 
	public static void saveInvertedFilesByPacks(File dir, Normalizer normalizer, File outDir) throws IOException {
		
		TreeMap<String, TreeSet<String>> invertedF = new TreeMap<>();
		
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
	
	//merge inverted files from dir directory to mergedInvertedFile file
	//TODO : verifier si on peux fusionner deux fichiers et mettre le resultat dans l'un d'eux 
	public static void mergeManyInvertedFiles(File dir, File mergedInvertedFile) throws IOException{
		
		int i;
		
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			Utils.mergeInvertedFiles(files[0], files[1], mergedInvertedFile);
			for(i=2; i< files.length; i++){
				
				Utils.mergeInvertedFiles(mergedInvertedFile, files[i], mergedInvertedFile);
				System.out.println("Fusion avec "+files[i].getName());
			}
			
			
		}
	}

	
	
	public static void main(String[] args) {
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
		if(true){
			try {
				String mergedFile = invertedFilesDir +"/index.ind";
				mergeManyInvertedFiles(new File(invertedFilesDir), new File(mergedFile));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		
		
	}
	
	
}
