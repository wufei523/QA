package info.ephyra.search;

import info.ephyra.OpenEphyra;
import info.ephyra.answerselection.filters.HitPositionSorterFilter;
import info.ephyra.fei.ControllerVariables;
import info.ephyra.nlp.OpenNLP;
import info.ephyra.querygeneration.Query;
import info.ephyra.search.searchers.KnowledgeAnnotator;
import info.ephyra.search.searchers.KnowledgeMiner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.io.*;
import java.text.BreakIterator;
import java.util.Locale;

/**
 * <p>
 * The <code>Search</code> component queries several unstructured and
 * (semi)structured knowledge sources in parallel and aggregate the results.
 * </p>
 * 
 * <p>
 * Queries are instances of the class
 * <code>info.ephyra.querygeneration.Query</code>, results are instances of the
 * <code>Result</code> class in this package.
 * </p>
 * 
 * @author Nico Schlaefer
 * @version 2007-05-29
 */
public class Search
{
    /** The maximum number of parallel queries. */
    private static final int MAX_PENDING = 30;

    /**
     * <code>KnowledgeAnnotators</code> used to query (semi)structured knowledge
     * sources.
     */
    private static ArrayList<KnowledgeAnnotator> kas = new ArrayList<KnowledgeAnnotator>();
    /**
     * <code>KnowledgeMiners</code> used to query unstructured knowledge
     * sources.
     */
    private static ArrayList<KnowledgeMiner> kms = new ArrayList<KnowledgeMiner>();
    /** Results from different searches are aggregated in this field. */
    private static ArrayList<Result> results;
    /** Number pending of queries. */
    private static int pending;
    
    public static boolean use_fei_version = true;
    public static boolean print_debugging_info = true;

    /**
     * Searches the (semi)structured knowledge sources.
     * 
     * @param query
     *            query to be processed
     */
    private static void queryKAs(Query query)
    {
        for (int i = 0; i < kas.size(); i++)
            kas.get(i).start(query);
    }

    /**
     * Searches the unstructured knowledge sources.
     * 
     * @param query
     *            query to be processed
     */
    private static void queryKMs(Query query)
    {
        for (int i = 0; i < kms.size(); i++)
            kms.get(i).start(query);
    }

    /**
     * Delays the main thread until all queries have been completed.
     */
    private static void waitForResults()
    {
        synchronized (results)
        {
            while (pending > 0)
                try
                {
                    results.wait();
                }
                catch (InterruptedException e)
                {
                }
        }
    }

    /**
     * Drops duplicates among results from <code>KnowledgeMiners</code>.
     * 
     * @param results
     *            results with duplicates
     * @return results without duplicates
     */
    private static ArrayList<Result> dropDuplicates(ArrayList<Result> results)
    {
        // sort results by their hit positions in ascending order
        Result[] sorted = results.toArray(new Result[results.size()]);
        sorted = (new HitPositionSorterFilter()).apply(sorted);

        Set<Result> noDups = new HashSet<Result>();
        ArrayList<Result> remaining = new ArrayList<Result>();
        for (Result result : sorted)
            if (result.getScore() != Float.NEGATIVE_INFINITY
                    || noDups.add(result))
                remaining.add(result);

        return remaining;
    }

    /**
     * Registers a <code>KnowledgeAnnotator</code> for a (semi)structured
     * knowledge source.
     * 
     * @param ka
     *            <code>KnowledgeAnnotator</code> to add
     */
    public static void addKnowledgeAnnotator(KnowledgeAnnotator ka)
    {
        kas.add(ka);
    }

    /**
     * Registers a <code>KnowledgeMiner</code> for an unstructured knowledge
     * source.
     * 
     * @param km
     *            <code>KnowledgeMiner</code> to add
     */
    public static void addKnowledgeMiner(KnowledgeMiner km)
    {
        kms.add(km);
    }

    /**
     * Unregisters all <code>KnowledgeAnnotators</code>.
     */
    public static void clearKnowledgeAnnotators()
    {
        kas.clear();
    }

    /**
     * Unregisters all <code>KnowledgeMiners</code>.
     */
    public static void clearKnowledgeMiners()
    {
        kms.clear();
    }

