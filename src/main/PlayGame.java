package main;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.List;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class PlayGame extends JFrame implements Runnable {
	private boolean isRunning;
	private Image dbImage;
	private Graphics dbg;
	
	private final int CARD_WIDTH = 208;
	private final int CARD_HEIGHT = 303;
	private ArrayList<Image> spades = new ArrayList<Image>();
	private ArrayList<Image> hearts = new ArrayList<Image>();
	private ArrayList<Image> diamonds = new ArrayList<Image>();
	private ArrayList<Image> clubs = new ArrayList<Image>();
	private ArrayList<ArrayList> deckOfCards = new ArrayList<ArrayList>();
	private MyRectangle tradeBox, playBox;
	private Image backOfCard;
	
	private boolean hoverCard1 = false;
	private boolean hoverCard2 = false;
	private boolean hoverCard3 = false;
	private boolean hoverCard4 = false;
	private boolean hoverCard5 = false;
	
	private boolean clickCard1 = false;
	private boolean clickCard2 = false;
	private boolean clickCard3 = false;
	private boolean clickCard4 = false;
	private boolean clickCard5 = false;
	private boolean hasTraded = false;
	private int numOfCardsToTrade = 0;
	
	private int mx, my;
	private boolean mouseClicked;
	private boolean playGameClicked = false;
	
	private MyRectangle[] hoverCards = new MyRectangle[5];
	private final int SCALE_X, SCALE_Y;
	private final int WIDTH, HEIGHT;
	
	private int[] availableCards = new int[52];
	private int numOfSpades = 12;
	private int numOfClubs = 12;
	private int numOfDiamonds = 12;
	private int numOfHearts = 12;
	private int winner = -1;
	
	private File wkdir = null;
	private boolean dealt = false;
	
	private ArrayList<String> playerOne = new ArrayList<String>();
	private ArrayList<String> playerTwo = new ArrayList<String>();

	public PlayGame(int width, int height, String title, int win_x, int win_y, int scale_x, int scale_y) {
		setSize(width, height);
		setTitle(title);
		setResizable(false);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation(win_x, win_y);
		addMouseMotionListener(new ML());
		addMouseListener(new ML());
		
		if(System.getProperty("os.name").toLowerCase().contains("linux")) 
			wkdir = new File(System.getProperty("user.home") + "/poker");
		else if(System.getProperty("os.name").toLowerCase().contains("windows")) 
			wkdir = new File(System.getProperty("user.home") + File.separator + "poker");
		else if(System.getProperty("os.name").toLowerCase().contains("mac os")) 
			wkdir = new File(System.getProperty("user.home") + File.separator + "poker");
		
		SCALE_X = scale_x;
		SCALE_Y = scale_y;
		WIDTH = width;
		HEIGHT = height - 50;
		
		init();
		
		isRunning = true;
	}
	
	public class ML extends MouseAdapter implements MouseMotionListener {
		public void mousePressed(MouseEvent e) {
			mouseClicked = true;
		}
		
		public void mouseReleased(MouseEvent e) {
			mouseClicked = false;
		}
		
		public void mouseMoved(MouseEvent e) {
			mx = e.getX();
			my = e.getY();
		}
	}
	
	public void init() {
		BufferedImageLoader loader = new BufferedImageLoader();
		BufferedImage ss = null;
		
		try {
			ss = loader.loadImage(wkdir.toString() + File.separator + "res" + File.separator + "spritesheet.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SpriteSheet spritesheet = new SpriteSheet(ss);
		
		for(int i=0; i < 13; i++) {
			spades.add(spritesheet.grabSprite(i * CARD_WIDTH, 606, CARD_WIDTH, CARD_HEIGHT));
			hearts.add(spritesheet.grabSprite(i * CARD_WIDTH,  909, CARD_WIDTH, CARD_HEIGHT));
			clubs.add(spritesheet.grabSprite(i * CARD_WIDTH, 0, CARD_WIDTH, CARD_HEIGHT));
			diamonds.add(spritesheet.grabSprite(i * CARD_WIDTH, 303, CARD_WIDTH, CARD_HEIGHT));
		}
		
		deckOfCards.add(hearts);
		deckOfCards.add(spades);
		deckOfCards.add(diamonds);
		deckOfCards.add(clubs);
		backOfCard = spritesheet.grabSprite(416, 1212, CARD_WIDTH, CARD_HEIGHT);
		
		for(int i=0; i < availableCards.length; i++) {
			availableCards[i] = i;
		}
		
		int offsetX = (int)((((CARD_WIDTH + 5) * 2.5) * SCALE_X) / 2);
		for(int i=0; i < hoverCards.length; i++) {
			hoverCards[i] = new MyRectangle(CARD_WIDTH * SCALE_X, 
					CARD_HEIGHT * SCALE_Y, (offsetX + (i * (CARD_WIDTH + 5))) * SCALE_X, (HEIGHT - CARD_HEIGHT)  * SCALE_Y);	
		}
		
		tradeBox = new MyRectangle(SCALE_X * 200, SCALE_Y * 100, (offsetX - 10) * SCALE_X, SCALE_Y * 383);
		playBox = new MyRectangle(SCALE_X * 200, SCALE_Y * 100, (offsetX * 2) * SCALE_X, SCALE_Y * 383);
	}	
	
	public ArrayList<String> dealHands(int numOfCards) {
		ArrayList<Character> sortedCards = new ArrayList<Character>(Arrays.asList('2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'A'));
				
		int suit = -1;
		int card = -1;
		ArrayList<String> currentHand = new ArrayList<String>();
		for(int i=0; i < numOfCards; i++) {
			
			do {
				suit = (int)(Math.random() * 4);
				card = (int)(Math.random() * 13);
				System.out.println(availableCards[(suit * 13) + card]);
			} while(availableCards[(suit * 13) + card] == -1);
			
			String suitString = "";
			String cardString = "";
			
			switch(suit) {
				case 0:
					suitString = "S";
					break;
				case 1:
					suitString = "H";
					break;
				case 2:
					suitString = "D";
					break;
				case 3:
					suitString = "C";
					break;
			}
			
			availableCards[(suit * 13) + card] = -1;
			
			switch(card) {
				case 0:
					cardString = "2" + suitString;
					break;
				case 1:
					cardString = "3" + suitString;
					break;
				case 2:
					cardString = "4" + suitString;
					break;
				case 3:
					cardString = "5" + suitString;
					break;
				case 4:
					cardString = "6" + suitString;
					break;
				case 5:
					cardString = "7" + suitString;
					break;
				case 6:
					cardString = "8" + suitString;
					break;
				case 7:
					cardString = "A" + suitString;
					break;
				case 8:
					cardString = "9" + suitString;
					break;
				case 9:
					cardString = "T" + suitString;
					break;
				case 10:
					cardString = "J" + suitString;
					break;
				case 11:
					cardString = "Q" + suitString;
					break;
				case 12:
					cardString = "K" + suitString;
					break;
			}
			currentHand.add(cardString);
			System.out.println(cardString);
		}
		
		int[] indexNumber = new int[numOfCards];
		for(int i=0; i < currentHand.size(); i++) {
			indexNumber[i] = sortedCards.indexOf(currentHand.get(i).charAt(0));
		}
		Arrays.sort(indexNumber);
		
		ArrayList<String> tmpArray = new ArrayList<String>(Arrays.asList("", "", "", "", ""));
		for(int i=0; i < indexNumber.length; i++) {
			for(int j=0; j < currentHand.size(); j++) {
				if(indexNumber[i] == sortedCards.indexOf(currentHand.get(j).charAt(0))) {
					tmpArray.set(i, currentHand.get(j));
					currentHand.remove(j);
					break;
				}
			}
		}
		System.out.println("Array Size" + tmpArray.get(0));
		return tmpArray;
	}
	
	public void move() {
		ArrayList<Character> sortedCards = new ArrayList<Character>(Arrays.asList('2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'A'));
		String winnerString = "";
		
		if(!dealt) {
			playerOne = dealHands(5);
			playerTwo = dealHands(5);
			dealt = true;
		}
		
		if(mx > tradeBox.x && mx < tradeBox.x + tradeBox.width && my > tradeBox.y && my < tradeBox.y + tradeBox.height && !hasTraded) {
			if(mouseClicked) {
				ArrayList<String> newCards = new ArrayList<String>();
				if(!clickCard1) {
					newCards.add(playerOne.get(0));
				}
				
				if(!clickCard2) {
					newCards.add(playerOne.get(1));
				}
				
				if(!clickCard3) {
					newCards.add(playerOne.get(2));
				}
				
				if(!clickCard4) {
					newCards.add(playerOne.get(3));
				}
				
				if(!clickCard5) {
					newCards.add(playerOne.get(4));
				}
				
				ArrayList<String> newCardsFromTrade = new ArrayList<String>();
				newCardsFromTrade = dealHands(numOfCardsToTrade);
				//playerOne.removeAll(playerOne);
				
				for(int i=0; i < newCardsFromTrade.size(); i++) {
					if(clickCard1) {
						playerOne.set(0, newCardsFromTrade.get(i));
						clickCard1 = false;
						continue;
					} else if(clickCard2) {
						playerOne.set(1, newCardsFromTrade.get(i));
						clickCard2 = false;
						continue;
					} else if(clickCard3) {
						playerOne.set(2, newCardsFromTrade.get(i));
						clickCard3 = false;
						continue;
					} else if(clickCard4) {
						playerOne.set(3, newCardsFromTrade.get(i));
						clickCard4 = false;
						continue;
					} else if(clickCard5) {
						playerOne.set(4, newCardsFromTrade.get(i));
						clickCard5 = false;
						continue;
					}
				}
				
				clickCard1 = false;
				clickCard2 = false;
				clickCard3 = false;
				clickCard4 = false;
				clickCard5 = false;
				mouseClicked = false;
				hasTraded = true;
			}
		}
		
		
		if(mx > playBox.x && mx < playBox.x + playBox.width && my > playBox.y && my < playBox.y + playBox.height) {
			if(mouseClicked) {
				playGameClicked = true;
				isRunning = false;
				String handOne = pokerHand(playerOne);
				String handTwo = pokerHand(playerTwo);
				String[] winnerArray = new String[2];
				
				System.out.println(this.playGame(handOne, handTwo)[0]);
				winnerArray = playGame(handOne, handTwo);
				if(winnerArray[0].equals("Player 1")) {
					winner = 0;
				} else if(winnerArray[0].equals("Player 2")) {
					winner = 1;
				} else if(winnerArray[0].equals("Tie")) {
					winnerString = tieBreaker(winnerArray, handOne, handTwo, sortedCards);
					if(winnerString.equals("Player 1")) {
						winner = 0;
					} else if(winnerString.equals("Player 2")) {
						winner = 1;
					} else {
						winner = 3;
					}
				} else {
					winner = 3;
				}
			}
		}
		
		
		
		if(hoverCard1 && hoverCards[0].y > (HEIGHT - hoverCards[0].height - 20)) {
			hoverCards[0].y -= 2;
		} else if (hoverCards[0].y < (HEIGHT - hoverCards[0].height - 20) || clickCard1){
			hoverCards[0].y = (HEIGHT - hoverCards[0].height- 20);
		} 
		
		if(!hoverCard1 && hoverCards[0].y < (HEIGHT - hoverCards[0].height)) {
			hoverCards[0].y += 2;
		} else if (hoverCards[0].y > (HEIGHT - hoverCards[0].height)) {
			hoverCards[0].y = (HEIGHT - hoverCards[0].height);
		}
		
		
		if(hoverCard2 && hoverCards[1].y > (HEIGHT - CARD_HEIGHT - 20)) {
			hoverCards[1].y -= 2;
		} else if (hoverCards[1].y < (HEIGHT - CARD_HEIGHT - 20) || clickCard2) {
			hoverCards[1].y = (HEIGHT - CARD_HEIGHT - 20);
		} 
		
		if(!hoverCard2 && hoverCards[1].y < (HEIGHT - CARD_HEIGHT)) {
			hoverCards[1].y += 2;
		} else if (hoverCards[0].y > (HEIGHT - CARD_HEIGHT)) {
			hoverCards[1].y = (HEIGHT - CARD_HEIGHT);
		}
		
		
		if(hoverCard3 && hoverCards[2].y > (HEIGHT - CARD_HEIGHT - 20)) {
			hoverCards[2].y -= 2;
		} else if (hoverCards[2].y < (HEIGHT - CARD_HEIGHT - 20) || clickCard3) {
			hoverCards[2].y = (HEIGHT - CARD_HEIGHT - 20);
		} 
		
		if(!hoverCard3 && hoverCards[2].y < (HEIGHT - CARD_HEIGHT)) {
			hoverCards[2].y += 2;
		} else if (hoverCards[0].y > (HEIGHT - CARD_HEIGHT)) {
			hoverCards[2].y = (HEIGHT - CARD_HEIGHT);
		}
		
		
		if(hoverCard4 && hoverCards[3].y > (HEIGHT - CARD_HEIGHT - 20)) {
			hoverCards[3].y -= 2;
		} else if (hoverCards[3].y < (HEIGHT - CARD_HEIGHT - 20) || clickCard4) {
			hoverCards[3].y = (HEIGHT - CARD_HEIGHT - 20);
		} 
		
		if(!hoverCard4 && hoverCards[3].y < (HEIGHT - CARD_HEIGHT)) {
			hoverCards[3].y += 2;
		} else if (hoverCards[0].y > (HEIGHT - CARD_HEIGHT)) {
			hoverCards[3].y = (HEIGHT - CARD_HEIGHT);
		}
		
		
		if(hoverCard5 && hoverCards[4].y > (HEIGHT - CARD_HEIGHT - 20)) {
			hoverCards[4].y -= 2;
		} else if (hoverCards[4].y < (HEIGHT - CARD_HEIGHT - 20) || clickCard5) {
			hoverCards[4].y = (HEIGHT - CARD_HEIGHT - 20);
		}
		
		if(!hoverCard5 && hoverCards[4].y < (HEIGHT - CARD_HEIGHT)) {
			hoverCards[4].y += 2;
		} else if (hoverCards[0].y > (HEIGHT - CARD_HEIGHT)) {
			hoverCards[4].y = (HEIGHT - CARD_HEIGHT);
		}

		
		
		
		if(mouseClicked && hoverCard1 && !clickCard1) {
			clickCard1 = true;
			mouseClicked = false;
			numOfCardsToTrade += 1;
		} else if(mouseClicked && clickCard1 && hoverCard1) {
			clickCard1 = false;
			mouseClicked = false;
			numOfCardsToTrade -= 1;
		}
		
		if(mouseClicked && hoverCard2 && !clickCard2) {
			clickCard2 = true;
			mouseClicked = false;
			numOfCardsToTrade += 1;
		} else if(mouseClicked && clickCard2 && hoverCard2) {
			clickCard2 = false;
			mouseClicked = false;
			numOfCardsToTrade -= 1;
		}
		
		if(mouseClicked && hoverCard3 && !clickCard3) {
			clickCard3 = true;
			mouseClicked = false;
			numOfCardsToTrade += 1;
		} else if(mouseClicked && clickCard3 && hoverCard3) {
			clickCard3 = false;
			mouseClicked = false;
			numOfCardsToTrade -= 1;
		}
		
		if(mouseClicked && hoverCard4 && !clickCard4) {
			clickCard4 = true;
			mouseClicked = false;
			numOfCardsToTrade += 1;
		} else if(mouseClicked && clickCard4 && hoverCard4) {
			clickCard4 = false;
			mouseClicked = false;
			numOfCardsToTrade -= 1;
		}
		
		if(mouseClicked && hoverCard5 && !clickCard5) {
			clickCard5 = true;
			mouseClicked = false;
			numOfCardsToTrade += 1;
		} else if(mouseClicked && clickCard5 && hoverCard5) {
			clickCard5 = false;
			mouseClicked = false;
			numOfCardsToTrade -= 1;
		}
		
		
		
		if(mx > hoverCards[0].x && mx < hoverCards[0].x + hoverCards[0].width && my > hoverCards[0].y && my < hoverCards[0].y + hoverCards[0].height) {
			hoverCard1 = true;
		}
		else 
			hoverCard1 = false;
		
		if(mx > hoverCards[1].x && mx < hoverCards[1].x + hoverCards[1].width && my > hoverCards[1].y && my < hoverCards[1].y + hoverCards[1].height) {
			hoverCard2 = true;
		}
		else 
			hoverCard2 = false;
		
		if(mx > hoverCards[2].x && mx < hoverCards[2].x + hoverCards[2].width && my > hoverCards[2].y && my < hoverCards[2].y + hoverCards[2].height) 
			hoverCard3 = true;
		else 
			hoverCard3 = false;
		
		if(mx > hoverCards[3].x && mx < hoverCards[3].x + hoverCards[3].width && my > hoverCards[3].y && my < hoverCards[3].y + hoverCards[3].height) 
			hoverCard4 = true;
		else 
			hoverCard4 = false;
		
		if(mx > hoverCards[4].x && mx < hoverCards[4].x + hoverCards[4].width && my > hoverCards[4].y && my < hoverCards[4].y + hoverCards[4].height) 
			hoverCard5 = true;
		else 
			hoverCard5 = false;
	}
	
	public static boolean sameSuit(ArrayList<String> cards) {
		String card = cards.get(0);
		char suit = card.charAt(1);
		
		for(int i=0; i < cards.size(); i++) {
			String curCard = cards.get(i);
			if(curCard.charAt(1) == suit) 
				continue;
			else 
				return false;
		}
		return true;
	}
	
	public String tieBreaker(String[] winner, String cards1, String cards2, ArrayList<Character> sortedCards) {
		int indexOne = -1;
		int indexTwo = -1;
		if(winner[1].equals("One Pair") || winner[1].equals("High Card") || winner[1].equals("Full House")) {
			indexOne = sortedCards.indexOf(cards1.charAt(cards1.length() - 1));
			indexTwo = sortedCards.indexOf(cards2.charAt(cards2.length() - 1));
		}
		if(indexOne > indexTwo) {
			return "Player 1";
		} else if(indexTwo > indexOne) {
			return "Player 2";
		} else {
			return "I give up";
		}
	}
	
	public static String highestValue(ArrayList<Character> cardValues, ArrayList<Character> sortedCards) {
		int highestValue = -1;
		int index;
		for(int i=0; i < cardValues.size(); i++) {
			char value = cardValues.get(i);
			index = sortedCards.indexOf(value);
			if(index > highestValue)
				highestValue = index;
		}
		return "High Card " + sortedCards.get(highestValue);
	}
	
	public String[] playGame(String handOne, String handTwo) {
		String[] winArray = new String[2];
		ArrayList<String> ranking = new ArrayList<String>(Arrays.asList("High Card", "One Pair", "Two Pair", "Three of a Kind", 
				"Straight", "Flush", "Full House", "Four of a Kind", "Straight Flush", "Royal Flush"
		));
		
		while(!ranking.contains(handOne)) {
			handOne = handOne.substring(0, handOne.length() - 2);
		}
		while(!ranking.contains(handTwo)) {
			handTwo = handTwo.substring(0, handTwo.length() - 2);
		}
		
		int playerOne = ranking.indexOf(handOne);
		int playerTwo = ranking.indexOf(handTwo);
		
		if(playerOne > playerTwo) {
			winArray[0] = "Player 1";
			winArray[1] = "0";
		} else if(playerTwo > playerOne) {
			winArray[0] = "Player 2";
			winArray[1] = "0";
		} else {
			winArray[0] = "Tie";
			winArray[1] = handOne;
		}
		
		return winArray;
	}
	
	public String pokerHand(ArrayList<String> cards) {
		ArrayList<Character> highestCards = new ArrayList<Character>(Arrays.asList('T', 'J', 'Q', 'K', 'A'));
		ArrayList<Character> sortedCards = new ArrayList<Character>(Arrays.asList('2', '3', '4', '5', '6', '7', '8', '9', 'A', 'J', 'K', 'Q', 'T'));
		ArrayList<Character> sortedCardsRight = new ArrayList<Character>(Arrays.asList('2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'A'));
		
		/*---------------------------------------------------
		 * Royal Flush
		 * This determines whether the play is a royal flush
		   -------------------------------------------------- */
		boolean royalFlush = true;
		for(int i=0; i < cards.size(); i++) {
			String card = cards.get(i);
			if(highestCards.contains(card.charAt(0))) {
				continue;
			} else {
				royalFlush = false;
				break;
			}
		}
		
		if(royalFlush) {
			if(!this.sameSuit(cards))
				royalFlush = false;
		}
		
		if(royalFlush) {
			return "Royal Flush";
		}
		
		/*------------------------------------------------------------
		 * Straight Flush and Straight
		 * This determines whether or not the hand is a straight flush
		 * or a straight.
		   ----------------------------------------------------------- */
		boolean straightFlush = true;
		int start = sortedCards.indexOf(cards.get(0).charAt(0));
		int i = 0;
		for(int j=0; j < cards.size(); j++) {
			String card = cards.get(j);
			if (card.charAt(0) == sortedCards.get(i + start)) {
				i += 1;
				continue;
			} else {
				straightFlush = false;
				break;
			}
		}
		
		if(straightFlush) {
			if(this.sameSuit(cards))
				return "Straight Flush " + cards.get(0).charAt(0);
			else
				return "Straight " + cards.get(0).charAt(0);
		}
		
		/*---------------------------------------------------
		 * Four and three of a kind, and one or two pairs
		 * Checks for all of the above
		  ---------------------------------------------------*/
		ArrayList<Character> cardValues = new ArrayList<Character>();
		boolean onePair, twoPair, threeOfKind;
		onePair = false;
		twoPair = false;
		threeOfKind = false;
		
		int pairValueOne, pairValueTwo, threeKindValue;
		pairValueOne = 0;
		pairValueTwo = 0;
		threeKindValue = 0;
		for(int j=0; j < cards.size(); j++) {
			String card = cards.get(j);
			cardValues.add(card.charAt(0));
		}
		
		for(int j=0; j < cardValues.size(); j++) {
			char value = cardValues.get(j);
			int count = Collections.frequency(cardValues, value);
			if(count >= 4)
				return "Four of a Kind " + value;
			if(count == 3) {
				threeOfKind = true;
				threeKindValue = value;
			}
			if(count == 2) {
				onePair = true;
				pairValueOne = (char)(value);
			}
		}
		
		if(threeOfKind) {
			for(int j=0; j < cardValues.size(); j++) {
				char value = cardValues.get(j);
				int count = Collections.frequency(cardValues, value);
				if(count == 2) {
					return "Full House " + value + " " + (char)(threeKindValue);
				}
			}
			return "Three of a Kind " + (char)(threeKindValue);
		}
		
		if(onePair) {
			for(int j=0; j < cardValues.size(); j++) {
				char value = cardValues.get(j);
				if(value == pairValueOne) {
					cardValues.remove(cardValues.indexOf(value));
				}
			}
			for(int j=0; j < cardValues.size(); j++) {
				char value = cardValues.get(j);
				int count = Collections.frequency(cardValues, value);
				if(count == 2) {
					onePair = false;
					twoPair = true;
					pairValueTwo = value;
					break;
				}
			}
		}
		
		if(twoPair) 
			return "Two Pair " + (char)(pairValueOne) + " " + (char)(pairValueTwo);
		if(onePair)
			return "One Pair " + (char)(pairValueOne);
		
		
		/*-------------------------------------------------------------------
		 * Highest Card
		 * returns the highest card if none of the others have the desired
		 * output.
		  -------------------------------------------------------------------*/
		return this.highestValue(cardValues, sortedCardsRight);
	}
	
	public void paint(Graphics g) {
		dbImage = createImage(getWidth(), getHeight());
		dbg = dbImage.getGraphics();
		paintComponent(dbg);
		g.drawImage(dbImage, 0, 0, this);
	}
	
	public void paintComponent(Graphics g) {
		Color feltGreen = new Color(39, 119, 20);
		g.setColor(feltGreen);
		g.fillRect(0, 0, getWidth(), getHeight());
		int offsetX = (int)((((CARD_WIDTH + 5) * 2.5) * SCALE_X) / 2);
		
		try {
			
			for(int i=0; i < playerOne.size(); i++) {
				int array = -1;
				int cardNum = -1;
				String currentCard = playerOne.get(i);
				char suit = currentCard.charAt(1);
				char num = currentCard.charAt(0);
				
				switch(suit) {
					case 'S':
						array = 1;
						break;
					case 'D':
						array = 2;
						break;
					case 'H':
						array = 0;
						break;
					case 'C':
						array = 3;
						break;
				}
				
				switch(num) {
					case '2':
						cardNum = 0;
						break;
					case '3':
						cardNum = 1;
						break;
					case '4':
						cardNum = 2;
							break;
					case '5':
						cardNum = 3;
							break;
					case '6':
						cardNum = 4;
						break;
					case '7':
						cardNum = 5;
						break;
					case '8':
						cardNum = 6;
						break;
					case '9':
						cardNum = 7;
						break;
					case 'T':
						cardNum = 8;
						break;
					case 'J':
						cardNum = 9;
						break;
					case 'Q':
						cardNum = 10;
						break;
					case 'K':
						cardNum = 11;
						break;
					case 'A':
						cardNum = 12;
						break;
				}
				ArrayList<Image> suits = deckOfCards.get(array);
				g.drawImage(suits.get(cardNum), (offsetX + (i * (CARD_WIDTH + 5))) * SCALE_X, hoverCards[i].y, CARD_WIDTH * SCALE_X, 
							CARD_HEIGHT* SCALE_Y, null);
			}
			
			
			for(int i=0; i < playerTwo.size(); i++) {
				int array = -1;
				int cardNum = -1;
				String currentCard = playerTwo.get(i);
				char suit = currentCard.charAt(1);
				char num = currentCard.charAt(0);
				
				switch(suit) {
					case 'S':
						array = 1;
						break;
					case 'D':
						array = 2;
						break;
					case 'H':
						array = 0;
						break;
					case 'C':
						array = 3;
						break;
				}
				
				switch(num) {
					case '2':
						cardNum = 0;
						break;
					case '3':
						cardNum = 1;
						break;
					case '4':
						cardNum = 2;
							break;
					case '5':
						cardNum = 3;
							break;
					case '6':
						cardNum = 4;
						break;
					case '7':
						cardNum = 5;
						break;
					case '8':
						cardNum = 6;
						break;
					case '9':
						cardNum = 7;
						break;
					case 'T':
						cardNum = 8;
						break;
					case 'J':
						cardNum = 9;
						break;
					case 'Q':
						cardNum = 10;
						break;
					case 'K':
						cardNum = 11;
						break;
					case 'A':
						cardNum = 12;
						break;
				}
				ArrayList<Image> suits = deckOfCards.get(array);
				
				if(playGameClicked) {
					g.drawImage(suits.get(cardNum), (offsetX + (i * (CARD_WIDTH + 5))) * SCALE_X, 50 * SCALE_X, CARD_WIDTH * SCALE_X, 
							CARD_HEIGHT* SCALE_Y, null);
				} else {
					g.drawImage(backOfCard, (offsetX + (i * (CARD_WIDTH + 5))) * SCALE_X, 50 * SCALE_X, CARD_WIDTH * SCALE_X, 
							CARD_HEIGHT* SCALE_Y, null);
				}
			}
			
			g.setColor(Color.black);
			g.fillRect(tradeBox.x, tradeBox.y, tradeBox.width, tradeBox.height);
			g.fillRect(playBox.x, playBox.y, playBox.width, playBox.height);
			
			g.setColor(Color.white);
			g.setFont(new Font("Times New Roman", 30, 50));
			g.drawString("Trade", SCALE_X * (offsetX + 10), SCALE_Y * 450); // +250
			g.drawString("Play", playBox.x + 20, SCALE_Y * 450);
			
			if(winner == 0) {
				g.drawString("You Win!", (offsetX + 600) * SCALE_X, SCALE_Y * 450);
			} else if(winner == 1) {
				g.drawString("Computer wins!", (offsetX + 600) * SCALE_X, SCALE_Y * 450);
			} else if(winner == 2) {
				g.drawString("Tie", (offsetX + 600) * SCALE_X, SCALE_Y * 450);
			} else if(winner == 3){
				g.drawString("Who knows who won!", (offsetX + 600) * SCALE_X, SCALE_Y * 450);
			}
			
		} catch(Exception e) {
		}
		repaint();
	}
	
	public void drawString(Graphics g, String s, int x, int y, int width)
	{
	    // FontMetrics gives us information about the width,
	    // height, etc. of the current Graphics object's Font.
	    FontMetrics fm = g.getFontMetrics();

	    int lineHeight = fm.getHeight();

	    int curX = x;
	    int curY = y;

	    String[] words = s.split(" ");

	    for (String word : words)
	    {
	        // Find out the width of the word.
	        int wordWidth = fm.stringWidth(word + " ");

	        // If text exceeds the width, then move to next line.
	        if (curX + wordWidth >= x + width)
	        {
	            curY += lineHeight;
	            curX = x;
	        }

	        g.drawString(word, curX, curY);

	        // Move over to the right for next word.
	        curX += wordWidth;
	    }
	}
	
	public void run() {
		try {
			while(isRunning) {
				move();	
				Thread.sleep(30);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
