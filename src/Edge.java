/**
 * Project: Node Based Agorithm
 * Package: DRE^2
 * File: Edge.java
 * Author: Yeswanth Polu
 * Date: Nov, 2020
 * Description: This program helps to store and retrieve the information of the edge between two nodes.
 */


public class Edge{

    private int tail;
    private int head;

    public Edge(int tail, int head, int sort) {
    	if (sort == 1) {
        	if (tail<=head) {
            	this.tail = tail;
            	this.head = head;
        	} else {
            	this.tail = head;
            	this.head = tail;
        	}
    	} else {
            this.tail = tail;
            this.head = head;
    	}
    }
    
    
    public int getTail(){
        return this.tail;
    }

    public int getHead(){
        return this.head;
    }

    @Override
    public boolean equals(Object o) {
        System.out.println("calling Edge's equals()");
        if(this.tail == ((Edge)o).getTail() && this.head == ((Edge)o).getHead()) {
            return true;
        } else if(this.tail == ((Edge)o).getHead() && this.head == ((Edge)o).getTail()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return "(" + tail + " -> " + head + ")";
    }
}
