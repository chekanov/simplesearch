package io.gitlab.encsearch;


import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import io.gitlab.hasanger.encyclosearch.*;
import io.gitlab.hasanger.encyclosearch.util.*;

public class Main
{


	// Logger
	// static Logger logger = LoggerFactory.getLogger(EncycloSearchServer.class);

	// The number of results to load for each encyclopedia.
	// Can't be in the main() method because it's used in a lambda.
	static int numResults = 20;

	// Counter, used to limit cache size.
	// cacheSize, the maximum allowed size of the cache.
	static int counter = 0, cacheSize = 50;

	// Cache of SearchResults
	public static ArrayList<SearchResults> cache = new ArrayList<SearchResults>();



	public static void main(String[] args) throws Exception
	{


		// split clinet string using :::
		String[] arrOfStr = args[0].split(":::");
		String config=args[1];

		String results=EnSearch.process( arrOfStr, config);
		System.out.println(results);

	}

}

