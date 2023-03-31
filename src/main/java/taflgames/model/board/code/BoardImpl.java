package taflgames.model.board.code;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import taflgames.common.Player;
import taflgames.common.api.Vector;
import taflgames.common.code.Position;
import taflgames.model.board.api.Board;
import taflgames.model.board.api.Eaten;
import taflgames.model.cell.api.Cell;
import taflgames.model.cell.api.Slider;
import taflgames.model.cell.api.TimedEntity;
import taflgames.model.memento.api.BoardMemento;
import taflgames.model.memento.api.CellMemento;
import taflgames.model.memento.api.PieceMemento;
import taflgames.model.pieces.api.Piece;
import taflgames.model.pieces.code.AbstractPiece;

/**
 * This class models a Board {@link taflgames.model.board.api.Board}.
 */
public class BoardImpl implements Board, TimedEntity {

    private Map<Position, Cell> cells;
    private Map<Player, Map<Position, Piece>> pieces;
    private final int size;
    private Position currentPos;
    private Set<Slider> slidersEntities = new HashSet<>();
    private final Eaten eatingManager;

    /**
     * Create a new BoardImpl based on the Map cells, the Map pieces and the size given. 
     * Add sliders to the set slidersEntities.
     * @param pieces the Map that associate to each Player it's own Map of Piece and Position.
     * @param cells the Map of Position and Cell that that associate
     * to each Position of the Board the type of Cell that is placed there.
     * @param size the size of the board.
     */
    public BoardImpl(final Map<Player, Map<Position, Piece>> pieces, final Map<Position, Cell> cells, final int size) {
        this.pieces = pieces;
        this.cells = cells;
        this.size = size;
        this.eatingManager = new EatenImpl(this);
        for (Slider slider : cells.values().stream()
                            .filter(cell -> cell.getType().equals("Slider"))
                            .map(slider -> (Slider) slider)
                            .collect(Collectors.toSet())) {
            slider.addMediator(this);
            slidersEntities.add(slider);
        } 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStartingPointValid(final Position start, final Player player) {
        if (pieces.get(player).containsKey(start) && pieces.get(player).get(start).isAlive()) {
            this.currentPos = start;
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDestinationValid(final Position start, final Position dest, final Player player) {
        Piece piece = pieces.get(player).get(start);
        /*
         * For the pieces that aren't a Swapper it's controlled that the Position of destination is free and can accept them.
         * Otherwise if the Piece is a Swapper it's controlled that the destination cell can accept it.
         */
        if (!piece.canSwap()) {
            if (!cells.get(dest).canAccept(piece)) {
                return false;
            }
        } else if (cells.get(dest).getType().equals("Throne") 
                || cells.get(dest).getType().equals("Exit")
                || (cells.get(start).getType().equals("Slider") && !cells.get(dest).isFree())
                || (!cells.get(dest).isFree() && getPieceAtPosition(dest).getPlayer().equals(player))) {
                return false;
        }

        /*This set represent the possible movements of a piece expressed by Vector*/
        Set<Vector> vectors = piece.whereToMove();
        /* Nel caso delle pedine normali, i vettori restituiti saranno (-1,0), (0,1), (1,0), (0,-1).
        *
        * NOTA1: uno spostamento equivale a sommare la posizione di partenza a un vettore v che indica lo spostamento: start + v = dest
        *
        * NOTA2: per fare un esempio, se una pedina si sposta di N caselle a destra, ciò equivale a dire che dest = start + N * (0, 1);
        * ciò equivale anche a dire che lo spostamento dato dal vettore (0, 1) è applicato N volte.
        *
        * QUINDI, per verificare se una mossa è valida, si verifica se, per uno dei vettori (v) dati da piece.getVectors(),
        * esiste uno scalare N t.c. start + N * v = dest.
        * Se se ne trova uno, si deve verificare che tutte le celle nel percorso che porta la pedina da start a dest siano libere.
        * Se lo sono, allora la mossa è valida, altrimenti non lo è e si deve continuare la ricerca.
        */
        //controllo se la cella di arrivo è libera per lo swapper, poichè se la cella non fosse libera dovrei gestire lo swapper come viene fatto dopo questo if
        if (cells.get(dest).isFree()) {
            for (Vector vector : vectors) {
                for (int numberOfBox = 1; numberOfBox < this.size; numberOfBox++) {
                    if (vector.multiplyByScalar(numberOfBox).applyToPosition(start).equals(dest)) {
                        if (isPathFree(start, dest)) {
                            return true;
                        }
                    }
                }
            }
        }

        /* SUPPONIAMO di non averne trovato nessuno. Allora si procede a verificare se è comunque uno spostamento valido secondo
        * altre proprietà della pedina.
        * QUINDI, per trattare il caso dello Swapper (che nel nostro caso è l'unico con una mossa speciale), possiamo dotare
        * qualsiasi pedina di un metodo canSwap(), che chiaramente ritorna true nel caso sia uno Swapper e false altrimenti.
        */
        if (piece.canSwap() 
            && (!cells.get(dest).getType().equals("Exit") || !cells.get(dest).getType().equals("Throne"))
            && !cells.get(dest).isFree()) {
            // Si verifica se la posizione dest è una delle posizioni occupate da una pedina avversaria.
            // Se lo è, allora la mossa è valida, altrimenti no.

            // trovo la tipologia di pedina nella casella di destinazione dopodichè controllo che non sia un re poichè lo swapper non può scambiare posizione con un re
            Piece destPiece = getPieceAtPosition(dest);
            if (destPiece != null && destPiece.getMyType().getTypeOfPiece().equals("KING")) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void movePlaceholder(final Position oldPos, final Position newPos, final Player currentPlayer) {
        final Piece pieceInTurn = getPieceAtPosition(oldPos);
        if (cells.get(newPos).isFree()) {
            pieces.get(currentPlayer).remove(oldPos);
            pieces.get(currentPlayer).put(newPos, pieceInTurn);
            pieceInTurn.setCurrentPosition(newPos);
            cells.get(oldPos).setFree(true);
            cells.get(newPos).setFree(false);
            this.currentPos = newPos;

        } else if (pieceInTurn.canSwap()) {
            pieces.get(currentPlayer).remove(oldPos);
            pieces.get(currentPlayer).put(newPos, pieceInTurn);
            pieceInTurn.setCurrentPosition(newPos);

            Piece pieceToSwap = pieces.get(Player.values()[(currentPlayer.ordinal() + 1) % Player.values().length]).get(newPos);
            pieces.get(Player.values()[(currentPlayer.ordinal() + 1) % Player.values().length]).remove(newPos);
            pieces.get(Player.values()[(currentPlayer.ordinal() + 1) % Player.values().length]).put(oldPos, pieceToSwap);
            pieceToSwap.setCurrentPosition(oldPos);
            this.currentPos = newPos;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePiecePos(final Position oldPos, final Position newPos, final Player currentPlayer) {
        movePlaceholder(oldPos, newPos, currentPlayer);
        signalOnMove(currentPos, getPieceAtPosition(currentPos));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Position getFurthestReachablePos(final Position startPos, final Vector direction) {
        Position furthestReachable = startPos;
        for (int numberOfBox = 1; numberOfBox < this.size; numberOfBox++) {
            Position reachablePos = direction.multiplyByScalar(numberOfBox).applyToPosition(startPos);
            if (reachablePos.getX() == this.size || reachablePos.getY() == this.size
                || reachablePos.getX() < 0 || reachablePos.getY() < 0 
                || !cells.get(reachablePos).canAccept(getPieceAtPosition(startPos))) {
                    if (getPieceAtPosition(startPos).canSwap()) {
                        if (!cells.get(reachablePos).isFree()
                            && (!cells.get(reachablePos).getType().equals("Throne") 
                                    || !cells.get(reachablePos).getType().equals("Exit"))
                            && cells.get(startPos).getType().equals("Sider")
                            && getPieceAtPosition(reachablePos).getPlayer().equals(getPieceAtPosition(startPos).getPlayer())) {
                            furthestReachable = reachablePos;
                        }
                    }
                break;
            } else {
                furthestReachable = reachablePos;
            }
        } 
        return furthestReachable;
    }

    /**
     * This method is callid by {@link #updatePiecePos(Position, Position, Player)}.
     * It notify a Cell that a Piece is moved there, the cells adjacents to the piece are notified too.
     * This method is fundamental for special cells like sliders and tombs.
     * @param source the Position where the Piece moved to.
     * @param movedPiece the Piece that was moved.
     */
    private void signalOnMove(final Position source, final Piece movedPiece) {
        if (cells.get(source).getType().equals("Slider")) {
            cells.get(source).notify(source, movedPiece, List.of(movedPiece.sendSignalMove()), pieces, cells);
        }
        // Ottengo le posizioni delle celle che potrebbero avere interesse nel conoscere l'ultima mossa fatta
        Set<Position> triggeredPos = eatingManager.trimHitbox(movedPiece, pieces, cells, size).stream()
                .collect(Collectors.toSet());
        // Controllo se nelle posizioni ottenute ci sono entità; in caso, vengono triggerate
        if (!triggeredPos.isEmpty()) {
            for (Position pos : triggeredPos) {
                Cell cell = cells.get(pos);
                cell.notify(source, movedPiece, List.of(movedPiece.sendSignalMove()), pieces, cells);
            }
        }
    }

    /**
     * This method verify if the path between two Position that are on the same row or column is free from pieces.
     * @param start the starting Position.
     * @param dest the Position to reach.
     * @return true if the path is free, false otherwise 
     */
    private boolean isPathFree(final Position start, final Position dest) {
        if (start.getX() == dest.getX()) { 
            if (start.getY() < dest.getY()) {
                for (int i = start.getY() + 1; i < dest.getY(); i++) {
                    if (!cells.get(new Position(start.getX(), i)).canAccept(getPieceAtPosition(start))) {
                        return false;
                    }
                }
            } else {
                for (int i = start.getY() - 1; i > dest.getY(); i--) {
                    if (!cells.get(new Position(start.getX(), i)).canAccept(getPieceAtPosition(start))) {
                        return false;
                    }
                }
            }
        } else {
            if (start.getX() < dest.getX()) {
                for (int i = start.getX() + 1; i < dest.getX(); i++) {
                    if (!cells.get(new Position(i, start.getY())).canAccept(getPieceAtPosition(start))) {
                        return false;
                    }
                }
            } else {
                for (int i = start.getX() - 1; i > dest.getX(); i--) {
                    if (!cells.get(new Position(i, start.getY())).canAccept(getPieceAtPosition(start))) {
                        return false;
                    }
                }
            }
        } 
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyTurnHasEnded(final int turn) {
        if (this.slidersEntities != null) {
            this.slidersEntities.forEach(e -> e.reset());
        } 
        if (this.slidersEntities != null) {
            this.slidersEntities.forEach(elem -> elem.notifyTurnHasEnded(turn));
        }
    }

    public Map<Position, Cell> getMapCells() {
        return Collections.unmodifiableMap(this.cells);
    }

    public Map<Player, Map<Position, Piece>> getMapPieces() {
        return Collections.unmodifiableMap(this.pieces);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eat() {
        Piece currPiece = getPieceAtPosition(currentPos);
        Set<Position> updatedHitbox = eatingManager.trimHitbox(currPiece, pieces, cells, size);
        if (!updatedHitbox.isEmpty()) {
            List<Piece> enemies = eatingManager.getThreatenedPos(updatedHitbox, pieces, currPiece);
            if (!enemies.isEmpty()) {
                Map<Piece, Set<Piece>> enemiesAndAllies = eatingManager.checkAllies(enemies, pieces, currPiece, cells, size);
                eatingManager.notifyAllThreatened(enemiesAndAllies, currPiece, cells, pieces);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDraw(final Player playerInTurn) {
        /*finding king position */
        Piece king = pieces.get(Player.DEFENDER).entrySet().stream()
                        .filter(elem -> elem.getValue().getMyType().getTypeOfPiece().equals("KING"))
                        .map(x -> x.getValue())
                        .findAny()
                        .get();
        /*If the king is on the border, the position adjacent to it are controlled to see if the king is trapped */

        if (king.getCurrentPosition().getX() == 0 || king.getCurrentPosition().getY() == 0
                || king.getCurrentPosition().getX() == this.size - 1 || king.getCurrentPosition().getX() == this.size - 1) {

                if (getAdjacentPositions(king.getCurrentPosition()).stream()
                    .filter(pos -> !cells.get(pos).isFree())
                    .filter(pos -> pieces.get(Player.ATTACKER).keySet().contains(pos))
                    .collect(Collectors.toSet())
                    .size() == 3) {
                                return true;
                }
        /* If there are no pieces that can move for the player in turn, it is automatically a draw. */
        }
        if (pieces.get(playerInTurn).values().stream()
            .filter(piece -> !getAdjacentPositions(piece.getCurrentPosition()).stream()
                .filter(adjPos -> cells.get(adjPos).canAccept(piece))
                .collect(Collectors.toSet()).isEmpty())
            .findAny().isPresent()) {
                return false;
        } else {
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Player> hasAPlayerWon() {
        if (pieces.get(Player.DEFENDER).entrySet().stream()
            .filter(elem -> elem.getValue().getMyType().getTypeOfPiece().equals("KING"))
            .findAny()
            .isEmpty()) {
                return Optional.of(Player.ATTACKER);
        } else {
            Piece king = pieces.get(Player.DEFENDER).entrySet().stream()
                .filter(elem -> elem.getValue().getMyType().getTypeOfPiece().equals("KING"))
                .map(position -> position.getValue())
                .findAny()
                .get();
            if(cells.get(king.getCurrentPosition()).getType().equals("Exit")) {
                return Optional.of(Player.DEFENDER);
            } else {
                return Optional.empty();
            }
        } 
    }

    /**
     * Return the Piece that is on the given Position. 
     * @param pos the position where the piece is located.
     * @return the Piece that is on the Position given.
     */
    private Piece getPieceAtPosition(final Position pos) {
        Piece p = pieces.entrySet().stream()
            .filter(x -> x.getValue().containsKey(pos))
            .map(x -> x.getValue().get(pos))
            .findAny()
            .get();
        return p;
    }

    private Set<Position> getAdjacentPositions(final Position currPos) {
        Set<Position> setOfPosition = new HashSet<>();
        setOfPosition.add(new Position(currPos.getX() + 1, currPos.getY()));
        setOfPosition.add(new Position(currPos.getX() - 1, currPos.getY()));
        setOfPosition.add(new Position(currPos.getX(), currPos.getY() + 1));
        setOfPosition.add(new Position(currPos.getX(), currPos.getY() - 1));
        return setOfPosition.stream()
                                .filter(pos -> pos.getX() >= 0 && pos.getY() >= 0 && pos.getX() < this.size && pos.getY() < this.size)
                                .collect(Collectors.toSet());
    }
 
    /**
     * This class is used to save the Board's current status.
     */
    public class BoardMementoImpl implements BoardMemento {
        private final Map<Position, Cell> innerCells;
        private final Map<Position, Piece> innerAttackerPieces;
        private final Map<Position, Piece> innerDefenderPieces;
        private final Position innerCurrentPos;
        private final List<PieceMemento> piecesMemento;
        private final List<CellMemento> cellsMemento;
        private Set<Slider> innerSlidersEntities = null;

        /**
         * Creates a BoardMemento from which the board will be able to restore its previous state.
         * @param piecesMemento a List of the saved states of the pieces.
         * @param cellsMemento a List of the saved states of the cells.
         */
        public BoardMementoImpl(final List<PieceMemento> piecesMemento, final List<CellMemento> cellsMemento) {
            this.innerCells = BoardImpl.this.cells.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            this.innerAttackerPieces = BoardImpl.this.pieces.get(Player.ATTACKER).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            this.innerDefenderPieces = BoardImpl.this.pieces.get(Player.DEFENDER).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            this.innerCurrentPos = BoardImpl.this.currentPos;
            if (BoardImpl.this.slidersEntities != null) {
                this.innerSlidersEntities = BoardImpl.this.slidersEntities.stream()
                    .collect(Collectors.toSet());
            }

            this.piecesMemento = piecesMemento;
            this.cellsMemento = cellsMemento;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<PieceMemento> getPiecesMemento() {
            return this.piecesMemento;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<CellMemento> getCellsMemento() {
            return this.cellsMemento;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<Position, Cell> getInnerCells() {
            return this.innerCells;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<Position, Piece> getInnerAttackerPieces() {
            return this.innerAttackerPieces;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<Position, Piece> getInnerDefenderPieces() {
            return this.innerDefenderPieces;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Position getInnerCurrentPos() {
            return this.innerCurrentPos;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<Slider> getInnerSlidersEntities() {
            return this.innerSlidersEntities != null ? this.innerSlidersEntities : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void restore() {
            BoardImpl.this.restore(this);
        }

    }

    /**
     * Saves a snapshot of the current state of this board.
     * @return the BoardMemento deriving from the saving of the board status.
     */
    @Override
    public BoardMemento save() {
        return this.new BoardMementoImpl(
            this.pieces.entrySet().stream()
            .flatMap(entry -> entry.getValue().values().stream())
            .map(piece -> (AbstractPiece) piece)
            .map(piece -> piece.save())
            .toList(), 
            this.cells.values().stream()
            .map(cell -> cell.save())
            .toList());
    }

    /**
     * Restores the status of this board to the one contained in the
     * {@link taflgames.model.board.code.BoardImpl.BoardMementoImpl}.
     * @param bm the BoardMemento from which to extract the information
     * required to restore the state of the board.
     */
    private void restore(final BoardMemento bm) {
        this.cells = bm.getInnerCells();
        this.pieces.put(Player.ATTACKER, bm.getInnerAttackerPieces());
        this.pieces.put(Player.DEFENDER, bm.getInnerDefenderPieces());
        this.currentPos = bm.getInnerCurrentPos();
        this.slidersEntities = bm.getInnerSlidersEntities();
        bm.getCellsMemento().forEach(c -> c.restore());
        bm.getPiecesMemento().forEach(p -> p.restore());
    }

}
