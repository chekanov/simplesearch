package io.gitlab.encsearch;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;


// Start java server and listern.
// We use a smiple caching (without experation) since we have a lot of memmory
// Note: this cache is not on disk, so rebut will remove it
// S.Chekanov
public class JavaServer {


        private static ConcurrentHashMap<String, String> enCache = new ConcurrentHashMap<>();
        
        private static final long MaxCounts=10000000; // 10M is max association 	


	public static void main(String args[]) throws Exception {
		String fromClient;
		String toClient;

		if (args.length<1) {
			System.out.println("EnSearch SEARCH SERVER MESSAGE: Exit. You must to pass home area with json files");
			System.exit(0);
		}

		String homearea=args[0].strip();

		ServerSocket server = new ServerSocket(8082);
		System.out.println("EnSearch SEARCH SERVER MESSAGE: wait for connection on port 8082");

		boolean run = true;
		while(run) {
			Socket client = server.accept();
			System.out.println("EnSearch SEARCH SERVER MESSAGE: on port 8082");
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter out = new PrintWriter(client.getOutputStream(),true);

			fromClient = in.readLine();
			System.out.println("EnSearch SERVER MESSAGE:" + fromClient);

			//out.println("Process result");

			if(fromClient.equals("exit-ensearch")) {
				//toClient = "eyB";
				//System.out.println("send eyB");
				//out.println(toClient);
				client.close();
				run = false;
				System.out.println("EnSearch SERVER MESSAGE: Got exit signal. Socket 8082 closed");
				break;
			}

			// split client string using :::
			//long istart = System.currentTimeMillis(); 
			// find it in cache..
                        String key=fromClient.toLowerCase();
			if (enCache.containsKey(key) && enCache.mappingCount()<MaxCounts) {
                             String output=(String)enCache.get(key);
                             out.println(output);// set cache to client 

			} else { 

			     // sent to client real result!
        		     try {
                                String[] arrOfStr = fromClient.split(":::"); 
		       		String output=EnSearch.process( arrOfStr, homearea );
		                enCache.put(fromClient.toLowerCase(),output); // put to cache 	
				out.println( output );
			    }  catch (Exception e) { }

			 } // end non-caching 

	         	//long iend = System.currentTimeMillis(); 
                        //System.out.println(iend-istart);
                        // out.println(iend-istart);




		}
		System.exit(0);
	}
}

