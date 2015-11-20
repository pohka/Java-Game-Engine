package levelloading;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import collision.CollisionShape;
import components.Animation;
import components.SpriteSheet;
import framework.Component;
import framework.Game;
import framework.GameObject;
import framework.Script;
import tiles.TileMap;

public class ObjConstructor {
	
	//pass in arraylist of string parameters
	public static void loadAddableObject(String[] args, String className, GameObject obj){

		try {
				Class<?> c = Class.forName(className);
				Object o = null;

					try {
						o=c.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {e.printStackTrace();}
					
					if(o instanceof Component){
						Component comp = (Component) o;
						comp.construct(args);
						obj.add(comp);
					}
					else if(o instanceof Script){
						Script script = (Script) o;
						script.construct(args);
						obj.add(script);
					}
					else if(o instanceof CollisionShape){
						CollisionShape cs = (CollisionShape) o;
						cs.construct(args);
						obj.add(cs);
					}
					
	        // production code should handle this exception more gracefully
		} catch (ClassNotFoundException x) {
		    x.printStackTrace();
		}
	}
	
	public static GameObject loadGameObject(String[] args, String className){
		try {
			Class<?> c = Class.forName(className);
			GameObject o = null;

				try {
					o= (GameObject) c.newInstance();
					o.construct(args);
				} catch (InstantiationException | IllegalAccessException e) {e.printStackTrace();}
				
				
				return o;
        // production code should handle this exception more gracefully
		} catch (ClassNotFoundException x) {
			x.printStackTrace();
		}
		return null;
	}
	
	/* returns a new instance of that class, returns null on error
	 * the class must have a default constructor
	 * uses Class.getName(); for accurate naming
	 */
	public static Object classNameToObject(String className){
		try {
			
			return Class.forName(className).newInstance();
			
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {e.printStackTrace();}
		return null;
	}
}