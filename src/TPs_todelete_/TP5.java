package TPs_todelete_;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


public class TP5 {
	private static String outDirName = "/home/tp-home010/akhoufi/Téléchargements/tmp/sorties/tools.FrenchStemmer";
	public static double getSimilarity(File file1, File file2) throws IOException{
		HashMap<String, Double> hits1 = new HashMap<String, Double>();
		HashMap<String, Double> hits2 = new HashMap<String, Double>();
		TreeSet<String> wordsList = new TreeSet<String>();
		BufferedReader br1 = new BufferedReader(new FileReader(file1));
		BufferedReader br2 = new BufferedReader(new FileReader(file2));
	    String line = br1.readLine();
	    String[] wordTfId = null;
	    double num = 0.0, denom1 = 0.0, denom2 = 0.0, w1, w2;
	    
	    
	    while ((line = br1.readLine()) != null) {
	    	wordTfId = line.split("\t");
	    	wordsList.add(wordTfId[0]);
			hits1.put(wordTfId[0], Double.parseDouble(wordTfId[1]));
		}
	    while ((line = br2.readLine()) != null) {
	    	wordTfId = line.split("\t");
	    	wordsList.add(wordTfId[0]);
			hits2.put(wordTfId[0], Double.parseDouble(wordTfId[1]));
		}
	    br1.close();
	    br2.close();
	    
	    for(String word : wordsList){
    		if (hits1.get(word) != null){
    			w1 = hits1.get(word);
    			
    		}
    		else{
    			w1 = 0.0;
    		}
    		
    		if (hits2.get(word) != null){
    			w2 = hits2.get(word);
    			
    		}
    		else{
    			w2 = 0.0;
    		}
    		
    		num += w1*w2;
    		denom1 += w1*w1;
    		denom2 += w2*w2; 	
	    	
	    }
	    
	    return num/(Math.sqrt(denom1)*Math.sqrt(denom2));
			
	}
	

	public static void getSimilarDocuments(File file, Set<File> fileList) throws IOException{
		HashMap<String, Double> hits = new HashMap<String, Double>();
		String fileName = null;
		double similarity; 
		
		
		for(File file2 : fileList){
			hits.put(file2.getName(), getSimilarity(file, file2));		
		}
		
		Comparator<String> comparator = new ValueComparator<String, Double>(hits);
		TreeMap<String, Double> sortedHits = new TreeMap<String, Double>(comparator);
		sortedHits.putAll(hits);
		
		FileWriter fw = new FileWriter (new File(outDirName+"/documentsSimilarity.txt"));
		BufferedWriter bw = new BufferedWriter (fw);
		PrintWriter out = new PrintWriter (bw);
		for (Map.Entry<String, Double> hit : sortedHits.entrySet()) {
			fileName = hit.getKey();
			similarity = hit.getValue();
			out.println(fileName + "\t" + similarity); 			
		}
		out.close();
		
	}
	public static void main(String[] args) {
		try {
			String inFileName = outDirName+"/texte.95-1.poids";
			File dir= new File(outDirName);
			if (dir.isDirectory()) {
				File[] files = dir.listFiles();
				Set<File> file_list = new HashSet<File>(Arrays.asList(files));
				getSimilarDocuments(new File(inFileName),file_list);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

// a comparator using generic type, source : http://www.programcreek.com/2013/03/java-sort-map-by-value/
class ValueComparator<K, V extends Comparable<V>> implements Comparator<K>{
 
	HashMap<K, V> map = new HashMap<K, V>();
 
	public ValueComparator(HashMap<K, V> map){
		this.map.putAll(map);
	}
 
	@Override
	public int compare(K s1, K s2) {
		return -map.get(s1).compareTo(map.get(s2));//descending order	
	}
	
	
}
