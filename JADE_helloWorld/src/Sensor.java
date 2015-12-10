

import sajas.core.AID;
import sajas.core.behaviours.CyclicBehaviour;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DTorus;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.awt.*;
import java.util.Vector;

public class Sensor extends sajas.core.Agent implements Drawable {
	private static int id = 0;
	private int personal_id;
	private int group_id;
	private String message;
	private float energy; //percent value
	private int x, y;
	private boolean isLeader;
	private double energyLossPerTick;
	private Vector<Sensor> neighbours = null;
	private Water water;
	private String description;
	private Color color;

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	private boolean isActive;
	private Object2DTorus space;

	public Sensor(int x, int y, Object2DTorus space, double energyLossPerTick, Water water) {
		this.personal_id = ++id;
		this.x = x;
		this.y = y;
		this.space = space;
		this.isLeader = true;
		this.energy = 100;
		this.color = Color.green;
		this.energyLossPerTick = energyLossPerTick;
		isActive = false;
		this.water = water;
		this.description = "Sensor" + personal_id;
	}

	protected void setup() {
		System.out.println("Sensor " + personal_id + " started.");

		this.addBehaviour( new CyclicBehaviour(){

			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				if(isActive) {

					if(energy - energyLossPerTick >= 0)
						energy -= energyLossPerTick;
					else
						energy = 0;
					System.out.println("   Sensor "+personal_id+ " energy: " + energy);

					//sampleEnvironment();
					float polution = water.getPollutionLvl(x, y);

					//tomar decisoes acerca da rede

					//inform neighbours
					for(Sensor sensor: neighbours){

						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.setContent("inform " + polution);

						msg.addReceiver(new sajas.core.AID(sensor.getDescription(), sajas.core.AID.ISLOCALNAME));

						this.myAgent.send(msg);

					}

				}
			}
		});

		//receiver behaviour
		this.addBehaviour( new CyclicBehaviour(){

			private static final long serialVersionUID = 2L;

			@Override
			public void action() {
				if(isActive) {

					//tratamento de mensagens recebidas
					ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
					if (msg != null) {

						String[] mensagem = msg.getContent().split(" ");

						if ( "inform".equalsIgnoreCase( mensagem[0] )){
							System.out.println(msg.getContent());

							if( true ){ //criterio de poluiçao permitida
								ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
								reply.setContent("fermAdherence");
								reply.addReceiver(msg.getSender());
								this.myAgent.send(reply);
							}
						}

						else if ( "fermAdherence".equalsIgnoreCase( mensagem[0] ) ){
							System.out.println(msg.getContent());

							ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
							reply.setContent("ackAdherence");
							reply.addReceiver(msg.getSender());
							this.myAgent.send(reply);

						}

						else if ( "ackAdherence".equalsIgnoreCase( mensagem[0] ) )
							System.out.println(msg.getContent());

						else if ( "break".equalsIgnoreCase( mensagem[0] ) ){
							System.out.println(msg.getContent());
							//sair do grupo

							ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
							reply.setContent("withdraw");
							reply.addReceiver(msg.getSender());
							this.myAgent.send(reply);
						}

						else if ( "withdraw".equalsIgnoreCase( mensagem[0] ) ){
							System.out.println(msg.getContent());
						}

					}
					else {
						// if no message is arrived, block the behaviour
						block();
					}

				}
			}
		});

	}

	public void draw(SimGraphics g) {
		g.drawFastCircle(Color.black);
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	/*
			getEnvironmentSample();
            updateEnvironmentModel();
            updateNeighboursRelationships();
            updateSocialNetwork();
	 */
	
	public double getEnergy() {
		return energy;
	}

	public void setNeighbours(Vector<Sensor> neighbours) {
		this.neighbours = neighbours;
	}

	public String getDescription() {
		return description;
	}

}