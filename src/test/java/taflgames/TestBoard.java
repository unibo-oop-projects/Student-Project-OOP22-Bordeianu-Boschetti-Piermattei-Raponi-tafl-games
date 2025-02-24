package taflgames;

import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import taflgames.common.code.Position;
import taflgames.common.code.VectorImpl;
import taflgames.model.board.api.Board;
import taflgames.model.board.code.BoardImpl;
import taflgames.model.pieces.api.Piece;
import taflgames.model.pieces.code.BasicPiece;
import taflgames.model.pieces.code.King;
import taflgames.model.pieces.code.Swapper;
import taflgames.common.Player;
import taflgames.model.cell.api.Cell;
import taflgames.model.cell.code.ClassicCell;
import taflgames.model.cell.code.SliderImpl;
import taflgames.model.cell.code.Throne;

/**
 * JUnit tests for {@link Board}.
 */
class TestBoard {
    // 
    /* CPD suppressed because tests are naturally repetitive and their purpose
     * should be clear enough.
     */
    private static final int DEFAULT_BOARD_SIZE = 5;

    private static Board board;
    private static Map<Player, Map<Position, Piece>> pieces = new HashMap<>();
    private static Map<Position, Cell> cells = new HashMap<>();
    private static Player p1 = Player.ATTACKER;
    private static Player p2 = Player.DEFENDER;

    /**
     * Initializes a board before the first test.
     */
    @BeforeAll
    static void init() {
        final Map<Position, Piece> piecesPlayer1 = new HashMap<>();
        final Map<Position, Piece> piecesPlayer2 = new HashMap<>();
        piecesPlayer1.put(new Position(0, 0), new BasicPiece(new Position(0, 0), p1));
        piecesPlayer2.put(new Position(3, 3), new BasicPiece(new Position(3, 3), p2));
        pieces.put(p1, piecesPlayer1);
        pieces.put(p2, piecesPlayer2);
        for (int i = 0; i < DEFAULT_BOARD_SIZE; i++) {
            for (int j = 0; j < DEFAULT_BOARD_SIZE; j++) {
                cells.put(new Position(i, j), new ClassicCell());
                cells.get(new Position(i, j)).setFree(true);
            }
        }
        cells.get(new Position(0, 0)).setFree(false);
        cells.get(new Position(3, 3)).setFree(false);
        board = new BoardImpl(pieces, cells, DEFAULT_BOARD_SIZE);
    }

    /**
     * Test the starting position of the player.
     */
    @Test
    void testIsStartingPointValid() {
        assertTrue(board.isStartingPointValid(new Position(3, 3), p2));
        assertTrue(board.isStartingPointValid(new Position(0, 0), p1)); 
        assertFalse(board.isStartingPointValid(new Position(3, 3), p1));
        assertFalse(board.isStartingPointValid(new Position(2, 2), p1));
    }

