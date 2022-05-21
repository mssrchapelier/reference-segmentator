import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexCollectionBuilder {
	ArrayList<Element> elements;
	ArrayList<String> fields;
	ArrayList<RegexInstruction> regexInstructions;
	
	public RegexCollectionBuilder (BufferedReader reader) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		
		String currentLine = "";
		currentLine = reader.readLine();
		
		// read lines with elements
		while (currentLine != null && !currentLine.equals("FIELDS")) {
			lines.add(currentLine);
			currentLine = reader.readLine();
		}
		
		this.elements = new ArrayList<Element>();
		this.buildElementDefinitions(lines);
		lines.clear();
		
		// read lines with fields
		if (currentLine.equals("FIELDS")) {
			currentLine = reader.readLine();
			while (currentLine != null && !currentLine.equals("ENTRIES")) {
				lines.add(currentLine);
				currentLine = reader.readLine();
			}
		}
		this.fields = new ArrayList<String>();
		this.buildFields(lines);
		lines.clear();
		
		// read lines with entries
		if (currentLine.equals("ENTRIES")) {
			currentLine = reader.readLine();
			while (currentLine != null) {
				lines.add(currentLine);
				currentLine = reader.readLine();
			}
		}
		this.regexInstructions = new ArrayList<RegexInstruction>();
		buildRegexInstructions(lines);
	}
	
	public void buildElementDefinitions (ArrayList<String> lines) {
		for (String currentLine : lines) {
			Element constructedElement = new Element("");
			boolean isNewElement = true;
			ArrayList<Token> tokens;
			String elementName;
			String remainingPart = currentLine;
			
			elementName = readLeftPart(remainingPart);
			
			if (this.elements.isEmpty()) {
				constructedElement = new Element(elementName);
			} else {
				for (Element e : this.elements) {
					if (e.name.equals(elementName)) {
						constructedElement = e;
						isNewElement = false;
						break;
					}
					constructedElement = new Element(elementName);
				}
			}
			
			remainingPart = trimLeftPart(remainingPart);
			
			tokens = this.readTokens(remainingPart);
			
			String constructedRegex = "";
			performTokenActionForElements(constructedRegex, tokens, constructedElement);
			
			if (isNewElement) {
				this.elements.add(constructedElement);
			}
			
			// строка обработана: либо добавлен новый элемент с вариантами, либо варианты к старому элементу
		}
		// определения элементов обработаны: this.elements заполнено
	}
	
	public void buildFields (ArrayList<String> lines) {
		for (String currentLine : lines) {
			for (Element e : this.elements) {
				if (currentLine.equals(e.name)) {
					this.fields.add(currentLine);
					break;
				}
			}
		}
	}
	
	public void buildRegexInstructions (ArrayList<String> lines) {
		
		for (String currentLine : lines) { // for each individual entry
			// reading tokens
			String entryDescriptor = readLeftPart(currentLine);
			currentLine = trimLeftPart(currentLine);
			
			ArrayList<Token> tokens = readTokens(currentLine);
			
			String constructedRegex = "";
			int currentLineGroupCount = 0;
			Map<String,Integer> currentFieldList = new HashMap<String,Integer>();
			performTokenActionForRegexInstructions(constructedRegex, currentLineGroupCount, tokens, entryDescriptor, currentFieldList);
		}
	}
	
	public ArrayList<Token> readTokens (String remainingPart) {
		ArrayList<Token> tokens = new ArrayList<Token>();
		while (!remainingPart.isEmpty()) {
			if (remainingPart.charAt(0) == ' ') {
				remainingPart = remainingPart.substring(1);
			} else {
				String s = "";
				Pattern p;
				Matcher m;
				TokenType t;
				
				if (remainingPart.charAt(0) == '"') { // regex
					t = TokenType.REGEX;
					p = Pattern.compile("\"(.+?)\"");
					m = p.matcher(remainingPart);
					if (m.find()) {
						s = m.group(1); // без кавычек
					}
					
					if (remainingPart.length() > s.length() + 2) {
						remainingPart = remainingPart.substring(s.length() + 2);  // также кавычки
					} else {
						remainingPart = "";
					}
				} else { // lexical
					t = TokenType.LEXICAL;
					p = Pattern.compile("[\\w\\-]+");
					m = p.matcher(remainingPart);
					if (m.find()) {
						s = m.group(0);
					}
					
					if (remainingPart.length() > s.length()) {
						remainingPart = remainingPart.substring(s.length());
					} else {
						remainingPart = "";
					}
					
				}
				tokens.add(new Token(t, s));
			}
		}
		return tokens;
	}
	
	public void performTokenActionForElements (String constructedRegex, ArrayList<Token> remainingTokens, Element constructedElement) {
		
		boolean tokensRegexOnly = true;
		for (Token t : remainingTokens) {
			if (t.type.equals(TokenType.LEXICAL)) { tokensRegexOnly = false; break; }
		}
		
		Token currentToken = remainingTokens.get(0);
		while (!remainingTokens.isEmpty() && currentToken.type == TokenType.REGEX) {
			constructedRegex += currentToken.value;
			remainingTokens.remove(0);
			if (!remainingTokens.isEmpty()) {
				currentToken = remainingTokens.get(0);
			}
		}
		
		if (!remainingTokens.isEmpty())	{ // if lexical
			currentToken = remainingTokens.get(0);	
			for (Element e : this.elements) {
				if (e.name.equals(currentToken.value)) { // find element with the same name in elements
					for (String ev : e.variants) {
						ArrayList<Token> newRemainingTokens = new ArrayList<Token>(remainingTokens);
						String newConstructedRegex = constructedRegex + ev;
						newRemainingTokens.remove(0); // remove copy of t
						if (! newRemainingTokens.isEmpty()) {
							performTokenActionForElements(newConstructedRegex, newRemainingTokens, constructedElement);
						} else {
							constructedElement.variants.add(newConstructedRegex);
						}
					}
				}
			}
		}
		
		if (tokensRegexOnly) {
			constructedElement.variants.add(constructedRegex);
		}
	}
	
	public void performTokenActionForRegexInstructions (String constructedRegex,
			int currentLineGroupCount, ArrayList<Token> remainingTokens,
			String entryDescriptor, Map<String,Integer> currentFieldList) {
		
		boolean tokensRegexOnly = true;
		for (Token t : remainingTokens) {
			if (t.type.equals(TokenType.LEXICAL)) { tokensRegexOnly = false; break; }
		}
		
		Token currentToken = remainingTokens.get(0);
		while (!remainingTokens.isEmpty() && currentToken.type == TokenType.REGEX) {
			currentLineGroupCount += countGroups(currentToken.value);
			constructedRegex += currentToken.value;
			remainingTokens.remove(0);
			if (!remainingTokens.isEmpty()) {
				currentToken = remainingTokens.get(0);
			}
		}
		
		if (!remainingTokens.isEmpty())	{ // if lexical
			currentToken = remainingTokens.get(0);	
			for (Element e : this.elements) {
				if (e.name.equals(currentToken.value)) { // find element with the same name in elements
					boolean isField = false;
					for (String s : this.fields) {
						if (s.equals(currentToken.value)) {
							isField = true;
							break;
						}
					}
					
					if (isField) {
						currentLineGroupCount++;
					}
					
					for (String ev : e.variants) {
						ArrayList<Token> newRemainingTokens = new ArrayList<Token>(remainingTokens);
						String newConstructedRegex = constructedRegex;
						
						if (isField) {
							newConstructedRegex += "(" + ev + ")"; // captured group
							currentFieldList.put(currentToken.value, currentLineGroupCount);
						} else {
							newConstructedRegex += ev;
						}
						
						// currentLineGroupCount += countGroups(ev);
						newRemainingTokens.remove(0); // remove copy of t
						if (! newRemainingTokens.isEmpty()) {
							performTokenActionForRegexInstructions(newConstructedRegex, currentLineGroupCount + countGroups(ev), newRemainingTokens, entryDescriptor, currentFieldList);
						} else {
							RegexInstruction ri = new RegexInstruction(entryDescriptor, newConstructedRegex, currentFieldList);
							this.regexInstructions.add(ri);
						}
					}
				}
			}
		}
		
		if (tokensRegexOnly) {
			RegexInstruction ri = new RegexInstruction(entryDescriptor, constructedRegex, currentFieldList);
			this.regexInstructions.add(ri);
		}
	}
	
	public void printDefinitions () {
		System.out.println("*** DEFINITIONS ***");
		int elementCount = 0;
		int variantCount = 0;
		for (Element e : this.elements) {
			elementCount++;
			System.out.println("ELEMENT: " + e.name);
			System.out.println("variants of this element: " + e.variants.size());
			for (String s : e.variants) {
				variantCount++;
				System.out.println("variant: " + s);
			}
		}
		System.out.format("***********\nDone. %d elements, %d variants.\n", elementCount, variantCount);
	}
	
	public void printRegexInstructions () {
		System.out.println("*** REGEX INSTRUCTIONS (ENTRIES) ***");
		int count = 0;
		for (RegexInstruction ri : this.regexInstructions) {
			count++;
			System.out.format("INSTRUCTION:\ndescriptor: %s\nregex: %s\nfields: ", ri.entryDescriptor, ri.regex);
			for (Map.Entry<String, Integer> field : ri.fields.entrySet()) {
				System.out.format("%s (%d), ", field.getKey(), field.getValue());
			}
			System.out.println();
		}
		System.out.format("***********\nDone. %d instructions.\n", count);
	}
	
	public static String readLeftPart (String s) {
		int currentIndex = 0;
		char currentChar = s.charAt(currentIndex);
		StringBuilder sb = new StringBuilder();
		
		while (currentChar != ' ') {
			sb.append(currentChar);
			currentIndex++;
			currentChar = s.charAt(currentIndex);
		}
		return sb.toString();
	}
	
	public static String trimLeftPart (String s) {
		int counter = 0;
		for (char c : s.toCharArray()) {
			if (c != '=') {
				counter++;
			} else {
				break;
			}
		}
		return s.substring(counter + 2);
	}
	
	public static int countGroups (String regex) {
		int counter = 0;
		for (int i = 0; i < regex.length(); i++) {
			if (regex.charAt(i) == '(') {
				if (i == 0 || regex.charAt(i - 1) != '\\') {
					counter++;
				}
			}
		}
		return counter;
	}
}
