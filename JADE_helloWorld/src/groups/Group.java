package groups;

import agents.Leader;
import agents.MyAgent;
import java.util.ArrayList;



public class Group {

    static int ID = 0;
    int groupId;
    ArrayList<MyAgent> dependants;
    MyAgent groupLeader;


    public Group () {
        this.groupId = ID++;

        this.dependants = new ArrayList<MyAgent>();
        this.groupLeader = new Leader();
    }

    public Group (ArrayList<MyAgent> desc, MyAgent leader) {
        this.groupId = ID++;

        this.dependants = desc;
        this.groupLeader = leader;
    }
}
