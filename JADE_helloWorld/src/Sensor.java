

import sajas.core.AID;
import sajas.core.behaviours.CyclicBehaviour;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DTorus;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.awt.*;

public class Sensor extends sajas.core.Agent implements Drawable {
    static int id = 0;
    private int personal_id;
    private int group_id;
    private String message;
    private float energy; //percent value
    private int coordX, coordY;
    private boolean isLeader;
    private double energyLossPerTick;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    private boolean isActive;
    private Object2DTorus space;

    public Sensor(int personal_id, int coordX, int coordY, Object2DTorus space, double energyLossPerTick) {
        this.personal_id = personal_id;
        this.coordX = coordX;
        this.coordY = coordY;
        this.space = space;
        this.isLeader = true;
        this.energy = 100;
        this.energyLossPerTick = energyLossPerTick;
        isActive = false;
    }

    protected void setup() {
    	
    	//receiver behaviour
    	this.addBehaviour( new sajas.core.behaviours.CyclicBehaviour(){

			@Override
			public void action() {
				
				ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				if (msg != null) {
					System.out.println(msg.getContent());
				}
				else {
					// if no message is arrived, block the behaviour
					block();
				}
				
			}
    		
    	});
    	
    }
    
    public void sendMessage(String t1AgentName){
    	ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("Hello agent blabla!");

		msg.addReceiver(new AID(t1AgentName, AID.ISLOCALNAME));

		send(msg);
		System.out.println("enviada");
		System.out.println(getLocalName()+" SENT GREETINGS MESSAGE  TO "+t1AgentName); 
    }

    public void draw(SimGraphics g) {
        g.drawFastCircle(Color.black);
    }

    @Override
    public int getX() {
        return coordX;
    }

    @Override
    public int getY() {
        return coordY;
    }

    public void executeCOSA(){
        while(this.energy > 0){
            /*getEnvironmentSample();
            updateEnvironmentModel();
            updateNeighboursRelationships();
            updateSocialNetwork();*/
        }
    }


    public static int getId() {
        return id;
    }

    public static void setId(int id) {
        Sensor.id = id;
    }

    public int getPersonal_id() {
        return personal_id;
    }

    public void setPersonal_id(int personal_id) {
        this.personal_id = personal_id;
    }

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public float getEnergy() {
        return energy;
    }

    public void setEnergy(float energy) {
        this.energy = energy;
    }
}