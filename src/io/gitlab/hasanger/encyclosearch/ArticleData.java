package io.gitlab.hasanger.encyclosearch;

import java.util.ArrayList;

public class ArticleData {

	public String encyclopediaName = "", encyclopediaImageName = "", articleTitle = "", shortDescription = "", url = "", searchUrl = "";
	
	public ArrayList<String> textLines = new ArrayList<>();
	
	public int relevance, levenshteinRelevance;
	
	public boolean equalTo(ArticleData a) {
		return (url.equals(a.url));
	}

}
