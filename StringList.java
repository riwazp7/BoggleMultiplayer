/*  
 * Class StringList - Recursive list of words entered by player
 * Methods: 3
 * 
 * -Converts all strings in the list to a single string
 * -Returns number of strings
 * -Finds if a given string is already on the list
 * 
 * Riwaz Poudyal
 * 
 */
public class StringList {

    //To check if list is empty
    private boolean empty=false;

    //First word in the list
    private String newString;

    //Rest of the string in the list
    private StringList restString;

    //Create an empty word list
    public StringList() {
        empty = true; 
    }

    //Create a list from a new list and an existing list
    public StringList( String current, StringList remaining) {
        newString = current;
        restString = remaining;

    }
    //Create a word list from all the strings added to the list
    public String toString(){
        if (empty){
            return "";
        }else{
            return newString + "\n" + restString.toString();
        }
    }

    //Check to see if the list contains the given word by going through it recusively
    public boolean contains ( String newWord ) {
        if (empty){
            return false;

        }else if (newString.equals( newWord  ) ){
            return true;

        }else{
            return restString.contains(newWord);
        }
    }

    //Return the no. of elements in the list
    public int size() {

        if (empty) {
            return 0;    
        } else { 
            return 1+ restString.size(); 
        }

    }

}
