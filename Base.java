import java.util.*;

/**
 * Created by t_shid on 10/31/2014.
 */
public class HelloWorld {
    public static void main(String[] args){
        int size = 12;

        Random rand = new Random();

        int[] clock = new int[size];

        LinkedList<Integer> toBeFilled = new LinkedList<Integer>();
        for (int i= 0; i<size; i++){
            toBeFilled.add(i);
        }

        Collections.shuffle(toBeFilled);

        int lastIndex = toBeFilled.removeFirst();
        int num = rand.nextInt(size/2) + 1;
        clock[lastIndex] = num;

        System.out.print(lastIndex + " ");

        while(!toBeFilled.isEmpty()){
            int index = toBeFilled.removeFirst();
            num = Math.abs(lastIndex - index);

            clock[index] = num;

            lastIndex = index;
            System.out.print(lastIndex + " ");
        }

        System.out.println();
        for (int i : clock){
            System.out.print(i + " ");
        }

    }
}