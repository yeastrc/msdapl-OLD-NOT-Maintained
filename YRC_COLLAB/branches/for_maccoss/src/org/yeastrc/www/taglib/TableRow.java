/**
 * TableRow.java
 * @author Vagisha Sharma
 * Apr 5, 2009
 * @version 1.0
 */
package org.yeastrc.www.taglib;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class TableRow {

    private List<TableCell> cells;

    public TableRow() {
        this.cells = new ArrayList<TableCell>();
    }
    
    public List<TableCell> getCells() {
        return cells;
    }

    public void setCells(List<TableCell> cells) {
        this.cells = cells;
    }
    
    public void addCell(TableCell cell) {
        cells.add(cell);
    }
}
