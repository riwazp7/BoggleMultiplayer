import java.awt.*;
import squint.*;
import javax.swing.*;
import java.io.File;

/*
 * Class BoggleMain - Main class for Boggle game.
 * Methods: 22
 * 
 * -Creates the User Interface for the game
 * -Adds all the buttons except game buttons
 * -Creates instance of BoggleButton class
 * -Starts and ends single player as well as multiplayer game
 * -Keeps track of time and ends game when time ends(During single player only)
 * -Handles network operation. Creates connection, sends messages to server and handles incoming messages.
 * -Updates Scores, words and messages in GUI.
 * -Uses several methods to avoid using many public variables and to reduce confusion
 * *BoggleMain and ButtonGrid communicate almost exclusively through methods
 * 
 * Riwaz Poudyal
 * 
 */
public class BoggleMain extends GUIManager
{
    // Change these values to adjust the size of the program's window
    private final int WINDOW_WIDTH = 650, WINDOW_HEIGHT = 497;
    
    //WIDTH and HEIGHT of textareas that display words created
    private final int TAREA_WIDTH = 9, TAREA_HEIGHT= 18;

    //Server information for multiplayer
    private final String serverName= "rath.cs.williams.edu";
    private final int port= 13413 ;

    //Stores opponents name in multiplayer
    private String opponentName;

    //Buttons to handle single/multi player start or stop
    private JButton newGame, findPartner;

    //Textfield to store group name
    private JTextField group;

    //Displays player word initially and opponentWord after the game has ended
    private JTextArea playerWord, opponentWord;

    //Display player name, game type information/ game messages, word being created/word messages
    //player score, opponent score respectively
    private JLabel name, info, wordCombination, singleScore, multiScore;

    //booleans to know whether single player or multiplayer game is currently running.
    private boolean singleRunning=false;
    public boolean multiRunning= false;

    //boolean to use to flash score with pacemaker
    private boolean flash= false;

    //To know if multiplayer game actually started started or not
    private boolean gameStarted=false;

    //progress bar to display time remaining
    private JProgressBar counter;

    //Pacemaker to keep track of time, its start value, tick interval,
    private PaceMaker timer;
    private final int START_TIME = 180;
    private final int TICK_TIME = 1;

    //variable to keep track of time
    private int time;

    //instance of class ButtonGrid that creates game buttons
    private ButtonGrid buttons;

    //server connection for multiplayer
    private NetConnection connection;

