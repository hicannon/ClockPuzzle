package comx.detian.clocklibrary;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

public class ClockPuzzle {
    private int[] clock;
    private boolean[] marked;

    public int getHandCW() {
        return handCW;
    }

    public int getHandCCW() {
        return handCCW;
    }

    private int handCW = -1;
    private int handCCW = -1;

    public int getSize() {
        return size;
    }

    private int size;

    public int getCurrentIndex() {
        return currentIndex;
    }

    private int currentIndex = -1;

    public ClockPuzzle(int size){
        clock = generate(size);
        marked = new boolean[size];
        this.size = size;
        handCCW = -1;
        handCW = -1;
        currentIndex = -1;
    }

    public int get(int index){
        return clock[index];
    }

    public boolean isMarked(int index){
        return marked[index];
    }

    public boolean setMarked(int index){
        if (index<0 || index > size){
            return false;
        }

        if (handCW!=-1 && (marked[index] || (index!=handCCW && index!=handCW))){
            return false;
        }else{
            marked[index] = true;
            handCCW = mapToIndex(index - clock[index]);
            handCW = mapToIndex(index + clock[index]);
            currentIndex = index;
            return true;
        }
    }

    public int mapToIndex(int target){
        target %= size;
        if (target<0){
            target = target + size;
        }
        return target;
    }

    public void clearMarks(){
        marked = new boolean[marked.length];
        handCCW = -1;
        handCW = -1;
    }

    public boolean hasWon(){
        for(boolean b : marked){
            if (!b)
                return false;
        }
        return true;
    }

    public boolean hasLost(){
        return marked[handCCW] && marked[handCW];
    }

    public static int[] generate(int size){
        Random rand = new Random(System.currentTimeMillis());
        int[] clock = new int[size];
        LinkedList<Integer> toBeFilled = new LinkedList<Integer>();
        for (int i= 0; i<size; i++){
            toBeFilled.add(i);
        }

        Collections.shuffle(toBeFilled);

        int lastIndex = toBeFilled.removeFirst();
        int num = rand.nextInt(size/2) + 1;
        clock[lastIndex] = num;

        //System.out.print(lastIndex + " ");

        while(!toBeFilled.isEmpty()){
            int index = toBeFilled.removeFirst();
            num = Math.abs(lastIndex - index);

            clock[index] = num;

            lastIndex = index;
            //System.out.print(lastIndex + " ");
        }
        return clock;
    }
}
