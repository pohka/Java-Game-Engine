package services;

import java.util.ArrayList;

import framework.GameObject;

public interface CollisionManagerI {
	//detect collisions
	public void detect(ArrayList<GameObject> objs);
}