package wsc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.apache.commons.math3.exception.ConvergenceException;
//import org.apache.commons.math3.exception.MathIllegalArgumentException;
//import org.apache.commons.math3.exception.NumberIsTooSmallException;
//import org.apache.commons.math3.exception.util.LocalizedFormats;
//import org.apache.commons.math3.ml.clustering.Clusterable;
//import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
//import org.apache.commons.math3.ml.distance.DistanceMeasure;
//import org.apache.commons.math3.ml.distance.EuclideanDistance;
//import org.apache.commons.math3.random.JDKRandomGenerator;
//import org.apache.commons.math3.random.RandomGenerator;
//import org.apache.commons.math3.stat.descriptive.moment.Variance;
//import org.apache.commons.math3.util.MathUtils;
//import com.sun.corba.se.spi.orbutil.fsm.State;


import java.util.Map.Entry;

import ec.EvolutionState;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import ec.vector.VectorIndividual;
//import weka.clusterers.SimpleKMeans;

public class SequenceVectorIndividual extends VectorIndividual {
	//SimpleKMeans kmeans = new SimpleKMeans();
	private static final long serialVersionUID = 1L;

	private double dcost;
	private double ccost;
	private double ctime;
	private double dtime;

	private double time;
	private double cost;
	public List<Service> genome;
	public int ClusterNum=4;
	public int[] clusteredData=new int[ClusterNum];

	@Override
	public Parameter defaultBase() {
		return new Parameter("sequencevectorindividual");
	}

	public SequenceVectorIndividual() {
		fitness = new SimpleFitness();
	}


	/**
	 * Initializes the individual.
	 */
/*	public void createclusters(EvolutionState state, int thread)
	{WSCInitializer init = (WSCInitializer) state.initializer;
		List<LocationWrapper> clusterInput = new ArrayList<LocationWrapper>(init.re);
		for (i=0; i<init.relevantList.)
			clusterInput.add(new LocationWrapper(location));

		// initialize a new clustering algorithm.
		// we use KMeans++ with 3 clusters and 10000 iterations maximum.
		KMeansPlusPlusClusterer<LocationWrapper> clusterer = new KMeansPlusPlusClusterer<LocationWrapper>(3, 10000);
		List<CentroidCluster<LocationWrapper>> clusterResults = clusterer.cluster(clusterInput);

	}*/


	@Override
	/**
	 * Initializes the individual.
	 */
		public void reset(EvolutionState state, int thread) {
		WSCInitializer init = (WSCInitializer) state.initializer;


		if(init.performClustering)
		{
			if(init.numInit<10)
			{

				genome=new ArrayList<Service>(init.clusterList.get(0));
			//	System.out.println(genome.size());
			}
			else if(init.numInit<20)
			{genome=new ArrayList<Service>(init.clusterList.get(1));
		//	System.out.println(genome.size());

			}
			else if(init.numInit<30 && init.numInit>=20)
			{
				genome=new ArrayList<Service>(init.clusterList.get(2));
			}
		}
		else
		{genome = new ArrayList<Service>(init.relevantList);}
		Collections.shuffle(genome, init.random);
		this.evaluated = false;
		init.numInit++;
	}

	@Override
	public boolean equals(Object ind) {
		boolean result = false;

		if (ind != null && ind instanceof SequenceVectorIndividual) {
			result = true;
			SequenceVectorIndividual other = (SequenceVectorIndividual) ind;

			if (genome.size() == other.genome.size()) {
				for (int i = 0; i < genome.size(); i++) {
					if (!genome.get(i).equals(other.genome.get(i))) {
						result = false;
						break;
					}
				}
			}
			else {
				result = false;
			}
		}
		return result;
	}

	@Override
	public int hashCode() {
		Service[] genomeArray = new Service[genome.size()];
		genome.toArray(genomeArray);
		return Arrays.hashCode(genomeArray);
	}

	@Override
	public String toString() {
		Service[] genomeArray = new Service[genome.size()];
		genome.toArray(genomeArray);
		return Arrays.toString(genomeArray);
	}

	public String toGraphString(EvolutionState state) {
		WSCInitializer init = (WSCInitializer) state.initializer;
		Graph g = createNewGraph(init.numLayers, init.startServ, init.endServ, init);
		return g.toString();
	}

