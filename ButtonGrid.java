import squint.*;
import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.awt.Color;

/*
 * Class ButtonGrid - Grid of game buttons.
 * Methods: 14
 * 
 * -Creates the game button grid
 * -Enables/Disables buttons in the grid
 * -Sets the letters in buttons for both single and multi player
 * -Handles the click of game buttons
 * -Determines the type (adjacent, last clicked, etc.) of buttons and handles what to do
 * -Checks if the word entered is a valid word, is a word already in the list, or is a valid prefix.
 * -Creates/Updates/Resets word lists and updates the score and word displayed in the GUI
 *
 ***One extra featue is, because there are only two players, unlike real boggle, this game verison cancels and updates scores 
 *  of common words in realtime to make the game more competitive.The original version can be created through minor changes.
 * 
 * Riwaz Poudyal
 * 
 *
 */
public class ButtonGrid extends GUIManager
{
    //Height and Width of the button grid
    private final int WIDTH = 4;
    private final int HEIGHT = 4;

    //Size of letters in game buttons
    private final float SIZE = 35;

    //Font used for buttons
    private final Font BUTTONFONT = this.getFont().deriveFont( SIZE );

    // To random letters to choose from the word array
    private Random chooser = new Random();

    // Array of all buttons
    private  BoggleButton [] allButtons = new BoggleButton[ WIDTH*HEIGHT ];

    //Array of letters from which the letters in the game is choosen
    private final String [][] cubeSides =
        new String[][]{
            { "A", "A", "C", "I", "O", "T" },
            { "A", "B", "I", "L", "T", "Y" },
            { "A", "B", "J", "M", "O", "Qu"},
            { "A", "C", "D", "E", "M", "P" },
            { "A", "C", "E", "L", "S", "R" },
            { "A", "D", "E", "N", "V", "Z" },
            { "A", "H", "M", "O", "R", "S" },
            { "B", "F", "I", "O", "R", "X" },
            { "D", "E", "N", "O", "S", "W" },
            { "D", "K", "N", "O", "T", "U" },
            { "E", "E", "F", "H", "I", "Y" },
            { "E", "G", "I", "N", "T", "V" },
            { "E", "G", "K", "L", "U", "Y" },
            { "E", "H", "I", "N", "P", "S" },
            { "E", "L", "P", "S", "T", "U" },
            { "G", "I", "L", "R", "U", "W" }
        };

    //An instance of the BoggleMain class
    private BoggleMain mainClass;   

    //Array to keep track of buttons that are clicked 
    private BoggleButton [] clicked = new BoggleButton[16];

    //int variable to store the no. of buttons clicked
    private int counter=0;

    //Instance of Lexicon
    private Lexicon wordTest= new Lexicon("lexicon.dat");

    //Word that is being built by the player
    private String buildingWord= "";

    //Word list for single player and opponent in multiplayer
    private StringList stringCheckSingle= new StringList();
    private StringList stringCheckMulti= new StringList();

    //The number of matching word between player and opponent to deduct from score
    private int sameWordCount=0;

    //Creates game button grid
    public ButtonGrid( BoggleMain mainBoggle) {
        // instance of BoggleMain class
        mainClass= mainBoggle;

        //negative value gives a nice Boggle-cube-like overlapping effect
        contentPane.setLayout( new GridLayout(HEIGHT, WIDTH, -7, -7) );

        //Create individual buttons and put them in gridLayout. Set initial text.
        for ( int y = 0; y < HEIGHT; y++ ) {
            for ( int x = 0; x < WIDTH; x++ ) {
                BoggleButton button = new BoggleButton( y, x );
                allButtons[ y*WIDTH + x ] = button;
                allButtons[ y*WIDTH + x].setText("Boggle");
                contentPane.add( button );
            }
        }
        //Initially disable buttons
        disableGrid();

    }

    //Generate random letters and set them to buttons
    public void setButtonSingleP( ) {
        //Reset common words count from previous game
        sameWordCount=0;
        
        int randomRow, randomColumn; //Random int generated
        int length=15;               //Length of array to look from
        for (int i=0; i<16; i++){
            //Generate random row, column and set its letter to button
            randomRow= chooser.nextInt(length+1);
            randomColumn= chooser.nextInt(6);
            allButtons[i].setText (cubeSides [randomRow][randomColumn]);

            //Replace random the row with the last row and last row with the random row
            String [] temp= cubeSides[randomRow];
            cubeSides[randomRow]= cubeSides[length];
            cubeSides[length]= temp;

            length--; //Stop looking at the current last row
            allButtons[i].setFont( BUTTONFONT );  //Set game button font

        }
        //reset counter and array from last game
        reset();
        
        //Enable game buttons
        enableGrid();
    }

    //Set button for multi player
    public void setButtonMultiP(String gridWord){
        //reset value from previous game
        sameWordCount=0;

        //Extract letters from string and set them to buttons.
        int space=-1;
        for(int i=0; i<16; i++){

            allButtons[i].setText (gridWord.substring(space+1, gridWord.indexOf(" ", space+1)));
            space=gridWord.indexOf(" ", space+1);

            allButtons[i].setFont( BUTTONFONT );   //Big font

        }
        
        //reset counter and array from last game
        reset();
        
        enableGrid();   //Enable game buttons

    }

