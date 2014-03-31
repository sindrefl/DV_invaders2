package dv_invaders;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import dv_invaders_game_logic.BonusPoints;
import dv_invaders_game_logic.ExtraLife;
import dv_invaders_game_logic.Objects;
import dv_invaders_game_logic.Obstacle;
import dv_invaders_game_logic.Player;
import dv_invaders_game_logic.Powerup;
import dv_invaders_game_logic.Reverse;
import dv_invaders_game_logic.Shield;
import dv_invaders_game_logic.Shooter;
import dv_invaders_game_logic.Shot;
import dv_invaders_game_logic.Shots;
import dv_invaders_game_logic.Slower;
import dv_invaders_game_logic.SpeedUp;

@SuppressWarnings("serial")
public class Game extends Canvas implements Runnable {

	public static final int WIDTH = 400;
	public static final int HEIGHT = 600;
	public static final int SCALE = 1;
	public static final String TITLE = "Dragvoll Invaders";
	private int score = 0;
	private int bonus = 0;
	private int life = 3;


	private Thread thread;
	private boolean running = false;

	private BufferedImage image = new BufferedImage(WIDTH*SCALE, HEIGHT*SCALE, BufferedImage.TYPE_INT_RGB );
	public BufferedImage playerSprite;
	public BufferedImage obstacleSprite;
	LevelMap map = new LevelMap(this, 1);

	Player player = new Player(this);
	public Objects objects = new Objects(this);
	public SpriteSheet ss;
	private Powerup powerup;
	private int hasPowerup;
	// No powerup - 0
	// Shooter - 1
	// Shield - 2
	// Slower - 3
	// Reverse - 4
	// Bonuspoints - 5
	// Speedup - 6
	// Extra Life - 7

	private boolean isPowerup = false; // true if powerup onscreen
	Random random =  new Random();

	private Shots shots = new Shots(this);



	private Menu menu = new Menu();

	private enum STATE{
		MENU,
		GAME,
		DEATH
	};

	private STATE state = STATE.MENU;



	private synchronized void start() {
		if (running) {
			return;
		}
		running = true;
		thread = new Thread(this);
		thread.start();

	}

	private synchronized void stop() {
		if (!running) {
			return;
		}
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(1);
	}

	private void init() {
		requestFocus();
		start();

		BufferedImageLoader loader = new BufferedImageLoader();
		try {
			ss = new SpriteSheet(loader.loadImage("/dv_spritesheet.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		addKeyListener(new KeyInput(this));
		playerSprite = ss.grabImage(1, 1, 32, 32);
		obstacleSprite = ss.grabImage(2, 1, 32, 32);

		hasPowerup = 0;


	}

	public void run() {
		init();
		System.out.println("running");

		// Makes sure that game updates 60 tics a secound! (Sett deg inn i denne
		// koden ordentlig);
		long lastTime = System.nanoTime();
		final double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		int frames = 0;
		int updates = 0;
		long timer = System.currentTimeMillis();
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			if (delta >= 1) {
				tick();
				updates++;
				delta--;
			}

			render();
			frames++;

			if ((System.currentTimeMillis() - timer) > 1000) {
				timer += 1000;
				System.out.println(updates + " Ticks, FPS " + frames + " Score: " + score + " Bonus: " + bonus);
				updates = 0;
				frames = 0;
			}

			// End of tick logic;

			// Implement game logic:


			//Gamelogic ends here;
		}
		stop();
	}

	public void render() {

		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null){
			createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image,0 ,0 ,this);

		//		if(state == STATE.MENU){
		//			menu.render(g);
		//		}
		//		else{
		map.render(g);
		player.render(g, playerSprite);
		if (isPowerup){
			powerup.render(g);
		}

		shots.render(g);
		objects.render(g);
		//		}

		g.dispose();
		bs.show();

	}

	int i = 0;
	int j = 0;
	public void tick(){
		map.tick();
		player.tick();
		objects.tick();
		score ++;
		if (j < 400){
			j++;
			if (j % 50 == 0){
				if (objects.getSize() < 7)
					objects.addObstacles();
			}
		}

		if (score % 500 == 0){
			//skal settes til nextInt(maxtemp) + 1;
						int temp = random.nextInt(5) + 1;
						if (temp == 1){
			powerup = new Shooter(this, player);	
						}
						else if (temp == 2){
							powerup = new Shield(this, player);
						}
						else if (temp == 3){
							powerup = new Slower(this, player);
						}
						else if	(temp == 4){
							powerup = new Reverse(this, player);
						}
						else if (temp == 5){
							powerup = new BonusPoints(this, player);
						}
						else if (temp == 6){
							powerup = new SpeedUp(this, player);
						}
						else if (temp == 7){
							powerup = new ExtraLife(this, player);
						}
			isPowerup = powerup.onScreen();
		}	
		if (isPowerup){
			powerup.tick();
			if (!(powerup.onScreen())){
				isPowerup = false;
			}
		}

		if ((hasPowerup!=0)){
			i++;
		}


		if (i == 300){
			hasPowerup = 0;
			i = 0;
			System.out.println("END POWERUP!!!");

			//			playerSprite = ss.grabImage(1, 1, 32, 32);
			deActivatePowerup(powerup);
		}

		shots.tick();
		shots.hasHit();
	}
	//Boss her: if score == ettellerannet;

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();

		if(key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A){
			if (hasPowerup == 4){
				player.setVelX(5);
			}
			else player.setVelX(-5);

		}

		else if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT){
			if (hasPowerup == 4){
				player.setVelX(-5);
			}
			else player.setVelX(5);

		}
		else if(key == KeyEvent.VK_W || key == KeyEvent.VK_UP){
			if (hasPowerup == 4){
				player.setVelY(5);
			}
			else player.setVelY(-5);

		}

