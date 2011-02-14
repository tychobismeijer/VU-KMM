package carRepairAssistant;

public class CRA {
    private Model   m;
    private View    v;
    private Control c;

    public CRA() {
        m = new Model();
        v = new View();
        c = new Control(m, v);
    }

    void start() {
        c.start();
    }

    public static void main(String[] arg) {
        (new CRA()).start();
    }
}
