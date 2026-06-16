package solver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SokobanState {
    public int playerRow;
    public int playerCol;
    public Set<Pos> crates;
    public String moves;

    public SokobanState(int playerRow, int playerCol, Set<Pos> crates, String moves) {
        this.playerRow = playerRow;
        this.playerCol = playerCol;
        this.crates = new HashSet<>(crates);
        this.moves = moves;
    }

    public static class Pos {
        public int r, c;

        public Pos(int r, int c) {
            this.r = r;
            this.c = c;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pos pos = (Pos) o;
            return r == pos.r && c == pos.c;
        }

        @Override
        public int hashCode() {
            return Objects.hash(r, c);
        }

        @Override
        public String toString() {
            return "(" + r + "," + c + ")";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SokobanState that = (SokobanState) o;
        return playerRow == that.playerRow &&
                playerCol == that.playerCol &&
                Objects.equals(crates, that.crates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerRow, playerCol, crates);
    }

    public SokobanState move(char direction, int width, int height, char[][] mapData) {
        int dr = 0, dc = 0;
        if (direction == 'u') dr = -1;
        else if (direction == 'd') dr = 1;
        else if (direction == 'l') dc = -1;
        else if (direction == 'r') dc = 1;
        else return null;

        int nextR = playerRow + dr;
        int nextC = playerCol + dc;

        // Check wall collision
        if (nextR < 0 || nextR >= height || nextC < 0 || nextC >= width || mapData[nextR][nextC] == '#') {
            return null;
        }

        Pos nextPos = new Pos(nextR, nextC);
        if (crates.contains(nextPos)) {
            // Push crate
            int crateNextR = nextR + dr;
            int crateNextC = nextC + dc;

            // Check if crate can be pushed
            if (crateNextR < 0 || crateNextR >= height || crateNextC < 0 || crateNextC >= width ||
                mapData[crateNextR][crateNextC] == '#' || crates.contains(new Pos(crateNextR, crateNextC))) {
                return null;
            }

            Set<Pos> nextCrates = new HashSet<>(crates);
            nextCrates.remove(nextPos);
            nextCrates.add(new Pos(crateNextR, crateNextC));
            return new SokobanState(nextR, nextC, nextCrates, moves + direction);
        } else {
            // Normal move
            return new SokobanState(nextR, nextC, crates, moves + direction);
        }
    }

    public List<SokobanState> getPossibleMoves(int width, int height, char[][] mapData) {
        List<SokobanState> possibleMoves = new ArrayList<>();
        char[] directions = {'u', 'd', 'l', 'r'};

        for (char direction : directions) {
            SokobanState nextState = move(direction, width, height, mapData);
            if (nextState != null) {
                possibleMoves.add(nextState);
            }
        }

        return possibleMoves;
    }

    public boolean isGoal(Set<Pos> targets) {
        return crates.equals(targets);
    }
}
