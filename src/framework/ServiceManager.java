package framework;

import saving.LoadingState;
import saving.LoadingStateI;
import saving.Saving;
import saving.SavingI;
import Collision.CollisionManager;
import Collision.CollisionManagerI;
import debugging.Print;
import debugging.SwingPrint;
import display.Camera;
import display.CameraSimple;
import display.SwingWindow;
import display.Window;

public class ServiceManager implements ServiceManagerI {
	
	//variables to be set
	private static Window window;
	private static Input input;
	private static Print print;
	private static SavingI saving;
	private static LoadingStateI loading;
	private static Camera camera;
	private static CollisionManagerI collisionManager;
	
	
	private static boolean gameStarted=false;
	
	public ServiceManager(){}
	
	//sets the window type
	public static void setWindow(Window windowType){
		if(gameStarted){
			Game.print("Window cannot be set once game has started");
			return;
		}
		window=windowType;
	}
	
	//sets the input type
	public static void setInputType(Input inputType){
		if(gameStarted){
			Game.print("Input cannot be set once game has started");
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
			Game.print("Print cannot be set once game has started");
			return;
		}
		print = printType;
	}
	
	//set state loader
	public static void setLoading(LoadingStateI stateLoader){
		if(gameStarted){
			Game.print("Loading cannot be set once game has started");
			return;
		}
		loading = stateLoader;
	}
	
	//checks to see if all attributes have been initialised correctly for the game
	//if not this method sets them to the defaults
	public static void checkInit(){
		int w=854,h=480; //default window size
		
		if(window==null)
			window = new SwingWindow(0,0,w,h,false, "Framework");
		
		if(window.getPreferredHeight()<=0 || window.getPreferredWidth()<=0){
			window.setPreferredSize(w, h);
		}
			
		if(saving==null)
			saving = new Saving();
		
		if(loading==null)
			loading = new LoadingState();
			
		if(camera==null)
			camera = new CameraSimple(0,0);
			
		if(input==null)
			input = new SwingInput();
			
		if(print==null)
			print = new SwingPrint();
			
		if(collisionManager==null)
			collisionManager = new CollisionManager();
	}
	
	//returns the input object
	public static Input getInput(){
		return input;
	}

	//returns the print object
	public static Print getPrint(){
		return print;
	}
	
	//returns the camera object
	public static Camera getCamera(){
		return camera;
	}

	//returns the window object
	public static Window getWindow(){
		return window;
	}
	
	public static CollisionManagerI getCollisionManager(){
		return collisionManager;
	}

	public static LoadingStateI getLoading() {
		return loading;
	}
	
	public static SavingI getSaving() {
		return saving;
	}
}
