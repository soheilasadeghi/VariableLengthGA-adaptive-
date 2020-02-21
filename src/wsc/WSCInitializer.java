package wsc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import ec.EvolutionState;
import ec.simple.SimpleInitializer;
import ec.util.Parameter;
import wsc.WSCRandom;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
//import org.apache.commons.math3.ml.neuralnet.twod.util.LocationFinder.Location;
public class WSCInitializer extends SimpleInitializer {

	private static final long serialVersionUID = 1L;
	// Constants with of order of QoS attributes
	public static final int TIME = 0;
	public static final int COST = 1;
	//public static final int AVAILABILITY = 2;
	//public static final int RELIABILITY = 3;
	public static final int DATASETTIME = 2;
	public static final int DATASETCOST = 3;


	public Map<String, Service> serviceMap = new HashMap<String, Service>();
	public Map<String, Integer> serviceToIndexMap = new HashMap<String, Integer>();
	public Set<Service> relevant;
	public List<Service> relevantList;
	public Map<String, TaxonomyNode> taxonomyMap = new HashMap<String, TaxonomyNode>();
	public Set<String> taskInput;
	public Set<String> taskOutput;
	public Service startServ;
	public Service endServ;
	public WSCRandom random;
	public int numLayers;


	public boolean performClustering=false;
	public List<LocationWrapper> clusterInput;
	List<CentroidCluster<LocationWrapper>> clusterResults;
	public int numInit=0;
	public List<ArrayList<Service>> clusterList=new ArrayList<ArrayList<Service>>();
	//public double minAvailability = 0.0;
//	public double maxAvailability = -1.0;
//	public double minReliability = 0.0;
	//public double maxReliability = -1.0;
	public double minTime = Double.MAX_VALUE;
	public double maxTime = -1.0;
	public double minCost = Double.MAX_VALUE;
	public double maxCost = -1.0;public double minCTime = Double.MAX_VALUE;
	public double maxCTime = -1.0;
	public double minCCost = Double.MAX_VALUE;
	public double maxCCost = -1.0;

	public double minDTime = Double.MAX_VALUE;
	public double maxDTime = -1.0;
	public double minDCost = Double.MAX_VALUE;
	public double maxDCost = -1.0;
	public static double  MAXIMUM_DISTANCE=4998.836;
	public static double MINIMUM_DISTANCE=0.253;

	public static double  MAXIMUM_COMTIME=MINIMUM_DISTANCE;
	public static double MINIMUM_COMCOST=MAXIMUM_DISTANCE;
	public static double  MAXIMUM_COMCOST=MINIMUM_DISTANCE;
	public static double MINIMUM_COMTIME=MAXIMUM_DISTANCE;

	//public double w1;
	//public double w2;
	public double w3;
	public double w4;
	public double wdc;
	public double wdt;
	public double wcomuc;
	public double wcomut;
	public static boolean dynamicNormalisation;
	public static int numMutations;

	public static double[] meanTimePerGen;
	public static double[] meanCostPerGen;
	public static double[] meanDataTimePerGen;
	public static double[] meanDataCostPerGen;
	public static double[] meanComTimePerGen;
	public static double[] meanComCostPerGen;

	//public static int availIdx = 0;
	//public static int reliaIdx = 0;
	public static int timeIdx = 0;
	public static int costIdx = 0;
	public static int datatimeIdx = 0;
	public static int datacostIdx = 0;
	public static int comtimeIdx = 0;
	public static int comcostIdx = 0;
	public static final int Num_dataset=3;
	public static final int Num_datasetPerServer=8;

	public static ArrayList<Double> convergenceData = new ArrayList<Double>();
	public static File evaluationsLogFile;
	public int evalSampleRate;
	public static long setupTime;

	// Statistics tracking
	public int numEvaluations = 1;
	public double globalFitness = 0.0;

