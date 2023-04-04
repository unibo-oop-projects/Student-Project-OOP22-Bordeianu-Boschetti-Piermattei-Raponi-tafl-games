package taflgames.model.cell.code;

import java.util.List;
import java.util.Map;

import taflgames.common.Player;
import taflgames.common.api.Vector;
import taflgames.common.code.Position;
import taflgames.controller.entitystate.CellState;
import taflgames.controller.entitystate.CellStateImpl;
import taflgames.model.cell.api.Cell;
import taflgames.model.pieces.api.Piece;

public final class Throne extends AbstractCell {

    private static final String THRONE_TYPE = "Throne";
    private static final String KING = "KING";

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(
        final Position source,
        final Piece sender,
        final List<String> events, 
        final Map<Player, Map<Position, Piece>> pieces,
        final Map<Position, Cell> cells
    ) {
        // Empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAccept(final Piece piece) {
        return KING.equals(piece.getMyType().getTypeOfPiece()) && super.isFree();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return THRONE_TYPE;
    }

    @Override
    public final CellState getSubclassCellState() {
        return new CellStateImpl(this.getType(), Vector.UP_VECTOR, null);
    }
    
}
