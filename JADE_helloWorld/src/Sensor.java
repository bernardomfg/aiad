

import sajas.core.behaviours.CyclicBehaviour;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

public class Sensor extends sajas.core.Agent implements Drawable, Serializable {
	private float energy; //percent value
	private int x, y;
	private double energyLossPerTick;
	private ArrayList<jade.core.AID> group;
	private String description;
	private Color color;
	private boolean isActive;
	private ArrayList<Sensor> sensorNeigh;
	private ArrayList<Water> waterNeigh;
	private float polutionAverage;
	private float lastPolutionAverage;
	private boolean usingCOSA;
	private ControlTower tower;

	public Sensor(int x, int y, String description, double energyLossPerTick, boolean cosa, ControlTower tower) {
		this.x = x;
		this.y = y;
		this.group = new ArrayList<jade.core.AID>();
		this.energy = 100;
		this.color = Color.decode("#1a7002");
		this.energyLossPerTick = energyLossPerTick;
		this.isActive = true;
		this.description = description;
		this.lastPolutionAverage = 0;
		this.polutionAverage = 0;
		this.usingCOSA = cosa;
		this.tower = tower;
	}

	protected void setup() {

		this.addBehaviour( new CyclicBehaviour(){

			private static final long serialVersionUID = 1L;

			public void updateEnergyValue() {
				if(energy - energyLossPerTick >= 0)
					energy -= energyLossPerTick;
				else{
					energy = 0;
					isActive = false;
				}

				if(energy < 30 && energy > 0)
					color = Color.yellow;
				else if(energy == 0)
					color = Color.RED;
			}

			public float getSample() {
				//sampleEnvironment();
				float total = 0;
				for(Water water: waterNeigh){
					total += water.getPollutionLvl();
				}

				return total/waterNeigh.size();
			}
			@Override
			public void action() {
				if(isActive) {
					lastPolutionAverage = polutionAverage;
					polutionAverage = getSample();

					ACLMessage tower_msg = new ACLMessage(ACLMessage.INFORM);
					tower_msg.setContent("inform " + polutionAverage);
					sajas.core.AID receiver_t = new sajas.core.AID(tower.getDescription(), sajas.core.AID.ISLOCALNAME);
					tower_msg.addReceiver(receiver_t);
					this.myAgent.send(tower_msg);

					if( Sensor.this.usingCOSA ){
						if( Math.abs( lastPolutionAverage - polutionAverage ) > 1 || Sensor.this.energy <= 10) { //diferenca de poluicao consideravel

							for(Sensor sensor: Sensor.this.sensorNeigh){

								ACLMessage msg = new ACLMessage(ACLMessage.CANCEL);
								msg.setContent("break");
								sajas.core.AID receiver = new sajas.core.AID(sensor.getDescription(), sajas.core.AID.ISLOCALNAME);
								msg.addReceiver(receiver);
								this.myAgent.send(msg);
								//System.out.println("Message: " + msg.getContent() + " From: " + msg.getSender().toString() + " to: " + receiver.toString());

							}

						}

						if( Sensor.this.energy > 0 ) {

							//inform neighbours
							for(Sensor sensor: Sensor.this.sensorNeigh){

								ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
								msg.setContent("inform " + Float.toString(polutionAverage));
								sajas.core.AID receiver = new sajas.core.AID(sensor.getDescription(), sajas.core.AID.ISLOCALNAME);
								msg.addReceiver(receiver);
								this.myAgent.send(msg);
								//System.out.println("Message: " + msg.getContent() + " From: " + msg.getSender().toString() + " to: " + receiver.toString());

							}
						}

					}

					updateEnergyValue();

					if( Sensor.this.energy <= 0 ) { Sensor.this.doDelete(); }
				}
			}
		});


		if( Sensor.this.usingCOSA ){

			//receiver behaviour
			this.addBehaviour( new CyclicBehaviour(){

				private static final long serialVersionUID = 2L;

				@Override
				public void action() {
					if(isActive) {

						//tratamento de mensagens recebidas
						ACLMessage msgInf = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
						ACLMessage msgProp = receive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
						ACLMessage msgAccep = receive(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));
						ACLMessage msgAgree = receive(MessageTemplate.MatchPerformative(ACLMessage.AGREE));

						if (msgInf != null) {
							String[] mensagem = msgInf.getContent().split(" ");

							if ( "inform".equalsIgnoreCase( mensagem[0] )){

								if( Float.parseFloat(mensagem[1]) > polutionAverage -3.0 && Float.parseFloat(mensagem[1]) < polutionAverage +3.0 ){ //criterio de poluicao permitida
									ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
									reply.setContent("fermAdherence");
									reply.addReceiver(msgInf.getSender());
									Sensor.this.send(reply);
									//System.out.println("Message: " + msgInf.getContent() + " From: " + reply.getSender().toString() + " to: " + msgInf.getSender().toString());
								}
							}
						}

						if ( msgProp != null  ){

							ACLMessage reply = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
							reply.setContent("ackAdherence");
							reply.addReceiver(msgProp.getSender());
							Sensor.this.send(reply);

							//System.out.println("Message: " + msgProp.getContent() + " From: " + reply.getSender().toString() + " to: " + msgProp.getSender().toString());

							Sensor.this.group.add(msgProp.getSender());
						}

						if ( msgAccep != null  ){ //entrou num grupo
							Sensor.this.isActive = false;
						}

						if ( msgAgree != null ){ //saiu do grupo
							Sensor.this.group.remove(msgAgree.getSender());
						}

					}

					ACLMessage msgCancel = receive(MessageTemplate.MatchPerformative(ACLMessage.CANCEL));

					if ( msgCancel != null ){

						//sair do grupo
						Sensor.this.isActive = true;

						ACLMessage reply = new ACLMessage(ACLMessage.AGREE);
						reply.setContent("withdraw");
						reply.addReceiver(msgCancel.getSender());
						Sensor.this.send(reply);

						//System.out.println("Message: " + msgCancel.getContent() + " From: " + reply.getSender().toString() + " to: " + msgCancel.getSender().toString());

					}

				}
			});
		}else {

		}
	}


	public void draw(SimGraphics g) {
		g.drawFastCircle(this.color);
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	public double getEnergy() {
		return energy;
	}

	public void setSensorNeighbours(ArrayList<Sensor> sensorNeigh2) {
		this.sensorNeigh = sensorNeigh2;
	}

	public void setWaterNeighbours(ArrayList<Water> water) {
		this.waterNeigh = water;
	}

	public String getDescription() {
		return description;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

}