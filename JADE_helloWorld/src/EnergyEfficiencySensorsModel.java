import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.lang.acl.ACLMessage;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class EnergyEfficiencySensorsModel extends Repast3Launcher {
	private ContainerController mainContainer;
	private Schedule schedule;
	private int numberOfAgents, spaceSizeX, spaceSizeY;
	private Water water;
	private DisplaySurface dsurf;
	private Object2DTorus space;
	private OpenSequenceGraph plot;
	private double energyLossPerTick;
	private ArrayList<Sensor> agentList; // description ex: "Sensor1", "Sensor2"...

	private enum MyBoolean { Yes , No }
	private MyBoolean allowGroupsFormation, nearAgents;
	private ArrayList<Water> waterObjects;

	public EnergyEfficiencySensorsModel() {
		this.numberOfAgents = 100;
		this.spaceSizeX = 300;
		this.spaceSizeY = 20;
		this.allowGroupsFormation = MyBoolean.No;
		this.nearAgents = MyBoolean.No;
		this.energyLossPerTick = 0.5;
	}

	public double getAgentsEnergy() {
		double agentsEnergy = 0;

		for (Sensor anAgentList : agentList) {
			agentsEnergy += anAgentList.getEnergy();
		}

		return agentsEnergy;
	}

	@Override
	public void setup() {
		super.setup();

		//water = new Water(1, this);

		if (dsurf != null) dsurf.dispose();
		dsurf = new DisplaySurface(this, "River Display");
		registerDisplaySurface("River Display", dsurf);

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
		//display and surface
		if (dsurf != null) dsurf.dispose();
		dsurf = new DisplaySurface(this, "River Display");
		dsurf.setSize(spaceSizeX, spaceSizeY);
		registerDisplaySurface("River Display", dsurf);

		Object2DDisplay display = new Object2DDisplay(space);
		display.setObjectList(this.agentList);
		dsurf.addDisplayableProbeable(display, "Sensors");

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
	}

	private void insertWaterElements() {
		this.waterObjects = new ArrayList<Water>();

		for(int i = 0; i < spaceSizeX; i++) {
			for(int j = 0; j < spaceSizeY; j++) {
				Water water = new Water(i, j, 0, this);
				waterObjects.add(water);

				space.putObjectAt(i, j, water);
			}
		}
	}
	
	private int getActiveAgents() {
		int sum = 0;
		for (Sensor agent : agentList) {
			if (agent.isActive())
				sum++;
		}
		return sum;
	}

	protected void launchJADE() {
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);

		if (dsurf != null)
			dsurf.dispose();
		dsurf = new DisplaySurface(this, "River Display");
		dsurf.setSize(spaceSizeX, spaceSizeY);
		registerDisplaySurface("River Display", dsurf);

		try {
			launchAgents();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

		settingNeighbours();

	}

	private void settingNeighbours() {
		for( Sensor sensor: this.agentList ){
			Vector<Sensor> neighbours = space.getMooreNeighbors(sensor.getX(), sensor.getY(), false);
			sensor.setNeighbours(neighbours);
		}
	}

	private void launchAgents() throws StaleProxyException {
		this.agentList = new ArrayList<Sensor>();

		space = new Object2DTorus(spaceSizeX, spaceSizeY);
		for (int i = 0; i < numberOfAgents; i++) {
			int x, y;
			do {
				x = Random.uniform.nextIntFromTo(0, space.getSizeX() - 1);
				y = Random.uniform.nextIntFromTo(0, space.getSizeY() - 1);
			} while (space.getObjectAt(x, y) != null);

			Sensor agent = new Sensor(x, y, space, energyLossPerTick, water);

			space.putObjectAt(x, y, agent);
			agentList.add(agent);
			mainContainer.acceptNewAgent(agent.getDescription(), agent).start();
		}
		
		insertWaterElements();
	}


	/**
	 * Launching Repast3
	 * @param args
	 */
	public static void main(String[] args) {
		boolean BATCH_MODE = true;
		SimInit init = new SimInit();
		init.setNumRuns(1);   // works only in batch mode
		init.loadModel(new EnergyEfficiencySensorsModel(), null, BATCH_MODE);
	}

}
