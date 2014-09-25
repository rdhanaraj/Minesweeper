import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Cell
{
    // attributes
    int neighboringMines, state;
	public BufferedImage mine, flag, mark, redmine;
    boolean isCovered;
    Image image;
    public static final int BLANK = 0, FLAG = 1, QUESTION_MARK = 2;
    boolean highlighted = false;
    
    public void highlight()
    {
    	highlighted = true;
    }
    
    public void unhighlight()
    {
    	highlighted = false;
    }
    
    public boolean isHighlighted()
    {
    	return highlighted;
    }

	public void setup()
	{
		try
		{
			mine = ImageIO.read(new File("src/birdie.png"));
			flag = ImageIO.read(new File("src/flag.png"));
			mark = ImageIO.read(new File("src/mark.png"));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
    public Cell(int num)
    {
    	setup();
        neighboringMines = num;
        isCovered = true;
    }

    public Cell()
    {
    	setup();
        isCovered = true;
    }

    public int getState()
    {
        return state;
    }

    public void incrementState()
    {
        state++;
        state %= 3;
    }

    public void incrementNeighboringMines()
    {
        neighboringMines++;
    }

    public int getNeighboringMines()
    {
        return neighboringMines;
    }

    public boolean isMine()
    {
        return (neighboringMines == -1);
    }
    
    public boolean isNumber()
    {
        return (neighboringMines > 0);
    }

    public void reveal()
    {
        isCovered = false;
    }

    public boolean isCovered()
    {
        return isCovered;
    }

    public boolean isUncovered()
    {
        return !isCovered;
    }

    public boolean hasNoNeighbors()
    {
        return (neighboringMines == 0);
    }
    
    public boolean isFlagged()
    {
    	return state == FLAG;
    }
    
    public void flag()
    {
    	state = FLAG;
    }

    public Image getImage()
    {
        if(isCovered)
            switch(state)
            {
                case FLAG:          return flag;
                case QUESTION_MARK: return mark;
            }
        else if(isMine())
        	return mine;
        return null;
    }
    
    public static int getUnrevealedNeighbors(Cell[][] cells, int row, int col)
    {
    	int count = 0;
    	for(int i = -1; i <= 1; i++)
    		for(int j = -1; j <= 1; j++)
    			if(!(i == 0 && j == 0) && Minesweeper.inBounds(cells, row+i, col+j))
    				if(cells[row+i][col+j].isCovered())
    					count++;
    	return count;
    }
    
    public static int getFlaggedNeighbors(Cell[][] cells, int row, int col)
    {
    	int count = 0;
    	for(int i = -1; i <= 1; i++)
    		for(int j = -1; j <= 1; j++)
    			if(!(i == 0 && j == 0) && Minesweeper.inBounds(cells, row+i, col+j))
    				if(cells[row + i][col + j].isFlagged())
    					count++;
    	return count;
    }
}