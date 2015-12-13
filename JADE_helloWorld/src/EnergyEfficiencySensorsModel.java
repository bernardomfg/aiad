import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;
import sajas.wrapper.ContainerController;
import sajas.sim.repast3.Repast3Launcher;
import sun.management.resources.agent;
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
	private OpenSequenceGraph plot, plot2, plot3;
	private double energyLossPerTick;
	private ArrayList<Sensor> agentsList;
	private ArrayList<Water> watersList;
	private float newPolLvl;
	private int pollutionWidth;
	private ArrayList<Color> agentsColors;
	private ControlTower tower;
	private enum MyBoolean { Yes , No }
	private MyBoolean cosa, showEachAgentEnergyGraph, showEachAgentActivityGraph, showOverallGraph;

	public EnergyEfficiencySensorsModel() {
		this.numberOfAgents = 100;
		this.spaceSizeX = 300;
		this.spaceSizeY = 20;
		this.cosa = MyBoolean.No;
		this.showOverallGraph = MyBoolean.Yes;
		this.showEachAgentActivityGraph = MyBoolean.No;
		this.showEachAgentEnergyGraph = MyBoolean.No;
		this.energyLossPerTick = 0.1;
		this.agentsList = new ArrayList<Sensor>();
		this.watersList = new ArrayList<Water>();
		this.space = new Object2DTorus(spaceSizeX, spaceSizeY);
		this.newPolLvl = 0;
		this.pollutionWidth = 6;
		this.agentsColors  = new ArrayList<Color>();
		this.tower = new ControlTower();
	}

	@Override
	public void setup() {
		super.setup();

		//property descriptor
		Vector<MyBoolean> mb = new Vector<MyBoolean>();
		for(int i=0; i<MyBoolean.values().length; i++) {
			mb.add(MyBoolean.values()[i]);
		}

		descriptors.put("Cosa", new ListPropertyDescriptor("MyBoolean", mb));
		descriptors.put("ShowOverallGraph", new ListPropertyDescriptor("MyBoolean", mb));
		descriptors.put("ShowEachAgentActivityGraph", new ListPropertyDescriptor("MyBoolean", mb));
		descriptors.put("ShowEachAgentEnergyGraph", new ListPropertyDescriptor("MyBoolean", mb));
	}

	@Override
	public void begin(){
		super.begin();
		buildDisplay();
		buildSchedule();
	}

	@Override
	public String[] getInitParam() {
		return new String[] { "numberOfAgents", "energyLossPerTick",  "cosa", "pollutionWidth", "showEachAgentEnergyGraph" , "showEachAgentActivityGraph", "showOverallGraph" } ;
	}

	@Override
	public String getName() {
		return "";
	}

	public void buildDisplay() {
		Object2DDisplay display_water = new Object2DDisplay(space);
		display_water.setObjectList(this.watersList);
		dsurf.addDisplayableProbeable(display_water, "Water");

		Object2DDisplay display_sensors = new Object2DDisplay(space);
		display_sensors.setObjectList(this.agentsList);
		dsurf.addDisplayableProbeable(display_sensors, "Sensors");

		dsurf.display();

		//graphs
		makeGraphs();
	}

	private void makeGraphs() {
		if(this.showOverallGraph == MyBoolean.Yes) {
			//graph 1
			if (plot != null) plot.dispose();
			plot = new OpenSequenceGraph("Energy Evolution and Active Agents Curves", this);
			plot.setAxisTitles("ticks", "percentage");
			plot.setYRange(0, 100);
			//Total energy of the agents
			plot.addSequence("All Agents' Energy (percentage)", new Sequence() {
				public double getSValue() {
					return getAgentsEnergy();
				}
			}, Color.RED);
			//Number of Active Agents (in percentage)
			plot.addSequence("Active Agents (percentage)", new Sequence() {
				public double getSValue() {
					return getAgentsActive();
				}
			}, Color.BLUE);
			plot.display();
		}

		//graph2
		if(plot2 != null) plot2.dispose();
		plot2 = new OpenSequenceGraph("Energy Per Agent", this);
		plot2.setAxisTitles("ticks", "percentage");
		plot2.setYRange(0, 100);
		//Total energy of the agents
		for (final Sensor agent :agentsList) {
			plot2.addSequence(agent.getName(),  new Sequence(){
				public double getSValue() {
					return agent.getEnergy();
				}
			}, getColorNotUsed());
		}
		if(this.showEachAgentEnergyGraph == MyBoolean.Yes)
			plot2.display();

		if(this.showEachAgentActivityGraph == MyBoolean.Yes) {
			//graph3
			if (plot3 != null) plot3.dispose();
			plot3 = new OpenSequenceGraph("Activity Per Agent", this);
			plot3.setAxisTitles("ticks", "percentage");
			plot3.setYRange(0, 1);
			//Total energy of the agents
			for (int i = 0; i < agentsList.size(); i++) {
				final Sensor agent = agentsList.get(i);
				plot3.addSequence(agent.getName(), new Sequence() {
					public double getSValue() {
						if (agent.isActive())
							return 1;
						else
							return 0;
					}
				}, this.agentsColors.get(i));
			}
			plot3.display();
		}
	}

	private Color getColorNotUsed() {
		Color color;
		do {
			float r = Random.uniform.nextFloatFromTo(0, 1);
			float g = Random.uniform.nextFloatFromTo(0, 1);
			float b = Random.uniform.nextFloatFromTo(0, 1);
			color = new Color(r, g, b);
		}while (colorExists(color));

		agentsColors.add(color);
		return color;
	}

	private boolean colorExists(Color color) {
		for (Color c : this.agentsColors) {
			if(c == color)
				return true;
		}

		return false;
	}

	public double getAgentsEnergy() {
		double agentsEnergy = 0;
		double totalEnergy = numberOfAgents * 100;

		for (Sensor anAgentList : agentsList) {
			agentsEnergy += anAgentList.getEnergy();
		}

		return (agentsEnergy * 100)/totalEnergy;
	}

	private double getAgentsActive() {
		double agentsActive = 0;

		for (Sensor anAgentList : agentsList) {
			if(anAgentList.isActive())
				agentsActive ++;
		}

		return (agentsActive * 100)/numberOfAgents;
	}

	private void buildSchedule() {
		this.schedule = getSchedule();
		schedule.scheduleActionAtInterval(1, dsurf, "updateDisplay", Schedule.LAST);
		if(this.showOverallGraph == MyBoolean.Yes)
			schedule.scheduleActionAtInterval(1, plot, "step", Schedule.LAST);
		if(this.showEachAgentEnergyGraph == MyBoolean.Yes)
			schedule.scheduleActionAtInterval(1, plot2, "step", Schedule.LAST);
		if(this.showEachAgentActivityGraph == MyBoolean.Yes)
			schedule.scheduleActionAtInterval(1, plot3, "step", Schedule.LAST);

		getSchedule().scheduleActionBeginning(1, this, "updateWaterPollution");
	}

	public void updateWaterPollution() {
		/* New pulse of pollution */
		if(waterNotPolluted()) {
			newPollutionPulse();
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

		if(x+this.pollutionWidth <= space.getSizeX()-1) {
			for (int y = 0; y < space.getSizeY(); y++) {
				((Water) space.getObjectAt(x+this.pollutionWidth, y)).setPollutionLvl(this.newPolLvl);
				((Water) space.getObjectAt(x+this.pollutionWidth, y)).updateWaterColor();
			}
		}
		for (int y = 0; y < space.getSizeY(); y++) {
			((Water) space.getObjectAt(x, y)).setPollutionLvl(0);
			((Water) space.getObjectAt(x, y)).updateWaterColor();
		}
	}

	private void newPollutionPulse() {
		this.newPolLvl = (float)Random.uniform.nextDoubleFromTo(5.0, 100.0);

		for(int x = 0; x <= this.pollutionWidth; x++) {
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

		//range X: +/- 50
		//range Y: +/- 4

		for( Sensor sensor: this.agentsList ){
			ArrayList<Sensor> sensorNeighs = new ArrayList<Sensor>();
			ArrayList<Water> waterNeighs = new ArrayList<Water>();

			int maxX = sensor.getX() +50, maxY = sensor.getY() +4, minX = sensor.getX() -50, minY = sensor.getY() -4;

			maxX = ( (maxX > space.getSizeX()) ? space.getSizeX() : maxX );
			maxY = ( (maxY > space.getSizeY()) ? space.getSizeY() : maxY );
			minX = ( (minX < 0) ? 0 : minX );
			minY = ( (minY < 0) ? 0 : minY );

			for (Sensor temp : this.agentsList) {
				if (temp.getDescription().equals(sensor.getDescription())) continue;
				if (temp.getX() >= minX && temp.getX() < maxX && temp.getY() >= minY && temp.getY() < maxY) {
					sensorNeighs.add(temp);
				}
			}

			for (Water temp : this.watersList) {
				if (temp.getX() >= minX && temp.getX() < maxX && temp.getY() >= minY && temp.getY() < maxY) {
					waterNeighs.add(temp);
				}
			}
			
			sensor.setSensorNeighbours(sensorNeighs);
			sensor.setWaterNeighbours(waterNeighs);
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
		mainContainer.acceptNewAgent(tower.getDescription(), tower).start();

		for (int i = 0; i < numberOfAgents; i++) {
			int x, y;
			do {
				x = Random.uniform.nextIntFromTo(0, space.getSizeX() - 1);
				y = Random.uniform.nextIntFromTo(0, space.getSizeY() - 1);
			} while (space.getObjectAt(x, y) != null);

			String name = "Sensor_" +i;
			Sensor agent;
			if(this.cosa == MyBoolean.Yes) {
				agent = new Sensor(x, y, name, energyLossPerTick, true, tower);
			}else {
				agent = new Sensor(x, y, name, energyLossPerTick, false, tower);
			}
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

	public MyBoolean getCosa() {
		return cosa;
	}

	public void setCosa(MyBoolean cosa) {
		this.cosa = cosa;
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

	public MyBoolean getShowEachAgentActivityGraph() {
		return showEachAgentActivityGraph;
	}

	public void setShowEachAgentActivityGraph(MyBoolean showEachAgentActivityGraph) {
		this.showEachAgentActivityGraph = showEachAgentActivityGraph;
	}

	public MyBoolean getShowEachAgentEnergyGraph() {
		return showEachAgentEnergyGraph;
	}

	public void setShowEachAgentEnergyGraph(MyBoolean showEachAgentEnergyGraph) {
		this.showEachAgentEnergyGraph = showEachAgentEnergyGraph;
	}

	public MyBoolean getShowOverallGraph() {
		return showOverallGraph;
	}

	public void setShowOverallGraph(MyBoolean showOverallGraph) {
		this.showOverallGraph = showOverallGraph;
	}

	public int getPollutionWidth() {
		return pollutionWidth;
	}

	public void setPollutionWidth(int pollutionWidth) {
		this.pollutionWidth = pollutionWidth;
	}
}
