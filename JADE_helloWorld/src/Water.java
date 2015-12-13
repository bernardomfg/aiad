import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import java.awt.*;

public class Water implements Drawable{

    private float pollutionLvl;
    private Color color;
    private int x, y;
    
    Water(int x, int y, float polLvl) {
        this.x = x;
        this.y = y;
    	this.pollutionLvl = polLvl;
        this.color = Color.decode("#00bbff");
    }

    public void updateWaterColor()
    {
        if (pollutionLvl > 80)
            setColor(Color.decode("#7c7a7c"));
        else if (pollutionLvl <= 80 && pollutionLvl > 30)
            setColor(Color.decode("#afacaf"));
        else if(pollutionLvl <= 30 && pollutionLvl > 0)
            setColor(Color.decode("#ceccce"));
        else
            setColor(Color.decode("#00bbff"));
    }

    @Override
    public void draw(SimGraphics g) {
        g.drawRect(this.color);
    }

	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return this.x;
	}

	@Override
	public int getY() {
		// TODO Auto-generated method stub
		return this.y;
	}

    public float getPollutionLvl() {
        return pollutionLvl;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setPollutionLvl(float pollutionLvl) {
        this.pollutionLvl = pollutionLvl;
    }
}
