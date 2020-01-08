import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class HeapTest {

    Logger logger = Logger.getLogger("HeapTest");

    private static int[] randomArray(int size, int min, int max) {
        List<Integer> l = IntStream.rangeClosed(min, max).boxed().collect(Collectors.toList());
        Collections.shuffle(l);
        int[] arr = new int[size];
        int i = 0;
        while (i < size) {
            arr[i] = l.get(i);
            i++;
        }
        return arr;
    }

    private static int[] sortedArray(int size, int start) {
        int[] arr = new int[size];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i + start;
        }

        return arr;
    }

    private static FibonacciHeap arrayToHeap(int[] arr) {
        FibonacciHeap heap = new FibonacciHeap();
        for (int x : arr) {
            heap.insert(x);
        }

        return heap;
    }

    private static void restartCutsLink() {
        FibonacciHeap.totalCuts = 0;
        FibonacciHeap.totalLinks = 0;
    }

    void testEmpty() {
        FibonacciHeap heap = new FibonacciHeap();

        assert (heap.isEmpty()) : "heap isn't empty";

        heap.insert(1);
        assert (!heap.isEmpty()) : "heap empty after insert";

        heap.deleteMin();
        assert (heap.isEmpty()) : "heap isn't empty after deleteMin";
        System.out.println("testEmpty Passed!");
    }

    void testDeleteMin() throws InterruptedException {
        int[] arr = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        FibonacciHeap heap = arrayToHeap(arr);

        assert (heap.findMin() != null);
        assert (heap.findMin().getKey() == 0) : "minimal key isn't as expected";
        assert (heap.getNumberOfTrees() == 16) : "number of internal trees isn't as expected";

        heap.deleteMin();

        assert (heap.findMin() != null);
        assert (heap.findMin().getKey() == 1) : "minimal key isn't as expected after deleteMin";
        assert (heap.getNumberOfTrees() == 4) : "number of internal trees isn't as expected after deleteMin";

        for (int tries = 1; tries < 5; tries++) {
            int numOfKeys;
            numOfKeys = 2 * 5 * tries;
            logger.info("creating heap with " + numOfKeys + " keys");
            arr = randomArray(numOfKeys, 0, 60);
            heap = arrayToHeap(arr);
            while (!heap.isEmpty()) {
                long expectedNumOfTrees = Integer.toBinaryString(numOfKeys - 1).chars().filter(ch -> ch == '1').count();
                heap.deleteMin();
                assert (heap.getNumberOfTrees() == expectedNumOfTrees) : "number of internal trees isn't as expected after deleteMin";
                numOfKeys -= 1;
            }
        }

        System.out.println("testDeleteMin Passed!");
    }

    void testDelete() {
        int[] arr = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        FibonacciHeap heap = arrayToHeap(arr);
        FibonacciHeap.HeapNode node;

        heap.deleteMin();

        FiboHeapPrinter.printHeap(heap);
        node = heap.insert(0);
        heap.delete(node);
        assert (heap.findMin().getKey() == 2) : "minimum is not updated correctly";
        assert (heap.size() == 9) : "new size is incorrect";

        node = heap.findMin();
        heap.delete(node);
        assert (heap.findMin().getKey() == 3) : "minimum is not updated correctly";
        assert (heap.size() == 8) : "new size is incorrect";


        node = heap.insert(15);
        assert (heap.size() == 9) : "insert problem";
        heap.delete(node);
        assert (heap.findMin().getKey() == 3) : "minimum is not updated correctly";
        assert (heap.size() == 8) : "new size is incorrect";

        System.out.println("testDelete Passed!");


    }

    void testDecreaseKey() {

        int[] arr = {5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
        FibonacciHeap heap = arrayToHeap(arr);
        FibonacciHeap.HeapNode node;
        node = heap.insert(15);
        heap.decreaseKey(node, 15);

        assert (heap.findMin().getKey() == 0) : "minimum is not updated correctly";

        heap.deleteMin();

        heap = new FibonacciHeap();
        int[] arr2 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        FibonacciHeap.HeapNode[] nodes = new FibonacciHeap.HeapNode[11];
        for (int j : arr2) {
            nodes[j] = heap.insert(j + 20);
        }

        for (int i = 10; i > 0; i--) {

            heap.decreaseKey(nodes[i], 20);
            assert (heap.findMin().getKey() == nodes[i].getKey()) : "incorrect minimum";
        }



        System.out.println("testDecreaseKey Passed!");

    }

    void testTotalCuts() {

        assert (FibonacciHeap.totalCuts == 0) : "reset cuts";

        FibonacciHeap heap = new FibonacciHeap();

        int[] arr2 = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        FibonacciHeap.HeapNode[] nodes = new FibonacciHeap.HeapNode[9];
        for (int j : arr2) {
            nodes[j] = heap.insert(j);
        }
        heap.deleteMin();


        heap.decreaseKey(nodes[7], 5);
        assert (FibonacciHeap.totalCuts == 1) : "incorrect cuts count";

        heap.decreaseKey(nodes[6], 2);

        assert (FibonacciHeap.totalCuts == 3) : "incorrect cuts count";

        heap.decreaseKey(nodes[3], 3);
        assert (FibonacciHeap.totalCuts == 4) : "incorrect cuts count";


        System.out.println("testTotalCuts Passed!");

    }

    void testCountersRep() {

        FibonacciHeap heap = new FibonacciHeap();
        int[] countersArray;

        int[] arr2 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        FibonacciHeap.HeapNode[] nodes = new FibonacciHeap.HeapNode[20];
        for (int j : arr2) {
            nodes[j] = heap.insert(j);
        }

        heap.deleteMin();
        countersArray = heap.countersRep();
        assert (countersArray[4] == 1) : "incorrect number of trees";

        heap.deleteMin();
        countersArray = heap.countersRep();
        for (int i : countersArray)
            assert (i == 1) : "incorrect number of trees";

        heap.decreaseKey(nodes[8], 8);
        countersArray = heap.countersRep();
        assert (countersArray[0] == 2) : "incorrect number of trees";

        System.out.println("testCountersRep Passed!");

    }

    void testPotential() {
        assert (FibonacciHeap.totalCuts == 0) : "reset cuts";

        FibonacciHeap heap = new FibonacciHeap();

        int[] arr2 = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        FibonacciHeap.HeapNode[] nodes = new FibonacciHeap.HeapNode[9];
        for (int j : arr2) {
            nodes[j] = heap.insert(j);
        }
        heap.deleteMin();

        assert (heap.potential() == 1) : "incorrect potential";

        heap.decreaseKey(nodes[7], 5);
        assert (heap.potential() == 4) : "incorrect potential";

        heap.decreaseKey(nodes[6], 2);
        assert (heap.potential() == 4) : "incorrect potential";

        System.out.println("testPotential Passed!");
    }

    void testKMin() {
        int[] arr = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        int[] expected = {1, 2, 3, 4, 5, 6}, result;
        FibonacciHeap heap = arrayToHeap(arr);

        heap.deleteMin();
        result = FibonacciHeap.kMin(heap, 6);
        assert (result.length == 6) : "wrong array size returned from kMin";
        for (int i = 0; i < expected.length; i++)
            assert (result[i] == expected[i]) : "kMin result is malformed";

        arr = sortedArray((int) Math.pow(2, 16) + 1, 0);
        heap = arrayToHeap(arr);
        heap.deleteMin();
        for (int tries = 1; tries < 5; tries++) {
            int k = (tries < 4) ? (int) Math.pow(2, 8) * tries : arr.length - 1;
            expected = sortedArray(k, 1);
            if (tries == 4) {
                tries = 4;
            }
            result = FibonacciHeap.kMin(heap, k);
            assert (result.length == k) : "wrong array size returned from kMin";
            for (int i = 0; i < expected.length; i++) {
                assert (result[i] == expected[i]) : "kMin result is malformed";
            }
        }

        System.out.println("testKMin Passed!");
    }

    void testMeld() {
        int[] arr1 = {1, 2, 3, 4, 5, 6, 7, 8};
        int[] arr2 = {9, 10, 0};
        FibonacciHeap.HeapNode node;

        FibonacciHeap heap1 = arrayToHeap(arr1);
        FibonacciHeap heap2 = arrayToHeap(arr2);
        heap1.meld(heap2);

        assert (heap1.findMin().getKey() == 0) : "incorrect min";
        assert (heap1.getNumberOfTrees() == 11) : "incorrect number of trees";

        for (int i = 0; i < 11; i++) {
            assert (i == heap1.findMin().getKey()) : "incorrect key";
            heap1.deleteMin();
        }

        heap1 = arrayToHeap(arr1);
        FibonacciHeap heap3 = new FibonacciHeap();
        heap1.meld(heap3);
        assert (heap1.findMin().getKey() == 1) : "incorrect min";
        assert (heap1.getNumberOfTrees() == 8) : "incorrect number of trees";

        for (int i = 1; i < 9; i++) {
            assert (i == heap1.findMin().getKey()) : "incorrect key";
            heap1.deleteMin();
        }

        heap3 = arrayToHeap(arr1);
        heap1 = new FibonacciHeap();
        heap1.meld(heap3);
        assert (heap1.findMin().getKey() == 1) : "incorrect min";
        assert (heap1.getNumberOfTrees() == 8) : "incorrect number of trees";

        for (int i = 1; i < 9; i++) {
            assert (i == heap1.findMin().getKey()) : "incorrect key";
            heap1.deleteMin();
        }

        System.out.println("testMeld Passed!");
    }

    public static void main(String[] args) throws Exception {
        HeapTest test = new HeapTest();

        test.testEmpty();
        test.testDeleteMin();
        test.testTotalCuts();
        restartCutsLink();
        test.testPotential();


        test.testDecreaseKey();
        test.testDelete();
        //TODO.testTotalLinks()
        test.testCountersRep();
        test.testKMin();
        test.testMeld();
        test.testPotential();
    }
}