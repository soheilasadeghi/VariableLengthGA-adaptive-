package wsc;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Random;

import org.apache.commons.math3.ml.clustering.Clusterable;

//import com.google.common.collect.Ordering;

public class LocationWrapper implements Clusterable {
	private double[] points;
	private Service name;
	//private Location location;
	//private double diff;// difference between y and predicted y
	//private double euclidean_distance;
	//private Location location;

	public LocationWrapper(Service S) {
	//	this.location = location;
		//S.get
		 	BufferedReader abt = null;
		String dat = new String();
		Double lat,longt;
		String[] arr;

		try {
			abt = new BufferedReader(new FileReader("serversattribute.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int j = 1; j <= S.ID; j++)
			try {
				dat = abt.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		// dat=abc.readLine();

		arr = dat.split(" ");
		if (arr == null )
			{lat=0.0;
			longt=0.0;}



		lat= Double.valueOf(arr[1]);
		longt= Double.valueOf(arr[2]);

		this.points = new double[] { lat, longt };
		this.name=S;
	}



	public double[] getPoint() {
		return points;
	}

	public Service getservice() {
		return name;
	}

}