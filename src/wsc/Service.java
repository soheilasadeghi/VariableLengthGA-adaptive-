package wsc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Service implements Cloneable{
	public List<TaxonomyNode> taxonomyOutputs = new ArrayList<TaxonomyNode>();
	public String name;
	public double[] qos;
	public Set<String> inputs;
	public Set<String> outputs;
	public int layer;
	public double X;
	public double Y;
	public int ID;

	public Service(String name, double[] qos, Set<String> inputs, Set<String> outputs,int id) {
		this.name = name;
		this.qos = qos;
		this.inputs = inputs;
		this.outputs = outputs;
		this.ID=id;
	}

	public double[] getQos() {
		return qos;
	}
	 protected Object clone() throws CloneNotSupportedException {
	        return super.clone();
	    }
	public Set<String> getInputs() {
		return inputs;
	}
public double[] getX(String name)
{//double lat,lon;
double[] point=new double[2];

//here gets the location for the service from serverattributes.


	return point;}


public int getID() {
	return ID;
}
	public Set<String> getOutputs() {
		return outputs;
	}

	public String getName() {
		return name;
	}

	public List<TaxonomyNode> getTaxonomyOutputs() {
		return taxonomyOutputs;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Service) {
			Service o = (Service) other;
			return name.equals(o.name);
		}
		else
			return false;
	}

	@Override
	public String toString() {
	    return name;
	}
}
