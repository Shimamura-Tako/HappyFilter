package xyz.n501yhappy.happyfilter.utils;

import java.util.ArrayList;
import java.util.List;

public class Filtered {
    private List<Integer> LIndexes = new ArrayList<>(), RIndexes = new ArrayList<>();
    private boolean isFiltered;
    public Filtered(List<Integer> LIndexes, List<Integer> RIndexes,boolean isFiltered) {
        this.LIndexes = LIndexes;
        this.RIndexes = RIndexes;
        this.isFiltered = isFiltered;
    }

    public void setFiltered(boolean filtered) {
        isFiltered = filtered;
    }

    public void setLIndexes(List<Integer> LIndexes) {
        this.LIndexes = LIndexes;
    }
    public void setRIndexes(List<Integer> RIndexes) {
        this.RIndexes = RIndexes;
    }
    public List<Integer> getLIndexes() {
        return LIndexes;
    }
    public List<Integer> getRIndexes() {
        return RIndexes;
    }
    public boolean isFiltered() {
        return isFiltered;
    }
    public Filtered merge(Filtered filtered) {
        this.LIndexes.addAll(filtered.getLIndexes());
        this.RIndexes.addAll(filtered.getRIndexes());
        this.isFiltered = filtered.isFiltered() || this.isFiltered;
        return this;
    }
}
