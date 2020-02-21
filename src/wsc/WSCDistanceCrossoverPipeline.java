package wsc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
public class WSCDistanceCrossoverPipeline extends BreedingPipeline {
	public static int LCScrossover=1;
	public static int indexcrossover=0;
	public static int distancecrossover=0;
	public static int combinationDistanceIndexcrossover=0;
	public static int FixedLengthLCS=0;
	public static int twopoint=0;

		private static final long serialVersionUID = 1L;

		@Override
		public Parameter defaultBase() {
			return new Parameter("wsccrossoverpipeline");
		}

		@Override
		public int numSources() {
			return 2;
		}

		@Override
		public int produce(int min, int max, int start, int subpopulation,
				Individual[] inds, EvolutionState state, int thread) {

			WSCInitializer init = (WSCInitializer) state.initializer;

			Individual[] inds1 = new Individual[inds.length];
			Individual[] inds2 = new Individual[inds.length];

			int n1 = sources[0].produce(min, max, 0, subpopulation, inds1, state, thread);
			int n2 = sources[1].produce(min, max, 0, subpopulation, inds2, state, thread);

	        if (!(sources[0] instanceof BreedingPipeline)) {
	            for(int q=0;q<n1;q++)
	                inds1[q] = (Individual)(inds1[q].clone());
	        }

	        if (!(sources[1] instanceof BreedingPipeline)) {
	            for(int q=0;q<n2;q++)
	                inds2[q] = (Individual)(inds2[q].clone());
	        }

	        if (!(inds1[0] instanceof SequenceVectorIndividual))
	            // uh oh, wrong kind of individual
	            state.output.fatal("WSCCrossoverPipeline didn't get a SequenceVectorIndividual. The offending individual is in subpopulation "
	            + subpopulation + " and it's:" + inds1[0]);

	        if (!(inds2[0] instanceof SequenceVectorIndividual))
	            // uh oh, wrong kind of individual
	            state.output.fatal("WSCCrossoverPipeline didn't get a SequenceVectorIndividual. The offending individual is in subpopulation "
	            + subpopulation + " and it's:" + inds2[0]);

	        int nMin = Math.min(n1, n2);
int index1=0,index2=0,index12, index22;
double maxdistance=0.0,d;
	        // Perform crossover
	        for(int q=start,x=0; q < nMin + start; q++,x++) {
	    		SequenceVectorIndividual t1 = ((SequenceVectorIndividual)inds1[x]);
	    		SequenceVectorIndividual t2 = ((SequenceVectorIndividual)inds2[x]);
	    		//if(t1.clone().equals(t2.clone())) {
	    			//System.out.println("DD");}
	    		List<Service> newGenome1 = new ArrayList<Service>();
	    		List<Service> newGenome2 = new ArrayList<Service>();


if(distancecrossover==1) {
	    		//find the index number for each cut based on the distance
	    		for(int p=1; p<t1.genome.size()-1;p++)
	    		{d=communication.ComunicationCostandTime(t1.genome.get(p).ID ,t1.genome.get(p+1).ID)[0];
	    			if(Double.compare(d, maxdistance)>0)
	    		maxdistance=d;
	    			index1=p;

	    			}
	    		maxdistance=0.0;
	    		for(int p=1; p<t2.genome.size()-1;p++)
	    		{d=communication.ComunicationCostandTime(t2.genome.get(p).ID ,t2.genome.get(p+1).ID)[0];
	    			if(Double.compare(d, maxdistance)>0)
	    		maxdistance=d;
	    			index2=p;
	    		}
	    		List<Service> genome1prefix = t1.genome.subList(0, index1);
	    		List<Service> genome1suffix = t1.genome.subList(index1, t1.genome.size());
	    		List<Service> genome2prefix = t2.genome.subList(0, index2);
	    		List<Service> genome2suffix = t2.genome.subList(index2, t2.genome.size());

	    		// Create new genomes, by appending other candidate's prefix
	    		newGenome1.addAll(genome2prefix);
	    		newGenome1.addAll(t1.genome);
	    		newGenome1.addAll(genome2suffix);
	    		newGenome2.addAll(genome1prefix);
	    		newGenome2.addAll(t2.genome);
	    		newGenome2.addAll(genome1suffix);
	    //System.out.println(t1.genome.size());
//	    	System.out.println(t2.genome.size());

}
else if(LCScrossover==1) {
	    		//find the index number for each cut based on the Longest common subsequence
int[] indices=new int[3];
try {
	indices=LCS(t1.genome,t2.genome,t1.genome.size(),t2.genome.size());
} catch (CloneNotSupportedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}

if(indices[2]==t1.genome.size()&& indices[2]==t2.genome.size()) {newGenome1.addAll(t1.genome);
List<Service> genomee = new ArrayList<Service>(init.relevantList);Collections.shuffle(genomee, init.random);

newGenome2.addAll(genomee);

}
else
 {
//	System.out.println("HH");
List<Service> gp1=t1.genome.subList(0, indices[0]);
List<Service> gp2=t2.genome.subList(0, indices[1]);

List<Service> gs1=t1.genome.subList(indices[0]+indices[2], t1.genome.size());
List<Service> gs2=t2.genome.subList(indices[1]+indices[2], t2.genome.size());
List<Service> LCS=t1.genome.subList(indices[0],indices[0]+indices[2]);

	//if(t1.genome.size()==0 || t2.genome.size()==0) {}
//	else {

	//System.out.println("DD");}

	if(gp1.size()==0 && gs2.size()==0)
	{
		//System.out.print("1"+t1.genome.size());
		//System.out.print("2"+t2.genome.size());
		//System.out.print(indices[2]);


		gp1=t1.genome.subList(init.random.nextInt(t1.genome.size()-indices[2]+1)+indices[2], t1.genome.size());
gs2=t2.genome.subList(indices[1], t2.genome.size());
}
if(gp2.size()==0 && gs1.size()==0)	{ gp2=t2.genome.subList(init.random.nextInt(t2.genome.size()-indices[2]+1)+indices[2], t2.genome.size());
gs1=t1.genome.subList(indices[0], t1.genome.size());

}

newGenome1.addAll(gp1);newGenome1.addAll(LCS);newGenome1.addAll(gs2);
newGenome2.addAll(gp2);newGenome2.addAll(LCS);newGenome2.addAll(gs1);
LinkedList<Service> extras = new LinkedList<Service>(init.relevant);
Collections.shuffle(extras, init.random);
newGenome1.addAll(0,extras);
newGenome2.addAll(0,extras);

//if(newGenome1.size()==0 ||newGenome2.size()==0) {System.out.print(gp1.size());System.out.print(gp2.size());System.out.print(gs1.size());System.out.print(gs2.size());}

//}
}
//else {System.out.println("dd");}
//else {System.out.println("dd");}
}
else if(indexcrossover==1) {
	if(t1.genome.size()!=0 && t2.genome.size()!=0) {
	// Select a random index number for each cut
	 index1 = init.random.nextInt(t1.genome.size());
	 index2 = init.random.nextInt(t2.genome.size());
	    		// Cut the genomes
	    		List<Service> genome1prefix = t1.genome.subList(0, index1);
	    		List<Service> genome1suffix = t1.genome.subList(index1, t1.genome.size());
	    		List<Service> genome2prefix = t2.genome.subList(0, index2);
	    		List<Service> genome2suffix = t2.genome.subList(index2, t2.genome.size());

	    		// Create new genomes, by appending other candidate's prefix
	    		newGenome1.addAll(genome2prefix);
	    		newGenome1.addAll(t1.genome);
	    		newGenome1.addAll(genome2suffix);
	    		newGenome2.addAll(genome1prefix);
	    		newGenome2.addAll(t2.genome);
	    		newGenome2.addAll(genome1suffix);
	}
	    	//System.out.println(t1.genome.size());
	    	//System.out.println(t2.genome.size());
}
else if(combinationDistanceIndexcrossover==1)
	{

	if(t1.genome.size()==t2.genome.size()) {

	//	System.out.println("hh");
		newGenome1.addAll(t1.genome);

	List<Service> genomee = new ArrayList<Service>(init.relevantList);Collections.shuffle(genomee, init.random);
	newGenome2.addAll(genomee);}
	else
{

	for(int p=1; p<t1.genome.size()-1;p++)
	{d=communication.ComunicationCostandTime(t1.genome.get(p).ID ,t1.genome.get(p+1).ID)[0];
		if(Double.compare(d, maxdistance)>0)
	maxdistance=d;
		index1=p;

		}
	maxdistance=0.0;
	for(int p=1; p<t2.genome.size()-1;p++)
	{d=communication.ComunicationCostandTime(t2.genome.get(p).ID ,t2.genome.get(p+1).ID)[0];
		if(Double.compare(d, maxdistance)>0)
	maxdistance=d;
		index2=p;
	}
	List<Service> genome1prefix = t1.genome.subList(0, index1);
	List<Service> genome1suffix = t1.genome.subList(index1, t1.genome.size());
	List<Service> genome2prefix = t2.genome.subList(0, index2);
	List<Service> genome2suffix = t2.genome.subList(index2, t2.genome.size());

	newGenome1.addAll(genome2prefix);
	newGenome1.addAll(t1.genome);
	newGenome1.addAll(genome2suffix);
	newGenome2.addAll(genome1prefix);
	newGenome2.addAll(t2.genome);
	newGenome2.addAll(genome1suffix);
}
	        }
else if(FixedLengthLCS==1)

{
	int[] indices=new int[3];
	try {
		indices=LCS(t1.genome,t2.genome,t1.genome.size(),t2.genome.size());
	} catch (CloneNotSupportedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	for(int p=0; p<indices[2]-1;p++)
	{d=communication.ComunicationCostandTime(t1.genome.get(p+indices[0]).ID ,t1.genome.get(p+indices[0]+1).ID)[0];
		if(Double.compare(d, maxdistance)>0)
	maxdistance=d;
		index1=p;
		}
	maxdistance=0.0;
	for(int p=0; p<indices[2]-1;p++)
	{d=communication.ComunicationCostandTime(t2.genome.get(p+indices[1]).ID ,t2.genome.get(p+indices[1]+1).ID)[0];
		if(Double.compare(d, maxdistance)>0)
	maxdistance=d;
		index2=p;
	}

	List<Service> genome1prefix = t1.genome.subList(0, index1);
	List<Service> genome1suffix = t1.genome.subList(index1, t1.genome.size());
	List<Service> genome2prefix = t2.genome.subList(0, index2);
	List<Service> genome2suffix = t2.genome.subList(index2, t2.genome.size());

	newGenome1.addAll(genome2prefix);
	newGenome1.addAll(t1.genome);
	newGenome1.addAll(genome2suffix);
	newGenome2.addAll(genome1prefix);
	newGenome2.addAll(t2.genome);
	newGenome2.addAll(genome1suffix);



}
else if(twopoint==1)
{

	if(t1.genome.size()!=0 && t2.genome.size()!=0) {
	 index1 = init.random.nextInt(t1.genome.size());
	 index12 = init.random.nextInt(t1.genome.size());
	 index22 = init.random.nextInt(t2.genome.size());
	 index2 = init.random.nextInt(t2.genome.size());
	    		// Cut the genomes
	    		List<Service> genome1prefix1 = t1.genome.subList(0, Math.min(index1,index12));
	    		List<Service> genome1prefix2 = t1.genome.subList(Math.min(index1,index12),Math.max(index1,index12));
	    		List<Service> genome1prefix3 = t1.genome.subList(Math.max(index1,index12),t1.genome.size());

	    		List<Service> genome2prefix1= t2.genome.subList(0, Math.min(index2,index22));
	    		List<Service> genome2prefix2 = t2.genome.subList(Math.min(index2,index22),Math.max(index2,index22));
	    		List<Service> genome2prefix3 = t2.genome.subList(Math.max(index2,index22),t2.genome.size());

	    		// Create new genomes, by appending other candidate's prefix
	    		newGenome1.addAll(genome1prefix1);
	    		newGenome1.addAll(genome2prefix1);
	    		newGenome1.addAll(genome1prefix2);
	    		newGenome1.addAll(genome2prefix2);
	    		newGenome1.addAll(genome1prefix3);
	    		newGenome1.addAll(genome2prefix3);


	    		newGenome2.addAll(genome1prefix3);

	    		newGenome2.addAll(genome2prefix1);
	    		newGenome2.addAll(genome1prefix1);
	    		newGenome2.addAll(genome2prefix2);
	    		newGenome2.addAll(genome1prefix2);
	    		newGenome2.addAll(genome2prefix3);
	}

}

// Replace the old genomes with the new ones
	    		t1.genome = newGenome1;
	    		t2.genome = newGenome2;

		        inds[q] = t1;
		        inds[q].evaluated=false;

		        if (q+1 < inds.length) {
		        	inds[q+1] = t2;
		        	inds[q+1].evaluated=false;
		        }
		       
	        }
	        return n1;
		}


		public int[] LCS(List<Service> List1, List<Service> List2, int l1, int l2) throws CloneNotSupportedException
		{
			//if(l1==0 || l2==0)	System.out.println("11");

			int[] indices= new int[3];

         int[][] arr = new int[l1 + 1][l2 + 1];
         int r,c,i;

        for ( r = 0; r<=l1; r++)
        {
            for ( c=0; c<=l2; c++)
            {
            	if(r==0 || c==0) {arr[r][c]=0;}
            	else if (List1.get(r-1).name.equals(List2.get(c-1).name))
                   arr[r][c] = arr[r-1][c-1] + 1;
                else {
                    arr[r][c] = Math.max(arr[r-1][c], arr[r][c-1]);
            }
        }
        }
        r=l1;
        c=l2;
        i=arr[r][c]; indices[2]=i;

       // List<Service> LCSstring=new ArrayList<Service>(i);
      Service[] LCSstring=new Service[i];

        //StringBuffer sb = new StringBuffer();
        while (r>0 && c>0)
        {

            if (List1.get(r-1).name.equals(List2.get(c-1).name))
            {

               // sb.append(List1.get(i).name);
            	LCSstring[i-1]=(Service)(List1.get(r-1)).clone();
            	i--;
            	c--;
            	r--;


            }
            else if (arr[r-1][c] >= arr[r][c-1])
		{r--;
}
            else
                {c--; }
        }
        List<Service> LLCS  = new ArrayList<Service>(Arrays.asList(LCSstring));

       if(indices[2]!=0)
       {
        indices[0]=List1.indexOf(LLCS.get(0));
        indices[1]=List2.indexOf(LLCS.get(0));
       }
       else

       {   indices[1]=0;indices[0]=0;}
//System.out.println(indices[2]);
if (indices[2]==6 && l1==6) {
	//System.out.println("He");

}
		return indices;

		}


	}

