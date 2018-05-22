package com.kyperbox.objects;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

public class ShaderObject extends GameObject{
	
	private Array<ShaderProgram> shaders;
	
	public ShaderObject() {
		shaders = new Array<ShaderProgram>();
	}
	
	public void clearShaders() {
		shaders.clear();
	}
	
	public void addShader(ShaderProgram shader) {
		this.shaders.add(shader);
	}
	
	public Array<ShaderProgram> getShaders(){
		return shaders;
	}
	
	public int getShaderAmount() {
		return shaders.size;
	}
	
	public void addShader(ShaderProgram shader, int index) {
		shaders.insert(index, shader);
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		if(shaders.size > 0) {
			ShaderProgram prev = batch.getShader();
			
			for (int i = 0; i < shaders.size; i++) {
				batch.setShader(shaders.get(i));
				super.draw(batch, parentAlpha);
			}
			batch.setShader(prev);
		}else
			super.draw(batch, parentAlpha);
	}
}
