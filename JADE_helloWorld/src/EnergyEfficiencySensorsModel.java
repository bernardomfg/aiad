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

import java.util.ArrayList;
import java.util.Vector;

public class EnergyEfficiencySensorsModel extends Repast3Launcher {
	private ContainerController mainContainer;
	private Schedule schedule;
	private int numberOfAgents, spaceSize;
	private ArrayList<ArrayList<Object>> ambient;
	private ArrayList<Sensor> agentList;
	private DisplaySurface dsurf;
	private Object2DTorus space;
	private OpenSequenceGraph plot;
	private OpenSequenceGraph plot2;
	private double energyLossPerTick;

	private enum MyBoolean { Yes , No }
	private MyBoolean allowGroupsFormation, nearAgents;

	public EnergyEfficiencySensorsModel() {
		this.numberOfAgents = 100;
		this.spaceSize = 100;
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
		return new String[] { "numberOfAgents", "energyLossPerTick",  "allowGroupsFormation", "nearAgents", "spaceSize" } ;
	}

	@Override
	public String getName() {
		return "Energy efficiency in a multi-agent system";
	}

	public void buildDisplay() {
		//display and surface
		Object2DDisplay display = new Object2DDisplay(space);
		display.setObjectList(this.agentList);
		dsurf.addDisplayableProbeable(display, "River");
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
		registerDisplaySurface("River Display", dsurf);

		try {
			launchAgents();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	private void launchAgents() throws StaleProxyException {
		agentList = new ArrayList<Sensor>();
		space = new Object2DTorus(spaceSize, spaceSize);
		for (int i = 0; i < numberOfAgents; i++) {
			int x, y;
			do {
				x = Random.uniform.nextIntFromTo(0, space.getSizeX() - 1);
				y = Random.uniform.nextIntFromTo(0, space.getSizeY() - 1);
			} while (space.getObjectAt(x, y) != null);

			Sensor agent = new Sensor(i, x, y, space, energyLossPerTick);
			System.out.println("Sensor "+i+" - Coords("+x+", "+y+")");
			space.putObjectAt(x, y, agent);
			agentList.add(agent);
			mainContainer.acceptNewAgent("Sensor "+i, agent).start();
		}
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

	public int getSpaceSize() {
		return spaceSize;
	}

	public void setSpaceSize(int spaceSize) {
		this.spaceSize = spaceSize;
	}
}
