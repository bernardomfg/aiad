import jade.core.Profile;
import jade.core.ProfileImpl;
import sajas.core.Runtime;
import sajas.wrapper.ContainerController;

import sajas.sim.repast3.Repast3Launcher;

import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.space.Object2DTorus;
import uchicago.src.sim.util.Random;

import java.util.ArrayList;

public class EnergyEfficiencySensorsModel extends Repast3Launcher {
	private ContainerController mainContainer;
	private int numberOfAgents, spaceSize;
	private ArrayList<Sensor> agentList;
	private DisplaySurface dsurf;
	private Object2DTorus space;
	private OpenSequenceGraph plot;

	public EnergyEfficiencySensorsModel() {
		this.numberOfAgents = 100;
		this.spaceSize = 100;
	}

	@Override
	public String[] getInitParam() {
		return new String[] { "numberOfAgents" } ;
	}

	@Override
	public String getName() {
		return "Energy efficiency in a multi-agent system";
	}

	protected void launchJADE() {
		
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);

		if (dsurf != null)
			dsurf.dispose();
		dsurf = new DisplaySurface(this, "River Display");
		registerDisplaySurface("River Display", dsurf);

		launchAgents();
	}
	
	private void launchAgents() {
		agentList = new ArrayList<>();
		space = new Object2DTorus(spaceSize, spaceSize);
		for (int i = 0; i < numberOfAgents; i++) {
			int x, y;
			do {
				x = Random.uniform.nextIntFromTo(0, space.getSizeX() - 1);
				y = Random.uniform.nextIntFromTo(0, space.getSizeY() - 1);
			} while (space.getObjectAt(x, y) != null);

			Sensor agent = new Sensor(i, x, y, space);
			space.putObjectAt(x, y, agent);
			agentList.add(agent);
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
}
