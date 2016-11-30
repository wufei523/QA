package info.ephyra.search.searchers;

import info.ephyra.io.MsgPrinter;
import info.ephyra.search.Result;
import info.ephyra.search.searchers.bingwrappers.DataContainer;
import info.ephyra.search.searchers.bingwrappers.SearchResult;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;

import com.aliasi.util.Collections;
import com.google.gson.Gson;

import org.apache.commons.codec.binary.Base64;
import java.io.*;

/**
 * <p>
 * A <code>KnowledgeMiner</code> that deploys the Bing Azure search engine to
 * search the Web. Reference:
 * https://datamarket.azure.com/dataset/bing/searchweb
 * </p>
 * 
 * <p>
 * It runs as a separate thread, so several queries can be performed in
 * parallel.
 * </p>
 * 
 * <p>
 * This class extends the class <code>KnowledgeMiner</code>.
 * </p>
 * 
 * @author Scott Jones
 * @version 2014-03-13
 */
public class BingAzureKM extends KnowledgeMiner
{
    // Bing account ID (replace this with your registered Azure account ID):
    //This is my account primary ID from Azure
    private static final String BING_AZURE_ID = "g+TzT0asQfdug2RqaGB55t1S7Rzq9kIdLVRNG430ZOY";
    
    // Bing base URL for web queries:
    private static final String BING_API_URL = "https://api.datamarket.azure.com/Bing/SearchWeb/Web?Query=%27";

    // Limits for search results - these can be adjusted according to desired
    // trade-off (higher results = more relevant answers, but much slower performance):
    private static final int MAX_RESULTS_TOTAL = 25;
    private static final int MAX_RESULTS_PERQUERY = 5;

    @Override
    protected int getMaxResultsTotal()
    {
        return MAX_RESULTS_TOTAL;
    }

    @Override
    protected int getMaxResultsPerQuery()
    {
        return MAX_RESULTS_PERQUERY;
    }

    /**
     * Returns a new instance of <code>BingAzureKM</code>. A new instance is
     * created for each query.
     * 
     * @return new instance of <code>BingAzureKM</code>
     */
    @Override
    public KnowledgeMiner getCopy()
    {
        return new BingAzureKM();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Result[] doSearch()
    {
        
    	//System.out.println("NOW doing Bing search for query: " + query.getQueryString());
    	//System.out.println("--------------------------------------------------------");
    	//System.out.println("");
        // Perform the search:

        String bingUrl = BING_API_URL + java.net.URLEncoder.encode(query.getQueryString()) + "%27&$format=JSON";

        byte[] accountKeyBytes = Base64.encodeBase64((BING_AZURE_ID + ":" + BING_AZURE_ID).getBytes());
        String accountKeyEnc = new String(accountKeyBytes);

        StringBuffer sb = new StringBuffer();

        
        
        
        try
        {
            URL url = new URL(bingUrl);
            //URLConnection urlConnection = url.openConnection();
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            String s1 = "Basic " + accountKeyEnc;
            urlConnection.setRequestProperty("Authorization", s1);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(25000);
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null){
                sb.append(inputLine);
                //
                //System.out.println(inputLine);
            }
            in.close();
        }
        catch (Exception ex)
        {
        	//System.out.print("THIS IS A TEST MESSAGE!!");
            MsgPrinter.printSearchError(ex);
        }
        
        
        /*
       // Now manually add result, by Fei
        
        
        // The name of the file to open.
        String fileName = "temp.txt";
        // This will reference one line at a time
        String line = null;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
            	sb.append(line);
                System.out.println(line);
            }   

            // Always close files.
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");                  
            // Or we could just do this: 
            // ex.printStackTrace();
        }
        */
        
        
        
        
        // Collect the results:

        ArrayList<String> snippets = new ArrayList<String>();
        ArrayList<String> urls = new ArrayList<String>();

        // Deserialize the JSON string into a DataContainer object:
        Gson gson = new Gson();
        DataContainer myDataContainer = gson.fromJson(sb.toString(),DataContainer.class);
        
        if (myDataContainer != null)
        {
            // Fill the results collection with the description and URL
            // from the search results:
            SearchResult[] search_results = myDataContainer.d.results;
            for (SearchResult result : search_results)
            {
                snippets.add(result.Description);
                urls.add(result.Url);
            }
        }
        
        //take a look at how result looks like
        //System.out.println(snippets.get(0));
        //System.out.println(urls.get(0));

        // Return results:
        return getResults(Collections.toStringArray(snippets),
            Collections.toStringArray(urls), true);
    }
}
