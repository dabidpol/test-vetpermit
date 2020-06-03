package etracs.training;

import com.rameses.osiris2.test.OsirisTestPlatform;
import java.util.HashMap;
import java.util.Map;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            Map env = new HashMap();
            env.put("app.host", "localhost:8072");
            env.put("app.context", "etracs25");
            env.put("app.cluster", "osiris3");

            Map roles = new HashMap();
            roles.put("AGRI.MASTER", null);
            roles.put("ENTITY.MASTER", null);
            roles.put("AGRI.ENCODER", null);
            roles.put("AGRI.APPROVER", null);
            Map profile = new HashMap();
            profile.put("CLIENTTYPE", "desktop");
            profile.put("USERID", "ADMIN");
            profile.put("USER", "ADMIN");
            profile.put("FULLNAME", "ADMINISTRATOR ROTARTSINIMDA");
            profile.put("JOBTITLE", "DALTANS");
            profile.put("ORGID", "059");
            profile.put("ORGCODE", "059");
            profile.put("ORGNAME", "PROVINCE OF BUKIDNON");
            profile.put("ORGCLASS", "PROVINCE");
            OsirisTestPlatform.runTest(env, roles, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