	public Graph createNewGraph(int numLayers, Service start, Service end, WSCInitializer init) {
		Service[] sequence = new Service[genome.size()];
		genome.toArray(sequence);
		Node endNode = new Node(end);
		Node startNode = new Node(start);

        Graph graph = new Graph();
        graph.nodeMap.put(endNode.getName(), endNode);

        // Populate inputs to satisfy with end node's inputs
        List<InputNodeLayerTrio> nextInputsToSatisfy = new ArrayList<InputNodeLayerTrio>();

        for (String input : end.getInputs()){
            nextInputsToSatisfy.add( new InputNodeLayerTrio(input, end.getName(), numLayers) );
        }

        // Fulfil inputs layer by layer
        for (int currLayer = numLayers; currLayer > 0; currLayer--) {

            // Filter out the inputs from this layer that need to fulfilled
            List<InputNodeLayerTrio> inputsToSatisfy = new ArrayList<InputNodeLayerTrio>();
            for (InputNodeLayerTrio p : nextInputsToSatisfy) {
               if (p.layer == currLayer)
                   inputsToSatisfy.add( p );
            }
            nextInputsToSatisfy.removeAll( inputsToSatisfy );

            int index = 0;
            while (!inputsToSatisfy.isEmpty()){

                if (index >= sequence.length) {
                    nextInputsToSatisfy.addAll( inputsToSatisfy );
                    inputsToSatisfy.clear();
                }
                else {
                	Service nextNode = sequence[index++];
                	if (nextNode.layer < currLayer) {
	                    Node n = new Node(nextNode);
	                    //int nLayer = nextNode.layerNum;

	                    List<InputNodeLayerTrio> satisfied = getInputsSatisfiedGraphBuilding(inputsToSatisfy, n, init);

	                    if (!satisfied.isEmpty()) {
	                        if (!graph.nodeMap.containsKey( n.getName() )) {
	                            graph.nodeMap.put(n.getName(), n);
	                        }

	                        // Add edges
	                        createEdges(n, satisfied, graph);
	                        inputsToSatisfy.removeAll(satisfied);


	                        for(String input : n.getInputs()) {
	                            nextInputsToSatisfy.add( new InputNodeLayerTrio(input, n.getName(), n.getLayer()) );
	                        }
	                    }
	                }
                }
            }
        }

        // Connect start node
        graph.nodeMap.put(startNode.getName(), startNode);
        createEdges(startNode, nextInputsToSatisfy, graph);

        return graph;
    }

	public void createEdges(Node origin, List<InputNodeLayerTrio> destinations, Graph graph) {
		// Order inputs by destination
		Map<String, Set<String>> intersectMap = new HashMap<String, Set<String>>();
		for(InputNodeLayerTrio t : destinations) {
			addToIntersectMap(t.service, t.input, intersectMap);
		}

		for (Entry<String,Set<String>> entry : intersectMap.entrySet()) {
			Edge e = new Edge(entry.getValue());
			origin.getOutgoingEdgeList().add(e);
			Node destination = graph.nodeMap.get(entry.getKey());
			destination.getIncomingEdgeList().add(e);
			e.setFromNode(origin);
        	e.setToNode(destination);
        	graph.edgeList.add(e);
		}
	}

	private void addToIntersectMap(String destination, String input, Map<String, Set<String>> intersectMap) {
		Set<String> intersect = intersectMap.get(destination);
		if (intersect == null) {
			intersect = new HashSet<String>();
			intersectMap.put(destination, intersect);
		}
		intersect.add(input);
	}

	public List<InputNodeLayerTrio> getInputsSatisfiedGraphBuilding(List<InputNodeLayerTrio> inputsToSatisfy, Node n, WSCInitializer init) {
	    List<InputNodeLayerTrio> satisfied = new ArrayList<InputNodeLayerTrio>();
	    for(InputNodeLayerTrio p : inputsToSatisfy) {
            if (init.taxonomyMap.get(p.input).servicesWithOutput.contains( n.getService() ))
                satisfied.add( p );
        }
	    return satisfied;
	}