	public void trackFitnessPerEvaluations(double fitness) {
		if (fitness > globalFitness)
			globalFitness = fitness;
		if (numEvaluations % evalSampleRate == 0)
			convergenceData.add(globalFitness);
		numEvaluations++;
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		long startTime = System.currentTimeMillis();
		random = new WSCRandom(state.random[0]);
		super.setup(state,base);

		Parameter servicesParam = new Parameter("composition-services");
		Parameter taskParam = new Parameter("composition-task");
		Parameter taxonomyParam = new Parameter("composition-taxonomy");
		Parameter weight1Param = new Parameter("fitness-weight1");
		Parameter weight2Param = new Parameter("fitness-weight2");
		Parameter weight3Param = new Parameter("fitness-weight3");
		Parameter weight4Param = new Parameter("fitness-weight4");
		Parameter weight5Param = new Parameter("fitness-weight5");
		Parameter weight6Param = new Parameter("fitness-weight6");
		Parameter evaluationsLogNameParam = new Parameter("stat.evaluations");
		Parameter evalSampleRateParam = new Parameter("stat.eval-sample-rate");
		Parameter dynamicNormalisationParam = new Parameter("dynamic-normalisation");
		Parameter numMutationsParam = new Parameter("num-mutations");
		Parameter probability=new Parameter("pop.subpop.0.species.pipe.source.1.prob");
		w3 = state.parameters.getDouble(weight1Param, null);
		w4 = state.parameters.getDouble(weight2Param, null);
		wdc = state.parameters.getDouble(weight3Param, null);
		wdt = state.parameters.getDouble(weight4Param, null);
		wcomuc = state.parameters.getDouble(weight5Param, null);
		wcomut = state.parameters.getDouble(weight6Param, null);
		dynamicNormalisation = state.parameters.getBoolean(dynamicNormalisationParam, null, false);
		numMutations = state.parameters.getInt(numMutationsParam, null);

		int numGens = state.parameters.getInt(new Parameter("generations"), null);
		meanDataTimePerGen = new double[numGens];
		meanDataCostPerGen = new double[numGens];
		meanComTimePerGen = new double[numGens];
		meanComCostPerGen = new double[numGens];
		meanTimePerGen = new double[numGens];
		meanCostPerGen = new double[numGens];

		parseWSCServiceFile(state.parameters.getString(servicesParam, null));
		parseWSCTaskFile(state.parameters.getString(taskParam, null));
		parseWSCTaxonomyFile(state.parameters.getString(taxonomyParam, null));
		evaluationsLogFile = state.parameters.getFile( evaluationsLogNameParam, null );
		evalSampleRate = state.parameters.getInt(evalSampleRateParam, null);
		findConceptsForInstances();
//state.parameters.set(probability, "0.9");
		double[] mockQos = new double[4];
		mockQos[TIME] = 0;
		mockQos[COST] = 0;
		mockQos[DATASETTIME] = 0;
		mockQos[DATASETCOST] = 0;
		Set<String> startOutput = new HashSet<String>();
		startOutput.addAll(taskInput);
		startServ = new Service("start", mockQos, new HashSet<String>(), taskInput,-2);
		endServ = new Service("end", mockQos, taskOutput ,new HashSet<String>(),-1);
	//	startServ = new Service("start", mockQos, new HashSet<String>(), taskInput);
	//	endServ = new Service("end", mockQos, taskOutput ,new HashSet<String>());

		populateTaxonomyTree();
		relevant = getRelevantServices(serviceMap, taskInput, taskOutput);
		relevantList = new ArrayList<Service>(relevant);
		if (!dynamicNormalisation) {
			calculateNormalisationBounds(relevant);
			//calculateNormalisationBounds(new HashSet<Service>(serviceMap.values())); // XXX
		}
		if(performClustering)
		{
			 clusterInput = new ArrayList<LocationWrapper>(relevantList.size());
			for (int i=0; i<relevantList.size();i++)
				clusterInput.add(new LocationWrapper(relevantList.get(i)));

			// initialize a new clustering algorithm.
			// we use KMeans++ with 3 clusters and 10000 iterations maximum.
			KMeansPlusPlusClusterer<LocationWrapper> clusterer = new KMeansPlusPlusClusterer<LocationWrapper>(3, 10000);
			 clusterResults = clusterer.cluster(clusterInput);
			// clusterList =new ArrayList<ArrayList<Service>>();

			 List<Service> lst;
			 List<LocationWrapper> llw;
			 for(int i=0; i<3;i++) {
					llw=clusterResults.get(i).getPoints();
					 lst=new ArrayList<Service>(llw.size());
					 for (LocationWrapper locationWrapper : clusterResults.get(i).getPoints())
					 {lst.add(locationWrapper.getservice());

					 }

					 clusterList.add((ArrayList<Service>) lst);


				}
		}
		// Set size of genome

		Parameter genomeSizeParam = new Parameter("pop.subpop.0.species.genome-size");
		state.parameters.set(genomeSizeParam, "" + relevant.size());
		setupTime = System.currentTimeMillis() - startTime;
	}

