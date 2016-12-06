package info.ephyra;

import info.ephyra.answerselection.AnswerSelection;
import info.ephyra.answerselection.filters.AnswerPatternFilter;
import info.ephyra.answerselection.filters.AnswerTypeFilter;
import info.ephyra.answerselection.filters.DuplicateFilter;
import info.ephyra.answerselection.filters.FactoidSubsetFilter;
import info.ephyra.answerselection.filters.FactoidsFromPredicatesFilter;
import info.ephyra.answerselection.filters.PredicateExtractionFilter;
import info.ephyra.answerselection.filters.QuestionKeywordsFilter;
import info.ephyra.answerselection.filters.ScoreCombinationFilter;
import info.ephyra.answerselection.filters.ScoreNormalizationFilter;
import info.ephyra.answerselection.filters.ScoreSorterFilter;
import info.ephyra.answerselection.filters.StopwordFilter;
import info.ephyra.answerselection.filters.TruncationFilter;
import info.ephyra.answerselection.filters.WebDocumentFetcherFilter;
import info.ephyra.io.Logger;
import info.ephyra.io.MsgPrinter;
import info.ephyra.nlp.LingPipe;
import info.ephyra.nlp.NETagger;
import info.ephyra.nlp.OpenNLP;
import info.ephyra.nlp.SnowballStemmer;
import info.ephyra.nlp.StanfordNeTagger;
import info.ephyra.nlp.StanfordParser;
import info.ephyra.nlp.indices.FunctionWords;
import info.ephyra.nlp.indices.IrregularVerbs;
import info.ephyra.nlp.indices.Prepositions;
import info.ephyra.nlp.indices.WordFrequencies;
import info.ephyra.nlp.semantics.ontologies.Ontology;
import info.ephyra.nlp.semantics.ontologies.WordNet;
import info.ephyra.querygeneration.Query;
import info.ephyra.querygeneration.QueryGeneration;
import info.ephyra.querygeneration.generators.BagOfTermsG;
import info.ephyra.querygeneration.generators.BagOfWordsG;
import info.ephyra.querygeneration.generators.PredicateG;
import info.ephyra.querygeneration.generators.QuestionInterpretationG;
import info.ephyra.querygeneration.generators.QuestionReformulationG;
import info.ephyra.questionanalysis.AnalyzedQuestion;
import info.ephyra.questionanalysis.QuestionAnalysis;
import info.ephyra.questionanalysis.QuestionInterpreter;
import info.ephyra.questionanalysis.QuestionNormalizer;
import info.ephyra.search.Result;
import info.ephyra.search.Search;
import info.ephyra.search.searchers.BingAzureKM;


import java.io.*;
//import au.com.bytecode.opencsv.CSVReader.CsvWriter;
import com.opencsv.CSVWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import info.ephyra.fei.*;

/**
 * <code>OpenEphyra</code> is an open framework for question answering (QA).
 * 
 * @author Nico Schlaefer
 * @version 2008-03-23
 */
public class OpenEphyra {

	// 701-741 topics
//	public static int fei_topic = -1;
//	public static String fei_systemID = null;
//	public static String current_question = "";
//	public static String[] systemWillBeTested = { "16", "25", "10", "26", "9", "30", "17", "22", "14", "4" };

	/** Factoid question type. */
	protected static final String FACTOID = "FACTOID";
	/** List question type. */
	protected static final String LIST = "LIST";

	/** Maximum number of factoid answers. */
	protected static final int FACTOID_MAX_ANSWERS = 1;
	/** Absolute threshold for factoid answer scores. */
	protected static final float FACTOID_ABS_THRESH = 0;
	/** Relative threshold for list answer scores (fraction of top score). */
	protected static final float LIST_REL_THRESH = 0.1f;

	/** Serialized classifier for score normalization. */
	public static final String NORMALIZER = "res/scorenormalization/classifiers/" + "AdaBoost70_" + "Score+Extractors_"
			+ "TREC10+TREC11+TREC12+TREC13+TREC14+TREC15+TREC8+TREC9" + ".serialized";

