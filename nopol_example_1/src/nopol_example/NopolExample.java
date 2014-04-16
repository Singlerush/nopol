package nopol_example;

public class NopolExample {

	/*
	 * Return the index char of s 
	 * or the last if index > s.length
	 * or the first if index < 0			
	 */
	public char charAt(String s, int index){
		
		if ( index == 0 ) // Fix index <= 0
			return s.charAt(0);
		
		if ( index < s.length() )
			return s.charAt(index);
		
		return s.charAt(s.length()-1);
	}
}