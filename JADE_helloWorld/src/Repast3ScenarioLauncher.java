import jade.core.Profile;
import jade.core.ProfileImpl;
import sajas.core.Runtime;
import sajas.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import sajas.sim.repast3.Repast3Launcher;

import uchicago.src.sim.engine.SimInit;

public class Repast3ScenarioLauncher extends Repast3Launcher {
	private ContainerController mainContainer;
	
	@Override
	public String[] getInitParam() {
		return new String[0];
	}

	@Override
	public String getName() {
		return "Scenario -- SAJaS Repast3 Test";
	}

	protected void launchJADE() {
		
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);
		
		launchAgents();
	}
	
	private void launchAgents() {

		try {
			mainContainer.createNewAgent("Agent", "agents.Leader", null).start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

	}

	
	/**
	 * Launching Repast3
	 * @param args
	 */
	public static void main(String[] args) {
		boolean BATCH_MODE = true;
		SimInit init = new SimInit();
		init.setNumRuns(1);   // works only in batch mode
		init.loadModel(new Repast3ScenarioLauncher(), null, BATCH_MODE);
	}

}
