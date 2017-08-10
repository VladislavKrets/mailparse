package online.omnia.mailparser;

import online.omnia.mailparser.zoho.dao.MySQLDaoImpl;
import online.omnia.mailparser.zoho.daoentities.EmailAccessEntity;

import java.util.List;

/**
 * Created by lollipop on 10.08.2017.
 */
public class Controller {
    public void messagesParsing() {
        List<EmailAccessEntity> accessEntities = MySQLDaoImpl.getInstance().getMailsByCheck(1);
        for (EmailAccessEntity accessEntity : accessEntities) {
            MailThread mailThread = new MailThread("orion_noreply@cmcm.com",
                    Utils.createPropertiesFile(accessEntity), accessEntity);

        }
    }
}