	/**
	 * Checks whether set of inputs can be completely satisfied by the search
	 * set, making sure to check descendants of input concepts for the subsumption.
	 *
	 * @param inputs
	 * @param searchSet
	 * @return true if search set subsumed by input set, false otherwise.
	 */
	public boolean isSubsumed(Set<String> inputs, Set<String> searchSet) {
		boolean satisfied = true;
		for (String input : inputs) {
			Set<String> subsumed = taxonomyMap.get(input).getSubsumedConcepts();
			if (!isIntersection( searchSet, subsumed )) {
				satisfied = false;
				break;
			}
		}
		return satisfied;
	}

	/**
	 * Returns the set of inputs that can be satisfied by the search set.
	 *
	 * @param inputs
	 * @param searchSet
	 * @return inputs subsumed.
	 */
	public Set<String> getInputsSubsumed(Set<String> inputs, Set<String> searchSet) {
		Set<String> satisfied = new HashSet<String>();
		for (String input : inputs) {
			Set<String> subsumed = taxonomyMap.get(input).getSubsumedConcepts();
			if (isIntersection(searchSet,subsumed)) {
				satisfied.add(input);
			}
		}
		return satisfied;
	}

	/**
	 * Checks whether set of inputs can be completely satisfied by the search
	 * set, making sure to check descendants of input concepts for the subsumption.
	 *
	 * @param inputs
	 * @param searchSet
	 * @return true if search set subsumed by input set, false otherwise.
	 */
	public Set<String> getInputsNotSubsumed(Set<String> inputs, Set<String> searchSet) {
		Set<String> notSatisfied = new HashSet<String>();
		for (String input : inputs) {
			Set<String> subsumed = taxonomyMap.get(input).getSubsumedConcepts();
			if (!isIntersection( searchSet, subsumed )) {
				notSatisfied.add(input);
			}
		}
		return notSatisfied;
	}

    private static boolean isIntersection( Set<String> a, Set<String> b ) {
        for ( String v1 : a ) {
            if ( b.contains( v1 ) ) {
                return true;
            }
        }
        return false;
    }

	/**
	 * Populates the taxonomy tree by associating services to the
	 * nodes in the tree.
	 */
	private void populateTaxonomyTree() {
		for (Service s: serviceMap.values()) {
			addServiceToTaxonomyTree(s);
		}
	}

	private void addServiceToTaxonomyTree(Service s) {
		// Populate outputs
	    Set<TaxonomyNode> seenConceptsOutput = new HashSet<TaxonomyNode>();
		for (String outputVal : s.getOutputs()) {
			TaxonomyNode n = taxonomyMap.get(outputVal);
			s.getTaxonomyOutputs().add(n);

			// Also add output to all parent nodes
			Queue<TaxonomyNode> queue = new LinkedList<TaxonomyNode>();
			queue.add( n );

			while (!queue.isEmpty()) {
			    TaxonomyNode current = queue.poll();
		        seenConceptsOutput.add( current );
		        current.servicesWithOutput.add(s);
		        for (TaxonomyNode parent : current.parents) {
		            if (!seenConceptsOutput.contains( parent )) {
		                queue.add(parent);
		                seenConceptsOutput.add(parent);
		            }
		        }
			}
		}
		// Populate inputs
		Set<TaxonomyNode> seenConceptsInput = new HashSet<TaxonomyNode>();
		for (String inputVal : s.getInputs()) {
			TaxonomyNode n = taxonomyMap.get(inputVal);

			// Also add input to all children nodes
			Queue<TaxonomyNode> queue = new LinkedList<TaxonomyNode>();
			queue.add( n );

			while(!queue.isEmpty()) {
				TaxonomyNode current = queue.poll();
				seenConceptsInput.add( current );

			    Set<String> inputs = current.servicesWithInput.get(s);
			    if (inputs == null) {
			    	inputs = new HashSet<String>();
			    	inputs.add(inputVal);
			    	current.servicesWithInput.put(s, inputs);
			    }
			    else {
			    	inputs.add(inputVal);
			    }

			    for (TaxonomyNode child : current.children) {
			        if (!seenConceptsInput.contains( child )) {
			            queue.add(child);
			            seenConceptsInput.add( child );
			        }
			    }
			}
		}
		return;
	}

