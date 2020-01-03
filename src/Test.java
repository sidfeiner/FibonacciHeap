import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Test {

    public static Logger logger = Logger.getLogger("Test");

    public static FibonacciHeap createHeap(int size) {
        List<Integer> l = IntStream.rangeClosed(1, size).boxed().collect(Collectors.toList());
        Collections.shuffle(l);
        FibonacciHeap heap = new FibonacciHeap();
        for (Integer n : l) {
            logger.info("inserting " + n);
            heap.insert(n);
        }

        heap.consolidate();
        return heap;

    }

    public static void testFibHeap(FibonacciHeap heap) {
        FibonacciHeap.HeapNode first = heap.findMin();
        FibonacciHeap.HeapNode cur = first;
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
                if (cur.getPrev().getNext() != cur) throw new RuntimeException("getPrev.getNext did not return current root: " + cur.getKey());
                if (cur.getNext().getPrev() != cur) throw new RuntimeException("getNext.getPrev did not return current root: " + cur.getKey());
                cur = cur.getNext();
            } while (cur != child);
        }
    }

    public static void testChildren(FibonacciHeap.HeapNode node) {
        if (node.getChild() != null) {
            if (node.getChild().getParent() != node) throw new RuntimeException("getChild.getParent did not return cur node: " +node.getKey());
            FibonacciHeap.HeapNode child = node.getChild();
            testChildren(child);
            FibonacciHeap.HeapNode cur = child;
            do {
                if (cur.getParent() != node) throw new RuntimeException("getParent of a sibling did not return current node: " + node.getKey());
                cur = cur.getNext();
            } while (cur != child);
        }
    }

    public static void testHeapMinProp(FibonacciHeap.HeapNode node) {
        if (node.getChild() != null) {
            FibonacciHeap.HeapNode firstChild = node.getChild();
            FibonacciHeap.HeapNode curChild = firstChild;
            do {
                if (curChild.getKey() < node.getKey()) throw new RuntimeException("child found with smaller key than parent: " + node.getKey());
                testHeapMinProp(curChild);
                curChild = curChild.getNext();
            } while (curChild != firstChild);
        }
    }

    public static void main(String[] args) {
        FibonacciHeap heap = createHeap(10);
        testFibHeap(heap);
        System.out.println("a");
    }
}
