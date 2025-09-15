import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class Driver {
	public int location = 0;
	//1. Open a GUI(allows users to select a file, return a File object)
	File file = new File("file_selected"); 
	
	
	
	
	//2. First pass
	 Map<String, String> map = new HashMap<>();
	 public void firstPass(File file) {
	/**
	 * (1) Create a HashMap object
	 * (2) Read line by line
	 * (3) update location
	 * (4) update the Map
	 * (5) iterate	
	*/ 
	 }
	 
	 
	//3. Second pass
	 File loadFile = new File("output_file");
	 public void secondPass(File file) {
	/** 
	 * (1) Set location back to 0
	 * (2) create instances
	 * (3) read line by line
	 * (4) If ..., run LoadInstructions.Op01()
	 * (5) write to loadFile
	 */
	 }
	 
	 
	 public static void main(String[] args) {
		 Driver driver = new Driver();
		 driver.firstPass(file);
		 driver.secondPass(file);
		 
	 }
	
	
	
}
