package framework;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import saving.GameState;
import saving.Saving;
import saving.SavingI;
import Collision.CollisionManager;
import Collision.CollisionManagerI;
import levels.Level1;
import levels.Level2;
import debugging.Print;
import debugging.SwingPrint;
import display.Camera;
import display.CameraSimple;
import display.SwingWindow;
import display.Window;

public class Game {
	
	//all game objects currently being used in the game
	private static ArrayList<GameObject> objs  = new ArrayList<GameObject>();
	
	//buffers
	private static ArrayList<String> deleteBufferTag = new ArrayList<String>();
	private static ArrayList<Integer> deleteBufferIDs = new ArrayList<Integer>();
	private static ArrayList<GameObject> objsToAdd = new ArrayList<GameObject>();
	
	private static Window window;
	private static Input input;
	private static Print print;
	private static SavingI saving;
	private static Camera camera;
	private static boolean drawDebug=false;
	private static Level currentLevel;
	private static Level nextLevel;
	private static boolean changeLevel=false;
	private static boolean gameStarted=false;
	private static CollisionManagerI collisionManager;
	private static boolean load=false;
	
	
	//default constructor
	public Game(){}
	
	//sets the window type
	public static void setWindow(Window windowType){
		if(gameStarted){
			Game.print().log("Window cannot be set once game has started");
			return;
		}
		window=windowType;
	}
	
	//sets the input type
	public static void setInputType(Input inputType){
		if(gameStarted){
			Game.print().log("Input cannot be set once game has started");
			return;
		}
		input=inputType;
	}
	
	//sets the collision manager
	public static void setCollisionManager(CollisionManagerI cm){
		collisionManager = cm;
	}
	
	//sets the printing type
	public static void setPrint(Print printType){
		if(gameStarted){
			Game.print().log("Print cannot be set once game has started");
			return;
		}
		print = printType;
	}
	
	//add a GameObject to the game
	public static void addGameObject(GameObject object){
		objsToAdd.add(object);
	}
	
	//request change level
	public static void changeLevel(Level level){
		changeLevel=true;
		nextLevel=level;
	}
	
	//change the level
	private static void doChangeLevel(){
		if(changeLevel){
			changeLevel=false;
			loadLevel(nextLevel);
		}
	}
	
	//loads level given
	private static void loadLevel(Level level){
		//clear all the buffers
		deleteBufferTag.clear();
		deleteBufferIDs.clear();
		
		for(GameObject g : objsToAdd)
			g.interruptThreads();
		objsToAdd.clear();

		//remove all GameObjects that are not global
		for(int i=0; i<objs.size(); i++){
			if(objs.get(i).getIsGlobal()==false){
				objs.get(i).interruptThreads();
				objs.remove(i);
				i--;
			}
		}
		
		
		//initialise level
		currentLevel = level;
		level.init();
	}
	
	//returns the current level
	public static Level getCurrentLevel(){
		return currentLevel;
	}

	
	//checks to see if all attributes have been initialised correctly for the game
	//if not this method sets them to the defaults
	static void checkInit(){
		int w=854,h=480; //default window size
		
		if(window==null)
			window = new SwingWindow(0,0,w,h,false, "Framework");
		
		if(window.getPreferredHeight()<=0 || window.getPreferredWidth()<=0){
			window.setPreferredSize(w, h);
		}
		
		if(saving==null){
			saving = new Saving();
		}
		
		if(camera==null)
			camera = new CameraSimple(0,0);
		
		if(input==null)
			input = new SwingInput();
		
		if(print==null)
			print = new SwingPrint();
		
		if(collisionManager==null)
			collisionManager = new CollisionManager();
	}
	
	//start thread for game loop
	public static void start(Level level){
		currentLevel = level;
		gameStarted=true;
		gameLoop.start();
	}
	
