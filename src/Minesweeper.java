/**
 * @author	Rishi Dhanaraj, P6
 * @version	03/08/2013
 * @time	6-8 hours
 * @reflection
 * DONE!!! This lab was really straightforward because I thought
 * everything through UNTIL the AI. I hadn't planned for that, and
 * so accessing the methods and such were all a pain. I felt like
 * I had done it neatly until I realized I had to set a button to
 * call the method. I had coded neat little flagging and revealing
 * methods, but they had to switch between highlighting and acting
 * upon the highlights which was super hard to debug. I just had so
 * many different errors throughout this program and I have no clue
 * how I'd do a lot of this without Eclipse. Things like the scrolling
 * would just be so much harder. The customization was a nice feature
 * I built in towards the end as well. I don't know why, but it
 * currently doesn't work, I get an OutOfMemoryError, but besides that
 * everything is working.
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class Minesweeper
{	
	// ATTRIBUTES
	JFrame window;
	Cell[][] cells;
	int BOX_NUM = 12, BOX_WIDTH = 40, MINES_NUM = 10;
	JPanel drawingPad, jp1;
	Point point;
	int mines, time, flags;
	JLabel mineCount, timeElapsed;
	JButton step;
    JScrollPane aboutPane, helpPane;
    javax.swing.Timer timer;
	boolean allUncovered, aiOn, onAuto, onPreview = true;
	Player ai;
	JCheckBoxMenuItem autoPlay;
	JLabel log;
	
	public static void main(String[] args)
	{
		new Minesweeper(12, 40, 10);
	}
	
	public static void makeGame(int i, int j, int k)
	{
		new Minesweeper(i, j, k);
	}
	
	public Minesweeper(int boxNum, int boxWidth, int minesNum)
	{
		BOX_NUM = boxNum;
		BOX_WIDTH = boxWidth;
		MINES_NUM = minesNum;
		log = new JLabel("Game begun! Let's play some minesweepuh :)");
		ai = new Player();
		step = new JButton("Step");
		
        // drawing board
        drawingPad = new MyDrawingPanel();
        drawingPad.setBounds(BOX_WIDTH, BOX_WIDTH, BOX_NUM*BOX_WIDTH, BOX_NUM*BOX_WIDTH);
        drawingPad.setPreferredSize(new Dimension(BOX_NUM*BOX_WIDTH, BOX_NUM*BOX_WIDTH));
        
        // set up window
        window = new JFrame("Minesweeper");
        
        window.setBounds(0, 0, BOX_NUM*BOX_WIDTH+50, BOX_NUM*BOX_WIDTH+140);
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        
        // Create & set menu
        JMenuBar menu = new JMenuBar();
        JMenu game = new JMenu("Game");
        JMenu options = new JMenu("Options");
        JMenu help = new JMenu("Help");
        JMenuItem newGame = new JMenuItem("New Game");
        JMenuItem exit = new JMenuItem("Exit");
        JMenuItem totalMines = new JMenuItem("Total Mines");
        autoPlay = new JCheckBoxMenuItem("Auto Play");
        JMenuItem customGame = new JMenuItem("Custom Game");
        JMenuItem overview = new JMenuItem("Overview");
        JMenuItem about = new JMenuItem("About");
        
        game.add(newGame);
        game.add(exit); 
        options.add(totalMines); 
        options.add(autoPlay); 
        options.add(customGame);
        help.add(overview); 
        help.add(about);
        
        menu.add(game);
        menu.add(options);
        menu.add(help);
        
        // info labels
        mineCount = new JLabel("" + mines);
        mineCount.setPreferredSize(new Dimension(drawingPad.getWidth()/2, 50));
        mineCount.setBorder(BorderFactory.createTitledBorder("Mine Count"));
        
        timeElapsed = new JLabel("" + time);
        timeElapsed.setPreferredSize(new Dimension(drawingPad.getWidth()/2, 50));
        timeElapsed.setBorder(BorderFactory.createTitledBorder("Time Elapsed"));
        jp1 = new JPanel();
        jp1.setLayout(new GridLayout(1, 2));
        jp1.add(mineCount);
        jp1.add(timeElapsed);
        
        drawingPad.addMouseListener(new MouseAdapter()
        {
        	@Override
        	public void mouseClicked(MouseEvent e)
        	{
        		int row = e.getX() / BOX_WIDTH, col = e.getY() / BOX_WIDTH;
        		if(cells[row][col].isUncovered()) return;
        		if(SwingUtilities.isRightMouseButton(e))
        			addFlag(row, col);
        		else
        			evaluate(row, col);
        	}
        });
        
        drawingPad.addMouseMotionListener(new MouseAdapter()
        {
			@Override
        	public void mouseMoved(MouseEvent e)
        	{
        		int row = e.getX() / BOX_WIDTH, col = e.getY() / BOX_WIDTH;
        		highlightCell(row, col);
        	}
        });
        
        newGame.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reload();
			}
		});
        
        exit.addActionListener(new ActionListener()
		{			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				window.dispose();
			}
		});
        
        totalMines.addActionListener(new ActionListener()
		{			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int num = Integer.parseInt(JOptionPane.showInputDialog("Select a number of mines"));
				fillCells(num);
				drawingPad.repaint();
				mineCount.setText(""+flags);
				JOptionPane.showMessageDialog(null, "Number of mines has been changed.");
			}
		});
        
        autoPlay.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				window.setLocationRelativeTo(null);
				if(autoPlay.getState() != onAuto) // if state is changed
				{
					if(autoPlay.getState()) // if on
					{
						reload();
						window.add(step);
						window.setBounds(BOX_WIDTH, BOX_WIDTH, window.getWidth(), window.getHeight());
						log.setText("The AI is going to take a shot now! Learn from a pro");
					}
					else
					{
						reload();
						window.remove(step);
						log.setText("Think you're ready? Alright, ready, set, GO!");
					}
					onAuto = !onAuto;
				}
			}
		});
        
        step.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				ai.move();
			}
		});
        
        customGame.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				window.dispose();
				int boxnum = Integer.parseInt(JOptionPane.showInputDialog("Select an n-by-n grid (Recommended 9-15)"));
				int boxwidth = Integer.parseInt(JOptionPane.showInputDialog("Select a box width (Recommended 25-40)"));
				int minesnum = Integer.parseInt(JOptionPane.showInputDialog("Select a number of mines, less than " + boxnum *boxnum));
				Minesweeper.makeGame(boxnum, boxwidth, minesnum);
			}
		});
        
        try
        {
        	JEditorPane helpContent = new JEditorPane(new URL("file:src/help.html"));
        	helpPane = new JScrollPane(helpContent);
        	
        	JEditorPane aboutContent = new JEditorPane(new URL("file:src/about.html"));
        	aboutPane = new JScrollPane(aboutContent);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
        overview.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JOptionPane.showMessageDialog(null, helpPane, "How To Play & More", JOptionPane.PLAIN_MESSAGE, null);
			}
		});
        
        about.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JOptionPane.showMessageDialog(null, aboutPane, "About", JOptionPane.PLAIN_MESSAGE, null);
			}
		});
        
        timer = new javax.swing.Timer(1000, new ActionListener()
		{			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				time++;
				timeElapsed.setText("" + time);
			}
		});
        timer.start();
        
        reload();
        
        window.setJMenuBar(menu);
        window.setLayout(new FlowLayout());
        window.add(jp1);
        window.add(drawingPad);
        JPanel temp = new JPanel();
        temp.setLayout(new BorderLayout());
        temp.add(log, BorderLayout.CENTER);
        window.add(temp);
        window.setVisible(true);
	}
	
	public void highlightCell(int row, int col)
	{
		point = new Point(row, col);
		drawingPad.repaint();
	}
	
	public void addFlag(int row, int col)
	{
		cells[row][col].incrementState();
		if(cells[row][col].getState() == Cell.QUESTION_MARK)
			flags++;
		else if(cells[row][col].getState() == Cell.FLAG)
			flags--;
		mineCount.setText(""+flags);
		log.setText("Last placed flag at (" + row + ", " + col + ")");
		drawingPad.repaint();
	}
	
    public void reload()
    {
    	allUncovered = false; 			
        cells = new Cell[BOX_NUM][BOX_NUM];
        flags = MINES_NUM;
        fillCells(MINES_NUM);
        mineCount.setText("" + flags);
        timer.stop();
        time = 0;
        drawingPad.repaint();
        timer.start();
    }
	
    public void fillCells(int minesnum)
    {
    	MINES_NUM = minesnum;
    	flags = MINES_NUM;
        for(int i = 0; i < cells.length; i++)
            for(int j = 0; j < cells[i].length; j++)            
                cells[i][j] = new Cell();
        for(int i = 0; i < minesnum; i++)
            cells[(int)(Math.random() * BOX_NUM)][(int)(Math.random() * BOX_NUM)]
            = new Cell(-1);
        for(int i = 0; i < cells.length; i++)
            for(int j = 0; j < cells[i].length; j++)
                if(cells[i][j].isMine())
                    for(int k = -1; k <= 1; k++)
                        for(int l = -1; l <= 1; l++)
                            if(inBounds(cells, i + k, j + l) && !cells[i + k][j + l].isMine())
                                cells[i + k][j + l].incrementNeighboringMines();
    }
    
    public static boolean inBounds(Cell[][] cells, int row, int col)
    {
        return (col >= 0) && (col < cells[0].length)
        && (row >= 0) && (row < cells.length);
    }
    
    public boolean makeMove(int x, int y)
    {
        if(!inBounds(cells, x, y))
            return false;
        cells[x][y].reveal();
        if(cells[x][y].isMine())
            return true;
        floodFill(x, y);
        drawingPad.repaint();
        return isFinished();
    }

    public void floodFill(int row, int col)
    {
        if(inBounds(cells, row, col))
        {
            cells[row][col].reveal();
            if(cells[row][col].isNumber())  return;
            foo(row+1, col);
            foo(row+1, col+1);
            foo(row, col+1);
            foo(row-1, col+1);
            foo(row-1, col);
            foo(row-1, col-1);
            foo(row, col-1);
            foo(row+1, col-1);
        }
    }

    // if a number, reveal and stop
    // if blank, reveal and continue
    public void foo(int row, int col)
    {
        if(inBounds(cells, row, col) && !cells[row][col].isMine() && cells[row][col].isCovered())
            floodFill(row, col);
    }

    public boolean isFinished()
    {
        boolean finished = true;
        for(Cell[] cc: cells)
            for(Cell c: cc)
                if (!(c.isUncovered() || c.isMine()))
                    finished = false;
        return finished;
    }
    
    public boolean evaluate(int x, int y)
    {
        if(makeMove(x, y))
        {
            if(isFinished()) 
            {
            	log.setText("Game success! Won in " + time + " seconds");
            	int choice = JOptionPane.showConfirmDialog(null, "Up for another game? Beat your last score of " +
        				time + " seconds");
        		if(choice == JOptionPane.YES_OPTION)
        			reload();
        		else if(choice == JOptionPane.NO_OPTION)
        			window.dispose();
            }
            else
            {
            	uncoverAll(x, y);
            	log.setText("Game over. Mine exploded at (" + x + ", " + y + ")");
            	
            	int choice = JOptionPane.showConfirmDialog(null, "Up for another game? You can do better");
        		if(choice == JOptionPane.YES_OPTION)
        			reload();
        		else if(choice == JOptionPane.NO_OPTION)
        			window.dispose();
            }
            return true;
        }
        else
        	log.setText("Last revealed at (" + x + ", " + y + ")");
        return false;
    }
	
    public void uncoverAll(int row, int col)
	{
		for(Cell[] cc: cells)
			for(Cell c: cc)
				c.reveal();
		allUncovered = true;
		drawingPad.repaint();
	}

	private class MyDrawingPanel extends JPanel
    {
        static final long serialVersionUID = 1234567890L;
        
        public void paintComponent(Graphics g) 
        {
        	// start from blank slate
            g.setColor(Color.BLACK);
            g.fillRect(2, 2, this.getWidth() - 2, this.getHeight() - 2);

            // draw "lines" and any filled boxes
            for(int i = 0; i < BOX_NUM; i++)
            	for(int j = 0; j < BOX_NUM; j++)
            	{
            		if(allUncovered && cells[i][j].isMine())
            			g.setColor(Color.RED);
            		else if(cells[i][j].isCovered())
            			g.setColor(Color.GRAY);
            		else
            			g.setColor(Color.WHITE);
            		g.fillRect(i*BOX_WIDTH, j*BOX_WIDTH, BOX_WIDTH, BOX_WIDTH);
            		
            		if(cells[i][j].getImage() != null)
            			g.drawImage(cells[i][j].getImage(), i*BOX_WIDTH, j*BOX_WIDTH, BOX_WIDTH, BOX_WIDTH, null);
            		
            		if(cells[i][j].isNumber() && cells[i][j].isUncovered())
            		{
            			switch(cells[i][j].getNeighboringMines())
            			{
            				case 1: g.setColor(new Color(255, 0, 132)); break;
            				case 2: g.setColor(new Color(10, 255, 17));	break;
            				case 3: g.setColor(new Color(255, 206, 0));	break;
            				case 4: g.setColor(new Color(0, 242, 255));	break;
            				case 5: g.setColor(new Color(160, 0, 255));	break;
            				case 6: g.setColor(new Color(246, 255, 0));	break;
            				default: g.setColor(Color.BLACK);
            				
            			}
            			g.setFont(g.getFont().deriveFont(Font.BOLD, 24));
            			FontMetrics metrics = g.getFontMetrics();
            			int mWidth = metrics.stringWidth("" + cells[i][j].getNeighboringMines());
            			int mHeight = metrics.getHeight();
            			
            			g.drawString("" + cells[i][j].getNeighboringMines(), i*BOX_WIDTH + BOX_WIDTH/2- mWidth/2, j*BOX_WIDTH+ BOX_WIDTH/2+ mHeight/2);
            		}
            		g.setColor(Color.LIGHT_GRAY);
            		g.drawRect(i*BOX_WIDTH, j*BOX_WIDTH, BOX_WIDTH, BOX_WIDTH);
            		
            		if(cells[i][j].isHighlighted())
            		{
	        			g.setColor(new Color(0, 255, 0, 100));
	        			g.fillRect(i * BOX_WIDTH, j * BOX_WIDTH, BOX_WIDTH, BOX_WIDTH);
            		}
            	}           
    		if(point != null)
    		{
    			g.setColor(new Color(0, 0, 255, 100));
    			g.fillRect(point.x * BOX_WIDTH, point.y * BOX_WIDTH, BOX_WIDTH, BOX_WIDTH);
    		}
        }
    }
    
    private class Player
    {
    	boolean state = false, guessingMode = false;
    	public void move()
    	{
    		System.out.println(new Object(){}.getClass().getEnclosingMethod().getName());
    		state = !state;
    		
    		// if less than 20% revealed, guess randomly
    		if(getUnrevealedCells() > (0.8 * BOX_NUM*BOX_NUM))
    		{
    			if(onPreview)
    				guessRandomly();
    			else
    				revealAll();
    			onPreview = !onPreview;
    			state = false;
    			return;
    		}
    		
    		
    		boolean changed = true;
    		if(!guessingMode)
    		{
	    		if(state)
	    		{
	    			if(onPreview)
	    			{
	    				flagPossible();
	    				state = !state; // to counteract the next changing of state
	    			}
	    			else
	    			{
	    				int count = flagged();
	    				flagAll();
	    				changed = (count != flagged());
	    			}
	    			onPreview = !onPreview;
	    		}
	    		else
	    		{
	    			
	    			if(onPreview)
	    			{
	    				revealPossible();
	    				log.setText("Highlighted known free spaces");
	    				state = !state; // to counteract the next changing of state
	    			}
	    			else
	    			{
	    				int count = revealed();
	    				revealAll();
	    				changed = (count != revealed());
	    			}
	    			onPreview = !onPreview;
	    		}
    		}
    		
    		if(guessingMode || !changed)
    		{
    			guessingMode = true;
    			if(onPreview)
    				guessRandomly();
    			else
    			{
	    			revealAll();
	    			guessingMode = false;
    			}
    			onPreview = !onPreview;
    		}
    	}
    	
    	private void guessRandomly()
    	{
    		System.out.println(new Object(){}.getClass().getEnclosingMethod().getName());
    		while(true)
    		{
    			int x = (int)(Math.random()*BOX_NUM), y = (int)(Math.random()*BOX_NUM);
    			if(cells[x][y].isCovered() && !cells[x][y].isFlagged())
    			{
    				cells[x][y].highlight();
    	    		drawingPad.repaint();
    	    		log.setText("Chosen random cell");
    				break;
    			}
    		}
    	}
    	
    	private int flagged()
    	{
    		int count = 0;
			for(Cell[] cc: cells)
				for(Cell c: cc)
					if(c.isFlagged())
						count++;
			return count;
    	}
    	
    	private int revealed()
    	{
    		int count = 0;
			for(Cell[] cc: cells)
				for(Cell c: cc)
					if(c.isUncovered())
						count++;
			return count;
    	}
    	
    	private void flagPossible()
    	{
    		System.out.println(new Object(){}.getClass().getEnclosingMethod().getName());
    		// flag all known mines: find cells that are numbers and have the same number of unrevealed neighbors
    		for(int i = 0; i < cells.length; i++)
    			for(int j = 0; j < cells[0].length; j++)
    				if(cells[i][j].isUncovered())
    					if(cells[i][j].isNumber() && Cell.getUnrevealedNeighbors(cells, i, j) == cells[i][j].getNeighboringMines())
    					{
    						for(int k = -1; k <= 1; k++)
    							for(int l = -1; l <= 1; l++)
    								if(!(k == 0 && l == 0) && Minesweeper.inBounds(cells, i+k, j+l))
    									if(cells[i+k][j+l].isCovered())
    										cells[i+k][j+l].highlight();
    					}
    		drawingPad.repaint();
    		log.setText("Highlighted known mine(s)");
    	}
    	
    	private void revealPossible()
    	{
    		System.out.println(new Object(){}.getClass().getEnclosingMethod().getName());
    		// reveal all known non-mines: find cells that are numbers that have the same number of flags around them
    		for(int i = 0; i < cells.length; i++)
    			for(int j = 0; j < cells[0].length; j++)
    				if(cells[i][j].isUncovered())
    					if(cells[i][j].isNumber() && Cell.getFlaggedNeighbors(cells, i, j) == cells[i][j].getNeighboringMines())
    					{
    						for(int k = -1; k <= 1; k++)
    							for(int l = -1; l <= 1; l++)
    								if(!(k == 0 && l == 0) && Minesweeper.inBounds(cells, i+k, j+l))
    									if(!cells[i+k][j+l].isFlagged())
    										cells[i+k][j+l].highlight();
    					}
    		log.setText("Revealed known free spaces");
    		drawingPad.repaint();
    	}
    	
		private void flagAll()
		{
			System.out.println(new Object(){}.getClass().getEnclosingMethod().getName());
			for(Cell[] cc: cells)
				for(Cell c: cc)
					if(c.isHighlighted())
					{
						c.flag();
						c.unhighlight();
					}
			log.setText("Flagged known mines");
			drawingPad.repaint();
		}
		
		private void revealAll()
		{
			System.out.println(new Object(){}.getClass().getEnclosingMethod().getName());
			for(int i = 0; i < cells.length; i++)
				for(int j = 0; j < cells[i].length; j++)
					if(cells[i][j].isHighlighted())
					{
						if(cells[i][j].isCovered())
							evaluate(i, j);
						cells[i][j].unhighlight();
					}
			drawingPad.repaint();
		}
		
		private int getUnrevealedCells()
		{
			int count = 0;
			for(Cell[] cc: cells)
				for(Cell c: cc)
					if(c.isCovered())
						count++;
			return count;
		}
    }
}