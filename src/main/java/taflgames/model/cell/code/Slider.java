package taflgames.model.cell.code;

import java.util.*;
import taflgames.common.api.Vector;
import taflgames.common.code.Position;
import taflgames.model.board.code.Piece;
import taflgames.model.cell.api.Mediator;
import taflgames.model.cell.api.Resettable;
import taflgames.model.cell.api.TimedEntity;

public class Slider extends AbstractCell implements TimedEntity, Resettable{

    private Vector orientation; //un versore che indica la direzione in cui questo slider punta
	private boolean triggered; //dice se è già stata attivata in questo turno
	private Mediator mediator;
	private Position sliderPos;
	private int lastActivityTurn;
	private boolean active;
	private static final int TURNS_FOR_REACTIVATION = 2;

    public Slider() {
        super();
    }

    @Override
    public boolean canAccept(Piece piece) {
        if(super.isFree()) {
            return true;
        } else {
            return false;
        }
    }
    
    public void notify(Position source, Piece movedPiece, List<String> events) {
        if (events.contains("MOVE")) {
            /* Non mi importa che tipo di pezzo sia arrivato, lo slider lo fa scivolare */
            if (!this.triggered && this.active) {
                this.triggered = true;
                Position p = this.mediator.requestMove(source, this.orientation); /*Trovo la casella più lontana su cui ci si possa
                spostare seguendo la direzione del vettore orientamento */
                this.mediator.updatePiecePos(this.sliderPos, p);
            }
        }
    }

    public void reset() {
        this.triggered = false;
    }

    public void notifyTurnHasEnded(final int turn){
        if (turn - this.lastActivityTurn == Slider.TURNS_FOR_REACTIVATION) {
			this.active = true;
			this.lastActivityTurn = turn;
		}
		else {
			this.active = false;
		}
	}

    @Override
    public String getType() {
        return "Slider";
    }

}
