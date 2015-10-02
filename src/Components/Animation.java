package Components;

import Display.Drawer;
import framework.Component;
import framework.GameObject;

public class Animation extends Component {

	private SpriteSheet spriteSheet;
	private int h,w,offsetX, offsetY;
	private int frameX = 0, frameY=0;
	private int currentFrame=0;
	private int frameSpeed; //number of game frames until change to next image in animation
	private int frameCount=0;
	/*
	 * width and height are display sizes
	 * offsetX and offsetY are offsets to append to the GameObject origin
	 */
	public Animation(SpriteSheet spriteSheet, int width, int height, int offsetX, int offsetY, int frameSpeed){
		this.spriteSheet = spriteSheet;
		h=height;
		w=width;
		this.offsetX=offsetX;
		this.offsetY=offsetY;
		this.frameSpeed=frameSpeed;
		System.out.println("X: " + spriteSheet.gridWidth() + "\nY: " + spriteSheet.gridHeight());
	}
	
	@Override
	public String getType() {
		return "Animation";
	}

	@Override
	public void init(GameObject obj) {
		
	}

	@Override
	public void update(GameObject obj) {
		frameCount++;
		
		//change to next image
		if(frameCount>=frameSpeed){
			System.out.println("here");
			frameCount=0;
			//decide image to move to in the grid
			//move right
			if(frameX+1 < spriteSheet.gridWidth())
				frameX++;
			
			//move down
			else if(frameX+1 >= spriteSheet.gridWidth()){
				//move down
				if(frameY+1 < spriteSheet.gridHeight()){
					frameY++;
				}
				//reset to 0,0
				else if(frameY+1 >= spriteSheet.gridHeight()){
					frameX=0;
					frameY=0;
				}
			}
		}
		
	}

	@Override
	public void draw(Drawer g, GameObject obj) {
		g.drawImage(spriteSheet.getFrame(frameX, frameY), obj.getPosition().intX()+ offsetX, obj.getPosition().intY() + offsetY, w, h);
	}

}
