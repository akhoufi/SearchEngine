package Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
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
	protected static String indexDir = "C:/Users/Younes/Desktop/M2 AIC/TC3 - Recherche et extraction d'informations dans les textes/github/index"; 	// where to store the final invertedFile


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
	//TODO : J'utilise 2 fichiers car sinon je vais utiliser le mm fichier comme entree et sortie de la fusion ! 
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
				String mergedFile = indexDir +"/index.ind";
				mergeManyInvertedFiles(new File(invertedFilesDir), new File(mergedFile));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		
		
	}
	
	
}
