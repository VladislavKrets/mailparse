package online.omnia.mailparser;


import online.omnia.mailparser.dao.MySQLAdsetDaoImpl;

import java.io.IOException;

/**
 * Created by lollipop on 07.08.2017.
 */
public class Main {
    public static int days;
    public static long deltaTime = 24 * 60 * 60 * 1000;
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            return;
        }
        else if (!args[0].matches("\\d+")) return;

        if (Integer.parseInt(args[0]) == 0) {
            days = 0;
            deltaTime = 0;
        }
        days = Integer.parseInt(args[0]);
        Controller controller = new Controller();
        controller.apiCheetahNew();
        //controller.propelleradsApiNew();
        //controller.zeroparkApiNew();
        MySQLAdsetDaoImpl.getSessionFactory().close();
    }
}
