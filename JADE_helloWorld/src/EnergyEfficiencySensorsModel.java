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
import uchicago.src.sim.space.Object2DGrid;
import uchicago.src.sim.space.Object2DTorus;
import uchicago.src.sim.util.Random;

import java.util.ArrayList;
import java.util.Vector;

public class EnergyEfficiencySensorsModel extends Repast3Launcher {
	private ContainerController mainContainer;
	private Schedule schedule;
	private int numberOfAgents, spaceSizeX, spaceSizeY;
	private ArrayList<Object> objectList;
	private DisplaySurface dsurf;
	private Object2DGrid space;
	private OpenSequenceGraph plot;
	private double energyLossPerTick;
	private String t1AgentName = null;
	
	private enum MyBoolean { Yes , No }
	private MyBoolean allowGroupsFormation, nearAgents;

	public EnergyEfficiencySensorsModel() {
		this.numberOfAgents = 20;
		this.spaceSizeX = 300;
		this.spaceSizeY = 20;
		this.allowGroupsFormation = MyBoolean.No;
		this.nearAgents = MyBoolean.No;
		this.energyLossPerTick = 0.01;
	}

	public double getAgentsEnergy() {
		double agentsEnergy = 0;

		for (Object obj : objectList) {
			if(obj.getClass().equals("Sensor")) {
				agentsEnergy += (Sensor) obj.getEnergy();
			}
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

	private void insertWaterElements() {
		this.waterObjects = new ArrayList<Water>();

		for(int i = 0; i < spaceSizeX; i++) {
			for(int j = 0; j < spaceSizeY; j++) {
				Water water = new Water(i, j, 0);
				waterObjects.add(water);

				space.putObjectAt(i, j, water);
			}
		}
	}

	private void buildSchedule() {
		this.schedule = getSchedule();
		schedule.scheduleActionAtInterval(1, dsurf, "updateDisplay", Schedule.LAST);
		schedule.scheduleActionAtInterval(1, plot, "step", Schedule.LAST);
	}

	private int getActiveAgents() {
		int sum = 0;
		for (Sensor anAgentList : agentList) {
			if (anAgentList.isActive())
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
	}

	public void communicationTest() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("Hello dear agent.");

		t1AgentName = "agent1";

		try {
			//System.out.println(EnergyEfficiencySensorsModel.class.getProtectionDomain().getCodeSource().getLocation().getPath()+);
			//t1 = mainContainer.createNewAgent(t1AgentName, "sensor.Sensor", new Object[] {1,2,3,4});
			space = new Object2DTorus(spaceSizeX, spaceSizeY);
			Sensor tum = new Sensor(10,10,10, space, energyLossPerTick);
			mainContainer.acceptNewAgent(t1AgentName, tum);
			tum.start();

		} catch (Exception any) {
			any.printStackTrace();
		}

		msg.addReceiver(new sajas.core.AID(t1AgentName, sajas.core.AID.ISLOCALNAME));

		agentList.get(0).send(msg);
	}

	private void launchAgents() throws StaleProxyException {
		agentList = new ArrayList<Sensor>();
		space = new Object2DTorus(spaceSizeX, spaceSizeY);
		for (int i = 0; i < numberOfAgents; i++) {
			int x, y;
			do {
				x = Random.uniform.nextIntFromTo(0, space.getSizeX() - 1);
				y = Random.uniform.nextIntFromTo(0, space.getSizeY() - 1);
			} while (space.getObjectAt(x, y) != null);

			Sensor agent = new Sensor(i, x, y, space, energyLossPerTick);
			space.putObjectAt(x, y, agent);
			agentList.add(agent);
			mainContainer.acceptNewAgent("Sensor "+i, agent).start();
		}

		insertWaterElements();
	}

	
	/**
	 * Launching Repast3
	 * @param args
	 */
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

	public MyBoolean getNearAgents() {
		return nearAgents;
	}

	public void setNearAgents(MyBoolean nearAgents) {
		this.nearAgents = nearAgents;
	}

	public MyBoolean getAllowGroupsFormation() {
		return allowGroupsFormation;
	}

	public void setAllowGroupsFormation(MyBoolean allowGroupsFormation) {
		this.allowGroupsFormation = allowGroupsFormation;
	}

	public double getEnergyLossPerTick() {
		return energyLossPerTick;
	}

	public void setEnergyLossPerTick(double energyPerTick) {
		this.energyLossPerTick = energyPerTick;
	}

	public int getSpaceSizeX() {
		return spaceSizeX;
	}

	public void setSpaceSizeX(int spaceSizeX) {
		this.spaceSizeX = spaceSizeX;
	}

	public int getSpaceSizeY() {
		return spaceSizeY;
	}

	public void setSpaceSizeY(int spaceSizeY) {
		this.spaceSizeY = spaceSizeY;
	}
}
