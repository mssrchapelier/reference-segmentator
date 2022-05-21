import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Segmentator {
	ArrayList<RegexInstruction> regexInstructions;
	ArrayList<Map<String,String>> entries;
	
	public Segmentator (ArrayList<RegexInstruction> regexInstructions, ArrayList<String> lines) {
		this.regexInstructions = regexInstructions;
		this.entries = new ArrayList<Map<String,String>>();
		
		for (String s : lines) {
			Map<String,String> entry = convertLineToEntry(s);
			if (!entry.isEmpty()) {
				this.entries.add(entry);
			}
		}
	}
	
	public Map<String,String> convertLineToEntryAuld (String line) {
		Map<String,String> entry = new HashMap<String,String>();
		for (RegexInstruction ri : this.regexInstructions) {
			Pattern p = Pattern.compile(ri.regex);
			Matcher m = p.matcher(line);
			if (m.find()) {
				for (Map.Entry<String, Integer> fieldNumberPair : ri.fields.entrySet()) {
					if (fieldNumberPair.getValue() <= m.groupCount()) {
						entry.put(fieldNumberPair.getKey(), m.group(fieldNumberPair.getValue())); // get captured groups from regex
					}
				}
				break;
			}
		}
		
		return entry;
	}
	
	public Map<String,String> convertLineToEntry (String line) {
		ArrayList<Map<String,String>> possibleEntries = new ArrayList<Map<String,String>>();
		Map<String, String> returnedEntry = new HashMap<String, String>();
		for (RegexInstruction ri : this.regexInstructions) {
			Pattern p = Pattern.compile(ri.regex);
			Matcher m = p.matcher(line);
			if (m.find()) {
				Map<String, String> entry = new HashMap<String, String>();
				for (Map.Entry<String, Integer> fieldNumberPair : ri.fields.entrySet()) {
					if (fieldNumberPair.getValue() <= m.groupCount()) {
						entry.put(fieldNumberPair.getKey(), m.group(fieldNumberPair.getValue())); // get captured groups from regex
					}
				}
				possibleEntries.add(entry);
			}
		}
		
		// find an entry which has the most non-empty fields to return it
		int maxFields = 0;
		
		for (Map<String, String> entry : possibleEntries) {
			int nonEmptyFields = 0;
			for (Map.Entry<String, String> field : entry.entrySet()) {
				if (!field.getValue().isEmpty()) { nonEmptyFields++; }
			}
			if (nonEmptyFields > maxFields) {
				maxFields = nonEmptyFields;
				returnedEntry = entry;
			}
		}
		
		return returnedEntry;
	}
}