	/**
	 * Converts input, output, and service instance values to their corresponding
	 * ontological parent.
	 */
	private void findConceptsForInstances() {
		Set<String> temp = new HashSet<String>();

		for (String s : taskInput)
			temp.add(taxonomyMap.get(s).parents.get(0).value);
		taskInput.clear();
		taskInput.addAll(temp);

		temp.clear();
		for (String s : taskOutput)
				temp.add(taxonomyMap.get(s).parents.get(0).value);
		taskOutput.clear();
		taskOutput.addAll(temp);

		for (Service s : serviceMap.values()) {
			temp.clear();
			Set<String> inputs = s.getInputs();
			for (String i : inputs)
				temp.add(taxonomyMap.get(i).parents.get(0).value);
			inputs.clear();
			inputs.addAll(temp);

			temp.clear();
			Set<String> outputs = s.getOutputs();
			for (String o : outputs)
				temp.add(taxonomyMap.get(o).parents.get(0).value);
			outputs.clear();
			outputs.addAll(temp);
		}
	}

	/**
	 * Goes through the service list and retrieves only those services which
	 * could be part of the composition task requested by the user.
	 *
	 * @param serviceMap
	 * @return relevant services
	 */
	private Set<Service> getRelevantServices(Map<String,Service> serviceMap, Set<String> inputs, Set<String> outputs) {
		// Copy service map values to retain original
		Collection<Service> services = new ArrayList<Service>(serviceMap.values());

		Set<String> cSearch = new HashSet<String>(inputs);
		Set<Service> sSet = new HashSet<Service>();
		int layer = 0;
		Set<Service> sFound = discoverService(services, cSearch);
		while (!sFound.isEmpty()) {
			sSet.addAll(sFound);
			// Record the layer that the services belong to in each node
			for (Service s : sFound)
				s.layer = layer;

			layer++;
			services.removeAll(sFound);
			for (Service s: sFound) {
				cSearch.addAll(s.getOutputs());
			}
			sFound.clear();
			sFound = discoverService(services, cSearch);
		}
		endServ.layer = layer++;
		numLayers = layer;

		if (isSubsumed(outputs, cSearch)) {
			return sSet;
		}
		else {
			String message = "It is impossible to perform a composition using the services and settings provided.";
			System.out.println(message);
			System.exit(0);
			return null;
		}
	}

	private void calculateNormalisationBounds(Set<Service> services) {
		for(Service service: services) {
			double[] qos = service.getQos();
/*
			//
			double comt = qos[DATASETTIME];
			if (comt > maxDTime)
				maxDTime = comt;

			//
			double comc = qos[DATASETCOST];
			if (comc > maxDCost)
				maxDCost = comc;
*/
			// Time
			double time = qos[TIME];
			if (time > maxTime)
				maxTime = time;
			if (time < minTime)
				minTime = time;

			// Cost
			double cost = qos[COST];
			if (cost > maxCost)
				maxCost = cost;
			if (cost < minCost)
				minCost = cost;
		}
		// Adjust max. cost and max. time based on the number of services in shrunk repository
		maxCost *= services.size();
		maxTime *= services.size();
maxDCost=services.size();
maxDTime=services.size();
//to be calculated
minDCost=0;
minDTime=0;

minCTime=0;
minCCost=0;
maxCTime=MAXIMUM_DISTANCE*(services.size()-1);
maxCCost=MAXIMUM_DISTANCE*(services.size()-1);

	}

	/**
	 * Discovers all services from the provided collection whose
	 * input can be satisfied either (a) by the input provided in
	 * searchSet or (b) by the output of services whose input is
	 * satisfied by searchSet (or a combination of (a) and (b)).
	 *
	 * @param services
	 * @param searchSet
	 * @return set of discovered services
	 */
	private Set<Service> discoverService(Collection<Service> services, Set<String> searchSet) {
		Set<Service> found = new HashSet<Service>();
		for (Service s: services) {
			if (isSubsumed(s.getInputs(), searchSet))
				found.add(s);
		}
		return found;
	}

