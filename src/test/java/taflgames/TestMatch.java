package taflgames;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import taflgames.common.Player;
import taflgames.common.code.Position;
import taflgames.controller.SettingsLoader;
import taflgames.controller.SettingsLoaderImpl;
import taflgames.model.board.code.BoardImpl;
import taflgames.model.Match;
import taflgames.model.MatchImpl;
import taflgames.model.builders.CellsCollectionBuilder;
import taflgames.model.builders.CellsCollectionBuilderImpl;
import taflgames.model.builders.PiecesCollectionBuilder;
import taflgames.model.builders.PiecesCollectionBuilderImpl;

/**
 * JUnit tests for {@link Match}.
 */
class TestMatch {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestMatch.class);

    private Match match;

    /**
     * Initializes each test before its execution.
     */
    @BeforeEach
    void init() {
        final SettingsLoader loader = new SettingsLoaderImpl();
        final CellsCollectionBuilder cellsCollBuilder = new CellsCollectionBuilderImpl();
        final PiecesCollectionBuilder piecesCollBuilder = new PiecesCollectionBuilderImpl();
        try {
            loader.loadVariantModeConfig(cellsCollBuilder, piecesCollBuilder);
            final var pieces = piecesCollBuilder.build();
            final var cells = cellsCollBuilder.build();
            final int size = (int) Math.sqrt(cells.size());
            this.match = new MatchImpl(
                new BoardImpl(pieces, cells, size)
            );
        } catch (final IOException ex) {
            LOGGER.error("Cannot read configuration file. {}", ex.getMessage());
            fail();
        }
    }

    // CHECKSTYLE: MagicNumber OFF
    // MagicNumber rule disabled because the numbers in the following code represent coordinates

    /**
     * Test the player turn queue.
     */
    @Test
    void testQueue() {
        final int turns = 4;
        final List<Player> resultingQueue = new LinkedList<>();
        for (int i = 0; i < turns; i++) {
            resultingQueue.add(match.getActivePlayer());
            match.setNextActivePlayer();
        }
        assertEquals(
            List.of(Player.ATTACKER, Player.DEFENDER, Player.ATTACKER, Player.DEFENDER), 
            resultingQueue
        );
    }

    /**
     * Test the selection of the piece to move.
     */
    @Test
    void testPieceSelection() {
        /*
         * Case: the current player does a valid selection.
         * For example, the attacker must be able to select piece initially at position (row=3, col=0).
         */
        Position source = new Position(3, 0);
        assertTrue(match.selectSource(source));
        /*
         * Case: the player does an invalid selection because the chosen piece that does not
         * belong to its team. For example, the attacker cannot chose the piece initially at position
         * (row=3, col=5).
         */
        source = new Position(3, 5);
        assertFalse(match.selectSource(source));
        /*
         * Case: the player does an invalid selection as the chosen cell does not contain a piece.
         * For example, the player cannot choose position (row=1, col=1) for the first move.
         */
        source = new Position(1, 1);
        assertFalse(match.selectSource(source));

        /*
         * Perform the same tests for the defender.
         */
        match.setNextActivePlayer();
        /*
         * For example, the defender can choose the piece initially at position (row=5, col=3).
         */
        source = new Position(5, 3);
        assertTrue(match.selectSource(source));
        /*
         * For example, the defender cannot choose the piece initially at position (row=1, col=5)
         */
        source = new Position(1, 5);
        assertFalse(match.selectSource(source));
        /*
         * For example, the defender cannot choose the initially empty cell at position (row=1, col=1).
         */
        source = new Position(1, 1);
        assertFalse(match.selectSource(source));
    }

    /**
     * Test the selection of the destination cell of the move.
     */
    @Test
    void testDestinationSelection() {
        /*
         * Case: the selected destination is valid because the selected destination cell
         * is empty (assuming that the selected piece is a classic piece).
         * For example, the piece at position (row=3, col=0) can move to position (row=2, col=0).
         */
        Position source = new Position(3, 0);
        Position dest = new Position(2, 0);
        assertTrue(match.selectDestination(source, dest));
        /*
         * Case: the selected destination is invalid because the selected destination cell 
         * is not empty and the selected piece is not a swapper.
         * For example, basic piece at position (row=3, col=0) cannot move to position (row=3, col=5)
         */
        source = new Position(3, 0);
        dest = new Position(3, 5);
        assertFalse(match.selectDestination(source, dest));
        /*
         * Case: the selected destination is invalid because the selected destination is an Exit.
         * For example, piece at position (row=3, col=0) cannot move to position (row=0, col=0).
         */
        source = new Position(3, 0);
        dest = new Position(0, 0);
        assertFalse(match.selectDestination(source, dest));
    }

    /**
     * Test the movement of a piece on the board.
     */
    @Test
    void testMove() {
        /*
         * Case: move of a piece which is not a swapper.
         */
        // The attacker moves the piece at (row=3, col=0) to position (row=3, col=4)
        Position source = new Position(3, 0);
        Position dest = new Position(3, 4);
        assertTrue(match.selectSource(source));
        assertTrue(match.selectDestination(source, dest));
        match.makeMove(source, dest);
        // Now the position (row=3, col=4) should be a valid source
        assertTrue(match.selectSource(dest));
        /*
         * Case: move of a swapper.
         */
        // Move piece at (9, 5) to be able to move the swapper at (10, 5)
        source = new Position(9, 5);
        dest = new Position(9, 4);
        assertTrue(match.selectSource(source));
        assertTrue(match.selectDestination(source, dest));
        match.makeMove(source, dest);
        // Now the swapper should be allowed to move to (7,5), even if a defender's piece is present.
        source = new Position(10, 5);
        dest = new Position(7, 5);
        assertTrue(match.selectSource(source));
        assertTrue(match.selectDestination(source, dest));
        match.makeMove(source, dest);
    }

    /**
     * Test the killing of a piece.
     */
    @Test
    void testPieceKilling() {
        // Attacker moves piece at (row=3, col=0) to (row=3, col=4)
        Position source = new Position(3, 0);
        Position dest = new Position(3, 4);
        assertTrue(match.selectSource(source));
        assertTrue(match.selectDestination(source, dest));
        match.makeMove(source, dest);
        /*
         * Now the defender moves piece at (row=5, col=3) to (row=3, col=3);
         * the attacker's piece at (row=3, col=4) should be killed,
         * because there is another defender's piece at (row=3, col=5).
         */
        match.setNextActivePlayer();
        assertTrue(match.getActivePlayer().equals(Player.DEFENDER));
        source = new Position(5, 3);
        dest = new Position(3, 3);
        assertTrue(match.selectSource(source));
        assertTrue(match.selectDestination(source, dest));
        match.makeMove(source, dest);
        /*
         * The piece at (row=3, col=4) should have been killed and then
         * it should not be selectable.
         */
        match.setNextActivePlayer();
        assertTrue(match.getActivePlayer().equals(Player.ATTACKER));
        source = new Position(3, 4);
        assertFalse(match.selectSource(source));
        /*
         * The defender should also be able to move a piece to the position where the attacker's piece
         * was killed, at (row=3, col=4).
         */
        match.setNextActivePlayer();
        assertTrue(match.getActivePlayer().equals(Player.DEFENDER));
        source = new Position(3, 3);
        dest = new Position(3, 4);
        assertTrue(match.selectSource(source));
        assertTrue(match.selectDestination(source, dest));
        match.makeMove(source, dest);
        assertTrue(match.selectSource(dest));
    }

    // CHECKSTYLE: MagicNumber ON

}
