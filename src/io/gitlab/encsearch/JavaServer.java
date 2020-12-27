package io.gitlab.encsearch;

import java.io.*;
import java.net.*;

// Start java server and listern.

public class JavaServer {
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

			// split clinet string using :::
			String[] arrOfStr = fromClient.split(":::");


			// sent to client!
			try {
				out.println(EnSearch.process( arrOfStr, homearea ));
			}  catch (Exception e) { }







		}
		System.exit(0);
	}
}

