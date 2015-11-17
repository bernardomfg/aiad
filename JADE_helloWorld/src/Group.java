import java.util.ArrayList;

public class Group {

    static int ID = 0;
    int groupId;
    ArrayList<Sensor> dependants;
    Sensor groupLeader;


    public Group () {
        this.groupId = ID++;

        this.dependants = new ArrayList<Sensor>();
    }

    public Group (ArrayList<Sensor> desc, Sensor leader) {
        this.groupId = ID++;

        this.dependants = desc;
        this.groupLeader = leader;
    }
}
