package wsc;

/**
 *
 * @author Soheila
 */
public class InputTimeLayerTrio {
	 public String input, destinationservicename;
	    public double time;
	    public int layer;
	    public double datatime, comtime,comcost;
	    public int ID;


	    public InputTimeLayerTrio(String input, double time, double dtime, double ctime,double ccost, int layer, String name, int id) {
	        this.input = input;
	        this.time = time;
	        this.layer = layer;
	        this.datatime=dtime;
	        this.destinationservicename=name;
	        this.comtime=ctime;
	        this.ID=id;
	        this.comcost=ccost;
 }
}
