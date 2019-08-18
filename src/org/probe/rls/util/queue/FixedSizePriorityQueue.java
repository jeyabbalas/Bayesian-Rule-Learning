package org.probe.rls.util.queue;

import java.util.Comparator;
import java.util.TreeSet;

public class FixedSizePriorityQueue<E> extends TreeSet<E> {
	
	public FixedSizePriorityQueue(int maxSize) {
        super();
        this.maxSize = maxSize;
        this.elementsLeft = maxSize;
    }

	public FixedSizePriorityQueue(int maxSize, Comparator<E> comparator) {
        super(comparator);
        this.maxSize = maxSize;
        this.elementsLeft = maxSize;
    }


    /**
     * @return true if element was added, false otherwise
     * */
    @Override
    public boolean add(E e) {
        if (elementsLeft == 0 && size() == 0) {
            // max size was initiated to zero, just return false
            return false;
        } else if (elementsLeft > 0) {
        	
            // queue isn't full, add element and decrement elementsLeft
            boolean added = super.add(e);
            System.out.println(added);
            if(!added) {
            	System.out.println(e.toString());
            }
            if (added) {
                elementsLeft--;
            }
            return added;
        } else { // queue is full, remove the smallest element
            int compared = super.comparator().compare(e, this.first());
            if (compared == 1) {
                // new element is larger than the least in queue? pull the least and add new one to queue
                pollFirst();
                super.add(e);
                return true;
            } else {
                // new element is less than the least in queue => return false
                return false;
            }
        }
    }
    
    
    public E poll() {
    	E polledItem = pollLast();
    	
    	if(elementsLeft + 1 <= maxSize) { // space created for more elements
    		elementsLeft++;
    	}
    	
    	return polledItem;
    }
    
    
    private static final long serialVersionUID = 1613660841706848172L;
    
    private int elementsLeft;
    private int maxSize;
    
}