    /**
     * Sends several alternative queries to all the searchers that have been
     * registered and returns the aggregated results.
     * 
     * @param queries
     *            queries to be processed
     * @return results returned by the searchers
     */
    public static Result[] doSearch(Query[] queries)
    {
        results = new ArrayList<Result>();
        pending = 0;

        
        
        if (!use_fei_version){
        // send only the first query to the KnowledgeAnnotators
        if (queries.length > 0)
            queryKAs(queries[0]);

        // send all queries to the KnowledgeMiners
        for (Query query : queries)
            queryKMs(query);

        // wait until all queries have been completed
        waitForResults();

        // drop duplicates among results from KnowledgeMiners
        results = dropDuplicates(results);
        
        //System.out.println("check result size(written in Search.java)");
        //System.out.println(results.size());
        //System.out.println("check result index=0 (written in Search.java)");
        //System.out.println(results.get(0).getAnswer());

        
        	
        	ArrayList<Result> first_10_result = new ArrayList<Result>();
        	System.out.println("Only return first 10 results... (written in Search.java)");
        	for (int i = 0; i<10; i++){
        		first_10_result.add(results.get(i));
        		System.out.println(i + "th result is (written in Search.java)");
                System.out.println(results.get(i).getAnswer());
        	}        	
        	
        	
        	return first_10_result.toArray(new Result[first_10_result.size()]);
        	
        	
        	//return results.toArray(new Result[results.size()]);
        }
        
        
        else{
        	// Now manually add result, by Fei
        	//added by Fei
        	
        	/*
			final File folder = new File("/Users/feiwu/Dropbox/16Summer/Papers/mainEval/manual/peers");
			File[] listofFiles = folder.listFiles(new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			        return name.contains("701");
			    }
			});
			System.out.println("How many files read = " + listofFiles.length);
			
			for (final File fileEntry : listofFiles) {
		        if (fileEntry.isFile()){
		        	String filename = fileEntry.getName();
		        	String topicName = filename.split("\\.")[0];
		        	String systemName = filename.split("\\.")[4];
        	*/
        	
        	
        	
            
            // The name of the file to open, knowledge source.
        	//String feismallfilename = "D0" + OpenEphyra.fei_topic + ".M.250.A." + OpenEphyra.fei_systemID;
            //String fileName = "/Users/feiwu/Dropbox/16Summer/Papers/mainEval/manual/peers/"+feismallfilename;
            //String fileName = "/home/fw/Dropbox/16Summer/Papers/mainEvalALL/manual/peers/"+feismallfilename;
            
        	File knowledgeSourceFileName = null;
        	String fileName = null;
            //find knowledge file
            //File folder = new File("/home/fw/Dropbox/16Summer/Papers/mainEvalALL/manual/peers");
            //mac
            File folder = new File(ControllerVariables.pathToTopicDocuments);
            
            
            File[] listOfSummarizationForAllTopics = folder.listFiles();
            for (int i = 0; i < listOfSummarizationForAllTopics.length; i++) {
          	  File file = listOfSummarizationForAllTopics[i];
          	  String[] temp1 = file.getName().split("\\.");
          	  if (temp1[0].contains(Integer.toString(ControllerVariables.currentTopicId)) && temp1[4].equals(ControllerVariables.currentSystemId)){
          		knowledgeSourceFileName = file;
          		fileName = file.getName();
          	  }
          	}
            
            
            
            
            // This will reference one line at a time
            String line = null;
            String fei_s = "";
            try {
                // FileReader reads text files in the default encoding.
                FileReader fileReader = new FileReader(knowledgeSourceFileName);
                System.out.println("Opened file as input knowledge source: " + knowledgeSourceFileName.getName());

                // Always wrap FileReader in BufferedReader.
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                while((line = bufferedReader.readLine()) != null) {
                	fei_s += line;
                    //System.out.println(line);
                }   

                // Always close files.
                bufferedReader.close();         
            }
            catch(FileNotFoundException ex) {
                System.out.println(
                    "Unable to open file '" + 
                    fileName + "'");   
                System.exit(1);
            }
            catch(IOException ex) {
                System.out.println("Error reading file '" + fileName + "'");                  
                // Or we could just do this: 
                // ex.printStackTrace();
                System.exit(1);
            } 
            
            
            
            
            BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
            ArrayList<String> fei_input_sentences = new ArrayList<String>();
            iterator.setText(fei_s);
            int start = iterator.first();
            for (int end = iterator.next();
                end != BreakIterator.DONE;
                start = end, end = iterator.next()) {
            	fei_input_sentences.add(fei_s.substring(start,end));
            }
            
            
            //filter sentences, keep the ones similar to the question:
            ArrayList<String> fei_input_sentences_filtered = new ArrayList<String>();
            //use my tokenizer, better, doesnt have digits
            String regex = "([^a-zA-Z']+)'*\\1*";
            String[] question_tokends = ControllerVariables.currentQuestion.split(regex);
            String[] pos_question = OpenNLP.tagPos(question_tokends);
            //System.out.println(question_tokends.length);
            
            //use this program's tokenizer
            //String[] question_tokens_2 = OpenNLP.tokenize(OpenEphyra.current_question);
            
            ArrayList<String> question_content_tokens = new ArrayList<String>();
            for (int i=0; i<question_tokends.length; i++){
            	if (checkPOS(pos_question[i])){
            		question_content_tokens.add(question_tokends[i]);
            		//System.out.println(question_tokends[i] + " " + pos_question[i] + " is CONTENT WORD");
            	}
            	//else {
            		//System.out.println(question_tokends[i] + " " + pos_question[i]);
            	
        	}
        	//System.out.println();
        	/*
        	for (int i=0; i<question_tokens_2.length; i++){
        		System.out.println(question_tokens_2[i]);
        	}
        	System.out.println();
            */
            
            int how_many_matches =0;
            double percentage_of_matches_in_question = 0;
            
            //System.out.println("Check each input sentence:");
            for (String sentence : fei_input_sentences){            	
            	String[] sentence_tokens = sentence.split(regex);
            	
            	//count how many matches
            	how_many_matches = findMatchCount(question_tokends, sentence_tokens);
            	percentage_of_matches_in_question = (double)how_many_matches/(question_content_tokens.size());
            	
            	if (percentage_of_matches_in_question >= 0.9){
            		fei_input_sentences_filtered.add(sentence);
            	}
            	
            	
            	//System.out.println(question_tokends.length);
            	//System.out.println("sentence is: " + sentence);
            	//System.out.println("Question is: " + OpenEphyra.current_question);
            	//System.out.println(how_many_matches);
            	
            	//System.out.println(percentage_of_matches_in_question);
            	//System.out.println();
            	/*
            	for (int i=0; i<sentence_tokens.length; i++){
            		System.out.println(sentence_tokens[i]);
            	}
            	System.out.println();
            	*/
            }
            
            
            
            
            
            
           //System.exit(0);
            
            
            ArrayList<Result> fei_results = new ArrayList<Result>();
            
            //here use filtered sentences
            for (String sentence : fei_input_sentences_filtered){
            //for (String sentence : fei_input_sentences){
    	        for (Query query : queries){
    	        	Result fei_result = new Result(sentence,query);
    	        	fei_result.setScore(0);
    	            fei_results.add(fei_result);
    	            System.out.println(fei_result.getAnswer());
    	        }
            }
            

            return fei_results.toArray(new Result[fei_results.size()]);
        }
        
        
    }