	/**
	 * Parses the WSC Web service file with the given name, creating Web
	 * services based on this information and saving them to the service map.
	 *
	 * @param fileName
	 */
	private void parseWSCServiceFile(String fileName) {
        Set<String> inputs = new HashSet<String>();
        Set<String> outputs = new HashSet<String>();
        double[] qos = new double[4];
        int[] dataset=new int[Num_dataset];
		int SID;
        try {
        	File fXmlFile = new File(fileName);
        	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        	Document doc = dBuilder.parse(fXmlFile);

        	NodeList nList = doc.getElementsByTagName("service");

        	for (int i = 0; i < nList.getLength(); i++) {
        		org.w3c.dom.Node nNode = nList.item(i);
        		Element eElement = (Element) nNode;

        		String name = eElement.getAttribute("name");
    		    qos[TIME] = Double.valueOf(eElement.getAttribute("Res"));
    		    qos[COST] = Double.valueOf(eElement.getAttribute("Pri"));
    		    SID=Integer.parseInt(eElement.getAttribute("serverid"));



				for(int l=0;l<Num_dataset;l++)
				{
					dataset[l]=Integer.parseInt(eElement.getAttribute("ds"+Integer.toString(l+1)));

				}

				qos[DATASETTIME]= calculateDatasetTime(dataset,Num_dataset,SID);
				qos[DATASETCOST] =calculateDatasetCost(dataset,Num_dataset);
    		//    qos[AVAILABILITY] = Double.valueOf(eElement.getAttribute("Ava"));
    		//    qos[RELIABILITY] = Double.valueOf(eElement.getAttribute("Rel"));
//
				// Get inputs
				org.w3c.dom.Node inputNode = eElement.getElementsByTagName("inputs").item(0);
				NodeList inputNodes = ((Element)inputNode).getElementsByTagName("instance");
				for (int j = 0; j < inputNodes.getLength(); j++) {
					org.w3c.dom.Node in = inputNodes.item(j);
					Element e = (Element) in;
					inputs.add(e.getAttribute("name"));
				}

				// Get outputs
				org.w3c.dom.Node outputNode = eElement.getElementsByTagName("outputs").item(0);
				NodeList outputNodes = ((Element)outputNode).getElementsByTagName("instance");
				for (int j = 0; j < outputNodes.getLength(); j++) {
					org.w3c.dom.Node out = outputNodes.item(j);
					Element e = (Element) out;
					outputs.add(e.getAttribute("name"));
				}

                Service ws = new Service(name, qos, inputs, outputs, SID);
                serviceMap.put(name, ws);
                inputs = new HashSet<String>();
                outputs = new HashSet<String>();
                qos = new double[4];
        	}
        }
        catch(IOException ioe) {
            System.out.println("Service file parsing failed...");
        }
        catch (ParserConfigurationException e) {
            System.out.println("Service file parsing failed...");
		}
        catch (SAXException e) {
            System.out.println("Service file parsing failed...");
		}
    }


	private double calculateDatasetCost(int[] dataset, int numDataset) throws IOException {
		String dat = new String();
		Double Cost=0.0;

		for(int i=0; i<numDataset; i++)
		{
			BufferedReader abc = new BufferedReader(new FileReader("datasetsattribute.txt"));

			for(int j=1;j<=dataset[i];j++)
			 dat=abc.readLine();
			//dat=abc.readLine();

			String[] arr=dat.split(" ");
			Cost+=(1-Double.valueOf(arr[5]));

abc.close();

		}


		return ((Cost)/3);
	}

