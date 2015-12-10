import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import java.awt.*;

public class Water implements Drawable{

    private float pollutionLvl;
    private Color color;
    private int x, y;
    private EnergyEfficiencySensorsModel model;
    
    Water(int x, int y, float polLvl, EnergyEfficiencySensorsModel model) {
        this.x = x;
        this.y = y;
    	this.pollutionLvl = polLvl;
        this.model = model;
    }

    public float getPollutionLvl(int x, int y) {
    	double currentTick = model.getTickCount();
    	
    	//inventar a formula para a poluiçao aqui (tem de usar o x, y e currentTick)
    	
        return pollutionLvl;
    }

    @Override
    public void draw(SimGraphics g) {
        g.drawFastRect(this.color);
    }

	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getY() {
		// TODO Auto-generated method stub
		return 0;
	}

}
