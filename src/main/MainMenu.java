package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
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
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class MainMenu extends JFrame implements Runnable {
	private Image dbImage;
	private Graphics dbg;
	private boolean isRunning;
	
	public MainMenu(int width, int height, String title, int win_x, int win_y, int scale_x, int scale_y) {

		
		init();
	}
	
	public class ML extends MouseAdapter implements MouseMotionListener {

	}
	
	private void init() {
		
	}

	
	public void move() {

	}
	
	public void paint(Graphics g) {
		dbImage = createImage(getWidth(), getHeight());
		dbg = dbImage.getGraphics();
		paintComponent(dbg);
		g.drawImage(dbImage, 0, 0, this);
	}
	
	public void paintComponent(Graphics g) {
		
		repaint();
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