	/** The directory of Ephyra, required when Ephyra is used as an API. */
	protected String dir;

	/**
	 * Entry point of Ephyra. Initializes the engine and starts the command line
	 * interface.
	 * 
	 * @param args
	 *            command line arguments are ignored
	 */
	public static void main(String[] args) {
		// initialize Ephyra and start command line interface
		(new OpenEphyra()).commandLine();
	}

	/**
	 * <p>
	 * Creates a new instance of Ephyra and initializes the system.
	 * </p>
	 * 
	 * <p>
	 * For use as a standalone system.
	 * </p>
	 */
	protected OpenEphyra() {
		this("");
	}

	/**
	 * <p>
	 * Creates a new instance of Ephyra and initializes the system.
	 * </p>
	 * 
	 * <p>
	 * For use as an API.
	 * </p>
	 * 
	 * @param dir
	 *            directory of Ephyra
	 */
	public OpenEphyra(String dir) {
		this.dir = dir;

		// Moved this initialization from main() so that this class can
		// be instantiated and used from another application:

		// set log file and enable logging
		Logger.setLogfile("log/OpenEphyra");
		Logger.enableLogging(ControllerVariables.enableLoggingOrNot);

		// enable output of status and error messages
		MsgPrinter.enableStatusMsgs(ControllerVariables.printStatusOrNot);
		MsgPrinter.enableErrorMsgs(ControllerVariables.printErrorsOrNot);
		MsgPrinter.printInitializing();

		// create tokenizer
		MsgPrinter.printStatusMsg("Creating tokenizer...");
		if (!OpenNLP.createTokenizer(dir + "res/nlp/tokenizer/opennlp/EnglishTok.bin.gz"))
			MsgPrinter.printErrorMsg("Could not create tokenizer.");
		// LingPipe.createTokenizer();

		// create sentence detector
		MsgPrinter.printStatusMsg("Creating sentence detector...");
		if (!OpenNLP.createSentenceDetector(dir + "res/nlp/sentencedetector/opennlp/EnglishSD.bin.gz"))
			MsgPrinter.printErrorMsg("Could not create sentence detector.");
		LingPipe.createSentenceDetector();

		// create stemmer
		MsgPrinter.printStatusMsg("Creating stemmer...");
		SnowballStemmer.create();

		// create part of speech tagger
		MsgPrinter.printStatusMsg("Creating POS tagger...");
		if (!OpenNLP.createPosTagger(dir + "res/nlp/postagger/opennlp/tag.bin.gz",
				dir + "res/nlp/postagger/opennlp/tagdict"))
			MsgPrinter.printErrorMsg("Could not create OpenNLP POS tagger.");
		// if (!StanfordPosTagger.init(dir + "res/nlp/postagger/stanford/" +
		// "wsj3t0-18-bidirectional/train-wsj-0-18.holder"))
		// MsgPrinter.printErrorMsg("Could not create Stanford POS tagger.");

		// create chunker
		MsgPrinter.printStatusMsg("Creating chunker...");
		if (!OpenNLP.createChunker(dir + "res/nlp/phrasechunker/opennlp/EnglishChunk.bin.gz"))
			MsgPrinter.printErrorMsg("Could not create chunker.");

		// create syntactic parser
		MsgPrinter.printStatusMsg("Creating syntactic parser...");
		// if (!OpenNLP.createParser(dir + "res/nlp/syntacticparser/opennlp/"))
		// MsgPrinter.printErrorMsg("Could not create OpenNLP parser.");
		try {
			StanfordParser.initialize();
		} catch (Exception e) {
			MsgPrinter.printErrorMsg("Could not create Stanford parser.");
		}

		// create named entity taggers
		MsgPrinter.printStatusMsg("Creating NE taggers...");
		NETagger.loadListTaggers(dir + "res/nlp/netagger/lists/");
		NETagger.loadRegExTaggers(dir + "res/nlp/netagger/patterns.lst");
		MsgPrinter.printStatusMsg("  ...loading models");
		// if (!NETagger.loadNameFinders(dir + "res/nlp/netagger/opennlp/"))
		// MsgPrinter.printErrorMsg("Could not create OpenNLP NE tagger.");
		if (!StanfordNeTagger.isInitialized() && !StanfordNeTagger.init())
			MsgPrinter.printErrorMsg("Could not create Stanford NE tagger.");
		MsgPrinter.printStatusMsg("  ...done");

		// create linker
		// MsgPrinter.printStatusMsg("Creating linker...");
		// if (!OpenNLP.createLinker(dir + "res/nlp/corefresolver/opennlp/"))
		// MsgPrinter.printErrorMsg("Could not create linker.");

		// create WordNet dictionary
		MsgPrinter.printStatusMsg("Creating WordNet dictionary...");
		if (!WordNet.initialize(dir + "res/ontologies/wordnet/file_properties.xml"))
			MsgPrinter.printErrorMsg("Could not create WordNet dictionary.");

		// load function words (numbers are excluded)
		MsgPrinter.printStatusMsg("Loading function verbs...");
		if (!FunctionWords.loadIndex(dir + "res/indices/functionwords_nonumbers"))
			MsgPrinter.printErrorMsg("Could not load function words.");

		// load prepositions
		MsgPrinter.printStatusMsg("Loading prepositions...");
		if (!Prepositions.loadIndex(dir + "res/indices/prepositions"))
			MsgPrinter.printErrorMsg("Could not load prepositions.");

		// load irregular verbs
		MsgPrinter.printStatusMsg("Loading irregular verbs...");
		if (!IrregularVerbs.loadVerbs(dir + "res/indices/irregularverbs"))
			MsgPrinter.printErrorMsg("Could not load irregular verbs.");

		// load word frequencies
		MsgPrinter.printStatusMsg("Loading word frequencies...");
		if (!WordFrequencies.loadIndex(dir + "res/indices/wordfrequencies"))
			MsgPrinter.printErrorMsg("Could not load word frequencies.");

		// load query reformulators
		MsgPrinter.printStatusMsg("Loading query reformulators...");
		if (!QuestionReformulationG.loadReformulators(dir + "res/reformulations/"))
			MsgPrinter.printErrorMsg("Could not load query reformulators.");

		// load answer types
		// MsgPrinter.printStatusMsg("Loading answer types...");
		// if (!AnswerTypeTester.loadAnswerTypes(dir +
		// "res/answertypes/patterns/answertypepatterns"))
		// MsgPrinter.printErrorMsg("Could not load answer types.");

		// load question patterns
		MsgPrinter.printStatusMsg("Loading question patterns...");
		if (!QuestionInterpreter.loadPatterns(dir + "res/patternlearning/questionpatterns/"))
			MsgPrinter.printErrorMsg("Could not load question patterns.");

		// load answer patterns
		MsgPrinter.printStatusMsg("Loading answer patterns...");
		if (!AnswerPatternFilter.loadPatterns(dir + "res/patternlearning/answerpatterns/"))
			MsgPrinter.printErrorMsg("Could not load answer patterns.");
	}

