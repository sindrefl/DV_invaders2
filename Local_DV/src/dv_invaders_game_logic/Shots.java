package dv_invaders_game_logic;

import java.awt.Graphics;
import java.util.LinkedList;

import dv_invaders.Game;

public class Shots {

	LinkedList<Shot> shots;
	private Game game;
	
	
	public Shots(Game game){
		this.game = game;
		shots = new LinkedList<Shot>();
	}
	
	public void addShot(Shot shot){
		shots.add(shot);
	}
	
	public void removeShot(int index){
	shots.remove(index);
	}
	
	public void tick(){
		for (int i = 0; i < shots.size(); i++) {
			shots.get(i).tick();
		}
	}
	public void render(Graphics g){
		for (int i = 0; i < shots.size(); i++) {
			shots.get(i).render(g);
		}
	}
	
	public boolean hasHit(){
		for (int i = 0; i < shots.size(); i++) {
			if (game.objects.hasBeenShot(shots.get(i))){
				removeShot(i);
				return true;
			}
		}
		return false;
	}
}