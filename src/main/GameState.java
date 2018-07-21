package main;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;

public class GameState  {
	
	private int currentState;
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private int sheight = screenSize.height;
	private int swidth = screenSize.width;
	private final int SCREEN_WIDTH = (swidth / 10) * 8;
	private final int SCREEN_HEIGHT = (sheight / 10) * 8;
	private final int WIN_X = (swidth / 2) - (SCREEN_WIDTH / 2);
	private final int WIN_Y = (sheight / 2) - ((SCREEN_HEIGHT / 2) + 50);
	private final int SCALE_X = (SCREEN_WIDTH / 1536);
	private final int SCALE_Y = (SCREEN_HEIGHT / 864);
	private final String TITLE = "Poker";
	
	ArrayList<Integer> states = new ArrayList<Integer>();
	
	public void setState(int newState) {
		currentState = newState;
		
		if(currentState == 0) {
			MainMenu menu = new MainMenu(SCREEN_WIDTH, SCREEN_HEIGHT, TITLE, WIN_X, WIN_Y, SCALE_X, SCALE_Y);
			Thread t1 = new Thread(menu);
			t1.start();
		} else if (currentState == 1) {
			PlayGame game = new PlayGame(SCREEN_WIDTH, SCREEN_HEIGHT, TITLE, WIN_X, WIN_Y, SCALE_X, SCALE_Y);
			Thread t2 = new Thread(game);
			t2.start();
		} 
	}
	
	public int getState() {
		return currentState;
	}
}
