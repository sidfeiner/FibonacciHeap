import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Test {

    public static class CreateHeapResult {
        private List<Integer> insertOrder;
        private FibonacciHeap heap;

        public CreateHeapResult(List<Integer> insertOrder, FibonacciHeap heap) {
            this.insertOrder = insertOrder;
            this.heap = heap;
        }

        public List<Integer> getInsertOrder() {
            return insertOrder;
        }

        public FibonacciHeap getHeap() {
            return heap;
        }
    }

    public static class ExpectedFields {
        private int size;
        private Integer numOfTrees;
        private Integer minKeyVal;

        public ExpectedFields(int size, Integer numOfTrees, Integer minKeyVal) {
            this.size = size;
            this.numOfTrees = numOfTrees;
            this.minKeyVal = minKeyVal;
        }

        public int getSize() {
            return size;
        }

        public int getNumOfTrees() {
            return numOfTrees;
        }

        public int getMinKeyVal() {
            return minKeyVal;
        }
    }

    public static Logger logger = Logger.getLogger("Test");


    public static FibonacciHeap testMeld() {
        Random rand = new Random();

        CreateHeapResult res1 = createHeap(1, 3);

        CreateHeapResult res2 = createHeap(4, 6);

        res1.getHeap().meld(res2.getHeap());


        return res1.getHeap();
    }

    public static CreateHeapResult createHeap(int from, int to) {
        return createHeap(from, to, false);
    }

    public static CreateHeapResult createHeap(int from, int to, boolean testAfterInsert) {
        List<Integer> l = IntStream.rangeClosed(from, to).boxed().collect(Collectors.toList());
        Collections.shuffle(l);
        FibonacciHeap heap = new FibonacciHeap();
        for (Integer n : l) {
            logger.info("inserting " + n);
            heap.insert(n);
            if (testAfterInsert) testFibHeap(heap);
        }
        return new CreateHeapResult(l, heap);
    }

    public static int expectedNumOfTrees(int size) {
        return (int) Integer.toBinaryString(size).chars().filter(ch -> ch == '1').count();
    }

    public static void testInserts(FibonacciHeap heap, List<Integer> keys) {
        int size = heap.size();
        double minKey = Double.POSITIVE_INFINITY;
        for (Integer n : keys) {
            if (n < minKey) {
                minKey = n;
            }
            heap.insert(n);
            testFibHeap(heap);
            size++;
            testHeapFields(
                    heap, new ExpectedFields(
                            size, null, (int) minKey
                    )
            );
        }
    }

    public static void testDeletes(FibonacciHeap heap, List<Integer> orderedKeys) {
        int size = heap.size();
        Integer expectedSize;
        for (Integer minVal : orderedKeys) {
            if (heap.findMin().getKey() != minVal) {
                throw new RuntimeException(String.format(
                        "wrong minimum value found. should have been %d but was %d", minVal, heap.findMin().getKey())
                );
            }
            expectedSize = size == heap.size() ? null : expectedNumOfTrees(size);
            testHeapFields(heap, new ExpectedFields(size, expectedSize, minVal));
            heap.deleteMin();
            size--;
        }
    }

    public static int[] dumbKMin(FibonacciHeap heap, int k) {
        int vals[] = new int[k];
        for (int i = 0; i < k; i++) {
            vals[i] = heap.findMin().getKey();
            heap.deleteMin();
        }
        return vals;
    }

    public static void testKMin(FibonacciHeap heap) {
        FibonacciHeap.HeapNode cur = heap.getFirst();
        Random rand = new Random(0L);
        int kMin;
        int[] kMinVals, dumbKMinVals;
        do {
            FibonacciHeap treeAsHeap = new FibonacciHeap(cur, Test.countTreeItems(cur));
            if (treeAsHeap.size() > 0) {
                kMin = rand.nextInt(treeAsHeap.size());
                kMinVals = FibonacciHeap.kMin(treeAsHeap, kMin);
                dumbKMinVals = dumbKMin(treeAsHeap, kMin);
                if (kMinVals.length != dumbKMinVals.length) {
                    throw new RuntimeException("kMin has wrong amount of values");
                } else {
                    for (int i = 0; i < kMinVals.length; i++) {
                        if (kMinVals[i] != dumbKMinVals[i]) {
                            throw new RuntimeException("kMin returns wrong values");
                        }
                    }
                }
            }
            cur = cur.getNext();
        } while (cur != heap.getFirst());
    }

    public static void testRootPointers(FibonacciHeap heap) {
        FibonacciHeap.HeapNode cur = heap.getFirst();
        do {
            if (cur==null) {
                throw new RuntimeException("we have a tree with root null");
            }
            if (cur.getNext().getPrev() != cur)
                throw new RuntimeException("getNext.getPrev did not return current root: " + cur.getKey());
            if (cur.getPrev().getNext() != cur)
                throw new RuntimeException("getPrev.getNext did not return current root: " + cur.getKey());
            if (cur.getParent() != null) {
                throw new RuntimeException(String.format("tree root %d has parent", cur.getKey()));
            }
            cur = cur.getNext();
        } while (cur != heap.getFirst());
    }

    public static void testRanks(FibonacciHeap.HeapNode treeRoot) {
        if (treeRoot == null) return;
        int rank = treeRoot.getRank();
        FibonacciHeap.HeapNode child = treeRoot.getChild();
        FibonacciHeap.HeapNode curNode = child;
        int childrenAmount = 0;
        if (rank == 0) {
            if (child != null) {
                throw new RuntimeException("rank is zero but node " + treeRoot.getKey() + " has child " + child.getKey());
            }
        } else {
            do {
                childrenAmount++;
                if (curNode.getChild() != null) testRanks(curNode.getChild());
                curNode = curNode.getNext();
            } while (curNode != child);
            if (rank != childrenAmount) {
                throw new RuntimeException(String.format(
                        "node %d has rank %d but found %d children",
                        treeRoot.getKey(), rank, childrenAmount)
                );
            }
        }
    }

    public static void testFibHeap(FibonacciHeap heap) {
        FibonacciHeap.HeapNode first = heap.findMin();
        FibonacciHeap.HeapNode cur = first;
        testRootPointers(heap);
        do {
            testHeapMinProp(cur);
            testChildren(cur);
            testSiblings(cur);
            testRanks(cur);
            cur = cur.getNext();
        } while (first != cur);
    }

    /**
     * Checks only siblings for root's first child
     */
    public static void testSiblings(FibonacciHeap.HeapNode root) {
        if (root.getChild() != null) {
            FibonacciHeap.HeapNode child = root.getChild();
            FibonacciHeap.HeapNode cur = child;
            do {
                if (cur.getPrev().getNext() != cur)
                    throw new RuntimeException("getPrev.getNext did not return current child: " + cur.getKey());
                if (cur.getNext().getPrev() != cur)
                    throw new RuntimeException("getNext.getPrev did not return current child: " + cur.getKey());
                cur = cur.getNext();
            } while (cur != child);
        }
    }

    public static void testChildren(FibonacciHeap.HeapNode node) {
        if (node.getChild() != null) {
            if (node.getChild().getParent() != node)
                throw new RuntimeException("getChild.getParent did not return cur node: " + node.getKey());
            FibonacciHeap.HeapNode child = node.getChild();
            testChildren(child);
            FibonacciHeap.HeapNode cur = child;
            do {
                if (cur.getParent() != node)
                    throw new RuntimeException("getParent of a sibling did not return current node: " + node.getKey());
                cur = cur.getNext();
            } while (cur != child);
        }
    }

    public static void testHeapMinProp(FibonacciHeap.HeapNode node) {
        if (node.getChild() != null) {
            FibonacciHeap.HeapNode firstChild = node.getChild();
            FibonacciHeap.HeapNode curChild = firstChild;
            do {
                if (curChild.getKey() < node.getKey())
                    throw new RuntimeException("child found with smaller key than parent: " + node.getKey());
                testHeapMinProp(curChild);
                curChild = curChild.getNext();
            } while (curChild != firstChild);
        }
    }

    private static int countTreeItemsRec(FibonacciHeap.HeapNode child) {
        if (child == null) return 0;
        int cnt = 0;
        FibonacciHeap.HeapNode cur = child;
        do {
            cnt++;
            if (cur.getChild() != null) cnt += countTreeItemsRec(cur.getChild());
            cur = cur.getNext();
        } while (cur != child);
        return cnt;

    }

    public static int countTreeItems(FibonacciHeap.HeapNode root) {
        return root == null ? 0 : 1 + countTreeItemsRec(root.getChild());

    }

    public static void testHeapFields(FibonacciHeap heap, ExpectedFields expected) {
        if (expected.numOfTrees != null && expected.numOfTrees != heap.getNumberOfTrees()) {
            throw new RuntimeException(
                    String.format("wrong amount of trees. found %d but expected %d",
                            heap.getNumberOfTrees(), expected.numOfTrees)
            );
        }
        if (expected.size != heap.size()) {
            throw new RuntimeException(
                    String.format("wrong size of tree. found %d but expected %d",
                            heap.size(), expected.size)
            );
        }
        if (expected.minKeyVal == null) {
            if (heap.findMin() != null) {
                throw new RuntimeException(
                        String.format("wrong minimum found. found %d but expected null", heap.findMin().getKey())
                );
            }
        } else {
            if (expected.minKeyVal != heap.findMin().getKey()) {
                throw new RuntimeException(
                        String.format("wrong min key in tree. found %d but expected %d",
                                heap.findMin().getKey(), expected.minKeyVal)
                );
            }
        }
    }

    public static void testRandomHeap(int size) {
        List<Integer> keys = IntStream.rangeClosed(0, size).boxed().collect(Collectors.toList());
        Collections.shuffle(keys);
        List<Integer> orderedKeys = new ArrayList<>(keys);
        try {
            Collections.sort(orderedKeys);
            FibonacciHeap heap = new FibonacciHeap();
            System.out.println("-testing inserts");
            testInserts(heap, keys);
            //BTreePrinter.printNode(tree.getRoot());
            System.out.println("-testing deletes");
            testDeletes(heap, orderedKeys);
            System.out.println("-testing kMin");
            testInserts(heap, keys);
            heap.deleteMin();
            testKMin(heap);
        } catch (Exception ex) {

        }
    }

    public static void testInsertAndThenDelete() {

        System.out.println("tesing tree of size 0");
        CreateHeapResult res = createHeap(0, -1);
        testHeapFields(res.getHeap(), new ExpectedFields(0, 0, null));
        res = createHeap(1, 1);
        System.out.println("tesing tree of size 1");
        testHeapFields(res.getHeap(), new ExpectedFields(1, 1, 1));
        res.getHeap().deleteMin();
        testHeapFields(res.getHeap(), new ExpectedFields(0, 0, null));


        Random rand = new Random(0L);

        for (int i = 0; i < 200; i++) {
            int size = rand.nextInt(10000);
            System.out.println("testing tree of size " + size);
            testRandomHeap(size);
        }


    }

    public static void main(String[] args) {

        testInsertAndThenDelete();

       /* FibonacciHeap heap = createHeap(10);
        for (int k : keys) {
            heap.insert(k);
        }
        for (int i = 0; i < keys.length; i++) {
            heap.deleteMin();
            FiboHeapPrinter.printHeap(heap);
        }
        System.out.println("done");*/
        //FibonacciHeap heap1 = testMeld();
        //heap1.printHeap();
    }
}
