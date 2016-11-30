package info.ephyra.fei;

import java.io.File;
import java.util.ArrayList;

import org.junit.Test;
public class HelpersTest {

	@Test
	public void testChooseTopicsToTest() {
		int [] a = Helpers.chooseTopicsToTest();
		Helpers.printArray(a);
	}
	
	@Test
	public void testGetSummarizationsForThisTopic() {
		ArrayList<File> files = Helpers.getSummarizationsForThisTopic(703);
		Helpers.printArrayList(files);
	}
	
	@Test
	public void testGetQuestionForTopic() {
		ArrayList<String> questionsList = Helpers.getQuestionForTopic(703);
	}
	

}
