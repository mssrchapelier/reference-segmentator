import java.util.ArrayList;

public class Element {
	String name;
	ArrayList<String> variants;
	
	public Element (String name) {
		this.name = name;
		this.variants = new ArrayList<String>();
	}
}
