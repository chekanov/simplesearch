package io.gitlab.hasanger.encyclosearch.util;




public class WikiString {


	/**
	 * Correct string for pagination.  
	 * @param query What to search for
	 * @param onlyTitles shows only title or not
	* @param poffset  starting page
	 * @param plimit   how many to show
	 * @param onlyTitles shows only title or not
	 * @param corrected URL 
	 */


	public static String correctSearch(String searchUrl, boolean onlyTitles, int poffset, int plimit){

		// when we do not like titles
		if (onlyTitles) searchUrl = searchUrl.replace("&fulltext=1","");

		searchUrl = searchUrl.replace("&offset=:OFFSET:","&offset="+Integer.toString(poffset));
		searchUrl = searchUrl.replace("&limit=:LIMIT:","&limit="+Integer.toString(plimit));

		// handwiki style
		searchUrl = searchUrl.replace("&start=:OFFSET:","&start="+Integer.toString(poffset));

		// britanica has a special counting
		// offset is usually 20, 40 etc, so we do simple convertion
		int page=1;
		if (poffset==0) page=1;
		else if (poffset==20) page=2;
		else if (poffset==40) page=3;
		else if (poffset==60) page=4; // fix  later
		else page=(poffset-20) / 10;
		searchUrl = searchUrl.replace("&page=:OFFSET:","&page="+Integer.toString(page));


		return searchUrl;
	};
}
