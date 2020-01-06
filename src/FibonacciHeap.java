import java.util.Arrays;

/**
 * FibonacciHeap
 * <p>
 * An implementation of fibonacci heap over integers.
 */
public class FibonacciHeap {

    public static final double GOLDEN = (1 + Math.sqrt(5)) / 2;

    private HeapNode minNode;
    private HeapNode first;
    private HeapNode last;
    private int size;
    public static int totalLinks;
    public static int totalCuts;
    private int numMarked;
    private int numOfTrees;

    /**
     * Links 2 nodes.
     * n1's root will be the root of the link's result
     * complexity: O(1) - pointer changes
     */
    private HeapNode link(HeapNode n1, HeapNode n2) {
        if (n1.getKey() > n2.getKey()) {
            // Make sure n1 has the smallest root
            HeapNode temp = n1;
            n1 = n2;
            n2 = temp;
        }
        if (n1.getChild() == null) {
            n2.setNext(n2);
            n2.setPrev(n2);
        } else {
            n2.setNext(n1.getChild().getNext());
            n1.getChild().getNext().setPrev(n2);
            n1.getChild().setNext(n2);
            n2.setPrev(n1.getChild());
        }
        n1.setChild(n2);
        n2.setParent(n1);
        n1.setRank(n1.getRank() + 1);
        totalLinks++;
        numOfTrees = numOfTrees - 1;
        return n1;
    }

    private void toBuckets(HeapNode[] cells) {
        HeapNode node = this.first;
        node.getPrev().setNext(null);
        HeapNode cur;
        while (node != null) {
            cur = node;
            node = node.getNext();
            while (cells[cur.getRank()] != null) {
                cur = link(cur, cells[cur.getRank()]);
                cells[cur.getRank() - 1] = null;
            }
            cells[cur.getRank()] = cur;
        }
    }

    /**
     * Inserts n2 after n1
     * Complexity: O(1)
     */
    private void insertAfter(HeapNode n1, HeapNode n2) {
        n2.setNext(n1.getNext());
        n1.getNext().setNext(n2);
        n1.setNext(n2);
        n2.setPrev(n1);
    }

    /**
     * returns array with first root, last root, minimum root and amount of trees we have
     */
    private HeapNode[] fromBuckets(HeapNode[] cells) {
        HeapNode minNode = null;
        HeapNode lastNode = null;
        HeapNode firstNode = null;
        int treesAmount = 0;
        for (HeapNode cell : cells) {
            if (cell != null) {
                lastNode = cell;
                treesAmount++;
                if (firstNode == null) firstNode = lastNode;
                if (minNode == null) {
                    minNode = lastNode;
                    minNode.setNext(minNode);
                    minNode.setPrev(minNode);
                } else {
                    insertAfter(minNode, lastNode);
                    if (lastNode.getKey() < minNode.getKey()) {
                        minNode = lastNode;
                    }
                }
            }
        }
        // treesAmount encapsualted by HeapNode just to avoid creating a new class
        // for this function's result
        HeapNode[] result = {firstNode, lastNode, minNode, new HeapNode(treesAmount)};
        return result;
    }

