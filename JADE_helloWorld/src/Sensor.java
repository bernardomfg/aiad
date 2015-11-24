import uchicago.src.sim.space.Object2DTorus;

public class Sensor extends sajas.core.Agent {
    static int id = 0;
    private int personal_id;
    private int group_id;
    private String message;
    private float energy; //percent value
    private int coordX, coordY;
    private boolean isLeader;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    private boolean isActive;
    private Object2DTorus space;

    public Sensor(int personal_id, int coordX, int coordY, Object2DTorus space) {
        this.personal_id = personal_id;
        this.coordX = coordX;
        this.coordY = coordY;
        this.space = space;
        this.isLeader = true;
        this.energy = 100;
        isActive = false;
    }

    protected void setup() {

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