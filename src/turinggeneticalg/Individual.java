/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package turinggeneticalg;

/**
 *
 * @author tania
 */
public class Individual implements Comparable<Individual>{
    
    public String code;
    public double fitness;
    
    public Individual(String c){
        code = c;
    }
    
    public double getFitness() {
        return fitness;
    }
    
    public void setFitness(double f) {
        this.fitness = f;
    }
    
    public void setCode(String c) {
        this.code = c;
    }

    @Override
    public int compareTo(Individual compareIndividual) {
        double compareFitness = ((Individual) compareIndividual).getFitness();
        //ascending order
        return (int) (compareFitness * 100 - this.fitness * 100);
    }
    
}
