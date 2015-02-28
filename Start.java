import java.awt.*;
import squint.*;
import javax.swing.*;

/* Class Start - Starting interface for the game. 
 * Methods: 2
 * -Prompts user to enter name and group
 * -Player can either start a single game (solitaire) or try to find partner
 * -Window gets hidden after button is clicked
 * 
 * Riwaz Poudyal
 * 
 */
public class Start extends GUIManager
{
    // Change these values to adjust the size of the program's window
    private final int WINDOW_WIDTH = 360, WINDOW_HEIGHT = 234;
    
    //Textfield lengths
    private final int FIELD_LENGTH = 6;
    
    //Jlabels for user interface
    private JLabel boggle, name, version, group, company;

    //Textfields to enter name and group
    private JTextField nameSpace, groupSpace;

    //Buttons to start solitaire game or find partner to play multiplayer
    private JButton solitaire, partner;
    
    //Image to display in interface
    ImageIcon icon= new ImageIcon("Images/boggle.png");

    //Instance of BoggleMain class
    private BoggleMain game;

   
    public Start() {
        // Create window to hold all the components
        this.createWindow( WINDOW_WIDTH, WINDOW_HEIGHT, "Boggle by MRsoft");
        this.setBackground(Color.LIGHT_GRAY);
        this.setLocation(470,250);
        
        //Panel with BOX layout holds boggle and version JLabel
        JPanel panel= new JPanel();
        panel.setLayout( new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground (Color.LIGHT_GRAY);
        
        boggle = new JLabel();
        panel.add(boggle);
        boggle.setIcon(icon);

        version= new JLabel("Version 1.01.014");
        version.setFont(new Font("Serif", Font.BOLD, 12));  //Set font
        version.setForeground(Color.RED);
        panel.add(version);
        
        contentPane.add( panel );

        //Panel with Gridlayout holds name and group JLabels and TextFields
        panel = new JPanel();
        panel.setLayout(new GridLayout(2,2));
        panel.setBackground(Color.DARK_GRAY);

        name = new JLabel(" Name    ");
        name.setForeground(Color.WHITE);
        panel.add( name );

        nameSpace = new JTextField( 6 );
        panel.add( nameSpace );
        nameSpace.setText("Guest");
        nameSpace.requestFocus();

        group= new JLabel(" Group    ");
        group.setForeground(Color.WHITE);
        panel.add(group);

        groupSpace = new JTextField( FIELD_LENGTH );
        panel.add( groupSpace );

        contentPane.add( panel);

        //Panel with GridLayout holds buttons solitaire and partner
        panel= new JPanel();
        panel.setLayout(new GridLayout(2,2));
        panel.setBackground(Color.DARK_GRAY);
        
        solitaire = new JButton( "Solitaire" );
        panel.add( solitaire );

        partner = new JButton( "Find Partner" );
        panel.add( partner );
        
        contentPane.add( panel);
        
        //Panel with gridLayout holds company JLabel
        panel= new JPanel();
        panel.setLayout(new GridLayout(2,1));
        panel.setBackground(Color.LIGHT_GRAY);
        
        panel.add(new JLabel(""));  //Adds empty space in interface

        company= new JLabel("©MRsoft-2014/134");
        company.setFont(new Font("Serif", Font.BOLD, 13));  
        company.setForeground(Color.BLACK);

        panel.add(company);
        
        contentPane.add(panel);

        //set cursor to nameSpace TextField
        nameSpace.requestFocus();

    }
   
    //Excutes when button is clicked
    public void buttonClicked( JButton which) {
        //Extract name and group
        String name= nameSpace.getText();
        String group= groupSpace.getText();
        if (which == solitaire && name.length()>0){
            //If Solitaire button clicked, create instance of Boggle main and
            //click newGame button. Hide this window
            game= new BoggleMain(name, group);
            game.clickSolitaire();
            this.setVisible(false);

        } else if (which == partner && name.length()>0 ) {
            //If partner button clicked, create instance of Boggle main and
            //click findPartner button. Hide this window.
            game= new BoggleMain(name, group);
            game.clickPartner();
            this.setVisible(false);
        } else if (!(name.length()>0)){
            //If name is not entered, display message on JTextArea
            nameSpace.setText("Enter Name");
        }

    }
    
    //Prevents players from resizing the window
    public void windowResized(){
        this.setSize( WINDOW_WIDTH, WINDOW_HEIGHT);

    }
}
 