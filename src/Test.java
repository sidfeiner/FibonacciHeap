import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Test {

    public static Logger logger = Logger.getLogger("Test");


    public static FibonacciHeap testMeld() {
        Random rand = new Random();

        FibonacciHeap heap1 = createHeap(1, 3);

        FibonacciHeap heap2 = createHeap(4, 6);

        heap1.meld(heap2);


        return heap1;
    }

    public static FibonacciHeap createHeap(int from, int to) {
        List<Integer> l = IntStream.rangeClosed(from, to).boxed().collect(Collectors.toList());
        Collections.shuffle(l);
        FibonacciHeap heap = new FibonacciHeap();
        for (Integer n : l) {
            logger.info("inserting " + n);
            heap.insert(n);
        }

        return heap;

    }

    public static void testRootPointers(FibonacciHeap heap) {
        FibonacciHeap.HeapNode cur = heap.getFirst();
        do {
            if (cur.getNext().getPrev() != cur)
                throw new RuntimeException("getNext.getPrev did not return current root: " + cur.getKey());
            if (cur.getPrev().getNext() != cur)
                throw new RuntimeException("getPrev.getNext did not return current root: " + cur.getKey());
            cur = cur.getNext();
        } while (cur != heap.getFirst());
    }

    public static void testFibHeap(FibonacciHeap heap) {
        FibonacciHeap.HeapNode first = heap.findMin();
        FibonacciHeap.HeapNode cur = first;
        testRootPointers(heap);
        do {
            testHeapMinProp(cur);
            testChildren(cur);
            testSiblings(cur);
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
        if (child==null) return 0;
        int cnt = 0;
        FibonacciHeap.HeapNode cur = child;
        do {
            cnt++;
            if (cur.getChild()!=null) cnt+=countTreeItemsRec(cur.getChild());
            cur = cur.getNext();
        } while (cur != child);
        return cnt;

    }

    public static int countTreeItems(FibonacciHeap.HeapNode root) {
        return root == null ? 0 : 1 + countTreeItemsRec(root.getChild());

    }

    public static void main(String[] args) {
//        FibonacciHeap heap = createHeap(10);
//        testFibHeap(heap);
//        System.out.println("a");
        int[] keys = {2,47,37,57,52,27,49,51,60,30,41,4,34,54,56,14,58,29,31,17};
        FibonacciHeap heap = new FibonacciHeap();
        for (int k : keys) {
            heap.insert(k);
        }
        for (int i=0;i<keys.length;i++) {
            heap.deleteMin();
            FiboHeapPrinter.printHeap(heap);
        }
        System.out.println("done");
        //FibonacciHeap heap1 = testMeld();
        //heap1.printHeap();
    }
}