    public int consolidate() {
        int cellsAmount = 1+(int) Math.ceil(Math.log(size()) / Math.log(GOLDEN));
        HeapNode[] cells = new HeapNode[cellsAmount];
        toBuckets(cells);
        HeapNode[] res = fromBuckets(cells);
        this.first = res[0];
        this.last = res[1];
        this.minNode = res[2];
        return res[3].getKey();
    }

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
        //if heap is empty
        if (size == 0) {
            first = newNode;
            last = newNode;
            newNode.next = newNode;
            newNode.prev = newNode;
            minNode = newNode;
        }//heap is not empty
        else {
            //insert new node at beginning of heap
            swapFirst(newNode);
        }
        size++;
        return first;
    }

    /*insert a non new node to beginning*/
    private void insertNodeAtStart(HeapNode node) {
        swapFirst(node);
    }

    /*makes node the new first of the heap, pushes old first ahead of it*/
    private void swapFirst(HeapNode node) {
        HeapNode oldFirst = first;
        first = node;
        first.next = oldFirst;
        first.prev = last;
        oldFirst.prev = first;
        last.next = first;
        if (node.getKey() < minNode.getKey()) { //check if new key will be new minimum
            minNode = first;
        }
        numOfTrees++;
    }

    /**
     * public void deleteMin()
     * <p>
     * Delete the node containing the minimum key.
     */
    public void deleteMin() {
        HeapNode minNode = this.minNode;
        HeapNode child = minNode.getChild();
        if (child == null) {
            minNode.getPrev().setNext(minNode.getNext());
            minNode.getNext().setPrev(minNode.getPrev());
        } else {
            minNode.getPrev().setNext(child);
            child.setPrev(minNode.getPrev());
            child.setParent(null);
        }
        minNode.setChild(null);

        int newTreesAmount = consolidate();
        this.numOfTrees = newTreesAmount;
        this.size--;


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
        if (this.isEmpty()) {
            heap1EmptyMeld(heap2);
            return;
        }
        //if heap2 is empty do nothing
        if (heap2.isEmpty()) {
            return;
        }
        //else, connect last of heap1 to first of heap2
        this.last.next = heap2.first;
        heap2.first.prev = this.last;
        heap2.last.next = first;
        first.prev = heap2.last;
        this.last = heap2.last;
        /*update size,numOftrees,numOfMarked*/
        size += heap2.size;
        numOfTrees += heap2.numOfTrees;
        numMarked += heap2.numMarked;
        //updateMin
        if (this.minNode.getKey() > heap2.minNode.getKey()) {
            minNode = heap2.minNode;
        }
    }

    //melds with heap2 ig this heap is empty
    private void heap1EmptyMeld(FibonacciHeap heap2) {
        first = heap2.first;
        last = heap2.last;
        minNode = heap2.minNode;
        size = heap2.size;
        numMarked = heap2.numMarked;
        numOfTrees = heap2.numOfTrees;
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
        if (size == 1) {
            maxRank = first.getRank();
            int[] resArray = new int[maxRank + 1];
            resArray[maxRank] = 1;
            return resArray;
        }
        while (tempFirst != first) {
            arr[tempFirst.getRank()]++;
            if (tempFirst.getRank() > maxRank) { //update max rank
                maxRank = tempFirst.getRank();
            }
            tempFirst = tempFirst.getNext();
        }
        int[] resArray = Arrays.copyOf(arr, maxRank + 1);
        return resArray;
    }

    /**
     * public void delete(HeapNode x)
     * <p>
     * Deletes the node x from the heap.
     */
    public void delete(HeapNode x) {
        decreaseKey(x, minNode.getKey() - 1);
        deleteMin();
    }

    /**
     * public void decreaseKey(HeapNode x, int delta)
     * <p>
     * The function decreases the key of the node x by delta. The structure of the heap should be updated
     * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
     */
    public void decreaseKey(HeapNode x, int delta) {
        x.setKey(x.getKey() - delta);
        /*if x is not a root check if we need to cut*/
        if (x.getParent() != null) {
            /*if the decrease cause a heap violation*/
            if (x.getParent().getKey() > x.getKey()) {
                cascadingCut(x, x.getParent());
            }
        }
    }

    private void cascadingCut(HeapNode node, HeapNode parent) {
        /*while parent is not the root*/
        while (parent.getParent() != null) {
            HeapNode curParent = parent;
            cut(node, parent);
            insertNodeAtStart(node);
            /*parent is not marked, cut and break form cuts*/
            if (parent.isMarked == false) {
                parent.mark();
                break;
            } else { /*keep cutting, node is now old parent, parent is old parents parent*/
                node = curParent;
                parent = node.getParent();
            }
        }
    }


    private void cut(HeapNode node, HeapNode parent) {
        node.parent = null;
        node.unmark();
        parent.setRank(parent.getRank() - 1);
        if (node.getNext() == node) {
            parent.child = null;
        } else {
            parent.child = node.next;
            node.prev.next = node.next;
            node.next.prev = node.prev;

        }
        totalCuts++;

    }


    /**
     * public int potential()
     * <p>
     * This function returns the current potential of the heap, which is:
     * Potential = #trees + 2*#marked
     * The potential equals to the number of trees in the heap plus twice the number of marked nodes in the heap.
     */
    public int potential() {
        return this.numOfTrees + 2 * this.numMarked; // should be replaced by student code
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


    public int getNumberOfTrees() {
        return numOfTrees;
    }

    public String printTree(HeapNode root) {
        StringBuilder s = new StringBuilder();
        s.append(", "+root.getKey());
        s.append("\n");
        HeapNode curr=root.getChild();

        while(curr!=null) {
            s.append(printLinkedList(curr));
            s.append("\n");
            curr = curr.getChild();
        }
        return s.toString();
    }

    public String printLinkedList(HeapNode n) {
        HeapNode p = n;
        StringBuilder s = new StringBuilder();
        int startKey = p.getKey();
        do {
            s.append(", "+p.getKey());
            p = p.getNext();
        }
        while(p!=null && p.getKey()!=startKey);
        return s.toString();
    }

    public void printHeap(){
        HeapNode curr = first;
        int startKey = curr.getKey();
        do {
            System.out.println(printTree(curr));
            System.out.println("_________________________________");
            curr = curr.getNext();
        }
        while(curr!=null && curr.getKey()!=startKey);
    }

    public HeapNode getFirst() {
        return first;
    }

    public void setSize(int i) {
        this.size = i;
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

        public void setChild(HeapNode child) {
            this.child = child;
        }

        public void mark() {
            this.isMarked = true;
        }

        public void unmark() {
            this.isMarked = false;
        }
    }
}
