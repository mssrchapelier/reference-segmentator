import java.util.Map;

public class RegexInstruction {
	public String entryDescriptor; // e. g. "book-multiple-authors"
	public String regex;
	public Map<String,Integer> fields; // field-name: group-number (group matcher)
	
	public RegexInstruction (String entryDescriptor, String regex, Map<String,Integer> fields) {
		this.entryDescriptor = entryDescriptor;
		this.regex = regex;
		this.fields = fields;
	}
}
