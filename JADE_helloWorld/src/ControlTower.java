import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sajas.core.behaviours.CyclicBehaviour;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mario on 13/12/2015.
 */
public class ControlTower extends sajas.core.Agent {

    private HashMap<AID, String> backlog;
    private String description;

    public ControlTower() {
        this.backlog = new HashMap<AID, String>();
        this.description = "ControlTower";
    }

    protected void setup() {
        //receiver behaviour
        this.addBehaviour( new CyclicBehaviour(){

            private static final long serialVersionUID = 2L;

            @Override
            public void action() {
                //tratamento de mensagens recebidas
                ACLMessage msgInf = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));

                if (msgInf != null) {
                    String[] mensagem = msgInf.getContent().split(" ");

                    if ( "inform".equalsIgnoreCase( mensagem[0] )){
                        ControlTower.this.backlog.put(msgInf.getSender(), msgInf.getContent());
                        ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
                        reply.setContent("ackreceived");
                        reply.addReceiver(msgInf.getSender());
                        ControlTower.this.send(reply);
                    }

                }

            }
        });
    }

    public String getDescription() {
        return description;
    }
}
