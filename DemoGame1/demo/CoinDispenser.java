package demo;

import Collision.CollisionBox;
import scripts.MoveScript;
import components.RigidBody;
import components.Sprite;
import framework.Game;
import framework.GameObject;
import framework.MathG;
import framework.Time;
import framework.Vector;

public class CoinDispenser extends GameObject {
	
	private long lastSpawn=0;
	private long delay = 1400;

	public CoinDispenser(int x, int y){
		super("CoinDispenser");
		super.moveTo(new Vector(x,y));
		
	}
	
	@Override
	public void update(){
		super.update();
		if(Time.getTime()-delay>lastSpawn){
			lastSpawn=Time.getTime();
			spawn();
		}
	}

	private void spawn() {
		Coin coin = new Coin("coin");
		coin.moveBy(super.getPosition());
		Game.addGameObject(coin);
	}

}