		else if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN){
			if (hasPowerup == 4){
				player.setVelY(-5);
			}
			else player.setVelY(5);

		}

		else if (key == KeyEvent.VK_SPACE){
			if (hasPowerup == 1){
				Shooter shooter = (Shooter) powerup;
				shots.addShot(shooter.shoot());
			}
		}
	}

	public void keyReleased(KeyEvent e) {

		int key = e.getKeyCode();

		if(key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A){
			player.setVelX(0);

		}

		else if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT){
			player.setVelX(0);

		} 
		else if(key == KeyEvent.VK_UP || key == KeyEvent.VK_W){
			player.setVelY(0);

		}

		else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S){
			player.setVelY(0);

		} 

	}
	public void isFinished(){
		if (life > 1){
			life--;
			System.out.println("YOU LOSE A LIFE!");
			objects.removeAll();
			j = 0;
			bonus-=100;
			hasPowerup=0;
			i=0;
			return;
		}
		running = false;
		//Legg inn mulighet til retry:
		JButton button = new JButton("Click here to try again!");
		JFrame frame = new JFrame();
		button.setBounds(100, 300, 200, 100);
		button.setBackground(new Color(34));
		button.setVisible(true);
	}

	public void activatePowerup(Powerup pp){
		bonus +=100;
		//		playerSprite = pp.changeSprite();
		hasPowerup = pp.activate();	
		isPowerup = false;
		if (hasPowerup == 5){
			bonus += 400;
			hasPowerup=0;
		}
		else if (hasPowerup == 7){
			life++;
			System.out.println("1UP!");
			hasPowerup = 0;
		}
	}

	public void deActivatePowerup(Powerup pp){
		pp.deactivate();
	}

	public void PowerupOut(){
		this.isPowerup = false;
	}

	public SpriteSheet getSpriteSheet(){
		return ss;
	}

	public Player getPlayer() {
		return player;
	}

	public int getScore() {
		return score;
	}

	public int getHasPowerup() {
		return hasPowerup;
	}

	public void resetJ() {
		this.j = 0;
	}

	public static void main(String[] args) {
		Game game = new Game();

		game.setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		game.setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		game.setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));

		@SuppressWarnings("static-access")
		JFrame frame = new JFrame(game.TITLE);
		JPanel panel = new JPanel();
		panel.setSize(WIDTH*SCALE, HEIGHT*SCALE);
		panel.add(game);
		frame.setContentPane(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		game.start();
	}
}