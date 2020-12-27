package io.gitlab.hasanger.encyclosearch;

import java.io.IOException;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import info.debatty.java.stringsimilarity.Levenshtein;

public class Encyclopedia {

	// Levenshtein instance, used for calculating Levenshtein distance (how similar two Strings are)
	public static Levenshtein l = new Levenshtein();

	// Various properties
	public String name, imageName, searchUrl, titleSelector, shortDescriptionSelector, urlSelector, textToRemove;

	// The code duplicates Wikipedia results for some reason, and duplicateResultFix toggles a fix for this.
	public boolean duplicateResultFix = false, requiresPageLoading = false;

	/**
	 * Create a new Encyclopedia.
	 * @param encyclopediaObject A JSONObject containing the required information
	*/
	public Encyclopedia(JSONObject encyclopediaObject) {
		this.name = encyclopediaObject.getString("name");
		this.imageName = encyclopediaObject.getString("imageName");
		this.searchUrl = encyclopediaObject.getString("searchUrl");
		this.titleSelector = encyclopediaObject.getString("titleSelector");
		this.shortDescriptionSelector = encyclopediaObject.getString("shortDescriptionSelector");
		this.urlSelector = encyclopediaObject.getString("urlSelector");
		this.textToRemove = encyclopediaObject.getString("textToRemove");
		this.duplicateResultFix = encyclopediaObject.getBoolean("duplicateResultFix");
		this.requiresPageLoading = encyclopediaObject.getBoolean("requiresPageLoading");
	}

	/**
	 * Search this Encyclopedia.
	 * @param query The keyword to search for
	 * @param resultIndex The index of the result to fetch
	*/
	public ArticleData search(Document document, String query, int resultIndex) throws IOException {

		// Instantiate ArticleData object
		ArticleData data = new ArticleData();

		// Assign all the values we can right now
		data.encyclopediaName = this.name;
		data.encyclopediaImageName = this.imageName;
		data.searchUrl = this.searchUrl.replace("QUERY", query);

		if ( document == null) {

			System.out.println("Document is NULL. ! Exit");
			return null;
		};


		try {

			// Get article title
			data.articleTitle = clean(document.select(titleSelector).get(resultIndex)).trim();

			// Get short description
			data.shortDescription = clean(document.select(shortDescriptionSelector).get(resultIndex)).trim();

			// Get article URL (not link to search page, direct link to article)
			data.url = clean(document.select(urlSelector).get(resultIndex).attr("abs:href")).trim();


			// Calculate relevance

			// Modified article title, used for sorting only
			String articleTitle = data.articleTitle.replace(textToRemove, "") // Remove text that should be removed
			                      .replaceAll("\\([^\\)]*\\)",""); // Remove parentheses

			// Add +2000 to relevance if title is exact match (case sensitive)
			if(articleTitle.equals(query)) data.relevance += 2000;

			// Add +1000 to relevance if title is exact match (not case sensitive)
			if(articleTitle.equalsIgnoreCase(query)) data.relevance += 1000;

			String[] splitQuery = query.split(" ");

			// Add +100 to relevance if title contains all keywords
			boolean containsAll = true;
			for(String keyword : splitQuery) {
				if(!articleTitle.contains(keyword)) containsAll = false;
			}
			if(containsAll) data.relevance += 100;

			// Add +50 to relevance if title contains some keywords
			boolean containsSome = false;
			for(String keyword : splitQuery) {
				if(articleTitle.contains(keyword)) {
					containsSome = true;
					break;
				}
			}
			if(containsSome) data.relevance += 50;


			// Calculate Levenshtein relevance (how similar the query and the article title are)
			data.levenshteinRelevance = similarity(articleTitle.toLowerCase(), query.toLowerCase());

			// Add 1000 to the levenshtein relevance if the title doesn't contain the query (higher is less relevant)
			if(!data.articleTitle.toLowerCase().contains(query.toLowerCase())) data.levenshteinRelevance += 1000;

		} catch(IndexOutOfBoundsException ioobe) {
			return null;
		}


		return data;
	}

	// Removes HTML tags from the given String/object.
	// If it isn't a String, it will be converted to a String.
	public static String clean(Object obj) {
		return Jsoup.clean(obj.toString(), Whitelist.none());
	}

	// Used to calculate Levenshtein distance
	public static int similarity(String one, String two) {
		return (int) l.distance(one, two);
	}

}