    //Create GUI
    public BoggleMain(String playerName, String playerGroup) {

        //Create window to hold all the components
        this.createWindow( WINDOW_WIDTH, WINDOW_HEIGHT, "Boggle by MRsoft" );
        this.setLocation(320,135);

        //Border layout
        contentPane.setLayout( new BorderLayout() );

        //JPanel to hold components
        JPanel panel= new JPanel();

        //Divide panel into grids and set its color
        panel.setLayout( new GridLayout(2,1,0,2));
        panel.setBackground (Color.GRAY);

        //Make a new progress bar with appropriate values and add to panel
        time= START_TIME;
        counter= new JProgressBar(0, time);
        counter.setStringPainted(true);
        counter.setString("3m");
        panel.add(counter);

        //JLabel to display forming word and word messages. 
        wordCombination= new JLabel("Welcome to Boggle!!", SwingConstants.CENTER);  //Set alignment to center

        //Adjust color and font. Add to panel
        wordCombination.setForeground(Color.BLACK);                                  
        wordCombination.setBackground(Color.LIGHT_GRAY);
        wordCombination.setFont(new Font("Serif", Font.BOLD, 19));
        panel.add(wordCombination);

        contentPane.add(panel, BorderLayout.NORTH );

        //new Panel
        panel= new JPanel();

        //Create a grid of buttons (ButtonGrid class) and place in the center
        buttons = new ButtonGrid(this);
        contentPane.add( buttons, BorderLayout.CENTER  );

        //new Panel with box layout
        panel= new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground (Color.DARK_GRAY);

        //JLabel to display player score
        singleScore= new JLabel("Your Score:",SwingConstants.LEFT  );

        //format and add to panel
        singleScore.setForeground(Color.GRAY);
        panel.add(singleScore);

        //TextArea to display player words
        playerWord= new JTextArea (TAREA_HEIGHT, TAREA_WIDTH);
        playerWord.setBackground(Color.LIGHT_GRAY);
        playerWord.setEditable(false);

        panel.add( new JScrollPane(playerWord)); 

        contentPane.add(panel,BorderLayout.WEST );

        //New Panel with Box layout
        panel= new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground (Color.DARK_GRAY);

        //JLabel for opponent score
        multiScore= new JLabel("Enemy Score:");
        multiScore.setForeground(Color.GRAY);
        panel.add(multiScore);

        //JTextArea to display opponent words after game ends
        opponentWord= new JTextArea (TAREA_HEIGHT, TAREA_WIDTH);
        opponentWord.setEditable(false);
        opponentWord.setBackground(Color.LIGHT_GRAY);

        panel.add(new JScrollPane(opponentWord));

        contentPane.add(panel,BorderLayout.EAST);

        //new panel with gridLayout for lower half of the program
        panel= new JPanel();
        panel.setLayout( new GridLayout(3,3,-4,-4));
        panel.setBackground (Color.GRAY);

        //Filling empty grid
        panel.add(new JLabel(""));

        //JLabel that displays game state and multiplayer information etc.
        info= new JLabel("Start Solitaire or Find Partner", SwingConstants.CENTER );
        info.setFont(new Font("Serif", Font.BOLD, 16));
        panel.add(info);

        //Filling empty grid
        panel.add (new JLabel(""));

        //Button to start/stop single player game
        newGame= new JButton("Start New Solitaire");

        newGame.setBackground(Color.DARK_GRAY);
        newGame.setOpaque(true);

        panel.add(newGame);

        //displays interface text
        panel.add(new JLabel("Your Name:  ", SwingConstants.RIGHT));

        //Label to display the player name entered in the start screen
        name=new JLabel();
        name.setText(playerName);

        name.setFont(new Font("Serif", Font.BOLD, 14));
        name.setForeground(Color.WHITE);

        panel.add(name);

        //Button to start/end multiplayer game
        findPartner= new JButton("Find Partner");
        panel.add(findPartner);

        findPartner.setBackground(Color.DARK_GRAY);
        findPartner.setOpaque(true);

        //Interface text
        panel.add(new JLabel("Group:  ", SwingConstants.RIGHT));

        //Textfield where group name can be modified
        group= new JTextField();
        panel.add(group);
        group.setText(playerGroup);

        //Add the lower portion of the interface
        contentPane.add(panel,BorderLayout.SOUTH);

    }

    //Executes when a button other than the game button is clicked
    public void buttonClicked( JButton which ) {
        if (which == newGame) //for newGame button
        {
            if (!singleRunning){
                //Call if solitaire is not running.
                startSingleGame();

            } else{
                //Call if solitaire is running. ends game
                endSingleGame();

            }

        } else if (which == findPartner) {
            if (!multiRunning){
                //Call if multiplayer is not already running
                startMultiGame();

            } else{
                //Call if multiplayer is running
                endMultiGame();

            }
        } 

    }

    //Starts Solitaire
    private void startSingleGame(){
        //Game is running
        singleRunning=true;

        //Set time to start time and start pacemaker
        time=START_TIME;
        timer= new PaceMaker( TICK_TIME, this);

        //Set random letters in the game buttons
        buttons.setButtonSingleP();

        //Change newGame Button text
        newGame.setText("Stop");

        //Reset score from last game 
        multiScore.setText("Enemy Score");
        singleScore.setText("Your Score: 0");

        multiScore.setForeground(Color.GRAY);

        //Reset Word list from last game
        playerWord.setText("");
        opponentWord.setText("");

        //Make JLabel changes to inform game started
        wordCombination.setText("Game Started!");
        wordCombination.setForeground(Color.BLUE);
        info.setText("Playing Solitaire!");

        //Disable multiplayer button
        findPartner.setEnabled(false);
    }

