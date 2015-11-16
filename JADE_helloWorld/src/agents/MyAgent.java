package agents;

public abstract class MyAgent extends sajas.core.Agent {
    static int id = 0;
    private int personal_id;
    private int group_id;
    private String message;
    private float energy; //percent value

    protected void setup() {
        this.energy = 100;
    }

    public void executeCOSA(){
        while(this.energy > 0){
            /*getEnvironmentSample();
            updateEnvironmentModel();
            updateNeighboursRelationshoips();
            updateSocialNetwork();*/
        }
    }

    public static int getId() {
        return id;
    }

    public static void setId(int id) {
        MyAgent.id = id;
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