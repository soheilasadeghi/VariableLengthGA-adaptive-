package wsc;

import ec.*;
import ec.simple.*;

public class WSCProblem extends Problem implements SimpleProblemForm {
	private static final long serialVersionUID = 1L;

	@Override
	public void evaluate(final EvolutionState state, final Individual ind,
			final int subpopulation, final int threadnum) {
		if (ind.evaluated)
			return;

		if (!(ind instanceof SequenceVectorIndividual))
			state.output.fatal("Whoa!  It's not a SequenceVectorIndividual!!!",
					null);

		SequenceVectorIndividual ind2 = (SequenceVectorIndividual) ind;
		WSCInitializer init = (WSCInitializer) state.initializer;

		if (!(ind2.fitness instanceof SimpleFitness)) state.output.fatal("Whoa!  It's not a SimpleFitness!!!", null);


		ind2.calculateSequenceFitness(init.numLayers, init.endServ, init, state, false, true);

	}

	@Override
	public void finishEvaluating(EvolutionState state, int threadnum) {
		WSCInitializer init = (WSCInitializer) state.initializer;

		// Get population
		Subpopulation pop = state.population.subpops[0];

		//	double minAvailability = 2.0;
		//	double maxAvailability = -1.0;
		//	double minReliability = 2.0;
		//	double maxReliability = -1.0;
			double minTime = Double.MAX_VALUE;
			double maxTime = -1.0;
			double minCost = Double.MAX_VALUE;
			double maxCost = -1.0;

			double minCTime = Double.MAX_VALUE;
			double maxCTime = -1.0;
			double minCCost = Double.MAX_VALUE;
			double maxCCost = -1.0;

			double minDTime = Double.MAX_VALUE;
			double maxDTime = -1.0;
			double minDCost = Double.MAX_VALUE;
			double maxDCost = -1.0;
			// Keep track of means
		//	double meanAvailability = 0.0;
		//	double meanReliability = 0.0;
			double meanTime = 0.0;
			double meanCost = 0.0;
			double meanCTime = 0.0;
			double meanCCost = 0.0;
			double meanDTime = 0.0;
			double meanDCost = 0.0;
		// Find the normalisation bounds
			for (Individual ind : pop.individuals) {
				SequenceVectorIndividual wscInd = (SequenceVectorIndividual) ind;
				//double a = wscInd.getAvailability();
			//	double r = wscInd.getReliability();
				double ct=wscInd.getCTime();
				double dt=wscInd.getDTime();
				double cc=wscInd.getCCost();
				double dc=wscInd.getDCost();
				double t = wscInd.getTime();
				double c = wscInd.getCost();

				meanCTime += ct;
				meanCCost += cc;
				meanDTime +=dt;
				meanDCost +=dc;
				meanTime += t;
				meanCost += c;

				if (WSCInitializer.dynamicNormalisation) {
					if (dt < minDTime)
						minDTime = dt;
					if (dt > maxDTime)
						maxDTime = dt;
					if (dc < minDCost)
						minDCost = dc;
					if (dc > maxDCost)
						maxDCost = dc;

					if (ct < minCTime)
						minCTime = ct;
					if (ct > maxCTime)
						maxCTime = ct;
					if (cc < minCCost)
						minCCost = cc;
					if (cc > maxCCost)
						maxCCost = cc;

					if (t < minTime)
						minTime = t;
					if (t > maxTime)
						maxTime = t;
					if (c < minCost)
						minCost = c;
					if (c > maxCost)
						maxCost = c;
				}
			}

			WSCInitializer.meanDataTimePerGen[WSCInitializer.datatimeIdx++] = meanDTime / pop.individuals.length;
			WSCInitializer.meanDataCostPerGen[WSCInitializer.datacostIdx++] = meanDCost / pop.individuals.length;

			WSCInitializer.meanComTimePerGen[WSCInitializer.comtimeIdx++] = meanCTime / pop.individuals.length;
			WSCInitializer.meanComCostPerGen[WSCInitializer.comcostIdx++] = meanCCost / pop.individuals.length;

			WSCInitializer.meanTimePerGen[WSCInitializer.timeIdx++] = meanTime / pop.individuals.length;
			WSCInitializer.meanCostPerGen[WSCInitializer.costIdx++] = meanCost / pop.individuals.length;

			if (WSCInitializer.dynamicNormalisation) {
				// Update the normalisation bounds with the newly found values
				init.minCTime = minCTime;
				init.maxCTime = maxCTime;
				init.minCCost = minCCost;
				init.maxCCost = maxCCost;

				init.minDTime = minDTime;
				init.maxDTime = maxDTime;
				init.minDCost = minDCost;
				init.maxDCost = maxDCost;

				init.minCost = minCost;
				init.maxCost = maxCost;
				init.minTime = minTime;
				init.maxTime = maxTime;

				// Finish calculating the fitness of each candidate
				for (Individual ind : pop.individuals) {
					((SequenceVectorIndividual)ind).finishCalculatingSequenceFitness(init, state);
				}
			}

		}
	}