    //Handles clicked game buttons
    public void buttonClicked( JButton which ) {
        //TypeCast Jbuttons to BoggleButtons
        BoggleButton whichButton= (BoggleButton)which;

        if (counter==0){
            //If first button clicked

            //Store clicked button and change its color
            clicked[counter]= whichButton;
            clicked [counter].setForeground(Color.BLUE);

            //Get the letter in the button and display it in Jlabel
            buildingWord=clicked[counter].getText();
            mainClass.updateWord(buildingWord);

            //Button count increased by one
            counter++;

        }else if (whichButton == clicked[counter-1]){
            //If clicked button is the last button clicked

            //Extract the letters from all previously clicked buttons and make a word
            String finalWord= "";
            for(int i=0; i<counter; i++){
                finalWord = finalWord + clicked[i].getText();

            }

            if (validWord(finalWord) && (counter>=3) && !(stringCheckSingle.contains(finalWord))) {
                //if entered word is valid, more than 2 letters long, and isn't already entered

                //To see if the entered word was in opponents list(to later display a message)
                Boolean cancelled= false;

                //Add word to string list
                stringCheckSingle = new StringList(finalWord, stringCheckSingle);

                //Get word list and update in TextArea
                String word= stringCheckSingle.toString();
                mainClass.updateSingleWordlist(word);

                //If multiplayer is running
                if (mainClass.multiRunning){
                    //send formed word to server
                    mainClass.sendWord(word); 

                    //If the multiplayer list contains the word, increase sameWordCount
                    if (stringCheckMulti.contains(finalWord)){
                        sameWordCount++;
                        cancelled= true;
                    }

                    //Update multiplayer score
                    mainClass.updateMultiScore(stringCheckMulti.size() - sameWordCount);
                }
                //Update single player score
                mainClass.updateSingleScore(stringCheckSingle.size()- sameWordCount);

                //reset word buliding Labels, strings, and variables
                reset();

                //Word was accepted
                mainClass.sayNice();

                //Say word cancelled if cancelled is true
                if (cancelled)
                    mainClass.sayCancelled();

            } else if (!(counter>=3)){
                //If word is less than 3 letters long, reset and say too short
                reset();
                mainClass.sayTooShort();

            }else if (!validWord(finalWord)){
                //If invalid word, say invalid word and reset
                reset();
                mainClass.sayNotValid();

            }  else if(stringCheckSingle.contains(finalWord)){
                //if word already entered, reset and say already entered
                reset();
                mainClass.sayAlreadyEntered();

            }
        } else if (alreadyClicked (whichButton)){
            //If button is already clicked ( but other than last clicked button)
            reset();

        } else if (whichButton.isAdjacentTo (clicked[counter-1])){
            //If it is an adjacent button

            //Get the word being built
            String prefix= buildingWord + whichButton.getText();

            if ( validPrefix(prefix) ){
                //If prefix is valid, store that button, display the prefix in JLabel, change colors appropriately,
                // and increase counter
                clicked[counter]= whichButton;
                buildingWord= buildingWord + clicked[counter].getText();

                clicked[counter-1].setForeground(Color.RED);
                clicked[counter].setForeground(Color.BLUE);

                counter++;

                mainClass.updateWord(buildingWord);
            } else{
                //Say no such words and reset
                reset();
                mainClass.sayNoSuchWord();
            }

        } else {
            //Else say button was invalid
            mainClass.sayInvalid();
        }
    }

    //Check if the button is already clicked by searching through the array of clicked buttons
    private boolean alreadyClicked ( JButton current){
        boolean check = false;
        for (int i=0; i<counter; i++){
            if (clicked[i]== current)
                check= true;
        }
        return check;
    }  

    //Check using Lexicon if the given word is a valid one
    private boolean validWord( String word){
        return (wordTest.contains(word));
    }

    //Check if the given String is a valid prefix
    private boolean validPrefix( String word){
        return (wordTest.containsPrefix(word));
    }

    //Enable all game buttons
    private void enableGrid(){
        for ( int y = 0; y < HEIGHT; y++ ) {
            for ( int x = 0; x < WIDTH; x++ ) {

                allButtons[ y*WIDTH + x].setEnabled(true);
            }
        }
    }

    //Disable all game buttons and also reset their color
    public void disableGrid(){
        for ( int y = 0; y < HEIGHT; y++ ) {
            for ( int x = 0; x < WIDTH; x++ ) {

                allButtons[ y*WIDTH + x].setEnabled(false);
                allButtons[ y*WIDTH + x].setForeground(Color.BLACK);
            }
        }
    }

    //Reset components used to bulid words in the game.
    private void reset(){
        for(int i=0; i<counter; i++){
            //Change all button color to black
            clicked[i].setForeground(Color.BLACK);
        }

        //reser counter and clicked button array.
        counter=0; 
        clicked= new BoggleButton[16];

        //Update JLabel
        mainClass.updateWord("");

    }

    //Reset word list
    public void resetStringList(){
        stringCheckSingle= new StringList();
        stringCheckMulti= new StringList();

    }

    //Updates scores when playing multiplayer
    public void multiWordListUpdate(String word){
        //Add word to multiplayer list
        stringCheckMulti= new StringList(word, stringCheckMulti);

        //If word is already entered by player, say cancelled and increase sameWordCount
        if(stringCheckSingle.contains(word)){
            sameWordCount++;
            mainClass.sayCancelled();
        }

        //Update scores in real time
        mainClass.updateMultiScore(stringCheckMulti.size()-sameWordCount);
        mainClass.updateSingleScore(stringCheckSingle.size()-sameWordCount);

    }

    //Return opponent word list
    public String sendMultiWordList(){
        return stringCheckMulti.toString();
    }
    
    //Returns single player score
    public int sendSingleScore(){
     return stringCheckSingle.size();
    }
    
    //return multiplayer score
    public int sendMultiScore(){
        return stringCheckMulti.size();
    }
    
}