	/**
	 * Reads a line from the command prompt.
	 * 
	 * @return user input
	 */
	protected String readLine() {
		try {
			return new java.io.BufferedReader(new java.io.InputStreamReader(System.in)).readLine();
		} catch (java.io.IOException e) {
			return new String("");
		}
	}

	/**
	 * Initializes the pipeline for factoid questions.
	 */
	protected void initFactoid() {
		// question analysis
		Ontology wordNet = new WordNet();
		// - dictionaries for term extraction
		QuestionAnalysis.clearDictionaries();
		QuestionAnalysis.addDictionary(wordNet);
		// - ontologies for term expansion
		QuestionAnalysis.clearOntologies();
		QuestionAnalysis.addOntology(wordNet);

		// query generation
		QueryGeneration.clearQueryGenerators();
		QueryGeneration.addQueryGenerator(new BagOfWordsG());
		QueryGeneration.addQueryGenerator(new BagOfTermsG());
		QueryGeneration.addQueryGenerator(new PredicateG());
		QueryGeneration.addQueryGenerator(new QuestionInterpretationG());
		QueryGeneration.addQueryGenerator(new QuestionReformulationG());

		// search
		// - knowledge miners for unstructured knowledge sources
		Search.clearKnowledgeMiners();
		Search.addKnowledgeMiner(new BingAzureKM());

		// for (String[] indriIndices : IndriKM.getIndriIndices())
		// Search.addKnowledgeMiner(new IndriKM(indriIndices, false));
		// for (String[] indriServers : IndriKM.getIndriServers())
		// Search.addKnowledgeMiner(new IndriKM(indriServers, true));

		// - knowledge annotators for (semi-)structured knowledge sources
		Search.clearKnowledgeAnnotators();

		// answer extraction and selection
		// (the filters are applied in this order)
		AnswerSelection.clearFilters();
		// - answer extraction filters
		AnswerSelection.addFilter(new AnswerTypeFilter());
		AnswerSelection.addFilter(new AnswerPatternFilter());
		AnswerSelection.addFilter(new WebDocumentFetcherFilter());
		AnswerSelection.addFilter(new PredicateExtractionFilter());
		AnswerSelection.addFilter(new FactoidsFromPredicatesFilter());
		AnswerSelection.addFilter(new TruncationFilter());
		// - answer selection filters
		AnswerSelection.addFilter(new StopwordFilter());
		// AnswerSelection.addFilter(new QuestionKeywordsFilter());
		AnswerSelection.addFilter(new ScoreNormalizationFilter(NORMALIZER));
		AnswerSelection.addFilter(new ScoreCombinationFilter());
		AnswerSelection.addFilter(new FactoidSubsetFilter());
		AnswerSelection.addFilter(new DuplicateFilter());
		AnswerSelection.addFilter(new ScoreSorterFilter());
	}

