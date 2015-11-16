package agents;

/**
 * Created by mario on 10/11/2015.
 */
public class Dependant extends MyAgent {

    @Override
    protected void setup() {

        System.out.println("Descendant");
    }

    @Override
    public void executeCOSA() {
        super.executeCOSA();
    }
}
