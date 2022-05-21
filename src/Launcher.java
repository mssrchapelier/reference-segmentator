import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Launcher {
	
	@Parameter(names = { "-c", "--config" }, description = "location of configuration file")
	String configFilePath = "res/config.txt";
	
	@Parameter(description = "location of input file")
	String inputFilePath = "input-sample.txt";
	
	@Parameter(names = { "-o", "--output" }, description = "location of output file")
	String outputFilePath = "output.json";
	
	public static void main(String[] args) {
		
		Launcher launcher = new Launcher();
		JCommander.newBuilder()
			.addObject(launcher)
			.build()
			.parse(args);
		
		launcher.run();
		
	}
	
	public void run() {
		
		try (
				BufferedReader configReader = new BufferedReader(new InputStreamReader(new FileInputStream(configFilePath), "UTF-8"));
				BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilePath), "UTF-8"));
				PrintWriter writer = new PrintWriter(outputFilePath, "UTF-8")
		) {
			
			ArrayList<RegexInstruction> regexInstructions = new RegexCollectionBuilder(configReader).regexInstructions;
			
			ArrayList<String> lines = new ArrayList<String>();
			String currentLine;
			while ((currentLine = inputReader.readLine()) != null) {
				lines.add(currentLine);
			}
			
			ArrayList<Map<String, String>> entries = new Segmentator(regexInstructions, lines).entries;
			writeEntries(writer, entries);
			
			System.out.format("Done! Output written to file: %s\n", this.outputFilePath);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeEntries (PrintWriter writer, ArrayList<Map<String, String>> entries) {
		writer.write(new GsonBuilder()
				.setPrettyPrinting()
				.create()
				.toJson(entries));
	}
	
}
