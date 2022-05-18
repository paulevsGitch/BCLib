package org.betterx.bclib.gui.gridlayout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.betterx.bclib.gui.gridlayout.GridLayout.GridValueType;
import org.betterx.bclib.gui.gridlayout.GridLayout.VerticalAlignment;

import java.util.List;

@Environment(EnvType.CLIENT)
public class GridColumn extends GridContainer {
    GridColumn(double width) {
        super(width);
    }

    GridColumn(double width, GridLayout.GridValueType widthType) {
        super(width, widthType);
    }

    public GridRow addRow() {
        return addRow(VerticalAlignment.TOP);
    }

    public GridRow addRow(VerticalAlignment align) {
        GridRow row = new GridRow(1.0,
                                  widthType == GridValueType.INHERIT
                                          ? GridValueType.INHERIT
                                          : GridLayout.GridValueType.PERCENTAGE,
                                  align);
        this.cells.add(row);
        return row;
    }


    public void addSpacerRow() {
        this.addSpacerRow(4);
    }

    public void addSpacerRow(int height) {
        GridCell cell = new GridCell(1.0, height, GridValueType.PERCENTAGE, null, null);
        this.cells.add(cell);
    }

    @Override
    public int calculateWidth(final int parentWidth) {
        if (widthType == GridValueType.INHERIT) {
            return cells.stream()
                        .filter(row -> row.widthType == GridValueType.INHERIT)
                        .map(row -> row.buildElement(0, 0, 1, 0, 0, null).width)
                        .reduce(0, (p, c) -> Math.max(p, c));

        } else {
            return super.calculateWidth(parentWidth);
        }
    }


    @Override
    protected GridElement buildElementAt(int left, int inTop, int width, final List<GridElement> collector) {
        int height = 0;
        int top = inTop;

        if (widthType == GridValueType.INHERIT) {
            width = calculateWidth(width);
        }

        for (GridCellDefinition row : cells) {
            GridElement element = row.buildElement(width, 0, 1, left, top, collector);
            top += element.height;
            height += element.height;
        }

        return new GridElement(left, inTop, width, height);
    }
}
