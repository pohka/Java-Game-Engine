package loaders;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import framework.Game;

public class CsvLoader {
	
	public static ArrayList<ArrayList<String>> load(String filename, String delimiter){
		//incorrect file extension
		if(Format.getFormat(filename).equalsIgnoreCase(".csv")==false){
			Game.print().log("Extension is not .csv for file: " + filename);
			return new ArrayList<ArrayList<String>>();
		}
		File f = new File(filename);
		if(f.exists()==false){
			Game.print().log("File was not found: "+ filename);
		}
		
		ArrayList<ArrayList<String>> items = new ArrayList<ArrayList<String>>();
		Scanner scanner = null;
		try {
			scanner = new Scanner(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        
        while(scanner.hasNext()){
           String line = scanner.nextLine();
           String elements [] = line.split(delimiter);
           //change to ArrayList and add to all items
           items.add(new ArrayList<String>(Arrays.asList(elements)));
        }
        scanner.close();
        
        return items;
	}
}