	   public void calculateSequenceFitness(int numLayers, Service end, WSCInitializer init, EvolutionState state, boolean isOperation, boolean performFiltering) {
			//System.out.println(state.generation);
			double highestLocalTimeVariable=0.0;
		   Set<Service> solution = new HashSet<Service>();
		   	List<Service> filteredGenome = new ArrayList<Service>();
		   	int length = genome.size();
			//System.out.println(genome.get(1).ID+"0"+genome.get(1)+"PP"+genome.get(10).ID+"O"+genome.get(1));

		   	Service[] sequence = new Service[length];
		   	genome.toArray(sequence);
     	//   System.out.println(sequence.length);
    	  // System.out.println(sequence[0].ID+"P"+sequence[10].ID);



	        cost = 0.0;
	        ccost = 0.0;
	        dcost = 0.0;

	        // Populate inputs to satisfy with end node's inputs
	        List<InputTimeLayerTrio> nextInputsToSatisfy = new ArrayList<InputTimeLayerTrio>();
	        double t = end.getQos()[WSCInitializer.TIME];
	        double dt=end.getQos()[WSCInitializer.DATASETTIME];
	        for (String input : end.getInputs()){
	            nextInputsToSatisfy.add( new InputTimeLayerTrio(input, t, dt,0.0,0.0,numLayers,end.getName(),-1) );
	        }

	        // Fulfil inputs layer by layer
	        for (int currLayer = numLayers; currLayer > 0; currLayer--) {
	            // Filter out the inputs from this layer that need to fulfilled
	            List<InputTimeLayerTrio> inputsToSatisfy = new ArrayList<InputTimeLayerTrio>();
	            for (InputTimeLayerTrio p : nextInputsToSatisfy) {
	               if (p.layer == currLayer)
	                   inputsToSatisfy.add( p );
	            }
	            nextInputsToSatisfy.removeAll( inputsToSatisfy );

	            int index = 0;
	            while (!inputsToSatisfy.isEmpty()){
	                // If all nodes have been attempted, inputs must be fulfilled with start node
	                if (index >= sequence.length) {
	                    nextInputsToSatisfy.addAll(inputsToSatisfy);
	                    inputsToSatisfy.clear();
	                }
	                else {
	                Service nextNode = sequence[index++];
            // 	   System.out.println("I"+index+"P"+nextNode.ID);

	                if (nextNode.layer < currLayer) {

	   	                List<InputTimeLayerTrio> satisfied = getInputsSatisfied(inputsToSatisfy, nextNode, init);
	   	                if (!satisfied.isEmpty()) {
	                           double[] qos = nextNode.getQos();
	                           if (!solution.contains( nextNode )) {
	                               solution.add(nextNode);
	                               filteredGenome.add(nextNode);
	                               //cost += qos[WSCInitializer.COST];
	                               cost += qos[WSCInitializer.COST];
	                               dcost += qos[WSCInitializer.DATASETCOST];
	                           }
	                           highestLocalTimeVariable=0.0;
	                           ccost=0.0;
	                           ctime=0.0;

	                           double highestLocalCostVariable=0.0;
	                           double[] comarray= new double[2];

	                           for(InputTimeLayerTrio isat: satisfied)
	                           {	//	System.out.println(isat.ID);
	                      //  System.out.println(nextNode.getID());
	                        	 //  System.out.println("I"+index);
	                        	   comarray=communication.ComunicationCostandTime(isat.ID, nextNode.getID());
	                        	//   comarray[0]=0.9;
	                        //	   comarray[1]=0.9;
//System.out.println(isat.ID);
	                           if(comarray[0]>highestLocalCostVariable) {highestLocalCostVariable=comarray[0]; ccost=highestLocalCostVariable+isat.comcost;}
	                           if(comarray[1]>highestLocalTimeVariable) {highestLocalTimeVariable=comarray[1]; ctime=highestLocalTimeVariable+isat.comtime;}

	                           }



	                           						//now calculate distance between n and satisfieds
	                           //cost between all satisfied to n
	                           						//here we should consider time of links as well maximum time between satisfieds and n (max comtimes of all satisfiedstime+this max time)
	                           						t = qos[WSCInitializer.TIME];
	                           						dt=qos[WSCInitializer.DATASETTIME];

	                           						inputsToSatisfy.removeAll(satisfied);
	                           						double[] arrat=findHighestTime(satisfied);
	                           						double highestT = arrat[1];
	                           						double highestDT = arrat[2];
	                           						for(String input : nextNode.getInputs()) {
	                           							nextInputsToSatisfy.add( new InputTimeLayerTrio(input, highestT + t, highestDT+dt,ctime, ccost, nextNode.layer, nextNode.getName(),nextNode.getID()));
	                           						 }
	                       }
		               }
	                }
	            }
	        }

	        // Find the highest overall time
	     			double timearray[] = findHighestTime(nextInputsToSatisfy);

	     			//particle.availability = availability;
	     			//particle.reliability = reliability;
	     			time = timearray[1];


	     			dtime=timearray[2];

	     			if(performFiltering) genome=filteredGenome;
	     	        if (!WSCInitializer.dynamicNormalisation || isOperation)
	     	        	finishCalculatingSequenceFitness(init, state);
	     	    }

