package online.omnia.mailparser;

import online.omnia.mailparser.dao.MySQLAdsetDaoImpl;
import online.omnia.mailparser.daoentities.AccountEntity;
import online.omnia.mailparser.utils.Utils;

import java.util.List;
import java.util.Map;

/**
 * Created by lollipop on 07.08.2017.
 */
public class Main {
    public static void main(String[] args){
        Controller controller = new Controller();
        controller.cheetahNew();

    }
}
