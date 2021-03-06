package edu.princeton.safe.internal;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.cursors.IntCursor;

import edu.princeton.safe.model.Domain;

public class DefaultDomain implements Domain {

    IntArrayList attributeIndexes;
    int index;
    String name;
    double[] color;

    public DefaultDomain() {
        attributeIndexes = new IntArrayList();
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void forEachAttribute(IntConsumer action) {
        attributeIndexes.forEach((Consumer<? super IntCursor>) (IntCursor c) -> action.accept(c.value));
    }

    @Override
    public int getAttribute(int memberIndex) {
        return attributeIndexes.get(memberIndex);
    }

    @Override
    public int getAttributeCount() {
        return attributeIndexes.size();
    }

    public void addAttribute(int attributeIndex) {
        attributeIndexes.add(attributeIndex);
    }

    @Override
    public double[] getColor() {
        return color;
    }

    @Override
    public void setColor(double[] color) {
        this.color = color;
    }

}