	     	   public void finishCalculatingSequenceFitness(WSCInitializer init, EvolutionState state) {
	     		   double f = calculateFitness(cost, time, dcost, dtime, ccost,ctime, init);
	     			init.trackFitnessPerEvaluations(f);

	     			((SimpleFitness) fitness).setFitness(state, f, false); // XXX Move this inside the other one
	     			evaluated = true;
	     	   }

	     	   public double[] findHighestTime(List<InputTimeLayerTrio> satisfied) {
	     			double max[] = new double[3];
	     			max[0]= Double.MIN_VALUE;
	     			max[1]= 0;
	     			max[2]= 0;
	     			//here we should modify and add comtime
	     			for (InputTimeLayerTrio p : satisfied) {
	     				if ((p.time+p.datatime) > max[0])
	     					{max[0] = p.time+p.datatime;
	     					max[1]=p.time;
	     					max[2]=p.datatime;

	     					}
	     			}

	     			return max;
	     		}


	     	  public double calculateFitness(double c, double t, double dc, double dt,double cc, double ct, WSCInitializer init) {

			       //normalising dataset attributes is different since their original value is in [0,1]
			        dt = normaliseDTime(dt,init);
			        dc = normaliseDCost(dc,init);

			        ct = normaliseCTime(ct,init);
			        cc = normaliseCCost(cc,init);


		        t = normaliseTime(t, init);
		        c = normaliseCost(c, init);

		       return ((1.0/(1+init.wdt * dt + init.wdc * dc + init.w3 * t + init.w4 * c+init.wcomuc * cc+init.wcomut * ct)));
		       //return (((init.wdt * (1-dt) + init.wdc * (1-dc) + init.w3 * (1-t) + init.w4 * (1-c)+init.wcomuc *(1- cc)+init.wcomut * (1-ct))));
		       // return ((init.wdt * dt + init.wdc * dc + init.w3 * t + init.w4 * c+init.wcomuc * cc+init.wcomut * ct));

			}

	     	 private double normaliseCCost(double cc, WSCInitializer init) {
	 			if (init.maxCCost - init.minCCost == 0.0)
	 				return 1.0;
	 			else
	 				return (init.maxCCost-cc)/(init.maxCCost  - init.minCCost);
	 		}

	 		private double normaliseCTime(double ct, WSCInitializer init) {
	 			if (init.maxCTime - init.minCTime == 0.0)
	 				return 1.0;
	 			else
	 				return (init.maxCTime-ct)/(init.maxCTime  - init.minCTime);
	 		}

	 		private double normaliseDCost(double dc, WSCInitializer init) {
	 			if (init.maxDCost - init.minDCost == 0.0)
	 				return 1.0;
	 			else
	 				return (init.maxDCost - dc)/(init.maxDCost - init.minDCost);
	 		}

	 		private double normaliseDTime(double dt, WSCInitializer init) {
	 			if (init.maxDTime - init.minDTime == 0.0)
	 				return 1.0;
	 			else
	 				return (init.maxDTime - dt)/(init.maxDTime - init.minDTime);
	 		}
	 		private double normaliseTime(double time, WSCInitializer init) {
				if (init.maxTime - init.minTime == 0.0)
					return 1.0;
				else
					return (init.maxTime - time)/(init.maxTime - init.minTime);
			}

			private double normaliseCost(double cost, WSCInitializer init) {
				if (init.maxCost - init.minCost == 0.0)
					return 1.0;
				else
					return (init.maxCost - cost)/(init.maxCost - init.minCost);
			}
		public List<InputTimeLayerTrio> getInputsSatisfied(List<InputTimeLayerTrio> inputsToSatisfy, Service n, WSCInitializer init) {
		    List<InputTimeLayerTrio> satisfied = new ArrayList<InputTimeLayerTrio>();
		    for(InputTimeLayerTrio p : inputsToSatisfy) {
	            if (init.taxonomyMap.get(p.input).servicesWithOutput.contains( n ))
	                satisfied.add( p );
	        }
		    return satisfied;
		}

		public void setDTime(double dtime) {
			this.dtime = dtime;
		}

		public void setDCost(double dcost) {
			this.dcost = dcost;
		}
		public void setCTime(double ctime) {
			this.ctime = ctime;
		}

		public void setCCost(double ccost) {
			this.ccost = ccost;
		}
		public void setTime(double time) {
			this.time = time;
		}

		public void setCost(double cost) {
			this.cost = cost;
		}

		public double getCTime() {
			return ctime;
		}

		public double getCCost() {
			return ccost;
		}
		public double getDTime() {
			return dtime;
		}

		public double getDCost() {
			return dcost;
		}

		public double getTime() {
			return time;
		}

		public double getCost() {
			return cost;
		}

}
