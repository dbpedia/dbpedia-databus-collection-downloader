package org.dbpedia.download;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Hello world!
 *
 */
public class Client 
{
	private static String defaultTargetPath = "./data/";

	private static String defaultCollection = "https://databus.dbpedia.org/collections/dbpedia/databus";

	/**
	 * Quick and dirty implementation of a download client, downloading the contents of a 
	 * Databus collection. Code is ugly and I know it! 
	 * This download process will be replaced by the official databus download client as soon as it 
	 * takes less time to install than writing this code :)
	 * @param args
	 */
	public static void main( String[] args )
	{
		
		Options options = new Options();
		options.addOption("p", "path", true, "The data path");
		options.addOption("c", "collection", true, "The config file path");

		String targetPath = defaultTargetPath;
		String collection = defaultCollection;

		CommandLineParser cmdParser = new DefaultParser();

		try {

			CommandLine cmd = cmdParser.parse(options, args);

			if(cmd.hasOption("p")) {
				targetPath = cmd.getOptionValue("p");
			}

			if(cmd.hasOption("c")) {
				collection = cmd.getOptionValue("c");
			}
			
			if(!targetPath.endsWith("/")) {
				targetPath += "/";
			}
			
			File directory = new File(targetPath);

			if(!directory.exists() && !directory.mkdir()) {
				System.out.println("Target path " + targetPath + " could not be created.");
				return;
			}
			
			System.out.println("Loading collection " + collection);
			

			String query = get(collection, "text/sparql");
			

			System.out.println("Collections resolved to query:");
			System.out.println(query);
			
			String sparqlQuery = "https://databus.dbpedia.org/repo/sparql?default-graph-uri=&format=application%2Fsparql-results%2Bjson&query=";
			
			// depending on running system, daytime or weather condition, the query is either already URL encoded or still plain text
			System.out.println("CHECKING FOR URLENCODED");
			System.out.println("RESULT: " + isURLEncoded(query));
			
			if(!isURLEncoded(query)) {
				query = URLEncoder.encode(query, "UTF-8");
			}
			
			sparqlQuery += query;
			
			String queryResult = get(sparqlQuery, null);
			
			ArrayList<String> files = new ArrayList<String>();
			
			JSONObject obj = new JSONObject(queryResult);	
			
			JSONArray bindings = obj.getJSONObject("results").getJSONArray("bindings");
			
			for (int i = 0; i < bindings.length(); i++)
			{
				JSONObject binding = bindings.getJSONObject(i);
				String key = binding.keys().next();
				
				JSONObject result = binding.getJSONObject(key);
				files.add(result.getString("value"));
			}
			
			for(String file : files) {
				
				System.out.println("Downloading file: " + file);
				
				String filename = file.substring(file.lastIndexOf('/') + 1);
				
				InputStream in = new URL(file).openStream();
				Files.copy(in, Paths.get(targetPath + filename), StandardCopyOption.REPLACE_EXISTING);
				
				System.out.println("File saved to " + targetPath + filename);
			}
			
			System.out.println("Done.");

		} catch (org.apache.commons.cli.ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static boolean isURLEncoded(String query) {
		
		Pattern hasWhites = Pattern.compile("\\s+");
		Matcher matcher = hasWhites.matcher(query);
		
		return !matcher.find();
		
	}

	private static String get(String urlString, String accept) throws IOException {


		System.out.println("GET: " + urlString + " / ACCEPT: " + accept);
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setRequestMethod("GET");
		
		if(accept != null) {
			con.setRequestProperty("Accept", accept);
		}
		
		con.connect();

		int status = con.getResponseCode();

		if(status == HttpURLConnection.HTTP_OK) {

			String content = readContent(con);
			con.disconnect();
			return content;
		}
		return null;	
	}

	private static String readContent(HttpURLConnection con) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

		String inputLine;
		StringBuffer content = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
			content.append("\n");
		}

		in.close();
		
		return content.toString();
	}

}