	private double calculateDatasetTime(int[] dataset, int numDataset, int sID) throws IOException{
		String dat = new String();
		Double distanceofservers, normaliseddistanceofservers,propagation=0.0;
		String[] arr;
		Double Tsall=0.0;
		BufferedReader abt=null;

		for(int i=0; i<numDataset; i++)
		{
		 abt = new BufferedReader(new FileReader("datasetsattribute.txt"));
		for(int j=1;j<=dataset[i];j++)
		 dat=abt.readLine();


		arr=dat.split(" ");
		Tsall+=(1-Double.valueOf(arr[4]));
abt.close();
		}

abt = new BufferedReader(new FileReader("DistanceOfServers.txt"));
for(int i=0; i<numDataset; i++)
{abt = new BufferedReader(new FileReader("DistanceOfServers.txt"));
double jj=((dataset[i]-1)/Num_datasetPerServer)+1;
for(int j=1;j<=jj;j++)
 dat=abt.readLine();
//dat=abc.readLine();

arr=dat.split(" ");
if(arr==null || sID>413 || arr.length<400)
{
	System.out.println("hello");
	System.out.println("hello");
jj++;
	System.out.println("jj");

}


distanceofservers=Double.valueOf(arr[sID-1]);
normaliseddistanceofservers=(MAXIMUM_DISTANCE-distanceofservers)/(MAXIMUM_DISTANCE-MINIMUM_DISTANCE);
propagation+=normaliseddistanceofservers;

}

		return ((Tsall+propagation)/6);
	}
	/**
	 * Parses the WSC task file with the given name, extracting input and
	 * output values to be used as the composition task.
	 *
	 * @param fileName
	 */
	private void parseWSCTaskFile(String fileName) {
		try {
	    	File fXmlFile = new File(fileName);
	    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    	Document doc = dBuilder.parse(fXmlFile);

	    	org.w3c.dom.Node provided = doc.getElementsByTagName("provided").item(0);
	    	NodeList providedList = ((Element) provided).getElementsByTagName("instance");
	    	taskInput = new HashSet<String>();
	    	for (int i = 0; i < providedList.getLength(); i++) {
				org.w3c.dom.Node item = providedList.item(i);
				Element e = (Element) item;
				taskInput.add(e.getAttribute("name"));
	    	}

	    	org.w3c.dom.Node wanted = doc.getElementsByTagName("wanted").item(0);
	    	NodeList wantedList = ((Element) wanted).getElementsByTagName("instance");
	    	taskOutput = new HashSet<String>();
	    	for (int i = 0; i < wantedList.getLength(); i++) {
				org.w3c.dom.Node item = wantedList.item(i);
				Element e = (Element) item;
				taskOutput.add(e.getAttribute("name"));
	    	}
		}
		catch (ParserConfigurationException e) {
            System.out.println("Task file parsing failed...");
            e.printStackTrace();
		}
		catch (SAXException e) {
            System.out.println("Task file parsing failed...");
            e.printStackTrace();
		}
		catch (IOException e) {
            System.out.println("Task file parsing failed...");
            e.printStackTrace();
		}
	}

	/**
	 * Parses the WSC taxonomy file with the given name, building a
	 * tree-like structure.
	 *
	 * @param fileName
	 */
	private void parseWSCTaxonomyFile(String fileName) {
		try {
	    	File fXmlFile = new File(fileName);
	    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    	Document doc = dBuilder.parse(fXmlFile);
	    	NodeList taxonomyRoots = doc.getChildNodes();

	    	processTaxonomyChildren(null, taxonomyRoots);
		}

		catch (ParserConfigurationException e) {
            System.err.println("Taxonomy file parsing failed...");
		}
		catch (SAXException e) {
            System.err.println("Taxonomy file parsing failed...");
		}
		catch (IOException e) {
            System.err.println("Taxonomy file parsing failed...");
		}
	}

	/**
	 * Recursive function for recreating taxonomy structure from file.
	 *
	 * @param parent - Nodes' parent
	 * @param nodes
	 */
	private void processTaxonomyChildren(TaxonomyNode parent, NodeList nodes) {
		if (nodes != null && nodes.getLength() != 0) {
			for (int i = 0; i < nodes.getLength(); i++) {
				org.w3c.dom.Node ch = nodes.item(i);

				if (!(ch instanceof Text)) {
					Element currNode = (Element) nodes.item(i);
					String value = currNode.getAttribute("name");
					TaxonomyNode taxNode = taxonomyMap.get( value );
					if (taxNode == null) {
					    taxNode = new TaxonomyNode(value);
					    taxonomyMap.put( value, taxNode );
					}
					if (parent != null) {
					    taxNode.parents.add(parent);
						parent.children.add(taxNode);
					}

					NodeList children = currNode.getChildNodes();
					processTaxonomyChildren(taxNode, children);
				}
			}
		}
	}
}
