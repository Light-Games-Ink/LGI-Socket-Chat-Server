package ru.lgi.main;

/**
 *
 * @author Thomas Otero H3R3T1C
 */
public class UpdateInfo {

  
    public String ver;
    
    public UpdateInfo(int ver) {
    	this.ver = String.valueOf(ver);
    	update();
    }

	private void update()
    {

		String[] run = {"java","-jar","updater/server_update.jar", "server_" + ver};
        try {
        	System.out.println("getting started..");
            Runtime.getRuntime().exec(run);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("started");
        System.exit(0);

    }

}
