package wsc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import java.util.Random;

public class WSCLocalSearchPipeline extends BreedingPipeline {
	 static int someNumber = 0;
	 static int countNumber = 0;
	 static int countFirstLS = 0;
	 static int countSecondLS = 0;
	 static int countMu = 0;




	private static final long serialVersionUID = 1L;

	@Override
	public Parameter defaultBase() {
		return new Parameter("wsclocalsearchpipeline");
	}

	@Override
	public int numSources() {
		return 1;
	}

	@Override
	public int produce(int min, int max, int start, int subpopulation,
			Individual[] inds, EvolutionState state, int thread) {
		Random rand = new Random();
		int n = sources[0].produce(min, max, start, subpopulation, inds, state, thread);

        if (!(sources[0] instanceof BreedingPipeline)) {
            for(int q=start;q<n+start;q++)
                inds[q] = (Individual)(inds[q].clone());
        }

        if (!(inds[start] instanceof SequenceVectorIndividual))
            // uh oh, wrong kind of individual
            state.output.fatal("WSCLocalSearchPipeline didn't get a SequenceVectorIndividual. The offending individual is in subpopulation "
            + subpopulation + " and it's:" + inds[start]);
        WSCInitializer init = (WSCInitializer) state.initializer;
        if(state.generation<10 && (state.generation>15 && state.generation<25)) {}

        else
        {
        	
        //	System.out.println("LS");
        double d=0.0,maxdistance=0.0;
int index1=0;
        // Perform local search
        for(int q=start;q<n+start;q++) {SequenceVectorIndividual ind = (SequenceVectorIndividual)inds[q];
        List<Service> bestNeighbour ;
		ind.calculateSequenceFitness(init.numLayers, init.endServ, init, state, true, true);

        List<Service> servicesToConsider = new ArrayList<Service>(ind.genome);
    	servicesToConsider.add(init.endServ);


    	for(int p=1; p<servicesToConsider.size()-1;p++)
		{d=communication.ComunicationCostandTime(servicesToConsider.get(p).ID ,servicesToConsider.get(p+1).ID)[0];
			if(Double.compare(d, maxdistance)>0)
		maxdistance=d;
			index1=p;

			}
    	servicesToConsider.get(index1);

     
        		Set<Service> predecessorspathlocalsearch = findPredecessors(init, servicesToConsider.get(index1));
        		List<Service> listpredecessorslocalsearch=new ArrayList<>(predecessorspathlocalsearch);

        		
        		SequenceVectorIndividual neighbour = new SequenceVectorIndividual();
            	neighbour.genome = new ArrayList<Service>();
            	
            	SequenceVectorIndividual neighbourpath = new SequenceVectorIndividual();
            	neighbourpath.genome = new ArrayList<Service>();
            	SequenceVectorIndividual neighbourpath2 = new SequenceVectorIndividual();
            	neighbourpath2.genome = new ArrayList<Service>();
            	
            	Set<Service> predecessors = findPredecessors(init, servicesToConsider.get(index1+1));
        		List<Service> listpredecessors=new ArrayList<>(predecessors);
            	
    if(state.generation>0)  {
    	 someNumber+=servicesToConsider.size();
  //  	 System.out.print("sizeofNeighbour");
 //   	 System.out.println(someNumber);

    	 countNumber++;
 //   	 System.out.print("countNumber");

   // 	 System.out.println(countNumber);

    	neighbour.genome.clear();
		neighbour.genome.addAll(servicesToConsider.subList(0, index1+2));
		neighbour.genome.addAll(listpredecessors);
		neighbour.genome.addAll(servicesToConsider.subList(index1+1, servicesToConsider.size()));
		
		
		neighbourpath.genome.clear();
		neighbourpath.genome.addAll(servicesToConsider.subList(0, index1));
		neighbourpath.genome.addAll(listpredecessorslocalsearch);
		neighbourpath.genome.addAll(servicesToConsider.subList(index1+2, servicesToConsider.size()));
    		Collections.shuffle(listpredecessorslocalsearch, init.random);

		neighbourpath2.genome.clear();
		neighbourpath.genome.addAll(servicesToConsider.subList(0, index1));
		neighbourpath.genome.addAll(listpredecessorslocalsearch);
		neighbourpath.genome.addAll(servicesToConsider.subList(index1+2, servicesToConsider.size()));
      		//neighbour.genome.addAll(listpredecessors);
            	//	neighbour.genome.addAll(servicesToConsider.subList(index1+1, servicesToConsider.size()));
        	
                		Collections.shuffle(listpredecessors, init.random);
                	//	Collections.shuffle(predecessorspathlocalsearch, init.random);

                	LinkedList<Service> extras = new LinkedList<Service>(init.relevant);
                    
                	
                //	Collections.shuffle(extras, init.random);
                  //  	neighbour.genome.addAll(0,extras);
                    	
                  //  	neighbourpath.genome.addAll(0,extras);
                 //   	Collections.shuffle(extras, init.random);
                  //  	neighbourpath2.genome.addAll(0,extras);

                    	
                    	

                   // 	neighbour.genome.addAll(0,extras);
                	

                          //	ind.genome.addAll(extras);
                		// Calculate fitness, and update the best neighbour if necessary
                		neighbour.calculateSequenceFitness(init.numLayers, init.endServ, init, state, true, true);

                		neighbourpath.calculateSequenceFitness(init.numLayers, init.endServ, init, state, true, true);
                		neighbourpath2.calculateSequenceFitness(init.numLayers, init.endServ, init, state, true, true);
if(neighbourpath2.fitness.fitness() > neighbourpath.fitness.fitness()) {
neighbourpath.genome=neighbourpath2.genome;
neighbourpath.fitness=neighbourpath2.fitness;
}
                		 double random= Math.random();
                		 if (neighbourpath.fitness.fitness() >= neighbour.fitness.fitness() && neighbourpath.fitness.fitness() >= ind.fitness.fitness()) { 
                			 countSecondLS++;
                    		 System.out.print("countSecondLS=");

                    		 //System.out.println(countSecondLS);
                		 System.out.println(neighbourpath.fitness.fitness());
                		 
                		 System.out.print("ind=");

                  		System.out.println(ind.fitness.fitness());
                 	    ind.genome=neighbourpath.genome;
                 	    ind.fitness=neighbourpath.fitness;
               	
                 		//System.out.println(inds[q].fitness.fitness());


                 	    //	int count = 0;
                 	 //   	while(count != WSCInitializer.numMutations && !extras.isEmpty()) {
                 	    	//	Service next = extras.poll();
//                 	    		//ind.genome.add(0, next);
                 	    //	}
                 	    //	ind.genome.addAll(extras);
                 	        ind.evaluated=false;
                		 }
                		 else
                		 {     		 if (neighbour.fitness.fitness() >= neighbourpath.fitness.fitness() && neighbour.fitness.fitness() >= ind.fitness.fitness())  
 
                		 { countFirstLS++;
                //		 System.out.print("firstLS=");
                	//	 System.out.println(countFirstLS);

                		 
                		// System.out.println("LSS");
                    		 
                    	//	 System.out.println(neighbourpath.fitness.fitness());
                     	    ind.genome=neighbour.genome;
                     	    ind.fitness=neighbour.fitness;
                     	//	System.out.println(ind.fitness.fitness());
                     		//System.out.println(inds[q].fitness.fitness());


                     	    //	int count = 0;
                     	 //   	while(count != WSCInitializer.numMutations && !extras.isEmpty()) {
                     	    	//	Service next = extras.poll();
//                     	    		//ind.genome.add(0, next);
                     	    //	}
                     	    //	ind.genome.addAll(extras);
                     	        ind.evaluated=false;
                    		 }else {ind.genome.addAll(0,extras); 
                    		 countMu++;
                    	//	 System.out.print("mut=");
                    	//	 System.out.println(countMu);

                     		ind.calculateSequenceFitness(init.numLayers, init.endServ, init, state, true, true);

                 	        ind.evaluated=false;

                    		 }
    }	
    
    
    /** if (neighbour.fitness.fitness() > ind.fitness.fitness() ) {
                					System.out.println(ind.fitness.fitness());
        	 System.out.println(neighbour.fitness.fitness());
        	    ind.genome=neighbour.genome;
        	    ind.fitness=neighbour.fitness;
        	//	System.out.println(ind.fitness.fitness());
        		//System.out.println(inds[q].fitness.fitness());


        	    //	int count = 0;
        	 //   	while(count != WSCInitializer.numMutations && !extras.isEmpty()) {
        	    	//	Service next = extras.poll();
//        	    		//ind.genome.add(0, next);
        	    	}
        
        	  
        	    //	ind.genome.addAll(extras);
        	        ind.evaluated=false;          			

              	}
        **/
      
                			/**	 servicesToConsider = new ArrayList<Service>(ind.genome);
                		    	servicesToConsider.add(init.endServ);

                		 

                		    	for(int p=1; p<servicesToConsider.size()-1;p++)
                				{d=communication.ComunicationCostandTime(servicesToConsider.get(p).ID ,servicesToConsider.get(p+1).ID)[0];
                					if(Double.compare(d, maxdistance)>0)
                				maxdistance=d;
                					index1=p;

                					}
                		    	servicesToConsider.get(index1);

                		     
                		        		predecessors = findPredecessors(init, servicesToConsider.get(index1));
                		        		 listpredecessors=new ArrayList<>(predecessors);
                		        		
                				neighbour.genome.clear();
                				neighbour.genome.addAll(servicesToConsider.subList(0, index1+1));
                				neighbour.genome.addAll(listpredecessors);
                				neighbour.genome.addAll(servicesToConsider.subList(index1, servicesToConsider.size()));
                		      		neighbour.genome.addAll(listpredecessors);
                		            		neighbour.genome.addAll(servicesToConsider.subList(index1, servicesToConsider.size()));
                		        	
                		                		Collections.shuffle(listpredecessors, init.random);

                		                		//LinkedList<Service> extras = new LinkedList<Service>(init.relevant);
                		                    	Collections.shuffle(extras, init.random);
                		                    //	ind.genome.addAll(extras);
                		                    	neighbour.genome.addAll(0,extras);
                		                		// Calculate fitness, and update the best neighbour if necessary
                		                		neighbour.calculateSequenceFitness(init.numLayers, init.endServ, init, state, true, true);
                		                		 
                		                				if (neighbour.fitness.fitness() > ind.fitness.fitness()) {
                		                					

                		                					System.out.println(ind.fitness.fitness());
                		        	 System.out.println(neighbour.fitness.fitness());
                		        	    ind.genome=neighbour.genome;
                		        	    ind.fitness=neighbour.fitness;
                		        		System.out.println(ind.fitness.fitness());
                		        		System.out.println(inds[q].fitness.fitness());


                		        	    //	int count = 0;
                		        	 //   	while(count != WSCInitializer.numMutations && !extras.isEmpty()) {
                		        	    	//	Service next = extras.poll();
//                		        	    		//ind.genome.add(0, next);
                		        	  //  	}
                		        	    //	ind.genome.addAll(extras);
                		        	        ind.evaluated=false;
//
                		            				
                		           

                		            			
                		            			

                		               	}
                		        		**/
        		
    	
    	//ind.genome.addAll(0,extras);
    	//ind.genome.addAll(index1,extras);
                	//	bestNeighbour = new ArrayList<Service>(neighbour.genome);
   
        
   
        
}
        }
        }
        return n;
	}

