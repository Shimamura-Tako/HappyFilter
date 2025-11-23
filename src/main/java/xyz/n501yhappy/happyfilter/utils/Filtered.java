package xyz.n501yhappy.happyfilter.utils;

import java.util.ArrayList;
import java.util.List;

public class Filtered {
    private List<Integer> leftIndexes = new ArrayList<>();
    private List<Integer> rightIndexes = new ArrayList<>();
    private boolean isFiltered;

    public Filtered(List<Integer> leftIndexes, List<Integer> rightIndexes, boolean isFiltered) {
        this.leftIndexes = leftIndexes;
        this.rightIndexes = rightIndexes;
        this.isFiltered = isFiltered;
    }

    public List<Integer> getLIndexes() {
        return leftIndexes;
    }

    public List<Integer> getRIndexes() {
        return rightIndexes;
    }

    public boolean isFiltered() {
        return isFiltered;
    }

    public Filtered merge(Filtered other) {
        this.leftIndexes.addAll(other.getLIndexes());
        this.rightIndexes.addAll(other.getRIndexes());
        this.isFiltered = other.isFiltered() || this.isFiltered;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Filtered{");
        sb.append("isFiltered=").append(isFiltered);
        sb.append(", ranges=[");
        
        for (int i = 0; i < leftIndexes.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("[").append(leftIndexes.get(i))
              .append(",").append(rightIndexes.get(i))
              .append(")");
        }
        
        sb.append("], rangesCount=").append(leftIndexes.size());
        sb.append("}");
        return sb.toString();
    }
}