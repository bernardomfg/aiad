import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import java.awt.*;

/**
 * Created by mario on 05/12/2015.
 */
public class Water implements Drawable{

    float pollutionLvl;
    int x, y;
    private Color color;


    Water(int x, int y, float polLvl) {
        this.x = x;
        this.y = y;
        this.color = Color.blue;
        this.pollutionLvl = polLvl;
    }

    public float getPollutionLvl() {
        return pollutionLvl;
    }

    public void setPollutionLvl(float pollutionLvl) {
        this.pollutionLvl = pollutionLvl;
    }

    @Override
    public void draw(SimGraphics g) {
        g.drawFastRect(this.color);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }
}
