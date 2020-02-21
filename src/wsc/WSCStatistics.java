package wsc;

import java.io.File;
import java.io.IOException;

import ec.EvolutionState;
import ec.Individual;
import ec.simple.SimpleShortStatistics;
import ec.util.Parameter;

/**
 *
 * @author Soheila
 */
public class WSCStatistics extends SimpleShortStatistics {

	private static final long serialVersionUID = 1L;
	public int evaluationsLog = 0; // 0 by default means stdout

    public void createEvaluationsLog( final EvolutionState state ) {
        File evaluationFile = WSCInitializer.evaluationsLogFile;
        if ( evaluationFile != null ) try {
            evaluationsLog = state.output.addLog( evaluationFile, true, false, false );
        }
        catch ( IOException i ) {
            state.output.fatal( "An IOException occurred trying to create the log " + evaluationFile + ":\n" + i );
        }
        // else we will just keep the log at 0, which is stdout
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state){
        boolean output = (state.generation % modulus == 0);

        // gather timings
        if (output && doTime)
            {
        	long time = System.currentTimeMillis()-lastTime;
        	if (state.generation == 0)
        		time += WSCInitializer.setupTime;
            state.output.print("" + time + "",  statisticslog);
            }

        int subpops = state.population.subpops.length;                          // number of supopulations
        totalIndsThisGen = new long[subpops];                                           // total assessed individuals
        bestOfGeneration = new Individual[subpops];                                     // per-subpop best individual this generation
        totalSizeThisGen = new long[subpops];                           // per-subpop total size of individuals this generation
        totalFitnessThisGen = new double[subpops];                      // per-subpop mean fitness this generation
        double[] meanFitnessThisGen = new double[subpops];                      // per-subpop mean fitness this generation


        prepareStatistics(state);

        // gather per-subpopulation statistics

        for(int x=0;x<subpops;x++)
            {
            for(int y=0; y<state.population.subpops[x].individuals.length; y++)
                {
                if (state.population.subpops[x].individuals[y].evaluated)               // he's got a valid fitness
                    {
                    // update sizes
                    long size = state.population.subpops[x].individuals[y].size();
                    totalSizeThisGen[x] += size;
                    totalSizeSoFar[x] += size;
                    totalIndsThisGen[x] += 1;
                    totalIndsSoFar[x] += 1;

                    // update fitness bashla
                    if (bestOfGeneration[x]==null ||
                        state.population.subpops[x].individuals[y].fitness.betterThan(bestOfGeneration[x].fitness))
                        {
                    	//if(bestOfGeneration[x]!=null )
                    	//{	//System.out.print(state.population.subpops[x].individuals[y].fitness.fitness()+"cur");           
                    	//System.out.print(bestOfGeneration[x].fitness.fitness()+"best"); }

                        bestOfGeneration[x] = state.population.subpops[x].individuals[y];
                        if (bestSoFar[x]==null || bestOfGeneration[x].fitness.betterThan(bestSoFar[x].fitness))
                            bestSoFar[x] = (Individual)(bestOfGeneration[x].clone());
                        }

                    // sum up mean fitness for population
                    totalFitnessThisGen[x] += state.population.subpops[x].individuals[y].fitness.fitness();

                    // hook for KozaShortStatistics etc.
                    gatherExtraSubpopStatistics(state, x, y);
                    }
                }
            // compute mean fitness stats
            meanFitnessThisGen[x] = (totalIndsThisGen[x] > 0 ? totalFitnessThisGen[x] / totalIndsThisGen[x] : 0);

            // hook for KozaShortStatistics etc.
            if (output && doSubpops) printExtraSubpopStatisticsBefore(state, x);

            // print out optional average size information
            if (output && doSize && doSubpops)
                {
                state.output.print("" + (totalIndsThisGen[x] > 0 ? ((double)totalSizeThisGen[x])/totalIndsThisGen[x] : 0) + " ",  statisticslog);
                state.output.print("" + (totalIndsSoFar[x] > 0 ? ((double)totalSizeSoFar[x])/totalIndsSoFar[x] : 0) + " ",  statisticslog);
                state.output.print("" + (double)(bestOfGeneration[x].size()) + " ", statisticslog);
                state.output.print("" + (double)(bestSoFar[x].size()) + " ", statisticslog);
                }

            // print out fitness information
            if (output && doSubpops)
                {
                state.output.print("" + meanFitnessThisGen[x] + " ", statisticslog);
                state.output.print("" + bestOfGeneration[x].fitness.fitness() + " ", statisticslog);
                state.output.print("" + bestSoFar[x].fitness.fitness() + " ", statisticslog);
                }

            // hook for KozaShortStatistics etc.
            if (output && doSubpops) printExtraSubpopStatisticsAfter(state, x);
            }



        // Now gather per-Population statistics
        long popTotalInds = 0;
        long popTotalIndsSoFar = 0;
        long popTotalSize = 0;
        long popTotalSizeSoFar = 0;
        double popMeanFitness = 0;
        double popTotalFitness = 0;
        Individual popBestOfGeneration = null;
        Individual popBestSoFar = null;

        for(int x=0;x<subpops;x++)
            {
            popTotalInds += totalIndsThisGen[x];
            popTotalIndsSoFar += totalIndsSoFar[x];
            popTotalSize += totalSizeThisGen[x];
            popTotalSizeSoFar += totalSizeSoFar[x];
            popTotalFitness += totalFitnessThisGen[x];
            if (bestOfGeneration[x] != null && (popBestOfGeneration == null || bestOfGeneration[x].fitness.betterThan(popBestOfGeneration.fitness)))
                popBestOfGeneration = bestOfGeneration[x];
            if (bestSoFar[x] != null && (popBestSoFar == null || bestSoFar[x].fitness.betterThan(popBestSoFar.fitness))) {
                popBestSoFar = bestSoFar[x];// System.out.println(bestSoFar[x].fitness.fitness()+"bestsofar");
                //System.out.println(popBestSoFar.fitness.fitness()+"popbestsofar");
            }

            // hook for KozaShortStatistics etc.
            gatherExtraPopStatistics(state, x);
            }

        // build mean
        popMeanFitness = (popTotalInds > 0 ? popTotalFitness / popTotalInds : 0);               // average out

        // hook for KozaShortStatistics etc.
        if (output) printExtraPopStatisticsBefore(state);

        // optionally print out mean size info
        if (output && doSize)
            {
            state.output.print("" + (popTotalInds > 0 ? popTotalSize / popTotalInds : 0)  + " " , statisticslog);                                           // mean size of pop this gen
            state.output.print("" + (popTotalIndsSoFar > 0 ? popTotalSizeSoFar / popTotalIndsSoFar : 0) + " " , statisticslog);                             // mean size of pop so far
            state.output.print("" + (double)(popBestOfGeneration.size()) + " " , statisticslog);                                    // size of best ind of pop this gen
            state.output.print("" + (double)(popBestSoFar.size()) + " " , statisticslog);                           // size of best ind of pop so far
            }

        // print out fitness info
        if (output)
            {
            state.output.print("" + popMeanFitness + " " , statisticslog);                                                                                  // mean fitness of pop this gen
            state.output.print("" + (popBestOfGeneration.fitness.fitness()) + " " , statisticslog);                 // best fitness of pop this gen
            state.output.print("" + (popBestSoFar.fitness.fitness()) + " " , statisticslog);                // best fitness of pop so far


            WSCInitializer.datatimeIdx = 0;
            WSCInitializer.datacostIdx = 0;
            WSCInitializer.comtimeIdx = 0;
            WSCInitializer.comcostIdx = 0;
            WSCInitializer.timeIdx = 0;
            WSCInitializer.costIdx = 0;

            state.output.print(WSCInitializer.meanDataTimePerGen[WSCInitializer.datatimeIdx++] + " ", statisticslog);
            state.output.print(WSCInitializer.meanDataCostPerGen[WSCInitializer.datacostIdx++] + " ", statisticslog);

            state.output.print(WSCInitializer.meanComTimePerGen[WSCInitializer.comtimeIdx++] + "", statisticslog);
            state.output.print(WSCInitializer.meanComCostPerGen[WSCInitializer.comcostIdx++] + " ", statisticslog);

            state.output.print(WSCInitializer.meanTimePerGen[WSCInitializer.timeIdx++] + " ", statisticslog);
            state.output.print(WSCInitializer.meanCostPerGen[WSCInitializer.costIdx++] + " ", statisticslog);

            state.output.print("" + ((SequenceVectorIndividual)popBestOfGeneration).getDTime() + " ", statisticslog);
            state.output.print("" + ((SequenceVectorIndividual)popBestOfGeneration).getDCost() + " ", statisticslog);
            state.output.print("" + ((SequenceVectorIndividual)popBestOfGeneration).getCTime() + " ", statisticslog);
            state.output.print("" + ((SequenceVectorIndividual)popBestOfGeneration).getCCost() + " ", statisticslog);
            state.output.print("" + ((SequenceVectorIndividual)popBestOfGeneration).getTime() + " ", statisticslog);
            state.output.print("" + ((SequenceVectorIndividual)popBestOfGeneration).getCost() + " ", statisticslog);

            state.output.print("" + ((SequenceVectorIndividual)popBestSoFar).getDTime() + " ", statisticslog);
            state.output.print("" + ((SequenceVectorIndividual)popBestSoFar).getDCost() + " ", statisticslog);
            state.output.print("" + ((SequenceVectorIndividual)popBestSoFar).getCTime() + " ", statisticslog);
            state.output.print("" + ((SequenceVectorIndividual)popBestSoFar).getCCost() + " ", statisticslog);
            state.output.print("" + ((SequenceVectorIndividual)popBestSoFar).getTime() + " ", statisticslog);
            state.output.print("" + ((SequenceVectorIndividual)popBestSoFar).getCost() + " ", statisticslog);

            }
        
        
        // hook for KozaShortStatistics etc.
        if (output) printExtraPopStatisticsAfter(state);

        // we're done!
        if (output) state.output.println("", statisticslog);

        if (output) {
            // Print the best candidate at the end of the run
            if (state.generation == state.parameters.getInt(new Parameter("generations"), null)-1) {
                state.output.println(((SequenceVectorIndividual)popBestSoFar).toGraphString(state), statisticslog);

                // Now let's write the evaluations log
                createEvaluationsLog(state);

                for(int i = 0; i < WSCInitializer.convergenceData.size(); i++) {
                	state.output.println(String.format("%d %f", i, WSCInitializer.convergenceData.get(i)), evaluationsLog);
                }
            }
        }
    }
}
