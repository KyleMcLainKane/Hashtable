/**
 * Program #4 Hashtable
 * This is the HashTable data structure, with the code for linear list after the iterator for hash.
 * CS310-01
 * 9-May-2019
 * @author Kyle McLain cssc 1497
 */

package data_structures;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Hashtable<K extends Comparable<K>, V extends Comparable<V>> implements DictionaryADT<K, V> {
	private int currentSize;
	private int maxSize;
	private int tableSize;
	private long modCounter;
	private LinearList<DictionaryNode<K,V>>[] list;
	

	/**
	 * This is the setup for the hashtable
	 * Code adapted/modified from "Lecture Notes & Supplementary Material" by Alan Riggins
	 * @param <K>
	 * @param <V>
	 */
	private class DictionaryNode<K,V> implements Comparable<DictionaryNode<K,V>>{
		K key;
		V value;
		
		public DictionaryNode(K k, V v) {
			key = k;
			value = v;
		}
		
		public int compareTo(DictionaryNode<K,V> node) {
			return ((Comparable<K>)key).compareTo((K)node.key);
		}
		
	}
	
	public Hashtable(int n) {
		currentSize = 0;
		maxSize = n;
		modCounter = 0;
		
		tableSize = (int) (maxSize * 1.3f);
		list = new LinearList[tableSize];
		for(int i = 0; i < tableSize; i++) {
			list[i] = new LinearList<DictionaryNode<K,V>>();
		}
	}
	
	/**
	 * Returns true if the dictionary has an object identified by key in it, otherwise false.
	 * Code adapted/modified from "Lecture Notes & Supplementary Material" by Alan Riggins
	 * @param key
	 * @return boolean
	 */
	@Override
	public boolean contains(K key) {
		return list[getHashCode(key)].contains(new DictionaryNode<K,V>(key,null));
	}

	/**
	 * Adds the given key/value pair to the dictionary. Returns false if the dictionary is full,
	 *  or if the key is a duplicate.Returns true if addition succeeded.
	 * Code adapted/modified from "Lecture Notes & Supplementary Material" by Alan Riggins
	 * @param key
	 * @param value
	 * @return boolean
	 */
	@Override
	public boolean add(K key, V value) {
		if(isFull())			//Checking to see if it is full
			return false;
		
		if(list[getHashCode(key)].contains(new DictionaryNode<K,V>(key, null)))	//Checking for duplicates
			return false;
		
		list[getHashCode(key)].addFirst(new DictionaryNode<K,V>(key, value));
		currentSize++;
		modCounter++;
		return true;
	}

	/** 
	 * Deletes the key/value pair identified by the key parameter. Returns true if the key/value pair was found and removed, otherwise false.
	 * @param key
	 * @return boolean
	 */
	@Override
	public boolean delete(K key) {
		if(isEmpty()) {
			return false;
		}
		
		if(list[getHashCode(key)].remove(new DictionaryNode<K,V>(key,null)) == null)
			return false;
		
		currentSize--;
		modCounter++;
		return true;
	}

	/** 
	 * Returns the value associated with the parameter key. Returns null if the key is not found or the dictionary is empty.
	 * Code adapted/modified from "Lecture Notes & Supplementary Material" by Alan Riggins
	 * @param key
	 * @return V
	 */
	@Override
	public V getValue(K key) {
		DictionaryNode<K,V> tmp = list[getHashCode(key)].find(new DictionaryNode<K,V>(key,null));
		if(tmp == null) return null;
		return tmp.value;
	}

	private int getHashCode(K key) {
		return (key.hashCode() & 0x7FFFFFFF) % tableSize;
	}
 
	/** 
	 * Returns the key associated with the parameter value. Returns 
	 * null if the value is not found in the dictionary. If more than one key exists that matches the given value, returns the first one found
	 * @param value
	 * @return K
	 */
	@Override
	public K getKey(V value) {
		K tmp = getHashKey(value);
		return tmp;
	}

	private K getHashKey(V value) {
		for(int i = 0; i < tableSize; i++) {
			for(DictionaryNode<K,V> node : list[i]) 
				if(((Comparable<V>)node.value).compareTo(value) == 0) 
					return node.key;			
		}
		return null;
	}
	
	/** 
	 * Returns the number of key/value pairs currently stored in the dictionary
	  * @return int
	 */
	@Override
	public int size() {
		return currentSize;
	}

	/** 
	 *  Returns true if the dictionary is at max capacity
	  * @return boolean
	 */
	@Override
	public boolean isFull() {			
		return currentSize == maxSize;
	}

	/**
	 *  Returns true if the dictionary is empty
	  * @return boolean
	 */
	@Override
	public boolean isEmpty() {
		return currentSize == 0;
	}

	/**  
	 * Returns the Dictionary object to an empty state.
	 */
	@Override
	public void clear() {
		for(int i = 0; i < tableSize; i++) {
			list[i].clear();
		}
		currentSize = 0;
		modCounter++;
	}
	
	/**
	 * Returns an Iterator of the keys in the dictionary, in ascending sorted order. The iterator must be fail-fast
	 * Code adapted/modified from "Lecture Notes & Supplementary Material" by Alan Riggins
	 */
	@Override
	public Iterator<K> keys() {
		return new KeyIteratorHelper<K>();
	}
	
	class KeyIteratorHelper<K> extends IteratorHelper<K>{
		public KeyIteratorHelper() {
			super();
		}
		
		public K next() {
			return (K) nodes[idx++].key;
		}
	}

	/** 
	 * Returns an Iterator of the values in the dictionary. The order of the values must match the order of the keys. 
	 * The iterator must be fail-fast
	 * Code adapted/modified from "Lecture Notes & Supplementary Material" by Alan Riggins
	 */
	@Override
	public Iterator<V> values() {
		return new ValueIteratorHelper<V>();		
	}
	
	class ValueIteratorHelper<V> extends IteratorHelper<V>{
		public ValueIteratorHelper() {
			super();
		}
		
		public V next() {
			return (V) nodes[idx++].value;
		}
	}
	
	abstract class IteratorHelper<E> implements Iterator<E>{	
		protected DictionaryNode<K,V> [] nodes;
		protected int idx;
		protected long modCheck;
		protected DictionaryNode<K,V> temp;
		
		public IteratorHelper() {
			nodes = new DictionaryNode[currentSize];
			idx = 0; 
			int j = 0;
			modCheck = modCounter;
			
			for(int i = 0; i < tableSize; i++)
				for(DictionaryNode<K, V> n : list[i])
					nodes[j++] = n;

			//Shell Sort
			int h = 1;
			int size = currentSize;
			
			while(h <= size/3) 
				h = h * 3 + 1;
			
			while(h > 0) {				
				for(int out = h; out < size; out++) {
					temp = nodes[out];					
					int in = out;
					
					while(in > h-1 && nodes[in-h].compareTo(temp) >= 0) {
						nodes[in] = nodes[in-h];
						in -= h;
					}
					
					nodes[in] = temp;	
	
				}
				h = (h-1)/3;
			}
			
		}
		
		public boolean hasNext() {
			if(modCheck != modCounter)
				throw new ConcurrentModificationException();
			return idx < currentSize;
		}
		public abstract E next();
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	
	/**
	 * Program LinearList
	 * This is a doubly linked list with functions to add and remove and alter this list 
	 * @author Kyle McLain cssc 1497
	 */
	private class LinearList <E extends Comparable <E>> implements Iterable<E>{
		
		private long modificationCounter;
		private int currentSize;
		private Node<E> previous = null;
		
		/**
		 *	This it is a no argument constructor
		 * @param <E>
		 */	
		private class Node<E>{
			E data;
			Node<E> next;
			Node<E> previous;
			public Node(E obj) {
				data = obj;
				next = null;
				previous = null;
			}
		}
		
		private Node<E> head, tail;

		public LinearList() {
			tail = head = null;
			currentSize = 0;
			modificationCounter = 0;
		}

		/** Adds the Object obj to the beginning of list and returns true if the list
		 * is not full.
		 * returns false and aborts the insertion if the list is full.
		 * @param obj
		 * @return boolean
		 */
		public boolean addFirst(E obj) {
		
			Node<E> newNode = new Node<E>(obj);
			
			if(isEmpty()) {
				head = tail = newNode;
			}
			
			else{
				newNode.next = head;
				head.previous = newNode;
				head = newNode;	
			}
			
			modificationCounter++;
			currentSize++;
			return true;
		}

		 /** Adds the Object obj to the end of list and returns true if the list is
		 * not full.
		 * returns false and aborts the insertion if the list is full.
		 * @param obj
		 * @return boolean
		 */
		public boolean addLast(E obj) {

			Node<E> newNode = new Node<E>(obj);
			
			if(isEmpty()) {
				head = tail = newNode;	
			}
			
			else{
				tail.next = newNode;
				newNode.previous = tail;
				tail = newNode;
			}
			
			modificationCounter++;
			currentSize++;
			return true;
		}

		/** Removes and returns the parameter object obj in first position in list
		 * if the list is not empty, null if the list is empty.
		 * @return E
		 */
		public E removeFirst() {

			if(head == null) {	//IF ITS EMPTY
				return null;
			}	
			
			E tmp = head.data;
			
			if(currentSize == 1) { //IF IT HAS ONE ITEM
				head = tail = null;
				
			}else { //IF IT HAS MORE THAN ONE ITEM
				head = head.next;
				head.previous = null;
			}
			
			modificationCounter++;
			currentSize--;		
			return tmp;
		}

		/** Removes and returns the parameter object obj in last position in list if
		 * the list is not empty, null if the list is empty.
		 * return E
		 */
		public E removeLast() {
			
			if(head == null) {	//IS EMPTY
				return null;
			}
			
			E tmp = tail.data;
			
			if(currentSize == 1) {	//ONE ELEMENT
				head = tail = null;
				
			}else {		//MORE THAN ONE ELEMENT
				
				tail = tail.previous;
				tail.next = null;
				
			}
				
			modificationCounter++;				
			currentSize--;
			
			return tmp;
		}

		 /** Removes and returns the parameter object obj from the list if the list
		 * contains it, null otherwise. The ordering of the list is preserved. 
		 * The list may contain duplicate elements. This method removes and returns
		 * the first matching element found when traversing the list from first
		 * position. Note that you may have to shift elements to fill in the slot
		 * where the deleted element was located.
		 * @param obj
		 * @return E
		 */
		public E remove(E obj) {
			
			if(isEmpty()) {
				return null;
			}
			
			if(((Comparable<E>)head.data).compareTo(obj) == 0) {	//CHECKING IF OBJ = HEAD
				return removeFirst();
			}
			
			if(((Comparable<E>)tail.data).compareTo(obj) == 0) {	//CHECKING IF OBJ = TAIL
				return removeLast();
			}
			
			Node <E> current = head;
			Node <E> previous = null;
			
			while(current != null) {
				
				if(((Comparable<E>)current.data).compareTo(obj) == 0) {
					
					currentSize--;
					modificationCounter++;
					previous.next = current.next;
					current.next.previous = current.previous;
					return current.data;
					
				}				
				
				previous = current;
				current = current.next;
				
			}	
			return null;
		}

		 /** Returns the first element in the list, null if the list is empty.
		 * The list is not modified.
		 * @return E
		 */
		public E peekFirst() {
			if(isEmpty()) {
				return null;
			}
			return head.data;
		}

		/** Returns the last element in the list, null if the list is empty.
		 * The list is not modified.
		 * @return E
		*/
		public E peekLast() {
			if(isEmpty()) {
				return null;
			}
			return tail.data;
		}

		/** Returns true if the parameter object obj is in the list, false otherwise.
		 * The list is not modified.
		 * @param obj
		 * @return boolean
		 */
		public boolean contains(E obj) {
			if(find(obj) == null) {
				return false;
			}
			return true;
		}

		 /** Returns the element matching obj if it is in the list, null otherwise.
		 * In the case of duplicates, this method returns the element closest to
		 * front. The list is not modified.
		 * @param obj
		 * @return E
		 */
		public E find(E obj) {
			
			Node<E> current =  head;
			if(isEmpty()) {
				return null;
			}
			
			while(current != null) {
				if(((Comparable <E>) current.data).compareTo(obj) == 0){
					return current.data;			
				}
				current = current.next;
			}
			return null;
		}
		
		 /** The list is returned to an empty state.
		 */
		public void clear() {
			currentSize = 0;
			modificationCounter++;
			head = tail = null;
		}

		 /** Returns true if the list is empty, otherwise false
		  * @return boolean
		 */
		public boolean isEmpty() {
			return currentSize == 0;
		}

		 /** Returns true if the list is full, otherwise false
		  * @return boolean
		 */
		public boolean isFull() {
			return false;	
		}

		 /** Returns the number of Objects currently in the list.
		  * @return int
		 */
		public int size() {
			return currentSize;
		}

		 /** Returns an Iterator of the values in the list, presented in the same
		 * order as the underlying order of the list. (front first, rear last)
		 */
		@Override
		public Iterator<E> iterator() {
			return new IteratorHelper();
		}
		
		class IteratorHelper implements Iterator <E>{ 
			
			Node<E> current;
			long stateCheck;
			E tmp;
			
			public IteratorHelper() {
				current = head;
				stateCheck = modificationCounter;
			}
			
			public boolean hasNext() {
				if(stateCheck != modificationCounter) {
					throw new ConcurrentModificationException();
				}
				return (current != null);
			}
			
			public E next() {
				if(!hasNext()) {
					throw new NoSuchElementException();
				}
				
				tmp = current.data;
				current = current.next;	
				return tmp;
				
			}
			
			public void remove() {
				throw new UnsupportedOperationException();
			}
		}

	}


}

