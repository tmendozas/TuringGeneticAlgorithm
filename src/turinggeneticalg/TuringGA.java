/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package turinggeneticalg;
import java.util.Random;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;



/**
 *
 * @author tania
 */
public class TuringGA {
    
    public String objectiveTape;
    public double crossProbability;
    public double mutationProbability;
    public int populationSize;
    private static int maxIter = 1000;
    private static TuringMachine tm = new TuringMachine();
    public static int tapeSize, tmMaxIters;
    private static Random randGenerator;
    
    public TuringGA(String tape){
        objectiveTape = tape;
        crossProbability = 0.95;
        mutationProbability = 0.01;
        populationSize = 20;
        randGenerator = new Random();
    }
    
    public TuringGA(String tape, double pc, double pm, int n,
                    int ts, int tmax,int seed){
        objectiveTape = tape;
        crossProbability = pc;
        mutationProbability = pm;
        populationSize = n;
        tapeSize = ts;
        tmMaxIters = tmax;
        randGenerator = new Random(seed);
    }

    public String generateRandomCode(){
        String[] code = new String[1024];
        for(int i=0; i<1024; i++){
            code[i] = String.valueOf(randGenerator.nextInt(2));
        }
        return String.join("", code);
    }
    
    public Individual crossOver(Individual xx, Individual yy){
        String x = xx.code;
        String y = yy.code;
        int geneLength = x.length();
        String xFirstHalf = x.substring(0, geneLength / 2);
        String ySecondHalf = y.substring(geneLength / 2);
        StringBuilder sb = new StringBuilder();
        sb.append(xFirstHalf);
        sb.append(ySecondHalf);
        Individual crossed = new Individual(sb.toString());
        return crossed;
    }
    
    public String mutate(String x){
        int mutationPositon = randGenerator.nextInt(1024);
        char currentValue = x.charAt(mutationPositon);
        StringBuilder sb = new StringBuilder(x);
        char newValue = (currentValue=='0') ? '1':'0';
        sb.setCharAt(mutationPositon, newValue);
        return sb.toString();
    }
    
