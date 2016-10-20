/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package turinggeneticalg;
import java.util.*;

/**
 *
 * @author tania
 */
public class TuringMachine {
    
    public int maxStates;
    final int stateCodeLength = 16;
    final int transitionLength = 8;
    final int stateLength = 6;
    final int writeMoveLength = 1;
    
    public TuringMachine() {
        maxStates = 64;
    }
    
    public TuringMachine(int x) {
        maxStates = x;
    }
    
    class Instruction {
        int currentStateNumber;
        int readSymbol; // true is 1, false is 0
        int newStateNumber;
        int writeSymbol; // true is 1, false is 0
        char moveDirection; //L is left, R is right

        boolean isHalt()
        {
            return (currentStateNumber == maxStates);
        }
    }
    
    /**
     *
     * @param tape
     * @return Instruction[]
     */
    private Instruction[] readCode(String tmCode){
        Instruction[] instructions = new Instruction[maxStates*2];
        int start;
        int end;
        for(int i=0; i<64*2; i++){
            //start with 0 instruction
            Instruction inst = new Instruction();
            inst.readSymbol = 0;
            inst.currentStateNumber = i;
            start = i*transitionLength;
            end = start + stateLength;
            inst.newStateNumber = Integer.parseInt(
                                    tmCode.substring(start,
                                    end),2);
            start = end;
            end = start + writeMoveLength;
            inst.writeSymbol = Integer.parseInt(tmCode.substring(start,
                                end));
            start = end;
            end = start + writeMoveLength;
            inst.moveDirection = (tmCode.substring(start,
                                    end).equals("1")) ? 'R' : 'L';
            instructions[i]=inst;
        }
        return instructions;
    }
    
    private int[] initTape(int tapeSize){
        int[] tape = new int[tapeSize];
        Arrays.fill(tape, 0);
        return tape;
    }
    
    public String simulate(String tmCode, int tapeSize, 
                           int maxIters, boolean returnStates) {
        Instruction[] instructions = readCode(tmCode);
        boolean halt = false;
        int[] tape = initTape(tapeSize);
        int position =  tapeSize/2;
        int state = 0;
        int numIters = 0;
        ArrayList<Integer> usedStates = new ArrayList<Integer>();
        while (!halt){
            usedStates.add(state);
            //System.out.print("position " );
            //System.out.println(position);
            numIters += 1;
            Instruction inst;
            int tapeValue = tape[position];
            //System.out.print("tape value " );
            //System.out.println(tapeValue);
            if (tapeValue == 0){
                inst = instructions[state*2];
            }else{
                inst = instructions[state*2 + 1];
            }
            //System.out.print("write symbol " );
            //System.out.println(inst.writeSymbol);
            tape[position] = inst.writeSymbol;
            position = (inst.moveDirection == 'L') ? position-1: position+1;
            //System.out.print("moveDirection " );
            //System.out.println(inst.moveDirection);
            if (position < 0 || position >= tapeSize ||
                numIters > maxIters || inst.isHalt())
            {
                halt = true;
            }
            state = inst.newStateNumber;
        }
        
        if (returnStates){
            StringBuilder sb = new StringBuilder(usedStates.size());
            for (int i : usedStates) {
              sb.append(i);
              sb.append(" ");
            }
            return sb.toString();
        }else{
            StringBuilder sb = new StringBuilder(tape.length);
            for (int i : tape) {
              sb.append(i);
            }
            return sb.toString();
        }
    }
    
    
    
}