    //Ends Solitaire
    private void endSingleGame(){
        //Game not running
        singleRunning=false;

        //Change and enable buttons
        newGame.setText("Start New Solitaire");
        findPartner.setEnabled(true);

        //Stop counting
        timer.stop();

        //Changes to the GUI:
        singleScore.setForeground(Color.RED);  //Display final score in red

        wordCombination.setText("Game Ended");  //Say game ended
        wordCombination.setForeground(Color.RED);

        info.setText("Start Solitaire or Find Partner");

        //Disable game button grid
        buttons.disableGrid();

        //Reset the list of words
        buttons.resetStringList();
    }

    private void startMultiGame(){
        //Multiplayer game is running
        multiRunning=true;

        //Create a connection with Boggle server and add message listener
        connection= new NetConnection(serverName, port);
        connection.addMessageListener( this );

        //Tell server player wants to paly
        connection.out.println("PLAY "+name.getText()+" "+ group.getText());

        //Display searching for opponents
        info.setText("Searching...");

        //Button changes
        findPartner.setText("Stop");
        newGame.setEnabled(false);

        //Reset score, labels and textareas from last game
        singleScore.setText("Your Score: 0");
        multiScore.setText("Enemy score: 0");

        wordCombination.setText("");

        opponentWord.setText("");
        playerWord.setText("");

    }

    public void endMultiGame(){
        //Prevent double execution from both connection closed and buttonclicked
        if(multiRunning){
            //Button changes
            findPartner.setText("Find Partner");
            newGame.setEnabled(true);

            //Disable game buttons
            buttons.disableGrid();

            //Display game ended
            wordCombination.setText("Game Ended");
            wordCombination.setForeground(Color.RED);

            //Final scores in red
            singleScore.setForeground(Color.RED);
            multiScore.setForeground(Color.RED);

            //Display what words opponent entered
            opponentWord.setText(buttons.sendMultiWordList());

            //Display game result by comparing final score
            if(buttons.sendMultiScore()==0 && buttons.sendSingleScore()==0){
                info.setText("Game didn't start!");  //if game couldn't start or no player entered words

            }else if(buttons.sendMultiScore()==buttons.sendSingleScore()) {
                info.setText("Tie!");               //If scores are equal

            } else if (buttons.sendMultiScore()>buttons.sendSingleScore()){
                info.setText(opponentName+" Won!!"); //Opponent wins

            } else {
                info.setText("You Won!!");           //player wins

            }

            //Reset the list of words
            buttons.resetStringList();

            //Stop timer only if game had started
            if (gameStarted)
                timer.stop();
            gameStarted=false;

            //Close the conection
            connection.close();
        }
        //Game has ended
        multiRunning= false;
    }

    //Executes when data from server is available
    public void dataAvailable(){
        //Store line from server
        String sData= connection.in.nextLine();

        if (sData.startsWith("START")){
            //If line is a START. extract name of opponent
            int sLength= sData.length();

            opponentName= sData.substring(6);
            info.setText("Playing with "+ opponentName);

            //Then extract the letters and add them to a string
            sData="";

            for (int i=0; i<16; i++){
                sData= sData + connection.in.nextLine()+ " ";

            }

            //Set the multiplayer game button using sData
            buttons.setButtonMultiP(sData);

            //Start pacemaker
            time=START_TIME;
            timer= new PaceMaker( TICK_TIME, this);

            //inform that game started
            wordCombination.setForeground(Color.BLUE);
            wordCombination.setText("Game Started with "+ opponentName);

            //game actually started
            gameStarted= true;

        }else if (sData.startsWith("WORD")){
            //Extract word and update list if sData starts with WORD
            buttons.multiWordListUpdate(sData.substring(5));
        } 

    }
    