	public Set<Service> findPredecessors(WSCInitializer init, Service s) {
		Set<Service> predecessors = new HashSet<Service>();

		// Get only inputs that are not subsumed by the given composition inputs (i.e. the start node)
		Set<String> inputsNotSatisfied = init.getInputsNotSubsumed(s.getInputs(), init.startServ.outputs);
		Set<String> inputsToSatisfy = new HashSet<String>(inputsNotSatisfied);

		// If start node is one of the predecessors, add it to set
		if (inputsToSatisfy.size() < s.getInputs().size())
			predecessors.add(init.startServ);

		// Randomly find services to satisfy all remaining inputs
		for (String i : inputsNotSatisfied) {
			if (inputsToSatisfy.contains(i)) {
				List<Service> candidates = init.taxonomyMap.get(i).servicesWithOutput;
				Collections.shuffle(candidates, init.random);

				Service chosen = null;
				candLoop:
				for(Service cand : candidates) {
					if (init.relevant.contains(cand) && cand.layer < s.layer) {
						predecessors.add(cand);
						chosen = cand;
						break candLoop;
					}
				}

				inputsToSatisfy.remove(i);

				// Check if other outputs can also be fulfilled by the chosen candidate, and remove them also
				Set<String> subsumed = init.getInputsSubsumed(inputsToSatisfy, chosen.outputs);
				inputsToSatisfy.removeAll(subsumed);
			}
		}
		return predecessors;
	}
}