    /**
     * Test the selection of the destination cell.
     */
    @Test
    void testIsDestinationValid() {
        /*Initialize a new board */
        Board board2;
        final Map<Player, Map<Position, Piece>> pieces = new HashMap<>();
        final Map<Position, Cell> cells = new HashMap<>();
        final Player p1 = Player.ATTACKER;
        final Player p2 = Player.DEFENDER;
        final Map<Position, Piece> piecesPlayer1 = new HashMap<>();
        final Map<Position, Piece> piecesPlayer2 = new HashMap<>();
        piecesPlayer1.put(new Position(3, 4), new BasicPiece(new Position(3, 4), p1));
        piecesPlayer1.put(new Position(0, 0), new BasicPiece(new Position(0, 0), p1));
        piecesPlayer1.put(new Position(1, 2), new Swapper(new Position(1, 2), p1));

        piecesPlayer2.put(new Position(3, 1), new BasicPiece(new Position(3, 1), p2));
        piecesPlayer2.put(new Position(3, 3), new Swapper(new Position(3, 3), p2));
        piecesPlayer2.put(new Position(4, 2), new King(new Position(4, 2)));

        pieces.put(p1, piecesPlayer1);
        pieces.put(p2, piecesPlayer2);

        for (int i = 0; i < DEFAULT_BOARD_SIZE; i++) {
            for (int j = 0; j < DEFAULT_BOARD_SIZE; j++) {
                if (i == 3 && j == 0) {
                    cells.put(new Position(i, j), new Throne());
                } else {
                    cells.put(new Position(i, j), new ClassicCell());
                }
                cells.get(new Position(i, j)).setFree(true);
            }
        }
        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));

        board2 = new BoardImpl(pieces, cells, DEFAULT_BOARD_SIZE);

        /*testing the validity of a destination if the starting position is 0,0 */
        assertFalse(board2.isDestinationValid(new Position(0, 0), new Position(3, 0), p1));
        assertTrue(board2.isDestinationValid(new Position(0, 0), new Position(0, 3), p1));
        assertFalse(board2.isDestinationValid(new Position(0, 0), new Position(2, 2), p1));
        assertFalse(board2.isDestinationValid(new Position(0, 0), new Position(1, 2), p1));
        assertFalse(board2.isDestinationValid(new Position(0, 0), new Position(3, 1), p1));

        /*testing the validity of a destination if the starting position is 3,3 */
        assertTrue(board2.isDestinationValid(new Position(3, 3), new Position(3, 4), p2));
        assertTrue(board2.isDestinationValid(new Position(3, 3), new Position(0, 3), p2));
        assertFalse(board2.isDestinationValid(new Position(3, 3), new Position(2, 4), p2));
        assertFalse(board2.isDestinationValid(new Position(3, 3), new Position(3, 0), p2));
        assertFalse(board2.isDestinationValid(new Position(3, 3), new Position(3, 1), p2));

        /*testing the validity of a destination for a swapper*/
        assertFalse(board2.isDestinationValid(new Position(1, 2), new Position(4, 2), p1));
        assertTrue(board2.isDestinationValid(new Position(1, 2), new Position(3, 1), p1));
        assertFalse(board2.isDestinationValid(new Position(1, 2), new Position(3, 4), p1));

        assertTrue(board2.isDestinationValid(new Position(3, 4), new Position(0, 4), p1));
        assertFalse(board2.isDestinationValid(new Position(3, 4), new Position(3, 2), p1));
        assertFalse(board2.isDestinationValid(new Position(3, 1), new Position(3, 4), p2));

        /*testing the validity of a destination if the moving piece is a king*/
        assertFalse(board2.isDestinationValid(new Position(4, 2), new Position(0, 2), p2));
    }

    /**
     * Test the update of the pieces'positions.
     */
    @Test
    void testUpdatePiecePos() {
        /*Initialize a new board */
        Board board3;
        final Map<Player, Map<Position, Piece>> pieces = new HashMap<>();
        final Map<Position, Cell> cells = new HashMap<>();
        final Player p1 = Player.ATTACKER;
        final Player p2 = Player.DEFENDER;

        final Map<Position, Piece> piecesPlayer1 = new HashMap<>();
        final Map<Position, Piece> piecesPlayer2 = new HashMap<>();
        piecesPlayer1.put(new Position(0, 0), new BasicPiece(new Position(0, 0), p1));
        piecesPlayer2.put(new Position(3, 3), new BasicPiece(new Position(3, 3), p2));
        pieces.put(p1, piecesPlayer1);
        pieces.put(p2, piecesPlayer2);

        for (int i = 0; i < DEFAULT_BOARD_SIZE; i++) {
            for (int j = 0; j < DEFAULT_BOARD_SIZE; j++) {
                cells.put(new Position(i, j), new ClassicCell());
                cells.get(new Position(i, j)).setFree(true);
            }
        } 
        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        board3 = new BoardImpl(pieces, cells, DEFAULT_BOARD_SIZE);

        /*test the position's update of a normal piece*/
        board3.updatePiecePos(new Position(0, 0), new Position(1, 1), p1);
        assertTrue(cells.get(new Position(0, 0)).isFree());
        assertFalse(cells.get(new Position(1, 1)).isFree());
        assertTrue(pieces.get(p1).keySet().contains(new Position(1, 1)));
        assertFalse(pieces.get(p1).keySet().contains(new Position(0, 0)));
        assertEquals(new Position(1, 1), pieces.get(p1).get(new Position(1, 1)).getCurrentPosition());

        /*test the position's update of a swapper*/
        piecesPlayer1.put(new Position(0, 3), new Swapper(new Position(0, 3), p1));
        pieces.put(p1, piecesPlayer1);
        cells.get(new Position(0, 3)).setFree(false);
        board3.updatePiecePos(new Position(0, 3), new Position(3, 3), p1);
        assertTrue(pieces.get(p1).keySet().contains(new Position(3, 3)));
        assertTrue(pieces.get(p2).keySet().contains(new Position(0, 3)));
        assertFalse(pieces.get(p2).keySet().contains(new Position(3, 3)));
        assertFalse(pieces.get(p1).keySet().contains(new Position(0, 3)));
        assertEquals(new Position(3, 3), pieces.get(p1).get(new Position(3, 3)).getCurrentPosition());
        assertEquals(new Position(0, 3), pieces.get(p2).get(new Position(0, 3)).getCurrentPosition());

        piecesPlayer2.put(new Position(3, 1), new Swapper(new Position(3, 1), p2));
        pieces.put(p2, piecesPlayer2);
        cells.get(new Position(3, 1)).setFree(false);
        board3.updatePiecePos(new Position(3, 1), new Position(0, 1), p2);
        assertTrue(pieces.get(p2).keySet().contains(new Position(0, 1)));
        assertTrue(cells.get(new Position(3, 1)).isFree());
        assertFalse(cells.get(new Position(0, 1)).isFree());

    }

    /**
     * Test the calculation of the furthest reachable position.
     */
    @Test
    void testgetFurthestReachablePos() {
        /*Initialize a new board */
        Board board1;
        final Map<Player, Map<Position, Piece>> pieces = new HashMap<>();
        final Map<Position, Cell> cells = new HashMap<>();
        final Player p1 = Player.ATTACKER;
        final Player p2 = Player.DEFENDER;
        final Map<Position, Piece> piecesPlayer1 = new HashMap<>();
        final Map<Position, Piece> piecesPlayer2 = new HashMap<>();
        piecesPlayer1.put(new Position(3, 4), new BasicPiece(new Position(3, 4), p1));
        piecesPlayer1.put(new Position(0, 0), new BasicPiece(new Position(0, 0), p1));
        piecesPlayer2.put(new Position(3, 3), new King(new Position(3, 3)));
        pieces.put(p1, piecesPlayer1);
        pieces.put(p2, piecesPlayer2);

        for (int i = 0; i < DEFAULT_BOARD_SIZE; i++) {
            for (int j = 0; j < DEFAULT_BOARD_SIZE; j++) {
                if (i == 3 && j == 0) {
                    cells.put(new Position(3, 0), new Throne());
                } else if (i == 0 && j == 3) {
                    cells.put(new Position(0, 3), new SliderImpl(new Position(0, 3)));
                } else {
                    cells.put(new Position(i, j), new ClassicCell());
                }
                cells.get(new Position(i, j)).setFree(true);
            }
        } 
        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        board1 = new BoardImpl(pieces, cells, DEFAULT_BOARD_SIZE);

        assertEquals(new Position(3, 3), board1.getFurthestReachablePos(new Position(3, 3), new VectorImpl(0, 1)));
        assertEquals(new Position(3, 0), board1.getFurthestReachablePos(new Position(3, 3), new VectorImpl(0, -1))); 
        assertEquals(new Position(4, 3), board1.getFurthestReachablePos(new Position(3, 3), new VectorImpl(1, 0)));
        assertEquals(new Position(0, 3), board1.getFurthestReachablePos(new Position(3, 3), new VectorImpl(-1, 0)));
        assertEquals(new Position(2, 0), board1.getFurthestReachablePos(new Position(0, 0), new VectorImpl(1, 0)));
        assertEquals(new Position(0, 4), board1.getFurthestReachablePos(new Position(0, 0), new VectorImpl(0, 1)));

        /*BasicPiece on a slider and a piece of the other player on the direction 
        in which we are trying to find the furthest position reacheable*/
        piecesPlayer1.put(new Position(0, 3), new Swapper(new Position(0, 3), p1));
        pieces.put(p1, piecesPlayer1);
        cells.get(new Position(0, 3)).setFree(false);
        assertEquals(new Position(2, 3), board1.getFurthestReachablePos(new Position(0, 3), new VectorImpl(1, 0)));
    }

    /**
     * Test the draw of the match.
     */
    @Test
    void testIsDraw() {
        Board board4;
        final Map<Player, Map<Position, Piece>> pieces = new HashMap<>();
        final Map<Position, Cell> cells = new HashMap<>();
        final Player p1 = Player.ATTACKER;
        final Player p2 = Player.DEFENDER;
        final Map<Position, Piece> piecesPlayer1 = new HashMap<>();
        final Map<Position, Piece> piecesPlayer2 = new HashMap<>();
        piecesPlayer1.put(new Position(0, 3), new BasicPiece(new Position(0, 3), p1));
        piecesPlayer1.put(new Position(0, 1), new BasicPiece(new Position(0, 1), p1));
        piecesPlayer1.put(new Position(1, 2), new BasicPiece(new Position(1, 2), p1));
        piecesPlayer2.put(new Position(0, 2), new King(new Position(0, 2)));
        pieces.put(p1, piecesPlayer1);
        pieces.put(p2, piecesPlayer2);

        for (int i = 0; i < DEFAULT_BOARD_SIZE; i++) {
            for (int j = 0; j < DEFAULT_BOARD_SIZE; j++) {
                cells.put(new Position(i, j), new ClassicCell());
                cells.get(new Position(i, j)).setFree(true);
            }
        } 
        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));

        board4 = new BoardImpl(pieces, cells, DEFAULT_BOARD_SIZE);

        /*king trapped */
        assertTrue(board4.isDraw(p2));
        assertTrue(board4.isDraw(p1));

        /*creating a new asset of the board */
        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(true));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(true));
        piecesPlayer1.clear();
        piecesPlayer2.clear();
        piecesPlayer1.put(new Position(2, 2), new BasicPiece(new Position(2, 2), p1));
        piecesPlayer1.put(new Position(1, 3), new BasicPiece(new Position(1, 3), p1));
        piecesPlayer1.put(new Position(3, 3), new BasicPiece(new Position(3, 3), p1));
        piecesPlayer1.put(new Position(0, 4), new BasicPiece(new Position(0, 4), p1));
        piecesPlayer1.put(new Position(4, 4), new BasicPiece(new Position(4, 4), p1));

        piecesPlayer2.put(new Position(2, 3), new King(new Position(2, 3)));
        piecesPlayer2.put(new Position(1, 4), new BasicPiece(new Position(1, 4), p2));
        piecesPlayer2.put(new Position(2, 4), new BasicPiece(new Position(2, 4), p2));
        piecesPlayer2.put(new Position(3, 4), new BasicPiece(new Position(3, 4), p2));

        pieces.clear();
        pieces.put(p1, piecesPlayer1);
        pieces.put(p2, piecesPlayer2);

        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));

        assertTrue(board4.isDraw(p2));
        assertFalse(board4.isDraw(p1));

        /*creating a new asset of the board */
        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(true));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(true));
        piecesPlayer1.clear();
        piecesPlayer2.clear();
        piecesPlayer1.put(new Position(1, 1), new BasicPiece(new Position(1, 1), p1));
        piecesPlayer1.put(new Position(2, 1), new BasicPiece(new Position(2, 1), p1));
        piecesPlayer1.put(new Position(3, 1), new BasicPiece(new Position(3, 1), p1));
        piecesPlayer1.put(new Position(4, 1), new BasicPiece(new Position(4, 1), p1));
        piecesPlayer1.put(new Position(1, 2), new BasicPiece(new Position(1, 2), p1));
        piecesPlayer1.put(new Position(1, 3), new BasicPiece(new Position(1, 3), p1));
        piecesPlayer1.put(new Position(2, 3), new BasicPiece(new Position(2, 3), p1));
        piecesPlayer1.put(new Position(3, 3), new BasicPiece(new Position(3, 3), p1));
        piecesPlayer1.put(new Position(4, 3), new BasicPiece(new Position(4, 3), p1));
        piecesPlayer1.put(new Position(4, 2), new BasicPiece(new Position(4, 2), p1)); 

        piecesPlayer2.put(new Position(2, 2), new King(new Position(2, 2)));
        piecesPlayer2.put(new Position(3, 2), new Swapper(new Position(3, 2), p2));

        pieces.clear();
        pieces.put(p1, piecesPlayer1);
        pieces.put(p2, piecesPlayer2);

        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));

        assertTrue(board4.isDraw(p2));
        assertFalse(board4.isDraw(p1));

        /*creating a new asset of the board */
        /*moved piece, king on the boarder with 3 enemies around */
        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(true));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(true));
        piecesPlayer1.clear();
        piecesPlayer2.clear();
        piecesPlayer1.put(new Position(1, 0), new BasicPiece(new Position(1, 0), p1));
        piecesPlayer1.put(new Position(3, 0), new BasicPiece(new Position(3, 0), p1));
        piecesPlayer1.put(new Position(2, 2), new BasicPiece(new Position(2, 2), p1));

        piecesPlayer2.put(new Position(2, 0), new King(new Position(2, 0)));

        pieces.clear();
        pieces.put(p1, piecesPlayer1);
        pieces.put(p2, piecesPlayer2);

        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));

        board4.updatePiecePos(new Position(2, 2), new Position(2, 1), p1);
        assertTrue(pieces.get(p1).keySet().contains(new Position(2, 1)));
        assertFalse(pieces.get(p1).keySet().contains(new Position(2, 2)));
        assertTrue(cells.get(new Position(2, 2)).isFree());
        assertFalse(cells.get(new Position(2, 1)).isFree());
        board4.eat();
        assertEquals(Optional.empty(), board4.checkForWinningPlayer()); 
        assertTrue(board4.isDraw(p2));
        assertTrue(board4.isDraw(p1));

    }
    // CPD-ON
}
