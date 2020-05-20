package org.dbpedia.download;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;



public class Client
{
	private static String defaultTargetPath = "./data/";

	private static String defaultCollection = "https://databus.dbpedia.org/jan/collections/pre-release-de";

    public enum GraphMode  { 
    	NO_GRAPH, 
    	DOWNLOAD_URL
	}

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
		options.addOption("g", "graph-mode", true, "change the mode in which .graph files are created"); //TODO only one mode so far

		String targetPath = defaultTargetPath;
		String collection = defaultCollection;

		GraphMode gmode = GraphMode.NO_GRAPH;
		CommandLineParser cmdParser = new DefaultParser();

		try {

			CommandLine cmd = cmdParser.parse(options, args);

			if(cmd.hasOption("p")) {
				targetPath = cmd.getOptionValue("p");
			}

			if(cmd.hasOption("c")) {
				collection = cmd.getOptionValue("c");
			}

			if(cmd.hasOption("g")) {
				String mode = cmd.getOptionValue("g"); //TODO add support for more modes
				
                switch (mode) {
                    case "":
                    case "no":
                        gmode = GraphMode.NO_GRAPH;
                        break;
                    case "download-url":
                        gmode = GraphMode.DOWNLOAD_URL;
                        break;
                    default:
                        gmode = GraphMode.DOWNLOAD_URL;
                }
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
			

			String query = get("GET", collection, "text/sparql");
			

			System.out.println("Collections resolved to query:");
			System.out.println(query);
			
			// depending on running system, daytime or weather condition, the query is either already URL encoded or still plain text
			System.out.println("CHECKING FOR URLENCODED");
			System.out.println("RESULT: " + isURLEncoded(query));
			
			if(!isURLEncoded(query)) {
				query = URLEncoder.encode(query, "UTF-8");
			}
			
			String queryResult = query("https://databus.dbpedia.org/repo/sparql", query);
			
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
                String prefix = filename.substring(0,filename.indexOf('.'));
                String suffixes = filename.substring(filename.indexOf('.'));
                String hash = DigestUtils.md5Hex(file).toUpperCase().substring(0,4);
                String uniqname = prefix + "_" + hash + suffixes;
				
				InputStream in = new URL(file).openStream();
				Files.copy(in, Paths.get(targetPath + uniqname), StandardCopyOption.REPLACE_EXISTING);
				in.close();
				
				if(gmode != GraphMode.NO_GRAPH) {
                    Files.write(Paths.get(targetPath + uniqname + ".graph"), file.getBytes("UTF-8"));
				}
				
				System.out.println("File saved to " + targetPath + uniqname);
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

	private static String query(String endpoint, String query) throws ParseException, IOException {
		
		HttpClient client = HttpClientBuilder.create().build();
		
		String body = "default-graph-uri=&format=application%2Fsparql-results%2Bjson&query=" + query;
		
		HttpEntity entity = new ByteArrayEntity(body.getBytes("UTF-8"));
		
		HttpPost request = new HttpPost(endpoint);
		request.setEntity(entity);
		request.setHeader("Content-type", "application/x-www-form-urlencoded");
		// request.addHeader("Accept",  accept);
		HttpResponse response = client.execute(request);
		HttpEntity responseEntity = response.getEntity();
		
		if(responseEntity != null) {
		    return EntityUtils.toString(responseEntity);
		}
		return null;

	}

	private static boolean isURLEncoded(String query) {
		
		Pattern hasWhites = Pattern.compile("\\s+");
		Matcher matcher = hasWhites.matcher(query);
		
		return !matcher.find();
		
	}

	private static String get(String method, String urlString, String accept) throws IOException {

		System.out.println(method + ": " + urlString + " / ACCEPT: " + accept);
			
		HttpClient client = HttpClientBuilder.create().build();
		
		if(method.equals("GET")) {

			HttpGet request = new HttpGet(urlString);
			request.addHeader("Accept",  accept);
			HttpResponse response = client.execute(request);
			HttpEntity responseEntity = response.getEntity();
			
			if(responseEntity != null) {
			    return EntityUtils.toString(responseEntity);
			}
		}
	
		return null;
		
	}


}

