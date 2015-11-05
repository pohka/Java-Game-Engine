package framework;

import saving.LoadingState;
import saving.LoadingStateI;
import saving.Saving;
import saving.SavingI;
import services.Camera;
import services.CameraSimple;
import services.CollisionManager;
import services.CollisionManagerI;
import services.Input;
import services.Print;
import services.SwingInput;
import services.SwingPrint;
import services.SwingWindow;
import services.Window;

public class ServiceManager implements ServiceManagerI {
	
	//services
	private static Window window;
	private static Input input;
	private static Print print;
	private static SavingI saving;
	private static LoadingStateI loading;
	private static Camera camera;
	private static CollisionManagerI collisionManager;
	
	//flags
	private static boolean drawDebug;
	private static boolean finalised=false;
	
	public ServiceManager(){}
	
	//sets the window 
	@Override
	public void setWindow(Window windowType){
		if(checkFinalised("Window"))
			return;

		window=windowType;
	}
	
	//sets the input 
	@Override
	public void setInputType(Input inputType){
		if(checkFinalised("Input"))
			return;

		input=inputType;
	}
		
	//sets the collision manager
	@Override
	public void setCollisionManager(CollisionManagerI cm){
		collisionManager = cm;
	}
	
	//sets the printing type
	@Override
	public void setPrint(Print printType){
		if(checkFinalised("Print"))
			return;

		print = printType;
	}
	
	//set saving
	@Override
	public void setSaving(SavingI savingI){
		 if(checkFinalised("Saving"))
			return;

		saving=savingI;
	}
	
	//set state loader
	@Override
	public void setLoading(LoadingStateI stateLoader){
		if(checkFinalised("Loading"))
			return;

		loading = stateLoader;
	}
	
	//returns finalised boolean and prints message if true
	private boolean checkFinalised(String name){
		if(finalised){
			Game.print(name + " service cannot be set once game has started");
		}
		return finalised;
	}
	
	//checks to see if all attributes have been initialised correctly for the game
	//if not this method sets them to the defaults
	@Override
	public void checkInit(){
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
		
		finalised=true;
	}
	
	//returns the input object
	@Override
	public Input getInput(){
		return input;
	}

	//returns the print object
	@Override
	public Print getPrint(){
		return print;
	}
	
	//returns the camera object
	@Override
	public Camera getCamera(){
		return camera;
	}

	//returns the window object
	@Override
	public Window getWindow(){
		return window;
	}
	
	//returns the collision manager object
	@Override
	public CollisionManagerI getCollisionManager(){
		return collisionManager;
	}

	//returns the loading object
	@Override
	public LoadingStateI getLoading() {
		return loading;
	}
	
	//returns saving object
	@Override
	public SavingI getSaving() {
		return saving;
	}
	
	//turn debug drawing on or off
	@Override
	public void enableDebugDraw(boolean isOn) {
		drawDebug=isOn;
	}
	
	//returns true if drawing debug
	@Override
	public boolean isDrawingDebug() {
		return drawDebug;
	}
}
