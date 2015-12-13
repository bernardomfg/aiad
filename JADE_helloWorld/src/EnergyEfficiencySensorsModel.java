import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;
import sajas.wrapper.ContainerController;
import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.space.Object2DTorus;
import uchicago.src.sim.util.Random;

import java.awt.*;
import java.util.ArrayList;
import java.util.Vector;


public class EnergyEfficiencySensorsModel extends Repast3Launcher {
	private ContainerController mainContainer;
	private Schedule schedule;
	private int numberOfAgents, spaceSizeX, spaceSizeY;
	private DisplaySurface dsurf;
	private Object2DTorus space;
	private OpenSequenceGraph plot;
	private double energyLossPerTick;
	private ArrayList<Sensor> agentsList; // description ex: "Sensor1", "Sensor2"...
	private ArrayList<Water> watersList;
	private float newPolLvl;

	private enum MyBoolean { Yes , No }
	private MyBoolean allowGroupsFormation, nearAgents;

	public EnergyEfficiencySensorsModel() {
		this.numberOfAgents = 100;
		this.spaceSizeX = 300;
		this.spaceSizeY = 20;
		this.allowGroupsFormation = MyBoolean.No;
		this.nearAgents = MyBoolean.No;
		this.energyLossPerTick = 0.01;
		this.agentsList = new ArrayList<Sensor>();
		this.watersList = new ArrayList<Water>();
		this.space = new Object2DTorus(spaceSizeX, spaceSizeY);
		this.newPolLvl = 0;
	}

	public double getAgentsEnergy() {
		double agentsEnergy = 0;

		for (Sensor anAgentList : agentsList) {
			agentsEnergy += anAgentList.getEnergy();
		}

		return agentsEnergy;
	}

	@Override
	public void setup() {
		super.setup();

		//property descriptor
		Vector<MyBoolean> mb = new Vector<MyBoolean>();
		for(int i=0; i<MyBoolean.values().length; i++) {
			mb.add(MyBoolean.values()[i]);
		}

		descriptors.put("AllowGroupsFormation", new ListPropertyDescriptor("MyBoolean", mb));
		descriptors.put("NearAgents", new ListPropertyDescriptor("MyBoolean", mb));
	}

	@Override
	public void begin(){
		super.begin();
		buildDisplay();
		buildSchedule();
	}

	@Override
	public String[] getInitParam() {
		return new String[] { "numberOfAgents", "energyLossPerTick",  "allowGroupsFormation", "nearAgents", "spaceSizeX", "spaceSizeY" } ;
	}

	@Override
	public String getName() {
		return "Energy efficiency in a multi-agent system";
	}

	public void buildDisplay() {
		Object2DDisplay display_water = new Object2DDisplay(space);
		display_water.setObjectList(this.watersList);
		dsurf.addDisplayableProbeable(display_water, "Water");

		Object2DDisplay display_sensors = new Object2DDisplay(space);
		display_sensors.setObjectList(this.agentsList);
		dsurf.addDisplayableProbeable(display_sensors, "Sensors");

		dsurf.display();

		//graph
		if(plot != null) plot.dispose();
		plot = new OpenSequenceGraph("Energy Evolution Curves", this);
		plot.setAxisTitles("time", "energy");
		plot.setYRange(0, 100);
		//desenha a linha
		plot.addSequence("Total Agents' Energy",  new Sequence(){
			public double getSValue() {
				return getAgentsEnergy();
			}
		});
		plot.display();
	}

	private void buildSchedule() {
		this.schedule = getSchedule();
		schedule.scheduleActionAtInterval(1, dsurf, "updateDisplay", Schedule.LAST);
		schedule.scheduleActionAtInterval(1, plot, "step", Schedule.LAST);
		getSchedule().scheduleActionBeginning(1, this, "updateWaterPollution");
	}

