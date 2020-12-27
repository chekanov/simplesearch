package io.gitlab.hasanger.encyclosearch;

import java.util.ArrayList;

public class SearchResults {
	
	public String query;
	
	public ArrayList<ArticleData> results = new ArrayList<>();
	
	public SearchResults(String query, ArrayList<ArticleData> results) {
		this.query = query;
		this.results = results;
	}

}