    //Executed when connection is closed
    public void connectionClosed(){
        //Stop timer only if game actually started
        if (gameStarted){
            timer.stop();
            gameStarted= false;
        }

        //End game
        endMultiGame();
    }

    //Executed every second( PaceMaker)
    public void tick(){
        if (time >= 0)
        {
            //Set JProgressBar value to time remaining
            counter.setValue(time);

            //Display time remaining as minutes and seconds
            counter.setString(""+ time/60+"m "+ time%60+ "s");

            //Decrease time every time tick is executed
            time--;

            //Change the color of JLabel displaying score to create a flashing effect.
            //flash opponent score only in multiplayer game
            if(flash){
                singleScore.setForeground(Color.DARK_GRAY);
                if (multiRunning)
                    multiScore.setForeground(Color.WHITE);

            } else{
                singleScore.setForeground(Color.WHITE);
                if (multiRunning)
                    multiScore.setForeground(Color.DARK_GRAY);

            }
            flash=!flash;

        }

        else{ 
            //End single player game only
            if (!multiRunning){
                //Stop timer, click buttons, and endgame
                newGame.doClick();
                timer.stop();
                endSingleGame();
            }
        }

    }

    //Update word being entered
    public void updateWord(String word){
        wordCombination.setForeground(Color.BLUE);
        wordCombination.setText(word);

    }

    //Update the single word list
    public void updateSingleWordlist (String word){
        playerWord.setText(word);

    }

    //Update the opponent word list
    public void updateMultiWordlist (String word){
        opponentWord.setText(word);
    }

    //Update single player score
    public void updateSingleScore(int n){
        singleScore.setText("Your Score: "+ n);
    }

    //Update opponent score
    public void updateMultiScore(int n){
        multiScore.setText("Enemy Score: "+ n);
    }

    //send created word to the server
    public void sendWord (String word){
        connection.out.println("WORD " + word);
    }

    //Following seven methods display messages on the wordCombination Label about entered words/Letters
    //They use timer to display one among two different message for each action

    //When a new word is entered
    public void sayNice(){
        if (time % 2==0)
            wordCombination.setText("Nice!");
        else
            wordCombination.setText("Bravo!");
        wordCombination.setForeground(Color.WHITE);
    }

    //When an invalid word is entered
    public void sayNotValid(){

        if (time % 2==0)
            wordCombination.setText("Word Doesn't Exist!");
        else
            wordCombination.setText("Inventing Words, Are You??");
        wordCombination.setForeground(Color.RED);
    } 

    //When entered word is less than 3 letters long
    public void sayTooShort(){

        if (time % 2==0)
            wordCombination.setText("Word Too Short!");
        else
            wordCombination.setText("Lazy Bone!!");
        wordCombination.setForeground(Color.RED);
    }

    //When player tries to reenter word already on list
    public void sayAlreadyEntered(){

        if (time % 2==0)
            wordCombination.setText("Word Already Entered!");
        else
            wordCombination.setText("Cheater, Repeater!!");
        wordCombination.setForeground(Color.RED);
    }

    //When invalid prefix is entered
    public void sayNoSuchWord(){

        if (time % 2==0)
            wordCombination.setText("Wrong Way, Shakespeare!");
        else
            wordCombination.setText("No Such Word Exists!");
        wordCombination.setForeground(Color.RED);
    }

    //When invalid button is clicked
    public void sayInvalid(){
        if (time % 2==0)
            wordCombination.setText("Invalid Button!");
        else
            wordCombination.setText("Where're you going??");
        wordCombination.setForeground(Color.RED);
    }

    //When common words are cancelled
    public void sayCancelled(){
        if (time % 2==0)
            wordCombination.setText("Common Word!");
        else
            wordCombination.setText("Copycats!");
        wordCombination.setForeground(Color.RED);
    }

    //Following two method is used by the start class

    //Click on the start new game button to start a solaitaire game
    public void clickSolitaire(){
        newGame.doClick();
    }

    //Click on findPlayer button to start a multiplayer game
    public void clickPartner(){
        findPartner.doClick();
    }
}
