package algorithm;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class LongestSequence extends RecursiveTask<Result> {

	public static ForkJoinPool fj = ForkJoinPool.commonPool();
	static final int SEQUENTIAL_THRESHOLD = 1;
	int key;
	int[] arr;
	int l;
	int r;
	int cutoff;
	
	LongestSequence(int cutoff, int l, int r, int key, int[] arr) {
		this.arr = arr;
		this.key = key;
		this.r = r;
		this.l = l;
		this.cutoff = cutoff;
	}

	@Override
	protected Result compute() {
		Result res = new Result(0, 0, 0, false);
		// Run sequentially if r - l is lower than the SEQ THRS 
		if(r - l <= cutoff) {
			int cons = 0;
			int string = 0;
			for(int i = l; i <= r-1; i++) {
				if(arr[i] == key) {
					cons++;
				}
				else if(cons > 0 && cons > res.numLongest) {
					string = cons;
					cons = 0;
				}
			}
			// Calculate numLeftEdge in the array
			for(int i = l; i <= r-1; i ++) {
				if(i == r-1 && arr[i] == key) { 
					res.entireRange = true; // entireRange = true if you pass through the whole array
				}
				if(arr[i] == key) {
					res.numLeftEdge += 1;
				}
				else {
					break;
				}
			}
			// Calculate numRightEdge in the array
			for(int i = r-1; i >= l; i--) {
				if(arr[i] == key) {
					res.numRightEdge += 1;
				}
				else {
					break;
				}
			}
			// I don't know why I need this but it breaks without it
			if(cons > string) {
				res.numLongest = cons;
			}
			else {
				res.numLongest = string;
			}
			return res;
		}
		
		// Run the program in parallel
		else { 	
			int mid = l + ((r - l) / 2);
			LongestSequence left = new LongestSequence(cutoff, l, mid, key, arr);
			LongestSequence right = new LongestSequence(cutoff, mid, r, key, arr);
			left.fork();
			Result rightAns = right.compute();
			Result leftAns = left.join();
			
			int leftEdge = 0;
			int rightEdge = 0;
			int longest = 0;
			boolean range = false;
			
			// Long ass conditional statements that make sense when you write them out
			if(rightAns.entireRange && leftAns.entireRange) {
				int edge = leftAns.numLongest + rightAns.numLongest;
				leftEdge = edge;
				rightEdge = edge;
				longest = edge;
				range = true;
			}
			else if(leftAns.entireRange) {
				if(leftAns.numLongest + rightAns.numLeftEdge > leftAns.numLongest) {
					leftEdge = leftAns.numLongest + rightAns.numLeftEdge;
					rightEdge = rightAns.numRightEdge;
					longest = leftEdge;
					// don't change range bc it's already false
				}
				else if(leftAns.numLongest > rightAns.numLongest){
					leftEdge = leftAns.numLeftEdge;
					rightEdge = rightAns.numRightEdge;
					longest = leftAns.numLongest;
				}
				else {
					leftEdge = leftAns.numLeftEdge;
					rightEdge = rightAns.numRightEdge;
					longest = rightAns.numLongest;
				}
			}
			else if (rightAns.entireRange) {
				if(leftAns.numRightEdge + rightAns.numLeftEdge > rightAns.numLongest) {
					leftEdge = leftAns.numLeftEdge;
					rightEdge = leftAns.numRightEdge + rightAns.numLeftEdge;
					longest = leftAns.numRightEdge + rightAns.numLeftEdge;
					// don't change range bc it's already false
				}
				else if(leftAns.numLongest > rightAns.numLongest){
					leftEdge = leftAns.numLeftEdge;
					rightEdge = rightAns.numRightEdge;
					longest = leftAns.numLongest;
				}
				else {
					leftEdge = leftAns.numLeftEdge;
					rightEdge = rightAns.numRightEdge;
					longest = rightAns.numLongest;
				}
			}
			else {
				if(leftAns.numRightEdge + rightAns.numLeftEdge > rightAns.numLongest && 
				   leftAns.numRightEdge + rightAns.numLeftEdge > leftAns.numLongest) {
					leftEdge = leftAns.numLeftEdge;
					rightEdge = rightAns.numRightEdge;
					longest = leftAns.numRightEdge + rightAns.numLeftEdge;
				}
				else {
					leftEdge = leftAns.numLeftEdge;
					rightEdge = rightAns.numRightEdge;
					longest = Math.max(leftAns.numLongest, rightAns.numLongest);
				}
			}
			
			return new Result(leftEdge, rightEdge, longest, range);

		}
		
	}
	
	public static void main(String [] args)
    {
        //Test 1
        System.out.println("Test 1: ");
        long start = System.nanoTime();
        int cutoff = 10;
        int [] array1 = {0,1,1,1,1,0};
        LongestSequence test1 = new LongestSequence(cutoff, 0, 6, 1, array1);
        Result result1 = test1.compute();
        long duration = (System.nanoTime() - start);
        
        System.out.println("Array Size: " + array1.length);
        System.out.println("Computation Time (nano-seconds): " + duration);
        System.out.println("Cutoff: " + cutoff);
        System.out.println("Longest Sequence of 1: " + result1.numLongest);
        System.out.println();
        
        //Test 2
        System.out.println("Test 2: ");
        start = System.nanoTime();
        cutoff = 1;
        int [] array2 = {1,0,1,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1};
        LongestSequence test2 = new LongestSequence(cutoff, 0, array2.length, 0, array2);
        Result result2 = test2.compute();
        duration = (System.nanoTime() - start);
        
        System.out.println("Array Size: " + array2.length);
        System.out.println("Computation Time: (nano-seconds): " + duration);
        System.out.println("Cutoff: " + cutoff);
        System.out.println("Longest Sequence of 0: " + result2.numLongest);
        System.out.println();
        
        //Test 3
        System.out.println("Test 3: ");
        int[] array3 = new int[50];
        for (int i = 0; i < 50; i++) {
            array3[i] = (int) (Math.random() * 6);
        }
        start = System.nanoTime();
        cutoff = 1;
        LongestSequence test3 = new LongestSequence(cutoff, 0, array3.length, 4, array3);
        Result result3 = test3.compute();
        duration = (System.nanoTime() - start);
        
        System.out.println("Array Size: " + array3.length);
        System.out.println("Computation Time (nano-seconds): " + duration);
        System.out.println("Cutoff: " + cutoff);
        System.out.println("Longest Sequence of 4: " + result3.numLongest);
        System.out.println();
        
        //Test 4
        System.out.println("Test 4: ");
        int[] array4 = new int[100];
        
        for (int i = 0; i < 100; i++) {
            array4[i] = (int) (Math.random() * 6);
        }
        
        start = System.nanoTime();
        cutoff = 20;
        LongestSequence test4 = new LongestSequence(cutoff, 0, array4.length, 4, array4);
        Result result4 = test4.compute();
        duration = (System.nanoTime() - start);
        
        System.out.println("Array Size: " + array4.length);
        System.out.println("Computation Time (nano-seconds): " + duration);
        System.out.println("Cutoff: " + cutoff);
        System.out.println("Longest Sequence of 4: " + result4.numLongest);
        System.out.println();
        
        //Test 5
        System.out.println("Test 5: ");
        int[] array5 = new int[1000];
        
        for (int i = 0; i < 1000; i++) {
            array5[i] = (int) (Math.random() * 6);
        }
        
        start = System.nanoTime();
        cutoff = 1;
        LongestSequence test5 = new LongestSequence(cutoff, 0, array5.length, 4, array5);
        Result result5 = test5.compute();
        duration = (System.nanoTime() - start);
        
        System.out.println("Array Size: " + array5.length);
        System.out.println("Computation Time (nano-seconds): " + duration);
        System.out.println("Cutoff: " + cutoff);
        System.out.println("Longest Sequence of 4: " + result5.numLongest);
        System.out.println();
        
        //Test 6
        System.out.println("Test 6: ");
        int[] array6 = new int[1000];
        
        for (int i = 0; i < 1000; i++) {
            array6[i] = (int) (Math.random() * 6);
        }
        
        start = System.nanoTime();
        cutoff = 100;
        LongestSequence test6 = new LongestSequence(cutoff, 0, array6.length, 4, array6);
        Result result6 = test6.compute();
        duration = (System.nanoTime() - start);
        
        System.out.println("Array Size: " + array6.length);
        System.out.println("Computation Time (nano-seconds): " + duration);
        System.out.println("Cutoff: " + cutoff);
        System.out.println("Longest Sequence of 4: " + result6.numLongest);
        System.out.println();
        
        //Test 7
        System.out.println("Test 7: ");
        int[] array7 = new int[10000];
        
        for (int i = 0; i < 10000; i++) {
            array7[i] = (int) (Math.random() * 6);
        }
        
        start = System.nanoTime();
        cutoff = 1;
        LongestSequence test7 = new LongestSequence(cutoff, 0, array7.length, 4, array7);
        Result result7 = test7.compute();
        duration = (System.nanoTime() - start);
        
        System.out.println("Array Size: " + array7.length);
        System.out.println("Computation Time (nano-seconds): " + duration);
        System.out.println("Cutoff: " + cutoff);
        System.out.println("Longest Sequence of 4: " + result7.numLongest);
        System.out.println();
        
        //Test 8
        System.out.println("Test 8: ");
        int[] array8 = new int[10000];
        
        for (int i = 0; i < 10000; i++) {
            array8[i] = (int) (Math.random() * 6);
        }
        
        start = System.nanoTime();
        cutoff = 100;
        LongestSequence test8 = new LongestSequence(cutoff, 0, array8.length, 4, array8);
        Result result8 = test8.compute();
        duration = (System.nanoTime() - start);
        
        System.out.println("Array Size: " + array8.length);
        System.out.println("Computation Time (nano-seconds): " + duration);
        System.out.println("Cutoff: " + cutoff);
        System.out.println("Longest Sequence of 4: " + result8.numLongest);
        System.out.println();
        
        //Test 9
        System.out.println("Test 9: ");
        int[] array9 = new int[10000];
        
        for (int i = 0; i < 10000; i++) {
            array9[i] = (int) (Math.random() * 6);
        }
        
        start = System.nanoTime();
        cutoff = 1000;
        LongestSequence test9 = new LongestSequence(cutoff, 0, array9.length, 4, array9);
        Result result9 = test9.compute();
        duration = (System.nanoTime() - start);
        
        System.out.println("Array Size: " + array9.length);
        System.out.println("Computation Time (nano-seconds): " + duration);
        System.out.println("Cutoff: " + cutoff);
        System.out.println("Longest Sequence of 4: " + result9.numLongest);
        System.out.println();
    }
	
}