	public void updateWaterPollution() {
		/* New pulse of pollution */
		if(waterNotPolluted()) {
			newPollutionPulse();
			System.out.println("New pulse of pollution");
		}

		/* pollution flow */
		int x=0;
		boolean polluted = false;
		while(!polluted) {
			if(((Water)space.getObjectAt(x, 1)).getPollutionLvl() != 0)
				polluted = true;
			else
				x++;
		}

		if(x+6 <= space.getSizeX()-1) {
			for (int y = 0; y < space.getSizeY(); y++) {
				((Water) space.getObjectAt(x+6, y)).setPollutionLvl(this.newPolLvl);
				((Water) space.getObjectAt(x+6, y)).updateWaterColor();
			}
		}
		for (int y = 0; y < space.getSizeY(); y++) {
			((Water) space.getObjectAt(x, y)).setPollutionLvl(0);
			((Water) space.getObjectAt(x, y)).updateWaterColor();
		}
	}

	private void newPollutionPulse() {
		this.newPolLvl = (float)Random.uniform.nextDoubleFromTo(5.0, 100.0);

		for(int x = 0; x < 7; x++) {
			for (int y = 0; y < space.getSizeY(); y++) {
				((Water) space.getObjectAt(x, y)).setPollutionLvl(newPolLvl);
				((Water) space.getObjectAt(x, y)).updateWaterColor();
			}
		}
	}

	private boolean waterNotPolluted() {
		for(int x = 0; x < space.getSizeX(); x++) {
			for(int y = 0; y < space.getSizeY(); y++) {
				if(((Water)space.getObjectAt(x, y)).getPollutionLvl() != 0)
					return false;
			}
		}

		return true;
	}

	protected void launchJADE() {
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);

		if (dsurf != null) dsurf.dispose();
		dsurf = new DisplaySurface(this, "River");
		registerDisplaySurface("River", dsurf);

		dsurf.setSize(spaceSizeX, spaceSizeY);

		try {
			launchAgents();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}

	private void settingNeighbours() {
		for( Sensor sensor: this.agentsList ){
			Vector<Sensor> neighbours = space.getMooreNeighbors(sensor.getX(), sensor.getY(), false);
			sensor.setNeighbours(neighbours);
		}
	}

	private void createWaterElements() {
		for(int x = 0; x < space.getSizeX(); x++) {
			for(int y = 0; y < space.getSizeY(); y++) {
				Water water = new Water(x, y, 0);
				space.putObjectAt(x, y, water);
				watersList.add(water);
			}
		}

		newPollutionPulse();
	}

	private void launchAgents() throws StaleProxyException {
		for (int i = 0; i < numberOfAgents; i++) {
			int x, y;
			do {
				x = Random.uniform.nextIntFromTo(0, space.getSizeX() - 1);
				y = Random.uniform.nextIntFromTo(0, space.getSizeY() - 1);
			} while (space.getObjectAt(x, y) != null);

			Sensor agent = new Sensor(x, y, "Sensor" +i, energyLossPerTick);
			agentsList.add(agent);

			mainContainer.acceptNewAgent(agent.getDescription(), agent).start();
			space.putObjectAt(x, y, agent);
		}

		createWaterElements();

		settingNeighbours();
	}

	public static void main(String[] args) {
		boolean BATCH_MODE = false;
		SimInit init = new SimInit();
		init.setNumRuns(1);   // works only in batch mode
		init.loadModel(new EnergyEfficiencySensorsModel(), null, BATCH_MODE);
	}

	public int getNumberOfAgents() {
		return numberOfAgents;
	}

	public void setNumberOfAgents(int numberOfAgents) {
		this.numberOfAgents = numberOfAgents;
	}

	public double getEnergyLossPerTick() {
		return energyLossPerTick;
	}

	public void setEnergyLossPerTick(double energyLossPerTick) {
		this.energyLossPerTick = energyLossPerTick;
	}

	public MyBoolean getAllowGroupsFormation() {
		return allowGroupsFormation;
	}

	public void setAllowGroupsFormation(MyBoolean allowGroupsFormation) {
		this.allowGroupsFormation = allowGroupsFormation;
	}

	public MyBoolean getNearAgents() {
		return nearAgents;
	}

	public void setNearAgents(MyBoolean nearAgents) {
		this.nearAgents = nearAgents;
	}

	public int getSpaceSizeY() {
		return spaceSizeY;
	}

	public void setSpaceSizeY(int spaceSizeY) {
		this.spaceSizeY = spaceSizeY;
	}

	public int getSpaceSizeX() {
		return spaceSizeX;
	}

	public void setSpaceSizeX(int spaceSizeX) {
		this.spaceSizeX = spaceSizeX;
	}

}