    public static String asciiToBinary(String s){
        byte[] bytes = s.getBytes();
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes)
        {
           int val = b;
           for (int i = 0; i < 8; i++)
           {
              binary.append((val & 128) == 0 ? 0 : 1);
              val <<= 1;
           }
        }
        return binary.toString();
    }
    
    public static String trimZeros(String str,boolean leaveFirst, 
                                   boolean leaveLast){
        int startIndex = str.indexOf('1');
        int endIndex = str.lastIndexOf('1');
        if (startIndex==-1){
            return "";
        }
        if (leaveFirst && leaveLast){
            startIndex = Math.max(0,startIndex-1);
            endIndex = Math.min(str.length(), endIndex+2);
            return str.substring(startIndex, endIndex);
        }else if(leaveFirst){
            startIndex = Math.max(0,startIndex-1);
            endIndex = Math.min(str.length(), endIndex+1);
            return str.substring(startIndex, endIndex);
        }else if(leaveLast){
            startIndex = Math.max(0,startIndex);
            endIndex = Math.min(str.length(), endIndex+2);
            return str.substring(startIndex, endIndex);
        }else{
            return str.substring(startIndex, endIndex+1);         
        }            
    }
    
    public double fitness(String machineCode){
        String x = tm.simulate(machineCode, tapeSize, tmMaxIters, false);
        int objTapeLen = objectiveTape.length();
        //Obtain relevant subset of output tape
        boolean leaveFirst = (objectiveTape.charAt(0)=='0');
        boolean leaveLast = (objectiveTape.charAt(objTapeLen-1)=='0');
        String testTape = trimZeros(x,leaveFirst,leaveLast);
        int testTapeLen = testTape.length();
        
        // Calculate bitwise fitness
        double fitness = 0;
        if (testTapeLen == objTapeLen){
            // Do bitwise comparing
            for(int i=0; i<objTapeLen; i++){
                if (testTape.charAt(i) == objectiveTape.charAt(i))
                    fitness ++;           
            }
            return fitness/objTapeLen;
        }else if(testTape.contains(objectiveTape)){
            // If tapes are contained, consider them almost equal
            return 0.95;
        }else{
            // Center and do bitwise comparing from center-left and center-right
            int testTapeCenter = (int) Math.ceil(testTapeLen/2);
            int objTapeCenter = (int) Math.ceil(objTapeLen/2);
            // Check left:
            String testTapeLeft = testTape.substring(0,testTapeCenter);
            String objTapeLeft = objectiveTape.substring(0,objTapeCenter);
            int minCenter = Math.min(testTapeCenter, objTapeCenter);
            for(int i=1; i<minCenter; i++){
                if(testTapeLeft.charAt(testTapeCenter-i) == objTapeLeft.charAt(objTapeCenter-i))
                    fitness++;
            } 
            // Check right:
            String testTapeRight = testTape.substring(testTapeCenter);
            String objTapeRight = objectiveTape.substring(objTapeCenter);
            for(int i=0; i<minCenter; i++){
                if(testTapeRight.charAt(i) == objTapeRight.charAt(i))
                    fitness++;
            }
            return (fitness/objTapeLen) * (Math.min(testTapeLen,objTapeLen)/Math.max(testTapeLen,objTapeLen));
        }
    }
    
    public String[] generateFirstGeneration(int size){
        String[] population = new String[size];
        for(int i=0; i<size; i++){
            population[i] = generateRandomCode();
        }
        return population;
    }
    
    public static boolean getBinomial(double p) {
        int x = 0;
        if(Math.random() < p)
            x++;
        return x==1;
    }
    
    public Individual[] concat(Individual[] a, Individual[] b) {
        Individual[] c = new Individual[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
     }

    
    public static void main(String[] args) throws IOException {
        String inputString;
        int n,seed;
        double pc, pm;
        
        BufferedReader ISR=new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Seed (integer > 0):");
        seed=Integer.parseInt(ISR.readLine());
        System.out.println("Objective tape (String):");
        inputString=ISR.readLine();
        System.out.println("Population Size:");
        n = Integer.parseInt(ISR.readLine());
        System.out.println("Number of Generations:");
        maxIter = Integer.parseInt(ISR.readLine());
        System.out.println("Crossing Probability (~ 0.95):");
        pc = Double.parseDouble(ISR.readLine());
        System.out.println("Mutation Probability (~ 0.01):");
        pm = Double.parseDouble(ISR.readLine());
        System.out.println(
            "Turing Machine MAX Transitions (integer > 0):");
        tmMaxIters = Integer.parseInt(ISR.readLine());
        System.out.println("Turing Machine Tape Length (integer > 0):");
        tapeSize = Integer.parseInt(ISR.readLine());
        
        TuringGA test = new TuringGA(asciiToBinary(inputString),pc,pm,
                                     n,tapeSize,tmMaxIters,seed);
       
        String[] strPopulation = test.generateFirstGeneration(n);
        Individual[] population = new Individual[n];
        // Fill out first population
        for(int i=0; i<n; i++){
            Individual ind = new Individual(strPopulation[i]);
            ind.fitness = test.fitness(ind.code);
            population[i] = ind;
            
        }
        Arrays.sort(population);
        Individual[] crossed = new Individual[n];
        Individual[] parentsSons = new Individual[2*n];
        boolean found = false;
        int numIter = 0;
        while(!found && numIter < maxIter){
            // Cross with probability pc.. simulate
            //System.out.println(numIter);
            for (int i=1; i<n/2+1; i++){
                if (getBinomial(pc)){
                    Individual first = test.crossOver(population[i-1],population[n-i]);
                    first.fitness = test.fitness(first.code);
                    crossed[i-1] = first;
                    Individual second = test.crossOver(population[n-i],population[i-1]);
                    second.fitness = test.fitness(second.code);
                    crossed[n-i] = second;
                }else{
                    Individual first = population[i-1];
                    first.fitness = test.fitness(first.code);
                    crossed[i-1] = first;
                    Individual second = population[n-i];
                    second.fitness = test.fitness(second.code);
                    crossed[n-i] = second;    
                }
            }
            // Mutate at random
            for (int i=0; i<n*pm; i++){
                int mutateIndex = randGenerator.nextInt(n);
                //System.out.println(mutateIndex);
                Individual ind = crossed[mutateIndex];
                String newCode = test.mutate(ind.code);
                double newFitness = test.fitness(newCode);
                ind.setCode(newCode);
                ind.setFitness(newFitness);
            }
            // Ordenar crossed U population con fitness
            parentsSons = test.concat(population,crossed);
            Arrays.sort(parentsSons);
            found = parentsSons[0].fitness >= 1.0;
            population = Arrays.copyOfRange(parentsSons, 0, n);
            numIter++;
        }
        
        
        System.out.print("ObjectiveTapeBinary: ");
        System.out.println(asciiToBinary(inputString));
        System.out.print("FittestMachineTape: ");
        System.out.println(trimZeros(tm.simulate(parentsSons[0].code,
                                                 tapeSize, tmMaxIters, false),
                                     true,true));
        System.out.print("MaxFitness: ");
        double maxFitness = parentsSons[0].fitness;
        System.out.println(parentsSons[0].fitness);
        
        boolean inBestSubset = true;
        int i = 0;
        String usedTransitions = tm.simulate(parentsSons[i].code,
                                       tapeSize, tmMaxIters, true);
        Set<String> usedTransitionsSet = 
               new HashSet<>(Arrays.asList(usedTransitions.split("\\s")));
        int kc = usedTransitionsSet.size();
        int minKIndex = i;
        while(inBestSubset){
            i++;
            if (parentsSons[i].fitness < maxFitness){
                inBestSubset = false;
            }else{
                usedTransitions = tm.simulate(parentsSons[i].code,
                                          tapeSize, tmMaxIters, true);
                usedTransitionsSet = new HashSet<>(
                                    Arrays.asList(usedTransitions.split("\\s")));
                if (usedTransitionsSet.size() < kc){
                    kc = usedTransitionsSet.size();
                    minKIndex = i;
                }
            }
        }
        
        System.out.print("ApproximatedKolmogorovComplexity: ");
        System.out.print(usedTransitionsSet.size());
        System.out.println(" * 16");
        System.out.println(tm.simulate(parentsSons[minKIndex].code,
                                       tapeSize, tmMaxIters, true));
        
        
    }
    
}
