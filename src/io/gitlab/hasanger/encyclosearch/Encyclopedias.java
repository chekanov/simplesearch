package io.gitlab.hasanger.encyclosearch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.gitlab.hasanger.encyclosearch.util.DocumentWrapper;
import io.gitlab.hasanger.encyclosearch.util.EncodingUtil;
import io.gitlab.hasanger.encyclosearch.util.WikiString;

public class Encyclopedias {
	
	// Logger for the class
	//static Logger logger = LoggerFactory.getLogger(Encyclopedias.class);
	
	// List of encyclopedias
	public static ArrayList<Encyclopedia> encyclopedias = new ArrayList<Encyclopedia>();
	
	// Static initializer. Loads all encyclopedias and puts them in the encyclopedias array
	static {
		try {


			String xfile=io.gitlab.encsearch.EnSearch.HomeArea+File.separator+"encyclopedias.json";
			// Load the file
			JSONObject encyclopediasObject = new JSONObject(new String(Files.readAllBytes(new File(xfile).toPath())));
			
			// Get the JSONArray of encyclopedias and convert it to an ArrayList
			JSONArray encyclopediasArray = encyclopediasObject.getJSONArray("encyclopedias");
			for(int i = 0; i < encyclopediasArray.length(); i++) {
				encyclopedias.add(new Encyclopedia((JSONObject) encyclopediasArray.get(i)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Search all encyclopedias.
	 * @param query What to search for
	 * @param poffset  starting page 
         * @param plimit   how many to show 
	 * @param onlyTitles shows only title or not 
	 * @param numResults The number of results to load from each encyclopedia
	*/
	public static SearchResults searchAll(String query, int numResults,  int poffset, int plimit, boolean onlyTitles) {
		
		//logger.info("Beginning search for \"" + query + "\"...");

                //System.out.println("Beginning search for \"" + query + "\"...");

		
		long searchStart = System.currentTimeMillis();
		
		// List of articles to return
		ArrayList<ArticleData> dataList = new ArrayList<>();
		
		// Jsoup Document object
		DocumentWrapper document = new DocumentWrapper();
		
		// Clear list of results
		dataList.clear();
		
		// Make a list of threads, so we can block until all threads are done executing
		ArrayList<Thread> threads = new ArrayList<>();
		
		CountDownLatch threadLatch = new CountDownLatch(encyclopedias.size());
		
		// Loop over all encyclopedias
		for(Encyclopedia encyclopedia : encyclopedias) {
			
			// Create a new thread for every encyclopedia, querying each simultaneously to save time
			Thread t = new Thread(() -> {
				
				long start = System.currentTimeMillis();
			
                                //  System.out.println("Loading " + encyclopedia.name + " page...");	
				//logger.info("Loading " + encyclopedia.name + " page...");
				
				String searchUrl = encyclopedia.searchUrl.replace("QUERY", EncodingUtil.encodeURIComponent(query));

				// correct search string
			        searchUrl=WikiString.correctSearch(searchUrl,onlyTitles,poffset,plimit);	

				// Check if the encyclopedia loads results with JS,
				// and retrieve the content different ways depending on this
					// Otherwise, just retrieve the page without loading
					try {
						document.document = Jsoup.connect(searchUrl).timeout(15 * 1000).get();
					} catch (IOException e) {
						e.printStackTrace();
					}
				
				// For every result...
				for(int i = 0; i < (encyclopedia.duplicateResultFix ? numResults+1 : numResults); i++) {
					
					// Scrape the website and add the results to the array
					try {
						ArticleData data = encyclopedia.search(document.document, query, i);
						if(data != null) dataList.add(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
				
				long end = System.currentTimeMillis();
				float sec = (end - start) / 1000F;
				//logger.info("Done loading " + encyclopedia.name + " page. Took " + sec + "s"); // Log
				
				threadLatch.countDown();
			});
			threads.add(t); // Add it to the array
		}
		
		// Block until all threads are finished executing,
		// so that the array isn't sorted and returned before it's done
		for(Thread t : threads) t.start();
		try {
			threadLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Remove duplicate results. These appear sometimes for some reason.
		// A new variable must be created because the other one is final.
		// It's final because it's used in a lambda.
		ArrayList<ArticleData> finalDataList = removeDuplicates(dataList);
		
		// Sort data twice: once by relevance (see Encyclopedia class),
		// and once by Levenshtein distance (how similar the article titles are to the query)
		
		// First sort: by Levenshtein distance
		Collections.sort(finalDataList, (a1, a2) -> {
			return a1.levenshteinRelevance - a2.levenshteinRelevance;
		});
		
		// Second sort: by relevance
		Collections.sort(finalDataList, (a1, a2) -> {
			return a2.relevance - a1.relevance;
		});
		
		long searchEnd = System.currentTimeMillis();
		float sec = (searchEnd - searchStart) / 1000F;
		//logger.info("Finished search for \"" + query + "\". Took " + sec + "s"); // Log
		
		// Return the results
		return new SearchResults(query, finalDataList);
	}
	
	// Function to remove duplicates from an ArrayList
	// https://www.geeksforgeeks.org/how-to-remove-duplicates-from-arraylist-in-java/
    public static ArrayList<ArticleData> removeDuplicates(ArrayList<ArticleData> list) { 
  
        // Create a new ArrayList 
        ArrayList<ArticleData> newList = new ArrayList<ArticleData>();
  
        // Traverse through the first list 
        for(ArticleData element : list) {
        	
        	boolean containsElement = false;
        	
        	for(ArticleData newElement : newList) {
        		if(newElement.articleTitle.equals(element.articleTitle)) containsElement = true;
        	}
        	
        	if(!containsElement) newList.add(element);
        } 
  
        // return the new list 
        return newList; 
    }

}
