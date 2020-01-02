import sun.net.www.HeaderParser;

import java.util.Arrays;

/**
 * FibonacciHeap
 * <p>
 * An implementation of fibonacci heap over integers.
 */
public class FibonacciHeap {

    private HeapNode minNode;
    private HeapNode first;
    private HeapNode last;
    private int size;
    public static int totalLinks;
    public static int totalCuts;
    private int numMarked;

    /**
     * public boolean isEmpty()
     * <p>
     * precondition: none
     * <p>
     * The method returns true if and only if the heap
     * is empty.
     */
    public boolean isEmpty() {
        return size == 0; // should be replaced by student code
    }

    /**
     * public HeapNode insert(int key)
     * <p>
     * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
     */
    public HeapNode insert(int key) {
        HeapNode newNode = new HeapNode(key);
        size++;
        //if heap is empty
        if(size == 0){
            first = newNode;
            last = newNode;
            newNode.next = newNode;
            newNode.prev = newNode;
            minNode = newNode;
        }//heap is not empty
        else {
            //insert new node at beginning of heap
            HeapNode oldFirst = first;
            first = newNode;
            first.next = oldFirst;
            first.prev = last;
            oldFirst.prev = first;
            last.next = first;
            if(key<minNode.getKey()){ //check if new key will be new minimum
                minNode = first;
            }
        }
        return first;
    }

    /**
     * public void deleteMin()
     * <p>
     * Delete the node containing the minimum key.
     */
    public void deleteMin() {
        return; // should be replaced by student code

    }

    /**
     * public HeapNode findMin()
     * <p>
     * Return the node of the heap whose key is minimal.
     */
    public HeapNode findMin() {
        return minNode;
    }

    /**
     * public void meld (FibonacciHeap heap2)
     * <p>
     * Meld the heap with heap2
     */
    public void meld(FibonacciHeap heap2) {
        //if heap1 is empty
        if(this.isEmpty()){
           heap1EmptyMeld(heap2);
        }
        //if heap2 is empty do nothing
        if (heap2.isEmpty()){
            return;
        }
        //else, connect last of heap1 to first of heap2
        this.last.next = heap2.first;
        heap2.first.prev = this.last;
        heap2.last.next = first;
        first.prev = heap2.last;
        //updateMin
        if(this.minNode.getKey()>heap2.minNode.getKey()){
            minNode = heap2.minNode;
        }
    }

    //melds with heap2 ig this heap is empty
    private void heap1EmptyMeld(FibonacciHeap heap2){
        first = heap2.first;
        last = heap2.last;
        minNode = heap2.minNode;
        size = heap2.size;
        numMarked = heap2.numMarked;
    }


    /**
     * public int size()
     * <p>
     * Return the number of elements in the heap
     */
    public int size() {
        return this.size; // should be replaced by student code
    }

    /**
     * public int[] countersRep()
     * <p>
     * Return a counters array, where the value of the i-th entry is the number of trees of order i in the heap.
     */
    public int[] countersRep() {
        int[] arr = new int[size];
        int treeCount = 0;
        int maxRank = 0;

        HeapNode tempFirst = first;
        //only 1 node in heap
        if(size == 1){
            maxRank = first.getRank();
            int [] resArray = new int[maxRank+1];
            resArray[maxRank] = 1;
            return resArray;
        }
        while (tempFirst!=first){
            arr[tempFirst.getRank()]++;
            if(tempFirst.getRank() > maxRank){ //update max rank
                maxRank = tempFirst.getRank();
            }
            tempFirst = tempFirst.getNext();
        }
        int [] resArray = Arrays.copyOf(arr,maxRank+1);
        return resArray;
    }

    /**
     * public void delete(HeapNode x)
     * <p>
     * Deletes the node x from the heap.
     */
    public void delete(HeapNode x) {
        decreaseKey(x,minNode.getKey()-1);
        deleteMin();
    }

    /**
     * public void decreaseKey(HeapNode x, int delta)
     * <p>
     * The function decreases the key of the node x by delta. The structure of the heap should be updated
     * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
     */
    public void decreaseKey(HeapNode x, int delta) {
        return; // should be replaced by student code
    }

    /**
     * public int potential()
     * <p>
     * This function returns the current potential of the heap, which is:
     * Potential = #trees + 2*#marked
     * The potential equals to the number of trees in the heap plus twice the number of marked nodes in the heap.
     */
    public int potential() {
        return this.size + 2 * this.numMarked; // should be replaced by student code
    }

    /**
     * public static int totalLinks()
     * <p>
     * This static function returns the total number of link operations made during the run-time of the program.
     * A link operation is the operation which gets as input two trees of the same rank, and generates a tree of
     * rank bigger by one, by hanging the tree which has larger value in its root on the tree which has smaller value
     * in its root.
     */
    public static int totalLinks() {
        return totalLinks; // should be replaced by student code
    }

    /**
     * public static int totalCuts()
     * <p>
     * This static function returns the total number of cut operations made during the run-time of the program.
     * A cut operation is the operation which diconnects a subtree from its parent (during decreaseKey/delete methods).
     */
    public static int totalCuts() {
        return totalCuts; // should be replaced by student code
    }

    /**
     * public static int[] kMin(FibonacciHeap H, int k)
     * <p>
     * This static function returns the k minimal elements in a binomial tree H.
     * The function should run in O(k(logk + deg(H)).
     */
    public static int[] kMin(FibonacciHeap H, int k) {
        int[] arr = new int[42];
        return arr; // should be replaced by student code
    }

    /**
     * public class HeapNode
     * <p>
     * If you wish to implement classes other than FibonacciHeap
     * (for example HeapNode), do it in this file, not in
     * another file
     */
    public class HeapNode {

        public int key;
        private int rank;
        private boolean isMarked;
        private HeapNode child;
        private HeapNode next;
        private HeapNode prev;
        private HeapNode parent;

        public HeapNode(int key) {
            this.key = key;
        }

        public int getKey() {
            return this.key;
        }

        public void setKey(int key) {
            this.key = key;
        }

        public int getRank() {
            return this.rank;
        }

        public HeapNode getChild() {
            return this.child;
        }

        public HeapNode getNext() {
            return this.next;
        }

        public HeapNode getParent() {
            return this.parent;
        }

        public HeapNode getPrev() {
            return this.prev;
        }

        public boolean isMarked() {
            return this.isMarked;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public void setParent(HeapNode parent) {
            this.parent = parent;
        }

        public void setNext(HeapNode next) {
            this.next = next;
        }

        public void setPrev(HeapNode prev) {
            this.prev = prev;
        }

        public void mark() {
            this.isMarked = true;
        }

        public void unmark() {
            this.isMarked = false;
        }
    }
}