    /**
     * Delays a thread until there are less than MAX_PENDING pending queries.
     */
	public static void waitForPending() {
		synchronized (results) {
			while (pending >= MAX_PENDING)
				try {
					results.wait();
				} catch (InterruptedException e) {
				}
		}
	}

    /**
     * Increments the number of pending queries by 1.
     */
    public static void incPending()
    {
        synchronized (results)
        {
            pending++;
        }
    }

    /**
     * Used by <code>Searchers</code> to return the results found in the
     * knowledge sources.
     * 
     * @param results
     *            results found in the knowledge sources
     */
    public static void addResults(Result[] results)
    {
        synchronized (Search.results)
        {
            for (Result result : results)
                Search.results.add(result);

            pending--;
            Search.results.notify(); // signal that the query is completed
        }
    }
    
    
    
    
    
    //added by Fei
    public static int findMatchCount(final String [] question,final String [] sentence){
        int matchCount = 0;
        /*
        //check out the elements
        System.out.print("QUESTION: ");
        for (int i=0; i<question.length; i++){
        	
    		System.out.print(question[i].toLowerCase() + " ");
    	}
    	System.out.println();
    	
    	
    	//check out the elements
    	System.out.print("SENTENCE: ");
        for (int i=0; i<sentence.length; i++){
    		System.out.print(sentence[i].toLowerCase() + " ");
    	}
    	System.out.println();
    	*/
        
        for (int i = 0; i<question.length; i++){
        	boolean skip = false;
        	for (int j=0; j<sentence.length; j++){
        		//compare question and all sentence tokens
        		int res = (question[i].toLowerCase()).compareTo((sentence[j]).toLowerCase()); 
        		if (res == 0){
        			matchCount++;
        			//System.out.print(question[i].toLowerCase()+" ");
        			skip = true;
        			break;
        		}
        	}
        	
        	/*
        	if (skip){
    			continue;
    		}
    		*/
        	
        }
        
        /*
        for(int i = 0, j = 0;i < a.length && j < b.length;){
            int res = a[i].compareTo(b[j]);
            if(res == 0){
                matchCount++;
                i++;
                j++;
            }else if(res < 0){
                i++;
            }else{
                j++;
            }
        }
        */
        return matchCount;
    }
    
    
    
    
    
    public static boolean checkPOS(String word_pos){
    	//String[] pos_list = {"NN", "NNS", "JJ", "RB", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ"};
    	String[] pos_list = {"NN", "NNS", "JJ", "RB", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "NNP", "NNPS", "CD", "JJR", "JJS", "RBR", "RBS", "SYM"};
    	if (Arrays.asList(pos_list).contains(word_pos)){
    		return true;
    	}
    	else
    		return false;
    }
    
}
