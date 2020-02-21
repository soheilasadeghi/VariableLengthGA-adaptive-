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

public class WSCMutationPipeline extends BreedingPipeline {

	private static final long serialVersionUID = 1L;

	@Override
	public Parameter defaultBase() {
		return new Parameter("wscmutationpipeline");
	}

	@Override
	public int numSources() {
		return 1;
	}

	@Override
	public int produce(int min, int max, int start, int subpopulation,
			Individual[] inds, EvolutionState state, int thread) {

		int n = sources[0].produce(min, max, start, subpopulation, inds, state, thread);

        if (!(sources[0] instanceof BreedingPipeline)) {
            for(int q=start;q<n+start;q++)
                inds[q] = (Individual)(inds[q].clone());
        }

        if (!(inds[start] instanceof SequenceVectorIndividual))
            // uh oh, wrong kind of individual
            state.output.fatal("WSCMutationPipeline didn't get a SequenceVectorIndividual. The offending individual is in subpopulation "
            + subpopulation + " and it's:" + inds[start]);

        WSCInitializer init = (WSCInitializer) state.initializer;
        double d=0.0,maxdistance=0.0;
        int index1=0;
        // Perform mutation
        for(int q=start;q<n+start;q++) {
        	
        	
        	SequenceVectorIndividual ind = (SequenceVectorIndividual)inds[q];

            List<Service> servicesToConsider = new ArrayList<Service>(ind.genome);
        	servicesToConsider.add(init.endServ);

     

        	for(int p=1; p<servicesToConsider.size()-1;p++)
    		{d=communication.ComunicationCostandTime(servicesToConsider.get(p).ID ,servicesToConsider.get(p+1).ID)[0];
    			if(Double.compare(d, maxdistance)>0)
    		maxdistance=d;
    			index1=p;

    			}
        	servicesToConsider.get(index1);

         
            		Set<Service> predecessors = findPredecessors(init, servicesToConsider.get(index1));
            		List<Service> listpredecessors=new ArrayList<>(predecessors);
            		ind.genome.clear();
            		ind.genome.addAll(servicesToConsider.subList(0, index1+1));
            		ind.genome.addAll(listpredecessors);
            		ind.genome.addAll(servicesToConsider.subList(index1-2, servicesToConsider.size()));

        	LinkedList<Service> extras = new LinkedList<Service>(init.relevant);
        	Collections.shuffle(extras, init.random);
        //	ind.genome.addAll(extras);
        	ind.genome.addAll(0,extras);
        	//ind.genome.addAll(index1,extras);

       // 	int count = 0;
     //   	while(count != WSCInitializer.numMutations && !extras.isEmpty()) {
        //		Service next = extras.poll();
        		//ind.genome.add(0, next);
       // 	}
        //	ind.genome.addAll(extras);
            ind.evaluated=false;
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
