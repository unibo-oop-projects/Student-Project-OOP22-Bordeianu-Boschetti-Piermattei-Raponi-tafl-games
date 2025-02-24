package taflgames;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import taflgames.common.code.Position;
import taflgames.model.board.api.Board;
import taflgames.model.board.code.BoardImpl;
import taflgames.model.board.code.EatenImpl;
import taflgames.model.pieces.api.Piece;
import taflgames.model.pieces.code.Archer;
import taflgames.model.pieces.code.BasicPiece;
import taflgames.model.pieces.code.King;
import taflgames.model.pieces.code.Swapper;
import taflgames.common.Player;
import taflgames.model.cell.api.Cell;
import taflgames.model.cell.code.ClassicCell;
import taflgames.model.cell.code.Exit;
import taflgames.model.cell.code.Throne;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import taflgames.model.board.api.Eaten;

/**
 * JUnit tests for {@link Eaten}.
 */
class TestEaten {
    // 
    /* CPD suppressed because tests are naturally repetitive and their purpose
     * should be clear enough.
     */
    private static final int DEFAULT_BOARD_SIZE = 5;

    private static Board boardToCheckEaten;
    private static Eaten eat;
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
        piecesPlayer1.put(new Position(1, 4), new Archer(new Position(1, 4), p1));
        piecesPlayer2.put(new Position(3, 3), new BasicPiece(new Position(3, 3), p2));
        piecesPlayer2.put(new Position(3, 2), new Archer(new Position(3, 2), p2));
        piecesPlayer2.put(new Position(4, 0), new King(new Position(3, 2)));

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
        cells.get(new Position(1, 4)).setFree(false);
        cells.get(new Position(3, 2)).setFree(false);
        cells.get(new Position(4, 0)).setFree(false);
        boardToCheckEaten = new BoardImpl(pieces, cells, DEFAULT_BOARD_SIZE);
        eat = new EatenImpl((BoardImpl) boardToCheckEaten);
    }

    /**
     * Test the trimming of pieces' hitbox.
     */
    @Test 
    void trimHitbox() {
        Set<Position> expectedHitbox = new HashSet<>();
        expectedHitbox.add(new Position(1, 0));
        expectedHitbox.add(new Position(0, 1));
        /*BasicPiece adjacent to one of the board's boarder*/
        assertEquals(expectedHitbox, eat.trimHitbox(new BasicPiece(new Position(0, 0), p1), pieces, cells, DEFAULT_BOARD_SIZE));

        expectedHitbox = new HashSet<>();
        expectedHitbox.add(new Position(3, 4));
        expectedHitbox.add(new Position(2, 3));
        expectedHitbox.add(new Position(4, 3));
        /*BasicPiece at the center of the board with a piece of the same player adjacent*/
        assertEquals(expectedHitbox, eat.trimHitbox(new BasicPiece(new Position(3, 3), p2), pieces, cells, DEFAULT_BOARD_SIZE));

        expectedHitbox = new HashSet<>();

        expectedHitbox.add(new Position(3, 0));
        expectedHitbox.add(new Position(3, 1));
        expectedHitbox.add(new Position(0, 2));
        expectedHitbox.add(new Position(1, 2));
        expectedHitbox.add(new Position(2, 2));
        expectedHitbox.add(new Position(4, 2));
        /*Archer at the center of the board with a piece of the same player adjacent*/
        assertEquals(expectedHitbox, eat.trimHitbox(new Archer(new Position(3, 2), p2), pieces, cells, DEFAULT_BOARD_SIZE));

        expectedHitbox = new HashSet<>();
        expectedHitbox.add(new Position(0, 4));
        expectedHitbox.add(new Position(1, 3));
        expectedHitbox.add(new Position(1, 2));
        expectedHitbox.add(new Position(1, 1));
        expectedHitbox.add(new Position(2, 4));
        expectedHitbox.add(new Position(3, 4));
        expectedHitbox.add(new Position(4, 4));
        /*Archer adjacent to one of the board's boarder*/
        assertEquals(expectedHitbox, eat.trimHitbox(new Archer(new Position(1, 4), p1), pieces, cells, DEFAULT_BOARD_SIZE));

        /*King */
        expectedHitbox = new HashSet<>();
        assertEquals(expectedHitbox, eat.trimHitbox(new King(new Position(4, 0)), pieces, cells, DEFAULT_BOARD_SIZE));
    }

    /**
     * Test the search for enemies that a piece threatens.
     */
    @Test
    void testGetThreatenedPos() {
        Set<Position> hitbox = eat.trimHitbox(new Archer(new Position(1, 4), p1), pieces, cells, DEFAULT_BOARD_SIZE);
        List<Piece> enemies = new ArrayList<>();
        assertEquals(enemies, eat.getThreatenedPos(hitbox, pieces, new Archer(new Position(1, 4), p1)));

        hitbox = eat.trimHitbox(new BasicPiece(new Position(3, 3), p2), pieces, cells, DEFAULT_BOARD_SIZE);
        enemies = new ArrayList<>();
        assertEquals(enemies, eat.getThreatenedPos(hitbox, pieces, new BasicPiece(new Position(3, 3), p2)));

        /*Initialize a new board */
        final Eaten eat;
        final Board secondBoard;
        final Map<Player, Map<Position, Piece>> pieces = new HashMap<>();
        final Map<Position, Cell> cells = new HashMap<>();
        final Player p1 = Player.ATTACKER;
        final Player p2 = Player.DEFENDER;

        final Map<Position, Piece> piecesPlayer1 = new HashMap<>();
        final Map<Position, Piece> piecesPlayer2 = new HashMap<>();
        piecesPlayer1.put(new Position(1, 1), new BasicPiece(new Position(1, 1), p1));
        piecesPlayer1.put(new Position(4, 1), new Archer(new Position(4, 1), p1));
        piecesPlayer2.put(new Position(1, 0), new BasicPiece(new Position(1, 0), p2));
        piecesPlayer2.put(new Position(1, 2), new BasicPiece(new Position(1, 2), p2));
        piecesPlayer2.put(new Position(4, 4), new BasicPiece(new Position(4, 4), p2));
        piecesPlayer2.put(new Position(0, 1), new King(new Position(0, 1)));

        pieces.put(p1, piecesPlayer1);
        pieces.put(p2, piecesPlayer2);

        for (int i = 0; i < DEFAULT_BOARD_SIZE; i++) {
            for (int j = 0; j < DEFAULT_BOARD_SIZE; j++) {
                cells.put(new Position(i, j), new ClassicCell());
                cells.get(new Position(i, j)).setFree(true);
            }
        }
        cells.get(new Position(1, 1)).setFree(false);
        cells.get(new Position(4, 1)).setFree(false);
        cells.get(new Position(1, 0)).setFree(false);
        cells.get(new Position(1, 2)).setFree(false);
        cells.get(new Position(4, 4)).setFree(false);
        cells.get(new Position(0, 1)).setFree(false);

        secondBoard = new BoardImpl(pieces, cells, DEFAULT_BOARD_SIZE);
        eat = new EatenImpl(secondBoard);

        /*BasicPiece surrounded by two enemies, one one above and one below */
        hitbox = eat.trimHitbox(new BasicPiece(new Position(1, 1), p1), pieces, cells, DEFAULT_BOARD_SIZE);
        enemies = new ArrayList<>();
        enemies.add(new BasicPiece(new Position(1, 0), p2));
        enemies.add(new BasicPiece(new Position(1, 2), p2));
        enemies.add(new King(new Position(0, 1)));
        assertEquals(enemies, eat.getThreatenedPos(hitbox, pieces, new BasicPiece(new Position(1, 1), p1)));

        /*Archer with an enemy on one of his hitbox's position */
        hitbox = eat.trimHitbox(new Archer(new Position(4, 1), p1), pieces, cells, DEFAULT_BOARD_SIZE);
        enemies = new ArrayList<>();
        enemies.add(new BasicPiece(new Position(4, 4), p2));
        assertEquals(enemies, eat.getThreatenedPos(hitbox, pieces, new Archer(new Position(4, 1), p1)));

        /*BasicPiece adjacent to the board's boarder with an enemy on one of his hitbox's position */
        hitbox = eat.trimHitbox(new BasicPiece(new Position(1, 0), p2), pieces, cells, DEFAULT_BOARD_SIZE);
        enemies = new ArrayList<>();
        enemies.add(new BasicPiece(new Position(1, 1), p1));
        assertEquals(enemies, eat.getThreatenedPos(hitbox, pieces, new BasicPiece(new Position(1, 0), p2)));

        /*King with an enemy on his side */
        hitbox = eat.trimHitbox(new King(new Position(0, 1)), pieces, cells, DEFAULT_BOARD_SIZE);
        enemies = new ArrayList<>();
        assertEquals(enemies, eat.getThreatenedPos(hitbox, pieces, new King(new Position(0, 1))));
    }

    /**
     * Test the search for pieces of the same player as the last piece moved 
     * that threaten the same enemies that the last piece moved threaten.
     */
    @Test 
    void testCheckAllies() {
        /*Initialize a new board */
        final Eaten eat;
        final Board thirdBoard;
        final Map<Player, Map<Position, Piece>> pieces = new HashMap<>();
        final Map<Position, Cell> cells = new HashMap<>();
        final Player p1 = Player.ATTACKER;
        final Player p2 = Player.DEFENDER;

        final Map<Position, Piece> piecesPlayer1 = new HashMap<>();
        final Map<Position, Piece> piecesPlayer2 = new HashMap<>();
        piecesPlayer1.put(new Position(1, 1), new BasicPiece(new Position(1, 1), p1));
        piecesPlayer1.put(new Position(4, 1), new Archer(new Position(4, 1), p1));
        piecesPlayer2.put(new Position(2, 1), new BasicPiece(new Position(2, 1), p2));
        piecesPlayer2.put(new Position(1, 2), new BasicPiece(new Position(1, 2), p2));

        pieces.put(p1, piecesPlayer1);
        pieces.put(p2, piecesPlayer2);

        for (int i = 0; i < DEFAULT_BOARD_SIZE; i++) {
            for (int j = 0; j < DEFAULT_BOARD_SIZE; j++) {
                cells.put(new Position(i, j), new ClassicCell());
                cells.get(new Position(i, j)).setFree(true);
            }
        } 
        cells.get(new Position(1, 1)).setFree(false);
        cells.get(new Position(4, 1)).setFree(false);
        cells.get(new Position(2, 1)).setFree(false);
        cells.get(new Position(1, 2)).setFree(false);
 
        thirdBoard = new BoardImpl(pieces, cells, DEFAULT_BOARD_SIZE);
        eat = new EatenImpl(thirdBoard);

        Set<Position> hitbox = eat.trimHitbox(new BasicPiece(new Position(1, 1), p1), pieces, cells, DEFAULT_BOARD_SIZE);
        List<Piece> enemies = eat.getThreatenedPos(hitbox, pieces, new BasicPiece(new Position(1, 1), p1));
        Map<Piece, Set<Piece>> finalmap = new HashMap<>();
        Set<Piece> allies = new HashSet<>();
        allies.add(new Archer(new Position(4, 1), p1));
        allies.add(new BasicPiece(new Position(1, 1), p1));
        finalmap.put(new BasicPiece(new Position(2, 1), p2), allies);

        allies = new HashSet<>();
        allies.add(new BasicPiece(new Position(1, 1), p1));
        finalmap.put(new BasicPiece(new Position(1, 2), p2), allies);

        assertEquals(
            finalmap,
            eat.checkAllies(enemies, pieces, new BasicPiece(new Position(1, 1), p1), cells, DEFAULT_BOARD_SIZE)
        );

        hitbox = eat.trimHitbox(new BasicPiece(new Position(1, 2), p2), pieces, cells, DEFAULT_BOARD_SIZE);
        enemies = eat.getThreatenedPos(hitbox, pieces, new BasicPiece(new Position(1, 2), p2));
        allies = new HashSet<>();
        allies.add(new BasicPiece(new Position(1, 2), p2));
        allies.add(new BasicPiece(new Position(2, 1), p2));
        finalmap = new HashMap<>();
        finalmap.put(new BasicPiece(new Position(1, 1), p1), allies);

        assertEquals(
            finalmap,
            eat.checkAllies(enemies, pieces, new BasicPiece(new Position(1, 2), p2), cells, DEFAULT_BOARD_SIZE)
        );

        /*creating a new asset of the players' pieces */
        /*King on the boarder of the board with 3 enemies around and an Archer of the other player*/
        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(true));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(true));
        piecesPlayer1.clear();
        piecesPlayer2.clear();
        piecesPlayer1.put(new Position(1, 0), new BasicPiece(new Position(1, 0), p1));
        piecesPlayer1.put(new Position(3, 0), new BasicPiece(new Position(3, 0), p1));
        piecesPlayer1.put(new Position(2, 1), new BasicPiece(new Position(2, 1), p1));
        piecesPlayer1.put(new Position(2, 2), new Archer(new Position(2, 2), p1));

        piecesPlayer2.put(new Position(2, 0), new King(new Position(2, 0)));

        pieces.clear();
        pieces.put(p1, piecesPlayer1);
        pieces.put(p2, piecesPlayer2);

        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));

        hitbox = eat.trimHitbox(new BasicPiece(new Position(2, 1), p1), pieces, cells, DEFAULT_BOARD_SIZE);
        enemies = eat.getThreatenedPos(hitbox, pieces, new BasicPiece(new Position(2, 1), p1));
        allies = new HashSet<>();
        allies.add(new BasicPiece(new Position(2, 1), p1));
        allies.add(new BasicPiece(new Position(1, 0), p1));
        allies.add(new BasicPiece(new Position(3, 0), p1));
        finalmap = new HashMap<>();
        finalmap.put(new King(new Position(2, 0)), allies);

        assertEquals(
            finalmap,
            eat.checkAllies(enemies, pieces, new BasicPiece(new Position(2, 1), p1), cells, DEFAULT_BOARD_SIZE)
        );

        /*creating a new asset of the players' pieces */
        /*BasicPiece between a King and a BasicPiece */
        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(true));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(true));
        piecesPlayer1.clear();
        piecesPlayer2.clear();
        piecesPlayer1.put(new Position(2, 0), new BasicPiece(new Position(2, 0), p1));
        piecesPlayer2.put(new Position(1, 0), new BasicPiece(new Position(1, 0), p2));
        piecesPlayer2.put(new Position(3, 0), new King(new Position(3, 0)));

        pieces.clear();
        pieces.put(p1, piecesPlayer1);
        pieces.put(p2, piecesPlayer2);

        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));

        hitbox = eat.trimHitbox(new BasicPiece(new Position(1, 0), p2), pieces, cells, DEFAULT_BOARD_SIZE);
        enemies = eat.getThreatenedPos(hitbox, pieces, new BasicPiece(new Position(1, 0), p2));
        allies = new HashSet<>();
        allies.add(new BasicPiece(new Position(1, 0), p2));
        finalmap = new HashMap<>();
        finalmap.put(new BasicPiece(new Position(2, 0), p1), allies);
        assertEquals(
            finalmap,
            eat.checkAllies(enemies, pieces, new BasicPiece(new Position(1, 0), p2), cells, DEFAULT_BOARD_SIZE)
        );
    }

    @Test
    void testNotifyAllThreatened() {
        /*Initialize a new board */
        final Board fourthBoard;
        final Eaten eat;
        final Map<Player, Map<Position, Piece>> pieces = new HashMap<>();
        final Map<Position, Cell> cells = new HashMap<>();
        final Player p1 = Player.ATTACKER;
        final Player p2 = Player.DEFENDER;

        final Map<Position, Piece> piecesPlayer1 = new HashMap<>();
        final Map<Position, Piece> piecesPlayer2 = new HashMap<>();
        piecesPlayer1.put(new Position(1, 1), new BasicPiece(new Position(1, 1), p1));
        piecesPlayer1.put(new Position(4, 1), new Archer(new Position(4, 1), p1));
        piecesPlayer1.put(new Position(1, 3), new BasicPiece(new Position(1, 3), p1));
        piecesPlayer2.put(new Position(2, 1), new BasicPiece(new Position(2, 1), p2));
        piecesPlayer2.put(new Position(1, 2), new BasicPiece(new Position(1, 2), p2));

        pieces.put(p1, piecesPlayer1);
        pieces.put(p2, piecesPlayer2);

        for (int i = 0; i < DEFAULT_BOARD_SIZE; i++) {
            for (int j = 0; j < DEFAULT_BOARD_SIZE; j++) {
                cells.put(new Position(i, j), new ClassicCell());
                cells.get(new Position(i, j)).setFree(true);
            }
        } 
        cells.get(new Position(1, 1)).setFree(false);
        cells.get(new Position(4, 1)).setFree(false);
        cells.get(new Position(1, 3)).setFree(false);
        cells.get(new Position(2, 1)).setFree(false);
        cells.get(new Position(1, 2)).setFree(false);

        fourthBoard = new BoardImpl(pieces, cells, DEFAULT_BOARD_SIZE);
        eat = new EatenImpl(fourthBoard);

        Set<Position> hitbox = eat.trimHitbox(new BasicPiece(new Position(1, 1), p1), pieces, cells, DEFAULT_BOARD_SIZE);
        List<Piece> enemies = eat.getThreatenedPos(hitbox, pieces, new BasicPiece(new Position(1, 1), p1));
        Map<Piece, Set<Piece>> finalmap = eat.checkAllies(
                enemies,
                pieces,
                new BasicPiece(new Position(1, 1), p1),
                cells,
                DEFAULT_BOARD_SIZE
            );
        eat.notifyAllThreatened(finalmap, new BasicPiece(new Position(1, 1), p1), cells, pieces, false);
        assertTrue(cells.get(new Position(2, 1)).isFree());
        assertTrue(cells.get(new Position(1, 2)).isFree());
        assertFalse(pieces.get(p2).containsKey(new Position(2, 1)));
        assertFalse(pieces.get(p2).containsKey(new Position(1, 2)));

        /*creating a new map*/
        /*king surrounde by 4 enemies, it dies*/
        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(true));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(true));
        piecesPlayer1.clear();
        piecesPlayer2.clear();
        piecesPlayer1.put(new Position(2, 0), new BasicPiece(new Position(2, 0), p1));
        piecesPlayer1.put(new Position(1, 1), new BasicPiece(new Position(1, 1), p1));
        piecesPlayer1.put(new Position(3, 1), new BasicPiece(new Position(3, 1), p1));
        piecesPlayer1.put(new Position(2, 2), new BasicPiece(new Position(2, 2), p1));

        piecesPlayer2.put(new Position(2, 1), new King(new Position(2, 1)));

        pieces.clear();
        pieces.put(p1, piecesPlayer1);
        pieces.put(p2, piecesPlayer2);

        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        hitbox = eat.trimHitbox(new BasicPiece(new Position(2, 2), p1), pieces, cells, DEFAULT_BOARD_SIZE);
        enemies = eat.getThreatenedPos(hitbox, pieces, new BasicPiece(new Position(2, 2), p1));
        finalmap =  eat.checkAllies(enemies, pieces, new BasicPiece(new Position(2, 2), p1), cells, DEFAULT_BOARD_SIZE);
        eat.notifyAllThreatened(finalmap, new BasicPiece(new Position(2, 2), p1), cells, pieces, false);
        assertTrue(cells.get(new Position(2, 1)).isFree());
        assertFalse(pieces.get(p2).containsKey(new Position(2, 1)));

        /*creating a new map*/
        /*king surrounde by 3 enemies*/
        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(true));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(true));
        piecesPlayer1.clear();
        piecesPlayer2.clear();
        piecesPlayer1.put(new Position(2, 0), new BasicPiece(new Position(2, 0), p1));
        piecesPlayer1.put(new Position(1, 1), new BasicPiece(new Position(1, 1), p1));
        piecesPlayer1.put(new Position(3, 1), new BasicPiece(new Position(3, 1), p1));

        piecesPlayer2.put(new Position(2, 1), new King(new Position(2, 1)));

        pieces.clear();
        pieces.put(p1, piecesPlayer1);
        pieces.put(p2, piecesPlayer2);

        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        hitbox = eat.trimHitbox(new BasicPiece(new Position(2, 0), p1), pieces, cells, DEFAULT_BOARD_SIZE);
        enemies = eat.getThreatenedPos(hitbox, pieces, new BasicPiece(new Position(2, 0), p1));
        finalmap =  eat.checkAllies(enemies, pieces, new BasicPiece(new Position(2, 0), p1), cells, DEFAULT_BOARD_SIZE);
        eat.notifyAllThreatened(finalmap, new BasicPiece(new Position(2, 0), p1), cells, pieces, false);
        assertTrue(pieces.get(p2).containsKey(new Position(2, 1)));
        assertFalse(cells.get(new Position(2, 1)).isFree());

        /*creating a new map*/
        /*king on the boarder of the map with 3 enemies around */
        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(true));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(true));
        piecesPlayer1.clear();
        piecesPlayer2.clear();
        piecesPlayer1.put(new Position(1, 0), new BasicPiece(new Position(1, 0), p1));
        piecesPlayer1.put(new Position(3, 0), new BasicPiece(new Position(3, 0), p1));
        piecesPlayer1.put(new Position(2, 1), new BasicPiece(new Position(2, 1), p1));

        piecesPlayer2.put(new Position(2, 0), new King(new Position(2, 0)));

        pieces.clear();
        pieces.put(p1, piecesPlayer1);
        pieces.put(p2, piecesPlayer2);

        piecesPlayer1.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        piecesPlayer2.entrySet().stream().forEach(piece -> cells.get(piece.getKey()).setFree(false));
        hitbox = eat.trimHitbox(new BasicPiece(new Position(2, 1), p1), pieces, cells, DEFAULT_BOARD_SIZE);
        enemies = eat.getThreatenedPos(hitbox, pieces, new BasicPiece(new Position(2, 1), p1));
        finalmap =  eat.checkAllies(enemies, pieces, new BasicPiece(new Position(2, 1), p1), cells, DEFAULT_BOARD_SIZE);
        eat.notifyAllThreatened(finalmap, new BasicPiece(new Position(2, 1), p1), cells, pieces, false);
        assertTrue(pieces.get(p2).containsKey(new Position(2, 0)));
        assertFalse(cells.get(new Position(2, 0)).isFree());
    }

    /**
     * Tests if the hitbox of the cells is considered when checking if pieces were eaten.
     */
    @Test
    void testEatenWithCellsHitbox() {
        final Board fifthBoard;
        final Map<Player, Map<Position, Piece>> pieces = new HashMap<>();
        final Map<Position, Cell> cells = new HashMap<>();
        final Player p1 = Player.ATTACKER;
        final Player p2 = Player.DEFENDER;
        final Position thronePos = new Position(2, 2);
        final Position exitPos = new Position(4, 4);

        for (int i = 0; i < DEFAULT_BOARD_SIZE; i++) {
            for (int j = 0; j < DEFAULT_BOARD_SIZE; j++) {
                cells.put(new Position(i, j), new ClassicCell());
            }
        }

        cells.values().forEach(e -> e.setFree(true));
        /* A Throne and an Exit */
        cells.put(thronePos, new Throne());
        cells.put(exitPos, new Exit());

        /*
         * Here follows a representation of the situation described by the test
         * (supposing that the attacker piece ATK was the last moved piece).
         *
         *      4 |_|_|_ATK__|DEF|Exit|
         *      3 |_|_|_DEF__|___|____|
         *      2 |_|_|Throne|___|____|
         *      1 |_|_|______|___|____| 
         *      0 |_|_|______|___|____|
         *         0 1    2    3    4
         */
        final Map<Position, Piece> attackerPieces = new HashMap<>();
        final Position attackerStartingPosition = new Position(1, 4);
        final Position attackerEndingPosition = new Position(2, 4);
        attackerPieces.put(attackerStartingPosition, new Swapper(attackerStartingPosition, p1));

        final Map<Position, Piece> defenderPieces = new HashMap<>();
        final Position defender1Pos = new Position(3, 4);
        final Position defender2Pos = new Position(2, 3);
        defenderPieces.put(defender1Pos, new BasicPiece(defender1Pos, p2));
        defenderPieces.put(defender2Pos, new BasicPiece(defender2Pos, p2));

        pieces.put(Player.ATTACKER, attackerPieces);
        pieces.put(Player.DEFENDER, defenderPieces);

        cells.get(attackerStartingPosition).setFree(false);
        cells.get(defender1Pos).setFree(false);
        cells.get(defender2Pos).setFree(false);

        fifthBoard = new BoardImpl(pieces, cells, DEFAULT_BOARD_SIZE);
        fifthBoard.updatePiecePos(attackerStartingPosition, attackerEndingPosition, Player.ATTACKER);
        fifthBoard.eat();

        assertTrue(cells.get(defender1Pos).isFree());
        assertTrue(cells.get(defender2Pos).isFree());
        assertFalse(cells.get(attackerEndingPosition).isFree());

    }
    // CPD-ON
}