	/**
	 * Runs the pipeline and returns an array of up to <code>maxAnswers</code>
	 * results that have a score of at least <code>absThresh</code>.
	 * 
	 * @param aq
	 *            analyzed question
	 * @param maxAnswers
	 *            maximum number of answers
	 * @param absThresh
	 *            absolute threshold for scores
	 * @return array of results
	 */
	protected Result[] runPipeline(AnalyzedQuestion aq, int maxAnswers, float absThresh) {
		// query generation
		MsgPrinter.printGeneratingQueries();
		Query[] queries = QueryGeneration.getQueries(aq);

		// search
		MsgPrinter.printSearching();
		Result[] results = new Result[0];
		results = Search.doSearch(queries);

		// answer selection
		MsgPrinter.printSelectingAnswers();
		results = AnswerSelection.getResults(results, maxAnswers, absThresh);

		return results;
	}

	/**
	 * Returns the directory of Ephyra.
	 * 
	 * @return directory
	 */
	public String getDir() {
		return dir;
	}

	/**
	 * <p>
	 * A command line interface for Ephyra.
	 * </p>
	 * 
	 * <p>
	 * Repeatedly queries the user for a question, asks the system the question
	 * and prints out and logs the results.
	 * </p>
	 * 
	 * <p>
	 * The command <code>exit</code> can be used to quit the program.
	 * </p>
	 */
	public void commandLine() {

		// 701-741
		// System.out.print("\nWhich Topic(701-745): ");
		// String read_topic_id = readLine().trim();
		// ArrayList<String> valid_topic_ids = new ArrayList<String>();
		// for(int i=701; i<=741; i++){
		// valid_topic_ids.add(String.valueOf(i));
		// //System.out.println(i);
		// }
		// if (valid_topic_ids.contains(read_topic_id)){
		// fei_topic = Integer.valueOf(read_topic_id);
		// }
		// else
		// {
		// System.out.println("error");
		// System.exit(0);
		// }

		int[] topicsChoosenToBeTested = Helpers.chooseTopicsToTest();
		for (int topicNumber = topicsChoosenToBeTested[0]; topicNumber <= topicsChoosenToBeTested[1]; topicNumber++) {
			ArrayList<File> files = Helpers.getSummarizationsForThisTopic(topicNumber);
			// Helpers.printArrayList(files);
			ArrayList<String> questionsForThisTopic = Helpers.getQuestionForTopic(topicNumber);
			ControllerVariables.currentTopicId = topicNumber;
			for (File file : files) {
				int num_answered_questions = 0;
				int num_questions_processed = 0;
				String current_system_id = file.getName().split("\\.")[4];
				ControllerVariables.currentSystemId = current_system_id;
				System.out.println("Now processing this System: " + topicNumber + "." + current_system_id);

				
				for (String question : questionsForThisTopic) {
					ControllerVariables.currentQuestion = question;
					num_questions_processed++;
					Result[] results = askFactoid(question, FACTOID_MAX_ANSWERS, FACTOID_ABS_THRESH);
					
					
					String outputFile = ControllerVariables.pathToIndividualResultFolder + "answers." + topicNumber + "." + current_system_id + ".csv";
					boolean alreadyExists = new File(outputFile).exists();

					try {
						CSVWriter csvOutput = new CSVWriter(new FileWriter(outputFile, true), ',');
						if (!alreadyExists) {
							//header
							String[] entries = ("question#answer".split("#"));
							csvOutput.writeNext(entries);
							csvOutput.close();

						}
						String resultsToWrite = "null";
						if (results.length > 0) {
							resultsToWrite = results[0].getAnswer();
							num_answered_questions++;

						}
						System.out.println("Current Topic: " + topicNumber);
						System.out.println("Current System: " + current_system_id);
						System.out.println("Question processed: " + num_questions_processed);
						System.out.println("Answered Questions: " + num_answered_questions);

						String[] entries = { question, resultsToWrite };
						csvOutput.writeNext(entries);
						csvOutput.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}//end of for question : questions for this topic
			}
		}
		System.exit(0);

		// File folder = new File("/home/fw/Dropbox/16Summer/Ready to run
		// Systems and results/FirstEvaluationTry/Questions");
		// read all summarization of a topic
		// File folder = new
		// File("/home/fw/Dropbox/16Summer/Papers/mainEvalALL/manual/peers");

		// This is mac path
//		File folder = new File(ControllerVariables.pathToTopicDocuments);
//
//		File[] listOfSummarizationForAllTopics = folder.listFiles();
//		ArrayList<File> thisTopicSummarizationFiles1 = new ArrayList<File>();
//		ArrayList<File> thisTopicSummarizationFiles = new ArrayList<File>();
//
//		for (int i = 0; i < listOfSummarizationForAllTopics.length; i++) {
//			File file = listOfSummarizationForAllTopics[i];
//			if (file.isFile() && file.getName().contains(String.valueOf(fei_topic))) {
//				thisTopicSummarizationFiles1.add(file);
//				// System.out.println(file.getName());
//			}
//		}
//		Collections.sort(thisTopicSummarizationFiles1);
//
//		for (File f : thisTopicSummarizationFiles1) {
//			String tempSystemID = f.getName().split("\\.")[4];
//			List myList = Arrays.asList(systemWillBeTested);
//			if (myList.contains(tempSystemID)) {
//				thisTopicSummarizationFiles.add(f);
//				System.out.println(f.getName());
//			}
//
//		}
//
//		System.out.println("Now processing this topic " + fei_topic + ", with " + thisTopicSummarizationFiles.size()
//				+ " system summarizations");
//		// System.exit(0);
//		ArrayList<String> feiQuestions = new ArrayList<String>();
//		// open question file for this topic
//		// String ss1 = "/Users/feiwu/Dropbox/16Summer/Ready to run Systems and
//		// results/FirstEvaluationTry/QuestionGenerationFirstTry/D0701.Questions.txt";
//		// String ss1 = "/home/fw/Dropbox/16Summer/Ready to run Systems and
//		// results/ThirdEvaluationTry/QuestionGeneratedFromOriginal/" +
//		// fei_topic + ".Questions.txt";
//		// String ss1 = "/home/fw/Downloads/test/D0" + fei_topic +
//		// ".Questions.txt";
//		String ss1 = "/Users/feiwu/Dropbox/16Summer/Ready to run Systems and results/ThirdEvaluationTry/QuestionGeneratedFromOriginal/"
//				+ fei_topic + ".Questions.txt";
//
//		BufferedReader br = null;
//		int totalQuestions = 0;
//		try {
//			br = new BufferedReader(new FileReader(ss1));
//
//			// StringBuilder sb = new StringBuilder();
//			String line = br.readLine();
//
//			while (line != null) {
//				totalQuestions++;
//				feiQuestions.add(line);
//				// sb.append(line);
//				// sb.append(System.lineSeparator());
//				line = br.readLine();
//			}
//			// String everything = sb.toString();
//		} catch (FileNotFoundException ex) {
//			System.out.println("Unable to open file '" + ss1 + "'");
//		} catch (IOException ex) {
//			System.out.println("Error reading file '" + ss1 + "'");
//			// Or we could just do this:
//			// ex.printStackTrace();
//		}
//		System.out.println(totalQuestions + " questions for topic " + fei_topic);
//		System.out.println("Question reading Done!");
//		System.out.println();
//		try {
//			TimeUnit.SECONDS.sleep(2);
//		} catch (InterruptedException e) {
//
//		}

		// while (true)
		// for each summarization of this topic(701,702 ... 741)
//		int count_not_null_questions = 0;
//		int num_questions_processed = 0;
//		for (File file : thisTopicSummarizationFiles) {
//			count_not_null_questions = 0;
//			num_questions_processed = 0;
//			fei_systemID = file.getName().split("\\.")[4];
//			System.out.println("Now processing this System: " + fei_topic + "." + fei_systemID);

			// query user for question, quit if user types in "exit"
			// MsgPrinter.printQuestionPrompt();
			// String question = readLine().trim();

			// added by fei, for each question:
//			for (String question : feiQuestions) {
//				num_questions_processed++;
//				current_question = question;
//
//				if (question.equalsIgnoreCase("exit"))
//					System.exit(0);
//
//				// determine question type and extract question string
//				String type;
//				if (question.matches("(?i)" + FACTOID + ":.*+")) {
//					// factoid question
//					type = FACTOID;
//					question = question.split(":", 2)[1].trim();
//				} else if (question.matches("(?i)" + LIST + ":.*+")) {
//					// list question
//					type = LIST;
//					question = question.split(":", 2)[1].trim();
//				} else {
//					// question type unspecified
//					type = FACTOID; // default type
//				}
//
//				// ask question
//				Result[] results = new Result[0];
//				if (type.equals(FACTOID)) {
//					Logger.logFactoidStart(question);
//					results = askFactoid(question, FACTOID_MAX_ANSWERS, FACTOID_ABS_THRESH);
//					Logger.logResults(results);
//					Logger.logFactoidEnd();
//				} else if (type.equals(LIST)) {
//					Logger.logListStart(question);
//					results = askList(question, LIST_REL_THRESH);
//					Logger.logResults(results);
//					Logger.logListEnd();
//				}
//
//				// print answers
//				// MsgPrinter.printAnswers(results);
//
//				// got one result, write it to file
//				// added by Fei, write answers to csv file
//				String outputFile = "individualResults/answers." + fei_topic + "." + fei_systemID + ".csv";
//
//				// before we open the file check to see if it already exists
//				boolean alreadyExists = new File(outputFile).exists();
//
//				try {
//					// use FileWriter constructor that specifies open for
//					// appending
//					// CSVWriter csvOutput = new CSVWriter(new
//					// FileWriter(outputFile, true), ',');
//					CSVWriter csvOutput = new CSVWriter(new FileWriter(outputFile, true), ',');
//					// if the file didn't already exist then we need to write
//					// out the header line
//					if (!alreadyExists) {
//						// feed in your array (or convert your data to an array)
//						String[] entries = ("question#answer".split("#"));
//						csvOutput.writeNext(entries);
//						csvOutput.close();
//
//					}
//					// else assume that the file already has the correct header
//					// line
//
//					// write out a few records
//					String resultsToWrite = "null";
//					if (results.length > 0) {
//						resultsToWrite = results[0].getAnswer();
//						count_not_null_questions++;
//
//					}
//					System.out.println("Current Topic: " + fei_topic);
//					System.out.println("Current System: " + fei_systemID);
//					System.out.println("Question processed: " + num_questions_processed);
//					System.out.println("NOT NULL ANSWERS: " + count_not_null_questions);
//
//					String[] entries = { question, resultsToWrite };
//					csvOutput.writeNext(entries);
//					csvOutput.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//
//			} // finish all questions in a question file
//				// end of (for question : feiQuestions)
//
//			// write summary result to file one system by one system
//			String outputFile2 = "topic." + fei_topic + ".csv";
//			boolean alreadyExists2 = new File(outputFile2).exists();
//			try {
//				// use FileWriter constructor that specifies open for appending
//				// CSVWriter csvOutput = new CSVWriter(new
//				// FileWriter(outputFile, true), ',');
//				CSVWriter csvOutput2 = new CSVWriter(new FileWriter(outputFile2, true), ',');
//				// if the file didn't already exist then we need to write out
//				// the header line
//				if (!alreadyExists2) {
//					// feed in your array (or convert your data to an array)
//					String[] entries2 = ("SystemID#answered#total#percentage".split("#"));
//					csvOutput2.writeNext(entries2);
//					// csvOutput2.close();
//
//				}
//				// else assume that the file already has the correct header line
//				double answered_percentage = (double) count_not_null_questions / totalQuestions;
//				// DecimalFormat df = new DecimalFormat("#.000000");
//
//				String[] entries2 = { fei_systemID, String.valueOf(count_not_null_questions),
//						String.valueOf(totalQuestions), Double.toString(answered_percentage) };
//
//				System.out.println("process for system " + fei_systemID + " done!");
//				System.out.println(count_not_null_questions + "/" + num_questions_processed + " questions answered");
//				System.out.println();
//				csvOutput2.writeNext(entries2);
//				csvOutput2.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//		} // end of (for file : thisTopicSummarizationFiles)

		// after all files for this topic, exit
	}

	/**
	 * Asks Ephyra a factoid question and returns up to <code>maxAnswers</code>
	 * results that have a score of at least <code>absThresh</code>.
	 * 
	 * @param question
	 *            factoid question
	 * @param maxAnswers
	 *            maximum number of answers
	 * @param absThresh
	 *            absolute threshold for scores
	 * @return array of results
	 */
	public Result[] askFactoid(String question, int maxAnswers, float absThresh) {
		// initialize pipeline
		initFactoid();

		// analyze question
		MsgPrinter.printAnalyzingQuestion();
		AnalyzedQuestion aq = QuestionAnalysis.analyze(question);

		// get answers
		Result[] results = runPipeline(aq, maxAnswers, absThresh);

		return results;
	}

	/**
	 * Asks Ephyra a factoid question and returns a single result or
	 * <code>null</code> if no answer could be found.
	 * 
	 * @param question
	 *            factoid question
	 * @return single result or <code>null</code>
	 */
	public Result askFactoid(String question) {
		Result[] results = askFactoid(question, 1, 0);

		return (results.length > 0) ? results[0] : null;
	}

	/**
	 * Asks Ephyra a list question and returns results that have a score of at
	 * least <code>relThresh * top score</code>.
	 * 
	 * @param question
	 *            list question
	 * @param relThresh
	 *            relative threshold for scores
	 * @return array of results
	 */
	public Result[] askList(String question, float relThresh) {
		question = QuestionNormalizer.transformList(question);

		Result[] results = askFactoid(question, Integer.MAX_VALUE, 0);

		// get results with a score of at least relThresh * top score
		ArrayList<Result> confident = new ArrayList<Result>();
		if (results.length > 0) {
			float topScore = results[0].getScore();

			for (Result result : results)
				if (result.getScore() >= relThresh * topScore)
					confident.add(result);
		}

		return confident.toArray(new Result[confident.size()]);
	}

	public void promptEnterKey() {
		System.out.println("Press \"ENTER\" to continue...");
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
	}

}
