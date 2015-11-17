/*****************************************************************
 SAJaS - Simple API for JADE-based Simulations is a framework to 
 facilitate running multi-agent simulations using the JADE framework.
 Copyright (C) 2015 Henrique Lopes Cardoso
 Universidade do Porto

 GNU Lesser General Public License

 This file is part of SAJaS.

 SAJaS is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 SAJaS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with SAJaS.  If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************/

package sajas.proto.states;

//#CUSTOM_EXCLUDE_FILE

import sajas.core.Agent;
import jade.core.*;
import sajas.core.behaviours.*;
import jade.lang.acl.*;

/**
 * Note: this class has been re-implemented to redirect the use of the agent and behaviour classes to SAJaS versions.
 * 
 * @see jade.proto.states.MsgReceiver
 * @author hlc
 *
 */
public class MsgReceiver extends SimpleBehaviour {

	/**
       A numeric constant to mean that a timeout expired.
	 */
	public static final int TIMEOUT_EXPIRED = -1001;

	/**
       A numeric constant to mean that the receive operation was
       interrupted.
	 */
	public static final int INTERRUPTED = -1002;

	/**
       A numeric constant to mean that the deadline for the receive
       operation will never expire.
	 */
	public static final int INFINITE = -1;

	protected MessageTemplate template;
	protected long deadline;
	protected Object receivedMsgKey;

	private boolean received;
	private boolean expired;
	private boolean interrupted;
	private int ret;

	/**
	 *  Constructor.
	 * @param a a reference to the Agent
	 * @param mt the MessageTemplate of the message to be received, if null
	 * the first received message is returned by this behaviour
	 * @param deadline a timeout for waiting until a message arrives. It must
	 * be expressed as an absolute time, as it would be returned by
	 * <code>System.currentTimeMillisec()</code>
	 * @param s the dataStore for this bheaviour
	 * @param msgKey the key where the beahviour must put the received
	 * message into the DataStore.
	 **/
	public MsgReceiver(Agent a, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
		super(a);
		setDataStore(s);
		template = mt;
		this.deadline = deadline;
		receivedMsgKey = msgKey;
		received = false;
		expired = false;
		interrupted = false;
	}



	//#APIDOC_EXCLUDE_BEGIN	

	// For persistence service
	protected MsgReceiver() {
	}

	public void action() {
		if (interrupted) {
			if (receivedMsgKey != null) {
				getDataStore().put(receivedMsgKey, null);
			}
			ret = INTERRUPTED;
			return;
		}

		ACLMessage msg = myAgent.receive(template);
		if (msg != null) {

			if (receivedMsgKey != null) {
				getDataStore().put(receivedMsgKey, msg);
			}
			received = true;
			ret = msg.getPerformative();
			handleMessage(msg);
		}
		else {
			if (deadline >= 0) {
				// If a timeout was set, then check if it is expired
				long blockTime = deadline - System.currentTimeMillis();
				if(blockTime <=0){
					//timeout expired
					if (receivedMsgKey != null) {
						getDataStore().put(receivedMsgKey, null);
					}
					expired = true;
					ret = TIMEOUT_EXPIRED;
					handleMessage(null);
				}else{
					block(blockTime);
				}
			}
			else {
				block();
			}
		}
	}

	public boolean done() {
		return received || expired || interrupted;
	}

	/**
	 * @return the performative if a message arrived,
	 * <code>TIMEOUT_EXPIRED</code> if the timeout expired or
	 * <code>INTERRUPTED</code> if this <code>MsgReceiver</code>
	 * was interrupted calling the <code>interrupt()</code> method.
	 **/
	public int onEnd() {
		received =false;
		expired =false;
		interrupted =false;
		return ret;
	}
	//#APIDOC_EXCLUDE_END

	/**
	   This is invoked when a message matching the specified template 
	   is received or the timeout has expired (the <code>msg</code>
	   parameter is null in this case). Users may redefine this method 
	   to react to this event. The default implementation of does nothing.
	 */
	protected void handleMessage(ACLMessage msg) {
	}

	/**
       Reset this behaviour, possibly replacing the receive templatt
       and other data.
       @param mt The template to match ACL messages against during the
       receive operation.
       @param deadline The relative timeout of the receive
       operation. If the <code>INFINITE</code> constant is used, then
       no deadline is set and the operation will wait until a matching
       ACL message arrives.
       @param s The datastore where the received ACL message is to be
       put.
       @param msgKey The key to use to put the received message into
       the selected datastore.
	 */
	public void reset(MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
		super.reset();
		received = false;
		expired = false;
		interrupted =false;
		setTemplate(mt);
		setDeadline(deadline);
		setDataStore(s);
		setReceivedKey(msgKey);
	}

	/**
	 * This method allows modifying the deadline
	 **/
	public void setDeadline(long deadline) {
		this.deadline = deadline;
	}

	/**
	 * This method allows modifying the template
	 **/
	public void setTemplate(MessageTemplate mt) {
		template=mt;
	}

	/**
	 * This method allows modifying the key in the DS where to put the 
	 * received message
	 **/
	public void setReceivedKey(Object key) {
		receivedMsgKey = key;
	}

	/**
       Signal an interruption to this receiver, and cause the ongoing
       receive operation to abort.
	 */
	public void interrupt() {
		interrupted = true;
		restart();
	}
}

