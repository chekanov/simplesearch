package io.gitlab.encsearch;


import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.gitlab.hasanger.encyclosearch.SearchResults;
import io.gitlab.hasanger.encyclosearch.*;
import io.gitlab.hasanger.encyclosearch.util.*;

public class EnSearch
{


	// Logger
	// static Logger logger = LoggerFactory.getLogger(EncycloSearchServer.class);
	// The number of results to load for each encyclopedia.
	// Can't be in the main() method because it's used in a lambda.
	static int numResults = 5;

	static boolean onlyTitles=false;

	// Counter, used to limit cache size.
	static int counter = 0;

	static public String HomeArea="";

	private static String TYPE_WIKI="<img src='img/wiki.png' style='vertical-align:middle;margin:0;'/>";

        //private static String TYPE_SEARCH="<img src='img/search.png' style='vertical-align:middle;margin:0;auto;'/>";
         private static String TYPE_SEARCH="<img src='img/search.png' title='Find' />";

	// Cache of SearchResults
	public static ArrayList<SearchResults> cache = new ArrayList<SearchResults>();



	private static String getTextAround(String str, String word) {


		int maxcha=100;
		String TXT=str.toLowerCase();
		String WORD=word.toLowerCase();


		int ilen=str.length();
		int iwordsize=word.length();

		int pos1=TXT.indexOf(WORD);
		if (pos1<0) return str;
		if (pos1+iwordsize>ilen) return str;

		int xpos0=pos1-maxcha;
		if (xpos0<0) xpos0=0;

		int xpos2=pos1+iwordsize+maxcha;
		if (xpos2>ilen) xpos2=ilen;

		String leftPart=str.substring(xpos0, pos1);
		String rightPart=str.substring(pos1+iwordsize, xpos2);
		WORD=str.substring(pos1, pos1+iwordsize);

		//leftPart = leftPart.trim().replaceAll("\n", "");
                //rightPart = rightPart.trim().replaceAll("\n", "");

		if (leftPart.startsWith("..") == false) leftPart=" ... "+leftPart;
		if (rightPart.endsWith("..") == false)  rightPart=rightPart+" ... ";

		String Sdesc=leftPart+"<b>"+WORD+"</b>"+rightPart;

		return  Sdesc;
	};


	// last argument is home area location for the encyclopedia.json file
	public static String process(String[] args, String homearea) throws Exception {

		if (args.length <4) {
			return "No search arguments! Shoule be: Start Hits Type INDEX_PATH Word1 Word2 Word3 Word4 Word5";
			//System.exit(0);
		}


		HomeArea=homearea;
		//System.out.println(args.length);

		int Offset=Integer.parseInt( args[0].trim() );
		int Limit=Integer.parseInt( args[1].trim() );

		// type=0 titles
		// type=1  - full text

		int type=Integer.parseInt( args[2].trim() );
		String INDEX_DIR=args[3].trim();
		String word1=args[4].trim();

		String SEARCH=word1;

		String word2="";
		if (args.length==6) {word2=args[5].trim(); SEARCH=word1+" "+word2;};
		String word3="";
		if (args.length==7) {word3=args[6].trim(); SEARCH=word1+" "+word2+" "+word3;};
		String word4="";
		if (args.length==8) {word4=args[7].trim(); SEARCH=word1+" "+word2+" "+word3+" "+word4;};
		String word5="";
		if (args.length==9) {word5=args[8].trim(); SEARCH=word1+" "+word2+" "+word3+" "+word4+" "+word5;};
		String word6="";
		if (args.length==10) {word6=args[9].trim(); SEARCH=word1+" "+word2+" "+word3+" "+word4+" "+word5+" "+word6;};


		try {
			JSONObject config = new JSONObject(new String(Files.readAllBytes(new File(homearea+File.separator+"config.json").toPath())));
			numResults = config.getInt("numResults");
		} catch (Exception e) {
			e.printStackTrace();
		}


		int ntot=0;
		String bestResult="";
		// overwitite
		numResults=Limit;
		onlyTitles=true;
		if (type==1) onlyTitles=false;

		// The SearchResults object
		SearchResults results = Encyclopedias.searchAll(SEARCH, numResults, Offset, Limit,onlyTitles); // Get results, but don't cache them

		String RES = "<ol>\n";

		String searchUrl="";
		// String tmp="";
		for(ArticleData data : results.results) {
			//System.out.println(results.results);
			//System.out.println(data.encyclopediaName);
			//System.out.println(data.encyclopediaImageName);
			//System.out.println(data.articleTitle);
			//System.out.println(data.shortDescription);
			//System.out.println(data.searchUrl);
			//System.out.println(data.url);
			//tmp=tmp+data.encyclopediaName+"\n"+data.encyclopediaImageName+"\n"+data.articleTitle+"\n"+data.shortDescription+"\n"+data.searchUrl;

			ntot++;
			if (ntot == 1) bestResult=data.articleTitle;

			// some fix - EOL returns empty string for data.url
			if (data.encyclopediaImageName.indexOf("eol")>-1)
                                   data.url=data.searchUrl; // EOL has more complex direct link (cannot be retrieved)
	    
			if (data.url.length()<1) continue;


			RES=RES+"<li>";

			// correct search string
			searchUrl=WikiString.correctSearch(data.searchUrl,onlyTitles,Offset, Limit);
                        TYPE_WIKI="<img src='img/"+data.encyclopediaImageName+"_s.png' title='" + data.encyclopediaName +"'/>";
			String desc=getTextAround(data.shortDescription,SEARCH);
                        String tmp="<span class=\"x1\"> "+ TYPE_WIKI + " <a href='"+searchUrl+"'>"+TYPE_SEARCH+"</a> <a href='"+data.url+"'>"+data.articleTitle+"</a> </span> <span class=\"x2\"> "+ desc + "</span>";

			RES=RES+tmp+"</li>\n";

		};


		RES=RES+"</ol>\n";

/*		// append best article (server specific) 
		try {

			final String filename= "/var/www/html/jwork/public_html/history.log" ;
			FileWriter fw = new FileWriter(filename,true); //the true will append the new data
			fw.write(bestResult+"\n");//appends the string to the file
			fw.close();
		}
		catch(IOException ioe)
		{
			System.err.println("IOException: " + ioe.getMessage());
		}
*/

		return RES;


	}

}

