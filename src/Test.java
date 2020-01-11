import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Test {

    static class DecreasedKey {
        private int key;
        private int delta;

        public DecreasedKey(int key, int delta) {
            this.key = key;
            this.delta = delta;
        }

        public int getKey() {
            return key;
        }

        public int getDelta() {
            return delta;
        }

        @Override
        public String toString() {
            return "DecreasedKey{" +
                    "key=" + key +
                    ", delta=" + delta +
                    '}';
        }
    }

    static class DeleteException extends Exception {
        int deleteKey;

        public DeleteException(int deleteKey, String s) {
            super(s);
            this.deleteKey = deleteKey;
        }

        public int getDeleteKey() {
            return deleteKey;
        }
    }

    static class DecreasedKeyException extends Exception {
        private List<DecreasedKey> keys;

        public DecreasedKeyException(List<DecreasedKey> keys, String msg, Throwable ex) {
            super(msg, ex);
            this.keys = keys;
        }

        public List<DecreasedKey> getKeys() {
            return keys;
        }
    }

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


    public static Map<Integer, FibonacciHeap.HeapNode> testInserts(FibonacciHeap heap, List<Integer> keys) {
        int size = heap.size();
        double minKey = Double.POSITIVE_INFINITY;
        Map<Integer, FibonacciHeap.HeapNode> nodes = new HashMap<>();
        int i = 0;
        for (Integer n : keys) {
            if (n < minKey) {
                minKey = n;
            }
            nodes.put(n, heap.insert(n));
            testFibHeap(heap);
            size++;
            testHeapFields(
                    heap, new ExpectedFields(
                            size, null, (int) minKey
                    )
            );
        }
        return nodes;
    }

    public static void testDeletes(FibonacciHeap heap, List<Integer> orderedKeys) throws DeleteException {
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
            try {
                heap.deleteMin();
            } catch (Exception ex) {
                throw new DeleteException(heap.findMin().getKey(), "error deleting key " + heap.findMin());
            }
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

    public static int getNumMarked(FibonacciHeap heap) {
        return (heap.potential() - heap.getNumberOfTrees()) / 2;
    }

    public static void testDecreaseKey(FibonacciHeap heap, Map<Integer, FibonacciHeap.HeapNode> nodes, int amount) throws DecreasedKeyException {
        int randIndex, delta;
        FibonacciHeap.HeapNode[] nodesArr = new FibonacciHeap.HeapNode[nodes.size()];
        List<DecreasedKey> decreasedKeys = new ArrayList<>();
        int i = 0;
        for (Map.Entry<Integer, FibonacciHeap.HeapNode> entry : nodes.entrySet()) {
            nodesArr[i++] = entry.getValue();
        }
        Random random = new Random(0L);
        int newKey, oldKey;
        try {
            for (i = 0; i < amount; i++) {
                do {
                    randIndex = random.nextInt(nodesArr.length);
                    delta = Math.max(1, random.nextInt(nodesArr.length));
                    oldKey = nodesArr[randIndex].getKey();
                    newKey = oldKey - delta;
                } while (nodes.containsKey(newKey));
                int old = nodes.get(oldKey).getKey();
                decreasedKeys.add(new DecreasedKey(oldKey, delta));
                heap.decreaseKey(nodes.get(oldKey), delta);
                nodes.put(newKey, nodes.remove(oldKey));
                testFibHeap(heap);
            }
        } catch (Exception ex) {
            throw new DecreasedKeyException(decreasedKeys, "Error during decreasedKey", ex);
        }
    }

    public static void testMeld(FibonacciHeap heap, int upperBound) {
        CreateHeapResult res = createHeap(upperBound, upperBound + 20, false);
        FibonacciHeap heap2 = res.getHeap();
        int size = heap.size(), numOfTrees = heap.getNumberOfTrees(), numMarked = getNumMarked(heap);
        heap.meld(heap2);
        if (heap.size() != size + heap2.size()) {
            throw new RuntimeException("size after meld is not sum of both heaps");
        }
        if (heap.getNumberOfTrees() != numOfTrees + heap2.getNumberOfTrees()) {
            throw new RuntimeException("numOfTrees after meld is not sum of both heaps");
        }
        if (getNumMarked(heap) != numMarked + getNumMarked(heap2)) {
            throw new RuntimeException("numMarked after meld is not sum of both heaps");
        }
        testFibHeap(heap);
    }

    public static void testKMin(FibonacciHeap heap) {
        if (heap.size() == 0) {
            int[] kMinVals = FibonacciHeap.kMin(heap, 0);
            if (kMinVals.length != 0) {
                throw new RuntimeException("kMin did not return empty array for empty heap");
            }
        } else {
            FibonacciHeap.HeapNode cur = heap.getFirst();
            FibonacciHeap.HeapNode next;
            Random rand = new Random(0L);
            int kMin;
            int[] kMinVals, dumbKMinVals;
            FibonacciHeap heaps[] = new FibonacciHeap[heap.getNumberOfTrees()];
            int heapIndex = 0;
            do {
                next = cur.getNext();
                cur.setPrev(cur);
                cur.setNext(cur);
                heaps[heapIndex] = new FibonacciHeap(cur, Test.countTreeItems(cur));
                heapIndex++;
                cur = next;
            } while (next != null && next != heap.getFirst());
            for (FibonacciHeap fHeap : heaps) {
                if (fHeap.size() > 0) {
                    kMin = rand.nextInt(fHeap.size());
                    kMinVals = FibonacciHeap.kMin(fHeap, kMin);
                    dumbKMinVals = dumbKMin(fHeap, kMin);
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
            }
        }
    }

    public static void testRootPointers(FibonacciHeap heap) {
        FibonacciHeap.HeapNode cur = heap.getFirst();
        do {
            if (cur == null) {
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

    public static void testFibMin(FibonacciHeap heap) {
        FibonacciHeap.HeapNode cur = heap.getFirst();
        do {
            if (cur.getKey() < heap.findMin().getKey()) {
                throw new RuntimeException(String.format("minimum must be %s but is %s", heap.findMin().getKey(), cur.getKey()));
            }
            cur = cur.getNext();
        } while (cur != heap.getFirst());
    }

    public static int calculateNumOfTrees(FibonacciHeap heap) {
        int i = heap.size() == 0 ? 0 : 1;
        FibonacciHeap.HeapNode cur = heap.getFirst().getNext();
        while (cur != heap.getFirst()) {
            i++;
            cur = cur.getNext();
        }
        return i;
    }


    public static void testNumOfTrees(FibonacciHeap heap) {
        int actualNumOfTrees = calculateNumOfTrees(heap);
        if (actualNumOfTrees != heap.getNumberOfTrees()) {
            throw new RuntimeException(String.format("should have %s trees but counted %s", heap.getNumberOfTrees(), actualNumOfTrees));
        }
    }

    public static void testFibHeap(FibonacciHeap heap) {
        FibonacciHeap.HeapNode first = heap.findMin();
        FibonacciHeap.HeapNode cur = first;
        testRootPointers(heap);
        testNumOfTrees(heap);
        do {
            testHeapMinProp(cur);
            testChildren(cur);
            testSiblings(cur);
            testRanks(cur);
            cur = cur.getNext();
        } while (cur != first);
        testFibMin(heap); // Minimum must be one of the roots otherwise testHeapMinProp would have failed
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

    public static void testRandomHeap(int size, int jumps) throws Exception {
        List<Integer> keys = IntStream.rangeClosed(0, size).boxed().map(x -> x * jumps).collect(Collectors.toList());

        Collections.shuffle(keys);
        List<Integer> orderedKeys = new ArrayList<>(keys);
        try {
            Collections.sort(orderedKeys);
            FibonacciHeap heap = new FibonacciHeap();
            System.out.println("-testing inserts");
            testInserts(heap, keys);
            System.out.println("-testing deletes");
            testDeletes(heap, orderedKeys);
            System.out.println("-testing kMin");
            testInserts(heap, keys);  // Repopulate after delete
            heap.deleteMin();  // Make valid heap structure
            testKMin(heap);
            // After kMin, tree is destroyed, reset
            heap = new FibonacciHeap();
            testInserts(heap, keys);
            System.out.println("-testing meld");
            testMeld(heap, size + 1);
            System.out.println("-testing decreaseKey");
            heap = new FibonacciHeap();
            Map<Integer, FibonacciHeap.HeapNode> nodes = testInserts(heap, keys);
            nodes.remove(heap.findMin().getKey());
            heap.deleteMin();
            testDecreaseKey(heap, nodes, nodes.size() / 2);


        } catch (DeleteException ex) {
            System.out.println("inserted: " + keys.toString());
            System.out.println("failed deleting minimum when min = " + ex.deleteKey);
            throw ex;
        } catch (DecreasedKeyException ex) {
            System.out.println("inserted: " + keys.toString());
            System.out.println("order of decreased keys: " + ex.getKeys().toString());
        } catch (Exception ex) {
            System.out.println("inserted: " + keys.toString());
            throw ex;
        }
    }

    public static void testTrees() throws Exception {

        System.out.println("testing tree of size 0");
        CreateHeapResult res = createHeap(0, -1);
        testHeapFields(res.getHeap(), new ExpectedFields(0, 0, null));
        testKMin(res.getHeap());
        System.out.println("testing tree of size 1");
        res = createHeap(1, 1);
        testHeapFields(res.getHeap(), new ExpectedFields(1, 1, 1));
        testKMin(res.getHeap());
        int delta = 5;
        res.getHeap().decreaseKey(res.getHeap().findMin(), delta);
        testHeapFields(res.getHeap(), new ExpectedFields(1, 1, 1 - delta));
        res.getHeap().delete(res.getHeap().findMin());
        testHeapFields(res.getHeap(), new ExpectedFields(0, 0, null));

        Random rand = new Random(0L);
        int jumps;
        for (int i = 0; i < 100; i++) {
            int size = rand.nextInt(10000);
            jumps = rand.nextInt(10) + 3;
            System.out.println("testing tree of size " + size);
            testRandomHeap(size, jumps);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("I <3 male Guy");
        testTrees();
        System.out.println("Dankovich");
    }
}
