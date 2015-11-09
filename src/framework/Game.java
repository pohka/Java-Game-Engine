package framework;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import misc.Time;
import misc.Timer;
import services.Camera;
import services.Input;
import threading.ComponentSplit;
import threading.Split;
import threading.ThreadList;

public class Game {
	
	//all game objects currently being used in the game
	private static ArrayList<GameObject> objs  = new ArrayList<GameObject>();
	
	//buffers
	private static ArrayList<String> deleteBufferTag = new ArrayList<String>();
	private static ArrayList<Integer> deleteBufferIDs = new ArrayList<Integer>();
	private static ArrayList<GameObject> objsToAdd = new ArrayList<GameObject>();
	
	//managers
	private static ServiceManagerI serMan;
	private static LevelManagerI levelManager;
	
	private static ThreadList compThreads;

	//time for start of frame, used to calculate sleep time
	private static long startTime;
	
	private static Timer timer=new Timer();
	private static Timer frameTimer = new Timer();
	
	
	//default constructor
	public Game(){
		
		//set threads for split
		Split compSplits []= new ComponentSplit[4];
		for(int i=0; i<compSplits.length; i++){
			compSplits[i] = new ComponentSplit();
		}
		compThreads=new ThreadList(compSplits);
		
		timer.setMicrosecs();
		timer.start();
		serMan = new ServiceManager();
		timer.stopAndPrint("ServiceManager time: ");
		timer.start();
		levelManager = new LevelManager();
		timer.stopAndPrint("LevelManager time: ");
	}
	
	//service and level manager constructor
	public Game(ServiceManagerI serviceManager, LevelManagerI levelMan){
		serMan = serviceManager;
		levelManager = levelMan;
	}
	
	//returns the service manager object
	public static ServiceManagerI getServices(){
		return serMan;
	}
	
	//add a GameObject to the game
	public static void addGameObject(GameObject object){
		objsToAdd.add(object);
	}
	
	//request change level
	public static void changeLevel(Level level){
		levelManager.queueChangeLevel(level);
	}

	//loads level given
	static void loadCurrentLevel(){
		//clear all the buffers
		deleteBufferTag.clear();
		deleteBufferIDs.clear();

		objsToAdd.clear();

		//remove all GameObjects that are not global
		for(int i=0; i<objs.size(); i++){
			if(objs.get(i).getIsGlobal()==false){
				objs.remove(i);
				i--;
			}
		}
		
		levelManager.getCurrentLevel().init();
	}
	
	//returns the current level
	public static Level getCurrentLevel(){
		return levelManager.getCurrentLevel();
	}

	//start thread for game loop
	public static void start(Level level){
		levelManager.setCurrentLevel(level);
		
		gameLoop.start();
	}
	
	//thread for the main game loop
	private static Thread gameLoop = new Thread(){
		
		public void run(){
			gameLoop.setPriority(Thread.MAX_PRIORITY);
			//check initialisation was done correctly and load the starting level
			timer.start();
			serMan.checkInit();
			timer.stopAndPrint("CheckInit time:");
			timer.clearLoggedTimes();
			loadCurrentLevel();
			addObjs(); //add GameObject in buffer that were created when level is loaded
			frameTimer.setMicrosecs();
			//main loop
			boolean flag=true;
			while(flag){
				//start frame time
				startTime = System.currentTimeMillis();
				frameTimer.start();
				
				//add and delete Game Objects in buffer
				deleteGameObjects();
				addObjs();
				
				//checks if loading state is required and loads if so
				levelManager.loadLatestState();
				
				//update all GameObjects
				for(GameObject g : objs)
					g.update();
				
				//update all components of all GameObjects
				
				//update components with multithreading
				compThreads.updateAndRunAll(objs);
				
				//single threaded method
				//for(GameObject g: objs)
				//	g.updateComp();
				
				
				
				//update new positions of collision shapes
				//as other GameObjects may have changed the current GameObjects position after the current GameObject updated
				for(GameObject g : objs)
					g.notifyCollisionShapes();
				
				//timer.start();
				//detect any collisions
				serMan.getCollisionManager().detect(objs);
				//timer.stopAndLog();
				
				//update camera once all GameObject positions are finalised
				serMan.getCamera().update();
				
				//draw Scene i.e. draw the level
				serMan.getWindow().drawScene();
				

				//change level if there is a request to change level
				levelManager.doChangeLevel();
				
				//clear the input buffer
				serMan.getInput().clear();
				
				//check if there is a request to quit the game
				if(checkExitGame())
					flag=false;
				
				frameTimer.stopAndLog();
				//sleep so there is a limit to the number of frames per second
				try {Thread.sleep(calculateSleepTime());} 
		    	catch (InterruptedException e) {e.printStackTrace();}
			}

			//exit game if ended loop
			System.exit(0);
		}

		//checks if the escape game command is true
		private boolean checkExitGame() {
			if(serMan.getInput().isKeyDown((char)KeyEvent.VK_ESCAPE))
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
		
		print("Warning: no gameObject was found with tag \"" + tag+ "\"");
		return new GameObject("-1");
	}
	
	//returns the GameObject with a matching id
	public static GameObject getGameObjectById(int id){
		for(GameObject g: objs){
			if(g.getID()==id)
				return g;
		}
		print("Warning: no gameObject was found with id \"" + id+ "\"");
		return new GameObject("-1");
	}
	
	public static void print(String msg) {
		serMan.getPrint().log(msg);
	}

	//returns a shallow copy of all the GameObjects
	public static ArrayList<GameObject> copyOfGameObjects(){
		return new ArrayList<GameObject>(objs);
	}
	
	
	
	//add tags to buffer which will be deleted later
	public static void deleteObjByTag(String tag){
		deleteBufferTag.add(tag);
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
	
	//short hand for loading latest saveed state
	public static void loadLatestSave(){
		levelManager.load();
	}

	//short hand for getting the input service
	//this encapsulates level manager
	public static Input getInput(){
		return serMan.getInput();
	}
	
	//short hand for getting the camera service
	public static Camera getCamera(){
		return serMan.getCamera();
	}

	//returns the time of when the execution started for the current frame
	public static long getFrameTime(){
		//return startTime;
		return frameTimer.calculateAvg()/1000;
	}
}
