/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 */
package cn.solo.sanguosha.block;

import cn.solo.sanguosha.block.TableShape;
import net.minecraft.core.Direction;

final class TableTopology {
    private TableTopology() {
    }

    static Result compute(boolean north, boolean east, boolean south, boolean west) {
        int count = (north ? 1 : 0) + (east ? 1 : 0) + (south ? 1 : 0) + (west ? 1 : 0);
        if (count == 0) {
            return new Result(TableShape.SINGLE, Direction.NORTH);
        }
        if (count == 1) {
            Direction connectionFacing = north ? Direction.NORTH : (east ? Direction.EAST : (south ? Direction.SOUTH : Direction.WEST));
            return new Result(TableShape.END, connectionFacing);
        }
        if (count == 2 && north && south) {
            return new Result(TableShape.CENTER, Direction.SOUTH);
        }
        if (count == 2 && east && west) {
            return new Result(TableShape.CENTER, Direction.EAST);
        }
        if (count == 2) {
            Direction connectionFacing = east && south ? Direction.NORTH : (south && west ? Direction.EAST : (west && north ? Direction.SOUTH : Direction.WEST));
            return new Result(TableShape.CORNER, connectionFacing);
        }
        return new Result(TableShape.FALLBACK, Direction.NORTH);
    }

    record Result(TableShape shape, Direction connectionFacing) {
    }
}

