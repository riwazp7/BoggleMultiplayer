import squint.*;
import javax.swing.*;

/*
 * Class BoggleButton - Extension of JButton with row and column as parameter
 * Methods: 3
 * -Returns row and column of buttons
 * -Decides if two buttons are adjacent
 * Riwaz Poudyal
 * 
 */
public class BoggleButton extends JButton
{
    //For row and column number of the buttons
    private int row;
    private int column;

    // Create a new button.
    public BoggleButton( int row, int column ) {
        this.row = row;
        this.column = column;
    }

    // Returns the row number of the button
    public int getRow() {
        return row;
    }

    // Returns the column number of the Boggle button
    public int getColumn() {
        return column;
    }

    // Return true if other button is adjacent to this button horizontally, vertically or diagonally
    public boolean isAdjacentTo( BoggleButton other ) {
        //Get the row and column number of other button
        int oRow= other.getRow();
        int oColumn= other.getColumn();

        if ((((row == oRow) || (column == oColumn)) && Math.abs( row - oRow ) + Math.abs( column - oColumn ) == 1) ) {
            //Return true(adjacent) for buttons that are horizontally or vertically adjacent
            return true;
        }else if ((row != oRow && column != oColumn) && Math.abs( row - oRow ) + Math.abs( column - oColumn ) == 2) {
            //Return true for diagonally adjacent buttons
            return true;
        }else{
            //else return false(not adjacent)
            return false;
        }

    }

}
