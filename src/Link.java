/**
 * Project: Node Based Agorithm
 * Package: DRE^2
 * File: Link.java
 * Author: Yeswanth Polu
 * Date: Nov, 2020
 * Description:
 */


import java.text.DecimalFormat;
public class Link{

    Edge edge;
    double distance;
    double Rcost;
    double Tcost;    
    double Scost;    
    double energy;
    DecimalFormat fix = new DecimalFormat("##.######");
    
    public Link(Edge edge, double distance, double Rcost, double Tcost, double Scost, double energy) {
        this.edge = edge;
        this.distance = distance;
        this.Rcost = Rcost;
        this.Tcost = Tcost;
        this.Scost = Scost;
        this.energy = energy;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setRCost(double Rcost) {
        this.Rcost = Rcost;
    }
    public void setTCost(double Tcost) {
        this.Tcost = Tcost;
    }
    public void setSCost(double Scost) {
        this.Scost = Scost;
    }
    
    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public Edge getEdge() {
        return edge;
    }

    public double getDistance() {
        return distance;
    }

    public double getRCost() {
        return Rcost;
    }
    
    public double getTCost() {
        return Tcost;
    }
    
    public double getSCost() {
        return Scost;
    }
    
    public double getEnergy() {
        return energy;
    }

    @Override
    public boolean equals(Object o) {
        if (this.edge.getTail()==((Link)o).getEdge().getTail()
            && this.edge.getHead()==((Link)o).getEdge().getHead()){
            return true;
        }
        return false;
    }




    @Override
    public String toString(){

        return "edge: " + edge.toString() +
                ", distance: " + Math.round(distance * 1000.0)/1000.0 +
                ", receivecost: " + Math.round(Rcost * Math.pow(10,7))/Math.pow(10,7) +
                ", transmitcost: " + Math.round(Tcost * Math.pow(10,7))/Math.pow(10,7) +
                ", storagecost: " + Math.round(Scost * Math.pow(10,7))/Math.pow(10,7) +
                ", energycapacity: " + energy;
    }

    public int compareTo(Link value) {
        if (this.getEnergy() < value.getEnergy()) {
            return 1;
        } else if (this.getEnergy() > value.getEnergy()) {
            return -1;
        } else {
            return 0;
        }
    }
}
