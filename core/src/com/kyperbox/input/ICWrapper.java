package com.kyperbox.input;

import com.badlogic.gdx.controllers.Controller;

/**
 * class used to abstract the controller
 * 
 * @author john
 *
 */
public interface ICWrapper {
	
	
	public boolean hasTOriggerAxis();
	
	public void update();
	
	public void remove();
	
	public Controller getController();

	/**
	 * check if the controller is connected
	 * 
	 * @return
	 */
	public boolean isConnected();

	/**
	 * check the value of the right trigger
	 * 
	 * @return
	 */
	public float getRightTrigger();

	/**
	 * check the value of the left trigger
	 * 
	 * @return
	 */
	public float getLeftTrigger();

	/**
	 * get the x-axis value of the right analog stick
	 * 
	 * @return -1 to 1
	 */
	public float getRightX();

	/**
	 * get the y-axis value of the right analog stick
	 * 
	 * @return -1 to 1
	 */
	public float getRightY();

	/**
	 * get the x-axis value of the right analog stick
	 * 
	 * @return -1 to 1
	 */
	public float getLeftX();

	/**
	 * get the y-axis value of the right analog stick
	 * 
	 * @return -1 to 1
	 */
	public float getLeftY();

	/**
	 * check if the `A` button is pressed
	 * 
	 * @return
	 */
	public boolean buttonAPressed();

	/**
	 * check if the `B` button is pressed
	 * 
	 * @return
	 */
	public boolean buttonBPressed();

	/**
	 * check if the `X` button is pressed - note this is based on xbox configuration
	 * 
	 * @return
	 */
	public boolean buttonXPressed();

	/**
	 * check if the `Y` button is pressed - note this is based on an xbox
	 * configuration
	 * 
	 * @return
	 */
	public boolean buttonYPressed();
	
	public boolean leftPressed();
	
	public boolean rightPressed();
	
	public boolean upPressed();
	
	public boolean downPressed();

	public boolean L1Pressed();

	public boolean R1Pressed();

	public boolean L3Pressed();

	public boolean R3Pressed();
	
	public boolean selectPressed();
	
	public boolean startPressed();

	/**
	 * check if the `A` button was just pressed
	 * 
	 * @return
	 */
	public boolean buttonAJustPressed();

	/**
	 * check if the `B` button was just pressed
	 * 
	 * @return
	 */
	public boolean buttonBJustPressed();

	/**
	 * check if the `X` button was just pressed - note this is based on an xbox
	 * configuration
	 * 
	 * @return
	 */
	public boolean buttonXJustPressed();

	/**
	 * check if the `Y` button was just pressed = note this is based on an xbox
	 * configuration
	 * 
	 * @return
	 */
	public boolean buttonYJustPressed();
	
	public boolean leftJustPressed();
	
	public boolean rightJustPressed();
	
	public boolean upJustPressed();
	
	public boolean downJustPressed();

	public boolean L1JustPressed();

	public boolean R1JustPressed();

	public boolean L3JustPressed();

	public boolean R3JustPressed();
	
	public boolean selectJustPressed();
	
	public boolean startJustPressed();
	


}