	//thread for the main game loop
	private static Thread gameLoop = new Thread(){
		
		//time for start of frame
		private long startTime;

		public void run(){
			//check initialisation was done correctly and load the starting level
			checkInit();
			loadLevel(currentLevel);
			addObjs(); //add GameObject in buffer that were created when level is loaded
			
			//main loop
			boolean flag=true;
			while(flag){
				//start frame time
				startTime = System.currentTimeMillis();
				
				//add and delete Game Objects in buffer
				deleteGameObjects();
				addObjs();
				
				loadLatestState();
				
				//update all GameObjects
				for(GameObject g : objs)
					g.update();
				
				//update new positions of collision shapes
				//todo use observer design pattern to keep position updated
				for(GameObject g : objs)
					g.updateCollisionShapes();
				
				//detect any collisions
				collisionManager.detect(objs);
				
				//update camera once all GameObject positions are finalised
				camera.update();
				
				//draw Scene (level)
				window.drawScene();

				//change level if there is a request to change level
				doChangeLevel();
				
				//clear the input buffer
				input.clear();
				
				//check if there is a request to quit the game
				if(checkExitGame())
					flag=false;
				
				//sleep so there is a limit to the number of frames per second
				try {Thread.sleep(calculateSleepTime());} 
		    	catch (InterruptedException e) {e.printStackTrace();}
			}
			
			//exit game if ended loop
			System.exit(0);
		}
		
		

		//checks if the escape game command is true
		private boolean checkExitGame() {
			if(input.isKeyDown((char)KeyEvent.VK_ESCAPE))
				return true;
			return false;
		}

		//adds GameObjects in buffer to the level
		private void addObjs() {
			for(GameObject o : objsToAdd){
				boolean flag=true;
				
				//only allow one global GameObject with the same tag
				//check if a global object with the same tag exists
				if(o.getIsGlobal()){
					for(int i=0; i<objs.size() && flag; i++){
						if(objs.get(i).getIsGlobal() && objs.get(i).getTag().equalsIgnoreCase(o.getTag())){
							flag=false;
						}
					}
				}
				
				//add if this GameObject has a unique tag
				if(flag)
				objs.add(o);	
			}
			objsToAdd.clear();
		}

		//deletes GameObjects in level that are listed in the buffers
		private void deleteGameObjects() {
			boolean found;
			
			//delete game objects with matching id
			for(String s : deleteBufferTag){
				found=false;
				for(int g=0; g<objs.size() && !found; g++){
					if(objs.get(g).getTag().equalsIgnoreCase(s)){
						objs.remove(g);
						found=true;
					}
				}
				//if(found==false)
				//	Game.print().log("GameObject tag not found when deleting GameObject by Tag : " + s);
			}
			deleteBufferTag.clear();
			
			//delete game objects with matching id
			for(Integer id : deleteBufferIDs){
				found=false;
				for(int g=0; g<objs.size() && !found; g++){
					if(objs.get(g).getID()==id){
						objs.remove(g);
						found=true;
					}
				}
				//if(found==false)
				//	Game.print().log("GameObject ID not found when deleting GameObject by ID : " + id);
			}
			deleteBufferIDs.clear();
		}

		private long calculateSleepTime() {
			long executionTime= System.currentTimeMillis()-startTime;
			long sleepTime=(long) (Time.FRAME_TIME-executionTime);
			if(sleepTime<0) sleepTime=0;
			return sleepTime;
		}
	};
	
	//returns the first GameObject found with a matching tag
	public static GameObject getGameObjectByTag(String tag){
		for(GameObject g : objs)
			if(g.getTag().equalsIgnoreCase(tag))
				return g;
		
		print.log("Warning: no gameObject was found with tag \"" + tag+ "\"");
		return new GameObject("-1");
	}
	
	//returns the GameObject with a matching id
	public static GameObject getGameObjectById(int id){
		for(GameObject g: objs){
			if(g.getID()==id)
				return g;
		}
		print.log("Warning: no gameObject was found with id \"" + id+ "\"");
		return new GameObject("-1");
	}
	
	//returns a shallow copy of all the GameObjects
	public static ArrayList<GameObject> copyOfGameObjects(){
		return new ArrayList<GameObject>(objs);
	}
	
	//returns the input object
	public static Input getInput(){
		return input;
	}
	
	//returns the print object
	public static Print print(){
		return print;
	}

	//turn debug drawing on or off
	public static void enableDebugDraw(boolean isOn) {
		drawDebug=isOn;
	}

	//returns true if drawing debug
	public static boolean isDrawingDebug() {
		return drawDebug;
	}
	
	//add tags to buffer which will be deleted later
	public static void deleteObjByTag(String tag){
		deleteBufferTag.add(tag);
	}
	
	//returns the camera object
	public static Camera getCamera(){
		return camera;
	}
	
	//returns the window object
	public static Window getWindow(){
		return window;
	}
	
	//add tags to buffer which will be deleted later
	public static void deleteObjByID(int id){
		deleteBufferIDs.add(id);
	}
	
	//returns true if a GameObject exists with a matching tag
	public static boolean objExistsWithTag(String tag){
		for(GameObject obj : objs)
			if(obj.getTag().equalsIgnoreCase(tag))
				return true;
		
		
		return false;
	}

	public static void saveState(){
		saving.saveState(getGameState());
	}
	
	public static void load(){
		load=true;
	}
	
	public static void loadLatestState(){
		if(load==false)return;
		load=false;
		GameState state = saving.getLastState();
		
		//if no state is saved yet
		if(state==null) 
			return;
		
		objs.clear();
		for(GameObjectStateI objState : state.getGameObjStates()){
			objs.add(new GameObject(objState));
		}
		//objs = state.getGameObjects();
		currentLevel= state.getCurrentLevel();
	}
	
	private static GameState getGameState(){
		return new GameState(copyOfGameObjects(), currentLevel);
	}

}
