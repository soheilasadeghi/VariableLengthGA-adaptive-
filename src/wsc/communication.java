package wsc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class communication {

	// private int SourceID, DestinationID;
	public static double MAXIMUM_DISTANCE = 4998.836;
	public static double MINIMUM_DISTANCE = 0.253;

	public static double[] ComunicationCostandTime(int sourceID, int destinationID) {
		
		double[] costandtime = new double[2];

		costandtime[0] = 0.0;
		costandtime[1] = 0.0;
		if (sourceID < 0 || destinationID < 0) {
			return costandtime;
		}
		BufferedReader abt = null;
		String dat = new String();
		Double distanceofservers, normaliseddistanceofservers;
		String[] arr;

		try {
			abt = new BufferedReader(new FileReader("DistanceOfServers.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int j = 1; j <= sourceID; j++)
			try {
				dat = abt.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		// dat=abc.readLine();

		arr = dat.split(" ");
		if (arr == null || destinationID > 413 || arr.length < 400) {
			
			

		
			sourceID++;


		}
		

		distanceofservers = Double.valueOf(arr[destinationID - 1]);
	
		normaliseddistanceofservers = (MAXIMUM_DISTANCE - distanceofservers) / (MAXIMUM_DISTANCE - MINIMUM_DISTANCE);
		costandtime[0] = normaliseddistanceofservers;
		costandtime[1] = normaliseddistanceofservers;

		return costandtime;

	}

}
