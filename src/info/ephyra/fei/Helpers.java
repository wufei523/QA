package info.ephyra.fei;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import info.ephyra.io.Logger;
import info.ephyra.search.Result;

public class Helpers {
	
	public static int[] chooseTopicsToTest(){
		
		int [] return_array = new int[2];
		try{
			System.out.println("\nChoose Topic to test: ??? - ??? (where 701<=???<=740)");
	    	String[] inputTextTokens = readLine().trim().split("-");
	    	return_array[0] = Integer.valueOf(inputTextTokens[0].trim());
	    	return_array[1] = Integer.valueOf(inputTextTokens[1].trim());
	    	if ( !(isValidTopicNumber(return_array[0]) && isValidTopicNumber(return_array[1])) ){
	    		System.out.println("Topic from 701 to 740. Quitting...");
	    		System.exit(0);
	    	}
		}
		catch (NumberFormatException e) {
			System.out.println("input error, put number. Quitting...");
			System.exit(0);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("input error, start and end topic. Quitting...");
			System.exit(0);
		}
		return return_array;
    	
//    	
//    	ArrayList<String> valid_topic_ids = new ArrayList<String>();
//    	for(int i=701; i<=745; i++){
//    		valid_topic_ids.add(String.valueOf(i));
//    		//System.out.println(i);
//    	}
//    	if (valid_topic_ids.contains(read_topic_id)){
//    		fei_topic = Integer.valueOf(read_topic_id);
//    	}
//    	else
//    	{
//    		System.out.println("error");
//    		System.exit(0);
//    	}
	}
	
	
	
	public static ArrayList<File> getSummarizationsForThisTopic(int thisTopic) {
		
		File folder = new File(ControllerVariables.pathToTopicDocuments);
		File[] listOfAllSummerizations = folder.listFiles();
		//ArrayList<File> listOfSummarizationsForThisTopic = new ArrayList<File>();
		ArrayList<File> listOfSummarizationsForThisTopicFromSelectedSystems = new ArrayList<File>();
		List selectedSystems = Arrays.asList(ControllerVariables.selectedSystems);
		for (File file : listOfAllSummerizations){
			String tempSystemID = file.getName().split("\\.")[4];
			if (file.isFile() && file.getName().contains(String.valueOf(thisTopic)) && selectedSystems.contains(tempSystemID)) {
				listOfSummarizationsForThisTopicFromSelectedSystems.add(file);
			}
		}
		Collections.sort(listOfSummarizationsForThisTopicFromSelectedSystems);
		System.out.println("Now processing this topic "+ thisTopic + ", with " + listOfSummarizationsForThisTopicFromSelectedSystems.size() + " system summarizations");
		return listOfSummarizationsForThisTopicFromSelectedSystems;
    	
	}
	
	
	public static ArrayList<String> getQuestionForTopic(int topicNumber){
		
		ArrayList<String> listOfQuestionForThisTopic = new ArrayList<String>();
		String pathToQuestionFile = ControllerVariables.pathToQuestionFileFolder + topicNumber + ".Questions.txt";
             
        BufferedReader br = null;
        int totalQuestions = 0;
        try{
        	br = new BufferedReader(new FileReader(pathToQuestionFile));
        	
            //StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
            	totalQuestions ++;
            	listOfQuestionForThisTopic.add(line);
                //sb.append(line);
                //sb.append(System.lineSeparator());
                line = br.readLine();
            }
            //String everything = sb.toString();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                		pathToQuestionFile + "'");
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + pathToQuestionFile + "'");                  
        }
        System.out.println(totalQuestions + " questions for topic " + topicNumber);
        System.out.println("Question reading Done!");
        System.out.println();
        try{
        	TimeUnit.SECONDS.sleep(2);
        }
        catch(InterruptedException e){
        	
        }
        return listOfQuestionForThisTopic;
	}
	
	public static void answerQuestionsAndRecord(File summary, ArrayList<String> questions, int topicNumber){
//		int num_answered_questions =0;
//        int num_questions_processed = 0;
//		String currentSystem = summary.getName().split("\\.")[4];
//		System.out.println("Now processing this System: " + topicNumber + "." + currentSystem);
//		
//		
//		
//        //added by fei, for each question:
//        for (String question : questions){
//        	num_questions_processed ++;
//        	//current_question = question;
//        	 Result[] results = results = askFactoid(question, FACTOID_MAX_ANSWERS, FACTOID_ABS_THRESH);
//        }
        	
        	
        	
//            if (question.equalsIgnoreCase("exit"))
//                System.exit(0);
//
//            // determine question type and extract question string
//            String type;
//            if (question.matches("(?i)" + FACTOID + ":.*+"))
//            {
//                // factoid question
//                type = FACTOID;
//                question = question.split(":", 2)[1].trim();
//            }
//            else if (question.matches("(?i)" + LIST + ":.*+"))
//            {
//                // list question
//                type = LIST;
//                question = question.split(":", 2)[1].trim();
//            }
//            else
//            {
//                // question type unspecified
//                type = FACTOID; // default type
//            }
//
//            // ask question
//            Result[] results = new Result[0];
//            if (type.equals(FACTOID))
//            {
//                Logger.logFactoidStart(question);
//                results = askFactoid(question, FACTOID_MAX_ANSWERS,
//                    FACTOID_ABS_THRESH);
//                Logger.logResults(results);
//                Logger.logFactoidEnd();
//            }
//            else if (type.equals(LIST))
//            {
//                Logger.logListStart(question);
//                results = askList(question, LIST_REL_THRESH);
//                Logger.logResults(results);
//                Logger.logListEnd();
//            }
	}
	
	private static String readLine()
    {
        try
        {
            return new java.io.BufferedReader(new java.io.InputStreamReader(
                System.in)).readLine();
        }
        catch (java.io.IOException e)
        {
            return new String("");
        }
    }
	
	public static void printArray(int[] a){
		for (int i = 0; i < a.length; i ++){
			System.out.println(a[i]);
		}
	}
	
	public static boolean isValidTopicNumber(int i){
		return (i>=701 && i<=740);
	}
	
	public static <E> void printArrayList(ArrayList<E> al){
		for (E e : al){
			System.out.println(e.toString());
		}
	}
}
